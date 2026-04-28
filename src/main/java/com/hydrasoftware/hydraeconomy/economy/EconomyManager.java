package com.hydrasoftware.hydraeconomy.economy;

import com.hydrasoftware.hydraeconomy.HydraEconomy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final HydraEconomy plugin;
    private final Map<UUID, BigDecimal> balances;
    private File dataFile;
    private FileConfiguration data;

    public EconomyManager(HydraEconomy plugin) {
        this.plugin = plugin;
        this.balances = new HashMap<>();
        load();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "balances.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear balances.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = data.getConfigurationSection("balances");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                BigDecimal balance = BigDecimal.valueOf(section.getDouble(key));
                balances.put(uuid, balance);
            }
        }
    }

    public void save() {
        data.set("balances", null);
        for (Map.Entry<UUID, BigDecimal> entry : balances.entrySet()) {
            data.set("balances." + entry.getKey().toString(), entry.getValue().doubleValue());
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar balances.yml: " + e.getMessage());
        }
    }

    public BigDecimal getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public void setBalance(UUID uuid, BigDecimal amount) {
        balances.put(uuid, amount.setScale(2, RoundingMode.HALF_UP));
    }

    public boolean hasBalance(UUID uuid, BigDecimal amount) {
        return getBalance(uuid).compareTo(amount) >= 0;
    }

    public boolean withdraw(UUID uuid, BigDecimal amount) {
        BigDecimal current = getBalance(uuid);
        if (current.compareTo(amount) < 0) {
            return false;
        }
        balances.put(uuid, current.subtract(amount).setScale(2, RoundingMode.HALF_UP));
        return true;
    }

    public void deposit(UUID uuid, BigDecimal amount) {
        BigDecimal current = getBalance(uuid);
        balances.put(uuid, current.add(amount).setScale(2, RoundingMode.HALF_UP));
    }

    public boolean transfer(UUID from, UUID to, BigDecimal amount) {
        if (!withdraw(from, amount)) {
            return false;
        }
        deposit(to, amount);
        return true;
    }
}
