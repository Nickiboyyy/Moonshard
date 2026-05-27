package de.moonprincess.moonshard.commands;

import de.moonprincess.moonshard.Moonshard;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShardAdminCommand implements CommandExecutor, TabCompleter {

    private final Moonshard plugin;

    public ShardAdminCommand(Moonshard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("moonshard.admin")) {
            sender.sendMessage(plugin.translate(plugin.getConfig().getString("messages.no_permission")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.translate("&cUsage: /shardadmin <set|add|remove|reset|addshopitem|logs> ..."));
            return true;
        }

        String action = args[0].toLowerCase();

        if (action.equals("logs") || action.equals("log")) {
            handleLogs(sender, args);
            return true;
        }

        if (action.equals("reset")) {
            if (args.length < 2) {
                sender.sendMessage(plugin.translate("&cUsage: /shardadmin reset <allonline|all>"));
                return true;
            }
            if (args[1].equalsIgnoreCase("allonline")) {
                plugin.getShardManager().resetAllOnline();
                sender.sendMessage(plugin.translate("&aSuccessfully reset %shards% for all online players."));
            } else if (args[1].equalsIgnoreCase("all")) {
                plugin.getShardManager().resetAll();
                sender.sendMessage(plugin.translate("&aSuccessfully reset %shards% for EVERYONE (data cleared)."));
            }
            return true;
        }

        if (action.equals("addshopitem")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(plugin.translate("&cUsage: /shardadmin addshopitem <id> <price>"));
                return true;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage(plugin.translate("&cYou must hold an item in your hand."));
                return true;
            }
            String id = args[1];
            int price = Integer.parseInt(args[2]);
            
            plugin.getConfig().set("shop.items." + id + ".material", item.getType().name());
            plugin.getConfig().set("shop.items." + id + ".name", item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name());
            plugin.getConfig().set("shop.items." + id + ".price", price);
            plugin.getConfig().set("shop.items." + id + ".commands", Arrays.asList("give %player% " + item.getType().name() + " 1"));
            plugin.saveConfig();
            
            player.sendMessage(plugin.translate("&aAdded item &e" + id + " &ato the shop for &e" + price + " %shards%&a."));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(plugin.translate("&cUsage: /shardadmin <set|add|remove> <player> <amount>"));
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(plugin.translate("&cPlayer has never played on this server."));
            return true;
        }

        UUID uuid = target.getUniqueId();
        try {
            long amount = Long.parseLong(args[2]);
            if (amount < 0) throw new NumberFormatException();

            switch (action) {
                case "set":
                    plugin.getShardManager().setShards(uuid, amount);
                    plugin.getLogManager().log(uuid, "Admin " + sender.getName() + " set balance to " + amount);
                    sender.sendMessage(plugin.translate("&7Set &b" + target.getName() + "'s %shards% &7to &e" + amount));
                    break;
                case "add":
                    plugin.getShardManager().addShards(uuid, amount);
                    plugin.getLogManager().log(uuid, "Admin " + sender.getName() + " added " + amount + " shards");
                    sender.sendMessage(plugin.translate("&7Added &e" + amount + " %shards% &7to &b" + target.getName()));
                    break;
                case "remove":
                    plugin.getShardManager().removeShards(uuid, amount);
                    plugin.getLogManager().log(uuid, "Admin " + sender.getName() + " removed " + amount + " shards");
                    sender.sendMessage(plugin.translate("&7Removed &e" + amount + " %shards% &7from &b" + target.getName()));
                    break;
                default:
                    sender.sendMessage(plugin.translate("&cInvalid action."));
                    break;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.translate("&cInvalid amount."));
        }

        return true;
    }

    private void handleLogs(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.translate("&cUsage: /shardadmin logs <player> [page]"));
            return;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || !target.hasPlayedBefore()) {
            sender.sendMessage(plugin.translate("&cPlayer not found or never played."));
            return;
        }

        List<String> logs = plugin.getLogManager().getLogs(target.getUniqueId());
        if (logs.isEmpty()) {
            sender.sendMessage(plugin.translate("&cNo logs found for this player."));
            return;
        }

        int page = 1;
        if (args.length >= 3) {
            try {
                page = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {}
        }

        int entriesPerPage = 10;
        int maxPages = (int) Math.ceil((double) logs.size() / entriesPerPage);
        page = Math.max(1, Math.min(page, maxPages));

        sender.sendMessage(plugin.translate("&8&m----------&b Logs: " + target.getName() + " &7(Page " + page + "/" + maxPages + ") &8&m----------"));
        
        int start = (page - 1) * entriesPerPage;
        int end = Math.min(start + entriesPerPage, logs.size());

        for (int i = start; i < end; i++) {
            sender.sendMessage(plugin.translate("&7" + (i + 1) + ". &f" + logs.get(i)));
        }
        sender.sendMessage(plugin.translate("&8&m-------------------------------------------"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("set", "add", "remove", "reset", "addshopitem", "logs").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset")) return Arrays.asList("allonline", "all");
            if (args[0].equalsIgnoreCase("addshopitem")) return Arrays.asList("<id>");
            
            // For other subcommands, suggest online players as primary targets
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("addshopitem")) return Arrays.asList("<price>");
            if (args[0].equalsIgnoreCase("logs")) return Arrays.asList("1", "2", "3", "4");
            return Arrays.asList("10", "100", "1000");
        }

        return new ArrayList<>();
    }
}
