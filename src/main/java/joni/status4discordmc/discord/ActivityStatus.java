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
        int delay = config.getInt("update-activity", 45_000);
        if (delay < 15_000) {
            logger.severe(
                    "Please keep the update interval above 15000 ms to avoid problems with Discord."
            );
            delay = 45_000;
        }

        jda.getPresence().setStatus(OnlineStatus.ONLINE);

        scheduler.scheduleWithFixedDelay(() -> {
            try {

                String[] activity = Placeholders.set(config.getString("activity")).split(" ", 2);

                try {
                    jda.getPresence().setActivity(Activity.of(ActivityType.valueOf(activity[0]), activity[1]));
                } catch (IllegalArgumentException e) {
                    logger.severe("The ActivityType " + activity[0] + " is invalid! Check your config!");
                    jda.getPresence().setActivity(Activity.playing(activity[1]));
                }

            } catch (Exception e) {
                logger.severe("Updating the activity failed: " + e.getMessage());
            }

        }, 5, delay, TimeUnit.MILLISECONDS);
    }

}
