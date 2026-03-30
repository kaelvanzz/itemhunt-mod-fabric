package com.example.itemhunt.game;

import com.example.itemhunt.ItemHuntMod;
import com.example.itemhunt.util.ColorUtils;
import com.example.itemhunt.util.ItemDataManager;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;

import java.util.*;

import static com.example.itemhunt.game.GameMode.*;

public class ItemHuntGame {
    
    public static final ItemHuntGame INSTANCE = new ItemHuntGame();

    private GameMode currentMode = null;
    private int roundTime = 0;
    private int roundCount = 0;
    private int currentRound = 0;
    private boolean gameRunning = false;
    private boolean roundActive = false;
    private boolean countdownActive = false;
    private boolean inDelay = false;
    private int delayTicks = 0;

    private Random random = new Random();
    private char currentLetter = 'A';
    private String currentPinyin = "a";
    private Map<ServerPlayerEntity, PlayerScore> playerScores = new HashMap<>();

    private long roundStartTick = 0;
    private long currentServerTick = 0;
    private long countdownStartTick = 0;
    private int lastCountdownSecond = -1;
    
    private ServerBossBar bossBar;
    private int lastAnnouncedSecond = 0;
    private long roundStartTime = 0;
    
    private ScoreboardObjective sidebarObjective;
    
    // Track previous fake score names to properly remove them
    private String prevRoundLine = "";
    private String prevModeLine = "";
    private String prevTargetLine = "";
    private List<String> prevPlayerLines = new ArrayList<>();

    private final Set<String> englishLetters = Set.of("A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","W","Y");
    private final Set<String> chinesePinyinLetters = Set.of("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","w","x","y","z");
    
    // Custom color for brown (hex: #894933)
    private static final int BROWN_COLOR = 0x894933;
    
    private static final Map<String, Formatting> COLOR_FORMATTING;
    static {
        Map<String, Formatting> map = new HashMap<>();
        map.put("red", Formatting.RED);
        map.put("green", Formatting.GREEN);
        map.put("blue", Formatting.BLUE);
        map.put("yellow", Formatting.YELLOW);
        map.put("purple", Formatting.DARK_PURPLE);
        map.put("pink", Formatting.LIGHT_PURPLE);
        map.put("gray", Formatting.GRAY);
        map.put("brown", Formatting.DARK_RED); // Fallback, will use custom color
        map.put("black", Formatting.BLACK);
        map.put("white", Formatting.WHITE);
        map.put("orange", Formatting.GOLD);
        map.put("cyan", Formatting.AQUA);
        COLOR_FORMATTING = Collections.unmodifiableMap(map);
    }

    public boolean isGameModeSet() {
        return currentMode != null;
    }

    public void setGameMode(GameMode mode) {
        this.currentMode = mode;
    }

    public void setRoundTime(int seconds) {
        this.roundTime = seconds;
    }

    public void setRoundCount(int count) {
        this.roundCount = count;
    }

    public void startGame(MinecraftServer server) {
        if (!isGameModeSet()) {
            System.out.println("[ItemHunt] Game mode not set");
            return;
        }
        if (gameRunning) {
            System.out.println("[ItemHunt] Game already running");
            return;
        }

        playerScores.clear();
        gameRunning = true;
        currentRound = 0;
        
        if (roundTime > 0) {
            bossBar = new ServerBossBar(Text.literal("ItemHunt"), BossBar.Color.GREEN, BossBar.Style.PROGRESS);
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                bossBar.addPlayer(player);
            }
        }
        
        createSidebarObjective(server);
        startCountdown(server);
    }

    private void startCountdown(MinecraftServer server) {
        countdownActive = true;
        countdownStartTick = currentServerTick;
        lastCountdownSecond = -1;
        inDelay = false;
    }

    public void endGame(MinecraftServer server) {
        gameRunning = false;
        roundActive = false;
        countdownActive = false;
        inDelay = false;
        
        if (bossBar != null) {
            bossBar.clearPlayers();
            bossBar = null;
        }
        
        removeSidebarObjective(server);
        playSoundToAll(server, "challenge_complete");
        showFinalRanking(server);
    }

    public void showConfig(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal(
            "[ItemHunt] Status:\n" +
            "- Mode: " + currentMode + "\n" +
            "- Round time: " + (roundTime > 0 ? roundTime + "s" : "unlimited") + "\n" +
            "- Rounds: " + (roundCount > 0 ? roundCount : "unlimited")
        ), false);
    }

    private void playSoundToPlayer(ServerPlayerEntity player, String soundName) {
        try {
            Identifier soundId = Identifier.of("itemhunt", soundName);
            SoundEvent sound = SoundEvent.of(soundId);
            player.playSoundToPlayer(sound, SoundCategory.MASTER, 1.0f, 1.0f);
        } catch (Exception e) {
            System.out.println("[ItemHunt] Failed to play sound: " + soundName + " - " + e.getMessage());
        }
    }

    private void playSoundToAll(MinecraftServer server, String soundName) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            playSoundToPlayer(player, soundName);
        }
    }

    private Formatting getColorFormatting(String color) {
        return COLOR_FORMATTING.getOrDefault(color.toLowerCase(), Formatting.WHITE);
    }
    
    /**
     * Get styled text for a color name, using custom color for brown
     */
    private Text getColoredText(String colorName, String displayText) {
        if ("brown".equalsIgnoreCase(colorName)) {
            return Text.literal(displayText).styled(style -> style.withColor(TextColor.fromRgb(BROWN_COLOR)));
        } else {
            return Text.literal(displayText).formatted(getColorFormatting(colorName));
        }
    }

    private void sendTitle(ServerPlayerEntity player, Text title, Text subtitle, int fadeIn, int stay, int fadeOut) {
        ServerPlayNetworkHandler handler = player.networkHandler;
        handler.sendPacket(new ClearTitleS2CPacket(true));
        handler.sendPacket(new TitleFadeS2CPacket(fadeIn, stay, fadeOut));
        handler.sendPacket(new TitleS2CPacket(title));
        if (subtitle != null) {
            handler.sendPacket(new SubtitleS2CPacket(subtitle));
        }
    }

    private void showRoundTitle(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            Text title;
            
            if (currentMode == COLORS) {
                title = Text.empty()
                    .append(Text.literal("Find item with color: ").formatted(Formatting.WHITE))
                    .append(getColoredText(currentPinyin, currentPinyin.toUpperCase()).copy().formatted(Formatting.BOLD));
            } else if (currentMode == ENGLISH) {
                title = Text.empty()
                    .append(Text.literal("Find item starting with: ").formatted(Formatting.WHITE))
                    .append(Text.literal(String.valueOf(currentLetter)).formatted(Formatting.GOLD, Formatting.UNDERLINE, Formatting.BOLD));
            } else {
                // Chinese mode - display in Chinese
                title = Text.empty()
                    .append(Text.literal("寻找首拼为: ").formatted(Formatting.WHITE))
                    .append(Text.literal(currentPinyin.toUpperCase()).formatted(Formatting.GOLD, Formatting.UNDERLINE, Formatting.BOLD))
                    .append(Text.literal(" 的物品").formatted(Formatting.WHITE));
            }
            
            sendTitle(player, title, null, 10, 60, 10);
        }
    }

    private void showWinnerTitle(MinecraftServer server, ServerPlayerEntity winner, double timeSeconds) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            Text title;
            Text subtitle;
            
            // English mode or Color mode - use English text
            if (currentMode == ENGLISH || currentMode == COLORS) {
                title = Text.empty()
                    .append(Text.literal(winner.getName().getString()).formatted(Formatting.WHITE, Formatting.ITALIC))
                    .append(Text.literal(" found the item!").formatted(Formatting.GREEN));
                
                subtitle = Text.empty()
                    .append(Text.literal("Time: ").formatted(Formatting.YELLOW))
                    .append(Text.literal(String.format("%.1f", timeSeconds)).formatted(Formatting.WHITE))
                    .append(Text.literal(" seconds").formatted(Formatting.YELLOW));
            } else {
                // Chinese mode - use Chinese text
                title = Text.empty()
                    .append(Text.literal(winner.getName().getString()).formatted(Formatting.WHITE, Formatting.ITALIC))
                    .append(Text.literal(" 率先找到了指定物品").formatted(Formatting.GREEN));
                
                subtitle = Text.empty()
                    .append(Text.literal("用时 ").formatted(Formatting.YELLOW))
                    .append(Text.literal(String.format("%.1f", timeSeconds)).formatted(Formatting.WHITE))
                    .append(Text.literal(" 秒").formatted(Formatting.YELLOW));
            }
            
            sendTitle(player, title, subtitle, 10, 60, 10);
        }
        playSoundToAll(server, "levelup");
    }

    private void showTimeUpTitle(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            Text title;
            Text subtitle;
            
            // English mode or Color mode - use English text
            if (currentMode == ENGLISH || currentMode == COLORS) {
                title = Text.literal("Time's Up!").formatted(Formatting.RED, Formatting.BOLD);
                subtitle = Text.literal("No player found the item!").formatted(Formatting.RED);
            } else {
                // Chinese mode - use Chinese text
                title = Text.literal("时间结束！").formatted(Formatting.RED, Formatting.BOLD);
                subtitle = Text.literal("没有玩家找到指定物品！").formatted(Formatting.RED);
            }
            
            sendTitle(player, title, subtitle, 10, 60, 10);
        }
        
        int growlNum = random.nextInt(4) + 1;
        playSoundToAll(server, "growl" + growlNum);
    }

    private void showRoundSkippedTitle(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            Text title;
            
            // English mode or Color mode - use English text
            if (currentMode == ENGLISH || currentMode == COLORS) {
                title = Text.literal("Round Skipped!").formatted(Formatting.YELLOW, Formatting.BOLD);
            } else {
                // Chinese mode - use Chinese text
                title = Text.literal("回合已跳过！").formatted(Formatting.YELLOW, Formatting.BOLD);
            }
            
            sendTitle(player, title, null, 10, 40, 10);
        }
        playSoundToAll(server, "pling");
    }

    public void skipRound(MinecraftServer server) {
        if (!gameRunning || !roundActive) {
            return;
        }
        
        showRoundSkippedTitle(server);
        resetAllItems(server);
        roundActive = false;
        updateSidebarDisplay(server);
        startNewRoundAfterDelay(40);
    }

    private void createSidebarObjective(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();
        
        String objectiveName = "itemhunt_sb";
        
        sidebarObjective = scoreboard.getNullableObjective(objectiveName);
        if (sidebarObjective != null) {
            scoreboard.removeObjective(sidebarObjective);
        }
        
        sidebarObjective = scoreboard.addObjective(
            objectiveName,
            ScoreboardCriterion.DUMMY,
            Text.literal("§6§l=== ItemHunt ==="),
            ScoreboardCriterion.RenderType.INTEGER,
            true,
            null
        );
        
        scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, sidebarObjective);
        
        // Reset tracking strings
        prevRoundLine = "";
        prevModeLine = "";
        prevTargetLine = "";
        prevPlayerLines.clear();
        
        updateSidebarDisplay(server);
    }
    
    private void updateSidebarDisplay(MinecraftServer server) {
        if (sidebarObjective == null) return;
        
        ServerScoreboard scoreboard = server.getScoreboard();
        
        // Clear previous lines
        removeScoreLine(scoreboard, prevRoundLine);
        removeScoreLine(scoreboard, prevModeLine);
        removeScoreLine(scoreboard, prevTargetLine);
        for (String line : prevPlayerLines) {
            removeScoreLine(scoreboard, line);
        }
        prevPlayerLines.clear();
        
        // Build new lines
        String modeText = currentMode == COLORS ? "Color" : 
                          currentMode == ENGLISH ? "English" : "Pinyin";
        String roundText = roundCount > 0 ? currentRound + "/" + roundCount : currentRound + "/∞";
        
        // Line 1: Round info
        String roundLine = "§eRound: " + roundText;
        ScoreHolder roundHolder = ScoreHolder.fromName(roundLine);
        scoreboard.getOrCreateScore(roundHolder, sidebarObjective).setScore(15);
        prevRoundLine = roundLine;
        
        // Line 2: Mode info
        String modeLine = "§bMode: " + modeText;
        ScoreHolder modeHolder = ScoreHolder.fromName(modeLine);
        scoreboard.getOrCreateScore(modeHolder, sidebarObjective).setScore(14);
        prevModeLine = modeLine;
        
        // Line 3: Target info (show current target)
        String targetLine = "";
        if (currentMode == COLORS) {
            String colorCode = getColorCode(currentPinyin);
            targetLine = "§fTarget: " + colorCode + currentPinyin.toUpperCase();
        } else if (currentMode == ENGLISH) {
            targetLine = "§fTarget: §6§o" + currentLetter;
        } else {
            targetLine = "§fTarget: §6§o" + currentPinyin.toUpperCase();
        }
        ScoreHolder targetHolder = ScoreHolder.fromName(targetLine);
        scoreboard.getOrCreateScore(targetHolder, sidebarObjective).setScore(13);
        prevTargetLine = targetLine;
        
        // Line 4: Separator
        String sepLine = "§7---------------";
        ScoreHolder sepHolder = ScoreHolder.fromName(sepLine);
        scoreboard.getOrCreateScore(sepHolder, sidebarObjective).setScore(12);
        
        // Player scores (sorted)
        List<ServerPlayerEntity> sorted = new ArrayList<>(playerScores.keySet());
        sorted.sort((a, b) -> Integer.compare(playerScores.get(b).getScore(), playerScores.get(a).getScore()));
        
        int scoreIndex = 11;
        for (int i = 0; i < sorted.size() && scoreIndex > 0; i++) {
            ServerPlayerEntity p = sorted.get(i);
            int score = playerScores.get(p).getScore();
            ScoreAccess playerScoreAccess = scoreboard.getOrCreateScore(p, sidebarObjective);
            playerScoreAccess.setScore(score);
            prevPlayerLines.add(p.getNameForScoreboard());
            scoreIndex--;
        }
    }
    
    private void removeScoreLine(ServerScoreboard scoreboard, String line) {
        if (line == null || line.isEmpty()) return;
        ScoreHolder holder = ScoreHolder.fromName(line);
        scoreboard.removeScore(holder, sidebarObjective);
    }
    
    private String getColorCode(String color) {
        switch (color.toLowerCase()) {
            case "red": return "§c";
            case "green": return "§a";
            case "blue": return "§9";
            case "yellow": return "§e";
            case "purple": return "§5";
            case "pink": return "§d";
            case "gray": return "§7";
            case "brown": return "§x§8§9§4§9§3§3"; // Custom brown color #894933 using JSON color format
            case "black": return "§0";
            case "white": return "§f";
            case "orange": return "§6";
            case "cyan": return "§b";
            default: return "§f";
        }
    }
    
    private void removeSidebarObjective(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();
        
        String objectiveName = "itemhunt_sb";
        ScoreboardObjective objective = scoreboard.getNullableObjective(objectiveName);
        if (objective != null) {
            scoreboard.removeObjective(objective);
        }
        
        sidebarObjective = null;
    }

    private void resetRound(MinecraftServer server) {
        roundStartTick = currentServerTick;
        roundStartTime = currentServerTick;
        lastAnnouncedSecond = 0;

        if (currentMode == COLORS) {
            List<String> colors = Arrays.asList("red", "green", "blue", "yellow", "purple", "pink", "gray", "brown", "black", "white", "orange", "cyan");
            String color = colors.get(random.nextInt(colors.size()));
            this.currentPinyin = color;
        } else if (currentMode == ENGLISH) {
            List<String> letters = new ArrayList<>(englishLetters);
            currentLetter = letters.get(random.nextInt(letters.size())).charAt(0);
        } else if (currentMode == CHINESE) {
            List<String> pinyin = new ArrayList<>(chinesePinyinLetters);
            currentPinyin = pinyin.get(random.nextInt(pinyin.size()));
        }
        
        if (bossBar != null && roundTime > 0) {
            bossBar.setName(Text.literal("Time: " + roundTime + "s").formatted(Formatting.GREEN));
            bossBar.setPercent(1.0f);
        }
    }

    public void onServerTick(MinecraftServer server) {
        currentServerTick++;

        if (inDelay) {
            delayTicks--;
            if (delayTicks <= 0) {
                inDelay = false;
                startNewRound(server);
            }
            return;
        }

        if (countdownActive) {
            long countdownTicks = currentServerTick - countdownStartTick;
            long countdownSeconds = countdownTicks / 20;
            
            if (countdownSeconds >= 0 && countdownSeconds < 3) {
                int currentSecond = (int) countdownSeconds;
                if (currentSecond != lastCountdownSecond) {
                    lastCountdownSecond = currentSecond;
                    playSoundToAll(server, "pling");
                }
            }
            
            if (countdownSeconds >= 3) {
                countdownActive = false;
                roundActive = true;
                currentRound++;
                resetRound(server);
                showRoundTitle(server);
                playSoundToAll(server, "orb");
                updateSidebarDisplay(server);
            }
            return;
        }

        if (!gameRunning || !roundActive) return;

        if (bossBar != null && roundTime > 0) {
            long elapsedTicks = currentServerTick - roundStartTick;
            long elapsedSeconds = elapsedTicks / 20;
            int remainingSeconds = roundTime - (int) elapsedSeconds;
            
            if (remainingSeconds < 0) remainingSeconds = 0;
            
            float progress = (float) remainingSeconds / roundTime;
            bossBar.setPercent(progress);
            bossBar.setName(Text.literal("Time: " + remainingSeconds + "s").formatted(Formatting.GREEN));
            
            if (remainingSeconds <= 10 && remainingSeconds > 0 && remainingSeconds != lastAnnouncedSecond) {
                lastAnnouncedSecond = remainingSeconds;
                playSoundToAll(server, "hat");
            }
            
            if (elapsedSeconds >= roundTime) {
                showTimeUpTitle(server);
                roundActive = false;
                startNewRoundAfterDelay(60);
                return;
            }
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!playerScores.containsKey(player)) {
                playerScores.put(player, new PlayerScore(player));
            }

            if (hasWon(player)) {
                playerScores.get(player).addScore(1);
                double timeSeconds = (currentServerTick - roundStartTime) / 20.0;
                showWinnerTitle(server, player, timeSeconds);
                resetAllItems(server);
                roundActive = false;
                updateSidebarDisplay(server);
                startNewRoundAfterDelay(60);
                return;
            }
        }
    }

    private void startNewRoundAfterDelay(int ticks) {
        delayTicks = ticks;
        inDelay = true;
    }

    private boolean hasWon(ServerPlayerEntity player) {
        ItemDataManager dataManager = ItemDataManager.getInstance();
        
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            // Use registry to get proper item ID (e.g., "minecraft:sea_lantern")
            Identifier itemId = Registries.ITEM.getId(item);
            String itemIdStr = itemId.toString();
            String itemName = itemId.getPath();

            switch (currentMode) {
                case COLORS:
                    // Use ItemDataManager for color matching
                    if (dataManager.matchesColor(itemIdStr, currentPinyin)) {
                        return true;
                    }
                    // Fallback to ColorUtils for compatibility
                    if (ColorUtils.containsColor(item.getTranslationKey(), currentPinyin)) {
                        return true;
                    }
                    break;

                case ENGLISH:
                    // Use ItemDataManager for English letter matching
                    if (dataManager.matchesEnglishLetter(itemIdStr, currentLetter)) {
                        return true;
                    }
                    // Fallback: check item name starts with letter
                    char lowerLetter = Character.toLowerCase(currentLetter);
                    if (!itemName.isEmpty() && Character.toLowerCase(itemName.charAt(0)) == lowerLetter) {
                        return true;
                    }
                    break;

                case CHINESE:
                    // Use ItemDataManager for Pinyin matching
                    if (dataManager.matchesPinyin(itemIdStr, currentPinyin)) {
                        return true;
                    }
                    // Fallback translation key check
                    String translationKey = item.getTranslationKey().toLowerCase();
                    if (translationKey.length() >= 8) {
                        String firstTwo = translationKey.substring(6, 8).toLowerCase();
                        String pinyin = currentPinyin.toLowerCase();
                        if (firstTwo.startsWith(pinyin) || (pinyin.equals("sh") && firstTwo.startsWith("sg"))) {
                            return true;
                        }
                    }
                    break;
            }
        }
        return false;
    }

    private void resetAllItems(MinecraftServer server) {
        // Clear inventory for ALL online players, not just those in playerScores
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            p.getInventory().clear();
        }
    }

    public void startNewRound(MinecraftServer server) {
        if (roundCount > 0 && currentRound >= roundCount) {
            endGame(server);
            return;
        }

        resetAllItems(server);
        startCountdown(server);
    }

    private void showFinalRanking(MinecraftServer server) {
        List<ServerPlayerEntity> ranked = new ArrayList<>(playerScores.keySet());
        ranked.sort((a, b) -> Integer.compare(playerScores.get(b).getScore(), playerScores.get(a).getScore()));

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // Use English for final ranking
            player.sendMessage(Text.literal("=== Final Ranking ===").formatted(Formatting.GOLD, Formatting.BOLD), false);
            for (int i = 0; i < ranked.size(); i++) {
                ServerPlayerEntity p = ranked.get(i);
                int score = playerScores.get(p).getScore();
                Text line = Text.empty()
                    .append(Text.literal((i + 1) + ". ").formatted(Formatting.WHITE))
                    .append(Text.literal(p.getName().getString()).formatted(Formatting.AQUA))
                    .append(Text.literal(" - ").formatted(Formatting.GRAY))
                    .append(Text.literal(score + " pts").formatted(Formatting.GOLD));
                player.sendMessage(line, false);
            }
        }
        playerScores.clear();
    }
}
