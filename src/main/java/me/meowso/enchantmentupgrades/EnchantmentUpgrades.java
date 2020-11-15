package me.meowso.enchantmentupgrades;

import org.bukkit.plugin.java.JavaPlugin;

public final class EnchantmentUpgrades extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        saveDefaultConfig();
    }
}