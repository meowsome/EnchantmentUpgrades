package me.meowso.enchantmentupgrades;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;

public final class EnchantmentUpgrades extends JavaPlugin {
    @Override
    public void onEnable() {
        Economy economy = setupVault();
        if (economy == null) {
            getLogger().severe("Please install and enable Vault");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new Listeners(this, economy), this);
        saveDefaultConfig();

        try {
            updateChecker();
        } catch (IOException e) {
            getLogger().warning("Error occurred checking for updates");
        }
    }

    private void updateChecker() throws IOException {
        if (getConfig().getBoolean("checkForUpdates")) {
            Updater updater = new Updater();
            String newVersion = updater.getVersion();
            String oldVersion = this.getDescription().getVersion();

            if (newVersion != null && updater.isNewVersion(newVersion, oldVersion)) getLogger().info("Version " + newVersion + " is now available for download here: https://github.com/meowsome/EnchantmentUpgrades/releases");
        }
    }

    // From https://github.com/MilkBowl/VaultAPI#readme
    private Economy setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return null;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return null;
        else return rsp.getProvider();
    }
}