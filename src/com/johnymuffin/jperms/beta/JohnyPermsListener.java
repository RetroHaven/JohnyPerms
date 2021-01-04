package com.johnymuffin.jperms.beta;

import com.johnymuffin.jperms.beta.override.JPInject;
import com.johnymuffin.jperms.beta.override.JPPermissibleBase;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JohnyPermsListener implements Listener {
    private JohnyPerms plugin;

    public JohnyPermsListener(JohnyPerms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = Event.Priority.Lowest)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        plugin.recalculatePlayer(event.getPlayer());


        //Override PermissibleBase
        JPPermissibleBase jpPermissibleBase = new JPPermissibleBase(event.getPlayer(), event.getPlayer());
        JPInject.inject(event.getPlayer(), jpPermissibleBase);
        plugin.getPlayerInjections().put(event.getPlayer().getUniqueId(), jpPermissibleBase);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerLeave(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        playerLeave(event.getPlayer());
    }

    public void playerLeave(Player player) {
        plugin.getPlayerInjections().remove(player.getUniqueId());
        plugin.removePlayer(player);
    }


}
