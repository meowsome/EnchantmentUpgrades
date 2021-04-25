package me.meowso.enchantmentupgrades;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import java.util.Map;

public class UpgradeCostUtility {
    private final Economy economy;
    private final FileConfiguration config;
    private final EnchantmentUtility enchantmentUtility;

    public UpgradeCostUtility(EnchantmentUpgrades enchantmentUpgrades, Economy economy) {
        config = enchantmentUpgrades.getConfig();
        enchantmentUtility = new EnchantmentUtility(enchantmentUpgrades);
        this.economy = economy;
    }

    public int calculateUpgradeCost (int level, Enchantment enchantment) {
        return (int) Math.ceil(level * getEnchantmentUpgradeCostMultiplier(enchantment));
    }

    public double getEnchantmentUpgradeCostMultiplier(Enchantment enchantment) {
        Map<String, Object> costMultipliers = config.getConfigurationSection("upgradeCostMultipliers").getValues(false);
        String enchantmentName = enchantmentUtility.getDefaultEnchantmentNameFromEnchantment(enchantment);
        return (costMultipliers.containsKey(enchantmentName)) ? (double) costMultipliers.get(enchantmentName) : config.getDouble("upgradeCostMultipliers.default");
    }

    public boolean hasEnoughMoney (Player player, Enchantment enchantment) {
        double balance = economy.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));
        return balance >= calculateUpgradeCost(player.getInventory().getItemInMainHand().getEnchantmentLevel(enchantment), enchantment);
    }
}
