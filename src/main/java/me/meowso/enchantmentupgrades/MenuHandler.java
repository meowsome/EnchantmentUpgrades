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
            lore.add(ChatColor.GRAY + "Current Level: " + ChatColor.WHITE + enchantmentLevel + ChatColor.GRAY + "/" + ChatColor.WHITE + enchantmentUtility.getEnchantmentMaximumLevel(enchantment));
            if (enchantmentLevel < enchantmentUtility.getEnchantmentMaximumLevel(enchantment)) {
                String cost = String.valueOf(upgradeCostUtility.calculateUpgradeCost(enchantmentLevel, enchantment));
                lore.add(ChatColor.translateAlternateColorCodes('&', ChatColor.GRAY + "Upgrade Cost: " + config.getString("currencySymbolFormat").replace("x", cost))); //make cost dynamic
                lore.add("");
                lore.add(ChatColor.GRAY + "Click to upgrade to level " + ChatColor.WHITE + (enchantmentLevel + 1));
            } else {
                lore.add("");
                lore.add(ChatColor.RED + "You have reached the maximum");
                lore.add(ChatColor.RED + "level for this enchantment.");
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
        confirmItemMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Confirm Upgrade");
        confirmItem.setItemMeta(confirmItemMeta);
        inventory.setItem(0, confirmItem);
        inventory.setItem(1, confirmItem);
        inventory.setItem(2, confirmItem);
        inventory.setItem(3, confirmItem);

        ItemMeta infoItemMeta = itemClicked.getItemMeta();
        infoItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        infoItemMeta.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + "Upgrade " + enchantmentName);
        List<String> infoItemLore = new ArrayList<>();
        infoItemLore.add("");
        infoItemLore.add(ChatColor.GRAY + "Item: " + ChatColor.WHITE + enchantmentUtility.getItemName(itemInHand));
        infoItemLore.add(ChatColor.GRAY + "Enchantment: " + ChatColor.WHITE + enchantmentName);
        infoItemLore.add(ChatColor.GRAY + "Level Upgrade: " + ChatColor.WHITE + level + ChatColor.GRAY + " → " + ChatColor.WHITE + (level + 1));
        String cost = String.valueOf(upgradeCostUtility.calculateUpgradeCost(level, enchantment));
        infoItemLore.add(ChatColor.translateAlternateColorCodes('&', ChatColor.GRAY + "Cost: " + config.getString("currencySymbolFormat").replace("x", cost)));
        infoItemMeta.setLore(infoItemLore);
        itemClicked.setItemMeta(infoItemMeta);
        inventory.setItem(4, itemClicked);

        ItemStack cancelItem = new ItemStack(Material.getMaterial(config.getString("cancelItem").toUpperCase()));
        ItemMeta cancelItemMeta = cancelItem.getItemMeta();
        cancelItemMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Cancel Upgrade");
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

        newItemMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Error");

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.RED + message);
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
                if (ChatColor.stripColor(player.getOpenInventory().getTitle()).equals(ChatColor.stripColor(config.getString("mainMenuTitle"))) && player.getOpenInventory().getItem(index).getItemMeta().getLore().equals(newItemMeta.getLore())) {
                    item.setItemMeta(oldItemMeta);
                    player.getOpenInventory().setItem(index, item);
                }
            }
        }, 3000);
    }
}