package joni.status4discordmc.discord;

import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

import joni.status4discordmc.Placeholders;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

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
					logger.fine("Updating the activity has failed! The Thread Interrupted!");
				}

				jda.getPresence().setStatus(OnlineStatus.ONLINE);

				while (updateActivity) {
					jda.getPresence().setActivity(Activity.playing(Placeholders.set(config.getString("activity"))));
					try {
						sleep(45000);
					} catch (InterruptedException e) {
						logger.fine("Updating the activity has failed! The Thread Interrupted!");
					}
				}

			}
		}.start();
	}

	public void stop() {
		updateActivity = false;
	}

}
