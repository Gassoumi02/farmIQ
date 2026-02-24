package com.farmiq.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ExternalApiService {
    private static final Logger logger = LogManager.getLogger(ExternalApiService.class);

    // Configuration Email API (SendGrid)
    private static final String SENDGRID_API_KEY = "YOUR_SENDGRID_API_KEY";
    private static final String EMAIL_API_URL = "https://api.sendgrid.com/v3/mail/send";

    // Configuration Cloudinary
    private static final String CLOUDINARY_CLOUD_NAME = "YOUR_CLOUD_NAME";
    private static final String CLOUDINARY_API_KEY = "YOUR_API_KEY";
    private static final String CLOUDINARY_API_SECRET = "YOUR_API_SECRET";
    private static final String CLOUDINARY_UPLOAD_URL =
            "https://api.cloudinary.com/v1_1/" + CLOUDINARY_CLOUD_NAME + "/image/upload";

    public boolean sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            logger.info("Envoi email réinitialisation à: {}", toEmail);
            URL url = new URL(EMAIL_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + SENDGRID_API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;
            String jsonBody = String.format(
                "{\"personalizations\":[{\"to\":[{\"email\":\"%s\"}]," +
                "\"subject\":\"Réinitialisation de mot de passe FarmIQ\"}]," +
                "\"from\":{\"email\":\"noreply@farmiq.com\",\"name\":\"FarmIQ\"}," +
                "\"content\":[{\"type\":\"text/html\"," +
                "\"value\":\"<h2>Réinitialisation de mot de passe FarmIQ</h2>" +
                "<p>Cliquez sur le lien ci-dessous :</p><a href='%s'>Réinitialiser</a>" +
                "<p>Ce lien expire dans 24 heures.</p>\"}]}",
                toEmail, resetLink);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes("utf-8"));
            }

            int responseCode = conn.getResponseCode();
            logger.info("Email envoyé. Code réponse: {}", responseCode);
            return responseCode == 202;

        } catch (Exception e) {
            logger.error("Erreur envoi email à: {}", toEmail, e);
            return false;
        }
    }

    public String uploadProfilePhoto(File photoFile) {
        try {
            logger.info("Upload photo: {}", photoFile.getName());
            URL url = new URL(CLOUDINARY_UPLOAD_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String boundary = "===" + System.currentTimeMillis() + "===";
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                      .append(photoFile.getName()).append("\"\r\n");
                writer.append("Content-Type: image/jpeg\r\n\r\n").flush();

                try (FileInputStream fis = new FileInputStream(photoFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.flush();
                }

                writer.append("\r\n").flush();
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"api_key\"\r\n\r\n");
                writer.append(CLOUDINARY_API_KEY).append("\r\n");
                writer.append("--").append(boundary).append("--\r\n").flush();
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line);
                String jsonResponse = response.toString();
                int urlStart = jsonResponse.indexOf("\"secure_url\":\"") + 14;
                int urlEnd = jsonResponse.indexOf("\"", urlStart);
                String photoUrl = jsonResponse.substring(urlStart, urlEnd);
                logger.info("Photo uploadée: {}", photoUrl);
                return photoUrl;
            }

        } catch (Exception e) {
            logger.error("Erreur upload photo: {}", photoFile.getName(), e);
            return null;
        }
    }

    public boolean verifyEmail(String email) {
        logger.debug("Vérification email: {}", email);
        return true;
    }

    public String enhancePrompt(String prompt) {
        try {
            logger.info("Enhancing prompt: {}", prompt);
            // Simulate an API call to an LLM
            Thread.sleep(1000); // Simulate latency
            
            // Simple logic to "enhance" the prompt
            String enhanced = prompt.trim();
            if (!enhanced.isEmpty()) {
                enhanced = "Je souhaite cultiver " + enhanced + " de manière optimale";
            }
            
            logger.info("Enhanced prompt: {}", enhanced);
            return enhanced;
        } catch (Exception e) {
            logger.error("Erreur enhance prompt", e);
            return prompt;
        }
    }
}
