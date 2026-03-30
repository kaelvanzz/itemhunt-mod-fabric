package com.example.itemhunt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import com.example.itemhunt.game.ItemHuntGame;

import static com.example.itemhunt.game.GameMode.*;

public class ItemHuntCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("itemhunt")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("start")
                        .executes(ctx -> {
                            if (!ItemHuntGame.INSTANCE.isGameModeSet()) {
                                ctx.getSource().sendError(Text.literal("Please set game mode first with /itemhunt mode"));
                                return 0;
                            }
                            ItemHuntGame.INSTANCE.startGame(ctx.getSource().getServer());
                            ctx.getSource().sendFeedback(() -> Text.literal("Game started!"), false);
                            return 1;
                        })
                )
                .then(CommandManager.literal("end")
                        .executes(ctx -> {
                            ItemHuntGame.INSTANCE.endGame(ctx.getSource().getServer());
                            ctx.getSource().sendFeedback(() -> Text.literal("Game ended! Showing final ranking..."), false);
                            return 1;
                        })
                )
                .then(CommandManager.literal("roundtime")
                        .then(CommandManager.argument("seconds", IntegerArgumentType.integer(0, 3600))
                                .executes(ctx -> {
                                    int time = IntegerArgumentType.getInteger(ctx, "seconds");
                                    ItemHuntGame.INSTANCE.setRoundTime(time);
                                    if (time == 0) {
                                        ctx.getSource().sendFeedback(() -> Text.literal("Round time set to unlimited"), false);
                                    } else {
                                        ctx.getSource().sendFeedback(() -> Text.literal("Round time set to " + time + " seconds"), false);
                                    }
                                    return 1;
                                })
                        )
                )
                .then(CommandManager.literal("rounds")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0, 100))
                                .executes(ctx -> {
                                    int rounds = IntegerArgumentType.getInteger(ctx, "amount");
                                    ItemHuntGame.INSTANCE.setRoundCount(rounds);
                                    if (rounds == 0) {
                                        ctx.getSource().sendFeedback(() -> Text.literal("Round count set to unlimited"), false);
                                    } else {
                                        ctx.getSource().sendFeedback(() -> Text.literal("Round count set to " + rounds), false);
                                    }
                                    return 1;
                                })
                        )
                )
                .then(CommandManager.literal("config")
                        .executes(ctx -> {
                            ItemHuntGame.INSTANCE.showConfig(ctx.getSource());
                            return 1;
                        })
                )
                .then(CommandManager.literal("mode")
                        .then(CommandManager.literal("colors")
                                .executes(ctx -> {
                                    ItemHuntGame.INSTANCE.setGameMode(COLORS);
                                    ctx.getSource().sendFeedback(() -> Text.literal("Game mode set to [Colors]"), false);
                                    return 1;
                                })
                        )
                        .then(CommandManager.literal("name_english")
                                .executes(ctx -> {
                                    ItemHuntGame.INSTANCE.setGameMode(ENGLISH);
                                    ctx.getSource().sendFeedback(() -> Text.literal("Game mode set to [Name - English] (first letter)"), false);
                                    return 1;
                                })
                        )
                        .then(CommandManager.literal("name_chinese")
                                .executes(ctx -> {
                                    ItemHuntGame.INSTANCE.setGameMode(CHINESE);
                                    ctx.getSource().sendFeedback(() -> Text.literal("Game mode set to [Name - Chinese] (pinyin first letter)"), false);
                                    return 1;
                                })
                        )
                )
                .then(CommandManager.literal("skip")
                        .executes(ctx -> {
                            ItemHuntGame.INSTANCE.skipRound(ctx.getSource().getServer());
                            ctx.getSource().sendFeedback(() -> Text.literal("Round skipped!"), false);
                            return 1;
                        })
                )
        );
    }
}
