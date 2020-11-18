package br.com.devpaulo.legendchat.commands;

import br.com.devpaulo.legendchat.api.LegendchatAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IgnoreCommand implements CommandExecutor
{
  @Override
  public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
  {
    if (sender==Bukkit.getConsoleSender())
    {
      return false;
    }
    if (args.length==0)
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("wrongcmd").replace("@command", "/ignore <player>"));
      return true;
    }
    Player p = Bukkit.getPlayer(args[0]);
    if (p==null)
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error8"));
      return true;
    }
    if (p==(Player) sender)
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error9"));
      return true;
    }
    if (LegendchatAPI.getIgnoreManager().hasPlayerIgnoredPlayer((Player) sender, p.getName()))
    {
      LegendchatAPI.getIgnoreManager().playerUnignorePlayer((Player) sender, p.getName());
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message15").replace("@player", p.getName()));
    }
    else
    {
      if (p.hasPermission("legendchat.block.ignore"))
      {
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error10"));
        return true;
      }
      LegendchatAPI.getIgnoreManager().playerIgnorePlayer((Player) sender, p.getName());
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message14").replace("@player", p.getName()));
    }
    return true;
  }
}
