package br.com.devpaulo.legendchat.listeners;

import me.giverplay.focalib.chat.channel.Channel;
import me.giverplay.focalib.chat.channel.ChannelManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;

public class Listeners implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    private void onJoin(PlayerJoinEvent e) {
        ChannelManager.getPlayerManager().setPlayerFocusedChannel(e.getPlayer(), ChannelManager.getDefaultChannel(), false);
    }

    private static HashMap<AsyncPlayerChatEvent, Boolean> chats = new HashMap<AsyncPlayerChatEvent, Boolean>();

    public static HashMap<AsyncPlayerChatEvent, Boolean> getChats() {
        HashMap<AsyncPlayerChatEvent, Boolean> clone = new HashMap<AsyncPlayerChatEvent, Boolean>();
        clone.putAll(chats);
        return clone;
    }

    public static void addFakeChat(AsyncPlayerChatEvent e, Boolean b) {
        if (!chats.containsKey(e)) {
            chats.put(e, b);
        }
    }

    public static void removeFakeChat(AsyncPlayerChatEvent e) {
        if (chats.containsKey(e)) {
            chats.remove(e);
        }
    }

    public static boolean hasFakeChat(AsyncPlayerChatEvent e) {
        return chats.containsKey(e);
    }

    public static boolean getFakeChat(AsyncPlayerChatEvent e) {
        if (chats.containsKey(e)) {
            return chats.get(e);
        }
        return true;
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    private void onChat(AsyncPlayerChatEvent e) {
        HashMap<String, String> ttt = ChannelManager.textToTag();
        if (ttt.size() > 0) {
            String new_format = "°1º°";
            int i = 2;
            for (String n : ttt.keySet()) {
                new_format += ttt.get(n) + ChatColor.RESET + "°" + i + "º°";
                i++;
            }
            e.setFormat(e.getFormat() + " " + new_format);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onChat2(AsyncPlayerChatEvent e) {
        if (e.getMessage() != null && !chats.containsKey(e) && !e.isCancelled()) {
            if (ChannelManager.getPrivateMessageManager().isPlayerTellLocked(e.getPlayer())) {
                ChannelManager.getPrivateMessageManager().tellPlayer(e.getPlayer(), null, e.getMessage());
            } else {
                if (ChannelManager.getPlayerManager().isPlayerFocusedInAnyChannel(e.getPlayer())) {
                    ChannelManager.getPlayerManager().getPlayerFocusedChannel(e.getPlayer()).sendMessage(e.getPlayer(), e.getMessage(), e.getFormat(), e.isCancelled());
                } else {
                    e.getPlayer().sendMessage(ChannelManager.getMessageManager().getMessage("error1"));
                }
            }
        } else if (chats.containsKey(e)) {
            chats.remove(e);
            chats.put(e, e.isCancelled());
        }
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onChat(PlayerCommandPreprocessEvent e) {
        boolean block = false;
        if (ChannelManager.blockShortcutsWhenCancelled()) {
            if (e.isCancelled()) {
                block = true;
            }
        }
        if (!block) {
            for (Channel c : ChannelManager.getChannelManager().getChannels()) {
                String lowered_msg = e.getMessage().toLowerCase();
                if (c.isShortcutAllowed()) {
                    if (lowered_msg.startsWith("/" + c.getNickname().toLowerCase())) {
                        if (e.getMessage().length() == ("/" + c.getNickname()).length()) {
                            e.getPlayer().sendMessage(ChannelManager.getMessageManager().getMessage("wrongcmd").replace("@command", "/" + c.getNickname().toLowerCase() + " <" + ChannelManager.getMessageManager().getMessage("message") + ">"));
                            e.setCancelled(true);
                        } else if (lowered_msg.startsWith("/" + c.getNickname().toLowerCase() + " ")) {
                            String message = "";
                            String[] split = e.getMessage().split(" ");
                            for (int i = 1; i < split.length; i++) {
                                if (message.length() == 0) {
                                    message = split[i];
                                } else {
                                    message += " " + split[i];
                                }
                            }
                            c.sendMessage(e.getPlayer(), message);
                            e.setCancelled(true);
                        }
                    }
                    if (lowered_msg.startsWith("/" + c.getName().toLowerCase())) {
                        if (e.getMessage().length() == ("/" + c.getName()).length()) {
                            e.getPlayer().sendMessage(ChannelManager.getMessageManager().getMessage("wrongcmd").replace("@command", "/" + c.getName().toLowerCase() + " <" + ChannelManager.getMessageManager().getMessage("message") + ">"));
                            e.setCancelled(true);
                        } else if (lowered_msg.startsWith("/" + c.getName().toLowerCase() + " ")) {
                            String message = "";
                            String[] split = e.getMessage().split(" ");
                            for (int i = 1; i < split.length; i++) {
                                if (message.length() == 0) {
                                    message = split[i];
                                } else {
                                    message += " " + split[i];
                                }
                            }
                            c.sendMessage(e.getPlayer(), message);
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}
