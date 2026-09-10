package joni.status4discord.discord;

import dev.dejvokep.boostedyaml.YamlDocument;
import joni.status4discord.Placeholders;
import joni.status4discord.Status4discord;
import joni.status4discord.libs.DebugLogger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Discord {

    private ActivityStatus activityStatus;
    private Logs logs;
    private EmbedStatus embedStatus;

    private final YamlDocument config;
    private JDA bot;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Discord() {
        this.config = Status4discord.getConfigManager().getConfig();
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
            Status4discord.LOGGER.error("The starting ActivityType {} is invalid! Check your config!", activity[0]);
        }

        builder.setActivity(Activity.of(activityType, activity[1]));

        OnlineStatus status = OnlineStatus.IDLE;
        try {
            status = OnlineStatus.valueOf(config.getString("server-starting-status").toUpperCase());
        } catch (IllegalArgumentException e) {
            Status4discord.LOGGER.error("The starting OnlineStatus {} is invalid! Check your config!", config.getString("server-starting-status"));
        }

        builder.setStatus(status);

        try {
            bot = builder.build();
        } catch (InvalidTokenException e) {
            Status4discord.LOGGER.error("Invalid Token Exception: The provided token is invalid!");
            Status4discord.LOGGER.error("Please setup Status4Discord and provide a valid token!");
            return;
        } catch (IllegalArgumentException e) {
            Status4discord.LOGGER.error("IllegalArgumentException: No Token was provided!");
            Status4discord.LOGGER.error("Please setup Status4Discord and provide a token!");
            return;
        }

        DebugLogger.log(token + " is valid!");

        try {
            bot.awaitReady();
        } catch (InterruptedException e) {
            Status4discord.LOGGER.error("InterruptedException: Bot could not initialize!");
            return;
        }

        DebugLogger.log("Bot is ready!");

        Status4discord.LOGGER.info("Logged in as {}", bot.getSelfUser().getName());

        bot.addEventListener(new DiscordCommands(this));

        DebugLogger.log("Commands event added!");

        if (!isInGuilds()) {
            Status4discord.LOGGER.warn("The Discord bot is not on any guild! Maybe you would like to invite him:");
            Status4discord.LOGGER.warn(getInvitationLink());
        }

        // Slash Commands
        bot.getGuilds().forEach(guild -> {
            guild.updateCommands().addCommands(
                    Commands.slash("setembed", "Set this channel as the status embed channel")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("setlogs", "Set this channel as the log channel")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
            ).queue();
        });

        createModules();
        startModules();
    }

    private void startModules() {
        logs.sendStart();
        activityStatus.start();
        embedStatus.start();
    }

    private void createModules() {
        logs = new Logs(bot);
        activityStatus = new ActivityStatus(bot, scheduler);
        embedStatus = new EmbedStatus(bot, scheduler);
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
