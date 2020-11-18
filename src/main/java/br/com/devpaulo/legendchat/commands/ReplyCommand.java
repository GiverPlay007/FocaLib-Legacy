package br.com.devpaulo.legendchat.commands;

import br.com.devpaulo.legendchat.api.LegendchatAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReplyCommand implements CommandExecutor
{
  @Override
  public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
  {
    if (sender.hasPermission("legendchat.block.reply") && !sender.hasPermission("legendchat.admin"))
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error6"));
      return true;
    }
    if (args.length==0)
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("wrongcmd").replace("@command", "/r <" + LegendchatAPI.getMessageManager().getMessage("message") + ">"));
      return true;
    }
    if (!LegendchatAPI.getPrivateMessageManager().playerHasReply(sender))
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("pm_error1"));
      return true;
    }

    CommandSender sendto = LegendchatAPI.getPrivateMessageManager().getPlayerReply(sender);
    String msg = "";

    for (int i = 0; i < args.length; i++)
    {
      if (msg.length()==0)
      {
        msg = args[i];
      }
      else
      {
        msg += " " + args[i];
      }
    }
    LegendchatAPI.getPrivateMessageManager().replyPlayer(sender, msg);
    return true;
  }
}
