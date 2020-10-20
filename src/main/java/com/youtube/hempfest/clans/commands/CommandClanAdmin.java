package com.youtube.hempfest.clans.commands;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandClanAdmin extends BukkitCommand {


    public CommandClanAdmin(String name, String description, String permission, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
        setPermission(permission);
    }

    private void sendMessage(CommandSender player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private List<String> helpMenu() {
        List<String> help = new ArrayList<>();
        help.add("&7|&e) &6/clanadmin &freload <&7configName&f>");
        help.add("&7|&e) &6/clanadmin &fgetid <&7clanNamef>");
        help.add("&7|&e) &6/clanadmin &fidmode");
        return help;
    }
    
    private ClanUtil getUtil() {
        return new ClanUtil();
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        if (!(commandSender instanceof Player)) {
            return true;
        }

        /*
        // VARIABLE CREATION
        //  \/ \/ \/ \/ \/ \/
         */
        int length = args.length;
        Player p = (Player) commandSender;
        StringLibrary lib = new StringLibrary();
        /*
        //  /\ /\ /\ /\ /\ /\
        //
         */
        if (!p.hasPermission(this.getPermission())) {
            lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + '"');
            return true;
        }
        if (length == 0) {
            lib.sendMessage(p, "&r- Command help. (&7/clan #page&r)");
        lib.paginatedList(p, helpMenu(), "c", 1, 5);
            return true;
        }
        if (!p.hasPermission(this.getPermission())) {
            lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + '"');
            return true;
        }
        if (length == 1) {
            String args0 = args[0];
            if (args0.equalsIgnoreCase("reload")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clanadmin reload <fileName>");
                return true;
            }
            if (args0.equalsIgnoreCase("getid")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clanadmin getid <playerName>");
                return true;
            }
            if (args0.equalsIgnoreCase("idmode")) {
                if (!HempfestClans.idMode.containsKey(p)) {
                    HempfestClans.idMode.put(p, "ENABLED");
                    lib.sendMessage(p, "&f[&a&oADMIN&f] &6&lID &fmode &aENABLED.");
                    return true;
                }
                if (HempfestClans.idMode.get(p).equals("ENABLED")) {
                    HempfestClans.idMode.put(p, "DISABLED");
                    lib.sendMessage(p, "&f[&a&oADMIN&f] &6&lID &fmode &cDISABLED.");
                    return true;
                }
                if (HempfestClans.idMode.get(p).equals("DISABLED")) {
                    HempfestClans.idMode.put(p, "ENABLED");
                    lib.sendMessage(p, "&f[&a&oADMIN&f] &6&lID &fmode &aENABLED.");
                    return true;
                }
                return true;
            }
            return true;
        }

        if (length == 2) {
            String args0 = args[0];
            String args1 = args[1];
            if (args0.equalsIgnoreCase("getid")) {
                Player target = Bukkit.getPlayer(args1);
                if (target == null) {
                    
                    try {
                        lib.sendMessage(p, "&7#&fID &7of clan " + '"' + args1 + '"' + " is: &e&o" + getUtil().getClanID(args1));
                    } catch (NullPointerException e) {
                        lib.sendMessage(p, "&c&oUh-oh there was an issue finding the clan.. Check console for errors");
                        HempfestClans.getInstance().getLogger().severe(String.format("[%s] - Illegal use of ID retrieval. Clan directory non-existent.", HempfestClans.getInstance().getDescription().getName()));
                    }
                    return true;
                }
                
                lib.sendMessage(p, "&7|&e) &6&l" + target.getName() + "'s &e&oclan ID is &f" + getUtil().getClan(target));
                return true;
            }
            if (args0.equalsIgnoreCase("reload")) {
                DataManager dm = new DataManager(args1, "Configuration");
                Config file = dm.getFile(ConfigType.MISC_FILE);
                if (file.exists()) {
                    file.reload();
                    lib.sendMessage(p, "&a&oFile by the name of " + '"' + args1 + '"' + " was reloaded.");
                } else {
                    lib.sendMessage(p, "&c&oFile by the name of " + '"' + args1 + '"' + " not found.");
                    return true;
                }
                return true;
            }
            return true;
        }

        if (length == 3) {
            String args0 = args[0];
            String args1 = args[1];
            String args2 = args[2];
            return true;
        }


        return false;
    }
}
