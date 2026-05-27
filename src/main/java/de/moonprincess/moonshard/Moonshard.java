package de.moonprincess.moonshard;

import de.moonprincess.moonshard.commands.ShardAdminCommand;
import de.moonprincess.moonshard.commands.ShardCommand;
import de.moonprincess.moonshard.listeners.ShardListener;
import de.moonprincess.moonshard.manager.LogManager;
import de.moonprincess.moonshard.manager.ShardManager;
import de.moonprincess.moonshard.placeholder.MoonshardPlaceholder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Moonshard extends JavaPlugin {

    private ShardManager shardManager;
    private LogManager logManager;
    private ShardCommand shardCommand;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.shardManager = new ShardManager(this);
        this.logManager = new LogManager(this);

        this.shardCommand = new ShardCommand(this);
        getCommand("shard").setExecutor(shardCommand);
        getCommand("shard").setTabCompleter(shardCommand);

        ShardAdminCommand shardAdminCommand = new ShardAdminCommand(this);
        getCommand("shardadmin").setExecutor(shardAdminCommand);
        getCommand("shardadmin").setTabCompleter(shardAdminCommand);

        getServer().getPluginManager().registerEvents(new ShardListener(this, shardCommand), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new MoonshardPlaceholder(this).register();
        }

        startIncomeTask();

        getLogger().info("Moonshard has been enabled!");
    }

    @Override
    public void onDisable() {
        if (shardManager != null) {
            shardManager.saveAll();
        }
    }

    private void startIncomeTask() {
        if (!getConfig().getBoolean("income.enabled", true)) return;

        int interval = getConfig().getInt("income.interval_minutes", 1) * 20 * 60;
        long amount = getConfig().getLong("income.amount", 1);
        String permission = getConfig().getString("income.permission", "moonshard.income");

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission(permission))
                    .forEach(player -> {
                        shardManager.addShards(player.getUniqueId(), amount);
                        // Optional: logs for passive income (could be too many)
                        // logManager.log(player.getUniqueId(), "Received " + amount + " passive shards");
                    });
        }, interval, (long) interval);
    }

    public String translate(String message) {
        if (message == null) return "";
        
        String currency = getConfig().getString("currency_name", "&#ff1e95S&#ff3ca2h&#ff5ab0a&#ff77bdr&#ff95cad&#ffb3d7s");
        message = message.replace("%shards%", currency);

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + group).toString());
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public ShardCommand getShardCommand() {
        return shardCommand;
    }
}
