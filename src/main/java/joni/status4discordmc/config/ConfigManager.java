package joni.status4discordmc.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {


    private final YamlDocument config;

    public ConfigManager(JavaPlugin plugin) throws IOException {
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
