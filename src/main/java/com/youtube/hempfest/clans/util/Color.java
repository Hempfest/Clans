package com.youtube.hempfest.clans.util;

public enum Color {

    BLACK("&0"),
    DARK_BLUE("&1"),
    DARK_GREEN("&2"),
    DARK_AQUA("&3"),
    DARK_RED("&4"),
    DARK_PURPLE("&5"),
    GOLD("&6"),
    GRAY("&7"),
    DARK_GRAY("&8"),
    BLUE("&9"),
    GREEN("&a"),
    AQUA("&b"),
    RED("&c"),
    LIGHT_PURPLE("&d"),
    YELLOW("&e"),
    WHITE("&f"),
    OBFUSCATED("&k"),
    BOLD("&l"),
    STRIKE_THROUGH("&m"),
    UNDERLINE("&n"),
    ITALIC("&o"),
    RESET("&r");

    String value;

    Color(String value) {
        this.value = value;
    }

    public String toCode() {
        String result = "";
        switch (this) {
            default:
                result = value;
                break;
        }
        return result;
    }


}
