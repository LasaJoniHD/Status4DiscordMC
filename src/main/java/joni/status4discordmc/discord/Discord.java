package joni.status4discordmc.discord;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;

import org.bukkit.Bukkit;

import joni.status4discordmc.Placeholders;
import joni.status4discordmc.Status4Discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class Discord {

	static JDA jda;

	public static void setup() {
		JDABuilder builder = JDABuilder
				.createDefault("ODE5OTc0NDMwOTA2MjUzMzEz.GXqzcC.NAm9D2lrV_-EaQ5q9D3R3NgQhqHxFwWJtLaRFw");
		builder.setActivity(Activity.customStatus("Server is starting..."));
		builder.setStatus(OnlineStatus.IDLE);
		jda = builder.build();
		try {
			jda.awaitReady();
		} catch (InterruptedException e) {
		}
		System.out.println("Logged in as " + jda.getSelfUser().getName());
		ActivityStatus a = new ActivityStatus(jda, Status4Discord.logger());
		a.start();
	}

	public static void statusEmbed() {
		new Thread() {

			private String message;

			public void run() {
				TextChannel textChannel = jda.getTextChannelById("1159154870704676905");
				if (textChannel.canTalk()) {
					EmbedBuilder e = new EmbedBuilder();
					e.setTitle("Online");
					e.addField("Server IP", Bukkit.getIp(), true);
					e.addField("Player Count", String.valueOf(Bukkit.getOnlinePlayers().size()), true);
					e.addField("RAM", "Free: " + String.valueOf(Runtime.getRuntime().freeMemory() / 1024L / 1024L) + "("
							+ String.valueOf((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory())
									/ 1024L / 1024L)
							+ "mb / " + String.valueOf(Runtime.getRuntime().maxMemory() / 1024L / 1024L) + " mb)",
							false);
					e.addField("Uptime", String.valueOf(System.currentTimeMillis() - Status4Discord.startUp), true);
					e.addField("TPS", String.valueOf(Math.round(Bukkit.getServer().getTPS()[0] * 10.0) / 10.0), true);
					e.addField("CPU", String.valueOf(Placeholders.getCPU()), false);
					e.addField("Players", Bukkit.getOnlinePlayers().toString(), true);

					textChannel.sendMessageEmbeds(e.build()).queue(msg -> {
						message = msg.getId();
					});
					Status4Discord.logger().info("a");
				} else {
					Status4Discord.logger().fine("The bot cannot talk in this channel, check your permissions!");
				}
				try {
					sleep(10000);
					Status4Discord.logger().info("b");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Status4Discord.logger().info(message);
				textChannel.editMessageById(message, "und edit");
			}
		}.start();
	}

	public static void shutdown() {
		jda.shutdown();
		try {
			if (!jda.awaitShutdown(Duration.ofSeconds(3))) {
				jda.shutdownNow();
			}
		} catch (InterruptedException e) {
		}

	}

	public static void sendMessangeToLogAsEmbed(String msg, Color c) {
		TextChannel textChannel = jda.getTextChannelById("1159154848508432414");
		if (textChannel.canTalk()) {
			EmbedBuilder embed = new EmbedBuilder();
			embed.setDescription(msg);
			embed.setColor(c);
			embed.setTimestamp(Instant.now());
			textChannel.sendMessageEmbeds(embed.build()).queue();
		} else {
			Status4Discord.logger().fine("The bot cannot talk in this channel, check your permissions!");
		}
	}

}
