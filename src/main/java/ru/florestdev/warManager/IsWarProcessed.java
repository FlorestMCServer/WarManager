package ru.florestdev.warManager;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

public class IsWarProcessed {
    private final WarManager plugin;
    // Cache: Attacker Name -> BossBar
    private final Map<String, BossBar> activeBars = new HashMap<>();

    public IsWarProcessed(WarManager plugin) {
        this.plugin = plugin;
    }

    public void startGlobalCheck() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 1. Iterate through active wars
                for (Map.Entry<String, String> entry : plugin.wars.entrySet()) {
                    String attacker = entry.getKey();
                    String defender = entry.getValue();

                    // 2. Get or create BossBar
                    BossBar bar = activeBars.computeIfAbsent(attacker, k -> {
                        BossBar newBar = Bukkit.createBossBar(
                                "§4§lВОЙНА: §e" + attacker + " §7vs §e" + defender,
                                BarColor.RED,
                                BarStyle.SOLID
                        );
                        return newBar;
                    });

                    // 3. Update viewers (Optimized)
                    updateBarPlayers(bar, attacker, defender);

                    bar.setProgress(1.0); // Replace with logic to update based on time/points
                }

                // 4. Cleanup: Remove bars for finished wars
                activeBars.entrySet().removeIf(entry -> {
                    if (!plugin.wars.containsKey(entry.getKey())) {
                        entry.getValue().removeAll();
                        return true;
                    }
                    return false;
                });
            }
        }.runTaskTimer(plugin, 0L, 40L); // Changed to 2 seconds (40L) for better performance
    }

    private void updateBarPlayers(BossBar bar, String attacker, String defender) {
        // Create a set of players who SHOULD see the bar
        Set<UUID> targetPlayerUUIDs = new HashSet<>();

        // --- IMPORTANT: ADD YOUR LOGIC HERE ---
        // Example: Get all players in "attacker" or "defender" country
        // For now, adding ALL online players to demonstrate:
        for (Player p : Bukkit.getOnlinePlayers()) {
            targetPlayerUUIDs.add(p.getUniqueId());
        }
        // --------------------------------------

        // Remove players who should NOT see it anymore
        bar.getPlayers().removeIf(player -> !targetPlayerUUIDs.contains(player.getUniqueId()));

        // Add players who SHOULD see it but don't
        for (UUID uuid : targetPlayerUUIDs) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && !bar.getPlayers().contains(player)) {
                bar.addPlayer(player);
            }
        }
    }
}