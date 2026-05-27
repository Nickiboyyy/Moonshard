package de.moonprincess.moonshard.manager;

import de.moonprincess.moonshard.Moonshard;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class LogManager {

    private final Moonshard plugin;
    private final File logFolder;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public LogManager(Moonshard plugin) {
        this.plugin = plugin;
        this.logFolder = new File(plugin.getDataFolder(), "logs");
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }
    }

    public void log(UUID uuid, String action) {
        File userFile = new File(logFolder, uuid.toString() + ".log");
        List<String> logs = new ArrayList<>();

        if (userFile.exists()) {
            try {
                logs = Files.readAllLines(userFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String time = dateFormat.format(new Date());
        logs.add("[" + time + "] " + action);

        // Limit to 200 entries
        if (logs.size() > 200) {
            logs = logs.subList(logs.size() - 200, logs.size());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userFile))) {
            for (String line : logs) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getLogs(UUID uuid) {
        File userFile = new File(logFolder, uuid.toString() + ".log");
        if (!userFile.exists()) return new ArrayList<>();
        try {
            return Files.readAllLines(userFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
