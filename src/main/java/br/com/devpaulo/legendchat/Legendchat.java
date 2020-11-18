package br.com.devpaulo.legendchat;

import br.com.devpaulo.legendchat.api.LegendchatAPI;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import br.com.devpaulo.legendchat.commands.*;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import br.com.devpaulo.legendchat.channels.types.BungeecordChannel;
import br.com.devpaulo.legendchat.channels.types.PermanentChannel;
import br.com.devpaulo.legendchat.listeners.Listeners;

public class Legendchat extends JavaPlugin implements PluginMessageListener
{
  public static Permission perms = null;
  public static Economy econ = null;
  public static Chat chat = null;
  public static boolean block_econ = false;
  public static boolean block_perms = false;
  public static boolean block_chat = false;
  public static boolean bungeeActive = false;
  
  @Override
  public void onEnable()
  {
    getLogger().info("Legendchat (V" + getDescription().getVersion() + ") - Author: SubZero0");
    LegendchatAPI.load(false);
    
    getServer().getPluginCommand("legendchat").setExecutor(new LegendchatCommand());
    getServer().getPluginCommand("channel").setExecutor(new ChannelCommand());
    getServer().getPluginCommand("tell").setExecutor(new TellCommand());
    getServer().getPluginCommand("reply").setExecutor(new ReplyCommand());
    getServer().getPluginCommand("ignore").setExecutor(new IgnoreCommand());
    getServer().getPluginCommand("mute").setExecutor(new MuteCommand());
    
    getServer().getPluginManager().registerEvents(new Listeners(), this);
    
    getServer().getMessenger().registerOutgoingPluginChannel(this, "legendchat:bungee");
    getServer().getMessenger().registerIncomingPluginChannel(this, "legendchat:bungee", this);
    
    File file = new File(getDataFolder(), "config.yml");
    if (!file.exists())
    {
      try
      {
        saveResource("config_template.yml", false);
        File file2 = new File(getDataFolder(), "config_template.yml");
        file2.renameTo(new File(getDataFolder(), "config.yml"));
      } catch (Exception e)
      {
      }
    }
    reloadConfig();
    
    try
    {
      if (!new File(getDataFolder(), "Lang.yml").exists())
      {
        saveResource("Lang.yml", false);
        getLogger().info("Saved Lang.yml");
      }
    } catch (Exception e)
    {
    }
    
    File channels = new File(getDataFolder(), "channels");
    if (!channels.exists())
    {
      channels.mkdir();
      LegendchatAPI.getChannelManager().createPermanentChannel(new PermanentChannel("global", "g", "{default}", "GRAY", true, false, 0, true, 0, 0, true));
      LegendchatAPI.getChannelManager().createPermanentChannel(new PermanentChannel("local", "l", "{default}", "YELLOW", true, false, 60, false, 0, 0, true));
      LegendchatAPI.getChannelManager().createPermanentChannel(new BungeecordChannel("bungeecord", "b", "{bungeecord}", "LIGHTPURPLE", true, false, 0, false, 0, 0, true));
    }
  
    LegendchatAPI.getChannelManager().loadChannels();
    
    if (!setupPermissions())
    {
      getLogger().warning("Vault is not linked to any permissions plugin.");
      block_perms = true;
    }
    else
    {
      getLogger().info("Hooked to Vault (Permissions).");
    }
    
    if (!setupEconomy())
    {
      getLogger().warning("Vault is not linked to any economy plugin.");
      block_econ = true;
    }
    else
    {
      getLogger().info("Hooked to Vault (Economy).");
    }
    
    if (!setupChat())
    {
      getLogger().warning("Vault is not linked to any chat plugin.");
      block_chat = true;
    }
    else
    {
      getLogger().info("Hooked to Vault (Chat).");
    }
    
    if (getConfig().getBoolean("bungeecord.use", false))
    {
      if (LegendchatAPI.getChannelManager().existsChannel(getConfig().getString("bungeecord.channel", "bungeecord")))
      {
        bungeeActive = true;
      }
    }
  
    LegendchatAPI.load(true);
    
    for (Player p : getServer().getOnlinePlayers())
      LegendchatAPI.getPlayerManager().setPlayerFocusedChannel(p, LegendchatAPI.getDefaultChannel(), false);
  }
  
  private boolean setupPermissions()
  {
    if (getServer().getPluginManager().getPlugin("Vault")==null)
    {
      return false;
    }
    RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
    if (rsp==null)
    {
      return false;
    }
    perms = rsp.getProvider();
    return perms!=null;
  }
  
  private boolean setupChat()
  {
    if (getServer().getPluginManager().getPlugin("Vault")==null)
    {
      return false;
    }
    RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
    if (rsp==null)
    {
      return false;
    }
    chat = rsp.getProvider();
    return chat!=null;
  }
  
  private boolean setupEconomy()
  {
    if (getServer().getPluginManager().getPlugin("Vault")==null)
    {
      return false;
    }
    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp==null)
    {
      return false;
    }
    econ = rsp.getProvider();
    return econ!=null;
  }
  
  @Override
  public void onDisable()
  {
    getLogger().info("Disabling Legendchat - Author: SubZero0");
    LegendchatAPI.getLogManager().saveLog();
  }
  
  @Override
  public void onPluginMessageReceived(String channel, Player player, byte[] message)
  {
    if (LegendchatAPI.isBungeecordActive())
    {
      if (!channel.equals("legendchat:bungee"))
      {
        return;
      }
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
      String raw_tags = "";
      String msg = "";
      try
      {
        raw_tags = in.readUTF();
        msg = in.readUTF();
      } catch (IOException e)
      {
        e.printStackTrace();
      }
      HashMap<String, String> tags = new HashMap<String, String>();
      raw_tags = raw_tags.substring(1, raw_tags.length() - 1);
      String[] pairs = raw_tags.split(",");
      for (String separated_pairs : pairs)
      {
        String[] pair = separated_pairs.split("=");
        tags.put(pair[0].replace(" ", ""), (pair.length==1 ? "":pair[1]));
      }
      this.getServer().getLogger().info("[Legendchat] Incoming message from server " + tags.get("server"));
      BungeecordChannel c = LegendchatAPI.getBungeecordChannel();
      if (c!=null)
      {
        c.sendBungeecordMessage(tags, msg);
      }
    }
  }
}
