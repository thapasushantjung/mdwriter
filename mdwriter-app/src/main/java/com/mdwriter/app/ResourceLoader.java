package com.mdwriter.app;

import java.net.URL;

/**
 * Utility class for loading resources (Images, CSS, Fonts) from the classpath.
 * Ensures compatibility across different execution environments (IDE vs Jar).
 */
public class ResourceLoader {
    
    /**
     * Loads a resource and returns its external form URL string.
     * @param path Path to the resource (e.g., "/fonts/font.ttf")
     * @return The string representation of the URL, or null if not found.
     */
    public static String load(String path) {
        URL url = ResourceLoader.class.getResource(path);
        if (url == null) {
            System.err.println("Could not find resource: " + path);
            return null;
        }
        return url.toExternalForm();
    }
    
    public static String getFontUrl(String fontName) {
        return load("/fonts/" + fontName);
    }
    
    public static String getImageUrl(String imageName) {
        return load("/images/" + imageName);
    }
    /**
     * Loads a resource and returns its content as a String.
     * @param path Path to the resource.
     * @return The text content of the resource, or empty string if not found.
     */
    public static String loadContent(String path) {
        try (java.io.InputStream is = ResourceLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Could not find resource content: " + path);
                return "";
            }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
