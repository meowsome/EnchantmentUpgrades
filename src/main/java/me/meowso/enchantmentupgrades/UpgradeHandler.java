package me.meowso.enchantmentupgrades;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class UpgradeHandler {
    private final Economy economy;
    private final MenuHandler menuHandler;
    private final UpgradeCostUtility upgradeCostUtility;
    private final EnchantmentUtility enchantmentUtility;
    private final FileConfiguration config;

    public UpgradeHandler(EnchantmentUpgrades enchantmentUpgrades, Economy economy) {
        this.menuHandler = new MenuHandler(enchantmentUpgrades, economy);
        this.upgradeCostUtility = new UpgradeCostUtility(enchantmentUpgrades, economy);
        this.enchantmentUtility = new EnchantmentUtility(enchantmentUpgrades);
        this.config = enchantmentUpgrades.getConfig();
        this.economy = economy;
    }

    public void upgradeEnchantment (Player player, ItemStack itemClicked, int itemClickedIndex) {
        try {
            Enchantment enchantment = enchantmentUtility.getEnchantmentByName(player, player.getOpenInventory().getItem(4).getItemMeta().getDisplayName().replace("Upgrade ", "")); // Get enchantment from the title of the informational item in the confirm menu

            if (canUpgrade(player, enchantment)) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                int level = itemInHand.getEnchantmentLevel(enchantment);
                int cost = upgradeCostUtility.calculateUpgradeCost(level, enchantment);

                economy.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), cost);
                itemInHand.removeEnchantment(enchantment);
                itemInHand.addUnsafeEnchantment(enchantment, level + 1);

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + " " + ChatColor.WHITE + enchantmentUtility.getFancyEnchantmentName(enchantment) + ChatColor.GRAY + " on " + ChatColor.WHITE + itemInHand.getItemMeta().getDisplayName() + ChatColor.GRAY + " has been upgraded to level " + ChatColor.WHITE + (level + 1) + ChatColor.GRAY + " for " + ChatColor.GREEN + cost + "♦" + ChatColor.GRAY + ".");
                menuHandler.displayUpgradeMenu(player);
            } else {
                menuHandler.displayUpgradeError(player, itemClicked, itemClickedIndex, "This enchantment upgrade cannot take place.");
            }
        } catch (Exception e) {
            menuHandler.displayUpgradeError(player, itemClicked, itemClickedIndex, "An error occurred determining the enchantment.");
            throw e;
        }
    }

    public boolean canUpgrade (Player player, Enchantment enchantment) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        return enchantmentUtility.isValidTool(itemInHand) && itemInHand.containsEnchantment(enchantment) && itemInHand.getEnchantmentLevel(enchantment) < enchantmentUtility.getEnchantmentMaximumLevel(enchantment, itemInHand);
    }
}
