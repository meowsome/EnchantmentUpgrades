package me.meowso.enchantmentupgrades;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.InputStreamReader;

// Takes existing config file and writes components that are not present to it
public class ConfigHandler {
    private FileConfiguration config;
    private YamlConfiguration defaultConfig;
    private EnchantmentUpgrades enchantmentUpgrades;

    public ConfigHandler(EnchantmentUpgrades enchantmentUpgrades) {
        // Either save the existing config or generate new one
        enchantmentUpgrades.saveDefaultConfig();
        this.enchantmentUpgrades = enchantmentUpgrades;
    }

    private void loadConfigs(File configFile) {
        defaultConfig = YamlConfiguration.loadConfiguration( new InputStreamReader(enchantmentUpgrades.getResource("config.yml")) );
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void initializeConfig() {
        File configFile = new File(enchantmentUpgrades.getDataFolder(), "config.yml");

        if (configFile.exists()) {
            loadConfigs(configFile);

            // Check each "key" in the default config and ensure it exists in the modified config
            for (String key : defaultConfig.getKeys(true)) {
                if (!config.contains(key)) {
                    // Value is missing, rename old config file & notify user
                    String fileName = "config-OLD-" + System.currentTimeMillis() + ".yml";
                    configFile.renameTo(new File(enchantmentUpgrades.getDataFolder(), fileName));
                    enchantmentUpgrades.getLogger().severe("IMPORTANT! The configuration file has been updated to version " + enchantmentUpgrades.getDescription().getVersion() + " because it was outdated or missing sections. Your old configuration file has been renamed to " + fileName + " and is now inactive. Please go through your old configuration file, adjust the new one accordingly, and delete the old one. Thank you.");
                    break;
                }
            }
        }

        // Either save the existing config or generate new one
        enchantmentUpgrades.saveDefaultConfig();
    }
}
