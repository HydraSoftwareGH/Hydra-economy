package com.hydrasoftware.hydraeconomy;

import com.hydrasoftware.hydraeconomy.commands.AdminEconomyCommand;
import com.hydrasoftware.hydraeconomy.commands.EconomyCommand;
import com.hydrasoftware.hydraeconomy.commands.MarketCommand;
import com.hydrasoftware.hydraeconomy.discord.DiscordManager;
import com.hydrasoftware.hydraeconomy.economy.DailyRewardManager;
import com.hydrasoftware.hydraeconomy.economy.EconomyManager;
import com.hydrasoftware.hydraeconomy.economy.MarketManager;
import com.hydrasoftware.hydraeconomy.gui.MarketGUI;
import org.bukkit.plugin.java.JavaPlugin;

public class HydraEconomy extends JavaPlugin {

    private EconomyManager economyManager;
    private DailyRewardManager dailyRewardManager;
    private MarketManager marketManager;
    private DiscordManager discordManager;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        saveDefaultConfig();

        economyManager = new EconomyManager(this);
        dailyRewardManager = new DailyRewardManager(this);
        marketManager = new MarketManager(this);

        getCommand("hydras").setExecutor(new EconomyCommand(this));
        getCommand("pay").setExecutor(new EconomyCommand(this));
        getCommand("daily").setExecutor(new EconomyCommand(this));
        getCommand("hydrasadmin").setExecutor(new AdminEconomyCommand(this));
        getCommand("mercado").setExecutor(new MarketCommand(this));

        getServer().getPluginManager().registerEvents(new MarketGUI(), this);

        String token = getConfig().getString("discord-token", "");
        if (!token.isEmpty()) {
            try {
                discordManager = new DiscordManager(this, token);
                discordManager.start();
                getLogger().info("Discord bot iniciado!");
            } catch (Exception e) {
                getLogger().severe("No se pudo iniciar el bot de Discord: " + e.getMessage());
            }
        } else {
            getLogger().info("Discord bot deshabilitado (sin token en config.yml)");
        }

        getLogger().info("HydraEconomy ha sido activado!");
    }

    @Override
    public void onDisable() {
        if (discordManager != null) discordManager.stop();
        if (economyManager != null) economyManager.save();
        if (dailyRewardManager != null) dailyRewardManager.save();
        if (marketManager != null) marketManager.save();
        getLogger().info("HydraEconomy ha sido desactivado!");
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public DailyRewardManager getDailyRewardManager() {
        return dailyRewardManager;
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }
}
