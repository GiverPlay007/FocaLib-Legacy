package br.com.devpaulo.legendchat.commands;

import br.com.devpaulo.legendchat.Legendchat;
import br.com.devpaulo.legendchat.api.LegendchatAPI;
import br.com.devpaulo.legendchat.channels.ChannelManager;
import br.com.devpaulo.legendchat.channels.types.Channel;
import br.com.devpaulo.legendchat.channels.types.PermanentChannel;
import br.com.devpaulo.legendchat.listeners.Listeners;
import br.com.devpaulo.legendchat.players.PlayerManager;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class LegendchatCommand implements CommandExecutor
{
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String l, String[] args)
  {
    if (args.length==0)
    {
      if (!PlayerManager.hasAnyPermission(sender))
      {
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
        return true;
      }

      sendHelp(sender);
    }
    else
    {
      if (args[0].equalsIgnoreCase("reload"))
      {
        if (!sender.hasPermission("legendchat.admin.reload") && !sender.hasPermission("legendchat.admin"))
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
          return true;
        }
        LegendchatAPI.load(false);
        Plugin lc = Bukkit.getPluginManager().getPlugin("Legendchat");
        lc.reloadConfig();
        LegendchatAPI.getCensorManager().loadCensoredWords(lc.getConfig().getStringList("censor.censored_words"));
        LegendchatAPI.getChannelManager().loadChannels();
  
        Legendchat.bungeeActive = false;
        if (lc.getConfig().getBoolean("bungeecord.use"))
        {
          if (LegendchatAPI.getChannelManager().existsChannel(lc.getConfig().getString("bungeecord.channel")))
          {
            Legendchat.bungeeActive = true;
          }
        }
        PlayerJoinEvent.getHandlerList().unregister(lc);
        PlayerQuitEvent.getHandlerList().unregister(lc);
        PlayerKickEvent.getHandlerList().unregister(lc);
        AsyncPlayerChatEvent.getHandlerList().unregister(lc);
        PlayerCommandPreprocessEvent.getHandlerList().unregister(lc);

        lc.getServer().getPluginManager().registerEvents(new Listeners(), lc);
        LegendchatAPI.load(true);
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message2"));
        return true;
      }
      if (args[0].equalsIgnoreCase("channel"))
      {
        if (!sender.hasPermission("legendchat.admin.channel") && !sender.hasPermission("legendchat.admin"))
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
          return true;
        }
        if (args.length < 3)
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("wrongcmd").replace("@command", "/lc channel <create/delete> <channel-name>"));
          return true;
        }
        if (args[1].equalsIgnoreCase("create"))
        {
          Channel c = LegendchatAPI.getChannelManager().getChannelByName(args[2].toLowerCase());
          if (c!=null)
          {
            sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error7"));
            return true;
          }
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message3").replace("@channel", args[2]));
          LegendchatAPI.getChannelManager().createPermanentChannel(new PermanentChannel(WordUtils.capitalizeFully(args[2]), Character.toString(args[2].charAt(0)).toLowerCase(), "{default}", "GRAY", true, false, 0, true, 0, 0, false));
        }
        else if (args[1].equalsIgnoreCase("delete"))
        {
          Channel c = LegendchatAPI.getChannelManager().getChannelByName(args[2].toLowerCase());
          if (c==null)
          {
            sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error4"));
            return true;
          }
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message4").replace("@channel", c.getName()));
          LegendchatAPI.getChannelManager().deleteChannel(c);
        }
        else
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("wrongcmd").replace("@command", "/lc channel <create/delete> <channel-name>"));
        }
        return true;
      }
      if (args[0].equalsIgnoreCase("playerch"))
      {
        if (!sender.hasPermission("legendchat.admin.playerch") && !sender.hasPermission("legendchat.admin"))
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
          return true;
        }
        if (args.length < 3)
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("wrongcmd").replace("@command", "/lc playerch <player> <channel-name>"));
          return true;
        }
        Player p = Bukkit.getPlayer(args[1]);
        if (p==null)
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error8"));
          return true;
        }
        Channel c = null;
        ChannelManager cm = LegendchatAPI.getChannelManager();
        c = cm.getChannelByName(args[2]);
        if (c==null)
        {
          c = cm.getChannelByNickname(args[2]);
        }
        if (c==null)
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error4"));
          return true;
        }
        LegendchatAPI.getPlayerManager().setPlayerFocusedChannel(p, c, false);
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message16").replace("@player", p.getName()).replace("@channel", c.getName()));
        p.sendMessage(LegendchatAPI.getMessageManager().getMessage("message17").replace("@player", sender.getName()).replace("@channel", c.getName()));
        return true;
      }
      else if (args[0].equalsIgnoreCase("spy"))
      {
        if (!sender.hasPermission("legendchat.admin.spy") && !sender.hasPermission("legendchat.admin"))
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
          return true;
        }
        if (sender==Bukkit.getConsoleSender())
        {
          return false;
        }
        Player player = (Player) sender;
        boolean spy = LegendchatAPI.getPlayerManager().isSpy(player);
        if (!spy)
        {
          LegendchatAPI.getPlayerManager().addSpy(player);
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message5"));
        }
        else
        {
          LegendchatAPI.getPlayerManager().removeSpy(player);
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message6"));
        }
        return true;
      }
      else if (args[0].equalsIgnoreCase("hide"))
      {
        if (!sender.hasPermission("legendchat.admin.hide") && !sender.hasPermission("legendchat.admin"))
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
          return true;
        }
        if (sender==Bukkit.getConsoleSender())
        {
          return false;
        }
        Player player = (Player) sender;
        boolean hidden = LegendchatAPI.getPlayerManager().isPlayerHiddenFromRecipients(player);
        if (!hidden)
        {
          LegendchatAPI.getPlayerManager().hidePlayerFromRecipients(player);
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message7"));
        }
        else
        {
          LegendchatAPI.getPlayerManager().showPlayerToRecipients(player);
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message8"));
        }
        return true;
      }
      else if (args[0].equalsIgnoreCase("mute"))
      {
        if (!sender.hasPermission("legendchat.admin.mute") && !sender.hasPermission("legendchat.admin"))
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
          return true;
        }
        if (args.length < 2)
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("wrongcmd").replace("@command", "/lc mute <player> [time {minutes}]"));
          return true;
        }
        Player p = Bukkit.getPlayer(args[1]);
        if (p==null)
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error8"));
          return true;
        }
        int time = 0;
        if (args.length > 2)
        {
          try
          {
            time = Integer.parseInt(args[2]);
          } catch (Exception e)
          {
            sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_error1"));
            return true;
          }
        }
        if (time < 0)
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_error1"));
          return true;
        }
        if (LegendchatAPI.getMuteManager().isPlayerMuted(p.getName()))
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_error2"));
          return true;
        }
        LegendchatAPI.getMuteManager().mutePlayer(p.getName(), time);
        if (time!=0)
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_msg3").replace("@player", p.getName()).replace("@time", Integer.toString(time)));
          p.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_msg4").replace("@player", sender.getName()).replace("@time", Integer.toString(time)));
        }
        else
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_msg1").replace("@player", p.getName()));
          p.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_msg2").replace("@player", sender.getName()));
        }
        return true;
      }
      else if (args[0].equalsIgnoreCase("unmute"))
      {
        if (!sender.hasPermission("legendchat.admin.unmute") && !sender.hasPermission("legendchat.admin"))
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
          return true;
        }
        if (args.length < 2)
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("wrongcmd").replace("@command", "/lc unmute <player>"));
          return true;
        }
        Player p = Bukkit.getPlayer(args[1]);
        if (p==null)
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error8"));
          return true;
        }
        if (!LegendchatAPI.getMuteManager().isPlayerMuted(p.getName()))
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_error3"));
          return true;
        }
        LegendchatAPI.getMuteManager().unmutePlayer(p.getName());
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_msg5").replace("@player", p.getName()));
        p.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_msg6").replace("@player", sender.getName()));
        return true;
      }
      else if (args[0].equalsIgnoreCase("muteall"))
      {
        if (!sender.hasPermission("legendchat.admin.muteall") && !sender.hasPermission("legendchat.admin"))
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
          return true;
        }
        if (LegendchatAPI.getMuteManager().isServerMuted())
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_error6"));
          return true;
        }
        LegendchatAPI.getMuteManager().muteServer();
        Bukkit.broadcastMessage(LegendchatAPI.getMessageManager().getMessage("mute_msg7").replace("@player", sender.getName()));
        return true;
      }
      else if (args[0].equalsIgnoreCase("unmuteall"))
      {
        if (!sender.hasPermission("legendchat.admin.muteall") && !sender.hasPermission("legendchat.admin"))
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
          return true;
        }
        if (!LegendchatAPI.getMuteManager().isServerMuted())
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_error7"));
          return true;
        }
        LegendchatAPI.getMuteManager().unmuteServer();
        Bukkit.broadcastMessage(LegendchatAPI.getMessageManager().getMessage("mute_msg8").replace("@player", sender.getName()));
        return true;
      }

      if (!PlayerManager.hasAnyPermission(sender))
      {
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
        return true;
      }

      sendHelp(sender);
    }
    return true;
  }

  private void sendHelp(CommandSender sender)
  {
    sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("listcmd1"));
    String msg2 = LegendchatAPI.getMessageManager().getMessage("listcmd2");

    if (sender.hasPermission("legendchat.admin.channel") || sender.hasPermission("legendchat.admin"))
    {
      sender.sendMessage(msg2.replace("@command", "/lc channel <create/delete> <channel>").replace("@description", "Channel manager"));
    }
    if (sender.hasPermission("legendchat.admin.playerch") || sender.hasPermission("legendchat.admin"))
    {
      sender.sendMessage(msg2.replace("@command", "/lc playerch <player> <channel>").replace("@description", "Change player channel"));
    }
    if (sender.hasPermission("legendchat.admin.spy") || sender.hasPermission("legendchat.admin"))
    {
      sender.sendMessage(msg2.replace("@command", "/lc spy").replace("@description", "Listen to all channels"));
    }
    if (sender.hasPermission("legendchat.admin.hide") || sender.hasPermission("legendchat.admin"))
    {
      sender.sendMessage(msg2.replace("@command", "/lc hide").replace("@description", "Hide from distance channels"));
    }
    if (sender.hasPermission("legendchat.admin.mute") || sender.hasPermission("legendchat.admin"))
    {
      sender.sendMessage(msg2.replace("@command", "/lc mute <player> [time {minutes}]").replace("@description", "Mute a player"));
    }
    if (sender.hasPermission("legendchat.admin.unmute") || sender.hasPermission("legendchat.admin"))
    {
      sender.sendMessage(msg2.replace("@command", "/lc unmute <player>").replace("@description", "Unmute a player"));
    }
    if (sender.hasPermission("legendchat.admin.muteall") || sender.hasPermission("legendchat.admin"))
    {
      sender.sendMessage(msg2.replace("@command", "/lc muteall").replace("@description", "Mute all players"));
    }
    if (sender.hasPermission("legendchat.admin.unmuteall") || sender.hasPermission("legendchat.admin"))
    {
      sender.sendMessage(msg2.replace("@command", "/lc unmuteall").replace("@description", "Unmute all players"));
    }
    if (sender.hasPermission("legendchat.admin.reload") || sender.hasPermission("legendchat.admin"))
    {
      sender.sendMessage(msg2.replace("@command", "/lc reload").replace("@description", "Configuration and channels reload"));
    }

    sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("listcmd3").replace("@version", LegendchatAPI.getPlugin().getDescription().getVersion()));
  }
}
