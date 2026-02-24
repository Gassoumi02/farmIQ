package com.farmiq.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyUtils {
    
    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.FRENCH);
    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.00", SYMBOLS);
    
    private CurrencyUtils() {
    }
    
    public static String format(double amount) {
        return FORMAT.format(amount) + " TND";
    }
    
    public static String formatSimple(double amount) {
        return FORMAT.format(amount);
    }
    
    public static double parse(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        try {
            String cleaned = value.replaceAll("[^0-9.,]", "").replace(",", ".");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
