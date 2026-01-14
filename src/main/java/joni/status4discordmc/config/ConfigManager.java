package joni.status4discordmc.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import joni.status4discordmc.Status4Discord;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {


    private final YamlDocument config;

    public ConfigManager(JavaPlugin plugin) throws IOException {

        // Update config: Convert milliseconds to seconds
        YamlConfiguration v2_config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        if (Integer.parseInt(v2_config.getString("config-version", "0")) <= 2) {
            v2_config.set("update-activity", v2_config.getInt("update_activity", 60000) / 1000);
            v2_config.set("embed.update", v2_config.getInt("embed.update", 30000) / 1000);
            v2_config.set("config-version", 3);
            v2_config.save(new File(plugin.getDataFolder(), "config.yml"));
            Status4Discord.getInstance().getLogger().info("Config updated to v3! (Converted milliseconds to seconds)");
        }

        config = YamlDocument.create(
                new File(plugin.getDataFolder(), "config.yml"),
                plugin.getResource("config.yml"),
                GeneralSettings.builder()
                        .setUseDefaults(false)
                        .build(),
                LoaderSettings.builder()
                        .setAutoUpdate(true)
                        .build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder()
                        .setKeepAll(true)
                        .setVersioning(new BasicVersioning("config-version"))
                        .build()
        );
    }

    public YamlDocument getConfig() {
        return config;
    }

    public void reloadConfig() throws IOException {
        config.reload();
    }

}
