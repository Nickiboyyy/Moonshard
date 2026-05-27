package de.moonprincess.moonshard.commands;

import de.moonprincess.moonshard.Moonshard;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ShardCommand implements CommandExecutor, TabCompleter {

    private final Moonshard plugin;
    private final Random random = new Random();

    public ShardCommand(Moonshard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is for players only.");
            return true;
        }

        if (!player.hasPermission("moonshard.use")) {
            player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.no_permission")));
            return true;
        }

        if (args.length == 0) {
            openMainGui(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "shop":
                if (checkPerm(player, "moonshard.shop")) openShopGui(player, "middle");
                break;
            case "gamble":
                if (checkPerm(player, "moonshard.gamble")) handleGamble(player, args);
                break;
            case "pay":
                if (checkPerm(player, "moonshard.pay")) handlePay(player, args);
                break;
            case "baltop":
            case "leaderboard":
                if (checkPerm(player, "moonshard.leaderboard")) openLeaderboardGui(player);
                break;
            case "daily":
                if (checkPerm(player, "moonshard.daily")) handleDaily(player);
                break;
            case "info":
                if (checkPerm(player, "moonshard.info")) sendInfo(player);
                break;
            default:
                player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + "&cUnknown subcommand."));
                break;
        }

        return true;
    }

    private boolean checkPerm(Player player, String permission) {
        if (!player.hasPermission(permission)) {
            player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.no_permission")));
            return false;
        }
        return true;
    }

    private void sendInfo(Player player) {
        player.sendMessage(plugin.translate("&8&m---------------------------------"));
        player.sendMessage(plugin.translate("          &b&lMoonshards"));
        player.sendMessage(plugin.translate(" "));
        player.sendMessage(plugin.translate("&7Name: &f" + plugin.getDescription().getName()));
        player.sendMessage(plugin.translate("&7Version: &f" + plugin.getDescription().getVersion()));
        player.sendMessage(plugin.translate("&7Author: &fMoonprincess"));
        
        String url = plugin.getConfig().getString("link.url", "https://satsuya.de/linktree/Moon");
        String linkText = plugin.getConfig().getString("link.text", "&b&l[KLICK HIER]");
        String hoverText = plugin.getConfig().getString("link.hover", "&7Klicke hier, um die Website zu öffnen!");

        TextComponent message = new TextComponent(plugin.translate("&7Website: "));
        TextComponent link = new TextComponent(plugin.translate(linkText));
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(plugin.translate(hoverText))));
        
        message.addExtra(link);
        player.spigot().sendMessage(message);
        
        player.sendMessage(plugin.translate(" "));
        player.sendMessage(plugin.translate("&8&m---------------------------------"));
    }

    public void openMainGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, plugin.translate("&bMoonshards - Menü"));
        
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }

        // Fill row 1 and 3
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, filler);
            gui.setItem(i + 18, filler);
        }

        // Shop Left (Black Dye)
        ItemStack shopLeft = new ItemStack(Material.BLACK_DYE);
        ItemMeta leftMeta = shopLeft.getItemMeta();
        if (leftMeta != null) {
            leftMeta.setDisplayName(plugin.translate(plugin.getConfig().getString("shops.left.title", "&0&lSchwarzer Shop (Links)")));
            shopLeft.setItemMeta(leftMeta);
        }
        gui.setItem(12, shopLeft);

        // Shop Middle (Gold Apple)
        ItemStack shopMid = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta midMeta = shopMid.getItemMeta();
        if (midMeta != null) {
            midMeta.setDisplayName(plugin.translate(plugin.getConfig().getString("shops.middle.title", "&6&lGoldener Shop")));
            shopMid.setItemMeta(midMeta);
        }
        gui.setItem(13, shopMid);

        // Shop Right (Black Dye)
        ItemStack shopRight = new ItemStack(Material.BLACK_DYE);
        ItemMeta rightMeta = shopRight.getItemMeta();
        if (rightMeta != null) {
            rightMeta.setDisplayName(plugin.translate(plugin.getConfig().getString("shops.right.title", "&0&lSchwarzer Shop (Rechts)")));
            shopRight.setItemMeta(rightMeta);
        }
        gui.setItem(14, shopRight);
        
        player.openInventory(gui);
    }

    public void openShopGui(Player player, String shopKey) {
        String title = plugin.getConfig().getString("shops." + shopKey + ".title", "Shard Shop");
        int size = plugin.getConfig().getInt("shops." + shopKey + ".size", 27);
        Inventory gui = Bukkit.createInventory(null, size, plugin.translate(title));
        
        if (plugin.getConfig().getConfigurationSection("shops." + shopKey + ".items") != null) {
            for (String key : plugin.getConfig().getConfigurationSection("shops." + shopKey + ".items").getKeys(false)) {
                String path = "shops." + shopKey + ".items." + key;
                Material mat = Material.valueOf(plugin.getConfig().getString(path + ".material", "BARRIER"));
                int slot = plugin.getConfig().getInt(path + ".slot", 0);
                
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(plugin.translate(plugin.getConfig().getString(path + ".name", "&fItem")));
                    List<String> lore = plugin.getConfig().getStringList(path + ".lore").stream()
                            .map(plugin::translate)
                            .collect(Collectors.toList());
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                
                gui.setItem(slot, item);
            }
        }
        
        player.openInventory(gui);
    }

    private void openLeaderboardGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, plugin.translate("&6Moonshard Leaderboard"));
        
        Map<UUID, Long> top = plugin.getShardManager().getTopShards(10);
        int rank = 1;
        int[] slots = {13, 21, 22, 23, 29, 30, 31, 32, 33, 40};
        
        for (Map.Entry<UUID, Long> entry : top.entrySet()) {
            if (rank > slots.length) break;
            
            OfflinePlayer op = Bukkit.getOfflinePlayer(entry.getKey());
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(op);
                meta.setDisplayName(plugin.translate("&eRank #" + rank + " &7- &b" + (op.getName() != null ? op.getName() : "Unknown")));
                List<String> lore = new ArrayList<>();
                lore.add(plugin.translate("&7%shards%: &e" + entry.getValue()));
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            
            gui.setItem(slots[rank-1], head);
            rank++;
        }

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(plugin.translate("&cBack to Menu"));
            back.setItemMeta(backMeta);
        }
        gui.setItem(0, back);

        player.openInventory(gui);
    }

    private void handleDaily(Player player) {
        long lastClaim = plugin.getShardManager().getLastDaily(player.getUniqueId());
        long cooldownMillis = plugin.getConfig().getInt("daily_reward.cooldown_hours", 24) * 3600000L;
        long now = System.currentTimeMillis();

        if (now - lastClaim < cooldownMillis) {
            long remaining = cooldownMillis - (now - lastClaim);
            String time = String.format("%02d:%02d:%02d", 
                TimeUnit.MILLISECONDS.toHours(remaining),
                TimeUnit.MILLISECONDS.toMinutes(remaining) % 60,
                TimeUnit.MILLISECONDS.toSeconds(remaining) % 60);
            
            player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.daily_cooldown").replace("%time%", time)));
            return;
        }

        long amount = plugin.getConfig().getLong("daily_reward.amount", 100);
        plugin.getShardManager().addShards(player.getUniqueId(), amount);
        plugin.getShardManager().setLastDaily(player.getUniqueId(), now);
        plugin.getLogManager().log(player.getUniqueId(), "Claimed daily reward: " + amount + " shards");
        
        player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.daily_claimed").replace("%amount%", String.valueOf(amount))));
    }

    private void handleGamble(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.gamble_usage")));
            return;
        }

        try {
            long bet = Long.parseLong(args[1]);
            String choice = args[2].toLowerCase();

            if (!choice.equals("black") && !choice.equals("white")) {
                player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + "&cChoice must be 'black' or 'white'."));
                return;
            }

            long min = plugin.getConfig().getLong("gamble.min_bet", 10);
            long max = plugin.getConfig().getLong("gamble.max_bet", 10000);

            if (bet < min || bet > max) {
                player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.invalid_bet")
                        .replace("%min%", String.valueOf(min)).replace("%max%", String.valueOf(max))));
                return;
            }

            if (plugin.getShardManager().getShards(player.getUniqueId()) < bet) {
                player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.not_enough_shards")));
                return;
            }

            plugin.getShardManager().removeShards(player.getUniqueId(), bet);
            
            boolean win = random.nextBoolean();
            String result = win ? choice : (choice.equals("black") ? "white" : "black");

            player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + "&7The result is... &l" + result.toUpperCase()));

            if (win) {
                double tax = plugin.getConfig().getDouble("gamble.tax", 0.05);
                long winAmount = (long) (bet * (2.0 - tax));
                plugin.getShardManager().addShards(player.getUniqueId(), winAmount);
                plugin.getLogManager().log(player.getUniqueId(), "Gamble WIN: Bet " + bet + " on " + choice + " -> Won " + winAmount);
                player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.gamble_win").replace("%amount%", String.valueOf(winAmount))));
            } else {
                plugin.getLogManager().log(player.getUniqueId(), "Gamble LOSS: Bet " + bet + " on " + choice + " -> Lost " + bet);
                player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.gamble_loss").replace("%amount%", String.valueOf(bet))));
            }

        } catch (NumberFormatException e) {
            player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + "&cInvalid bet amount."));
        }
    }

    private void handlePay(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + "&cUsage: /shard pay <player> <amount>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + "&cPlayer not found."));
            return;
        }

        try {
            long amount = Long.parseLong(args[2]);
            if (amount <= 0) throw new NumberFormatException();

            if (plugin.getShardManager().getShards(player.getUniqueId()) < amount) {
                player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.not_enough_shards")));
                return;
            }

            plugin.getShardManager().removeShards(player.getUniqueId(), amount);
            plugin.getShardManager().addShards(target.getUniqueId(), amount);
            
            plugin.getLogManager().log(player.getUniqueId(), "Sent " + amount + " shards to " + target.getName());
            plugin.getLogManager().log(target.getUniqueId(), "Received " + amount + " shards from " + player.getName());

            player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.sent_shards")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%receiver%", target.getName())));
            
            target.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.received_shards")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%sender%", player.getName())));

        } catch (NumberFormatException e) {
            player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.prefix") + "&cInvalid amount."));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("shop", "daily", "gamble", "pay", "leaderboard", "info").stream()
                    .filter(s -> sender.hasPermission("moonshard." + s))
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("pay") && sender.hasPermission("moonshard.pay")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("gamble") && sender.hasPermission("moonshard.gamble")) {
                return Arrays.asList("10", "50", "100", "500", "1000");
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("gamble") && sender.hasPermission("moonshard.gamble")) {
                return Arrays.asList("black", "white").stream()
                        .filter(s -> s.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}
