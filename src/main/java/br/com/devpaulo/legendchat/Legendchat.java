package br.com.devpaulo.legendchat;

import br.com.devpaulo.legendchat.api.LegendchatAPI;
import br.com.devpaulo.legendchat.channels.types.PermanentChannel;
import br.com.devpaulo.legendchat.commands.*;
import br.com.devpaulo.legendchat.listeners.Listeners;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Legendchat extends JavaPlugin
{
  public static Economy econ = null;
  public static Chat chat = null;
  public static boolean block_econ = false;
  public static boolean block_chat = false;
  
  @Override
  public void onEnable()
  {
    LegendchatAPI.load(false);
    getServer().getPluginCommand("legendchat").setExecutor(new LegendchatCommand());
    getServer().getPluginCommand("channel").setExecutor(new ChannelCommand());
    getServer().getPluginCommand("tell").setExecutor(new TellCommand());
    getServer().getPluginCommand("reply").setExecutor(new ReplyCommand());
    getServer().getPluginCommand("ignore").setExecutor(new IgnoreCommand());
    getServer().getPluginCommand("mute").setExecutor(new MuteCommand());
    getServer().getPluginManager().registerEvents(new Listeners(), this);
    
    File file = new File(getDataFolder(), "config.yml");
    if (!file.exists())
    {
      try
      {
        saveResource("config_template.yml", false);
        File file2 = new File(getDataFolder(), "config_template.yml");
        file2.renameTo(new File(getDataFolder(), "config.yml"));
      } catch (Exception ignored)
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
    }
  
    LegendchatAPI.getChannelManager().loadChannels();
  
    LegendchatAPI.load(true);
    
    for (Player p : getServer().getOnlinePlayers())
      LegendchatAPI.getPlayerManager().setPlayerFocusedChannel(p, LegendchatAPI.getDefaultChannel(), false);
  }
}
