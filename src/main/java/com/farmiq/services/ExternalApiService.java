package com.farmiq.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;
import java.util.UUID;

/**
 * Service for external integrations using free APIs and local resources.
 * <ul>
 *   <li>Email: JavaMail SMTP (free with any SMTP provider, e.g. Gmail)</li>
 *   <li>Images: Local file storage under the configured images directory</li>
 * </ul>
 * All credentials are loaded from config.properties with environment variable overrides.
 */
public class ExternalApiService {
    private static final Logger logger = LogManager.getLogger(ExternalApiService.class);

    private final String mailHost;
    private final String mailPort;
    private final String mailUser;
    private final String mailPassword;
    private final String mailFrom;
    private final String imagesDir;

    public ExternalApiService() {
        Properties config = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            if (input != null) {
                config.load(input);
            }
        } catch (IOException e) {
            logger.warn("Could not load config.properties for external API settings", e);
        }

        // Environment variables take precedence over config.properties
        this.mailHost = resolveConfig("MAIL_HOST", "mail.host", config, "smtp.gmail.com");
        this.mailPort = resolveConfig("MAIL_PORT", "mail.port", config, "587");
        this.mailUser = resolveConfig("MAIL_USER", "mail.user", config, "");
        this.mailPassword = resolveConfig("MAIL_PASSWORD", "mail.password", config, "");
        this.mailFrom = resolveConfig("MAIL_FROM", "mail.from", config, "noreply@farmiq.com");
        this.imagesDir = resolveConfig("APP_IMAGES_DIR", "app.images.dir", config, "./images");
    }

    /**
     * Resolves a configuration value: environment variable > config property > default.
     */
    private static String resolveConfig(String envVar, String propKey, Properties config, String defaultValue) {
        String envValue = System.getenv(envVar);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return config.getProperty(propKey, defaultValue);
    }

    /**
     * Sends a password reset email using JavaMail SMTP (free).
     */
    public boolean sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            logger.info("Envoi email réinitialisation à: {}", toEmail);

            if (mailUser.isEmpty() || mailPassword.isEmpty()) {
                logger.warn("SMTP credentials not configured — email not sent. " +
                        "Set mail.user/mail.password in config.properties or MAIL_USER/MAIL_PASSWORD env vars.");
                return false;
            }

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", mailHost);
            props.put("mail.smtp.port", mailPort);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailUser, mailPassword);
                }
            });

            String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;
            String htmlBody = "<h2>Réinitialisation de mot de passe FarmIQ</h2>"
                    + "<p>Cliquez sur le lien ci-dessous :</p>"
                    + "<a href='" + resetLink + "'>Réinitialiser</a>"
                    + "<p>Ce lien expire dans 24 heures.</p>";

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Réinitialisation de mot de passe FarmIQ");
            message.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(message);
            logger.info("Email envoyé avec succès à: {}", toEmail);
            return true;

        } catch (MessagingException e) {
            logger.error("Erreur envoi email à: {}", toEmail, e);
            return false;
        }
    }

    /**
     * Stores a profile photo to local file storage (free, no external API needed).
     * Returns the relative path to the stored image.
     */
    public String uploadProfilePhoto(File photoFile) {
        try {
            logger.info("Upload photo (local): {}", photoFile.getName());

            Path uploadDir = Paths.get(imagesDir, "profiles");
            Files.createDirectories(uploadDir);

            String extension = getFileExtension(photoFile.getName());
            String uniqueName = UUID.randomUUID() + extension;
            Path destination = uploadDir.resolve(uniqueName);

            Files.copy(photoFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = "images/profiles/" + uniqueName;
            logger.info("Photo stockée localement: {}", relativePath);
            return relativePath;

        } catch (IOException e) {
            logger.error("Erreur stockage photo: {}", photoFile.getName(), e);
            return null;
        }
    }

    /**
     * Basic email format verification (no external API needed).
     */
    public boolean verifyEmail(String email) {
        logger.debug("Vérification email: {}", email);
        return email != null && email.matches("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
    }

    public String enhancePrompt(String prompt) {
        try {
            logger.info("Enhancing prompt: {}", prompt);

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

    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0) {
            return filename.substring(dotIndex);
        }
        return ".jpg";
    }
}
