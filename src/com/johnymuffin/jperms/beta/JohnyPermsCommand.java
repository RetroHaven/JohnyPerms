package com.johnymuffin.jperms.beta;

import com.johnymuffin.jperms.beta.config.JPermsLanguage;
import com.johnymuffin.jperms.core.models.PermissionsGroup;
import com.johnymuffin.jperms.core.models.PermissionsUser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.UUID;

import static com.johnymuffin.jperms.beta.util.Util.getUUIDFromUsername;

public class JohnyPermsCommand implements CommandExecutor {
    private JohnyPerms plugin;
    private JPermsLanguage lang;

    public JohnyPermsCommand(JohnyPerms plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguage();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.command")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }
        if (strings.length > 0) {
            String subcommand = strings[0];
            if (subcommand.equalsIgnoreCase("user")) return userCommand(commandSender, command, s, strings);
            if (subcommand.equalsIgnoreCase("group")) return groupCommand(commandSender, command, s, strings);
            if (subcommand.equalsIgnoreCase("plugin")) return pluginCommand(commandSender, command, s, strings);
        }

        commandSender.sendMessage(lang.getMessage("jperms_main_general_use"));
        return true;
    }


    private boolean userCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.user")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }
        String rawPlayerName = null; //Player name
        UUID uuid = null;
        if (strings.length >= 2) {
            rawPlayerName = strings[1];
            uuid = getUUIDFromUsername(rawPlayerName); //We need to find a UUID for a user command
            //Check JPerms UUID cache which is cap insensitive
            if(uuid == null) {
                plugin.getJpuuidCache().getUUIDFromUsername(rawPlayerName);
            }
            if (uuid == null) {
                //Server couldn't find an associated player
                commandSender.sendMessage(lang.getMessage("player_not_found_full"));
                return true;
            }
        }
        //User information command
        if (strings.length == 2) {
            commandSender.sendMessage(ChatColor.BLUE + "Information on " + ChatColor.GOLD + uuid.toString());
            PermissionsUser permissionsUser = plugin.getUser(uuid);
            commandSender.sendMessage(ChatColor.BLUE + "Group: " + ChatColor.GOLD + permissionsUser.getGroup().getName());
            commandSender.sendMessage(ChatColor.BLUE + "Save Status: " + ChatColor.GOLD + permissionsUser.isSaving());
            return true;
        }
        //User Perm or User Group command
        if (strings.length >= 3) {
            String subCommand = strings[2];
            if (subCommand.equalsIgnoreCase("perm")) return userPermCommand(commandSender, command, s, strings, uuid);
            if (subCommand.equalsIgnoreCase("group")) return userGroupCommand(commandSender, command, s, strings, uuid);
        }

        commandSender.sendMessage(lang.getMessage("jperms_user_general_use"));
        return true;
    }

    private boolean userPermCommand(CommandSender commandSender, Command command, String s, String[] strings, UUID user) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.user.perm")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }
        if (strings.length >= 4) {
            String subCommand = strings[3];
            if (subCommand.equalsIgnoreCase("add")) return userPermAddCommand(commandSender, command, s, strings, user);
            if (subCommand.equalsIgnoreCase("list"))
                return userPermListCommand(commandSender, command, s, strings, user);
            if (subCommand.equalsIgnoreCase("remove"))
                return userPermRemoveCommand(commandSender, command, s, strings, user);
        }
        commandSender.sendMessage(lang.getMessage("jperms_user_perm_use"));
        return true;
    }

    private boolean userPermListCommand(CommandSender commandSender, Command command, String s, String[] strings, UUID user) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.user.perm.list")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }

        PermissionsUser permissionsUser = plugin.getUser(user);
        commandSender.sendMessage(ChatColor.GOLD + "User Specific Perms: ");
        for (Map.Entry<String, Boolean> permission : permissionsUser.getPermissions(false).entrySet()) {
            if (permission.getValue()) {
                commandSender.sendMessage(ChatColor.GOLD + permission.getKey());
            } else {
                commandSender.sendMessage(ChatColor.GOLD + "-" + permission.getKey());
            }
        }

        return true;
    }

    private boolean userPermAddCommand(CommandSender commandSender, Command command, String s, String[] strings, UUID user) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.user.perm.add")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }
        if (strings.length >= 5) {
            String permission = strings[4];
            PermissionsUser permissionsUser = plugin.getUser(user);
            permissionsUser.addPermission(permission);
            commandSender.sendMessage(lang.getMessage("generic_action_completed"));
            return true;
        }
        plugin.recalculateAllPlayers();
        commandSender.sendMessage(lang.getMessage("jperms_user_perm_add_use"));
        return true;
    }

    private boolean userPermRemoveCommand(CommandSender commandSender, Command command, String s, String[] strings, UUID user) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.user.perm.remove")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }
        if (strings.length >= 5) {
            String permission = strings[4];
            PermissionsUser permissionsUser = plugin.getUser(user);
            permissionsUser.removePermission(permission);
            commandSender.sendMessage(lang.getMessage("generic_action_completed"));
            return true;
        }
        plugin.recalculateAllPlayers();
        commandSender.sendMessage(lang.getMessage("jperms_user_perm_remove_use"));
        return false;
    }

    private boolean userGroupCommand(CommandSender commandSender, Command command, String s, String[] strings, UUID user) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.user.group")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }

        if (strings.length == 3 || strings.length == 4 || !strings[3].equalsIgnoreCase("set")) {
            commandSender.sendMessage(lang.getMessage("jperms_user_group_general_use"));
            commandSender.sendMessage(ChatColor.GRAY + "Group: " + plugin.getUser(user).getGroup().getName());
            return true;
        }

        PermissionsGroup permissionsGroup = plugin.getGroups().get(strings[4].toLowerCase());
        if (permissionsGroup == null) {
            commandSender.sendMessage(lang.getMessage("jperms_group_general_unknown"));
            return true;
        }

        plugin.getUser(user).setGroup(permissionsGroup);
        commandSender.sendMessage(lang.getMessage("generic_action_completed"));
        plugin.recalculateAllPlayers();
        return true;
    }

    private boolean groupCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.group")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }

        if (strings.length == 1) {
            commandSender.sendMessage(lang.getMessage("jperms_group_general_use"));
            return true;
        }

        PermissionsGroup permissionsGroup = null;
        if (strings.length > 1) {
            permissionsGroup = plugin.getGroups().get(strings[1]);
            if (permissionsGroup == null) {
                commandSender.sendMessage(lang.getMessage("jperms_group_general_unknown"));
                return true;
            }
        }

        if (strings.length == 2) {
            commandSender.sendMessage(ChatColor.BLUE + "Selected Group: " + permissionsGroup.getName());
            return true;
        }

        String subCommand = strings[2];
        if (subCommand.equalsIgnoreCase("inheritance"))
            return groupInheritanceCommand(commandSender, command, s, strings, permissionsGroup);


        commandSender.sendMessage(lang.getMessage("jperms_group_general_use"));
        return true;
    }

    private boolean groupInheritanceCommand(CommandSender commandSender, Command command, String s, String[] strings, PermissionsGroup group) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.group.inheritance")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }

        commandSender.sendMessage(ChatColor.GRAY + "Inheritance for: " + group.getName());
        for (int i = 0; i < group.getInheritanceGroups().length; i++) {
            commandSender.sendMessage(ChatColor.GRAY + String.valueOf(i + 1) + " " + ChatColor.BLUE + group.getInheritanceGroups()[i].getName());
        }

        return true;
    }

    private boolean pluginCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.plugin")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }
        if (strings.length > 1) {
            String subcommand = strings[1];
            if (subcommand.equalsIgnoreCase("reload")) return pluginReloadCommand(commandSender, command, s, strings);
            if (subcommand.equalsIgnoreCase("save")) return pluginSaveCommand(commandSender, command, s, strings);
        }
        commandSender.sendMessage(lang.getMessage("jperms_plugin_use"));
        return true;
    }

    private boolean pluginReloadCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.plugin.reload")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }
        long unixStart = System.currentTimeMillis();
        if (!plugin.reloadStorage()) {
            commandSender.sendMessage(ChatColor.RED + "A severe error occurred while reloading, and JPerms is shutting down immediately. Please check console for details.");
            return true;
        }
        long timeTaken = System.currentTimeMillis() - unixStart;
        commandSender.sendMessage(ChatColor.RED + "Reload completed in " + timeTaken + " milliseconds.");

        return true;
    }

    private boolean pluginSaveCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.plugin.save")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }
        long unixStart = System.currentTimeMillis();
        plugin.save(true, true, true);
        long timeTaken = System.currentTimeMillis() - unixStart;
        commandSender.sendMessage(ChatColor.RED + "Save completed in " + timeTaken + " milliseconds.");

        return true;
    }

    private boolean isAuthorized(CommandSender commandSender, String permission) {
        return (commandSender.isOp() || commandSender.hasPermission(permission));
    }

}
