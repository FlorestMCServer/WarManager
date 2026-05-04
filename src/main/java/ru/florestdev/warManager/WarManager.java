package ru.florestdev.warManager;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class WarManager extends JavaPlugin {

    ConfigManager configManager = null;
    public Map<UUID, List<PassportData>> passports = new HashMap<>();
    Map<String, String> wars = new HashMap<>();

    Map<String, String> requests = new HashMap<>();

    @Override
    public void onEnable() {
        Plugin passportManager = getServer().getPluginManager().getPlugin("PassportManager");
        if (passportManager == null) {
            getLogger().severe("PluginManager is not enabled! Install it!");
            getLogger().severe("Disabling War Manager...");
            getServer().getPluginManager().disablePlugin(this);
        }
        ConfigManager managerPassports = new ConfigManager(passportManager);
        configManager = managerPassports;
        passports = managerPassports.getDatabase();
        getCommand("war").setExecutor(new WarCommandExecutor(this));
        new IsWarProcessed(this).startGlobalCheck();
    }

    @Override
    public void onDisable() {
        getLogger().info("DISABLING...");
    }
}
