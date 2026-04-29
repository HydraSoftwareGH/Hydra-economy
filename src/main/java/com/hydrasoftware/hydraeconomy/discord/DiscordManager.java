package com.hydrasoftware.hydraeconomy.discord;

import com.hydrasoftware.hydraeconomy.HydraEconomy;
import com.hydrasoftware.hydraeconomy.economy.DailyRewardManager;
import com.hydrasoftware.hydraeconomy.economy.MarketManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DiscordManager extends ListenerAdapter {

    private final HydraEconomy plugin;
    private final String token;
    private JDA jda;

    public DiscordManager(HydraEconomy plugin, String token) {
        this.plugin = plugin;
        this.token = token;
    }

    public void start() throws Exception {
        jda = JDABuilder.createLight(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("HydraEconomy | /hydras"))
                .addEventListeners(this)
                .build();

        jda.awaitReady();

        jda.updateCommands().addCommands(
                Commands.slash("hydras", "Ver saldo de Hydras de un jugador")
                        .addOption(OptionType.STRING, "jugador", "Nombre del jugador (opcional)", false),

                Commands.slash("mercado", "Ver items publicados en el mercado"),

                Commands.slash("daily", "Ver estado de la recompensa diaria de un jugador")
                        .addOption(OptionType.STRING, "jugador", "Nombre del jugador (opcional)", false),

                Commands.slash("top", "Ver top 10 de Hydras")
        ).queue();
    }

    public void stop() {
        if (jda != null) {
            jda.shutdown();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "hydras" -> handleBalance(event);
            case "mercado" -> handleMarket(event);
            case "daily" -> handleDaily(event);
            case "top" -> handleTop(event);
        }
    }

    private UUID findPlayer(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player != null) return player.getUniqueId();

        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        if (offline.hasPlayedBefore() || offline.isOnline()) {
            return offline.getUniqueId();
        }
        return null;
    }

    private void handleBalance(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String name = event.getOption("jugador") != null ? event.getOption("jugador").getAsString() : null;

        if (name == null) {
            event.getHook().sendMessage("Usa: `/hydras <jugador>` - Especifica un nombre de jugador.").queue();
            return;
        }

        UUID uuid = findPlayer(name);
        if (uuid == null) {
            event.getHook().sendMessage("Jugador \"" + name + "\" no encontrado.").queue();
            return;
        }

        BigDecimal balance = plugin.getEconomyManager().getBalance(uuid);
        event.getHook().sendMessage("**" + name + "** tiene **" + balance + " Hydras**.").queue();
    }

    private void handleMarket(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        List<MarketManager.MarketListing> listings = plugin.getMarketManager().getListings();
        if (listings.isEmpty()) {
            event.getHook().sendMessage("El mercado esta vacio.").queue();
            return;
        }

        StringBuilder msg = new StringBuilder("**== MERCADO ==**\n");
        for (int i = 0; i < Math.min(listings.size(), 20); i++) {
            MarketManager.MarketListing l = listings.get(i);
            msg.append("`ID ").append(l.id).append("` ")
                    .append(l.item.getType().name())
                    .append(" x").append(l.item.getAmount())
                    .append(" — **").append(l.price).append(" Hydras**\n");
        }
        if (listings.size() > 20) {
            msg.append("*... y ").append(listings.size() - 20).append(" mas*");
        }
        event.getHook().sendMessage(msg.toString()).queue();
    }

    private void handleDaily(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String name = event.getOption("jugador") != null ? event.getOption("jugador").getAsString() : null;

        if (name == null) {
            event.getHook().sendMessage("Usa: `/daily <jugador>` - Especifica un nombre de jugador.").queue();
            return;
        }

        UUID uuid = findPlayer(name);
        if (uuid == null) {
            event.getHook().sendMessage("Jugador \"" + name + "\" no encontrado.").queue();
            return;
        }

        DailyRewardManager.RewardData data = plugin.getDailyRewardManager().getRewardData(uuid);
        String lastClaim = data.lastClaim != null ? data.lastClaim : "Nunca";
        event.getHook().sendMessage("**" + name + "**\n"
                + "Ultimo reclamo: " + lastClaim + "\n"
                + "Racha: **" + data.streak + " dias**").queue();
    }

    private void handleTop(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        List<Player> players = Bukkit.getOnlinePlayers().stream()
                .sorted(Comparator.comparing(p -> plugin.getEconomyManager().getBalance(p.getUniqueId())).reversed())
                .limit(10)
                .collect(Collectors.toList());

        if (players.isEmpty()) {
            event.getHook().sendMessage("No hay jugadores online.").queue();
            return;
        }

        StringBuilder msg = new StringBuilder("**== TOP 10 HYDRAS ==**\n");
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            BigDecimal bal = plugin.getEconomyManager().getBalance(p.getUniqueId());
            msg.append("**").append(i + 1).append(".** ").append(p.getName())
                    .append(" — ").append(bal).append(" Hydras\n");
        }
        event.getHook().sendMessage(msg.toString()).queue();
    }
}
