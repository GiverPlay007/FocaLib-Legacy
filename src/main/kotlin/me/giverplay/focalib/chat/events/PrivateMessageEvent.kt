package me.giverplay.focalib.chat.events

import me.giverplay.focalib.player.FocaPlayer
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PrivateMessageEvent(
    private var sender: FocaPlayer,
    private var receiver: FocaPlayer,
    private var message: String
) :
    Event(), Cancellable {

    private var cancelled = false

    fun getSender(): FocaPlayer = sender

    fun setSender(sender: FocaPlayer?) {
        if (sender != null) this.sender = sender
    }

    fun getReceiver(): FocaPlayer = receiver

    fun setReceiver(receiver: FocaPlayer?) {
        if (receiver != null) this.receiver = receiver
    }

    fun getMessage(): String = message

    fun setMessage(message: String?) {
        if (message == null) this.message = "" else this.message = message
    }

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        val handlerList = HandlerList()
    }
}
