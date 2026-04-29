package com.hydrasoftware.hydraeconomy.commands;

import com.hydrasoftware.hydraeconomy.HydraEconomy;
import com.hydrasoftware.hydraeconomy.economy.DailyRewardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class EconomyCommand implements CommandExecutor {

    private final HydraEconomy plugin;

    public EconomyCommand(HydraEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (command.getName().toLowerCase()) {
            case "hydras":
                return handleBalance(sender, args);
            case "pay":
                return handlePay(sender, args);
            case "daily":
                return handleDaily(sender);
            default:
                return false;
        }
    }

    private boolean handleBalance(CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (args.length == 1 && sender.hasPermission("hydraeconomy.admin")) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("Jugador no encontrado.").color(NamedTextColor.RED));
                return true;
            }
            BigDecimal balance = plugin.getEconomyManager().getBalance(target.getUniqueId());
            sender.sendMessage(Component.text(target.getName() + " tiene " + balance + " Hydras.").color(NamedTextColor.GOLD));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Solo los jugadores pueden usar este comando.").color(NamedTextColor.RED));
            return true;
        }

        BigDecimal balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        sender.sendMessage(Component.text("Tienes " + balance + " Hydras.").color(NamedTextColor.GOLD));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== HYDRAECONOMY - AYUDA ===").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/hydras [jugador] - Ver saldo de Hydras").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/pay <jugador> <cantidad> - Pagar Hydras a otro jugador").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/daily - Reclamar recompensa diaria (10 Hydras, +5 cada 5 dias)").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mercado help - Sistema de mercado").color(NamedTextColor.YELLOW));
        if (sender.hasPermission("hydraeconomy.admin")) {
            sender.sendMessage(Component.text("").color(NamedTextColor.WHITE));
            sender.sendMessage(Component.text("=== ADMIN ===").color(NamedTextColor.RED));
            sender.sendMessage(Component.text("/hydrasadmin give <jugador> <cantidad> - Dar Hydras").color(NamedTextColor.RED));
            sender.sendMessage(Component.text("/hydrasadmin remove <jugador> <cantidad> - Quitar Hydras").color(NamedTextColor.RED));
            sender.sendMessage(Component.text("/hydrasadmin set <jugador> <cantidad> - Establecer saldo").color(NamedTextColor.RED));
            sender.sendMessage(Component.text("/hydrasadmin reload - Recargar configuracion").color(NamedTextColor.RED));
        }
        sender.sendMessage(Component.text("").color(NamedTextColor.WHITE));
        sender.sendMessage(Component.text("Documentacion completa: https://github.com/HydraSoftwareGH/Hydra-economy")
                .color(NamedTextColor.AQUA)
                .clickEvent(ClickEvent.openUrl("https://github.com/HydraSoftwareGH/Hydra-economy/blob/main/README.md"))
                .hoverEvent(Component.text("Click para abrir la documentacion").color(NamedTextColor.GRAY)));
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Solo los jugadores pueden usar este comando.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Uso correcto: /pay <jugador> <cantidad>").color(NamedTextColor.RED));
            sender.sendMessage(Component.text("Usa /hydras help para ver la ayuda completa.").color(NamedTextColor.YELLOW));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Jugador no encontrado.").color(NamedTextColor.RED));
            return true;
        }

        if (target.equals(player)) {
            sender.sendMessage(Component.text("No puedes pagarte a ti mismo.").color(NamedTextColor.RED));
            return true;
        }

        try {
            BigDecimal amount = new BigDecimal(args[1]);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                sender.sendMessage(Component.text("La cantidad debe ser positiva.").color(NamedTextColor.RED));
                return true;
            }

            if (plugin.getEconomyManager().transfer(player.getUniqueId(), target.getUniqueId(), amount)) {
                player.sendMessage(Component.text("Pagaste " + amount + " Hydras a " + target.getName() + ".").color(NamedTextColor.GREEN));
                target.sendMessage(Component.text("Recibiste " + amount + " Hydras de " + player.getName() + ".").color(NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("No tienes suficientes Hydras.").color(NamedTextColor.RED));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Cantidad invalida.").color(NamedTextColor.RED));
        }
        return true;
    }

    private boolean handleDaily(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Solo los jugadores pueden usar este comando.").color(NamedTextColor.RED));
            return true;
        }

        DailyRewardManager.ClaimResult result = plugin.getDailyRewardManager().claim(player.getUniqueId());
        if (result.success) {
            player.sendMessage(Component.text(result.message).color(NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text(result.message).color(NamedTextColor.RED));
        }
        return true;
    }
}
