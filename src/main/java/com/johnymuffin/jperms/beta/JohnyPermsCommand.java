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
import com.johnymuffin.jperms.beta.util.ColorUtil;

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
                uuid = plugin.getJpuuidCache().getUUIDFromUsername(rawPlayerName);
            }
            if (uuid == null) {
                //Server couldn't find an associated player
                String message = lang.getMessage("player_not_found_full");
                message = message.replace("%username%", rawPlayerName);
                commandSender.sendMessage(message);
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
            if (subCommand.equalsIgnoreCase("title")) return userTitleCommand(commandSender, command, s, strings, uuid);
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

    private boolean userTitleCommand(CommandSender commandSender, Command command, String s, String[] strings, UUID user) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.user.title")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }

        com.johnymuffin.jperms.beta.objects.User userObj = (com.johnymuffin.jperms.beta.objects.User) plugin.getUser(user);

        if (strings.length == 3) {
            String currentTitle = userObj.getTitle();
            commandSender.sendMessage(ChatColor.GRAY + "Current title for " + userObj.getLastKnownUsername() + ": " +
                (currentTitle != null && !currentTitle.isEmpty() ? ColorUtil.translateAlternateColorCodes('&', currentTitle) : "None"));
            return true;
        }

        if (strings.length >= 4) {
            String action = strings[3];
            if (action.equalsIgnoreCase("set") && strings.length >= 5) {
                String title = strings[4];
                for (int i = 5; i < strings.length; i++) {
                    title += " " + strings[i];
                }
                userObj.setTitle(title);
                commandSender.sendMessage(ChatColor.GREEN + "Title set to: " + ColorUtil.translateAlternateColorCodes('&', title));
                plugin.recalculateAllPlayers();
                return true;
            } else if (action.equalsIgnoreCase("remove")) {
                userObj.setTitle("");
                commandSender.sendMessage(ChatColor.GREEN + "Title removed for " + userObj.getLastKnownUsername());
                plugin.recalculateAllPlayers();
                return true;
            }
        }

        commandSender.sendMessage(ChatColor.GOLD + "Usage: /jperms user <user> title [set <title>|remove]");
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

        // Check for create/delete commands that don't need an existing group
        if (strings.length >= 3) {
            String subCommand = strings[2];
            if (subCommand.equalsIgnoreCase("create")) {
                return groupCreateCommand(commandSender, command, s, strings);
            }
            if (subCommand.equalsIgnoreCase("delete")) {
                return groupDeleteCommand(commandSender, command, s, strings);
            }
        }

        PermissionsGroup permissionsGroup = null;
        if (strings.length > 1) {
            permissionsGroup = plugin.getGroups().get(strings[1].toLowerCase());
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
        if (subCommand.equalsIgnoreCase("prefix"))
            return groupPrefixCommand(commandSender, command, s, strings, permissionsGroup);
        if (subCommand.equalsIgnoreCase("suffix"))
            return groupSuffixCommand(commandSender, command, s, strings, permissionsGroup);
        if (subCommand.equalsIgnoreCase("info"))
            return groupInfoCommand(commandSender, command, s, strings, permissionsGroup);
        if (subCommand.equalsIgnoreCase("perm"))
            return groupPermCommand(commandSender, command, s, strings, permissionsGroup);

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

    private boolean groupPrefixCommand(CommandSender commandSender, Command command, String s, String[] strings, PermissionsGroup group) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.group.prefix")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }

        if (strings.length == 3) {
            String currentPrefix = group.getPrefix();
            commandSender.sendMessage(ChatColor.GRAY + "Current prefix for " + group.getName() + ": " +
                (currentPrefix != null ? ColorUtil.translateAlternateColorCodes('&', currentPrefix) : "None"));
            return true;
        }

        if (strings.length >= 4) {
            String action = strings[3];
            if (action.equalsIgnoreCase("set") && strings.length >= 5) {
                String prefix = strings[4];
                for (int i = 5; i < strings.length; i++) {
                    prefix += " " + strings[i];
                }
                group.setPrefix(prefix);
                group.setSaveStatus(true);
                commandSender.sendMessage(ChatColor.GREEN + "Prefix set to: " + ColorUtil.translateAlternateColorCodes('&', prefix));
                plugin.recalculateAllPlayers();
                return true;
            } else if (action.equalsIgnoreCase("remove")) {
                group.setPrefix("");
                group.setSaveStatus(true);
                commandSender.sendMessage(ChatColor.GREEN + "Prefix removed for group " + group.getName());
                plugin.recalculateAllPlayers();
                return true;
            }
        }

        commandSender.sendMessage(ChatColor.GOLD + "Usage: /jperms group <group> prefix [set <prefix>|remove]");
        return true;
    }

    private boolean groupSuffixCommand(CommandSender commandSender, Command command, String s, String[] strings, PermissionsGroup group) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.group.suffix")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }

        if (strings.length == 3) {
            String currentSuffix = group.getSuffix();
            commandSender.sendMessage(ChatColor.GRAY + "Current suffix for " + group.getName() + ": " +
                (currentSuffix != null ? ColorUtil.translateAlternateColorCodes('&', currentSuffix) : "None"));
            return true;
        }

        if (strings.length >= 4) {
            String action = strings[3];
            if (action.equalsIgnoreCase("set") && strings.length >= 5) {
                String suffix = strings[4];
                for (int i = 5; i < strings.length; i++) {
                    suffix += " " + strings[i];
                }
                group.setSuffix(suffix);
                group.setSaveStatus(true);
                commandSender.sendMessage(ChatColor.GREEN + "Suffix set to: " + ColorUtil.translateAlternateColorCodes('&', suffix));
                plugin.recalculateAllPlayers();
                return true;
            } else if (action.equalsIgnoreCase("remove")) {
                group.setSuffix("");
                group.setSaveStatus(true);
                commandSender.sendMessage(ChatColor.GREEN + "Suffix removed for group " + group.getName());
                plugin.recalculateAllPlayers();
                return true;
            }
        }

        commandSender.sendMessage(ChatColor.GOLD + "Usage: /jperms group <group> suffix [set <suffix>|remove]");
        return true;
    }

    private boolean groupInfoCommand(CommandSender commandSender, Command command, String s, String[] strings, PermissionsGroup group) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.group")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }

        commandSender.sendMessage(ChatColor.GOLD + "=== Group Info: " + group.getName() + " ===");
        commandSender.sendMessage(ChatColor.GRAY + "Prefix: " +
            (group.getPrefix() != null && !group.getPrefix().isEmpty() ?
                ColorUtil.translateAlternateColorCodes('&', group.getPrefix()) : "None"));
        commandSender.sendMessage(ChatColor.GRAY + "Suffix: " +
            (group.getSuffix() != null && !group.getSuffix().isEmpty() ?
                ColorUtil.translateAlternateColorCodes('&', group.getSuffix()) : "None"));
        commandSender.sendMessage(ChatColor.GRAY + "Default Group: " + (group.isDefaultGroup() ? "Yes" : "No"));

        return true;
    }

    private boolean groupPermCommand(CommandSender commandSender, Command command, String s, String[] strings, PermissionsGroup group) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.group.perm")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }

        if (strings.length >= 4) {
            String action = strings[3];
            if (action.equalsIgnoreCase("add") && strings.length >= 5) {
                String permission = strings[4];
                group.addPermission(permission);
                group.setSaveStatus(true);
                commandSender.sendMessage(ChatColor.GREEN + "Permission " + permission + " added to group " + group.getName());
                plugin.recalculateAllPlayers();
                return true;
            } else if (action.equalsIgnoreCase("remove") && strings.length >= 5) {
                String permission = strings[4];
                group.removePermission(permission);
                group.setSaveStatus(true);
                commandSender.sendMessage(ChatColor.GREEN + "Permission " + permission + " removed from group " + group.getName());
                plugin.recalculateAllPlayers();
                return true;
            } else if (action.equalsIgnoreCase("list")) {
                commandSender.sendMessage(ChatColor.GOLD + "Permissions for group " + group.getName() + ":");
                for (Map.Entry<String, Boolean> permission : group.getPermissions(false).entrySet()) {
                    if (permission.getValue()) {
                        commandSender.sendMessage(ChatColor.GOLD + permission.getKey());
                    } else {
                        commandSender.sendMessage(ChatColor.GOLD + "-" + permission.getKey());
                    }
                }
                return true;
            }
        }

        commandSender.sendMessage(ChatColor.GOLD + "Usage: /jperms group <group> perm [add <permission>|remove <permission>|list]");
        return true;
    }

    private boolean groupCreateCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.group.create")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }

        String groupName = strings[1].toLowerCase();

        // Check if group already exists
        if (plugin.getGroups().containsKey(groupName)) {
            commandSender.sendMessage(ChatColor.RED + "Group " + groupName + " already exists!");
            return true;
        }

        // Create new group with empty JSONObject
        com.johnymuffin.jperms.beta.objects.Group newGroup = new com.johnymuffin.jperms.beta.objects.Group(plugin, groupName, new org.json.simple.JSONObject());
        newGroup.setSaveStatus(true);
        plugin.getGroups().put(groupName, newGroup);
        plugin.save(false, true, false);

        commandSender.sendMessage(ChatColor.GREEN + "Group " + groupName + " created successfully!");
        return true;
    }

    private boolean groupDeleteCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!isAuthorized(commandSender, "johnyperms.jperms.group.delete")) {
            commandSender.sendMessage(lang.getMessage("no_permission"));
            return true;
        }

        String groupName = strings[1].toLowerCase();

        // Check if group exists
        if (!plugin.getGroups().containsKey(groupName)) {
            commandSender.sendMessage(ChatColor.RED + "Group " + groupName + " does not exist!");
            return true;
        }

        PermissionsGroup group = plugin.getGroups().get(groupName);

        // Prevent deleting the default group
        if (group.isDefaultGroup()) {
            commandSender.sendMessage(ChatColor.RED + "Cannot delete the default group! Set another group as default first.");
            return true;
        }

        // Check if any users are in this group
        PermissionsUser[] usersInGroup = group.getUsers();
        if (usersInGroup.length > 0) {
            commandSender.sendMessage(ChatColor.RED + "Cannot delete group " + groupName + " because " + usersInGroup.length + " user(s) are still in it!");
            commandSender.sendMessage(ChatColor.RED + "Move all users to another group first.");
            return true;
        }

        // Remove the group
        plugin.getGroups().remove(groupName);
        plugin.getPermissionsConfig().deleteGroup(groupName);

        commandSender.sendMessage(ChatColor.GREEN + "Group " + groupName + " deleted successfully!");
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
