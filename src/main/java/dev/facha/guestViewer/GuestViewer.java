package dev.facha.guestViewer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import dev.facha.guestViewer.commands.GuestViewerCommand;
import dev.facha.guestViewer.listeners.PlayerListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GuestViewer extends JavaPlugin {
    private FileConfiguration config;
    private Map<UUID, Player> spectatorTargets;
    private int maxDistance;
    
    // Messages
    private String spectatorJoinMessage;
    private String playerJoinMessage;
    private String distanceWarning;
    private String chatRestrictedMessage;
    
    // Chat settings
    private boolean restrictSpectatorChat;
    
    // Broadcast settings
    private boolean playerJoinBroadcastEnabled;
    private String playerJoinBroadcastMessage;
    private boolean playerQuitBroadcastEnabled;
    private String playerQuitBroadcastMessage;
    private boolean spectatorJoinBroadcastEnabled;
    private String spectatorJoinBroadcastMessage;
    private boolean spectatorQuitBroadcastEnabled;
    private String spectatorQuitBroadcastMessage;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Load config values
        loadConfig();
        
        // Initialize the spectator targets map
        spectatorTargets = new HashMap<>();
        
        // Register command
        GuestViewerCommand commandExecutor = new GuestViewerCommand(this);
        getCommand("guestviewer").setExecutor(commandExecutor);
        getCommand("guestviewer").setTabCompleter(commandExecutor);
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("GuestViewer has been enabled!");
    }

    @Override
    public void onDisable() {
        // Clear data
        spectatorTargets.clear();
        
        getLogger().info("GuestViewer has been disabled!");
    }
    
    public void loadConfig() {
        reloadConfig();
        config = getConfig();
        
        maxDistance = config.getInt("max-distance", 100);
        
        // Load messages
        spectatorJoinMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("messages.spectator-join", "&cYou are not whitelisted! You have been set to spectator mode."));
        playerJoinMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("messages.player-join", "&aWelcome back! You are playing in survival mode."));
        distanceWarning = ChatColor.translateAlternateColorCodes('&', 
                config.getString("messages.distance-warning", "&cYou cannot move more than 100 blocks from the player you are spectating!"));
        chatRestrictedMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("messages.chat-restricted", "&cYou don't have permission to chat. You are in spectator mode."));
        
        // Load chat settings
        restrictSpectatorChat = config.getBoolean("chat.restrict-spectator-chat", true);
        
        // Load broadcast settings
        playerJoinBroadcastEnabled = config.getBoolean("broadcast.player-join-enabled", true);
        playerJoinBroadcastMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("broadcast.player-join-message", "&e%player% &ahas joined the server."));
        playerQuitBroadcastEnabled = config.getBoolean("broadcast.player-quit-enabled", true);
        playerQuitBroadcastMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("broadcast.player-quit-message", "&e%player% &chas left the server."));
        
        spectatorJoinBroadcastEnabled = config.getBoolean("broadcast.spectator-join-enabled", true);
        spectatorJoinBroadcastMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("broadcast.spectator-join-message", "&7Guest &e%player% &7has joined as a spectator."));
        spectatorQuitBroadcastEnabled = config.getBoolean("broadcast.spectator-quit-enabled", true);
        spectatorQuitBroadcastMessage = ChatColor.translateAlternateColorCodes('&', 
                config.getString("broadcast.spectator-quit-message", "&7Guest &e%player% &7has left the server."));
    }
    
    public void setSpectatorTarget(Player spectator, Player target) {
        if (target == null) {
            spectatorTargets.remove(spectator.getUniqueId());
        } else {
            spectatorTargets.put(spectator.getUniqueId(), target);
        }
    }
    
    public Player getSpectatorTarget(Player spectator) {
        return spectatorTargets.get(spectator.getUniqueId());
    }
    
    public boolean hasTargetPlayer(Player spectator) {
        return spectatorTargets.containsKey(spectator.getUniqueId());
    }
    
    public int getMaxDistance() {
        return maxDistance;
    }
    
    public String getSpectatorJoinMessage() {
        return spectatorJoinMessage;
    }
    
    public String getPlayerJoinMessage() {
        return playerJoinMessage;
    }
    
    public String getDistanceWarning() {
        return distanceWarning;
    }
    
    public String getChatRestrictedMessage() {
        return chatRestrictedMessage;
    }
    
    public boolean isRestrictSpectatorChat() {
        return restrictSpectatorChat;
    }
    
    public boolean isPlayerJoinBroadcastEnabled() {
        return playerJoinBroadcastEnabled;
    }
    
    public String getPlayerJoinBroadcastMessage(String playerName) {
        return playerJoinBroadcastMessage.replace("%player%", playerName);
    }
    
    public boolean isPlayerQuitBroadcastEnabled() {
        return playerQuitBroadcastEnabled;
    }
    
    public String getPlayerQuitBroadcastMessage(String playerName) {
        return playerQuitBroadcastMessage.replace("%player%", playerName);
    }
    
    public boolean isSpectatorJoinBroadcastEnabled() {
        return spectatorJoinBroadcastEnabled;
    }
    
    public String getSpectatorJoinBroadcastMessage(String playerName) {
        return spectatorJoinBroadcastMessage.replace("%player%", playerName);
    }
    
    public boolean isSpectatorQuitBroadcastEnabled() {
        return spectatorQuitBroadcastEnabled;
    }
    
    public String getSpectatorQuitBroadcastMessage(String playerName) {
        return spectatorQuitBroadcastMessage.replace("%player%", playerName);
    }
    
    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }
    
    public boolean canBypassRestrictions(Player player) {
        return hasPermission(player, "guestviewer.bypass");
    }
    
    public boolean canFreeRoam(Player player) {
        return hasPermission(player, "guestviewer.freeroam") || hasPermission(player, "guestviewer.bypass");
    }
    
    public boolean canChat(Player player) {
        return hasPermission(player, "guestviewer.chat") || hasPermission(player, "guestviewer.bypass");
    }
    
    public void setSpectatorMode(Player player) {
        if (canBypassRestrictions(player)) {
            // Set whitelisted players to survival mode
            if (player.getGameMode() != GameMode.SURVIVAL) {
                player.setGameMode(GameMode.SURVIVAL);
                if (!playerJoinMessage.isEmpty()) {
                    player.sendMessage(playerJoinMessage);
                }
            }
        } else if (player.getGameMode() != GameMode.SPECTATOR) {
            // Set non-whitelisted players to spectator mode
            player.setGameMode(GameMode.SPECTATOR);
            if (!spectatorJoinMessage.isEmpty()) {
                player.sendMessage(spectatorJoinMessage);
            }
        }
    }
    
    public void broadcastPlayerJoin(Player player) {
        if (canBypassRestrictions(player) && isPlayerJoinBroadcastEnabled()) {
            Bukkit.broadcastMessage(getPlayerJoinBroadcastMessage(player.getName()));
        } else if (!canBypassRestrictions(player) && isSpectatorJoinBroadcastEnabled()) {
            Bukkit.broadcastMessage(getSpectatorJoinBroadcastMessage(player.getName()));
        }
    }
    
    public void broadcastPlayerQuit(Player player) {
        if (canBypassRestrictions(player) && isPlayerQuitBroadcastEnabled()) {
            Bukkit.broadcastMessage(getPlayerQuitBroadcastMessage(player.getName()));
        } else if (!canBypassRestrictions(player) && isSpectatorQuitBroadcastEnabled()) {
            Bukkit.broadcastMessage(getSpectatorQuitBroadcastMessage(player.getName()));
        }
    }
}
