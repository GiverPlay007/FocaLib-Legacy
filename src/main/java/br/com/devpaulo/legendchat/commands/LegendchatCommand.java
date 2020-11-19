package br.com.devpaulo.legendchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LegendchatCommand implements CommandExecutor
{
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String l, String[] args)
  {
    if (args[0].equalsIgnoreCase("spy"))
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

    if (args[0].equalsIgnoreCase("hide"))
    {
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

    if (args[0].equalsIgnoreCase("mute"))
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
    return true;
  }
}
