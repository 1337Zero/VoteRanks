package de.cultcraft.zero.utils;

import org.bukkit.ChatColor;

public class ChatManager {
	
	private ChatManager() {
		
	}
	
	public String ReplaceIt(String message, String replacethis, String replacewith) {
		return message.replace(replacethis, replacewith);
	}

	public static String ColorIt(String message) {		
		return ChatColor.translateAlternateColorCodes('&', normalize(message));
	}

	public static String ColorIt1(String message) {	
		return ChatColor.translateAlternateColorCodes('&', normalize(message));
	}

	/**
	 * Replace [Color] in &x Color codes
	 * @param message
	 * @return
	 */
	public static String normalize(String message) {
		message = message.replace("[RESET]", "&r");
		message = message.replace("[MAGIC]", "&k");
		message = message.replace("[BOLD]", "&l");
		message = message.replace("[ITALIC]", "&o");
		message = message.replace("[UNDERLINE]", "&n");
		message = message.replace("[STRIKETHROUGH]", "&m");
		message = message.replace("[WHITE]", "&f");
		message = message.replace("[YELLOW]", "&e");
		message = message.replace("[LIHT_PURPLE]", "&d");
		message = message.replace("[RED]", "&c");
		message = message.replace("[AQUA]", "&4");
		message = message.replace("[GREEN]", "&a");
		message = message.replace("[BLUE]", "&9");
		message = message.replace("[DARK_GRAY]", "&8");
		message = message.replace("[GRAY]", "&7");
		message = message.replace("[GOLD]", "&6");
		message = message.replace("[DARK_PURPLE]", "&5");
		message = message.replace("[DARK_RED]", "&4");
		message = message.replace("[DARK_AQUA]", "&b");
		message = message.replace("[DARK_GREEN]", "&2");
		message = message.replace("[DARK_BLUE]", "&1");
		message = message.replace("[BLACK]", "&0");
		return message;
	}
	
	public static String encode(String msg) {
		msg = msg.replace("" + ChatColor.BLACK, "&0");
		msg = msg.replace("" + ChatColor.DARK_BLUE, "&1");
		msg = msg.replace("" + ChatColor.DARK_GREEN, "&2");
		msg = msg.replace("" + ChatColor.DARK_AQUA, "&3");
		msg = msg.replace("" + ChatColor.DARK_RED, "&4");
		msg = msg.replace("" + ChatColor.DARK_PURPLE, "&5");
		msg = msg.replace("" + ChatColor.GOLD, "&6");
		msg = msg.replace("" + ChatColor.GRAY, "&7");
		msg = msg.replace("" + ChatColor.DARK_GRAY, "&8");
		msg = msg.replace("" + ChatColor.BLUE, "&9");
		msg = msg.replace("" + ChatColor.GREEN, "&a");
		msg = msg.replace("" + ChatColor.AQUA, "&b");
		msg = msg.replace("" + ChatColor.RED, "&c");
		msg = msg.replace("" + ChatColor.LIGHT_PURPLE, "&d");
		msg = msg.replace("" + ChatColor.YELLOW, "&e");
		msg = msg.replace("" + ChatColor.WHITE, "&f");
		msg = msg.replace("" + ChatColor.MAGIC, "&g");
		msg = msg.replace("" + ChatColor.BOLD, "&h");
		msg = msg.replace("" + ChatColor.ITALIC, "&i");
		msg = msg.replace("" + ChatColor.UNDERLINE, "&j");
		msg = msg.replace("" + ChatColor.STRIKETHROUGH, "&k");
		return msg;
	}
}