package joni.status4discordmc.discord;

import java.util.logging.Logger;

import joni.status4discordmc.Placeholders;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class ActivityStatus {

	private JDA jda;
	private Logger logger;
	private boolean updateActivity;

	public ActivityStatus(JDA jda, Logger logger) {
		this.jda = jda;
		this.logger = logger;
	}

	public void start() {
		new Thread() {
			public void run() {
				updateActivity = true;
				jda.getPresence().setStatus(OnlineStatus.ONLINE);

				try {
					sleep(3000);
				} catch (InterruptedException e) {
					logger.fine("Updating the activity has failed! The Thread Interrupted!");
				}

				while (updateActivity) {
					jda.getPresence().setActivity(Activity
							.playing(Placeholders.set("uptime: %server_uptime% | %cpu_system% | %server_version%")));
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
