package ru.florestdev.warManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class WarCommandExecutor implements CommandExecutor {

    private final WarManager m;

    public WarCommandExecutor(WarManager m) {
        this.m = m;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cИспользуй: /war <create|stop|decline|accept> [аргументы]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(sender, args);
                break;

            case "accept":
                handleAccept(sender);
                break;

            case "decline":
                handleDecline(sender);
                break;

            case "stop":
                handleStop(sender, args);
                break;

            default:
                sender.sendMessage("§cНеизвестная подкоманда.");
                break;
        }

        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cИспользуй: /war create <твоя_страна> <страна_враг>");
            return;
        }

        String myCountry = args[1];
        String enemyCountry = args[2];
        List<String> myLeaders = m.configManager.getLeaders(myCountry);

        // Проверка, является ли отправитель лидером своей страны
        if (myLeaders == null || !myLeaders.contains(sender.getName())) {
            sender.sendMessage("§cВы не являетесь лидером страны %s.".formatted(myCountry));
            return;
        }

        if (!m.configManager.countryExists(enemyCountry)) {
            sender.sendMessage("§cСтраны %s не существует.".formatted(enemyCountry));
            return;
        }

        List<String> enemyLeaders = m.configManager.getLeaders(enemyCountry);
        if (enemyLeaders == null || enemyLeaders.isEmpty()) {
            sender.sendMessage("§cУ этой страны нет лидеров в сети или они не назначены.");
            return;
        }

        // Записываем запрос: КТО объявил -> КОМУ объявили (берем первого лидера врага для мапы)
        String targetLeader = enemyLeaders.get(0);
        m.requests.put(sender.getName(), targetLeader);

        sender.sendMessage("§aЗапрос на создание войны отправлен лидерам %s!".formatted(enemyCountry));

        for (String leaderName : enemyLeaders) {
            Player target = Bukkit.getPlayer(leaderName);
            if (target != null) {
                target.sendMessage("§c§lВНИМАНИЕ! §eЛидер %s из страны %s объявил вам войну!".formatted(sender.getName(), myCountry));
                target.sendMessage("§7Используйте §a/war accept §7или §c/war decline");
            }
        }
    }

    private void handleAccept(CommandSender sender) {
        // Ищем, кто отправлял запрос текущему игроку
        String attacker = null;
        for (Map.Entry<String, String> entry : m.requests.entrySet()) {
            if (entry.getValue().equals(sender.getName())) {
                attacker = entry.getKey();
                break;
            }
        }

        if (attacker == null) {
            sender.sendMessage("§cУ вас нет активных запросов на войну.");
            return;
        }

        // Переносим из запросов в активные войны
        m.requests.remove(attacker);
        m.wars.put(attacker, sender.getName());

        Bukkit.broadcastMessage("§4§lВОЙНА! §6%s §eпринял вызов от §6%s§e. Мир содрогнется!".formatted(sender.getName(), attacker));
    }

    private void handleDecline(CommandSender sender) {
        String attacker = null;
        for (Map.Entry<String, String> entry : m.requests.entrySet()) {
            if (entry.getValue().equals(sender.getName())) {
                attacker = entry.getKey();
                break;
            }
        }

        if (attacker == null) {
            sender.sendMessage("§cУ вас нет активных запросов.");
            return;
        }

        m.requests.remove(attacker);
        sender.sendMessage("§eВы отклонили запрос на войну.");

        Player attackerPlayer = Bukkit.getPlayer(attacker);
        if (attackerPlayer != null) {
            attackerPlayer.sendMessage("§c%s отклонил ваш запрос на войну. Трус!".formatted(sender.getName()));
        }
    }

    private void handleStop(CommandSender sender, String[] args) {
        // Логика остановки: либо админ, либо один из участников
        String player = sender.getName();
        String partner = null;

        if (m.wars.containsKey(player)) {
            partner = m.wars.remove(player);
        } else {
            // Ищем, если игрок был тем, кто принимал (value в мапе)
            for (Map.Entry<String, String> entry : m.wars.entrySet()) {
                if (entry.getValue().equals(player)) {
                    partner = entry.getKey();
                    m.wars.remove(partner);
                    break;
                }
            }
        }

        if (partner == null) {
            sender.sendMessage("§cВы не находитесь в состоянии войны.");
            return;
        }

        Bukkit.broadcastMessage("§aВойна между %s и %s завершена.".formatted(player, partner));
    }
}