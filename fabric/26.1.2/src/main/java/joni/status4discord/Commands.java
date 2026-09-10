package joni.status4discord;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import joni.status4discord.libs.ColorTranslator;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class Commands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(net.minecraft.commands.Commands.literal("status4discord")
                .executes(Commands::executeInfo)
                .then(net.minecraft.commands.Commands.literal("info")
                        .executes(Commands::executeInfo))
                .then(net.minecraft.commands.Commands.literal("reload")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                        .executes(Commands::executeReload))
                .then(net.minecraft.commands.Commands.literal("invite")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                        .executes(Commands::executeInvite))
                .then(net.minecraft.commands.Commands.literal("help")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                        .executes(Commands::executeHelp))
        );
    }

    private static int executeInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
                () -> ColorTranslator.translateColor(
                        "&f[&9Status&f4&9Discord&f] &6by Joni"
                ),
                false
        );
        source.sendSuccess(
                () -> ColorTranslator.translateColor(
                        "&f[&9Status&f4&9Discord&f] &6Version " + Status4discord.getVersion()
                ),
                false
        );
        return 1;
    }

    private static int executeReload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
                () -> ColorTranslator.translateColor(
                        "&f[&9Status&f4&9Discord&f] &6Reloading Status4Discord..."
                ),
                true
        );

        CompletableFuture.runAsync(() -> {
            Status4discord.getDiscord().stop();
            try {
                Status4discord.getConfigManager().reloadConfig();
            } catch (IOException e) {
                Status4discord.LOGGER.error("Failed to reload config file! Please check if access to the file is granted!");
                Status4discord.LOGGER.error("Disabling Discord integration!");
                return;
            }
            Status4discord.startDiscord();
            source.sendSuccess(
                    () -> ColorTranslator.translateColor(
                            "&f[&9Status&f4&9Discord&f] &6Status4Discord reloaded!"), true);
        });

        return 1;
    }

    private static int executeInvite(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() ->
                        ColorTranslator.translateColor("&f[&9Status&f4&9Discord&f] &6" + Status4discord.getDiscord().getInvitationLink())
                , false);
        return 1;
    }

    private static int executeHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() ->
                ColorTranslator.translateColor("&f[&9Status&f4&9Discord&f] &6Use /status4discord [reload | invite | info | help]"), false);
        return 1;
    }
}