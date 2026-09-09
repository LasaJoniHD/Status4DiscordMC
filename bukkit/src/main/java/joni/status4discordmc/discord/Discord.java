package joni.status4discordmc.discord;

import dev.dejvokep.boostedyaml.YamlDocument;
import joni.status4discordmc.Placeholders;
import joni.status4discordmc.Status4Discord;
import joni.status4discordmc.libs.DebugLogger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

public class Discord {

    private ActivityStatus activityStatus;
    private Logs logs;
    private EmbedStatus embedStatus;

    private final JavaPlugin plugin;
    private final YamlDocument config;
    private JDA bot;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Discord(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = Status4Discord.getInstance().getConfigManager().getConfig();
    }

    public void start() {

        // Set Debug Logging
        DebugLogger.setDebug(config.getBoolean("debug"));

        String token = config.getString("token");

        JDABuilder builder = JDABuilder.createDefault(token);

        // Activity
        String[] activity = Placeholders.set(config.getString("server-starting-activity")).split(" ", 2);

        Activity.ActivityType activityType = Activity.ActivityType.CUSTOM_STATUS;
        try {
            activityType = Activity.ActivityType.valueOf(activity[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("The starting ActivityType " + activity[0] + " is invalid! Check your config!");
        }

        builder.setActivity(Activity.of(activityType, activity[1]));

        OnlineStatus status = OnlineStatus.IDLE;
        try {
            status = OnlineStatus.valueOf(config.getString("server-starting-status").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("The starting OnlineStatus " + config.getString("server-starting-status") + " " +
                    "is invalid! Check your config!");
        }

        builder.setStatus(status);

        try {
            bot = builder.build();
        } catch (InvalidTokenException e) {
            plugin.getLogger().severe("Invalid Token Exception: The provided token is invalid!");
            plugin.getLogger().severe("Please setup Status4Discord and provide a valid token!");
            return;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("IllegalArgumentException: No Token was provided!");
            plugin.getLogger().severe("Please setup Status4Discord and provide a token!");
            return;
        }

        DebugLogger.log(token + " is valid!");

        try {
            bot.awaitReady();
        } catch (InterruptedException e) {
            plugin.getLogger().severe("InterruptedException: Bot could not initialize!");
            return;
        }

        DebugLogger.log("Bot is ready!");

        plugin.getLogger().info("Logged in as " + bot.getSelfUser().getName());

        bot.addEventListener(new DiscordCommands(this));

        DebugLogger.log("Commands event added!");

        if (!isInGuilds()) {
            plugin.getLogger().warning("The Discord bot is not on any guild! Maybe you would like to invite him:");
            plugin.getLogger().warning(getInvitationLink());
        }

        createModules();
        startModules();
    }

    private void startModules() {
        logs.sendStart();
        activityStatus.start();
        embedStatus.start();
    }

    private void createModules() {
        Logger log = plugin.getLogger();
        logs = new Logs(bot, log);
        activityStatus = new ActivityStatus(bot, log, scheduler);
        embedStatus = new EmbedStatus(bot, log, scheduler);
    }

    public ActivityStatus getActivityStatus() {
        return activityStatus;
    }

    public EmbedStatus getEmbedStatus() {
        return embedStatus;
    }

    public JDA getJDA() {
        return bot;
    }

    public void stop() {
        if (bot == null)
            return;

        scheduler.shutdownNow();
        embedStatus.stop();
        logs.sendStop();

        bot.shutdown();
        try {
            if (!bot.awaitShutdown(java.time.Duration.ofSeconds(10))) {
                bot.shutdownNow();
                bot.awaitShutdown();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Boolean isInGuilds() {
        if (bot == null)
            return null;

        return !bot.getGuilds().isEmpty();
    }

    public String getInvitationLink() {
        if (bot == null)
            return null;

        return bot.getInviteUrl(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_MANAGE,
                Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_HISTORY,
                Permission.MESSAGE_ADD_REACTION);
    }

}
