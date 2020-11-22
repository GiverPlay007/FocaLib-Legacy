package me.giverplay.focalib.chat

import me.giverplay.focalib.FocaLib
import me.giverplay.focalib.chat.events.PrivateMessageEvent
import me.giverplay.focalib.command.commands.*
import me.giverplay.focalib.listeners.ListenerChat
import me.giverplay.focalib.player.FocaPlayer
import me.giverplay.focalib.utils.ColorUtils
import me.giverplay.focalib.utils.DependencyException
import me.giverplay.focalib.utils.Messages
import me.giverplay.focalib.utils.Messages.Companion.msg
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.RegisteredServiceProvider

class ChatManager(private val plugin: FocaLib)
{
    private var channelManager: ChannelManager = ChannelManager(plugin, this)

    private var chat: Chat? = null

    init {
        if (!setupChat())
            throw DependencyException(Messages.msg("error.dependency.setupchat"))

        channelManager.load()
        plugin.commandManager.registerCommands(
            CommandChat(channelManager),
            CommandChannel(channelManager),
            CommandTell(this),
            CommandReply(this),
            CommandIgnore()
        )
        plugin.registerEvent(ListenerChat(plugin, channelManager, this))
    }

    private fun setupChat(): Boolean {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null)
            return false

        val rsp: RegisteredServiceProvider<Chat> = Bukkit.getServicesManager().getRegistration(Chat::class.java)
            ?: return false

        chat = rsp.provider
        return chat != null
    }

    fun sendMessage(c: Channel, sender: Player, message: String?, bukkit_format: String, cancelled: Boolean) {
        if (!sender.hasPermission("channel." + c.name.toLowerCase() + ".chat") && !sender.hasPermission("admin")) {
            sender.sendMessage(Messages.msg("error2"))
            return
        }
        if (sender.hasPermission("channel." + c.name.toLowerCase() + ".blockwrite") && !sender.hasPermission(
                "admin"
            )
        ) {
            sender.sendMessage(Messages.msg("error2"))
            return
        }
        if (c.isFocusNeeded) {
            if (getPlayerManager().getPlayerFocusedChannel(sender) !== c) {
                sender.sendMessage(Messages.msg("error12"))
                return
            }
        }
        val delay = getDelayManager().getPlayerDelayFromChannel(sender.name, c)
        if (delay > 0) {
            sender.sendMessage(
                Messages.msg("error11").replace("@time", Integer.toString(delay))
            )
            return
        }

        if (getMuteManager().isServerMuted) {
            sender.sendMessage(Messages.msg("mute_error8"))
            return
        }

        val recipients: MutableSet<Player> = HashSet()
        for (p in Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("channel." + c.name.toLowerCase() + ".chat") || p.hasPermission("admin")) {
                recipients.add(p)
            }
        }
        val recipients2: MutableSet<Player> = HashSet()
        recipients2.addAll(recipients)
        for (p in recipients2) {
            if (c.maxDistance != 0.0) {
                if (sender.world !== p.world) {
                    recipients.remove(p)
                    continue
                } else if (sender.location.distance(p.location) > c.maxDistance) {
                    recipients.remove(p)
                    continue
                }
            } else {
                if (!c.isCrossworlds) {
                    if (sender.world !== p.world) {
                        recipients.remove(p)
                        continue
                    }
                }
            }
            if (getIgnoreManager().hasPlayerIgnoredPlayer(p, sender.name)) {
                recipients.remove(p)
                continue
            }
            if (getIgnoreManager().hasPlayerIgnoredChannel(p, c)) {
                recipients.remove(p)
                continue
            }
        }
        var gastou = false
        if (!block_econ && c.msgCost() > 0) {
            if (!sender.hasPermission("channel." + c.name.toLowerCase() + ".free") && !sender.hasPermission("admin")) {
                if (econ.getBalance(sender.name) < c.msgCost()) {
                    sender.sendMessage(
                        Messages.msg("error3")
                            .replace("@price", java.lang.Double.toString(c.msgCost()))
                    )
                    return
                }
                econ.withdrawPlayer(sender.name, c.msgCost())
                gastou = true
            }
        }
        var n_format_p_p = ""
        var n_format_p = ""
        var n_format_s = ""
        if (bukkit_format.contains("<") && bukkit_format.contains(">")) {
            var name_code: String? = null
            if (bukkit_format.contains("%1\$s")) {
                name_code = "%1\$s"
            } else if (bukkit_format.contains("%s")) {
                name_code = "%s"
            }
            val seploc = bukkit_format.indexOf(name_code!!)
            var finalloc = -1
            for (i in seploc downTo 0) if (bukkit_format[i] == '<') {
                finalloc = i
                break
            }
            if (finalloc != -1) {
                n_format_p_p = bukkit_format.substring(0, finalloc)
                if (name_code != null) {
                    val n_format = bukkit_format.substring(finalloc + 1).split(">").toTypedArray()[0].split(name_code)
                        .toTypedArray()
                    if (n_format.size > 0) {
                        n_format_p = n_format[0].replace(name_code, "").replace("{factions_relcolor}", "")
                    }
                    if (n_format.size > 1) {
                        n_format_s = n_format[1]
                    }
                }
            }
        }
        val tags = HashMap<String, String>()
        tags["name"] = c.name
        tags["nick"] = c.nickname
        tags["color"] = c.color
        tags["sender"] = sender.displayName
        tags["plainsender"] = sender.name
        tags["world"] = sender.world.name
        tags["bprefix"] = if (n_format_p_p == " ") "" else n_format_p_p.replace("  ", " ")
        tags["bprefix2"] = if (n_format_p == " ") "" else n_format_p.replace("  ", " ")
        tags["bsuffix"] = if (n_format_s == " ") "" else n_format_s.replace("  ", " ")

        if (!block_chat) {
            tags["prefix"] = tag(chat.getPlayerPrefix(sender))
            tags["suffix"] = tag(chat.getPlayerSuffix(sender))
            tags["groupprefix"] = tag(chat.getGroupPrefix(sender.world, chat.getPrimaryGroup(sender)))
            tags["groupsuffix"] = tag(chat.getGroupSuffix(sender.world, chat.getPrimaryGroup(sender)))
            for (g in chat.getPlayerGroups(sender)) {
                tags[g.toLowerCase() + "prefix"] = tag(chat.getGroupPrefix(sender.world, g))
                tags[g.toLowerCase() + "suffix"] = tag(chat.getGroupSuffix(sender.world, g))
            }
        }

        val e = ChatMessageEvent(c, sender, message, format(c.format), c.format, bukkit_format, recipients, tags, cancelled)
        val effectiveGastou = gastou
        Bukkit.getScheduler().runTask(plugin, Runnable {
            Bukkit.getPluginManager().callEvent(e)
            realMessage0(e, c, effectiveGastou)
        })
    }

    private fun tag(tag: String?): String = tag ?: ""

    private fun realMessage0(e: ChatMessageEvent, c: Channel, gastou: Boolean) {
        if (e.isCancelled) {
            return
        }
        val sender = e.sender
        val message = e.message
        var completa = e.format
        if (e.tags.contains("prefix") && e.tags.contains("groupprefix")) {
            if (e.getTagValue("prefix") == e.getTagValue("groupprefix")) {
                e.setTagValue("prefix", "")
            }
        }
        if (e.tags.contains("suffix") && e.tags.contains("groupsuffix")) {
            if (e.getTagValue("suffix") == e.getTagValue("groupsuffix")) {
                e.setTagValue("suffix", "")
            }
        }
        for (n in e.tags) completa =
            completa.replace("{$n}", ChatColor.translateAlternateColorCodes('&', e.getTagValue(n)))
        completa = completa.replace("{msg}", ColorUtils.translateAlternateChatColorsWithPermission(sender, message)!!)
        for (p in e.recipients) p.sendMessage(completa)
        if (c.delayPerMessage > 0 && !sender.hasPermission("channel." + c.name.toLowerCase() + ".nodelay") && !sender.hasPermission(
                "admin"
            )
        ) {
            getDelayManager().addPlayerDelay(sender.name, c)
        }
        if (c.maxDistance != 0.0) {
            if (showNoOneHearsYou()) {
                var show = false
                if (e.recipients.size == 0) {
                    show = true
                } else if (e.recipients.size == 1 && e.recipients.contains(sender)) {
                    show = true
                } else {
                    show = true
                    for (p in e.recipients) if (p !== sender && !getPlayerManager()
                            .isPlayerHiddenFromRecipients(p)
                    ) {
                        show = false
                        break
                    }
                }
                if (show) {
                    sender.sendMessage(Messages.msg("special"))
                }
            }
        }
        for (p in getPlayerManager().getOnlineSpys()) if (!e.recipients.contains(p)) {
            p.sendMessage(
                ChatColor.translateAlternateColorCodes(
                    '&', getFormat("spy")?.replace(
                        "{msg}", ChatColor.stripColor(
                            completa
                        )!!
                    )
                )
            )
        }
        if (gastou) {
            if (c.showCostMessage())
                sender.sendMessage(Messages.msg("message9").replace("@money", java.lang.Double.toString(c.costPerMessage)))
        }
        Bukkit.getConsoleSender().sendMessage(completa)
    }

    fun performTell(player: FocaPlayer, other: FocaPlayer, message: Array<out String>)
    {
        val sb: StringBuilder = StringBuilder()

        for(s: String in message)
        {
            sb.append(s).append(" ")
        }

        performTell(player, other, sb.toString().trim())
    }

    fun performTell(player: FocaPlayer, other: FocaPlayer, message: String)
    {
        if(!other.settings.tellEnabled)
            return player.sendRaw(msg("error.tell-locked")!!)

        if(player.ignoring.contains(other.name))
        {
            player.ignoring.remove(other.name)
            player.sendRaw(msg("info.unignore", other.name)!!)
        }

        if(other.ignoring.contains(player.name))
            return player.sendRaw(msg("error.ignored", other.name)!!)

        if(!player.settings.tellEnabled)
        {
            player.settings.tellEnabled = true
            player.sendRaw(msg("info.tell-enabled")!!)
        }

        val event = PrivateMessageEvent(player, other, message)
        Bukkit.getPluginManager().callEvent(event)

        if(event.isCancelled)
            return

        val sender: FocaPlayer = event.getSender()
        val receiver: FocaPlayer = event.getReceiver()
        val msg: String = event.getMessage()
        val sendFormat: String = ChatColor.translateAlternateColorCodes('&', channelManager.getPrivateMessageFormat("send")!!)
        val receiveFormat: String = ChatColor.translateAlternateColorCodes('&', channelManager.getPrivateMessageFormat("receive")!!)
        val spyFormat: String = ChatColor.translateAlternateColorCodes('&', channelManager.getPrivateMessageFormat("spy")!!)

        sender.sendRaw(sendFormat.replace("{receiver}", receiver.name).replace("{msg}", msg))
        receiver.sendRaw(receiveFormat.replace("{sender}", sender.name).replace("{msg}", msg))

        plugin.playerManager.getPlayers().forEach { p ->
            if (p.settings.spying && p != sender && p != other)
                p.sendRaw(spyFormat.replace("{sender}", sender.name).replace("{receiver}", receiver.name).replace("{msg}", msg))
        }

        receiver.lastTell = sender
        Bukkit.getConsoleSender().sendMessage(spyFormat.replace("{sender}", sender.name).replace("{receiver}", receiver.name).replace("{msg}", msg))
    }

    fun performMessage(player: FocaPlayer, message: String, shotcut: Boolean) {
        // TODO
    }
}