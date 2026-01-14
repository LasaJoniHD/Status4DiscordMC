package joni.status4discordmc.discord;

import dev.dejvokep.boostedyaml.YamlDocument;
import joni.status4discordmc.Placeholders;
import joni.status4discordmc.Status4Discord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ActivityStatus {

    private final JDA jda;
    private final Logger logger;
    private final YamlDocument config;
    private final ScheduledExecutorService scheduler;


    public ActivityStatus(JDA jda, Logger logger, ScheduledExecutorService scheduler) {
        this.jda = jda;
        this.logger = logger;
        this.config = Status4Discord.getInstance().getConfigManager().getConfig();
        this.scheduler = scheduler;
    }

    public void start() {
        int delay = config.getInt("update-activity", 45);
        if (delay < 15) {
            logger.severe(
                    "Please keep the update interval above 15s to avoid problems with Discord."
            );
            delay = 45;
        }

        OnlineStatus status = OnlineStatus.ONLINE;
        try {
            status = OnlineStatus.valueOf(config.getString("status-activity").toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.severe("The OnlineStatus " + config.getString("status-activity") + " is invalid! Check your config!");
        }

        jda.getPresence().setStatus(status);

        scheduler.scheduleWithFixedDelay(() -> {
            String[] activity = Placeholders.set(config.getString("activity")).split(" ", 2);

            ActivityType activityType = ActivityType.PLAYING;
            try {
                activityType = ActivityType.valueOf(activity[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.severe("The ActivityType " + activity[0] + " is invalid! Check your config!");
            }

            jda.getPresence().setActivity(Activity.of(activityType, activity[1]));

        }, 1, delay, TimeUnit.SECONDS);
    }

}
