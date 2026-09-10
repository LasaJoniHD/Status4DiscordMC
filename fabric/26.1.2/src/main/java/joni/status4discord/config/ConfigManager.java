package joni.status4discord.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import joni.status4discord.Status4discord;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {


    private final YamlDocument config;

    public ConfigManager() throws IOException {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path pluginDir = configDir.resolve(Status4discord.MOD_ID);
        Files.createDirectories(pluginDir);
        File configFile = pluginDir.resolve("config.yml").toFile();
        config = YamlDocument.create(
                configFile,
                getResource("config.yml"),
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

    public static InputStream getResource(String filename) {
        return FabricLoader.getInstance()
                .getModContainer(Status4discord.MOD_ID)
                .flatMap(container -> container.findPath(filename))
                .map(path -> {
                    try {
                        return Files.newInputStream(path);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    public YamlDocument getConfig() {
        return config;
    }

    public void reloadConfig() throws IOException {
        config.reload();
    }

}
