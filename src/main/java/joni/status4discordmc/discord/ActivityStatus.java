package joni.status4discordmc.discord;

import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

import joni.status4discordmc.Placeholders;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

public class ActivityStatus {

	private JDA jda;
	private Logger logger;
	private boolean updateActivity;
	private FileConfiguration config;

	public ActivityStatus(JDA jda, Logger logger, FileConfiguration config) {
		this.jda = jda;
		this.logger = logger;
		this.config = config;
	}

	public void start() {
		new Thread() {
			public void run() {
				updateActivity = true;

				try {
					sleep(5000);
				} catch (InterruptedException e) {
					logger.severe("Updating the activity has failed! The Thread Interrupted!");
				}

				jda.getPresence().setStatus(OnlineStatus.ONLINE);

				while (updateActivity) {
					String[] activity = Placeholders.set(config.getString("activity")).split(" ", 2);
					try {
						jda.getPresence().setActivity(Activity.of(ActivityType.valueOf(activity[0]), activity[1]));
					} catch (IllegalArgumentException e) {
						logger.severe("The ActivityType " + activity[0] + " is invalid! Check your config!");
						jda.getPresence().setActivity(Activity.playing(activity[1]));
					}
					try {
						sleep(45000);
					} catch (InterruptedException e) {
						logger.severe("Updating the activity has failed! The Thread Interrupted!");
					}
				}

			}
		}.start();
	}

	public void stop() {
		updateActivity = false;
	}

}
