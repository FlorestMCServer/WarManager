package ru.florestdev.warManager;

test

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.*;

public class ConfigManager {
    private final Plugin plugin;
    private FileConfiguration countriesCfg;
    private final File passportsFile;
    public Map<UUID, List<PassportData>> database = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.passportsFile = new File(plugin.getDataFolder(), "passports.json");
        reloadCountries();
        loadPassports();
    }

    public void reloadCountries() {
        File f = new File(plugin.getDataFolder(), "countries.yml");
        if (!f.exists()) plugin.saveResource("countries.yml", false);
        countriesCfg = YamlConfiguration.loadConfiguration(f);
    }

    private void loadPassports() {
        if (!passportsFile.exists()) return;
        try (Reader reader = new FileReader(passportsFile)) {
            database = gson.fromJson(reader, new TypeToken<Map<UUID, List<PassportData>>>(){}.getType());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void savePassports() {
        try (Writer writer = new FileWriter(passportsFile)) {
            gson.toJson(database, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public List<String> getLeaders(String country) {
        return countriesCfg.getStringList("countries." + country + ".leaders");
    }

    public boolean countryExists(String country) {
        return countriesCfg.contains("countries." + country);
    }

    public Map<UUID, List<PassportData>> getDatabase() { return database; }
}
