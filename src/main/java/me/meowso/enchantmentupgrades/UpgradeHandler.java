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
            Enchantment enchantment = enchantmentUtility.getEnchantmentByName(player, player.getOpenInventory().getItem(4).getItemMeta().getDisplayName().replace(ChatColor.translateAlternateColorCodes('&', config.getString("upgradeEnchantNameTitleMessage")).replace("$0" ,""), "")); // Get enchantment from the title of the informational item in the confirm menu

            if (canUpgrade(player, enchantment)) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                int level = itemInHand.getEnchantmentLevel(enchantment);
                int cost = upgradeCostUtility.calculateUpgradeCost(level, enchantment);

                economy.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), cost);
                itemInHand.removeEnchantment(enchantment);
                itemInHand.addUnsafeEnchantment(enchantment, level + 1);

                String enchantName = enchantmentUtility.getFancyEnchantmentName(enchantment);
                String itemName = enchantmentUtility.getItemName(itemInHand);
                String levelName = String.valueOf(level + 1);
                String costName = config.getString("currencySymbolFormat").replace("x", String.valueOf(cost));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix") + " " + config.getString("confirmedUpgradeMessage").replace("$0", enchantName).replace("$1", itemName).replace("$2", levelName).replace("$3", costName)));
                menuHandler.displayUpgradeMenu(player);
            } else {
                menuHandler.displayUpgradeError(player, itemClicked, itemClickedIndex, config.getString("errorInvalidUpgrade"));
            }
        } catch (Exception e) {
            menuHandler.displayUpgradeError(player, itemClicked, itemClickedIndex, config.getString("errorDeterminingEnchantMessage"));
            throw e;
        }
    }

    public boolean canUpgrade (Player player, Enchantment enchantment) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        return enchantmentUtility.isValidTool(itemInHand) && itemInHand.containsEnchantment(enchantment) && itemInHand.getEnchantmentLevel(enchantment) < enchantmentUtility.getEnchantmentMaximumLevel(enchantment) && player.hasPermission("enchantmentupgrades.upgrade");
    }
}
