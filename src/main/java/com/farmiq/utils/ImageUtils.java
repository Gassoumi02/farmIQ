package com.farmiq.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ImageUtils {
    private static final Logger logger = LogManager.getLogger(ImageUtils.class);
    
    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 600;
    private static final String LISTINGS_DIR = "images/listings";
    private static final String AVATARS_DIR = "images/avatars";
    
    static {
        createDirectories();
    }
    
    private static void createDirectories() {
        try {
            Files.createDirectories(Paths.get(LISTINGS_DIR));
            Files.createDirectories(Paths.get(AVATARS_DIR));
        } catch (IOException e) {
            logger.error("Erreur création répertoires images", e);
        }
    }
    
    public static String saveListingImage(InputStream inputStream, String extension) throws IOException {
        return saveImage(inputStream, extension, LISTINGS_DIR);
    }
    
    public static String saveAvatar(InputStream inputStream, String extension) throws IOException {
        return saveImage(inputStream, extension, AVATARS_DIR);
    }
    
    private static String saveImage(InputStream inputStream, String directory, String extension) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);
        if (originalImage == null) {
            throw new IOException("Format d'image invalide");
        }
        
        BufferedImage resizedImage = originalImage;
        if (originalImage.getWidth() > MAX_WIDTH || originalImage.getHeight() > MAX_HEIGHT) {
            int newWidth = Math.min(originalImage.getWidth(), MAX_WIDTH);
            int newHeight = (int) ((double) originalImage.getHeight() * newWidth / originalImage.getWidth());
            if (newHeight > MAX_HEIGHT) {
                newHeight = MAX_HEIGHT;
                newWidth = (int) ((double) originalImage.getWidth() * newHeight / originalImage.getHeight());
            }
            resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();
        }
        
        String filename = UUID.randomUUID().toString() + "." + extension;
        String filepath = directory + "/" + filename;
        
        File outputFile = new File(filepath);
        ImageIO.write(resizedImage, extension, outputFile);
        
        logger.info("Image enregistrée: " + filepath);
        return filepath;
    }
    
    public static boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }
        try {
            File file = new File(imagePath);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            logger.error("Erreur suppression image: " + imagePath, e);
        }
        return false;
    }
    
    public static String getPlaceholderUrl() {
        return "images/placeholder.png";
    }
}
