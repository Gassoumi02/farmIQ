package com.farmiq.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service pour récupérer les données météo via l'API Open-Meteo
 */
public class WeatherService {
    
    private static final Logger logger = LogManager.getLogger(WeatherService.class);
    private static WeatherService instance;
    
    private final HttpClient httpClient;
    private final Gson gson;
    private String defaultCity;
    private double defaultLat;
    private double defaultLon;
    
    private WeatherData currentWeather;
    private final ScheduledExecutorService scheduler;
    
    // Classe interne pour les données météo
    public static class WeatherData {
        public double temperature;
        public double windSpeed;
        public int weatherCode;
        public String description;
        public String city;
        public String icon;
        
        public WeatherData(double temperature, double windSpeed, int weatherCode, String city) {
            this.temperature = temperature;
            this.windSpeed = windSpeed;
            this.weatherCode = weatherCode;
            this.city = city;
            this.description = getWeatherDescription(weatherCode);
            this.icon = getWeatherIcon(weatherCode);
        }
        
        private String getWeatherDescription(int code) {
            return switch (code) {
                case 0 -> "Ciel dégagé";
                case 1, 2, 3 -> "Partiellement nuageux";
                case 45, 48 -> "Brouillard";
                case 51, 53, 55 -> "Bruine";
                case 61, 63, 65 -> "Pluie";
                case 71, 73, 75 -> "Neige";
                case 77 -> "Grains de neige";
                case 80, 81, 82 -> "Averses";
                case 85, 86 -> "Averses de neige";
                case 95 -> "Orage";
                case 96, 99 -> "Orage avec grêle";
                default -> "Inconnu";
            };
        }
        
        private String getWeatherIcon(int code) {
            return switch (code) {
                case 0 -> "☀️";
                case 1, 2, 3 -> "⛅";
                case 45, 48 -> "🌫️";
                case 51, 53, 55, 61, 63, 65, 80, 81, 82 -> "🌧️";
                case 71, 73, 75, 77, 85, 86 -> "❄️";
                case 95, 96, 99 -> "⛈️";
                default -> "🌡️";
            };
        }
    }
    
    private WeatherService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Charger les paramètres par défaut depuis config.properties
        Properties config = new Properties();
        try {
            config.load(getClass().getResourceAsStream("/config.properties"));
            this.defaultCity = config.getProperty("weather.default.city", "Tunis");
            this.defaultLat = Double.parseDouble(config.getProperty("weather.default.lat", "36.8065"));
            this.defaultLon = Double.parseDouble(config.getProperty("weather.default.lon", "10.1815"));
        } catch (Exception e) {
            logger.warn("Impossible de charger config.properties, utilisation des valeurs par défaut", e);
            this.defaultCity = "Tunis";
            this.defaultLat = 36.8065;
            this.defaultLon = 10.1815;
        }
        
        // Charger la météo au démarrage
        refreshWeather();
        
        // Rafraîchir toutes les 30 minutes
        scheduler.scheduleAtFixedRate(this::refreshWeather, 30, 30, TimeUnit.MINUTES);
    }
    
    public static synchronized WeatherService getInstance() {
        if (instance == null) {
            instance = new WeatherService();
        }
        return instance;
    }
    
    /**
     * Récupère la météo actuelle de manière synchrone
     */
    public WeatherData getCurrentWeather() {
        if (currentWeather == null) {
            refreshWeather();
        }
        return currentWeather;
    }
    
    /**
     * Récupère la météo pour une ville spécifique
     */
    public WeatherData getWeatherForCity(String city, double lat, double lon) {
        try {
            String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current_weather=true",
                lat, lon
            );
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                JsonObject currentWeather = json.getAsJsonObject("current_weather");
                
                double temperature = currentWeather.get("temperature").getAsDouble();
                double windSpeed = currentWeather.get("windspeed").getAsDouble();
                int weatherCode = currentWeather.get("weathercode").getAsInt();
                
                return new WeatherData(temperature, windSpeed, weatherCode, city);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la météo pour {}: {}", city, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Rafraîchit la météo pour la ville par défaut
     */
    public void refreshWeather() {
        try {
            WeatherData data = getWeatherForCity(defaultCity, defaultLat, defaultLon);
            if (data != null) {
                this.currentWeather = data;
                logger.info("Météo mise à jour: {} {}°C, {} km/h - {}", 
                    data.icon, data.temperature, data.windSpeed, data.description);
            }
        } catch (Exception e) {
            logger.error("Erreur lors du rafraîchissement de la météo", e);
        }
    }
    
    /**
     * Récupère la météo de manière asynchrone
     */
    public CompletableFuture<WeatherData> getWeatherAsync() {
        return CompletableFuture.supplyAsync(this::getCurrentWeather);
    }
    
    /**
     * Arrête le service de rafraîchissement automatique
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
    
    /**
     * Met à jour la position par défaut
     */
    public void updateLocation(String city, double lat, double lon) {
        logger.info("Mise à jour de la localisation: {} ({}, {})", city, lat, lon);
        WeatherData data = getWeatherForCity(city, lat, lon);
        if (data != null) {
            this.currentWeather = data;
        }
    }
    
    public String getDefaultCity() {
        return defaultCity;
    }
}
