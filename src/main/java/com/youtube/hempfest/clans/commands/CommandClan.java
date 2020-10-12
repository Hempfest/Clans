package com.youtube.hempfest.clans.commands;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanClaim;
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
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;

public class CommandClan extends BukkitCommand {


    public CommandClan(String name, String description, String permission, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
        setPermission(permission);
    }

    private void sendMessage(CommandSender player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private String notPlayer() {
        return String.format("[%s] - You aren't a player..", HempfestClans.getInstance().getDescription().getName());
    }

    private List<String> helpMenu() {
        List<String> help = new ArrayList<>();
        help.add("&7|&e) &6/clan &fcreate <&7clanName&f> <&7password&f>");
        help.add("&7|&e) &6/clan &fpassword <&7newPassword&f>");
        help.add("&7|&e) &6/clan &fleave");
        help.add("&7|&e) &6/clan &fkick <&7playerName&f>");
        help.add("&7|&e) &6/clan &fmessage <&7message&f>");
        help.add("&7|&e) &6/clan &fchat");
        help.add("&7|&e) &6/clan &finfo <&7clanName&f>");
        help.add("&7|&e) &6/clan &finfo <&7playerName&f>");
        help.add("&7|&e) &6/clan &finfo");
        help.add("&7|&e) &6/clan &fpromote <&7playerName&f>");
        help.add("&7|&e) &6/clan &fdemote <&7playerName&f>");
        help.add("&7|&e) &6/clan &ftag <&7newTag&f>");
        help.add("&7|&e) &6/clan &fnickname <&7nickName&f>");
        help.add("&7|&e) &6/clan &flist");
        help.add("&7|&e) &6/clan &fbase");
        help.add("&7|&e) &6/clan &fsetbase");
        help.add("&7|&e) &6/clan &ftop");
        help.add("&7|&e) &6/clan &fclaim");
        help.add("&7|&e) &6/clan &funclaim");
        help.add("&7|&e) &6/clan &funclaim all");
        help.add("&7|&e) &6/clan &fpassowner <&7playerName&f>");
        help.add("&7|&e) &6/clan &fally <&7clanName&f>");
        help.add("&7|&e) &6/clan &fally <&aadd&7,&cremove&f> <&7clanName&f>");
        help.add("&7|&e) &6/clan &fenemy <&7clanName&f>");
        help.add("&7|&e) &6/clan &fenemy <&aadd&7,&cremove&f> <&7clanName&f>");
        return help;
    }

    private boolean claimingAllowed() {
        DataManager dm = new DataManager("Config", "Configuration");
        Config main = dm.getFile(ConfigType.MISC_FILE);
        return main.getConfig().getBoolean("Clans.land-claiming.allow");
    }

    private boolean isAlphaNumeric(String s) {
        return s != null && s.matches("^[a-zA-Z0-9]*$");
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(notPlayer());
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
            if (args0.equalsIgnoreCase("create")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan create <clanName> <password>");
                return true;
            }
            if (args0.equalsIgnoreCase("password")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan password <newPassword>");
                return true;
            }
            if (args0.equalsIgnoreCase("join")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan join <clanName> <password>");
                return true;
            }
            if (args0.equalsIgnoreCase("top")) {
                ClanUtil clanUtil = new ClanUtil();
                clanUtil.getLeaderboard(p, 1);
                return true;
            }
            if (args0.equalsIgnoreCase("list")) {
                ClanUtil clanUtil = new ClanUtil();
                lib.sendMessage(p, "&r- Clan roster. (&7/clan info clanName&r)");
                lib.paginatedClanList(p, clanUtil.getAllClanNames(), "c list", 1, 10);
                return true;
            }
            if (args0.equalsIgnoreCase("claim")) {
                ClanUtil clanUtil = new ClanUtil();
                if (claimingAllowed()) {
                    if (clanUtil.getClan(p) != null) {
                        if (clanUtil.getRankPower(p) >= clanUtil.claimingClearance()) {
                            ClanClaim clanClaim = new ClanClaim();
                            clanClaim.obtain(p);
                        } else {
                            lib.sendMessage(p, "&c&oYou do not have clan clearance.");
                            return true;
                        }
                    } else {
                        lib.sendMessage(p, lib.notInClan());
                        return true;
                    }
                } else {
                    lib.sendMessage(p, "&c&oYour server doesn't allow the use of clan land-claiming.");
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("unclaim")) {
                ClanUtil clanUtil = new ClanUtil();
                if (claimingAllowed()) {
                    if (clanUtil.getClan(p) != null) {
                        if (clanUtil.getRankPower(p) >= clanUtil.claimingClearance()) {
                            ClanClaim clanClaim = new ClanClaim();
                            clanClaim.remove(p);
                        } else {
                            lib.sendMessage(p, "&c&oYou do not have clan clearance.");
                        }
                    } else {
                        lib.sendMessage(p, lib.notInClan());
                        return true;
                    }
                } else {
                    lib.sendMessage(p, "&c&oYour server doesn't allow the use of clan land-claiming.");
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("chat")) {
                ClanUtil clanUtil = new ClanUtil();
                if (clanUtil.getClan(p) != null) {
                    if (ClanUtil.chatMode.get(p).equals("GLOBAL")) {
                        ClanUtil.chatMode.put(p, "CLAN");
                        lib.sendMessage(p, "&3&oClan chat activated.");
                        return true;
                    }
                    if (ClanUtil.chatMode.get(p).equals("CLAN")) {
                        ClanUtil.chatMode.put(p, "GLOBAL");
                        lib.sendMessage(p, "&7&oClan chat de-activated.");
                        return true;
                    }
                }
                return true;
            }
            if (args0.equalsIgnoreCase("kick")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan kick <playerName>");
                return true;
            }
            if (args0.equalsIgnoreCase("passowner")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan passowner <playerName>");
                return true;
            }
            if (args0.equalsIgnoreCase("tag")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan tag <newTag>");
                return true;
            }
            if (args0.equalsIgnoreCase("nick") || args0.equalsIgnoreCase("nickname")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan nickname <newNickname>");
                return true;
            }
            if (args0.equalsIgnoreCase("promote")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan promote <playerName>");
                return true;
            }
            if (args0.equalsIgnoreCase("demote")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan demote <playerName>");
                return true;
            }
            if (args0.equalsIgnoreCase("ally")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan ally <clanName>");
                return true;
            }
            if (args0.equalsIgnoreCase("enemy")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan enemy <clanName>");
                return true;
            }
            if (args0.equalsIgnoreCase("leave")) {
                ClanUtil clanUtil = new ClanUtil();
                clanUtil.leave(p);
                return true;
            }
            if (args0.equalsIgnoreCase("message")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan message <message>");
                return true;
            }
            if (args0.equalsIgnoreCase("base")) {
                ClanUtil clanUtil = new ClanUtil();
                if (clanUtil.getClan(p) != null) {
                    clanUtil.teleportBase(p);
                    lib.sendMessage(p, "&e&oWelcome to the clan base... i think..");
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("setbase")) {
                ClanUtil clanUtil = new ClanUtil();
                Clan clan = new Clan(clanUtil.getClan(p), p);
                if (clanUtil.getRankPower(p) >= clanUtil.baseClearance()) {
                    clan.updateBase(p.getLocation());
                } else {
                    lib.sendMessage(p, "&c&oYou do not have clan clearance.");
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("info") || args0.equalsIgnoreCase("i")) {
                ClanUtil clanUtil = new ClanUtil();
                if (clanUtil.getClan(p) != null) {
                    clanUtil.getMyClanInfo(p, 1);
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("members")) {
                ClanUtil clanUtil = new ClanUtil();
                if (clanUtil.getClan(p) != null) {
                    clanUtil.getMyClanInfo(p, 1);
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            try {
                int page = Integer.parseInt(args0);
                lib.paginatedList(p, helpMenu(), "c", page, 5);
            } catch (NumberFormatException e) {
                lib.sendMessage(p, "&c&oInvalid page number!");
            }
            return true;
        }

        if (length == 2) {
            String args0 = args[0];
            String args1 = args[1];
            if (args0.equalsIgnoreCase("create")) {
                ClanUtil clanUtil = new ClanUtil();
                if (!isAlphaNumeric(args1)) {
                    lib.sendMessage(p, "&c&oInvalid clan name. Must contain only Alpha-numeric characters.");
                    return true;
                }
                clanUtil.create(p, args1, null);
                return true;
            }
            if (args0.equalsIgnoreCase("nick") || args0.equalsIgnoreCase("nickname")) {
                ClanUtil clanUtil = new ClanUtil();
                if (clanUtil.getClan(p) != null) {
                    if (!isAlphaNumeric(args1)) {
                        lib.sendMessage(p, "&c&oInvalid nickname. Must contain only Alpha-numeric characters.");
                        return true;
                    }
                    clanUtil.changeNickname(p, args1);
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("top")) {
                ClanUtil clanUtil = new ClanUtil();
                try {
                    clanUtil.getLeaderboard(p, Integer.parseInt(args1));
                } catch (IllegalFormatException e) {
                lib.sendMessage(p, "&c&oInvalid page number!");
                }
                return true;
            }
            if (args0.equalsIgnoreCase("passowner")) {
                ClanUtil clanUtil = new ClanUtil();
                if (clanUtil.getClan(p) != null) {
                    clanUtil.transferOwner(p, args1);
                } else {
                    lib.sendMessage(p, lib.notInClan());
                }
                return true;
            }
            if (args0.equalsIgnoreCase("list")) {
                ClanUtil clanUtil = new ClanUtil();
                try {
                    lib.paginatedList(p, clanUtil.getAllClanNames(), "c list", Integer.parseInt(args1), 10);
                } catch (NumberFormatException e) {
                    lib.sendMessage(p, "&c&oInvalid page number!");
                }
                return true;
            }
            if (args0.equalsIgnoreCase("join")) {
                ClanUtil clanUtil = new ClanUtil();
                clanUtil.joinClan(p, args1, "none");
                return true;
            }
            if (args0.equalsIgnoreCase("tag")) {
                ClanUtil clanUtil = new ClanUtil();
                Clan clan = new Clan(clanUtil.getClan(p), p);
                if (clanUtil.getClan(p) != null) {
                    if (clanUtil.getRankPower(p) >= clanUtil.tagChangeClearance()) {
                        if (!isAlphaNumeric(args1)) {
                            lib.sendMessage(p, "&c&oInvalid clan name. Must contain only Alpha-numeric characters.");
                            return true;
                        }
                        clan.changeTag(args1);
                    } else {
                        lib.sendMessage(p, "&c&oYou do not have clan clearance.");
                    }
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("promote")) {
                ClanUtil clanUtil = new ClanUtil();
                DataManager dm = new DataManager("Config", "Configuration");
                Config main = dm.getFile(ConfigType.MISC_FILE);
                String adminRank = main.getConfig().getString("Formatting.Styles.Full.Admin");
                String ownerRank = main.getConfig().getString("Formatting.Styles.Full.Owner");
                if (clanUtil.getClan(p) != null) {
                    if (clanUtil.getRankPower(p) >= clanUtil.positionClearance()) {
                        Player target = Bukkit.getPlayer(args1);
                        if (target == null) {
                            lib.sendMessage(p, "&c&oPlayer not online. Unable to promote.");
                            return true;
                        }
                        if (clanUtil.getRankPower(target) >= 2) {
                            lib.sendMessage(p, "&c&oThis player is rank &b&o" + adminRank.toUpperCase() + " &c&omost powerful next to &b&l" + ownerRank);
                            return true;
                        }
                        clanUtil.promotePlayer(target);
                    } else {
                        lib.sendMessage(p, "&c&oYou do not have clan clearance.");
                        return true;
                    }
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("demote")) {
                ClanUtil clanUtil = new ClanUtil();
                if (clanUtil.getClan(p) != null) {
                    if (clanUtil.getRankPower(p) >= clanUtil.positionClearance()) {
                        Player target = Bukkit.getPlayer(args1);
                        if (target == null) {
                            lib.sendMessage(p, "&c&oPlayer not online. Unable to demote.");
                            return true;
                        }
                        if (clanUtil.getRankPower(target) >= clanUtil.getRankPower(p)) {
                            lib.sendMessage(p, "&c&oThis player has more or the same level power as you.");
                            return true;
                        }
                        clanUtil.demotePlayer(target);
                    } else {
                        lib.sendMessage(p, "&c&oYou do not have clan clearance.");
                        return true;
                    }
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("unclaim")) {
                if (claimingAllowed()) {
                    if (args1.equalsIgnoreCase("all")) {
                        ClanUtil clanUtil = new ClanUtil();
                        if (clanUtil.getClan(p) != null) {
                            if (clanUtil.getRankPower(p) >= clanUtil.unclaimAllClearance()) {
                                ClanClaim clanClaim = new ClanClaim();
                                clanClaim.removeAll(p);
                            } else {
                                lib.sendMessage(p, "&c&oYou do not have clan clearance.");
                                return true;
                            }
                        } else {
                            lib.sendMessage(p, lib.notInClan());
                            return true;
                        }
                        return true;
                    }
                } else {
                    lib.sendMessage(p, "&c&oYour server doesn't allow the use of clan land-claiming.");
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("password")) {
                ClanUtil clanUtil = new ClanUtil();
                Clan clan = new Clan(clanUtil.getClan(p), p);
                if (clanUtil.getClan(p) != null) {
                    if (clanUtil.getRankPower(p) >= clanUtil.passwordClearance()) {
                        if (!isAlphaNumeric(args1)) {
                            lib.sendMessage(p, "&c&oInvalid password. Must contain only Alpha-numeric characters.");
                            return true;
                        }
                        clan.changePassword(args1);
                    } else {
                        lib.sendMessage(p, "&c&oYou do not have clan clearance.");
                        return true;
                    }
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("kick")) {
                ClanUtil clanUtil = new ClanUtil();
                if (clanUtil.getClan(p) != null) {
                    if (clanUtil.getRankPower(p) >= clanUtil.kickClearance()) {
                        Player target = Bukkit.getPlayer(args1);
                        if (target == null) {

                            return true;
                        }
                        Clan clan = new Clan(clanUtil.getClan(p), target);
                        if (!Arrays.asList(clan.getMembers()).contains(target.getName())) {
                            lib.sendMessage(p, "&c&oThis player isn't a member of your clan.");
                            return true;
                        }
                        if (clanUtil.getRankPower(target) > clanUtil.getRankPower(p)) {
                            lib.sendMessage(p, "&c&oThis player has more power than you.");
                            return true;
                        }
                        clanUtil.kickPlayer(target);
                        clan.messageClan("&e&oPlayer " + '"' + target.getName() + '"' + " was kicked from the clan..");
                        lib.sendMessage(target, "&4&o" + clanUtil.getClanTag(clanUtil.getClan(p)) + " kicked you from the clan.");
                    } else {
                        lib.sendMessage(p, "&c&oYou do not have clan clearance.");
                        return true;
                    }
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("members")) {
                ClanUtil clanUtil = new ClanUtil();
                if (clanUtil.getClan(p) != null) {
                    try {
                        int page = Integer.parseInt(args1);
                        clanUtil.getMyClanInfo(p, page);
                    } catch (NumberFormatException e) {
                        lib.sendMessage(p, "&c&oInvalid page number!");
                    }
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("info") || args0.equalsIgnoreCase("i")) {
                ClanUtil clanUtil = new ClanUtil();
                Player target = Bukkit.getPlayer(args1);
                if (target == null) {
                    String clanName = args1;
                    if (!clanUtil.getAllClanNames().contains(clanName)) {
                        lib.sendMessage(p, "&c&oThis clan does not exist!");
                        return true;
                    }
                    Clan clan = new Clan(clanUtil.getClanID(clanName), p);
                    for (String info : clan.getClanInfo()) {
                        sendMessage(p, info);
                    }
                    if (HempfestClans.idMode.containsKey(p) && HempfestClans.idMode.get(p).equals("ENABLED")) {
                        lib.sendMessage(p, "&7#&fID &7of clan " + '"' + clanName + '"' + " is: &e&o" + clanUtil.getClanID(clanName));
                    }
                return true;
                }
                if (clanUtil.getClan(target) != null) {
                    Clan clanIndex = new Clan(clanUtil.getClan(target), p);
                    String clanName = clanUtil.getClanTag(clanUtil.getClan(target));
                    for (String info : clanIndex.getClanInfo()) {
                        sendMessage(p, info);
                    }
                    if (HempfestClans.idMode.containsKey(p) && HempfestClans.idMode.get(p).equals("ENABLED")) {
                        lib.sendMessage(p, "&7#&fID &7of player " + '"' + target.getName() + '"' +  " clan " + '"' + clanName + '"' + " is: &e&o" + clanUtil.getClanID(clanName));
                    }
                } else {
                    lib.sendMessage(p, target.getName() + " &c&oisn't in a clan.");
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("message")) {
                ClanUtil clanUtil = new ClanUtil();
                Clan clan = new Clan(clanUtil.getClan(p), p);
                if (clanUtil.getClan(p) != null)
                clan.messageClan(p.getName() + " say's : " + args1);
                return true;
            }
            if (args0.equalsIgnoreCase("ally")) {
                Bukkit.dispatchCommand(p, "c ally add " + args1);
                return true;
            }
            if (args0.equalsIgnoreCase("enemy")) {
                Bukkit.dispatchCommand(p, "c enemy add " + args1);
                return true;
            }
            lib.sendMessage(p, "Unknown sub-command. Use " + '"' + "/clan" + '"' + " for help.");
            return true;
        }

        if (length == 3) {
            String args0 = args[0];
            String args1 = args[1];
            String args2 = args[2];
            if (args0.equalsIgnoreCase("create")) {
                ClanUtil clanUtil = new ClanUtil();
                clanUtil.create(p, args1, args2);
                return true;
            }
            if (args0.equalsIgnoreCase("join")) {
                ClanUtil clanUtil = new ClanUtil();
                clanUtil.joinClan(p, args1, args2);
                return true;
            }
            if (args0.equalsIgnoreCase("message")) {
                ClanUtil clanUtil = new ClanUtil();
                if (clanUtil.getClan(p) != null) {
                    Clan clan = new Clan(clanUtil.getClan(p), p);
                    clan.messageClan(p.getName() + " say's : " + args1 + " " + args2);
                }
                return true;
            }
            if (args0.equalsIgnoreCase("enemy")) {
                ClanUtil clanUtil = new ClanUtil();
                if (args1.equalsIgnoreCase("add")) {
                    if (clanUtil.getClan(p) != null) {
                        String name = args2;
                        if (!clanUtil.getAllClanNames().contains(name)) {
                            lib.sendMessage(p, "&c&oThis clan does not exist!");
                            return true;
                        }
                        if (name.equals(clanUtil.getClanTag(clanUtil.getClan(p)))) {
                            lib.sendMessage(p, "&c&oYou can not ally your own clan!");
                            return true;
                        }
                        String targetClan = clanUtil.getClanID(name);
                        if (clanUtil.getEnemies(clanUtil.getClan(p)).contains(targetClan)) {
                            lib.sendMessage(p, "&c&oYou are already enemies with this clan.\nTo become neutral type &7/clan enemy remove " + name);
                            return true;
                        }
                        if (clanUtil.isNeutral(clanUtil.getClan(p), targetClan)) {
                            clanUtil.addEnemy(clanUtil.getClan(p), targetClan);
                            return true;
                        }
                        if (clanUtil.getAllies(clanUtil.getClan(p)).contains(targetClan)) {
                            clanUtil.addEnemy(clanUtil.getClan(p), targetClan);
                            return true;
                        }
                    }
                    return true;
                }
                if (args1.equalsIgnoreCase("remove")) {
                    if (clanUtil.getClan(p) != null) {
                        String name = args2;
                        if (!clanUtil.getAllClanNames().contains(name)) {
                            lib.sendMessage(p, "&c&oThis clan does not exist!");
                            return true;
                        }
                        if (name.equals(clanUtil.getClanTag(clanUtil.getClan(p)))) {
                            lib.sendMessage(p, "&c&oYou can not ally your own clan!");
                            return true;
                        }
                        String targetClan = clanUtil.getClanID(name);
                        if (clanUtil.getEnemies(targetClan).contains(clanUtil.getClan(p))) {
                            lib.sendMessage(p, "&c&oThis clan has marked you as an &4enemy");
                            return true;
                        }
                        if (!clanUtil.getEnemies(clanUtil.getClan(p)).contains(targetClan)) {
                            lib.sendMessage(p, "&f&oYou are not enemies with this clan.");
                            return true;
                        }
                        clanUtil.removeEnemy(clanUtil.getClan(p), targetClan);
                    } else {
                        lib.sendMessage(p, lib.notInClan());
                        return true;
                    }
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("ally")) {
                ClanUtil clanUtil = new ClanUtil();
                if (args1.equalsIgnoreCase("add")) {
                    if (clanUtil.getClan(p) != null) {
                        String name = args2;
                        if (!clanUtil.getAllClanNames().contains(name)) {
                            lib.sendMessage(p, "&c&oThis clan does not exist!");
                            return true;
                        }
                        if (name.equals(clanUtil.getClanTag(clanUtil.getClan(p)))) {
                            lib.sendMessage(p, "&c&oYou can not ally your own clan!");
                            return true;
                        }
                        String targetClan = clanUtil.getClanID(name);
                        if (clanUtil.getAllies(clanUtil.getClan(p)).contains(targetClan)) {
                            lib.sendMessage(p, "&a&oYou are already allies\nTo become neutral type &7/clan ally remove " + name);
                            return true;
                        }
                        if (clanUtil.getEnemies(targetClan).contains(clanUtil.getClan(p))) {
                            lib.sendMessage(p, "&c&oClan " + '"' + "&4" + name + "&c&o" + '"' + " is currently enemies with you.");
                            return true;
                        }
                        if (clanUtil.isNeutral(clanUtil.getClan(p), targetClan)) {
                            clanUtil.sendAllyRequest(p, clanUtil.getClan(p), targetClan);
                            return true;
                        }
                        clanUtil.addAlly(p, clanUtil.getClan(p), targetClan);
                    } else {
                        lib.sendMessage(p, lib.notInClan());
                        return true;
                    }
                    return true;
                }
                if (args1.equalsIgnoreCase("remove")) {
                    if (clanUtil.getClan(p) != null) {
                        String name = args2;
                        if (!clanUtil.getAllClanNames().contains(name)) {
                            lib.sendMessage(p, "&c&oThis clan does not exist!");
                            return true;
                        }
                        if (name.equals(clanUtil.getClanTag(clanUtil.getClan(p)))) {
                            lib.sendMessage(p, "&c&oYou can not ally your own clan!");
                            return true;
                        }
                        String targetClan = clanUtil.getClanID(name);
                        if (clanUtil.isNeutral(clanUtil.getClan(p), targetClan)) {
                            lib.sendMessage(p, "&f&oYou are currently neutral with this clan.");
                            return true;
                        }
                        clanUtil.removeAlly(clanUtil.getClan(p), targetClan);
                        clanUtil.removeAlly(targetClan, clanUtil.getClan(p));
                        Clan clan = new Clan(clanUtil.getClan(p), null);
                        Clan clan2 = new Clan(targetClan, null);
                        clan.messageClan("&f&oNow neutral with clan " + '"' + "&e" + clanUtil.getClanTag(targetClan) + "&f&o" + '"');
                        clan2.messageClan("&f&oNow neutral with clan " + '"' + "&e" + clanUtil.getClanTag(clanUtil.getClan(p)) + "&f&o" + '"');
                    } else {
                        lib.sendMessage(p, lib.notInClan());
                        return true;
                    }
                    return true;
                }
            }
            lib.sendMessage(p, "Unknown sub-command. Use " + '"' + "/clan" + '"' + " for help.");
            return true;
        }

        if (length > 3) {
            String args0 = args[0];
            StringBuilder rsn = new StringBuilder();
            for (int i = 1; i < args.length; i++)
                rsn.append(String.valueOf(args[i]) + " ");
            int stop = rsn.length() - 1;
            if (args0.equalsIgnoreCase("message")) {
                ClanUtil clanUtil = new ClanUtil();
                Clan clan = new Clan(clanUtil.getClan(p), p);
                clan.messageClan(p.getName() + " say's : " + rsn.toString().substring(0, stop));
                return true;
            }
            lib.sendMessage(p, "Unknown sub-command. Use " + '"' + "/clan" + '"' + " for help.");
            return true;
        }


        return false;
    }
}
