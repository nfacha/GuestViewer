package dev.facha.guestViewer.listeners;

import dev.facha.guestViewer.GuestViewer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {
    private final GuestViewer plugin;

    public PlayerListener(GuestViewer plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Hide the default join message
        event.setJoinMessage(null);
        
        // Schedule a delayed task to ensure the player is fully loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    plugin.setSpectatorMode(player);
                    plugin.broadcastPlayerJoin(player);
                }
            }
        }.runTaskLater(plugin, 5L); // 5 ticks = 0.25 seconds
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Hide the default quit message
        event.setQuitMessage(null);
        
        // Broadcast custom quit message
        plugin.broadcastPlayerQuit(player);
        
        // Clean up any spectator targets if player quits
        if (plugin.hasTargetPlayer(player)) {
            plugin.setSpectatorTarget(player, null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        
        // If player doesn't have bypass permission and is changing from spectator mode
        if (!plugin.canBypassRestrictions(player) && event.getNewGameMode() != GameMode.SPECTATOR) {
            event.setCancelled(true);
            player.sendMessage(plugin.getSpectatorJoinMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is in spectator mode and doesn't have permission to chat
        if (plugin.isRestrictSpectatorChat() && 
            player.getGameMode() == GameMode.SPECTATOR && 
            !plugin.canChat(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getChatRestrictedMessage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Only check spectators without bypass permission
        if (player.getGameMode() == GameMode.SPECTATOR && !plugin.canBypassRestrictions(player)) {
            checkPlayerDistance(player, event.getTo());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        // Only check spectators without bypass permission
        if (player.getGameMode() == GameMode.SPECTATOR && !plugin.canBypassRestrictions(player)) {
            // If teleport is outside allowed range, cancel it
            if (!isWithinAllowedDistance(player, event.getTo())) {
                event.setCancelled(true);
                player.sendMessage(plugin.getDistanceWarning());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSpectateEntity(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is in spectator mode and looking at an entity
        if (player.getGameMode() == GameMode.SPECTATOR && !plugin.canBypassRestrictions(player)) {
            // This is not an exact way to detect spectator changes, but it's a workaround
            // as there's no direct API for spectating entities in Bukkit/Spigot
            new BukkitRunnable() {
                @Override
                public void run() {
                    Entity spectatedEntity = player.getSpectatorTarget();
                    if (spectatedEntity instanceof Player) {
                        plugin.setSpectatorTarget(player, (Player) spectatedEntity);
                    } else if (spectatedEntity == null && plugin.hasTargetPlayer(player)) {
                        plugin.setSpectatorTarget(player, null);
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    // Helper method to check if a player is within allowed distance
    private boolean isWithinAllowedDistance(Player player, Location location) {
        int maxDistance = plugin.getMaxDistance();
        
        // If player is spectating another player
        if (plugin.hasTargetPlayer(player)) {
            Player target = plugin.getSpectatorTarget(player);
            if (target != null && target.isOnline() && player.getWorld().equals(target.getWorld())) {
                return location.distance(target.getLocation()) <= maxDistance;
            }
        }
        
        // If free roam is allowed and player has permission, check distance from world spawn
        if (plugin.canFreeRoam(player)) {
            return location.distance(player.getWorld().getSpawnLocation()) <= maxDistance;
        }
        
        // If we get here, player is not allowed to be here
        return false;
    }

    // Check player distance and teleport if needed
    private void checkPlayerDistance(Player player, Location newLocation) {
        if (!isWithinAllowedDistance(player, newLocation)) {
            // If player has a target, teleport near target
            if (plugin.hasTargetPlayer(player)) {
                Player target = plugin.getSpectatorTarget(player);
                if (target != null && target.isOnline()) {
                    player.teleport(target.getLocation());
                    player.sendMessage(plugin.getDistanceWarning());
                }
            } 
            // Otherwise teleport to world spawn if free roam is allowed
            else if (plugin.canFreeRoam(player)) {
                player.teleport(player.getWorld().getSpawnLocation());
                player.sendMessage(plugin.getDistanceWarning());
            }
        }
    }
} 