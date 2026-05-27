package de.moonprincess.moonshard.manager;

import de.moonprincess.moonshard.Moonshard;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ShardManager {

    private final Moonshard plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, Long> shardCache = new HashMap<>();
    private final Map<UUID, Long> lastDailyCache = new HashMap<>();

    public ShardManager(Moonshard plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadAll();
    }

    public void loadAll() {
        if (dataConfig.getConfigurationSection("shards") != null) {
            for (String key : dataConfig.getConfigurationSection("shards").getKeys(false)) {
                shardCache.put(UUID.fromString(key), dataConfig.getLong("shards." + key));
            }
        }
        if (dataConfig.getConfigurationSection("daily") != null) {
            for (String key : dataConfig.getConfigurationSection("daily").getKeys(false)) {
                lastDailyCache.put(UUID.fromString(key), dataConfig.getLong("daily." + key));
            }
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, Long> entry : shardCache.entrySet()) {
            dataConfig.set("shards." + entry.getKey().toString(), entry.getValue());
        }
        for (Map.Entry<UUID, Long> entry : lastDailyCache.entrySet()) {
            dataConfig.set("daily." + entry.getKey().toString(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getShards(UUID uuid) {
        return shardCache.getOrDefault(uuid, 0L);
    }

    public void setShards(UUID uuid, long amount) {
        shardCache.put(uuid, Math.max(0, amount));
    }

    public void addShards(UUID uuid, long amount) {
        setShards(uuid, getShards(uuid) + amount);
    }

    public void removeShards(UUID uuid, long amount) {
        setShards(uuid, getShards(uuid) - amount);
    }

    public void resetAllOnline() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            setShards(player.getUniqueId(), 0);
        }
    }

    public void resetAll() {
        shardCache.clear();
        dataConfig.set("shards", null);
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getLastDaily(UUID uuid) {
        return lastDailyCache.getOrDefault(uuid, 0L);
    }

    public void setLastDaily(UUID uuid, long time) {
        lastDailyCache.put(uuid, time);
    }

    public Map<UUID, Long> getTopShards(int limit) {
        return shardCache.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
