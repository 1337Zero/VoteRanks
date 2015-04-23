package de.cultcraft.zero.utils;

import org.bukkit.ChatColor;

public class ChatManager
{
  public String ReplaceIt(String Message, String replacethis, String replacewith)
  {
    return Message.replace(replacethis, replacewith);
  }

  public static String ColorIt(String Message)
  {
    while (((Message.contains("&0") | Message.contains("&1") | Message.contains("&2") | Message.contains("&3") | Message.contains("&4") | Message.contains("&5") | Message.contains("&6") | Message.contains("&7"))) || 
      (Message.contains("&8")) || (Message.contains("&9")) || (Message.contains("&a")) || (Message.contains("&b")) || (Message.contains("&c")) || (Message.contains("&d")) || (Message.contains("&e")) || (Message.contains("&f")) || 
      (Message.contains("&g")) || (Message.contains("&h")) || (Message.contains("&i")) || (Message.contains("&j")) || (Message.contains("&k")) || (Message.contains("&r")) || (Message.contains("[RESET]")) || (Message.contains("[BOLD]")) || 
      (Message.contains("[MAGIC]")) || (Message.contains("[ITALIC]")) || (Message.contains("[UNDERLINE]")) || (Message.contains("[STRIKETHROUGH]")) || (Message.contains("[WHITE]")) || (Message.contains("[YELLOW]")) || (Message.contains("[LIGHT_PURPLE]")) || 
      (Message.contains("[RED]")) || (Message.contains("[AQUA]")) || (Message.contains("[GREEN]")) || (Message.contains("[BLUE]")) || (Message.contains("[DARK_GRAY]")) || (Message.contains("[GRAY]")) || (Message.contains("[GOLD]")) || 
      (Message.contains("[DARK_PURPLE]")) || (Message.contains("[DARK_RED]")) || (Message.contains("[DARK_AQUA]")) || (Message.contains("[DARK_GREEN]")) || (Message.contains("[DARK_BLUE]")) || (Message.contains("[BLACK]")))
    {
      if ((Message.contains("&r")) || (Message.contains("[RESET]")))
      {
        Message = Message.replace("&r", "" + ChatColor.RESET).replace("[RESET]", "" + ChatColor.RESET);
      }
      else if ((Message.contains("&g")) || (Message.contains("[MAGIC]")))
      {
        Message = Message.replace("&g", "" + ChatColor.MAGIC).replace("[MAGIC]", "" + ChatColor.MAGIC);
      }
      else if ((Message.contains("&h")) || (Message.contains("[BOLD]")))
      {
        Message = Message.replace("&h", "" + ChatColor.BOLD).replace("[BOLD]", "" + ChatColor.BOLD);
      }
      else if ((Message.contains("&i")) || (Message.contains("[ITALIC]")))
      {
        Message = Message.replace("&i", "" + ChatColor.ITALIC).replace("[ITALIC]", "" + ChatColor.ITALIC);
      }
      else if ((Message.contains("&j")) || (Message.contains("[UNDERLINE]")))
      {
        Message = Message.replace("&j", "" + ChatColor.UNDERLINE).replace("[UNDERLINE]","" +  ChatColor.UNDERLINE);
      }
      else if ((Message.contains("&k")) || (Message.contains("[STRIKETROUGH]")))
      {
        Message = Message.replace("&k","" +  ChatColor.STRIKETHROUGH).replace("[STRIKETHROUGH]", "" + ChatColor.STRIKETHROUGH);
      }
      else if ((Message.contains("&f")) || (Message.contains("[WHITE]")))
      {
        Message = Message.replace("&f", "" + ChatColor.WHITE).replace("[WHITE]", "" + ChatColor.WHITE);
      }
      else if ((Message.contains("&e")) || (Message.contains("[YELLOW]")))
      {
        Message = Message.replace("&e","" +  ChatColor.YELLOW).replace("[YELLOW]", "" + ChatColor.YELLOW);
      }
      else if ((Message.contains("&d")) || (Message.contains("[LIGHT_PURPLE]")))
      {
        Message = Message.replace("&d", "" + ChatColor.LIGHT_PURPLE).replace("[LIHT_PURPLE]","" +  ChatColor.LIGHT_PURPLE);
      }
      else if ((Message.contains("&c")) || (Message.contains("[RED]")))
      {
        Message = Message.replace("&c","" +  ChatColor.RED).replace("[RED]", "" + ChatColor.RED);
      }
      else if ((Message.contains("&b")) || (Message.contains("[AQUA]")))
      {
        Message = Message.replace("&b", "" + ChatColor.AQUA).replace("[AQUA]", "" + ChatColor.AQUA);
      }
      else if ((Message.contains("&a")) || (Message.contains("[GREEN]")))
      {
        Message = Message.replace("&a", "" + ChatColor.GREEN).replace("[GREEN]", "" + ChatColor.GREEN);
      }
      else if ((Message.contains("&9")) || (Message.contains("[BLUE]")))
      {
        Message = Message.replace("&9", "" + ChatColor.BLUE).replace("[BLUE]","" +  ChatColor.BLUE);
      }
      else if ((Message.contains("&8")) || (Message.contains("[DARK_GRAY]")))
      {
        Message = Message.replace("&8", "" + ChatColor.DARK_GRAY).replace("[DARK_GRAY]","" +  ChatColor.DARK_GRAY);
      }
      else if ((Message.contains("&7")) || (Message.contains("[GRAY]")))
      {
        Message = Message.replace("&7", "" + ChatColor.GRAY).replace("[GRAY]","" +  ChatColor.GRAY);
      }
      else if ((Message.contains("&6")) || (Message.contains("[GOLD]")))
      {
        Message = Message.replace("&6", "" + ChatColor.GOLD).replace("[GOLD]", "" + ChatColor.GOLD);
      }
      else if ((Message.contains("&5")) || (Message.contains("[DARK_PURPLE]")))
      {
        Message = Message.replace("&5", "" + ChatColor.DARK_PURPLE).replace("[DARK_PURPLE]","" +  ChatColor.DARK_PURPLE);
      }
      else if ((Message.contains("&4")) || (Message.contains("[DARK_RED]")))
      {
        Message = Message.replace("&4", "" + ChatColor.DARK_RED).replace("[DARK_RED]", "" + ChatColor.DARK_RED);
      }
      else if ((Message.contains("&3")) || (Message.contains("[DARK_AQUA]")))
      {
        Message = Message.replace("&3","" +  ChatColor.DARK_AQUA).replace("[DARK_AQUA]","" +  ChatColor.DARK_AQUA);
      }
      else if ((Message.contains("&2")) || (Message.contains("[DARK_GREEN]")))
      {
        Message = Message.replace("&2", "" + ChatColor.DARK_GREEN).replace("[DARK_GREEN]", "" + ChatColor.DARK_GREEN);
      }
      else if ((Message.contains("&1")) || (Message.contains("[DARK_BLUE]")))
      {
        Message = Message.replace("&1", "" + ChatColor.DARK_BLUE).replace("[DARK_BLUE]","" +  ChatColor.DARK_BLUE);
      }
      else if ((Message.contains("&0")) || (Message.contains("[BLACK]")))
      {
        Message = Message.replace("&0", "" + ChatColor.BLACK).replace("[BLACK]", "" + ChatColor.BLACK);
      }
    }

    return Message;
  }

  public String ColorIt1(String Message) {
    while ((Message.contains("[RESET]")) || (Message.contains("[BOLD]")) || 
      (Message.contains("[MAGIC]")) || (Message.contains("[ITALIC]")) || (Message.contains("[UNDERLINE]")) || (Message.contains("[STRIKETHROUGH]")) || (Message.contains("[WHITE]")) || (Message.contains("[YELLOW]")) || (Message.contains("[LIGHT_PURPLE]")) || 
      (Message.contains("[RED]")) || (Message.contains("[AQUA]")) || (Message.contains("[GREEN]")) || (Message.contains("[BLUE]")) || (Message.contains("[DARK_GRAY]")) || (Message.contains("[GRAY]")) || (Message.contains("[GOLD]")) || 
      (Message.contains("[DARK_PURPLE]")) || (Message.contains("[DARK_RED]")) || (Message.contains("[DARK_AQUA]")) || (Message.contains("[DARK_GREEN]")) || (Message.contains("[DARK_BLUE]")) || (Message.contains("[BLACK]")))
    {
      if (Message.contains("[RESET]"))
      {
        Message = Message.replace("[RESET]","" +  ChatColor.RESET);
      }
      else if (Message.contains("[MAGIC]"))
      {
        Message = Message.replace("[MAGIC]", "" + ChatColor.MAGIC);
      }
      else if (Message.contains("[BOLD]"))
      {
        Message = Message.replace("[BOLD]", "" + ChatColor.BOLD);
      }
      else if (Message.contains("[ITALIC]"))
      {
        Message = Message.replace("[ITALIC]", "" + ChatColor.ITALIC);
      }
      else if (Message.contains("[UNDERLINE]"))
      {
        Message = Message.replace("[UNDERLINE]","" +  ChatColor.UNDERLINE);
      }
      else if (Message.contains("[STRIKETROUGH]"))
      {
        Message = Message.replace("[STRIKETHROUGH]", "" + ChatColor.STRIKETHROUGH);
      }
      else if (Message.contains("[WHITE]"))
      {
        Message = Message.replace("[WHITE]", "" + ChatColor.WHITE);
      }
      else if (Message.contains("[YELLOW]"))
      {
        Message = Message.replace("[YELLOW]", "" + ChatColor.YELLOW);
      }
      else if (Message.contains("[LIGHT_PURPLE]"))
      {
        Message = Message.replace("[LIHT_PURPLE]", "" + ChatColor.LIGHT_PURPLE);
      }
      else if (Message.contains("[RED]"))
      {
        Message = Message.replace("[RED]","" +  ChatColor.RED);
      }
      else if (Message.contains("[AQUA]"))
      {
        Message = Message.replace("[AQUA]", "" + ChatColor.AQUA);
      }
      else if (Message.contains("[GREEN]"))
      {
        Message = Message.replace("[GREEN]", "" + ChatColor.GREEN);
      }
      else if (Message.contains("[BLUE]"))
      {
        Message = Message.replace("[BLUE]","" +  ChatColor.BLUE);
      }
      else if (Message.contains("[DARK_GRAY]"))
      {
        Message = Message.replace("[DARK_GRAY]", "" + ChatColor.DARK_GRAY);
      }
      else if (Message.contains("[GRAY]"))
      {
        Message = Message.replace("[GRAY]","" +  ChatColor.GRAY);
      }
      else if (Message.contains("[GOLD]"))
      {
        Message = Message.replace("[GOLD]","" +  ChatColor.GOLD);
      }
      else if (Message.contains("[DARK_PURPLE]"))
      {
        Message = Message.replace("[DARK_PURPLE]", "" + ChatColor.DARK_PURPLE);
      }
      else if (Message.contains("[DARK_RED]"))
      {
        Message = Message.replace("[DARK_RED]", "" + ChatColor.DARK_RED);
      }
      else if (Message.contains("[DARK_AQUA]"))
      {
        Message = Message.replace("[DARK_AQUA]", "" + ChatColor.DARK_AQUA);
      }
      else if (Message.contains("[DARK_GREEN]"))
      {
        Message = Message.replace("[DARK_GREEN]", "" + ChatColor.DARK_GREEN);
      }
      else if (Message.contains("[DARK_BLUE]"))
      {
        Message = Message.replace("[DARK_BLUE]", "" + ChatColor.DARK_BLUE);
      }
      else if (Message.contains("[BLACK]"))
      {
        Message = Message.replace("[BLACK]", "" + ChatColor.BLACK);
      }
    }

    return Message;
  }
  public String encode(String msg) {
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