package joni.status4discord.discord;

import dev.dejvokep.boostedyaml.YamlDocument;
import joni.status4discord.Placeholders;
import joni.status4discord.Status4discord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ActivityStatus {

    private final JDA jda;
    private final YamlDocument config;
    private final ScheduledExecutorService scheduler;


    public ActivityStatus(JDA jda, ScheduledExecutorService scheduler) {
        this.jda = jda;
        this.config = Status4discord.getConfigManager().getConfig();
        this.scheduler = scheduler;
    }

    public void start() {
        int delay = config.getInt("update-activity", 45);
        if (delay < 15) {
            Status4discord.LOGGER.error(
                    "Please keep the update interval above 15s to avoid problems with Discord."
            );
            delay = 45;
        }

        OnlineStatus status = OnlineStatus.ONLINE;
        try {
            status = OnlineStatus.valueOf(config.getString("status-activity").toUpperCase());
        } catch (IllegalArgumentException e) {
            Status4discord.LOGGER.error("The OnlineStatus {} is invalid! Check your config!", config.getString("status-activity"));
        }

        jda.getPresence().setStatus(status);

        scheduler.scheduleWithFixedDelay(() -> {
            String[] activity = Placeholders.set(config.getString("activity")).split(" ", 2);

            ActivityType activityType = ActivityType.PLAYING;
            try {
                activityType = ActivityType.valueOf(activity[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                Status4discord.LOGGER.error("The ActivityType {} is invalid! Check your config!", activity[0]);
            }

            jda.getPresence().setActivity(Activity.of(activityType, activity[1]));

        }, 1, delay, TimeUnit.SECONDS);
    }

}
