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
    if (args[0].equalsIgnoreCase("unmute"))
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
    if (args[0].equalsIgnoreCase("muteall"))
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
    if (args[0].equalsIgnoreCase("unmuteall"))
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
