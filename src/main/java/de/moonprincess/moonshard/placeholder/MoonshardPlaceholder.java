package de.moonprincess.moonshard.placeholder;

import de.moonprincess.moonshard.Moonshard;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MoonshardPlaceholder extends PlaceholderExpansion {

    private final Moonshard plugin;

    public MoonshardPlaceholder(Moonshard plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "Moonprincess";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "moonshard";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // %moonshard_shards% - Aktuelle Shards des Spielers
        if (params.equalsIgnoreCase("shards")) {
            return String.valueOf(plugin.getShardManager().getShards(player.getUniqueId()));
        }

        // %moonshard_shards_formatted% - Formatiert (z.B. mit Tausender-Trenner)
        if (params.equalsIgnoreCase("shards_formatted")) {
            return String.format("%,d", plugin.getShardManager().getShards(player.getUniqueId()));
        }

        // %moonshard_top_name_<rank>% - Name des Spielers auf Platz X
        if (params.toLowerCase().startsWith("top_name_")) {
            try {
                int rank = Integer.parseInt(params.split("_")[2]);
                List<Map.Entry<UUID, Long>> top = new ArrayList<>(plugin.getShardManager().getTopShards(rank).entrySet());
                if (top.size() >= rank) {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(top.get(rank - 1).getKey());
                    return op.getName() != null ? op.getName() : "Unknown";
                }
                return "---";
            } catch (Exception e) {
                return "Error";
            }
        }

        // %moonshard_top_shards_<rank>% - Shards des Spielers auf Platz X
        if (params.toLowerCase().startsWith("top_shards_")) {
            try {
                int rank = Integer.parseInt(params.split("_")[2]);
                List<Map.Entry<UUID, Long>> top = new ArrayList<>(plugin.getShardManager().getTopShards(rank).entrySet());
                if (top.size() >= rank) {
                    return String.valueOf(top.get(rank - 1).getValue());
                }
                return "0";
            } catch (Exception e) {
                return "0";
            }
        }

        return null;
    }
}
