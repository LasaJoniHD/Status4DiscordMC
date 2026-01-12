package joni.status4discordmc;

import joni.status4discordmc.lib.ColorTranslator;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Commands implements CommandExecutor, TabExecutor {

    private final JavaPlugin plugin;

    public Commands(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (args.length == 0 || args[0].equals("info")) {
            sendPluginInfo(s);
            return false;
        }

        if (!s.hasPermission("status4discord.admin"))
            return false;

        switch (args[0]) {
            case "reload" -> {
                doReload(s);
                return false;
            }
            case "invite" -> {
                doInvite(s);
                return false;
            }
            case "help" -> {
                doHelp(s);
                return false;
            }
        }

        return false;
    }

    private void sendPluginInfo(CommandSender s) {
        s.sendMessage(ColorTranslator.translateColor("&f[&9Status&f4&9Discord&f] &6by Joni &9/help"));
        s.sendMessage(ColorTranslator.translateColor("&f[&9Status&f4&9Discord&f] &6Version ")
                + Status4Discord.getInstance().getVersion());
    }

    private void doReload(CommandSender s) {
        Status4Discord.getInstance().getDiscord().stop();
        try {
            Status4Discord.getInstance().getConfigManager().reloadConfig();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to reload config file! Please check if access to the file is granted!");
            plugin.getLogger().severe("Disabling plugin!");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }
        CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS).execute(() -> {
            Status4Discord.getInstance().startDiscord();
        });
        CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS).execute(() -> {
            s.sendMessage(ColorTranslator.translateColor("&f[&9Status&f4&9Discord&f] &6Status4Discord reloaded!"));
        });

    }

    private void doInvite(CommandSender s) {
        s.sendMessage(ColorTranslator.translateColor("&f[&9Status&f4&9Discord&f] &6") + Status4Discord.getInstance().getDiscord().getInvitationLink());
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String label, String[] args) {
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> flist = new ArrayList<>();
        if (args.length == 1) {
            list.add("help");
            list.add("reload");
            list.add("invite");
            list.add("info");
        }
        String current = args[args.length - 1].toLowerCase();
        for (String string : list) {
            if (string.toLowerCase().startsWith(current))
                flist.add(string);
        }
        return flist;
    }

    private void doHelp(CommandSender s) {
        s.sendMessage(ColorTranslator
                .translateColor("&f[&9Status&f4&9Discord&f] &6Use /status4discord [reload | invite | info | help]"));
    }

}
