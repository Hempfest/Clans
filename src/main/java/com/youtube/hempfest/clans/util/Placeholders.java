package com.youtube.hempfest.clans.util;

import com.github.ms5984.clans.clansbanks.ClansBanks;
import com.github.ms5984.clans.clansbanks.api.BanksAPI;
import com.github.ms5984.clans.clansbanks.api.ClanBank;
import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.util.construct.Clan;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

    public HempfestClans plugin;

    public Placeholders(HempfestClans plugin) {
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     * 
     * @return The name of the author as a String.
     */
    @Override
    public @NotNull String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest 
     * method to obtain a value if a placeholder starts with our 
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public @NotNull String getIdentifier(){
        return "clans";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public @NotNull String getVersion(){
        return plugin.getDescription().getVersion();
    }

    /**
     * This is the method called when a placeholder with our identifier 
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A player link
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier){

        if(player == null){
            return "";
        }

        // %someplugin_placeholder1%
        if(identifier.equals("name")){
            String result = "";
            if (Clan.clanUtil.getClan(player) != null) {
                if (Clan.clanUtil.getClanTag(Clan.clanUtil.getClan(player)) == null) {
                    result = "&4Error";
                } else
                result = Clan.clanUtil.getClanTag(Clan.clanUtil.getClan(player));
            }
            return result;
        }

        if(identifier.equals("power")){
            String result = "0";
            if (Clan.clanUtil.getClan(player) != null) {
                if (Clan.clanUtil.getClanTag(Clan.clanUtil.getClan(player)) == null) {
                    result = "&4Error";
                } else
                    result = "" + HempfestClans.clanManager(player).getPower();
            }
            return result;
        }

        if(identifier.equals("bank")){
            String result = "0";
            if (Clan.clanUtil.getClan(player) != null) {
                if (Clan.clanUtil.getClanTag(Clan.clanUtil.getClan(player)) == null) {
                    result = "&4Error";
                } else {
                    if (Bukkit.getPluginManager().isPluginEnabled("ClansBanks")) {
                        BanksAPI api = ClansBanks.getAPI();
                        ClanBank bank = api.getBank(HempfestClans.clanManager(player));
                        double bal = bank.getBalance().doubleValue();
                        result = "" + bal;
                    } else {
                        result = "&4Error";
                    }
                }
            }
            return result;
        }

        if (identifier.equals("rank")) {
            String result = "";
            if (Clan.clanUtil.getClan(player) != null) {
                if (Clan.clanUtil.getClanTag(Clan.clanUtil.getClan(player)) == null) {
                    result = "&4Error";
                } else
                result = Clan.clanUtil.getRank(player);
            }
            return result;
        }

        if (identifier.equals("raidshield")) {
            if (Clan.clanUtil.shieldStatus()) {
                return "&a&oActive";
            }
            if (!Clan.clanUtil.shieldStatus()) {
                return "&c&oDe-active";
            }
        }
 
        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%) 
        // was provided
        return null;
    }
}
	
	

