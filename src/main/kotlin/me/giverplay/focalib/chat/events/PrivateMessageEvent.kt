package me.giverplay.focalib.chat.events

import org.bukkit.command.CommandSender
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PrivateMessageEvent(
    private var sender: CommandSender,
    private var receiver: CommandSender,
    private var message: String
) :
    Event(), Cancellable {

    private var cancelled = false

    fun getSender(): CommandSender = sender

    fun setSender(sender: CommandSender?) {
        if (sender != null) this.sender = sender
    }

    fun getReceiver(): CommandSender = receiver

    fun setReceiver(receiver: CommandSender?) {
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
