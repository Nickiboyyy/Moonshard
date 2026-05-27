package de.moonprincess.moonshard.listeners;

import de.moonprincess.moonshard.Moonshard;
import de.moonprincess.moonshard.commands.ShardCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ShardListener implements Listener {

    private final Moonshard plugin;
    private final ShardCommand shardCommand;

    public ShardListener(Moonshard plugin, ShardCommand shardCommand) {
        this.plugin = plugin;
        this.shardCommand = shardCommand;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        ItemStack item = event.getCurrentItem();
        
        if (item == null || item.getType() == Material.AIR) return;

        // Main Menu check
        if (title.equals(plugin.translate("&bMoonshards - Menü"))) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 12) {
                shardCommand.openShopGui(player, "left");
            } else if (slot == 13) {
                shardCommand.openShopGui(player, "middle");
            } else if (slot == 14) {
                shardCommand.openShopGui(player, "right");
            }
            return;
        }

        // Leaderboard check
        if (title.equals(plugin.translate("&6Moonshard Leaderboard"))) {
            event.setCancelled(true);
            if (item.getType() == Material.BARRIER) {
                shardCommand.openMainGui(player);
            }
            return;
        }

        // Custom Shops check
        String[] shopKeys = {"left", "middle", "right"};
        for (String shopKey : shopKeys) {
            String shopTitle = plugin.translate(plugin.getConfig().getString("shops." + shopKey + ".title", "---"));
            if (title.equals(shopTitle)) {
                event.setCancelled(true);
                handleShopPurchase(player, shopKey, event.getRawSlot());
                return;
            }
        }
    }

    private void handleShopPurchase(Player player, String shopKey, int slot) {
        if (plugin.getConfig().getConfigurationSection("shops." + shopKey + ".items") == null) return;

        for (String key : plugin.getConfig().getConfigurationSection("shops." + shopKey + ".items").getKeys(false)) {
            String path = "shops." + shopKey + ".items." + key;
            if (plugin.getConfig().getInt(path + ".slot") == slot) {
                long price = plugin.getConfig().getLong(path + ".price", 0);
                long balance = plugin.getShardManager().getShards(player.getUniqueId());

                if (balance < price) {
                    player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.not_enough_shards")));
                    return;
                }

                plugin.getShardManager().removeShards(player.getUniqueId(), price);
                List<String> commands = plugin.getConfig().getStringList(path + ".commands");
                for (String cmd : commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                }
                
                String itemName = plugin.translate(plugin.getConfig().getString(path + ".name", key));
                player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + "&aYou bought &f" + itemName + " &afor &e" + price + " %shards%&a!"));
                return;
            }
        }
    }
}
