package com.hydrasoftware.hydraeconomy.commands;

import com.hydrasoftware.hydraeconomy.HydraEconomy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class AdminEconomyCommand implements CommandExecutor {

    private final HydraEconomy plugin;

    public AdminEconomyCommand(HydraEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("hydraeconomy.admin")) {
            sender.sendMessage(Component.text("No tienes permiso para usar este comando.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Jugador no encontrado.").color(NamedTextColor.RED));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Uso: /hydrasadmin give <jugador> <cantidad>").color(NamedTextColor.RED));
                    return true;
                }
                try {
                    BigDecimal amount = new BigDecimal(args[2]);
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        sender.sendMessage(Component.text("La cantidad debe ser positiva.").color(NamedTextColor.RED));
                        return true;
                    }
                    plugin.getEconomyManager().deposit(target.getUniqueId(), amount);
                    sender.sendMessage(Component.text("Diste " + amount + " Hydras a " + target.getName() + ".").color(NamedTextColor.GREEN));
                    target.sendMessage(Component.text("Has recibido " + amount + " Hydras.").color(NamedTextColor.GREEN));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Cantidad invalida.").color(NamedTextColor.RED));
                }
            }
            case "remove" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Uso: /hydrasadmin remove <jugador> <cantidad>").color(NamedTextColor.RED));
                    return true;
                }
                try {
                    BigDecimal amount = new BigDecimal(args[2]);
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        sender.sendMessage(Component.text("La cantidad debe ser positiva.").color(NamedTextColor.RED));
                        return true;
                    }
                    if (plugin.getEconomyManager().withdraw(target.getUniqueId(), amount)) {
                        sender.sendMessage(Component.text("Quitaste " + amount + " Hydras a " + target.getName() + ".").color(NamedTextColor.GREEN));
                        target.sendMessage(Component.text("Te quitaron " + amount + " Hydras.").color(NamedTextColor.RED));
                    } else {
                        sender.sendMessage(Component.text(target.getName() + " no tiene suficientes Hydras.").color(NamedTextColor.RED));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Cantidad invalida.").color(NamedTextColor.RED));
                }
            }
            case "set" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Uso: /hydrasadmin set <jugador> <cantidad>").color(NamedTextColor.RED));
                    return true;
                }
                try {
                    BigDecimal amount = new BigDecimal(args[2]);
                    if (amount.compareTo(BigDecimal.ZERO) < 0) {
                        sender.sendMessage(Component.text("La cantidad no puede ser negativa.").color(NamedTextColor.RED));
                        return true;
                    }
                    plugin.getEconomyManager().setBalance(target.getUniqueId(), amount);
                    sender.sendMessage(Component.text("Estableciste el saldo de " + target.getName() + " a " + amount + " Hydras.").color(NamedTextColor.GREEN));
                    target.sendMessage(Component.text("Tu saldo ha sido establecido a " + amount + " Hydras.").color(NamedTextColor.GREEN));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Cantidad invalida.").color(NamedTextColor.RED));
                }
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getEconomyManager().load();
                plugin.getMarketManager().load();
                plugin.getDailyRewardManager().load();
                sender.sendMessage(Component.text("Configuracion recargada.").color(NamedTextColor.GREEN));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== HYDRAECONOMY ADMIN ===").color(NamedTextColor.RED));
        sender.sendMessage(Component.text("/hydrasadmin give <jugador> <cantidad> - Dar Hydras").color(NamedTextColor.RED));
        sender.sendMessage(Component.text("/hydrasadmin remove <jugador> <cantidad> - Quitar Hydras").color(NamedTextColor.RED));
        sender.sendMessage(Component.text("/hydrasadmin set <jugador> <cantidad> - Establecer saldo").color(NamedTextColor.RED));
        sender.sendMessage(Component.text("/hydrasadmin reload - Recargar configuracion").color(NamedTextColor.RED));
        sender.sendMessage(Component.text("").color(NamedTextColor.WHITE));
        sender.sendMessage(Component.text("Documentacion completa: https://github.com/HydraSoftwareGH/Hydra-economy/blob/main/README.md").color(NamedTextColor.AQUA));
    }
}
