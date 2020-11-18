package br.com.devpaulo.legendchat.commands;

import br.com.devpaulo.legendchat.api.LegendchatAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TellCommand implements CommandExecutor
{
  private CommandSender console = Bukkit.getConsoleSender();

  @Override
  public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
  {
    if (sender.hasPermission("legendchat.block.tell") && !sender.hasPermission("legendchat.admin"))
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
      return true;
    }
    if (args.length==0)
    {
      if (LegendchatAPI.getPrivateMessageManager().isPlayerTellLocked(sender))
      {
        LegendchatAPI.getPrivateMessageManager().unlockPlayerTell(sender);
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message11"));
      }
      else
      {
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("wrongcmd").replace("@command", "/tell <player> [" + LegendchatAPI.getMessageManager().getMessage("message") + "]"));
      }
      return true;
    }
    CommandSender to = Bukkit.getPlayer(args[0]);
    if (to==null)
    {
      if (args[0].equalsIgnoreCase("console"))
      {
        to = console;
      }
      else
      {
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error8"));
        return true;
      }
    }
    if (to==sender)
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error9"));
      return true;
    }
    if (args.length==1)
    {
      if (sender==console)
      {
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("wrongcmd").replace("@command", "/tell <player> [" + LegendchatAPI.getMessageManager().getMessage("message") + "]"));
        return true;
      }
      if (LegendchatAPI.getPrivateMessageManager().isPlayerTellLocked(sender) && LegendchatAPI.getPrivateMessageManager().getPlayerLockedTellWith(sender)==to)
      {
        LegendchatAPI.getPrivateMessageManager().unlockPlayerTell(sender);
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message11"));
      }
      else
      {
        if (sender.hasPermission("legendchat.block.locktell") && !sender.hasPermission("legendchat.admin"))
        {
          sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
          return true;
        }

        LegendchatAPI.getPrivateMessageManager().lockPlayerTell(sender, to);
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message10").replace("@player", to.getName()));
      }
    }
    else
    {
      StringBuilder msg = new StringBuilder();

      for (int i = 1; i < args.length; i++)
      {
        if (msg.length()==0)
        {
          msg = new StringBuilder(args[i]);
        }
        else
        {
          msg.append(" ").append(args[i]);
        }
      }

      LegendchatAPI.getPrivateMessageManager().tellPlayer(sender, to, msg.toString().trim());
    }
    return true;
  }
}
