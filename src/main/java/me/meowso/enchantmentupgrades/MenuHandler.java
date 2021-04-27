package me.meowso.enchantmentupgrades;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public class MenuHandler {
    private final FileConfiguration config;
    private final EnchantmentUtility enchantmentUtility;
    private final UpgradeCostUtility upgradeCostUtility;

    public MenuHandler(EnchantmentUpgrades enchantmentUpgrades, Economy economy) {
        config = enchantmentUpgrades.getConfig();
        enchantmentUtility = new EnchantmentUtility(enchantmentUpgrades);
        upgradeCostUtility = new UpgradeCostUtility(enchantmentUpgrades, economy);
    }

    public void displayUpgradeMenu(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        Map<Enchantment, Integer> enchants = item.getItemMeta().getEnchants();
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&',  config.getString("mainMenuTitle")));
        int index = 0;

        for (Enchantment enchantment : enchants.keySet()) {
            ItemStack inventoryItemRepresentation = new ItemStack(enchantmentUtility.getEnchantmentItemRepresentation(enchantment)); //make item material dynamic
            int enchantmentLevel = item.getEnchantmentLevel(enchantment);

            // Set inventory item representation of enchant's meta
            ItemMeta itemMeta = inventoryItemRepresentation.getItemMeta();
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemMeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + enchantmentUtility.getFancyEnchantmentName(enchantment));

            List<String> lore = new ArrayList<>();
            lore.add("");

            String enchantLevelString = String.valueOf(enchantmentLevel);
            String maxEnchantLevelString = String.valueOf(enchantmentUtility.getEnchantmentMaximumLevel(enchantment));
            lore.add(ChatColor.translateAlternateColorCodes('&', config.getString("enchantmentUpgradeLevelString").replace("$0", enchantLevelString).replace("$1", maxEnchantLevelString)));

            if (enchantmentLevel < enchantmentUtility.getEnchantmentMaximumLevel(enchantment)) {
                String cost = String.valueOf(upgradeCostUtility.calculateUpgradeCost(enchantmentLevel, enchantment));
                String costFormattedString = config.getString("costToUpgradeString").replace("$0", config.getString("currencySymbolFormat").replace("x", cost));
                String levelToUpgradeToString = config.getString("levelToUpgradeToString").replace("$0", String.valueOf(enchantmentLevel + 1));

                lore.add(ChatColor.translateAlternateColorCodes('&', costFormattedString)); //make cost dynamic
                lore.add("");
                lore.add(ChatColor.translateAlternateColorCodes('&', levelToUpgradeToString));
            } else {
                lore.add("");
                lore.add(ChatColor.translateAlternateColorCodes('&', config.getString("errorMaxLevelMessage")));
            }
            itemMeta.setLore(lore);

            inventoryItemRepresentation.setItemMeta(itemMeta);
            inventory.setItem(index, inventoryItemRepresentation);

            index++;
        }

        player.openInventory(inventory);
    }

    public void displayUpgradeConfirmationMenu(Player player, Enchantment enchantment, ItemStack itemClicked) {
        String enchantmentName = enchantmentUtility.getFancyEnchantmentName(enchantment);
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        int level = itemInHand.getEnchantmentLevel(enchantment);
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.translateAlternateColorCodes('&', config.getString("confirmMenuTitle")));

        ItemStack confirmItem = new ItemStack(Material.getMaterial(config.getString("confirmItem").toUpperCase()));
        ItemMeta confirmItemMeta = confirmItem.getItemMeta();
        confirmItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("confirmUpgradeMessage")));
        confirmItem.setItemMeta(confirmItemMeta);
        inventory.setItem(0, confirmItem);
        inventory.setItem(1, confirmItem);
        inventory.setItem(2, confirmItem);
        inventory.setItem(3, confirmItem);

        ItemMeta infoItemMeta = itemClicked.getItemMeta();
        infoItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        infoItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("upgradeEnchantNameTitleMessage").replace("$0", enchantmentName)));
        List<String> infoItemLore = new ArrayList<>();
        infoItemLore.add("");
        infoItemLore.add(ChatColor.translateAlternateColorCodes('&', config.getString("upgradeItemNameMessage").replace("$0", enchantmentUtility.getItemName(itemInHand))));
        infoItemLore.add(ChatColor.translateAlternateColorCodes('&', config.getString("upgradeEnchantNameMessage").replace("$0", enchantmentName)));
        infoItemLore.add(ChatColor.translateAlternateColorCodes('&', config.getString("upgradeLevelMessage").replace("$0", String.valueOf(level)).replace("$1", String.valueOf(level + 1))));
        String cost = String.valueOf(upgradeCostUtility.calculateUpgradeCost(level, enchantment));
        String costFormatString = config.getString("currencySymbolFormat").replace("x", cost);
        infoItemLore.add(ChatColor.translateAlternateColorCodes('&', config.getString("upgradeCostMessage").replace("$0", costFormatString)));
        infoItemMeta.setLore(infoItemLore);
        itemClicked.setItemMeta(infoItemMeta);
        inventory.setItem(4, itemClicked);

        ItemStack cancelItem = new ItemStack(Material.getMaterial(config.getString("cancelItem").toUpperCase()));
        ItemMeta cancelItemMeta = cancelItem.getItemMeta();
        cancelItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("upgradeCancelTitleMessage")));
        cancelItem.setItemMeta(cancelItemMeta);
        inventory.setItem(5, cancelItem);
        inventory.setItem(6, cancelItem);
        inventory.setItem(7, cancelItem);
        inventory.setItem(8, cancelItem);

        player.openInventory(inventory);
    }

    public void displayUpgradeError(Player player, ItemStack item, int index, String message) {
        ItemMeta oldItemMeta = item.getItemMeta();
        ItemMeta newItemMeta = item.getItemMeta();

        newItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("errorTitleMessage")));

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.translateAlternateColorCodes('&', message));
        newItemMeta.setLore(lore);

        item.setItemMeta(newItemMeta);
        player.getOpenInventory().setItem(index, item);

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                // Check if the user has a menu open still and if the item has the same lore, ensuring some random other inventory item doesn't get replaced
                if (player.getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', config.getString("mainMenuTitle"))) && player.getOpenInventory().getItem(index).getItemMeta().getLore().equals(newItemMeta.getLore())) {
                    item.setItemMeta(oldItemMeta);
                    player.getOpenInventory().setItem(index, item);
                }
            }
        }, 3000);
    }
}