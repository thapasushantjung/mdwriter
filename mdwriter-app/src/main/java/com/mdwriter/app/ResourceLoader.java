package com.mdwriter.app;

import java.net.URL;

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
}
