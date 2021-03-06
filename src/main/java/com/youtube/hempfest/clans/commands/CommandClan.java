package com.youtube.hempfest.clans.commands;

import com.github.sanctum.labyrinth.formatting.string.ColoredString;
import com.github.sanctum.labyrinth.formatting.string.PaginatedAssortment;
import com.github.sanctum.labyrinth.library.TextLib;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import com.youtube.hempfest.clans.HempfestClans;
import com.youtube.hempfest.clans.bank.BankPermission;
import com.youtube.hempfest.clans.bank.Message;
import com.youtube.hempfest.clans.bank.api.BankAPI;
import com.youtube.hempfest.clans.bank.api.ClanBank;
import com.youtube.hempfest.clans.bank.model.BankAction;
import com.youtube.hempfest.clans.bank.model.BankLog;
import com.youtube.hempfest.clans.util.StringLibrary;
import com.youtube.hempfest.clans.util.construct.Claim;
import com.youtube.hempfest.clans.util.construct.ClaimUtil;
import com.youtube.hempfest.clans.util.construct.Clan;
import com.youtube.hempfest.clans.util.construct.ClanUtil;
import com.youtube.hempfest.clans.util.data.Config;
import com.youtube.hempfest.clans.util.data.ConfigType;
import com.youtube.hempfest.clans.util.data.DataManager;
import com.youtube.hempfest.clans.util.events.ClanBaseUpdateEvent;
import com.youtube.hempfest.clans.util.events.ClanCreateEvent;
import com.youtube.hempfest.clans.util.events.CommandHelpEvent;
import com.youtube.hempfest.clans.util.events.SubCommandEvent;
import com.youtube.hempfest.clans.util.events.TabInsertEvent;
import com.youtube.hempfest.clans.util.misc.Color;
import com.youtube.hempfest.clans.util.misc.Member;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

public class CommandClan extends BukkitCommand {

	private final String helpPrefix = "&7|&e) ";
	private final ImmutableList<String> defaultHelp;
	private final ImmutableList<String> bankFunctions;
	private final TextLib textLib = TextLib.getInstance();

	public CommandClan() {
		super("clan");
		setDescription("Base command for clans.");
		setAliases(Arrays.asList("clans", "cl", "c"));
		setPermission("clans.use");
		this.defaultHelp = new ImmutableList.Builder<String>()
				.add("&7|&e) &6/clan &fcreate <&7clanName&f> <&7password&f>")
		        .add("&7|&e) &6/clan &fjoin <&7clanName&f>")
		        .add("&7|&e) &6/clan &frequest <&7playerName&f>")
		        // <bank lines>
				.add("&7|&e) &6/clan &fbank")
				.add("&7|&e) &6/clan &fbank &f<&adeposit&7,&cwithdraw&f> <&7amount&f>")
				// <end bank lines>
		        .add("&7|&e) &6/clan &fblock <&7playerName&f>")
		        .add("&7|&e) &6/clan &fjoin <&7clanName&f> <&7password?&f>")
		        .add("&7|&e) &6/clan &fpassword <&7newPassword&f>")
		        .add("&7|&e) &6/clan &fleave")
		        .add("&7|&e) &6/clan &fkick <&7playerName&f>")
		        .add("&7|&e) &6/clan &fmessage <&7message&f>")
		        .add("&7|&e) &6/clan &fchat")
		        .add("&7|&e) &6/clan &finfo <&7clanName&f>")
		        .add("&7|&e) &6/clan &finfo <&7playerName&f>")
		        .add("&7|&e) &6/clan &finfo")
		        .add("&7|&e) &6/clan &fpromote <&7playerName&f>")
		        .add("&7|&e) &6/clan &fdemote <&7playerName&f>")
		        .add("&7|&e) &6/clan &ftag <&7newTag&f>")
		        .add("&7|&e) &6/clan &fnickname <&7nickName&f>")
		        .add("&7|&e) &6/clan &flist")
		        .add("&7|&e) &6/clan &fbase")
		        .add("&7|&e) &6/clan &fsetbase")
		        .add("&7|&e) &6/clan &ftop")
		        .add("&7|&e) &6/clan &fclaim")
		        .add("&7|&e) &6/clan &funclaim")
		        .add("&7|&e) &6/clan &funclaim all")
		        .add("&7|&e) &6/clan &fmap")
		        .add("&7|&e) &6/clan &ffriendlyfire")
		        .add("&7|&e) &6/clan &funmap")
		        .add("&7|&e) &6/clan &fpassowner <&7playerName&f>")
		        .add("&7|&e) &6/clan &fally <&7clanName&f>")
		        .add("&7|&e) &6/clan &fally <&aadd&7,&cremove&f> <&7clanName&f>")
		        .add("&7|&e) &6/clan &fenemy <&7clanName&f>")
		        .add("&7|&e) &6/clan &fenemy <&aadd&7,&cremove&f> <&7clanName&f>")
				.build();
		this.bankFunctions = new ImmutableList.Builder<String>()
				.add("balance")
				.add("deposit")
				.add("withdraw")
				.add("viewlog")
				.add("setperm")
				.build();
	}

	private final ConcurrentMap<Player, List<UUID>> blockedUsers = new MapMaker().
			weakKeys().
			weakValues().
			makeMap();

	private void sendMessage(CommandSender player, String message) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	private String notPlayer() {
		return String.format("[%s] - You aren't a player..", HempfestClans.getInstance().getDescription().getName());
	}

	private List<String> helpMenu() {
		List<String> help = new ArrayList<>(defaultHelp);
		CommandHelpEvent e = new CommandHelpEvent(help);
		Bukkit.getPluginManager().callEvent(e);
		return e.getHelpMenu();
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

	private final List<String> arguments = new ArrayList<String>();

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {


		List<String> result = new ArrayList<>();
		if (args.length == 1) {
			arguments.clear();
			arguments.addAll(Arrays.asList("create", "request", "bank", "block", "friendlyfire", "color", "password", "kick", "leave", "message", "chat", "info", "promote", "demote", "tag", "nickname", "list", "base", "setbase", "top", "claim", "unclaim", "passowner", "ally", "enemy"));
			TabInsertEvent event = new TabInsertEvent(args);
			Bukkit.getPluginManager().callEvent(event);
			arguments.addAll(event.getArgs(1));
			for (String a : arguments) {
				if (a.toLowerCase().startsWith(args[0].toLowerCase()))
					result.add(a);
			}
			return result;
		}
		if (args.length == 2) {
			TabInsertEvent event = new TabInsertEvent(args);
			Bukkit.getPluginManager().callEvent(event);
			arguments.addAll(event.getArgs(2));

			for (String t : event.getArgs(2)) {
				if (t.toLowerCase().startsWith(args[1].toLowerCase()))
					result.add(t);
			}
			if (args[0].equalsIgnoreCase("unclaim")) {
				arguments.clear();
				arguments.add("all");
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("color")) {
				arguments.clear();
				for (Color color : Color.values()) {
					arguments.add(color.name().toLowerCase());
				}
				for (String a : arguments) {
					String arg = args[1];
					if (arg.endsWith(",")) {
						int stop = arg.length() - 1;
						arg = arg.substring(0, stop);
						result.add(arg + "," + a);
					}
					int len = arg.length() - 1;
					if (len > 4) {
						if (a.toLowerCase().startsWith(arg.substring(arg.length() - 4).toLowerCase())) {
							int stop = arg.length() - 2;
							int stop2 = arg.length() - 4;
							arg = arg.substring(0, stop);
							result.add(arg.substring(0, stop2) + a);
						}
					}
					if (a.toLowerCase().startsWith(args[1].toLowerCase())) {
						result.add(a);
					}

				}
				return result;
			}
			if (args[0].equalsIgnoreCase("ally")) {
				arguments.clear();
				arguments.add("add");
				arguments.add("remove");
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("enemy")) {
				arguments.clear();
				arguments.add("add");
				arguments.add("remove");
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("bank")) {
				arguments.clear();
				arguments.addAll(bankFunctions);
				arguments.add("viewperms");
				for (String a : arguments) {
					if (a.startsWith(args[1].toLowerCase(Locale.ROOT)))
						result.add(a);
				}
				return result;
			}
			return result;
		}
		if (args.length == 3) {
			TabInsertEvent event = new TabInsertEvent(args);
			Bukkit.getPluginManager().callEvent(event);
			arguments.addAll(event.getArgs(3));

			for (String t : event.getArgs(3)) {
				if (t.toLowerCase().startsWith(args[2].toLowerCase()))
					result.add(t);
			}
			if (args[0].equalsIgnoreCase("ally")) {
				arguments.clear();
				arguments.addAll(Clan.clanUtil.getAllClanNames());
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[2].toLowerCase()))
						result.add(a);
				}
				return result;
			}

			if (args[0].equalsIgnoreCase("enemy")) {
				arguments.clear();
				arguments.addAll(Clan.clanUtil.getAllClanNames());
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[2].toLowerCase()))
						result.add(a);
				}
				return result;
			}

			if (args[0].equalsIgnoreCase("bank")) {
				if ("deposit".equalsIgnoreCase(args[1]) || "withdraw".equalsIgnoreCase(args[1])) {
					arguments.clear();
					arguments.add("10");
					for (String a : arguments) {
						if (a.startsWith(args[2]))
							result.add(a);
					}
					return result;
				} else if ("setperm".equalsIgnoreCase(args[1])) {
					arguments.clear();
					arguments.addAll(bankFunctions);
					for (String a : arguments) {
						if (a.startsWith(args[2].toLowerCase()))
							result.add(a);
					}
					return result;
				}
			}
			return result;
		}
		return super.tabComplete(sender, alias, args);
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
		SubCommandEvent event = new SubCommandEvent(p, args);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCommand()) {
			return event.isCommand();
		}

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
		if (HempfestClans.getInstance().dataManager.getAllowedWorlds().stream().noneMatch(w -> w.getName().equals(p.getWorld().getName()))) {
			lib.sendMessage(p, "&4&oClan features have been locked within this world.");
			return true;
		}
		if (length == 1) {
			String args0 = args[0];
			if (args0.equalsIgnoreCase("create")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan create <clanName> <password>");
				return true;
			}
			if (args0.equalsIgnoreCase("friendlyfire") || args0.equalsIgnoreCase("ff")) {
					if (Clan.clanUtil.getClan(p) != null) {
						if (Clan.clanUtil.getRankPower(p) >= Clan.clanUtil.friendfireClearance()) {
							Clan c = HempfestClans.clanManager(p);
							if (c.isFriendlyFire()) {
								c.messageClan(p.getName() + " &aturned friendly-fire off.");
								c.setFriendlyFire(false);
							} else {
								c.messageClan(p.getName() + " &4turned friendly-fire on.");
								c.setFriendlyFire(true);
								return true;
							}
						} else {
							lib.sendMessage(p, "&c&oYou don't have clan clearance.");
							return true;
						}
					} else {
						lib.sendMessage(p, lib.notInClan());
						return true;
					}
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
			if (args0.equalsIgnoreCase("request")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan request <playerName>");
				return true;
			}
			if (args0.equalsIgnoreCase("bank")) {
				if (BankPermission.USE.check(p)) {
					final Optional<Clan> optionalClan = testHasClan(p);
					if (!optionalClan.isPresent()) return true;
					final Clan clan = optionalClan.get();
					final String[] split = Message.GREETING.toString().split("\\{0}");
					final String greetingHover = Message.GREETING_HOVER.toString();
					if (BankPermission.USE_BALANCE.check(p)) {
						p.spigot().sendMessage(textLib.textRunnable(
								split[0], "&o" + p.getName(), split[1],
								Message.GREETING_HOVER.replace(clan.getClanTag()),
								"clan bank balance"
						));
					} else {
						p.spigot().sendMessage(textLib.textHoverable(
								split[0], "&o" + p.getName(), split[1],
								greetingHover.substring(0, greetingHover.indexOf("\n"))
										.replace("{0}", clan.getClanTag())
						));
					}
					sendMessage(p, Message.COMMAND_LISTING.toString());
					p.spigot().sendMessage(textLib.textSuggestable(Message.HELP_PREFIX + " ",
							"&7balance", Message.HOVER_BALANCE.toString(),
							"clan bank balance"
					));
					final TextComponent composed = textLib.textSuggestable(
							Message.HELP_PREFIX + " &f<",
							"&adeposit", Message.HOVER_DEPOSIT.toString(),
							"clan bank deposit 1"
					);
					composed.addExtra(textLib.textSuggestable(
							"&7,",
							"&cwithdraw", Message.HOVER_WITHDRAW.toString(),
							"clan bank withdraw 1"
					));
					composed.addExtra(new ColoredString("&f> <&7" + Message.AMOUNT + "&f>",
							ColoredString.ColorType.MC_COMPONENT).toComponent());
					p.spigot().sendMessage(composed);
					if (BankAction.VIEW_LOG.testForPlayer(clan, p)) {
						p.spigot().sendMessage(textLib.textSuggestable(
								Message.HELP_PREFIX + " ",
								"&7viewlog", Message.HOVER_VIEW_LOG.toString(),
								"clan bank viewlog"
						));
					}
					if (BankAction.SET_PERM.testForPlayer(clan, p)) {
						p.spigot().sendMessage(textLib.textSuggestable(
								Message.HELP_PREFIX + " ",
								"&7setperm", Message.HOVER_SET_PERM.toString(),
								"clan bank viewlog"
						));
					}
				} else {
					sendMessage(p, Message.NO_PERM_PLAYER_COMMAND.toString());
				}
				return true;
			}
			if (args0.equalsIgnoreCase("block")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan block <playerName>");
				return true;
			}
			if (args0.equalsIgnoreCase("top")) {
				if (!p.hasPermission(this.getPermission() + ".top")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".top" + '"');
					return true;
				}
				getUtil().getLeaderboard(p, 1);
				return true;
			}
			if (args0.equalsIgnoreCase("list")) {
				if (!p.hasPermission(this.getPermission() + ".roster")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".roster" + '"');
					return true;
				}
				lib.sendMessage(p, "&r- Clan roster. (&7/clan info clanName&r)");
				lib.paginatedClanList(p, getUtil().getAllClanNames(), "c list", 1, 10);
				return true;
			}
			if (args0.equalsIgnoreCase("claim")) {
				if (!p.hasPermission(this.getPermission() + ".claim")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".claim" + '"');
					return true;
				}
				if (Claim.claimUtil.claimingAllowed()) {
					if (getUtil().getClan(p) != null) {
						if (getUtil().getRankPower(p) >= getUtil().claimingClearance()) {
							getClaim().obtain(p);
							HempfestClans.getInstance().claimManager.refresh();
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
				if (!p.hasPermission(this.getPermission() + ".claim.remove")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".claim.remove" + '"');
					return true;
				}
				if (Claim.claimUtil.claimingAllowed()) {
					if (getUtil().getClan(p) != null) {
						if (getUtil().getRankPower(p) >= getUtil().claimingClearance()) {
							getClaim().remove(p);
							HempfestClans.getInstance().claimManager.refresh();
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
				if (!p.hasPermission(this.getPermission() + ".chat")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".chat" + '"');
					return true;
				}
				if (getUtil().getClan(p) != null) {
					switch (HempfestClans.chatMode.get(p)) {
						case "GLOBAL":
							HempfestClans.chatMode.put(p, "CLAN");
							lib.sendMessage(p, "&7&oSwitched to &3CLAN &7&ochat channel.");
							return true;
						case "CLAN":
							HempfestClans.chatMode.put(p, "ALLY");
							lib.sendMessage(p, "&7&oSwitched to &aALLY &7&ochat channel.");
							return true;
						default:
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
				if (!p.hasPermission(this.getPermission() + ".leave")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".roster" + '"');
					return true;
				}
				getUtil().leave(p);
				HempfestClans.chatMode.put(p, "GLOBAL");
				return true;
			}
			if (args0.equalsIgnoreCase("message")) {
				lib.sendMessage(p, "&7|&e) &fInvalid usage : /clan message <message>");
				return true;
			}
			if (args0.equalsIgnoreCase("base")) {
				if (!p.hasPermission(this.getPermission() + ".base")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".base" + '"');
					return true;
				}
				if (getUtil().getClan(p) != null) {
					if (HempfestClans.clanManager(p).getBase() != null) {
						getUtil().teleportBase(p);
						lib.sendMessage(p, "&e&oWelcome to the clan base.");
					} else {
						lib.sendMessage(p, "&c&oYour clan doesn't have a set base.");
						return true;
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("setbase")) {
				if (!p.hasPermission(this.getPermission() + ".base.set")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".base.set" + '"');
					return true;
				}
				Clan clan = HempfestClans.clanManager(p);
				if (getUtil().getRankPower(p) >= getUtil().baseClearance()) {
					ClanBaseUpdateEvent e = new ClanBaseUpdateEvent(p, p.getLocation());
					Bukkit.getPluginManager().callEvent(e);
					if (!e.isCancelled()) {
						clan.updateBase(p.getLocation());
					}
				} else {
					lib.sendMessage(p, "&c&oYou do not have clan clearance.");
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("info") || args0.equalsIgnoreCase("i")) {
				if (!p.hasPermission(this.getPermission() + ".info")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".info" + '"');
					return true;
				}
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
			if (args0.equalsIgnoreCase("bank")) {
				if (BankPermission.USE.check(p)) {
					final Optional<Clan> optionalClan = testHasClan(p);
					if (!optionalClan.isPresent()) return true;
					final Clan clan = optionalClan.get();
					final Optional<ClanBank> testBank = optionalClan.map(BankAPI.getInstance()::getBank);
					if (testBank.isPresent()) {
						if ("deposit".equalsIgnoreCase(args1)) {
							if (BankPermission.USE_DEPOSIT.check(p)) {
								// msg usage (need amount param)
								sendMessage(p, Message.USAGE.toString());
								p.spigot().sendMessage(textLib.textHoverable(
										Message.HELP_PREFIX + " ",
										"&7<&fdeposit&7>",
										" ",
										"&7<&c" + Message.AMOUNT + "&7>",
										Message.HOVER_DEPOSIT.toString(),
										Message.HOVER_NO_AMOUNT.toString()
								));
								return true;
							}
						} else if ("withdraw".equalsIgnoreCase(args1)) {
							if (BankPermission.USE_WITHDRAW.check(p)) {
								// msg usage (need amount param)
								sendMessage(p, Message.USAGE.toString());
								p.spigot().sendMessage(textLib.textHoverable(
										Message.HELP_PREFIX + " ",
										"&7<&fwithdraw&7>",
										" ",
										"&7<&c" + Message.AMOUNT + "&7>",
										Message.HOVER_WITHDRAW.toString(),
										Message.HOVER_NO_AMOUNT.toString()
								));
								return true;
							}
						} else if ("viewlog".equalsIgnoreCase(args1)) {
							if (BankAction.VIEW_LOG.testForPlayer(clan, p)) {
								// display log
								p.sendMessage(BankLog.getForClan(clan).getTransactions().stream().map(Object::toString).toArray(String[]::new));
								return true;
							}
						} else if ("viewperms".equalsIgnoreCase(args1)) {
							// display perms
							sendMessage(p, "&6Bank perm levels:");
							sendMessage(p, "Balance&e=&7[&f" + BankAction.BALANCE.getValueInClan(clan) + "&7]");
							sendMessage(p, "Deposit&e=&7[&f" + BankAction.DEPOSIT.getValueInClan(clan) + "&7]");
							sendMessage(p, "Withdraw&e=&7[&f" + BankAction.WITHDRAW.getValueInClan(clan) + "&7]");
							sendMessage(p, "ViewLog&e=&7[&f" + BankAction.VIEW_LOG.getValueInClan(clan) + "&7]");
							return true;
						} else {
							// msg usage (invalid subcommand)
							sendMessage(p, Message.INVALID_SUBCOMMAND.toString());
							return true;
						}
					}
				}
				sendMessage(p, Message.NO_PERM_PLAYER_COMMAND.toString());
				return true;
			}
			if (args0.equalsIgnoreCase("block")) {
				if (!p.hasPermission(this.getPermission() + ".block")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".block" + '"');
					return true;
				}
				Player target = Bukkit.getPlayer(args1);
				if (target != null) {
					if (blockedUsers.containsKey(p)) {
						List<UUID> a = blockedUsers.get(p);
						if (a.contains(target.getUniqueId())) {
							// already blocked
							a.remove(target.getUniqueId());
							blockedUsers.put(p, a);
							lib.sendMessage(p, target.getName() + " &a&ohas been unblocked.");
						} else {
							a.add(target.getUniqueId());
							blockedUsers.put(p, a);
							lib.sendMessage(p, target.getName() + " &c&ohas been blocked.");
							return true;
						}
					} else {
						// make it
						List<UUID> ids = new ArrayList<>();
						ids.add(target.getUniqueId());
						blockedUsers.put(p, ids);
						lib.sendMessage(p, target.getName() + " &c&ohas been blocked.");
						return true;
					}
				}
				return true;
			}
			if (args0.equalsIgnoreCase("request")) {
				if (!p.hasPermission(this.getPermission() + ".request")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".request" + '"');
					return true;
				}
				Player target = Bukkit.getPlayer(args1);
				if (target != null) {
					if (Clan.clanUtil.getClan(p) != null) {
						if (Clan.clanUtil.getClan(target) != null) {
							lib.sendMessage(p, "&c&oThis user is already in a clan.");
							return true;
						}
						if (Clan.clanUtil.getRankPower(p) >= Clan.clanUtil.invitationClearance()) {
							if (blockedUsers.containsKey(target)) {
								List<UUID> users = blockedUsers.get(target);
								if (users.contains(p.getUniqueId())) {
									lib.sendMessage(p, "&c&oThis person has you blocked. Unable to send invitation.");
									return true;
								}
							}
							HempfestClans.clanManager(p).messageClan(p.getName() + " &e&ohas invited player &6&l" + target.getName());
							lib.sendMessage(target, "&b&o" + p.getName() + " &3invites you to their clan.");
							if (Bukkit.getVersion().contains("1.16")) {
								TextComponent text = new TextComponent("§3|§7> §3Click a button to respond. ");
								TextComponent click = new TextComponent("§b[§nACCEPT§b]");
								TextComponent clickb = new TextComponent(" §7| ");
								TextComponent click2 = new TextComponent("§4[§nDENY§4]");
								click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new Text("§3Click to accept the request from '" + p.getName() + "'."))));
								click2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new Text("§3Click to deny the request from '" + p.getName() + "'."))));
								if (Clan.clanUtil.getClanPassword(Clan.clanUtil.getClan(p)) != null) {
									click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan join " + HempfestClans.clanManager(p).getClanTag() + " " + Clan.clanUtil.getClanPassword(Clan.clanUtil.getClan(p))));
								} else {
									click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan join " + HempfestClans.clanManager(p).getClanTag()));
								}
								click2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/msg " + p.getName() + " Nah im good, thank you though."));
								text.addExtra(click);
								text.addExtra(clickb);
								text.addExtra(click2);
								target.spigot().sendMessage(text);
							} else {
								TextComponent text = new TextComponent("§3|§7> §3Click a button to respond. ");
								TextComponent click = new TextComponent("§b[§nACCEPT§b]");
								TextComponent clickb = new TextComponent(" §7| ");
								TextComponent click2 = new TextComponent("§4[§nDENY§4]");
								click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§3Click to accept the request from '" + p.getName() + "'.")).create()));
								click2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§3Click to deny the request from '" + p.getName() + "'.")).create()));
								if (Clan.clanUtil.getClanPassword(Clan.clanUtil.getClan(p)) != null) {
									click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan join " + HempfestClans.clanManager(p).getClanTag() + " " + Clan.clanUtil.getClanPassword(Clan.clanUtil.getClan(p))));
								} else {
									click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan join " + HempfestClans.clanManager(p).getClanTag()));
								}
								click2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/msg " + p.getName() + " Nah im good, thank you though."));
								text.addExtra(click);
								text.addExtra(clickb);
								text.addExtra(click2);
								target.spigot().sendMessage(text);
							}
						} else {
							lib.sendMessage(p, "&c&oYou do not have clan clearance.");
							return true;
						}
					} else {
						lib.sendMessage(p, lib.notInClan());
						return true;
					}
				} else {
					lib.sendMessage(p, "&c&oTarget not found.");
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("create")) {
				if (!p.hasPermission(this.getPermission() + ".create")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".create" + '"');
					return true;
				}
				if (!HempfestClans.getInstance().dataManager.symbolsAllowed()) {
					if (!isAlphaNumeric(args1)) {
						lib.sendMessage(p, "&c&oInvalid clan name. Must contain only Alpha-numeric characters.");
						return true;
					}
				}
				if (Clan.clanUtil.getAllClanNames().contains(args1)) {
					lib.sendMessage(p, "&c&oA clan with this name already exists! Try another.");
					return true;
				}
				ClanCreateEvent e = new ClanCreateEvent(p, args1, null);
				Bukkit.getPluginManager().callEvent(e);
				if (!e.isCancelled()) {
					getUtil().create(p, args1, null);
				}
				return true;
			}
			if (args0.equalsIgnoreCase("nick") || args0.equalsIgnoreCase("nickname")) {
				if (!p.hasPermission(this.getPermission() + ".nick")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".nick" + '"');
					return true;
				}
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
				if (!p.hasPermission(this.getPermission() + ".top")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".top" + '"');
					return true;
				}
				try {
					getUtil().getLeaderboard(p, Integer.parseInt(args1));
				} catch (IllegalFormatException e) {
					lib.sendMessage(p, "&c&oInvalid page number!");
				}
				return true;
			}
			if (args0.equalsIgnoreCase("passowner")) {
				if (!p.hasPermission(this.getPermission() + ".passowner")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".passowner" + '"');
					return true;
				}
				if (getUtil().getClan(p) != null) {
					getUtil().transferOwner(p, args1);
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args0.equalsIgnoreCase("list")) {
				if (!p.hasPermission(this.getPermission() + ".roster")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".roster" + '"');
					return true;
				}
				try {
					lib.paginatedClanList(p, getUtil().getAllClanNames(), "c list", Integer.parseInt(args1), 10);
				} catch (NumberFormatException e) {
					lib.sendMessage(p, "&c&oInvalid page number!");
				}
				return true;
			}
			if (args0.equalsIgnoreCase("join")) {
				if (!p.hasPermission(this.getPermission() + ".join")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".join" + '"');
					return true;
				}
				getUtil().joinClan(p, args1, "none");
				return true;
			}
			if (args0.equalsIgnoreCase("tag")) {
				if (!p.hasPermission(this.getPermission() + ".tag")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".tag" + '"');
					return true;
				}
				if (getUtil().getClan(p) != null) {
					Clan clan = HempfestClans.clanManager(p);
					if (getUtil().getRankPower(p) >= getUtil().tagChangeClearance()) {
						if (!isAlphaNumeric(args1)) {
							lib.sendMessage(p, "&c&oInvalid clan name. Must contain only Alpha-numeric characters.");
							return true;
						}
						if (args1.length() > HempfestClans.getMain().getConfig().getInt("Formatting.tag-size")) {
							getUtil().sendMessage(p, "&c&oThe clan name you have chosen is too long! Max tag length reached.");
							return true;
						}
						clan.changeTag(args1);
						for (String s : clan.getMembers()) {
							Player target = Bukkit.getPlayer(s);
							if (target != null) {
								if (HempfestClans.getInstance().dataManager.prefixedTagsAllowed()) {
									Member.updatePrefix(target, "&7[" + Clan.clanUtil.getColor(clan.getChatColor()) + clan.getClanTag() + "&7] ");
								}
							}
						}
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
				if (!p.hasPermission(this.getPermission() + ".color")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".color" + '"');
					return true;
				}
				Clan clan = HempfestClans.clanManager(p);
				if (getUtil().getClan(p) != null) {
					if (getUtil().getRankPower(p) >= getUtil().colorChangeClearance()) {
						if (HempfestClans.getInstance().dataManager.symbolsAllowed()) {
							lib.sendMessage(p, "&c&oSymbols are allowed. Use '&' to color your clans tag.");
							return true;
						}
						clan.changeColor(args1.replaceAll("_", "").toLowerCase());
						for (String s : clan.getMembers()) {
							Player target = Bukkit.getPlayer(s);
							if (target != null) {
								if (HempfestClans.getInstance().dataManager.prefixedTagsAllowed()) {
									Member.updatePrefix(target, "&7[" + Clan.clanUtil.getColor(clan.getChatColor()) + clan.getClanTag() + "&7] ");
								}
							}
						}
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
				if (!p.hasPermission(this.getPermission() + ".promote")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".promote" + '"');
					return true;
				}
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
				if (!p.hasPermission(this.getPermission() + ".demote")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".demote" + '"');
					return true;
				}
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
						if (getUtil().getRankPower(target) == 0) {
							lib.sendMessage(p, "&c&oThis player is already the lowest rank possible.");
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
						if (!p.hasPermission(this.getPermission() + ".claim.removeall")) {
							lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".claim.removeall" + '"');
							return true;
						}
						if (getUtil().getClan(p) != null) {
							if (getUtil().getRankPower(p) >= getUtil().unclaimAllClearance()) {
								getClaim().removeAll(p);
								HempfestClans.getInstance().claimManager.refresh();
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
				if (!p.hasPermission(this.getPermission() + ".password")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".password" + '"');
					return true;
				}
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
				if (!p.hasPermission(this.getPermission() + ".kick")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".kick" + '"');
					return true;
				}
				if (getUtil().getClan(p) != null) {
					if (getUtil().getRankPower(p) >= getUtil().kickClearance()) {
						if (Clan.clanUtil.getUserID(args1) == null) {
							lib.sendMessage(p, "&c&oThis player doesn't exist.");
							return true;
						}
						OfflinePlayer target = Bukkit.getOfflinePlayer(Clan.clanUtil.getUserID(args1));
						Clan clan = HempfestClans.clanManager(p);
						if (!Arrays.asList(clan.getMembers()).contains(target.getName())) {
							lib.sendMessage(p, "&c&oThis player isn't a member of your clan.");
							return true;
						}
						if (getUtil().getRankPower(target) > getUtil().getRankPower(p)) {
							lib.sendMessage(p, "&c&oThis player has more power than you.");
							return true;
						}
						getUtil().kickPlayer(args1);
						String format = String.format(HempfestClans.getMain().getConfig().getString("Response.kick-out"), target.getName());
						String format1 = String.format(HempfestClans.getMain().getConfig().getString("Response.kick-in"), getUtil().getClanTag(getUtil().getClan(p)));
						clan.messageClan(format);
						if (target.isOnline()) {
							lib.sendMessage(target.getPlayer(), format1);
						}
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
				if (!p.hasPermission(this.getPermission() + ".info")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".info" + '"');
					return true;
				}
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
						lib.sendMessage(p, "&7#&fID &7of player " + '"' + target.getName() + '"' + " clan " + '"' + clanName + '"' + " is: &e&o" + getUtil().getClanID(clanName));
					}
				} else {
					lib.sendMessage(p, target.getName() + " &c&oisn't in a clan.");
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("message")) {
				if (!p.hasPermission(this.getPermission() + ".broadcast")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".broadcast" + '"');
					return true;
				}
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
				if (!p.hasPermission(this.getPermission() + ".create")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".create" + '"');
					return true;
				}
				if (!HempfestClans.getInstance().dataManager.symbolsAllowed()) {
					if (!isAlphaNumeric(args1)) {
						lib.sendMessage(p, "&c&oInvalid clan name. Must contain only Alpha-numeric characters.");
						return true;
					}
				}
				if (Clan.clanUtil.getAllClanNames().contains(args1)) {
					lib.sendMessage(p, "&c&oA clan with this name already exists! Try another.");
					return true;
				}
				ClanCreateEvent e = new ClanCreateEvent(p, args1, args2);
				Bukkit.getPluginManager().callEvent(e);
				if (!e.isCancelled()) {
					getUtil().create(p, args1, args2);
				}
				return true;
			}
			if (args0.equalsIgnoreCase("join")) {
				if (!p.hasPermission(this.getPermission() + ".join")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".join" + '"');
					return true;
				}
				getUtil().joinClan(p, args1, args2);
				return true;
			}
			if (args0.equalsIgnoreCase("message")) {
				if (!p.hasPermission(this.getPermission() + ".broadcast")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".broadcast" + '"');
					return true;
				}
				if (getUtil().getClan(p) != null) {
					Clan clan = HempfestClans.clanManager(p);
					clan.messageClan(p.getName() + " say's : " + args1 + " " + args2);
				}
				return true;
			}
			if (args0.equalsIgnoreCase("enemy")) {
				if (!p.hasPermission(this.getPermission() + ".enemy")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".enemy" + '"');
					return true;
				}
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
						List<String> online = new ArrayList<>();
						for (String mem : Clan.clanUtil.getClan(targetClan).getMembers()) {
							Player t = Bukkit.getPlayer(mem);
							if (t != null) {
								online.add(t.getName());
							}
						}
						if (online.isEmpty()) {
							lib.sendMessage(p, "&c&oThis clan has no members online, unable to mark as enemy.");
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
						List<String> online = new ArrayList<>();
						for (String mem : Clan.clanUtil.getClan(targetClan).getMembers()) {
							Player t = Bukkit.getPlayer(mem);
							if (t != null) {
								online.add(t.getName());
							}
						}
						if (online.isEmpty()) {
							lib.sendMessage(p, "&c&oThis clan has no members online, unable to mark as enemy.");
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
				if (!p.hasPermission(this.getPermission() + ".ally")) {
					lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + ".ally" + '"');
					return true;
				}
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
	// Resolves clan; sends message if none
	private Optional<Clan> testHasClan(Player sender) {
		final Optional<Clan> optionalClan = optionalClan(sender);
		if (!optionalClan.isPresent()) {
			sendMessage(sender, Message.PLAYER_NO_CLAN.toString());
			return Optional.empty();
		}
		return optionalClan;
	}
	// Attempts to resolve a player's clan
	private Optional<Clan> optionalClan(Player player) {
		return Optional.ofNullable(HempfestClans.getInstance().playerClan.get(player.getUniqueId()))
				.map(s -> HempfestClans.clanManager(player));
	}
}
