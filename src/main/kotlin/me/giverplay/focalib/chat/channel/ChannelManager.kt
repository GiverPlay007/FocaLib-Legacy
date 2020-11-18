package me.giverplay.focalib.chat.channel

import br.com.devpaulo.delays.DelayManager
import br.com.devpaulo.events.ChatMessageEvent
import br.com.devpaulo.ignore.IgnoreManager
import br.com.devpaulo.legendchat.delays.DelayManager
import br.com.devpaulo.legendchat.events.ChatMessageEvent
import br.com.devpaulo.legendchat.ignore.IgnoreManager
import br.com.devpaulo.legendchat.messages.MessageManager
import br.com.devpaulo.legendchat.mutes.MuteManager
import br.com.devpaulo.legendchat.privatemessages.PrivateMessageManager
import br.com.devpaulo.listeners.Listeners
import br.com.devpaulo.messages.MessageManager
import br.com.devpaulo.mutes.MuteManager
import br.com.devpaulo.privatemessages.PrivateMessageManager
import me.giverplay.focalib.FocaLib
import me.giverplay.focalib.player.FocaPlayer
import me.giverplay.focalib.player.PlayerManager
import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class ChannelManager(private val plugin: FocaLib) {
    private val channels = HashMap<String, Channel>()
    fun createChannel(c: Channel) {
        if (existsChannel(c.name)) return
        channels[c.name.toLowerCase()] = c
    }

    fun createPermanentChannel(c: Channel) {
        if (existsChannel(c.name)) return
        channels[c.name.toLowerCase()] = c
        val channel =
            File(plugin.dataFolder, "channels" + File.separator + c.name.toLowerCase() + ".yml")
        if (!channel.exists()) {
            try {
                channel.createNewFile()
            } catch (e: Exception) {
            }
            val channel2 = YamlConfiguration.loadConfiguration(channel)
            channel2["name"] = c.name
            channel2["nickname"] = c.nickname
            channel2["format"] = c.format
            channel2["color"] = c.color.name.toLowerCase()
            channel2["shortcutAllowed"] = c.isShortcutAllowed
            channel2["needFocus"] = c.isFocusNeeded
            channel2["distance"] = c.maxDistance
            channel2["crossworlds"] = c.isCrossworlds
            channel2["delayPerMessage"] = c.delayPerMessage
            channel2["costPerMessage"] = c.costPerMessage
            channel2["showCostMessage"] = c.showCostMessage
            try {
                channel2.save(channel)
            } catch (e: Exception) {
            }
        }
    }

    fun deleteChannel(c: Channel) {
        if (!existsChannel(c.name)) return
        for (p in c.getPlayersFocusedInChannel()) plugin.getPlayerManager()
            .setPlayerFocusedChannel(p, getDefaultChannel(), false)
        channels.remove(c.name.toLowerCase())
        File(plugin.dataFolder, "channels" + File.separator + c.name.toLowerCase() + ".yml").delete()
    }

    fun getChannelByName(name: String): Channel? {
        var name = name
        name = name.toLowerCase()
        return if (existsChannel(name)) channels[name] else null
    }

    fun getChannelByNickname(nickname: String?): Channel? {
        for (c in getChannels()) if (c.nickname.equals(nickname, ignoreCase = true)) return c
        return null
    }

    fun getChannelByNameOrNickname(name_or_nickname: String): Channel? {
        var c = getChannelByName(name_or_nickname)
        if (c == null) c = getChannelByNickname(name_or_nickname)
        return c
    }

    fun existsChannel(name: String): Boolean {
        return channels.containsKey(name.toLowerCase())
    }

    fun existsChannelAdvanced(name_or_nickname: String): Boolean {
        var e = channels.containsKey(name_or_nickname.toLowerCase())
        if (!e) e = if (getChannelByNickname(name_or_nickname) == null) false else true
        return e
    }

    fun getChannels(): List<Channel> {
        val c: MutableList<Channel> = ArrayList()
        c.addAll(channels.values)
        return c
    }

    fun loadChannels() {
        val bungee = plugin.config.getString("bungeecord.channel")!!
        channels.clear()
        for (channel in File(plugin.dataFolder, "channels").listFiles()) {
            if (channel.name.toLowerCase().endsWith(".yml")) {
                if (channel.name.toLowerCase() != channel.name) channel.renameTo(
                    File(
                        plugin.dataFolder,
                        "channels" + File.separator + channel.name.toLowerCase()
                    )
                )
                loadChannel(channel, bungee)
            }
        }
        for (p in Bukkit.getOnlinePlayers()) getPlayerManager()
            .setPlayerFocusedChannel(p, getDefaultChannel(), false)
    }

    private fun loadChannel(channel: File, bungee: String) {
        val channel2 = YamlConfiguration.loadConfiguration(channel)
        createPermanentChannel(
            Channel(
                channel2.getString("name"),
                channel2.getString("nickname")!!,
                channel2.getString("format")!!,
                channel2.getString("color")!!,
                channel2.getBoolean("shortcutAllowed"),
                channel2.getBoolean("needFocus"),
                channel2.getDouble("distance"),
                channel2.getBoolean("crossworlds"),
                channel2.getInt("delayPerMessage"),
                channel2.getDouble("costPerMessage")
                    .toInt(),
                channel2.getBoolean("showCostMessage")
            )
        )
    }


    fun fakeMessage(c: Channel, sender: Player?, message: String?) {
        if (!sendFakeMessageToChat()) {
            c.sendMessage(sender, message, "", false)
            return
        }
        val p = HashSet<Player?>()
        p.add(sender)
        val event = AsyncPlayerChatEvent(
            true,
            sender!!, "legendchat", p
        )
        Listeners.addFakeChat(event, false)
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            Bukkit.getPluginManager().callEvent(event)
            c.sendMessage(sender, message, event.format, Listeners.getFakeChat(event))
            Listeners.removeFakeChat(event)
        })
    }

    fun realMessage(c: Channel, sender: Player, message: String?, bukkit_format: String, cancelled: Boolean) {
        if (!sender.hasPermission("channel." + c.name.toLowerCase() + ".chat") && !sender.hasPermission("admin")) {
            sender.sendMessage(getMessageManager().getMessage("error2"))
            return
        }
        if (sender.hasPermission("channel." + c.name.toLowerCase() + ".blockwrite") && !sender.hasPermission(
                "admin"
            )
        ) {
            sender.sendMessage(getMessageManager().getMessage("error2"))
            return
        }
        if (c.isFocusNeeded) {
            if (getPlayerManager().getPlayerFocusedChannel(sender) !== c) {
                sender.sendMessage(getMessageManager().getMessage("error12"))
                return
            }
        }
        val delay = getDelayManager().getPlayerDelayFromChannel(sender.name, c)
        if (delay > 0) {
            sender.sendMessage(
                getMessageManager().getMessage("error11").replace("@time", Integer.toString(delay))
            )
            return
        }
        if (getMuteManager().isPlayerMuted(sender.name)) {
            val time = getMuteManager().getPlayerMuteTimeLeft(sender.name)
            if (time == 0) {
                sender.sendMessage(getMessageManager().getMessage("mute_error4"))
            } else {
                sender.sendMessage(
                    getMessageManager().getMessage("mute_error5").replace("@time", Integer.toString(time))
                )
            }
            return
        }
        if (getMuteManager().isServerMuted) {
            sender.sendMessage(getMessageManager().getMessage("mute_error8"))
            return
        }
        if (getIgnoreManager().hasPlayerIgnoredChannel(sender, c)) {
            sender.sendMessage(getMessageManager().getMessage("error14"))
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
            if (c.isFocusNeeded) {
                if (getPlayerManager().getPlayerFocusedChannel(p) !== c) {
                    recipients.remove(p)
                }
            }
        }
        var gastou = false
        if (!block_econ && c.getMessageCost() > 0) {
            if (!sender.hasPermission("channel." + c.name.toLowerCase() + ".free") && !sender.hasPermission("admin")) {
                if (econ.getBalance(sender.name) < c.getMessageCost()) {
                    sender.sendMessage(
                        getMessageManager().getMessage("error3")
                            .replace("@price", java.lang.Double.toString(c.getMessageCost()))
                    )
                    return
                }
                econ.withdrawPlayer(sender.name, c.getMessageCost())
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
        tags["bprefix"] =
            if (forceRemoveDoubleSpacesFromBukkit()) if (n_format_p_p == " ") "" else n_format_p_p.replace(
                "  ",
                " "
            ) else n_format_p_p
        tags["bprefix2"] =
            if (forceRemoveDoubleSpacesFromBukkit()) if (n_format_p == " ") "" else n_format_p.replace(
                "  ",
                " "
            ) else n_format_p
        tags["bsuffix"] =
            if (forceRemoveDoubleSpacesFromBukkit()) if (n_format_s == " ") "" else n_format_s.replace(
                "  ",
                " "
            ) else n_format_s
        tags["server"] = getMessageManager().getMessage("bungeecord_server")
        tags["time_hour"] = Integer.toString(Calendar.getInstance()[Calendar.HOUR_OF_DAY])
        tags["time_min"] = Integer.toString(Calendar.getInstance()[Calendar.MINUTE])
        tags["time_sec"] = Integer.toString(Calendar.getInstance()[Calendar.SECOND])
        tags["date_day"] = Integer.toString(Calendar.getInstance()[Calendar.DAY_OF_MONTH])
        tags["date_month"] = Integer.toString(Calendar.getInstance()[Calendar.MONTH])
        tags["date_year"] = Integer.toString(Calendar.getInstance()[Calendar.YEAR])
        if (!block_chat) {
            tags["prefix"] = tag(chat.getPlayerPrefix(sender))
            tags["suffix"] = tag(chat.getPlayerSuffix(sender))
            tags["groupprefix"] =
                tag(chat.getGroupPrefix(sender.world, chat.getPrimaryGroup(sender)))
            tags["groupsuffix"] =
                tag(chat.getGroupSuffix(sender.world, chat.getPrimaryGroup(sender)))
            for (g in chat.getPlayerGroups(sender)) {
                tags[g.toLowerCase() + "prefix"] = tag(chat.getGroupPrefix(sender.world, g))
                tags[g.toLowerCase() + "suffix"] = tag(chat.getGroupSuffix(sender.world, g))
            }
        }
        val ttt = textToTag()
        if (ttt.size > 0) {
            val p = HashSet<Player>()
            p.add(sender)
            var i = 1
            for (n in ttt.keys) {
                var tag = ""
                tag = try {
                    bukkit_format.split("°" + i + "º°").toTypedArray()[1].split("°" + (i + 1) + "º°").toTypedArray()[0]
                } catch (e: Exception) {
                    ""
                }
                tags[n] = tag
                i++
            }
        }
        val e = ChatMessageEvent(
            c,
            sender,
            message,
            format(c.format),
            c.format,
            bukkit_format,
            recipients,
            tags,
            cancelled
        )
        val effectiveGastou = gastou
        Bukkit.getScheduler().runTask(plugin, Runnable {
            Bukkit.getPluginManager().callEvent(e)
            realMessage0(e, c, effectiveGastou)
        })
    }

    fun performTell(player: FocaPlayer, message: String)
    {
        // TODO
    }

    fun performMessage(player: FocaPlayer, message: String)
    {
        // TODO
    }

    fun checkChannel(string: String): Channel?
    {
        // TODO
        return null
    }

    private fun realMessage0(e: ChatMessageEvent, c: Channel, gastou: Boolean) {
        if (e.isCancelled) {
            return
        }
        val sender = e.sender
        val message = e.message
        var completa = e.format
        if (blockRepeatedTags()) {
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
        }
        for (n in e.tags) completa =
            completa.replace("{$n}", ChatColor.translateAlternateColorCodes('&', e.getTagValue(n)))
        completa = completa.replace("{msg}", translateAlternateChatColorsWithPermission(sender, message)!!)
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
                    sender.sendMessage(getMessageManager().getMessage("special"))
                }
            }
        }
        for (p in getPlayerManager().getOnlineSpys()) if (!e.recipients.contains(p)) {
            p.sendMessage(
                ChatColor.translateAlternateColorCodes(
                    '&', getFormat("spy").replace(
                        "{msg}",
                        ChatColor.stripColor(completa)!!
                    )
                )
            )
        }
        if (gastou) {
            if (c.showCostMessage()) {
                sender.sendMessage(
                    getMessageManager().getMessage("message9")
                        .replace("@money", java.lang.Double.toString(c.costPerMessage))
                )
            }
        }
        if (logToBukkit()) {
            Bukkit.getConsoleSender().sendMessage(completa)
        }
    }

    fun otherMessage(c: Channel, message: String?) {
        val recipients: MutableSet<Player> = HashSet()
        for (p in Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("channel." + c.name.toLowerCase() + ".chat") || p.hasPermission("admin")) {
                recipients.add(p)
            }
        }
        val recipients2: MutableSet<Player> = HashSet()
        recipients2.addAll(recipients)
        for (p in recipients2) {
            if (getIgnoreManager().hasPlayerIgnoredChannel(p, c)) {
                recipients.remove(p)
                continue
            }
            if (c.isFocusNeeded) {
                if (getPlayerManager().getPlayerFocusedChannel(p) !== c) {
                    recipients.remove(p)
                }
            }
        }

        for (p in recipients) p.sendMessage(message!!)
        if (logToBukkit()) {
            Bukkit.getConsoleSender().sendMessage(message!!)
        }
    }

    private fun tag(tag: String?): String {
        return tag ?: ""
    }

    private var logToBukkit = false
    private var blockRepeatedTags = false
    private var showNoOneHearsYou = false
    private var forceRemoveDoubleSpacesFromBukkit = false
    private var sendFakeMessageToChat = false
    private var blockShortcutsWhenCancelled = false
    private var maintainSpyMode = false
    private var defaultChannel: Channel? = null
    private val formats = HashMap<String, String>()
    private val pm_formats = HashMap<String, String>()
    private val text_to_tag = HashMap<String, String>()

    private var cm: ChannelManager? = null
    private var pm: PlayerManager? = null
    private var mm: MessageManager? = null
    private var im: IgnoreManager? = null
    private var pmm: PrivateMessageManager? = null
    private var dm: DelayManager? = null
    private var mum: MuteManager? = null

    fun getChannelManager(): ChannelManager? {
        return cm
    }

    fun getPlayerManager(): PlayerManager? {
        return pm
    }

    fun getMessageManager(): MessageManager? {
        return mm
    }

    fun getIgnoreManager(): IgnoreManager? {
        return im
    }

    fun getPrivateMessageManager(): PrivateMessageManager? {
        return pmm
    }

    fun getDelayManager(): DelayManager? {
        return dm
    }

    fun getMuteManager(): MuteManager? {
        return mum
    }

    fun getDefaultChannel(): Channel? {
        return defaultChannel
    }

    fun logToBukkit(): Boolean {
        return logToBukkit
    }

    fun blockRepeatedTags(): Boolean {
        return blockRepeatedTags
    }

    fun showNoOneHearsYou(): Boolean {
        return showNoOneHearsYou
    }

    fun forceRemoveDoubleSpacesFromBukkit(): Boolean {
        return forceRemoveDoubleSpacesFromBukkit
    }

    fun sendFakeMessageToChat(): Boolean {
        return sendFakeMessageToChat
    }

    fun blockShortcutsWhenCancelled(): Boolean {
        return blockShortcutsWhenCancelled
    }

    fun maintainSpyMode(): Boolean {
        return maintainSpyMode
    }

    fun getPlugin(): Plugin? {
        return plugin
    }

    fun format(msg: String): String? {
        var msg = msg
        for (f in formats.keys) msg = msg.replace("{$f}", formats[f]!!)
        return msg
    }

    fun getFormat(base_format: String): String? {
        return formats[base_format.toLowerCase()]
    }

    fun getPrivateMessageFormat(format: String): String? {
        return pm_formats[format.toLowerCase()]
    }

    fun textToTag(): HashMap<String, String>? {
        val h = HashMap<String, String>()
        h.putAll(text_to_tag)
        return h
    }

    fun load(all: Boolean) {
        plugin = Bukkit.getPluginManager().getPlugin("Legendchat")
        if (!all) {
            cm = ChannelManager()
            pm = PlayerManager()
            mm = MessageManager()
            im = IgnoreManager()
            pmm = PrivateMessageManager()
            dm = DelayManager()
            mum = MuteManager()
            return
        }
        val fc = Bukkit.getPluginManager().getPlugin("Legendchat")!!
            .config
        defaultChannel =
            getChannelManager().getChannelByName(fc.getString("default_channel", "local")!!.toLowerCase())
        logToBukkit = fc.getBoolean("log_to_bukkit", false)
        blockRepeatedTags = fc.getBoolean("block_repeated_tags", true)
        showNoOneHearsYou = fc.getBoolean("show_no_one_hears_you", true)
        forceRemoveDoubleSpacesFromBukkit = fc.getBoolean("force_remove_double_spaces_from_bukkit", true)
        sendFakeMessageToChat = fc.getBoolean("send_fake_message_to_chat", true)
        blockShortcutsWhenCancelled = fc.getBoolean("block_shortcuts_when_cancelled", true)
        maintainSpyMode = fc.getBoolean("maintain_spy_mode", false)
        formats.clear()
        pm_formats.clear()
        for (f in fc.getConfigurationSection("format")!!.getKeys(false)) formats[f.toLowerCase()] =
            fc.getString("format.$f")!!
        for (f in fc.getConfigurationSection("private_message_format")!!.getKeys(false)) pm_formats[f.toLowerCase()] =
            fc.getString("private_message_format.$f")!!
        for (f in fc.getStringList("text_to_tag")) {
            val s = f.split(";").toTypedArray()
            text_to_tag[s[0].toLowerCase()] = s[1]
        }
        mm!!.loadMessages(File(plugin.getDataFolder(), "Lang.yml"))
    }

    var econ: Economy? = null
    var chat: Chat? = null
    var block_econ = false
    var block_chat = false

    fun onEnable() {
        load(false)
        val file: File = File(plugin.getDataFolder(), "config.yml")
        if (!file.exists()) {
            try {
                plugin.saveResource("config_template.yml", false)
                val file2: File = File(plugin.getDataFolder(), "config_template.yml")
                file2.renameTo(File(plugin.getDataFolder(), "config.yml"))
            } catch (ignored: Exception) {
            }
        }
        try {
            if (!File(getDataFolder(), "Lang.yml").exists()) {
                plugin.saveResource("Lang.yml", false)
                plugin.getLogger().info("Saved Lang.yml")
            }
        } catch (e: Exception) {
        }
        val channels: File = File(getDataFolder(), "channels")
        if (!channels.exists()) {
            getChannelManager()
                .createPermanentChannel(Channel("global", "g", "{default}", "GRAY", true, false, 0, true, 0, 0, true))
            getChannelManager().createPermanentChannel(
                Channel(
                    "local",
                    "l",
                    "{default}",
                    "YELLOW",
                    true,
                    false,
                    60,
                    false,
                    0,
                    0,
                    true
                )
            )
        }
        getChannelManager().loadChannels()
        load(true)
        for (p in Bukkit.getOnlinePlayers()) getPlayerManager()
            .setPlayerFocusedChannel(p, getDefaultChannel(), false)
    }
}