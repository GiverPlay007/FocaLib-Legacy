package br.com.devpaulo.legendchat.commands;

import br.com.devpaulo.legendchat.api.LegendchatAPI;
import br.com.devpaulo.legendchat.channels.ChannelManager;
import br.com.devpaulo.legendchat.channels.types.Channel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChannelCommand implements CommandExecutor
{
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
  {
    if (sender==Bukkit.getConsoleSender())
    {
      return false;
    }
    if (args.length==0)
    {
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("wrongcmd").replace("@command", "/ch <" + LegendchatAPI.getMessageManager().getMessage("channel") + ">"));
      String mlist = "";
      for (Channel c : LegendchatAPI.getChannelManager().getChannels())
      {
        if (LegendchatAPI.getPlayerManager().canPlayerSeeChannel((Player) sender, c))
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
      }
      sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("message21").replace("@channels", (mlist.length()==0 ? LegendchatAPI.getMessageManager().getMessage("nothing"):mlist)));
    }
    else
    {
      Channel c = null;
      ChannelManager cm = LegendchatAPI.getChannelManager();
      c = cm.getChannelByName(args[0].toLowerCase());
      if (c==null)
      {
        c = cm.getChannelByNickname(args[0].toLowerCase());
      }
      if (c==null)
      {
        sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error4"));
        return true;
      }

      LegendchatAPI.getPlayerManager().setPlayerFocusedChannel((Player) sender, c, true);
    }
    return true;
  }
}
