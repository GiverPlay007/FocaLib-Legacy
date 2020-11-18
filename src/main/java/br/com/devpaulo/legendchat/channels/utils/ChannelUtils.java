package br.com.devpaulo.legendchat.channels.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import br.com.devpaulo.legendchat.Legendchat;
import br.com.devpaulo.legendchat.api.LegendchatAPI;
import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import br.com.devpaulo.legendchat.channels.types.BungeecordChannel;
import br.com.devpaulo.legendchat.channels.types.Channel;
import br.com.devpaulo.legendchat.listeners.Listeners;

@SuppressWarnings("deprecation")
public class ChannelUtils
{
  public static void fakeMessage(final Channel c, final Player sender, final String message)
  {
    if (!LegendchatAPI.sendFakeMessageToChat())
    {
      c.sendMessage(sender, message, "", false);
      return;
    }

    HashSet<Player> p = new HashSet<Player>();
    p.add(sender);
    final AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender, "legendchat", p);
    Listeners.addFakeChat(event, false);
    Bukkit.getScheduler().runTaskAsynchronously(LegendchatAPI.getPlugin(), new Runnable()
    {
      public void run()
      {
        Bukkit.getPluginManager().callEvent(event);
        c.sendMessage(sender, message, event.getFormat(), Listeners.getFakeChat(event));
        Listeners.removeFakeChat(event);
      }
    });
  }

  public static void realMessage(Channel c, Player sender, String message, String bukkit_format, boolean cancelled)
  {
    if (!sender.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".chat") && !sender.hasPermission("legendchat.admin"))
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error2"));
      return;
    }
    if (sender.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".blockwrite") && !sender.hasPermission("legendchat.admin"))
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error2"));
      return;
    }

    if (c.isFocusNeeded())
    {
      if (LegendchatAPI.getPlayerManager().getPlayerFocusedChannel(sender)!=c)
      {
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error12"));
        return;
      }
    }
    int delay = LegendchatAPI.getDelayManager().getPlayerDelayFromChannel(sender.getName(), c);
    if (delay > 0)
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error11").replace("@time", Integer.toString(delay)));
      return;
    }
    if (LegendchatAPI.getMuteManager().isPlayerMuted(sender.getName()))
    {
      int time = LegendchatAPI.getMuteManager().getPlayerMuteTimeLeft(sender.getName());
      if (time==0)
      {
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_error4"));
      }
      else
      {
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_error5").replace("@time", Integer.toString(time)));
      }
      return;
    }
    if (LegendchatAPI.getMuteManager().isServerMuted())
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_error8"));
      return;
    }
    if (LegendchatAPI.getIgnoreManager().hasPlayerIgnoredChannel(sender, c))
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error14"));
      return;
    }

    Set<Player> recipients = new HashSet<Player>();

    for (Player p : Bukkit.getOnlinePlayers())
    {
      if (p.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".chat") || p.hasPermission("legendchat.admin"))
      {
        recipients.add(p);
      }
    }

    Set<Player> recipients2 = new HashSet<Player>();
    recipients2.addAll(recipients);

    for (Player p : recipients2)
    {
      if (c.getMaxDistance()!=0)
      {
        if (sender.getWorld()!=p.getWorld())
        {
          recipients.remove(p);
          continue;
        }
        else if (sender.getLocation().distance(p.getLocation()) > c.getMaxDistance())
        {
          recipients.remove(p);
          continue;
        }
      }
      else
      {
        if (!c.isCrossworlds())
        {
          if (sender.getWorld()!=p.getWorld())
          {
            recipients.remove(p);
            continue;
          }
        }
      }
      if (LegendchatAPI.getIgnoreManager().hasPlayerIgnoredPlayer(p, sender.getName()))
      {
        recipients.remove(p);
        continue;
      }
      if (LegendchatAPI.getIgnoreManager().hasPlayerIgnoredChannel(p, c))
      {
        recipients.remove(p);
        continue;
      }
      if (c.isFocusNeeded())
      {
        if (LegendchatAPI.getPlayerManager().getPlayerFocusedChannel(p)!=c)
        {
          recipients.remove(p);
        }
      }
    }

    boolean gastou = false;
    if (!Legendchat.block_econ && c.getMessageCost() > 0)
    {
      if (!sender.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".free") && !sender.hasPermission("legendchat.admin"))
      {
        if (Legendchat.econ.getBalance(sender.getName()) < c.getMessageCost())
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error3").replace("@price", Double.toString(c.getMessageCost())));
          return;
        }
        Legendchat.econ.withdrawPlayer(sender.getName(), c.getMessageCost());
        gastou = true;
      }
    }
    String n_format_p_p = "";
    String n_format_p = "";
    String n_format_s = "";
    if (bukkit_format.contains("<") && bukkit_format.contains(">"))
    {
      String name_code = null;
      if (bukkit_format.contains("%1$s"))
      {
        name_code = "%1$s";
      }
      else if (bukkit_format.contains("%s"))
      {
        name_code = "%s";
      }
      int seploc = bukkit_format.indexOf(name_code);
      int finalloc = -1;
      for (int i = seploc; i >= 0; i--)
        if (bukkit_format.charAt(i)=='<')
        {
          finalloc = i;
          break;
        }
      if (finalloc!=-1)
      {
        n_format_p_p = bukkit_format.substring(0, finalloc);
        if (name_code!=null)
        {
          String[] n_format = bukkit_format.substring(finalloc + 1).split(">")[0].split(name_code);
          if (n_format.length > 0)
          {
            n_format_p = n_format[0].replace(name_code, "").replace("{factions_relcolor}", "");
          }
          if (n_format.length > 1)
          {
            n_format_s = n_format[1];
          }
        }
      }
    }
    HashMap<String, String> tags = new HashMap<String, String>();
    tags.put("name", c.getName());
    tags.put("nick", c.getNickname());
    tags.put("color", c.getColor());
    tags.put("sender", sender.getDisplayName());
    tags.put("plainsender", sender.getName());
    tags.put("world", sender.getWorld().getName());
    tags.put("bprefix", (LegendchatAPI.forceRemoveDoubleSpacesFromBukkit() ? (n_format_p_p.equals(" ") ? "":n_format_p_p.replace("  ", " ")):n_format_p_p));
    tags.put("bprefix2", (LegendchatAPI.forceRemoveDoubleSpacesFromBukkit() ? (n_format_p.equals(" ") ? "":n_format_p.replace("  ", " ")):n_format_p));
    tags.put("bsuffix", (LegendchatAPI.forceRemoveDoubleSpacesFromBukkit() ? (n_format_s.equals(" ") ? "":n_format_s.replace("  ", " ")):n_format_s));
    tags.put("server", LegendchatAPI.getMessageManager().getMessage("bungeecord_server"));
    tags.put("time_hour", Integer.toString(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
    tags.put("time_min", Integer.toString(Calendar.getInstance().get(Calendar.MINUTE)));
    tags.put("time_sec", Integer.toString(Calendar.getInstance().get(Calendar.SECOND)));
    tags.put("date_day", Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
    tags.put("date_month", Integer.toString(Calendar.getInstance().get(Calendar.MONTH)));
    tags.put("date_year", Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
    if (!Legendchat.block_chat)
    {
      tags.put("prefix", tag(Legendchat.chat.getPlayerPrefix(sender)));
      tags.put("suffix", tag(Legendchat.chat.getPlayerSuffix(sender)));
      tags.put("groupprefix", tag(Legendchat.chat.getGroupPrefix(sender.getWorld(), Legendchat.chat.getPrimaryGroup(sender))));
      tags.put("groupsuffix", tag(Legendchat.chat.getGroupSuffix(sender.getWorld(), Legendchat.chat.getPrimaryGroup(sender))));
      for (String g : Legendchat.chat.getPlayerGroups(sender))
      {
        tags.put(g.toLowerCase() + "prefix", tag(Legendchat.chat.getGroupPrefix(sender.getWorld(), g)));
        tags.put(g.toLowerCase() + "suffix", tag(Legendchat.chat.getGroupSuffix(sender.getWorld(), g)));
      }
    }
    HashMap<String, String> ttt = LegendchatAPI.textToTag();
    if (ttt.size() > 0)
    {
      HashSet<Player> p = new HashSet<Player>();
      p.add(sender);
      int i = 1;
      for (String n : ttt.keySet())
      {
        String tag = "";
        try
        {
          tag = bukkit_format.split("°" + i + "º°")[1].split("°" + (i + 1) + "º°")[0];
        } catch (Exception e)
        {
          tag = "";
        }
        tags.put(n, tag);
        i++;
      }
    }
    ChatMessageEvent e = new ChatMessageEvent(c, sender, message, LegendchatAPI.format(c.getFormat()), c.getFormat(), bukkit_format, recipients, tags, cancelled);
    
    final boolean effectiveGastou = gastou;
    
    Bukkit.getScheduler().runTask(LegendchatAPI.getPlugin(), () -> {
      Bukkit.getPluginManager().callEvent(e);
      realMessage0(e, c, effectiveGastou);
    });
  }
  
  private static void realMessage0(ChatMessageEvent e, Channel c, boolean gastou)
  {
    if (e.isCancelled())
    {
      return;
    }
  
    Player sender = e.getSender();
    String message = e.getMessage();
    
    if (LegendchatAPI.isCensorActive())
    {
      message = LegendchatAPI.getCensorManager().censorFunction(message);
    }
    
    String completa = e.getFormat();
    
    if (LegendchatAPI.blockRepeatedTags())
    {
      if (e.getTags().contains("prefix") && e.getTags().contains("groupprefix"))
      {
        if (e.getTagValue("prefix").equals(e.getTagValue("groupprefix")))
        {
          e.setTagValue("prefix", "");
        }
      }
      if (e.getTags().contains("suffix") && e.getTags().contains("groupsuffix"))
      {
        if (e.getTagValue("suffix").equals(e.getTagValue("groupsuffix")))
        {
          e.setTagValue("suffix", "");
        }
      }
    }
    
    for (String n : e.getTags())
      completa = completa.replace("{" + n + "}", ChatColor.translateAlternateColorCodes('&', e.getTagValue(n)));
    completa = completa.replace("{msg}", translateAlternateChatColorsWithPermission(sender, message));
  
    for (Player p : e.getRecipients())
      p.sendMessage(completa);
  
    if (c.getDelayPerMessage() > 0 && !sender.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".nodelay") && !sender.hasPermission("legendchat.admin"))
    {
      LegendchatAPI.getDelayManager().addPlayerDelay(sender.getName(), c);
    }
  
    if (c.getMaxDistance()!=0)
    {
      if (LegendchatAPI.showNoOneHearsYou())
      {
        boolean show = false;
        if (e.getRecipients().size()==0)
        {
          show = true;
        }
        else if (e.getRecipients().size()==1 && e.getRecipients().contains(sender))
        {
          show = true;
        }
        else
        {
          show = true;
          for (Player p : e.getRecipients())
            if (p!=sender && !LegendchatAPI.getPlayerManager().isPlayerHiddenFromRecipients(p))
            {
              show = false;
              break;
            }
        }
        if (show)
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("special"));
        }
      }
    }
  
    for (Player p : LegendchatAPI.getPlayerManager().getOnlineSpys())
      if (!e.getRecipients().contains(p))
      {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', LegendchatAPI.getFormat("spy").replace("{msg}", ChatColor.stripColor(completa))));
      }
  
    if (gastou)
    {
      if (c.showCostMessage())
      {
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message9").replace("@money", Double.toString(c.getCostPerMessage())));
      }
    }
  
    if (LegendchatAPI.logToBukkit())
    {
      Bukkit.getConsoleSender().sendMessage(completa);
    }
  
    if (LegendchatAPI.logToFile())
    {
      LegendchatAPI.getLogManager().addLogToCache(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', completa)));
    }
  
    if (c instanceof BungeecordChannel)
    {
      if (LegendchatAPI.isBungeecordActive())
      {
        if (LegendchatAPI.getBungeecordChannel()==((BungeecordChannel) c))
        {
          ByteArrayOutputStream b = new ByteArrayOutputStream();
          DataOutputStream out = new DataOutputStream(b);
          try
          {
            HashMap<String, String> tags_packet = new HashMap<String, String>();
            for (String tag_packet : e.getTags())
              tags_packet.put(tag_packet, e.getTagValue(tag_packet));
            out.writeUTF(tags_packet.toString());
            out.writeUTF(translateAlternateChatColorsWithPermission(sender, message));
          } catch (IOException e1)
          {
            e1.printStackTrace();
          }
          sender.sendPluginMessage(Bukkit.getPluginManager().getPlugin("Legendchat"), "Legendchat", b.toByteArray());
        }
      }
    }
  }
  
  public static void otherMessage(Channel c, String message)
  {
    Set<Player> recipients = new HashSet<Player>();

    for (Player p : Bukkit.getOnlinePlayers())
    {
      if (p.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".chat") || p.hasPermission("legendchat.admin"))
      {
        recipients.add(p);
      }
    }

    Set<Player> recipients2 = new HashSet<Player>();
    recipients2.addAll(recipients);

    for (Player p : recipients2)
    {
      if (LegendchatAPI.getIgnoreManager().hasPlayerIgnoredChannel(p, c))
      {
        recipients.remove(p);
        continue;
      }
      if (c.isFocusNeeded())
      {
        if (LegendchatAPI.getPlayerManager().getPlayerFocusedChannel(p)!=c)
        {
          recipients.remove(p);
        }
      }
    }
		
		/*ChatMessageEvent e = new ChatMessageEvent(c,sender,message,Legendchat.format(c.getFormat()),c.getFormat(),recipients,tags,cancelled);
		Bukkit.getPluginManager().callEvent(e);
		if(e.isCancelled())
			return;
		sender = e.getSender();
		message = e.getMessage();
		
		for(Player p : e.getRecipients())
			p.sendMessage(completa);*/
    for (Player p : recipients)
      p.sendMessage(message);

    if (LegendchatAPI.logToBukkit())
    {
      Bukkit.getConsoleSender().sendMessage(message);
    }

    if (LegendchatAPI.logToFile())
    {
      LegendchatAPI.getLogManager().addLogToCache(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message)));
    }
  }

  public static String translateStringColor(String color)
  {
    switch (color.toLowerCase())
    {
      case "black":
      {
        return ChatColor.BLACK.toString();
      }
      case "darkblue":
      {
        return ChatColor.DARK_BLUE.toString();
      }
      case "darkgreen":
      {
        return ChatColor.DARK_GREEN.toString();
      }
      case "darkaqua":
      {
        return ChatColor.DARK_AQUA.toString();
      }
      case "darkred":
      {
        return ChatColor.DARK_RED.toString();
      }
      case "darkpurple":
      {
        return ChatColor.DARK_PURPLE.toString();
      }
      case "gold":
      {
        return ChatColor.GOLD.toString();
      }
      case "gray":
      {
        return ChatColor.GRAY.toString();
      }
      case "darkgray":
      {
        return ChatColor.DARK_GRAY.toString();
      }
      case "blue":
      {
        return ChatColor.BLUE.toString();
      }
      case "green":
      {
        return ChatColor.GREEN.toString();
      }
      case "aqua":
      {
        return ChatColor.AQUA.toString();
      }
      case "red":
      {
        return ChatColor.RED.toString();
      }
      case "lightpurple":
      {
        return ChatColor.LIGHT_PURPLE.toString();
      }
      case "yellow":
      {
        return ChatColor.YELLOW.toString();
      }
      default:
      {
        return ChatColor.WHITE.toString();
      }
    }
  }

  public static ChatColor translateStringColorToChatColor(String color)
  {
    switch (color.toLowerCase())
    {
      case "black":
      {
        return ChatColor.BLACK;
      }
      case "darkblue":
      {
        return ChatColor.DARK_BLUE;
      }
      case "darkgreen":
      {
        return ChatColor.DARK_GREEN;
      }
      case "darkaqua":
      {
        return ChatColor.DARK_AQUA;
      }
      case "darkred":
      {
        return ChatColor.DARK_RED;
      }
      case "darkpurple":
      {
        return ChatColor.DARK_PURPLE;
      }
      case "gold":
      {
        return ChatColor.GOLD;
      }
      case "gray":
      {
        return ChatColor.GRAY;
      }
      case "darkgray":
      {
        return ChatColor.DARK_GRAY;
      }
      case "blue":
      {
        return ChatColor.BLUE;
      }
      case "green":
      {
        return ChatColor.GREEN;
      }
      case "aqua":
      {
        return ChatColor.AQUA;
      }
      case "red":
      {
        return ChatColor.RED;
      }
      case "lightpurple":
      {
        return ChatColor.LIGHT_PURPLE;
      }
      case "yellow":
      {
        return ChatColor.YELLOW;
      }
      default:
      {
        return ChatColor.WHITE;
      }
    }
  }

  public static String translateChatColorToStringColor(ChatColor color)
  {
    switch (color)
    {
      case BLACK:
      {
        return "black";
      }
      case DARK_BLUE:
      {
        return "darkblue";
      }
      case DARK_GREEN:
      {
        return "darkgreen";
      }
      case DARK_AQUA:
      {
        return "darkaqua";
      }
      case DARK_RED:
      {
        return "darkred";
      }
      case DARK_PURPLE:
      {
        return "darkpurple";
      }
      case GOLD:
      {
        return "gold";
      }
      case GRAY:
      {
        return "gray";
      }
      case DARK_GRAY:
      {
        return "darkgray";
      }
      case BLUE:
      {
        return "blue";
      }
      case GREEN:
      {
        return "green";
      }
      case AQUA:
      {
        return "aqua";
      }
      case RED:
      {
        return "red";
      }
      case LIGHT_PURPLE:
      {
        return "lightpurple";
      }
      case YELLOW:
      {
        return "yellow";
      }
      default:
      {
        return "white";
      }
    }
  }

  private static String tag(String tag)
  {
    if (tag==null)
    {
      return "";
    }
    return tag;
  }

  public static String translateAlternateChatColorsWithPermission(Player p, String msg)
  {
    if (msg.contains("&0") && (p.hasPermission("legendchat.color.black") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&0", ChatColor.BLACK.toString());
    }
    if (msg.contains("&1") && (p.hasPermission("legendchat.color.darkblue") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&1", ChatColor.DARK_BLUE.toString());
    }
    if (msg.contains("&2") && (p.hasPermission("legendchat.color.darkgreen") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&2", ChatColor.DARK_GREEN.toString());
    }
    if (msg.contains("&3") && (p.hasPermission("legendchat.color.darkaqua") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&3", ChatColor.DARK_AQUA.toString());
    }
    if (msg.contains("&4") && (p.hasPermission("legendchat.color.darkred") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&4", ChatColor.DARK_RED.toString());
    }
    if (msg.contains("&5") && (p.hasPermission("legendchat.color.darkpurple") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&5", ChatColor.DARK_PURPLE.toString());
    }
    if (msg.contains("&6") && (p.hasPermission("legendchat.color.gold") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&6", ChatColor.GOLD.toString());
    }
    if (msg.contains("&7") && (p.hasPermission("legendchat.color.gray") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&7", ChatColor.GRAY.toString());
    }
    if (msg.contains("&8") && (p.hasPermission("legendchat.color.darkgray") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&8", ChatColor.DARK_GRAY.toString());
    }
    if (msg.contains("&9") && (p.hasPermission("legendchat.color.blue") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&9", ChatColor.BLUE.toString());
    }
    if (msg.contains("&a") && (p.hasPermission("legendchat.color.green") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&a", ChatColor.GREEN.toString());
    }
    if (msg.contains("&b") && (p.hasPermission("legendchat.color.aqua") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&b", ChatColor.AQUA.toString());
    }
    if (msg.contains("&c") && (p.hasPermission("legendchat.color.red") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&c", ChatColor.RED.toString());
    }
    if (msg.contains("&d") && (p.hasPermission("legendchat.color.lightpurple") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&d", ChatColor.LIGHT_PURPLE.toString());
    }
    if (msg.contains("&e") && (p.hasPermission("legendchat.color.yellow") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&e", ChatColor.YELLOW.toString());
    }
    if (msg.contains("&f") && (p.hasPermission("legendchat.color.white") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&f", ChatColor.WHITE.toString());
    }
    if (msg.contains("&k") && (p.hasPermission("legendchat.color.obfuscated") || p.hasPermission("legendchat.color.obfuscate") || p.hasPermission("legendchat.color.allformats") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&k", ChatColor.MAGIC.toString());
    }
    if (msg.contains("&l") && (p.hasPermission("legendchat.color.bold") || p.hasPermission("legendchat.color.allformats") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&l", ChatColor.BOLD.toString());
    }
    if (msg.contains("&m") && (p.hasPermission("legendchat.color.strikethrough") || p.hasPermission("legendchat.color.allformats") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&m", ChatColor.STRIKETHROUGH.toString());
    }
    if (msg.contains("&n") && (p.hasPermission("legendchat.color.underline") || p.hasPermission("legendchat.color.allformats") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&0n", ChatColor.UNDERLINE.toString());
    }
    if (msg.contains("&o") && (p.hasPermission("legendchat.color.italic") || p.hasPermission("legendchat.color.allformats") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&o", ChatColor.ITALIC.toString());
    }
    if (msg.contains("&r") && (p.hasPermission("legendchat.color.reset") || p.hasPermission("legendchat.color.allformats") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&r", ChatColor.RESET.toString());
    }
    if (msg.contains("&A") && (p.hasPermission("legendchat.color.green") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&A", ChatColor.GREEN.toString());
    }
    if (msg.contains("&B") && (p.hasPermission("legendchat.color.aqua") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&B", ChatColor.AQUA.toString());
    }
    if (msg.contains("&C") && (p.hasPermission("legendchat.color.red") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&C", ChatColor.RED.toString());
    }
    if (msg.contains("&D") && (p.hasPermission("legendchat.color.lightpurple") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&D", ChatColor.LIGHT_PURPLE.toString());
    }
    if (msg.contains("&E") && (p.hasPermission("legendchat.color.yellow") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&E", ChatColor.YELLOW.toString());
    }
    if (msg.contains("&F") && (p.hasPermission("legendchat.color.white") || p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&F", ChatColor.WHITE.toString());
    }
    if (msg.contains("&K") && (p.hasPermission("legendchat.color.obfuscated") || p.hasPermission("legendchat.color.obfuscate") || p.hasPermission("legendchat.color.allformats") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&K", ChatColor.MAGIC.toString());
    }
    if (msg.contains("&L") && (p.hasPermission("legendchat.color.bold") || p.hasPermission("legendchat.color.allformats") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&L", ChatColor.BOLD.toString());
    }
    if (msg.contains("&M") && (p.hasPermission("legendchat.color.strikethrough") || p.hasPermission("legendchat.color.allformats") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&M", ChatColor.STRIKETHROUGH.toString());
    }
    if (msg.contains("&N") && (p.hasPermission("legendchat.color.underline") || p.hasPermission("legendchat.color.allformats") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&N", ChatColor.UNDERLINE.toString());
    }
    if (msg.contains("&O") && (p.hasPermission("legendchat.color.italic") || p.hasPermission("legendchat.color.allformats") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&O", ChatColor.ITALIC.toString());
    }
    if (msg.contains("&R") && (p.hasPermission("legendchat.color.reset") || p.hasPermission("legendchat.color.allformats") || p.hasPermission("legendchat.admin")))
    {
      msg = msg.replace("&R", ChatColor.RESET.toString());
    }
    return msg;
  }
}
