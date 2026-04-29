package com.hydrasoftware.hydraeconomy.gui;

import com.hydrasoftware.hydraeconomy.HydraEconomy;
import com.hydrasoftware.hydraeconomy.economy.MarketManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MarketGUI implements Listener {

    private static final int PAGE_SIZE = 27;
    private static final Map<UUID, Integer> playerPages = new HashMap<>();

    public static void open(Player player, HydraEconomy plugin) {
        open(player, plugin, 0);
    }

    public static void open(Player player, HydraEconomy plugin, int page) {
        List<MarketManager.MarketListing> listings = plugin.getMarketManager().getListings();

        if (listings.isEmpty()) {
            player.sendMessage(Component.text("No hay nada publicado en el mercado.").color(NamedTextColor.YELLOW));
            return;
        }

        int totalPages = (listings.size() - 1) / PAGE_SIZE + 1;
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text("Mercado - Pagina " + (page + 1) + "/" + totalPages));

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, listings.size());

        for (int i = start; i < end; i++) {
            MarketManager.MarketListing listing = listings.get(i);
            ItemStack displayItem = listing.item.clone();
            ItemMeta meta = displayItem.getItemMeta();

            List<Component> lore = meta.lore();
            if (lore == null) lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("ID: " + listing.id).color(NamedTextColor.GRAY));
            lore.add(Component.text("Precio: " + listing.price + " Hydras").color(NamedTextColor.GOLD));
            lore.add(Component.text("Click para comprar").color(NamedTextColor.GREEN));
            meta.lore(lore);
            displayItem.setItemMeta(meta);

            inv.setItem(i - start, displayItem);
        }

        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.displayName(Component.text("Pagina anterior").color(NamedTextColor.YELLOW));
            prev.setItemMeta(prevMeta);
            inv.setItem(18, prev);
        }

        if (page + 1 < totalPages) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(Component.text("Pagina siguiente").color(NamedTextColor.YELLOW));
            next.setItemMeta(nextMeta);
            inv.setItem(26, next);
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("Cerrar").color(NamedTextColor.RED));
        close.setItemMeta(closeMeta);
        inv.setItem(22, close);

        playerPages.put(player.getUniqueId(), page);
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().title() instanceof Component title)) return;
        String titleStr = ((Component) title).toString();
        if (!titleStr.contains("Mercado")) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        ItemStack clicked = event.getCurrentItem();
        HydraEconomy plugin = (HydraEconomy) Bukkit.getPluginManager().getPlugin("HydraEconomy");
        if (plugin == null) return;

        if (clicked.getType() == Material.ARROW) {
            int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
            if (clicked.getItemMeta() != null && clicked.getItemMeta().displayName() != null) {
                String displayName = ((Component) clicked.getItemMeta().displayName()).toString();
                if (displayName.contains("anterior")) {
                    open(player, plugin, currentPage - 1);
                    return;
                }
                if (displayName.contains("siguiente")) {
                    open(player, plugin, currentPage + 1);
                    return;
                }
            }
        }

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            playerPages.remove(player.getUniqueId());
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || meta.lore() == null) return;

        int id = -1;
        for (Component line : meta.lore()) {
            String text = ((Component) line).toString();
            if (text.contains("ID:")) {
                try {
                    id = Integer.parseInt(text.replaceAll("[^0-9]", "").trim());
                } catch (NumberFormatException e) {
                    return;
                }
                break;
            }
        }

        if (id == -1) return;

        MarketManager.MarketListing listing = plugin.getMarketManager().getListing(id);
        if (listing == null) {
            player.sendMessage(Component.text("Ese anuncio ya no existe.").color(NamedTextColor.RED));
            player.closeInventory();
            playerPages.remove(player.getUniqueId());
            return;
        }

        if (plugin.getMarketManager().buyListing(id, player.getUniqueId())) {
            player.getInventory().addItem(listing.item);
            player.sendMessage(Component.text("Compraste " + listing.item.getType().name() + " por " + listing.price + " Hydras.").color(NamedTextColor.GREEN));
            open(player, plugin, playerPages.getOrDefault(player.getUniqueId(), 0));
        } else {
            player.sendMessage(Component.text("No tienes suficientes Hydras o el anuncio ya no esta disponible.").color(NamedTextColor.RED));
            open(player, plugin, playerPages.getOrDefault(player.getUniqueId(), 0));
        }
    }
}
