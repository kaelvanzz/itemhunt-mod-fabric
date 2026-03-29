package com.example.itemhunt.game;

import net.minecraft.entity.player.PlayerEntity;

public class PlayerScore {
    private final PlayerEntity player;
    private int score;

    public PlayerScore(PlayerEntity player) {
        this.player = player;
        this.score = 0;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int amount) {
        this.score += amount;
    }

    public void reset() {
        this.score = 0;
    }
}
