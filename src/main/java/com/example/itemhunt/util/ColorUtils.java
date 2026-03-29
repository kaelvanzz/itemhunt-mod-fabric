package com.example.itemhunt.util;

import java.util.Set;
import java.util.HashSet;

public class ColorUtils {
    private static final Set<String> COLOR_WORDS = Set.of(
        "red", "green", "blue", "yellow", "purple", "pink", "gray", "brown",
        "black", "white", "orange", "cyan", "magenta", "teal", "lavender"
    );

    public static boolean containsColor(String name, String targetColor) {
        if (name == null || targetColor == null) return false;
        String lowerName = name.toLowerCase();
        String lowerColor = targetColor.toLowerCase();
        
        // Check if the target color is a valid color word
        if (!COLOR_WORDS.contains(lowerColor)) return false;
        
        // Check if the item name contains the target color
        return lowerName.contains(lowerColor);
    }

    public static boolean isColorItem(String name) {
        return COLOR_WORDS.stream().anyMatch(color ->
            name.toLowerCase().contains(color)
        );
    }
}
