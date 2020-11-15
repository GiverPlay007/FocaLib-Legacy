package me.giverplay.focalib.command

import org.apache.commons.lang.Validate
import org.bukkit.command.TabCompleter

abstract class FocaCommand(name: String, allowConsole: Boolean) : TabCompleter
{
    val isAllowConsole: Boolean

    val name: String

    var isEnabled = true

    abstract fun execute(sender: CommandSource?, args: Array<out String>)

    val basePermission
        get() = "foca.command.$name"

    @JvmOverloads
    fun sendUsage(source: CommandSource, usage: String, example: String? = null)
    {
        Validate.notNull(source, "O CommandSource não pode ser nulo...")
        Validate.notNull(usage, "A instrução não pode ser nulo...")

        source.sendMessage("&cUso correto do comando: $usage.")

        if (example != null)
            source.sendMessage("&cExemplo: $example.")
    }

    override fun toString() = "FocaCommand: $name"

    init {
        Validate.notNull(name, "O nome do comando não pode ser nulo!")
        this.name = name
        isAllowConsole = allowConsole
    }
}