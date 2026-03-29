package com.example.itemhunt.game;

public enum GameMode {
    COLORS("colors"),
    ENGLISH("name_en"),
    CHINESE("name_zh");
    
    private final String key;
    
    GameMode(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }
    
    public static GameMode fromString(String s) {
        for (GameMode mode : values()) {
            if (mode.getKey().equalsIgnoreCase(s)) {
                return mode;
            }
        }
        return null;
    }
}
