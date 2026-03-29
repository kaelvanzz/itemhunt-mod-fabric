package com.example.itemhunt;

import com.example.itemhunt.command.ItemHuntCommand;
import com.example.itemhunt.game.ItemHuntGame;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ItemHuntMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        System.out.println("[ItemHunt] Fabric mod loaded!");
        
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ItemHuntCommand.register(dispatcher);
        });
        
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ItemHuntGame.INSTANCE.onServerTick(server);
        });
    }
    
    public static void broadcastToAll(MinecraftServer server, String message) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(Text.literal(message), false);
        }
    }
    
    public static void broadcastToAllActionbar(MinecraftServer server, String message) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(Text.literal(message), true);
        }
    }
}