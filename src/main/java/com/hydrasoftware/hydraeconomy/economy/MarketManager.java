package com.hydrasoftware.hydraeconomy.economy;

import com.hydrasoftware.hydraeconomy.HydraEconomy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class MarketManager {

    private final HydraEconomy plugin;
    private final Map<Integer, MarketListing> listings;
    private int nextId;
    private File dataFile;
    private FileConfiguration data;

    public MarketManager(HydraEconomy plugin) {
        this.plugin = plugin;
        this.listings = new HashMap<>();
        this.nextId = 1;
        load();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "market.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear market.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        nextId = data.getInt("nextId", 1);
        ConfigurationSection listingsSection = data.getConfigurationSection("listings");
        if (listingsSection != null) {
            for (String key : listingsSection.getKeys(false)) {
            int id = Integer.parseInt(key);
            UUID seller = UUID.fromString(data.getString("listings." + key + ".seller"));
            BigDecimal price = BigDecimal.valueOf(data.getDouble("listings." + key + ".price"));
            ItemStack item = data.getItemStack("listings." + key + ".item");
            listings.put(id, new MarketListing(id, seller, price, item));
        }
    }

    public void save() {
        data.set("listings", null);
        for (Map.Entry<Integer, MarketListing> entry : listings.entrySet()) {
            String path = "listings." + entry.getKey();
            data.set(path + ".seller", entry.getValue().seller.toString());
            data.set(path + ".price", entry.getValue().price.doubleValue());
            data.set(path + ".item", entry.getValue().item);
        }
        data.set("nextId", nextId);
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar market.yml: " + e.getMessage());
        }
    }

    public int createListing(UUID seller, BigDecimal price, ItemStack item) {
        int id = nextId++;
        listings.put(id, new MarketListing(id, seller, price, item));
        save();
        return id;
    }

    public boolean buyListing(int id, UUID buyer) {
        MarketListing listing = listings.get(id);
        if (listing == null) return false;

        if (listing.seller.equals(buyer)) {
            return false;
        }

        BigDecimal price = listing.price;
        if (!plugin.getEconomyManager().hasBalance(buyer, price)) {
            return false;
        }

        plugin.getEconomyManager().withdraw(buyer, price);
        plugin.getEconomyManager().deposit(listing.seller, price);
        listings.remove(id);
        save();
        return true;
    }

    public boolean cancelListing(int id, UUID seller) {
        MarketListing listing = listings.get(id);
        if (listing == null || !listing.seller.equals(seller)) {
            return false;
        }
        listings.remove(id);
        save();
        return true;
    }

    public List<MarketListing> getListings() {
        return listings.values().stream().sorted(Comparator.comparingInt(l -> l.id)).collect(Collectors.toList());
    }

    public MarketListing getListing(int id) {
        return listings.get(id);
    }

    public class MarketListing {
        public final int id;
        public final UUID seller;
        public final BigDecimal price;
        public final ItemStack item;

        public MarketListing(int id, UUID seller, BigDecimal price, ItemStack item) {
            this.id = id;
            this.seller = seller;
            this.price = price;
            this.item = item;
        }
    }
}
