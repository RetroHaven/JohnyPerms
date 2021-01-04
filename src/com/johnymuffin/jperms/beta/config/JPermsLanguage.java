package com.johnymuffin.jperms.beta.config;

import org.bukkit.util.config.Configuration;

import java.io.File;
import java.util.HashMap;

public class JPermsLanguage extends Configuration {
    private HashMap<String, String> map;

    public JPermsLanguage(File file) {
        super(file);
        map = new HashMap<String, String>();
        loadDefaults();
        loadFile();
    }

    private void loadDefaults() {
        //General Stuff
        map.put("no_permission", "&4Sorry, you don't have permission for this command.");
        map.put("unavailable_to_console", "&4Sorry, console can't run this command.");
        map.put("player_not_found_full", "&4Can't find a player called &9%username%");
        map.put("generic_error", "&4Sorry, an error occurred running that command, please contact staff!");
        map.put("generic_error_player", "&4Sorry, an error occurred:&f %var1%");
        map.put("generic_no_save_data", "&4Sorry, JPerms has no information on that player.");
        map.put("generic_invalid_world", "&cSorry, a world with that name couldn't be located");
        map.put("generic_action_completed", "&bYour action has been completed");
        //JPerms command
        map.put("jperms_main_general_use", "&cSorry, that is invalid. Try /jperms (user/group/plugin)");
        map.put("jperms_user_general_use", "&cSorry, that is invalid. Try /jperms user (username/uuid) (group/perm)");
        map.put("jperms_user_perm_use", "&cSorry, that is invalid. Try /jperms user (username/uuid) perm (add/list/remove)");
        map.put("jperms_user_perm_add_use", "&cSorry, that is invalid. Try /jperms user (username/uuid) perm add (perm)");
        map.put("jperms_user_group_general_use", "&cSorry, that is invalid. Try /jperms user (username/uuid) group (set)");
        map.put("jperms_group_general_use", "&cSorry, that is invalid. Try /jperms group (group) (list/perm/inheritance)");
        map.put("jperms_group_general_unknown", "&cSorry, that group is unknown");
        map.put("jperms_user_perm_remove_use", "&cSorry, that is invalid. Try /jperms user (username/uuid) perm remove (perm)");

    }

    private void loadFile() {
        this.load();
        for (String key : map.keySet()) {
            if (this.getString(key) == null) {
                this.setProperty(key, map.get(key));
            } else {
                map.put(key, this.getString(key));
            }
        }
        this.save();
    }

    public String getMessage(String msg) {
        String loc = map.get(msg);
        if (loc != null) {
            return loc.replace("&", "\u00a7");
        }
        return msg;
    }


}
