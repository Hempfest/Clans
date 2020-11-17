package com.youtube.hempfest.clans.commands;

import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.Color;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.construct.ClaimUtil;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.hempcore.formatting.string.PaginatedAssortment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.dynmap.markers.AreaMarker;

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
        help.add("&7|&e) &6/clan &fmap");
        help.add("&7|&e) &6/clan &funmap");
        help.add("&7|&e) &6/clan &fpassowner <&7playerName&f>");
        help.add("&7|&e) &6/clan &fally <&7clanName&f>");
        help.add("&7|&e) &6/clan &fally <&aadd&7,&cremove&f> <&7clanName&f>");
        help.add("&7|&e) &6/clan &fenemy <&7clanName&f>");
        help.add("&7|&e) &6/clan &fenemy <&aadd&7,&cremove&f> <&7clanName&f>");
        return help;
    }

    private boolean isAlphaNumeric(String s) {
        return s != null && s.matches("^[a-zA-Z0-9]*$");
    }
    
    private ClanUtil getUtil() {
        return Clan.clanUtil;
    }

    private ClaimUtil getClaim() {
        return Claim.claimUtil;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        List<String> array = new ArrayList<>();
        if (args.length == 1)
            array.addAll(Arrays.asList("create", "map", "unmap", "password", "kick", "leave", "message", "chat", "info", "promote", "demote", "tag", "nickname", "list", "base", "setbase", "top", "claim", "unclaim", "passowner", "ally", "enemy"));
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("unclaim")) {
                array.add("all");
            }
            if (args[0].equalsIgnoreCase("ally")) {
                array.add("add");
                array.add("remove");
            }
            if (args[0].equalsIgnoreCase("enemy")) {
                array.add("add");
                array.add("remove");
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("ally")) {
                array.addAll(Clan.clanUtil.getAllClanNames());
            }
            if (args[0].equalsIgnoreCase("enemy")) {
                array.addAll(Clan.clanUtil.getAllClanNames());
            }
        }
        return array;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        return super.tabComplete(sender, alias, args, location);
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
            PaginatedAssortment helpAssist = new PaginatedAssortment(p, helpMenu());
            lib.sendMessage(p, "&r- Command help. (&7/clan #page&r)");
            helpAssist.setListTitle("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            helpAssist.setListBorder("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            helpAssist.setNavigateCommand("c");
            helpAssist.setLinesPerPage(5);
            helpAssist.export(1);
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
            if (args0.equalsIgnoreCase("password") || args0.equalsIgnoreCase("pass")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan password <newPassword>");
                return true;
            }
            if (args0.equalsIgnoreCase("join")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan join <clanName> <password>");
                return true;
            }
            if (args0.equalsIgnoreCase("top")) {
                getUtil().getLeaderboard(p, 1);
                return true;
            }
            if (args0.equalsIgnoreCase("list")) {
                
                lib.sendMessage(p, "&r- Clan roster. (&7/clan info clanName&r)");
                lib.paginatedClanList(p, getUtil().getAllClanNames(), "c list", 1, 10);
                return true;
            }
            if (args0.equalsIgnoreCase("claim")) {
                
                if (Claim.claimUtil.claimingAllowed()) {
                    if (getUtil().getClan(p) != null) {
                        if (getUtil().getRankPower(p) >= getUtil().claimingClearance()) {
                            getClaim().obtain(p);
                            HempfestClans.getInstance().claimMap.clear();
                            Claim.claimUtil.loadClaims();
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
            if (args0.equalsIgnoreCase("unmap")) {
                if (Clan.clanUtil.getClan(p) != null) {
                    Clan clan = HempfestClans.clanManager(p);
                    if (Claim.claimUtil.isInClaim(p.getLocation())) {
                        Claim claim = new Claim(Claim.claimUtil.getClaimID(p.getLocation()));
                        if (Arrays.asList(clan.getOwnedClaims()).contains(claim.getClaimID())) {
                            Set<AreaMarker> markers = HempfestClans.getInstance().integration.markerset.getAreaMarkers();
                            for (AreaMarker am : markers) {
                                if (am.getMarkerID().equals(claim.getClaimID())) {
                                    am.deleteMarker();
                                    lib.sendMessage(p, "&b&oCurrent claim visibility has been removed from the map.");
                                    return true;
                                }
                            }
                            return true;
                        }
                        // not owner
                    } else {
                        // not in claim
                    }
                }
            }

            if (args0.equalsIgnoreCase("unclaim")) {
                
                if (Claim.claimUtil.claimingAllowed()) {
                    if (getUtil().getClan(p) != null) {
                        if (getUtil().getRankPower(p) >= getUtil().claimingClearance()) {
                            getClaim().remove(p);
                            HempfestClans.getInstance().claimMap.clear();
                            Claim.claimUtil.loadClaims();
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
                
                if (getUtil().getClan(p) != null) {
                    if (HempfestClans.chatMode.get(p).equals("GLOBAL")) {
                        HempfestClans.chatMode.put(p, "CLAN");
                        lib.sendMessage(p, "&7&oSwitched to &3CLAN &7&ochat channel.");
                        return true;
                    }
                    if (HempfestClans.chatMode.get(p).equals("CLAN")) {
                        HempfestClans.chatMode.put(p, "ALLY");
                        lib.sendMessage(p, "&7&oSwitched to &aALLY &7&ochat channel.");
                        return true;
                    }
                    if (HempfestClans.chatMode.get(p).equals("ALLY")) {
                        HempfestClans.chatMode.put(p, "GLOBAL");
                        lib.sendMessage(p, "&7&oSwitched to &fGLOBAL &7&ochat channel.");
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
            if (args0.equalsIgnoreCase("color")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan color <newTagColor>");
                for (Color color : Color.values()) {
                    lib.sendMessage(p, "&7|&e)&r " + getUtil().getColor(color.name().replaceAll("_", "")) + color.name());
                }
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
                
                getUtil().leave(p);
                HempfestClans.chatMode.put(p, "GLOBAL");
                return true;
            }
            if (args0.equalsIgnoreCase("message")) {
                lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan message <message>");
                return true;
            }
            if (args0.equalsIgnoreCase("base")) {
                
                if (getUtil().getClan(p) != null) {
                    getUtil().teleportBase(p);
                    lib.sendMessage(p, "&e&oWelcome to the clan base... i think..");
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("setbase")) {
                
                Clan clan = HempfestClans.clanManager(p);
                if (getUtil().getRankPower(p) >= getUtil().baseClearance()) {
                    clan.updateBase(p.getLocation());
                } else {
                    lib.sendMessage(p, "&c&oYou do not have clan clearance.");
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("info") || args0.equalsIgnoreCase("i")) {
                
                if (getUtil().getClan(p) != null) {
                    getUtil().getMyClanInfo(p, 1);
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("members")) {
                
                if (getUtil().getClan(p) != null) {
                    getUtil().getMyClanInfo(p, 1);
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            try {
                int page = Integer.parseInt(args0);
                PaginatedAssortment helpAssist = new PaginatedAssortment(p, helpMenu());
                lib.sendMessage(p, "&r- Command help. (&7/clan #page&r)");
                helpAssist.setListTitle("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                helpAssist.setListBorder("&7&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                helpAssist.setNavigateCommand("c");
                helpAssist.setLinesPerPage(5);
                helpAssist.export(page);
            } catch (NumberFormatException e) {
                lib.sendMessage(p, "&c&oInvalid page number!");
            }
            return true;
        }

        if (length == 2) {
            String args0 = args[0];
            String args1 = args[1];
            if (args0.equalsIgnoreCase("create")) {
                
                if (!isAlphaNumeric(args1)) {
                    lib.sendMessage(p, "&c&oInvalid clan name. Must contain only Alpha-numeric characters.");
                    return true;
                }
                if (Clan.clanUtil.getAllClanNames().contains(args1)) {
                    lib.sendMessage(p, "&c&oA clan with this name already exists! Try another.");
                    return true;
                }
                getUtil().create(p, args1, null);
                return true;
            }
            if (args0.equalsIgnoreCase("nick") || args0.equalsIgnoreCase("nickname")) {
                
                if (getUtil().getClan(p) != null) {
                    if (!isAlphaNumeric(args1)) {
                        lib.sendMessage(p, "&c&oInvalid nickname. Must contain only Alpha-numeric characters.");
                        return true;
                    }
                    getUtil().changeNickname(p, args1);
                } else {
                    lib.sendMessage(p, lib.notInClan());
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("top")) {
                
                try {
                    getUtil().getLeaderboard(p, Integer.parseInt(args1));
                } catch (IllegalFormatException e) {
                lib.sendMessage(p, "&c&oInvalid page number!");
                }
                return true;
            }
            if (args0.equalsIgnoreCase("passowner")) {
                
                if (getUtil().getClan(p) != null) {
                    getUtil().transferOwner(p, args1);
                } else {
                    lib.sendMessage(p, lib.notInClan());
                }
                return true;
            }
            if (args0.equalsIgnoreCase("list")) {
                
                try {
                    lib.paginatedClanList(p, getUtil().getAllClanNames(), "c list", Integer.parseInt(args1), 10);
                } catch (NumberFormatException e) {
                    lib.sendMessage(p, "&c&oInvalid page number!");
                }
                return true;
            }
            if (args0.equalsIgnoreCase("join")) {
                
                getUtil().joinClan(p, args1, "none");
                return true;
            }
            if (args0.equalsIgnoreCase("tag")) {
                
                Clan clan = HempfestClans.clanManager(p);
                if (getUtil().getClan(p) != null) {
                    if (getUtil().getRankPower(p) >= getUtil().tagChangeClearance()) {
                        if (!isAlphaNumeric(args1)) {
                            lib.sendMessage(p, "&c&oInvalid clan name. Must contain only Alpha-numeric characters.");
                            return true;
                        }
                        if (args1.length() > HempfestClans.getMain().getConfig().getInt("Clans.tag-size")) {
                            getUtil().sendMessage(p, "&c&oThe clan name you have chosen is too long! Max tag length reached.");
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
            if (args0.equalsIgnoreCase("color")) {

                Clan clan = HempfestClans.clanManager(p);
                if (getUtil().getClan(p) != null) {
                    if (getUtil().getRankPower(p) >= getUtil().colorChangeClearance()) {
                        clan.changeColor(args1.replaceAll("_", ""));
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
                
                DataManager dm = new DataManager("Config", "Configuration");
                Config main = dm.getFile(ConfigType.MISC_FILE);
                String adminRank = main.getConfig().getString("Formatting.Styles.Full.Admin");
                String ownerRank = main.getConfig().getString("Formatting.Styles.Full.Owner");
                if (getUtil().getClan(p) != null) {
                    if (getUtil().getRankPower(p) >= getUtil().positionClearance()) {
                        Player target = Bukkit.getPlayer(args1);
                        if (target == null) {
                            lib.sendMessage(p, "&c&oPlayer not online. Unable to promote.");
                            return true;
                        }
                        if (getUtil().getRankPower(target) >= 2) {
                            lib.sendMessage(p, "&c&oThis player is rank &b&o" + adminRank.toUpperCase() + " &c&omost powerful next to &b&l" + ownerRank);
                            return true;
                        }
                        getUtil().promotePlayer(target);
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
                
                if (getUtil().getClan(p) != null) {
                    if (getUtil().getRankPower(p) >= getUtil().positionClearance()) {
                        Player target = Bukkit.getPlayer(args1);
                        if (target == null) {
                            lib.sendMessage(p, "&c&oPlayer not online. Unable to demote.");
                            return true;
                        }
                        if (getUtil().getRankPower(target) >= getUtil().getRankPower(p)) {
                            lib.sendMessage(p, "&c&oThis player has more or the same level power as you.");
                            return true;
                        }
                        getUtil().demotePlayer(target);
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
                if (Claim.claimUtil.claimingAllowed()) {
                    if (args1.equalsIgnoreCase("all")) {
                        
                        if (getUtil().getClan(p) != null) {
                            if (getUtil().getRankPower(p) >= getUtil().unclaimAllClearance()) {
                                getClaim().removeAll(p);
                                HempfestClans.getInstance().claimMap.clear();
                                Claim.claimUtil.loadClaims();
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
            if (args0.equalsIgnoreCase("password") || args0.equalsIgnoreCase("pass")) {
                
                Clan clan = HempfestClans.clanManager(p);
                if (getUtil().getClan(p) != null) {
                    if (getUtil().getRankPower(p) >= getUtil().passwordClearance()) {
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
                
                if (getUtil().getClan(p) != null) {
                    if (getUtil().getRankPower(p) >= getUtil().kickClearance()) {
                        Player target = Bukkit.getPlayer(args1);
                        if (target == null) {
                            lib.sendMessage(p, "&c&oThis player doesn't exist or isn't online.");
                            return true;
                        }
                        Clan clan = HempfestClans.clanManager(p);
                        if (!Arrays.asList(clan.getMembers()).contains(target.getName())) {
                            lib.sendMessage(p, "&c&oThis player isn't a member of your clan.");
                            return true;
                        }
                        if (getUtil().getRankPower(target) > getUtil().getRankPower(p)) {
                            lib.sendMessage(p, "&c&oThis player has more power than you.");
                            return true;
                        }
                        getUtil().kickPlayer(target);
                        clan.messageClan("&e&oPlayer " + '"' + target.getName() + '"' + " was kicked from the clan..");
                        lib.sendMessage(target, "&4&o" + getUtil().getClanTag(getUtil().getClan(p)) + " kicked you from the clan.");
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
                
                if (getUtil().getClan(p) != null) {
                    try {
                        int page = Integer.parseInt(args1);
                        getUtil().getMyClanInfo(p, page);
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
                
                Player target = Bukkit.getPlayer(args1);
                if (target == null) {
                    if (!getUtil().getAllClanNames().contains(args1)) {
                        lib.sendMessage(p, "&c&oThis clan does not exist!");
                        return true;
                    }
                    if (args1.equals(getUtil().getClanTag(getUtil().getClan(p)))) {
                        getUtil().getMyClanInfo(p, 1);
                        return true;
                    }
                    Clan clan = new Clan(getUtil().getClanID(args1));
                        for (String info : clan.getClanInfo()) {
                            sendMessage(p, info);
                        }
                    if (HempfestClans.idMode.containsKey(p) && HempfestClans.idMode.get(p).equals("ENABLED")) {
                        lib.sendMessage(p, "&7#&fID &7of clan " + '"' + args1 + '"' + " is: &e&o" + getUtil().getClanID(args1));
                    }
                return true;
                }
                if (getUtil().getClan(target) != null) {
                    Clan clanIndex = HempfestClans.clanManager(target);
                    String clanName = getUtil().getClanTag(getUtil().getClan(target));
                        for (String info : clanIndex.getClanInfo()) {
                            sendMessage(p, info);
                        }
                    if (HempfestClans.idMode.containsKey(p) && HempfestClans.idMode.get(p).equals("ENABLED")) {
                        lib.sendMessage(p, "&7#&fID &7of player " + '"' + target.getName() + '"' +  " clan " + '"' + clanName + '"' + " is: &e&o" + getUtil().getClanID(clanName));
                    }
                } else {
                    lib.sendMessage(p, target.getName() + " &c&oisn't in a clan.");
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("message")) {
                
                Clan clan = HempfestClans.clanManager(p);
                if (getUtil().getClan(p) != null)
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
                if (!isAlphaNumeric(args1)) {
                    lib.sendMessage(p, "&c&oInvalid clan name. Must contain only Alpha-numeric characters.");
                    return true;
                }
                if (Clan.clanUtil.getAllClanNames().contains(args1)) {
                    lib.sendMessage(p, "&c&oA clan with this name already exists! Try another.");
                    return true;
                }
                getUtil().create(p, args1, args2);
                return true;
            }
            if (args0.equalsIgnoreCase("join")) {
                
                getUtil().joinClan(p, args1, args2);
                return true;
            }
            if (args0.equalsIgnoreCase("message")) {
                
                if (getUtil().getClan(p) != null) {
                    Clan clan = HempfestClans.clanManager(p);
                    clan.messageClan(p.getName() + " say's : " + args1 + " " + args2);
                }
                return true;
            }
            if (args0.equalsIgnoreCase("enemy")) {
                
                if (args1.equalsIgnoreCase("add")) {
                    if (getUtil().getClan(p) != null) {
                        if (!getUtil().getAllClanNames().contains(args2)) {
                            lib.sendMessage(p, "&c&oThis clan does not exist!");
                            return true;
                        }
                        if (args2.equals(getUtil().getClanTag(getUtil().getClan(p)))) {
                            lib.sendMessage(p, "&c&oYou can not ally your own clan!");
                            return true;
                        }
                        String targetClan = getUtil().getClanID(args2);
                        if (getUtil().getEnemies(getUtil().getClan(p)).contains(targetClan)) {
                            lib.sendMessage(p, "&c&oYou are already enemies with this clan.\nTo become neutral type &7/clan enemy remove " + args2);
                            return true;
                        }
                        if (getUtil().isNeutral(getUtil().getClan(p), targetClan)) {
                            getUtil().addEnemy(getUtil().getClan(p), targetClan);
                            return true;
                        }
                        if (getUtil().getAllies(getUtil().getClan(p)).contains(targetClan)) {
                            getUtil().addEnemy(getUtil().getClan(p), targetClan);
                            return true;
                        }
                    }
                    return true;
                }
                if (args1.equalsIgnoreCase("remove")) {
                    if (getUtil().getClan(p) != null) {
                        if (!getUtil().getAllClanNames().contains(args2)) {
                            lib.sendMessage(p, "&c&oThis clan does not exist!");
                            return true;
                        }
                        if (args2.equals(getUtil().getClanTag(getUtil().getClan(p)))) {
                            lib.sendMessage(p, "&c&oYou can not ally your own clan!");
                            return true;
                        }
                        String targetClan = getUtil().getClanID(args2);
                        if (getUtil().getEnemies(targetClan).contains(getUtil().getClan(p))) {
                            lib.sendMessage(p, "&c&oThis clan has marked you as an &4enemy");
                            return true;
                        }
                        if (!getUtil().getEnemies(getUtil().getClan(p)).contains(targetClan)) {
                            lib.sendMessage(p, "&f&oYou are not enemies with this clan.");
                            return true;
                        }
                        getUtil().removeEnemy(getUtil().getClan(p), targetClan);
                    } else {
                        lib.sendMessage(p, lib.notInClan());
                        return true;
                    }
                    return true;
                }
                return true;
            }
            if (args0.equalsIgnoreCase("ally")) {
                
                if (args1.equalsIgnoreCase("add")) {
                    if (getUtil().getClan(p) != null) {
                        if (!getUtil().getAllClanNames().contains(args2)) {
                            lib.sendMessage(p, "&c&oThis clan does not exist!");
                            return true;
                        }
                        if (args2.equals(getUtil().getClanTag(getUtil().getClan(p)))) {
                            lib.sendMessage(p, "&c&oYou can not ally your own clan!");
                            return true;
                        }
                        String targetClan = getUtil().getClanID(args2);
                        if (getUtil().getAllies(getUtil().getClan(p)).contains(targetClan)) {
                            lib.sendMessage(p, "&a&oYou are already allies\nTo become neutral type &7/clan ally remove " + args2);
                            return true;
                        }
                        if (getUtil().getEnemies(targetClan).contains(getUtil().getClan(p))) {
                            lib.sendMessage(p, "&c&oClan " + '"' + "&4" + args2 + "&c&o" + '"' + " is currently enemies with you.");
                            return true;
                        }
                        if (getUtil().isNeutral(getUtil().getClan(p), targetClan)) {
                            getUtil().sendAllyRequest(p, getUtil().getClan(p), targetClan);
                            return true;
                        }
                        getUtil().addAlly(getUtil().getClan(p), targetClan);
                    } else {
                        lib.sendMessage(p, lib.notInClan());
                        return true;
                    }
                    return true;
                }
                if (args1.equalsIgnoreCase("remove")) {
                    if (getUtil().getClan(p) != null) {
                        if (!getUtil().getAllClanNames().contains(args2)) {
                            lib.sendMessage(p, "&c&oThis clan does not exist!");
                            return true;
                        }
                        if (args2.equals(getUtil().getClanTag(getUtil().getClan(p)))) {
                            lib.sendMessage(p, "&c&oYou can not ally your own clan!");
                            return true;
                        }
                        String targetClan = getUtil().getClanID(args2);
                        if (getUtil().isNeutral(getUtil().getClan(p), targetClan)) {
                            lib.sendMessage(p, "&f&oYou are currently neutral with this clan.");
                            return true;
                        }
                        getUtil().removeAlly(getUtil().getClan(p), targetClan);
                        getUtil().removeAlly(targetClan, getUtil().getClan(p));
                        Clan clan = HempfestClans.clanManager(p);
                        Clan clan2 = new Clan(targetClan);
                        clan.messageClan("&f&oNow neutral with clan " + '"' + "&e" + getUtil().getClanTag(targetClan) + "&f&o" + '"');
                        clan2.messageClan("&f&oNow neutral with clan " + '"' + "&e" + getUtil().getClanTag(getUtil().getClan(p)) + "&f&o" + '"');
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

        String args0 = args[0];
        StringBuilder rsn = new StringBuilder();
        for (int i = 1; i < args.length; i++)
            rsn.append(args[i]).append(" ");
        int stop = rsn.length() - 1;
        if (args0.equalsIgnoreCase("message")) {
            Clan clan = HempfestClans.clanManager(p);
            clan.messageClan(p.getName() + " say's : " + rsn.substring(0, stop));
            return true;
        }

        lib.sendMessage(p, "Unknown sub-command. Use " + '"' + "/clan" + '"' + " for help.");
        return true;


    }
}
