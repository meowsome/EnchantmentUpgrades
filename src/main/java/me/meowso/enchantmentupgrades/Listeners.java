package me.meowso.enchantmentupgrades;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Listeners implements Listener {
    private final FileConfiguration config;
    private final UpgradeHandler upgradeHandler;
    private final MenuHandler menuHandler;
    private final EnchantmentUtility enchantmentUtility;
    private final UpgradeCostUtility upgradeCostUtility;
    private final Economy economy;

    public Listeners(EnchantmentUpgrades enchantmentUpgrades, Economy economy) {
        config = enchantmentUpgrades.getConfig();
        upgradeHandler = new UpgradeHandler(enchantmentUpgrades, economy);
        menuHandler = new MenuHandler(enchantmentUpgrades, economy);
        enchantmentUtility = new EnchantmentUtility(enchantmentUpgrades);
        upgradeCostUtility = new UpgradeCostUtility(enchantmentUpgrades, economy);
        this.economy = economy;
    }

    // Detect right-clicking an item
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Cancel if player doesn't have valid permissions
        if (!player.hasPermission("enchantmentupgrades.upgrade")) return;

        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && enchantmentUtility.isValidTool(item)) {
            if (player.isSneaking())  {
                if (item.getEnchantments().isEmpty()) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("noEnchantmentsOnItemMessage"))));
                else menuHandler.displayUpgradeMenu(player);
            } else if (config.getBoolean("showTooltips") && !item.getEnchantments().isEmpty()) {
                // Show information tooltip if enabled AND there are enchantments on item
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("clickTooltipMessage"))));
            }
        }
    }

    // Handle clicking in a menu
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        if (title.equals(ChatColor.translateAlternateColorCodes('&', config.getString("mainMenuTitle"))) || title.equals(ChatColor.translateAlternateColorCodes('&', config.getString("confirmMenuTitle")))) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack itemClicked = event.getCurrentItem();
            int itemClickedIndex = event.getSlot();

            if (itemClicked != null && itemClicked.hasItemMeta() && event.getSlotType() == SlotType.CONTAINER && !itemClicked.getItemMeta().getDisplayName().contains(ChatColor.translateAlternateColorCodes('&', config.getString("errorTitleMessage")))) {
                if (title.equals(ChatColor.translateAlternateColorCodes('&', config.getString("mainMenuTitle")))) {
                    handleEnchantmentUpgradeMenuClick(player, itemClicked, itemClickedIndex);
                } else if (title.equals(ChatColor.translateAlternateColorCodes('&', config.getString("confirmMenuTitle")))) {
                    handleEnchantmentUpgradeConfirmationMenuClick(player, itemClicked, itemClickedIndex);
                }
            }
        }
    }

    // Prevent inventory dragging
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', config.getString("mainMenuTitle")))) {
            event.setCancelled(true);
        }
    }

    // Handle clicking in the upgrade main menu
    public void handleEnchantmentUpgradeMenuClick(Player player, ItemStack itemClicked, int itemClickedIndex) {
        try {
            Enchantment enchantment = enchantmentUtility.getEnchantmentByName(player, itemClicked.getItemMeta().getDisplayName());

            if (upgradeHandler.canUpgrade(player, enchantment)) {
                if (upgradeCostUtility.hasEnoughMoney(player, enchantment)) {
                    menuHandler.displayUpgradeConfirmationMenu(player, enchantment, itemClicked);
                } else {
                    menuHandler.displayUpgradeError(player, itemClicked, itemClickedIndex, config.getString("errorNoMoneyMessage"));
                }
            } else {
                menuHandler.displayUpgradeError(player, itemClicked, itemClickedIndex, config.getString("errorInvalidUpgrade"));
            }
        } catch (Exception e) {
            menuHandler.displayUpgradeError(player, itemClicked, itemClickedIndex, config.getString("errorDeterminingEnchantMessage"));
            throw e;
        }
    }

    // Handle clicking in the upgrade confirm menu
    public void handleEnchantmentUpgradeConfirmationMenuClick(Player player, ItemStack itemClicked, int itemClickedIndex) {
        if (itemClicked.getType() == Material.getMaterial(config.getString("confirmItem").toUpperCase())) {
            upgradeHandler.upgradeEnchantment(player, itemClicked, itemClickedIndex); // Actually upgrade enchantment
        } else if (itemClicked.getType() == Material.getMaterial(config.getString("cancelItem").toUpperCase())) {
            menuHandler.displayUpgradeMenu(player); // Return to main menu
        }
    }
}
