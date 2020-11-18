package br.com.devpaulo.legendchat.commands;

import br.com.devpaulo.legendchat.api.LegendchatAPI;
import br.com.devpaulo.legendchat.channels.types.Channel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuteCommand implements CommandExecutor
{
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args)
  {
    if (sender==Bukkit.getConsoleSender())
    {
      return false;
    }
    if (args.length==0)
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("wrongcmd").replace("@command", "/mute <" + LegendchatAPI.getMessageManager().getMessage("channel") + ">"));
      if (LegendchatAPI.getIgnoreManager().playerHasIgnoredChannelsList((Player) sender))
      {
        String mlist = "";
        for (Channel c : LegendchatAPI.getIgnoreManager().getPlayerIgnoredChannelsList((Player) sender))
        {
          if (mlist.length()==0)
          {
            mlist = c.getName();
          }
          else
          {
            mlist += ", " + c.getName();
          }
        }
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message20").replace("@channels", (mlist.length()==0 ? "...":mlist)));
      }
      return true;
    }
    Channel c = LegendchatAPI.getChannelManager().getChannelByNameOrNickname(args[0]);
    if (c==null)
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error4"));
      return true;
    }
    if (LegendchatAPI.getIgnoreManager().hasPlayerIgnoredChannel((Player) sender, c))
    {
      LegendchatAPI.getIgnoreManager().playerUnignoreChannel((Player) sender, c);
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message19").replace("@channel", c.getName()));
    }
    else
    {
      if (sender.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".blockmute") && !sender.hasPermission("legendchat.admin"))
      {
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error13"));
        return true;
      }
      if (!c.getPlayersWhoCanSeeChannel().contains((Player) sender))
      {
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error4"));
        return true;
      }
      LegendchatAPI.getIgnoreManager().playerIgnoreChannel((Player) sender, c);
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message18").replace("@channel", c.getName()));
    }

    return true;
  }
}
