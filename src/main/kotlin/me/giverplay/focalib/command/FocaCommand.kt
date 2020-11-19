package me.giverplay.focalib.command

import me.giverplay.focalib.utils.Messages
import org.apache.commons.lang.Validate
import org.bukkit.command.TabCompleter

abstract class FocaCommand(name: String, allowConsole: Boolean) : TabCompleter
{
    val isAllowConsole: Boolean
    val name: String
    var isEnabled: Boolean = true

    abstract fun execute(sender: CommandSource, args: Array<out String>)

    val basePermission
        get() = "foca.command.$name"

    @JvmOverloads
    fun sendUsage(source: CommandSource, usage: String, example: String? = null)
    {
        Validate.notNull(source, msg("error.internal.nullsource"))
        source.sendMessage(msg("info.usage", usage))

        if (example != null)
            source.sendMessage(msg("info.example", example))
    }

    override fun toString() = "FocaCommand: $name"

    init {
        Validate.notNull(name, msg("error.internal.nullcommand"))
        this.name = name
        isAllowConsole = allowConsole
    }

    companion object {

        fun msg(str: String, vararg args: String?): String? = Messages.msg(str, *args)

    }
}