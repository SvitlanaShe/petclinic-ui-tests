package com.adesso.qa.config;

public class FrameworkConfig {

    public static String getBrowser() {
        return System.getProperty("browser", "chrome").toLowerCase();
    }

    public static String getBaseUrl() {
        return System.getProperty("baseUrl", "http://localhost:8080");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(System.getProperty("headless", "false"));
    }
}