package me.giverplay.focalib.chat.events

import me.giverplay.focalib.chat.Channel
import me.giverplay.focalib.chat.MessageManager
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ChatMessageEvent(
    manager: MessageManager,
    ch: Channel,
    private var sender: Player,
    private var message: String,
    format: String,
    base_format: String,
    bukkit_format: String,
    recipients: Set<Player>?,
    tags: HashMap<String, String>?,
    cancelled: Boolean
) :
    Event(), Cancellable {
    private val TAGS = HashMap<String, String>()
    private val RECIPIENTS: MutableSet<Player> = HashSet()
    val bukkitFormat: String
    val baseFormat: String
    val channel: Channel
    private val MANAGER: MessageManager
    private var format: String
    private var cancelled: Boolean

    fun getMessage(): String = message

    fun setMessage(message: String?) {
        if (message == null) this.message = "" else this.message = message
    }

    fun getFormat(): String = format

    fun setFormat(format: String?) {
        if (format != null) this.format = format
    }

    fun getSender(): Player = sender

    fun setSender(sender: Player?) {
        if (sender != null) this.sender = sender
    }

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    val recipients: Set<Player>
        get() = RECIPIENTS

    fun baseFormatToFormat(baseFormat: String?): String? = MANAGER.format(baseFormat!!)

    val tags: List<String>
        get() = ArrayList(TAGS.keys)

    fun setTagValue(tag: String?, value: String?): Boolean {
        var tag = tag
            ?: return false

        tag = tag.toLowerCase()

        if (!TAGS.containsKey(tag))
            return false

        TAGS.remove(tag)

        TAGS[tag] = value ?: ""
        return true
    }

    fun getTagValue(tag: String?): String? = if (tag == null) null else TAGS[tag.toLowerCase()]

    fun addTag(tag: String?, value: String?) {
        var tag = tag
        var value = value

        if (tag == null)
            return

        tag = tag.toLowerCase()

        if (TAGS.containsKey(tag))
            return

        if (value == null)
            value = ""

        TAGS[tag] = value
    }

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        val handlerList = HandlerList()
    }

    init {
        RECIPIENTS.addAll(recipients!!)
        TAGS.putAll(tags!!)
        this.cancelled = cancelled
        channel = ch
        MANAGER = manager
        baseFormat = base_format
        bukkitFormat = bukkit_format
        this.format = ChatColor.translateAlternateColorCodes('&', format)

        for (i in format.indices)
            if (format[i] == '{') {
                val tag = format.substring(i + 1).split("}").toTypedArray()[0].toLowerCase()

                if (tag != "msg")
                    if (!TAGS.containsKey(tag))
                        TAGS[tag] = ""
            }
    }
}
