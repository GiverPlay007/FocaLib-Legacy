package me.giverplay.focalib.chat

import me.giverplay.focalib.FocaLib
import me.giverplay.focalib.chat.channel.ChannelManager
import me.giverplay.focalib.utils.DependencyException
import me.giverplay.focalib.utils.Messages
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.plugin.RegisteredServiceProvider

class ChatManager(private val plugin: FocaLib)
{
    var channelManager: ChannelManager = ChannelManager(plugin)

    private var chat: Chat? = null

    init {
        if (!setupChat())
            throw DependencyException(Messages.msg("error.dependency.setupchat"))
    }

    private fun setupChat(): Boolean {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null)
            return false

        val rsp: RegisteredServiceProvider<Chat> = Bukkit.getServicesManager().getRegistration(Chat::class.java)
            ?: return false

        chat = rsp.provider
        return chat != null
    }
}