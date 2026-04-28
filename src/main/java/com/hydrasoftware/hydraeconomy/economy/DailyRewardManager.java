package com.hydrasoftware.hydraeconomy.economy;

import com.hydrasoftware.hydraeconomy.HydraEconomy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DailyRewardManager {

    private static final BigDecimal BASE_REWARD = BigDecimal.TEN;
    private static final int DAYS_FOR_BONUS = 5;
    private static final BigDecimal BONUS_AMOUNT = BigDecimal.valueOf(5);

    private final HydraEconomy plugin;
    private final Map<UUID, RewardData> playerData;
    private File dataFile;
    private FileConfiguration data;

    public DailyRewardManager(HydraEconomy plugin) {
        this.plugin = plugin;
        this.playerData = new HashMap<>();
        load();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "dailyrewards.yml");
        if (!dataFile.exists()) {
            plugin.saveResource("dailyrewards.yml", false);
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : data.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String lastClaim = data.getString(key + ".lastClaim");
            int streak = data.getInt(key + ".streak");
            playerData.put(uuid, new RewardData(lastClaim, streak));
        }
    }

    public void save() {
        for (Map.Entry<UUID, RewardData> entry : playerData.entrySet()) {
            data.set(entry.getKey().toString() + ".lastClaim", entry.getValue().lastClaim);
            data.set(entry.getKey().toString() + ".streak", entry.getValue().streak);
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar dailyrewards.yml: " + e.getMessage());
        }
    }

    public ClaimResult claim(UUID uuid) {
        RewardData rd = playerData.getOrDefault(uuid, new RewardData(null, 0));
        String today = LocalDate.now().toString();

        if (today.equals(rd.lastClaim)) {
            return new ClaimResult(false, BigDecimal.ZERO, "Ya reclamaste tu recompensa hoy!");
        }

        boolean consecutive = rd.lastClaim != null;
        if (consecutive) {
            LocalDate lastDate = LocalDate.parse(rd.lastClaim);
            LocalDate yesterday = LocalDate.now().minusDays(1);
            if (!lastDate.equals(yesterday)) {
                rd.streak = 0;
            }
        }

        rd.streak++;
        rd.lastClaim = today;

        BigDecimal reward = BASE_REWARD;
        if (rd.streak % DAYS_FOR_BONUS == 0) {
            reward = reward.add(BONUS_AMOUNT);
        }

        playerData.put(uuid, rd);
        plugin.getEconomyManager().deposit(uuid, reward);
        save();

        String message = "Reclamaste " + reward + " Hydras! (Dia " + rd.streak + ")";
        if (rd.streak % DAYS_FOR_BONUS == 0) {
            message += " BONUS +" + BONUS_AMOUNT + " por " + DAYS_FOR_BONUS + " dias seguidos!";
        }
        return new ClaimResult(true, reward, message);
    }

    public RewardData getRewardData(UUID uuid) {
        return playerData.getOrDefault(uuid, new RewardData(null, 0));
    }

    public static class RewardData {
        public final String lastClaim;
        public int streak;

        public RewardData(String lastClaim, int streak) {
            this.lastClaim = lastClaim;
            this.streak = streak;
        }
    }

    public static class ClaimResult {
        public final boolean success;
        public final BigDecimal amount;
        public final String message;

        public ClaimResult(boolean success, BigDecimal amount, String message) {
            this.success = success;
            this.amount = amount;
            this.message = message;
        }
    }
}
