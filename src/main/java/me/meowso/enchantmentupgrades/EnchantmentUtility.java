package me.meowso.enchantmentupgrades;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

import java.util.Map;

public class EnchantmentUtility {
    private final FileConfiguration config;

    public EnchantmentUtility(EnchantmentUpgrades enchantmentUpgrades) {
        this.config = enchantmentUpgrades.getConfig();
    }

    public Enchantment getEnchantmentByName(Player player, String enchantmentName) {
        Enchantment enchantment = null;

        // Loop through the enchantments of the held item to determine which is the one needed. Used because Enchantment.getByKey(NamespacedKey.minecraft(enchantmentName)) did not work with Enchantment Solution
        for (Enchantment enchantmentOnItem : player.getInventory().getItemInMainHand().getEnchantments().keySet()) {
            if (getDefaultEnchantmentNameFromEnchantment(enchantmentOnItem).equals(getDefaultEnchantmentNameFromTitle(enchantmentName))) {
                enchantment = enchantmentOnItem;
                break;
            }
        }

        return enchantment;
    }

    // Gets enchantment name to be displayed (e.g. changes minecraft:silk_touch to Silk Touch)
    public String getFancyEnchantmentName(Enchantment enchantment) {
        return capitilizeFirstChars(getDefaultEnchantmentNameFromEnchantment(enchantment));
    }

    public String capitilizeFirstChars(String string) {
        String[] nameSplit = string.toLowerCase().split("_");

        for (int i = 0; i < nameSplit.length; i++) {
            nameSplit[i] = nameSplit[i].substring(0, 1).toUpperCase() + nameSplit[i].substring(1);
        }

        return String.join(" ", nameSplit);
    }

    // Gets enchantment name w/o prefix. (e.g. changes minecraft:silk_touch to just silk_touch)
    public String getDefaultEnchantmentNameFromEnchantment(Enchantment enchantment) {
        return enchantment.getKey().toString().replaceAll(".*:", "");
    }

    // Gets enchantment name from name displayed in the menus (e.g. changes &f&lSilk Touch to just silk_touch)
    public String getDefaultEnchantmentNameFromTitle(String enchantmentName) {
        return ChatColor.stripColor(enchantmentName.toLowerCase().replaceAll(" ", "_"));
    }

    public int getEnchantmentMaximumLevel(Enchantment enchantment) {
        int maxLevel = enchantment.getMaxLevel();
        String enchantmentName = getDefaultEnchantmentNameFromEnchantment(enchantment);
        Map<String, Object> maxLevelsOfTools = config.getConfigurationSection("maximumEnchantmentLevels").getValues(false);

        if (maxLevelsOfTools.containsKey(enchantmentName)) maxLevel = (int) maxLevelsOfTools.get(enchantmentName);

        return maxLevel;
    }

    public boolean isValidTool(ItemStack item) {
        return config.getStringList("validTools").contains(item.getType().toString().toLowerCase());
    }

    public Material getEnchantmentItemRepresentation(Enchantment enchantment) {
        Map<String, Object> itemRepresentations = config.getConfigurationSection("enchantmentItemRepresentations").getValues(false);
        String enchantmentName = getDefaultEnchantmentNameFromEnchantment(enchantment);
        String itemRepresentationString = (itemRepresentations.containsKey(enchantmentName)) ? (String) itemRepresentations.get(enchantmentName) : config.getString("enchantmentItemRepresentations.default");
        
        return Material.getMaterial(itemRepresentationString.toUpperCase());
    }

    public String getItemName(ItemStack item) {
        return item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : capitilizeFirstChars(item.getType().name());
    }
}
