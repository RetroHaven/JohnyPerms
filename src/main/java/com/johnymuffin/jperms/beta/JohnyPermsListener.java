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
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.ChatColor;
import com.johnymuffin.jperms.core.models.PermissionsUser;
import com.johnymuffin.jperms.core.models.PermissionsGroup;
import com.johnymuffin.jperms.beta.util.ColorUtil;

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

        //Add to cache
        plugin.getJpuuidCache().addUser(event.getPlayer().getName(), event.getPlayer().getUniqueId());
        //Save username to user
        plugin.getUser(event.getPlayer().getUniqueId()).setLastKnownUsername(event.getPlayer().getName());

        //Override PermissibleBase
        if (plugin.getConfig().getConfigBoolean("super-perms-override.enable")) {
            JPPermissibleBase jpPermissibleBase = new JPPermissibleBase(event.getPlayer(), event.getPlayer());
            JPInject.inject(event.getPlayer(), jpPermissibleBase);
            plugin.getPlayerInjections().put(event.getPlayer().getUniqueId(), jpPermissibleBase);
        }
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

    @EventHandler(priority = Event.Priority.High)
    public void onPlayerChat(PlayerChatEvent event) {
        if (!plugin.getConfig().getConfigBoolean("chat.enable")) {
            return;
        }

        Player player = event.getPlayer();
        PermissionsUser user = plugin.getUser(player.getUniqueId());
        PermissionsGroup group = user.getGroup();

        String format = plugin.getConfig().getConfigOption("chat.format", "<%title %prefix %username%suffix> &f%message").toString();

        // Get prefix and suffix from group (fallback to user if implemented)
        String prefix = "";
        String suffix = "";
        String title = "";

        if (group != null) {
            if (group.getPrefix() != null && !group.getPrefix().isEmpty()) {
                prefix = group.getPrefix();
            }
            if (group.getSuffix() != null && !group.getSuffix().isEmpty()) {
                suffix = group.getSuffix();
            }
        }

        // Get title from user
        if (user instanceof com.johnymuffin.jperms.beta.objects.User) {
            com.johnymuffin.jperms.beta.objects.User userObj = (com.johnymuffin.jperms.beta.objects.User) user;
            if (userObj.getTitle() != null && !userObj.getTitle().isEmpty()) {
                title = userObj.getTitle();
            }
        }

        // Apply color codes
        prefix = ColorUtil.translateAlternateColorCodes('&', prefix);
        suffix = ColorUtil.translateAlternateColorCodes('&', suffix);
        title = ColorUtil.translateAlternateColorCodes('&', title);

        // Handle spacing: add space after elements only if they exist
        String titleWithSpace = title.isEmpty() ? "" : title + " ";
        String prefixWithSpace = prefix.isEmpty() ? "" : prefix + " ";

        // Replace placeholders
        format = format.replace("%title ", titleWithSpace); // Handle "%title " (with space)
        format = format.replace("%title", title); // Handle "%title" (without space) as fallback
        format = format.replace("%prefix ", prefixWithSpace); // Handle "%prefix " (with space)
        format = format.replace("%prefix", prefix); // Handle "%prefix" (without space) as fallback
        format = format.replace("%suffix", suffix);
        format = format.replace("%username", player.getName());
        format = format.replace("%displayname", player.getDisplayName());
        format = format.replace("%group", group != null ? group.getName() : "default");
        format = format.replace("%message", event.getMessage());

        // Apply color codes to final format
        format = ColorUtil.translateAlternateColorCodes('&', format);

        // Set the new format
        event.setFormat(format);
    }

}
