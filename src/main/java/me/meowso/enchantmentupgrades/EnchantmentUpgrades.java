package me.meowso.enchantmentupgrades;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

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
    }

    // From https://github.com/MilkBowl/VaultAPI#readme
    private Economy setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return null;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return null;
        else return rsp.getProvider();
    }
}