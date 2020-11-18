package br.com.devpaulo.legendchat;

import me.giverplay.focalib.chat.channel.ChannelManager;
import me.giverplay.focalib.chat.channel.Channel;
import br.com.devpaulo.legendchat.delays.DelayManager;
import br.com.devpaulo.legendchat.ignore.IgnoreManager;
import br.com.devpaulo.legendchat.messages.MessageManager;
import br.com.devpaulo.legendchat.mutes.MuteManager;
import br.com.devpaulo.legendchat.players.PlayerManager;
import br.com.devpaulo.legendchat.privatemessages.PrivateMessageManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;

public class LegendchatAPI
{
	private static boolean logToBukkit = false;
	private static boolean blockRepeatedTags = false;
	private static boolean showNoOneHearsYou = false;
	private static boolean forceRemoveDoubleSpacesFromBukkit = false;
	private static boolean sendFakeMessageToChat = false;
	private static boolean blockShortcutsWhenCancelled = false;
	private static boolean maintainSpyMode = false;
	private static Channel defaultChannel = null;
	private static Plugin plugin = null;
	private static HashMap<String,String> formats = new HashMap<String,String>();
	private static HashMap<String,String> pm_formats = new HashMap<String,String>();
	private static HashMap<String,String> text_to_tag = new HashMap<String,String>();
	
	private static ChannelManager cm = null;
	private static PlayerManager pm = null;
	private static MessageManager mm = null;
	private static IgnoreManager im = null;
	private static PrivateMessageManager pmm = null;
	private static DelayManager dm = null;
	private static MuteManager mum = null;
	
	public static ChannelManager getChannelManager() {
		return cm;
	}
	
	public static PlayerManager getPlayerManager() {
		return pm;
	}
	
	public static MessageManager getMessageManager() {
		return mm;
	}
	
	public static IgnoreManager getIgnoreManager() {
		return im;
	}
	
	public static PrivateMessageManager getPrivateMessageManager() {
		return pmm;
	}
	
	public static DelayManager getDelayManager() {
		return dm;
	}
	
	public static MuteManager getMuteManager() {
		return mum;
	}
	
	public static Channel getDefaultChannel() {
		return defaultChannel;
	}
	
	public static boolean logToBukkit() {
		return logToBukkit;
	}
	
	public static boolean blockRepeatedTags() {
		return blockRepeatedTags;
	}
	
	public static boolean showNoOneHearsYou() {
		return showNoOneHearsYou;
	}
	
	public static boolean forceRemoveDoubleSpacesFromBukkit() {
		return forceRemoveDoubleSpacesFromBukkit;
	}
	
	public static boolean sendFakeMessageToChat() {
		return sendFakeMessageToChat;
	}
	
	public static boolean blockShortcutsWhenCancelled() {
		return blockShortcutsWhenCancelled;
	}

	public static boolean maintainSpyMode() {
		return maintainSpyMode;
	}
	
	public static Plugin getPlugin() {
		return plugin;
	}
	
	public static String format(String msg) {
		for(String f : formats.keySet())
			msg = msg.replace("{"+f+"}", formats.get(f));
		return msg;
	}
	
	public static String getFormat(String base_format) {
		return formats.get(base_format.toLowerCase());
	}
	
	public static String getPrivateMessageFormat(String format) {
		return pm_formats.get(format.toLowerCase());
	}
	
	public static HashMap<String,String> textToTag() {
		HashMap<String,String> h = new HashMap<String,String>();
		h.putAll(text_to_tag);
		return h;
	}
	
	public static void load(boolean all) {
		plugin=Bukkit.getPluginManager().getPlugin("Legendchat");
		if(!all) {
			cm=new ChannelManager();
			pm=new PlayerManager();
			mm=new MessageManager();
			im=new IgnoreManager();
			pmm=new PrivateMessageManager();
			dm=new DelayManager();
			mum=new MuteManager();
			return;
		}
		FileConfiguration fc = Bukkit.getPluginManager().getPlugin("Legendchat").getConfig();
		defaultChannel= LegendchatAPI.getChannelManager().getChannelByName(fc.getString("default_channel","local").toLowerCase());
		logToBukkit=fc.getBoolean("log_to_bukkit",false);
		blockRepeatedTags=fc.getBoolean("block_repeated_tags",true);
		showNoOneHearsYou=fc.getBoolean("show_no_one_hears_you",true);
		forceRemoveDoubleSpacesFromBukkit=fc.getBoolean("force_remove_double_spaces_from_bukkit",true);
		sendFakeMessageToChat=fc.getBoolean("send_fake_message_to_chat",true);
		blockShortcutsWhenCancelled=fc.getBoolean("block_shortcuts_when_cancelled",true);
		maintainSpyMode=fc.getBoolean("maintain_spy_mode",false);
		formats.clear();
		pm_formats.clear();
		for(String f : fc.getConfigurationSection("format").getKeys(false))
			formats.put(f.toLowerCase(), fc.getString("format."+f));
		for(String f : fc.getConfigurationSection("private_message_format").getKeys(false))
			pm_formats.put(f.toLowerCase(), fc.getString("private_message_format."+f));
		for(String f : fc.getStringList("text_to_tag")) {
			String[] s = f.split(";");
			text_to_tag.put(s[0].toLowerCase(), s[1]);
		}

		mm.loadMessages(new File(plugin.getDataFolder(), "Lang.yml"));
	}
}
