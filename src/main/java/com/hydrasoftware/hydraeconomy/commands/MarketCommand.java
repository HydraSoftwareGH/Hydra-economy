package com.hydrasoftware.hydraeconomy.commands;

import com.hydrasoftware.hydraeconomy.HydraEconomy;
import com.hydrasoftware.hydraeconomy.economy.MarketManager;
import com.hydrasoftware.hydraeconomy.gui.MarketGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class MarketCommand implements CommandExecutor {

    private final HydraEconomy plugin;

    public MarketCommand(HydraEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Solo los jugadores pueden usar este comando.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 1 || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("lista")) {
            MarketGUI.open(player, plugin);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help", "ayuda" -> sendHelp(player);
            case "sell", "vender" -> handleSell(player, args);
            case "buy", "comprar" -> handleBuy(player, args);
            case "cancel", "cancelar" -> handleCancel(player, args);
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Component.text("=== MERCADO ===").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("/mercado sell <precio> - Vender item en mano").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/mercado buy <id> - Comprar item por ID").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/mercado list - Ver items en venta").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/mercado cancel <id> - Cancelar publicacion").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("").color(NamedTextColor.WHITE));
        player.sendMessage(Component.text("Documentacion completa: https://github.com/HydraSoftwareGH/Hydra-economy")
                .color(NamedTextColor.AQUA)
                .clickEvent(ClickEvent.openUrl("https://github.com/HydraSoftwareGH/Hydra-economy/blob/main/README.md"))
                .hoverEvent(Component.text("Click para abrir la documentacion").color(NamedTextColor.GRAY)));
    }

    private void handleSell(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Uso correcto: /mercado sell <precio>").color(NamedTextColor.RED));
            player.sendMessage(Component.text("Usa /mercado help para ver la ayuda completa.").color(NamedTextColor.YELLOW));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(Component.text("Debes tener un item en la mano.").color(NamedTextColor.RED));
            return;
        }

        try {
            BigDecimal price = new BigDecimal(args[1]);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                player.sendMessage(Component.text("El precio debe ser positivo.").color(NamedTextColor.RED));
                return;
            }

            item.setAmount(1);
            item = item.clone();
            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);

            int id = plugin.getMarketManager().createListing(player.getUniqueId(), price, item);
            player.sendMessage(Component.text("Publicaste " + item.getType().name() + " por " + price + " Hydras. (ID: " + id + ")").color(NamedTextColor.GREEN));
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Precio invalido.").color(NamedTextColor.RED));
        }
    }

    private void handleBuy(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Uso: /mercado buy <id>").color(NamedTextColor.RED));
            return;
        }

        try {
            int id = Integer.parseInt(args[1]);
            MarketManager.MarketListing listing = plugin.getMarketManager().getListing(id);

            if (listing == null) {
                player.sendMessage(Component.text("Ese anuncio no existe.").color(NamedTextColor.RED));
                return;
            }

            if (plugin.getMarketManager().buyListing(id, player.getUniqueId())) {
                player.getInventory().addItem(listing.item);
                player.sendMessage(Component.text("Compraste " + listing.item.getType().name() + " por " + listing.price + " Hydras.").color(NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("No tienes suficientes Hydras o el anuncio ya no esta disponible.").color(NamedTextColor.RED));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("ID invalido.").color(NamedTextColor.RED));
        }
    }

    private void handleCancel(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Uso: /mercado cancel <id>").color(NamedTextColor.RED));
            return;
        }

        try {
            int id = Integer.parseInt(args[1]);
            MarketManager.MarketListing listing = plugin.getMarketManager().getListing(id);

            if (listing == null) {
                player.sendMessage(Component.text("Ese anuncio no existe.").color(NamedTextColor.RED));
                return;
            }

            if (plugin.getMarketManager().cancelListing(id, player.getUniqueId())) {
                player.getInventory().addItem(listing.item);
                player.sendMessage(Component.text("Anuncio ID " + id + " cancelado. Item devuelto.").color(NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("No puedes cancelar un anuncio que no es tuyo.").color(NamedTextColor.RED));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("ID invalido.").color(NamedTextColor.RED));
        }
    }
}
