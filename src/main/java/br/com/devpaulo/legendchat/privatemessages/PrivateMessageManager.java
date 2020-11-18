package br.com.devpaulo.legendchat.privatemessages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.com.devpaulo.legendchat.api.LegendchatAPI;
import br.com.devpaulo.legendchat.api.events.PrivateMessageEvent;

public class PrivateMessageManager
{
  private HashMap<CommandSender, CommandSender> telling = new HashMap<CommandSender, CommandSender>();
  private HashMap<CommandSender, CommandSender> reply = new HashMap<CommandSender, CommandSender>();
  private CommandSender console = Bukkit.getConsoleSender();

  public void tellPlayer(CommandSender from, CommandSender to, String msg)
  {
    if (to == null)
    {
			if (!isPlayerTellLocked(from))
			{
				return;
			}
      to = telling.get(from);
    }

    PrivateMessageEvent e = new PrivateMessageEvent(from, to, msg);
    Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled())
		{
			return;
		}
    from = e.getSender();
    to = e.getReceiver();
    msg = e.getMessage();

    boolean ignored = false;
		if (to!=console && from!=console)
		{
			if (LegendchatAPI.getIgnoreManager().hasPlayerIgnoredPlayer((Player) to, from.getName()))
			{
				ignored = true;
			}
		}

		if (!ignored)
		{
			setPlayerReply(to, from);
		}

    from.sendMessage(ChatColor.translateAlternateColorCodes('&', LegendchatAPI.getPrivateMessageFormat("send")).replace("{sender}", from.getName()).replace("{receiver}", to.getName()).replace("{msg}", msg));
		if (!ignored)
		{
			to.sendMessage(ChatColor.translateAlternateColorCodes('&', LegendchatAPI.getPrivateMessageFormat("receive")).replace("{sender}", from.getName()).replace("{receiver}", to.getName()).replace("{msg}", msg));
		}

    String spy = ChatColor.translateAlternateColorCodes('&', LegendchatAPI.getPrivateMessageFormat("spy").replace("{sender}", from.getName()).replace("{receiver}", to.getName()).replace("{ignored}", (ignored ? LegendchatAPI.getMessageManager().getMessage("ignored"):""))).replace("{msg}", msg);

    for (Player p : LegendchatAPI.getPlayerManager().getOnlineSpys())
			if ((p!=from && p!=to) || (ignored && p==to))
			{
				p.sendMessage(spy);
			}

		if (LegendchatAPI.logToBukkit())
		{
			Bukkit.getConsoleSender().sendMessage(spy);
		}
  }

  public void replyPlayer(CommandSender from, String msg)
  {
    if (!playerHasReply(from))
    {
      from.sendMessage(LegendchatAPI.getMessageManager().getMessage("pm_error1"));
      return;
    }
    tellPlayer(from, getPlayerReply(from), msg);
  }

  public void lockPlayerTell(CommandSender from, CommandSender to)
  {
    unlockPlayerTell(from);
    telling.put(from, to);
  }

  public void unlockPlayerTell(CommandSender p)
  {
		if (isPlayerTellLocked(p))
		{
			telling.remove(p);
		}
  }

  public boolean isPlayerTellLocked(CommandSender p)
  {
    return telling.containsKey(p);
  }

  public CommandSender getPlayerLockedTellWith(CommandSender p)
  {
		if (isPlayerTellLocked(p))
		{
			return telling.get(p);
		}
    return null;
  }

  public List<CommandSender> getAllTellLockedPlayers()
  {
    List<CommandSender> l = new ArrayList<CommandSender>();
    l.addAll(telling.keySet());
    return l;
  }

  public void setPlayerReply(CommandSender to, CommandSender from)
  {
		if (playerHasReply(to))
		{
			reply.remove(to);
		}
    reply.put(to, from);
  }

  public CommandSender getPlayerReply(CommandSender p)
  {
		if (!playerHasReply(p))
		{
			return null;
		}
    return reply.get(p);
  }

  public boolean playerHasReply(CommandSender p)
  {
    return reply.containsKey(p);
  }

  public List<CommandSender> getAllPlayersWithReply()
  {
    List<CommandSender> l = new ArrayList<CommandSender>();
    l.addAll(reply.keySet());
    return l;
  }

  public void playerDisconnect(CommandSender p)
  {
    unlockPlayerTell(p);
		if (reply.containsKey(p))
		{
			reply.remove(p);
		}
    List<CommandSender> lista = new ArrayList<CommandSender>();
    for (CommandSender p2 : getAllTellLockedPlayers())
			if (telling.get(p2)==p)
			{
				lista.add(p2);
			}
    for (CommandSender p3 : lista)
      telling.remove(p3);
    lista.clear();
    for (CommandSender p2 : getAllPlayersWithReply())
			if (reply.get(p2)==p)
			{
				lista.add(p2);
			}
    for (CommandSender p3 : lista)
      reply.remove(p3);
  }
}
