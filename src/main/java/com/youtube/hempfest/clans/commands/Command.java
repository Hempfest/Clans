package com.youtube.hempfest.clans.commands;

import com.youtube.hempfest.clans.HempfestClans;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public class Command {


    public void registerCommand(BukkitCommand command) {
        try {

            final Field commandMapField = HempfestClans.getInstance().getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            final CommandMap commandMap = (CommandMap) commandMapField.get(HempfestClans.getInstance().getServer());
            commandMap.register(command.getLabel(), command);

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void registerAll() {
        ArrayList<String> aliases = new ArrayList<String>();
        aliases.addAll(Arrays.asList("cl", "c"));
        registerCommand(new CommandClan("clan", "Base command for clan actions", "clans.use", "/clan <subCommand>", aliases));
        ArrayList<String> aliases2 = new ArrayList<String>();
        aliases2.addAll(Arrays.asList("ca", "cla"));
        registerCommand(new CommandClanAdmin("clanadmin", "Base command for clan admin actions", "clans.admin.use", "/clanadmin <subCommand>", aliases2));
    }

}
