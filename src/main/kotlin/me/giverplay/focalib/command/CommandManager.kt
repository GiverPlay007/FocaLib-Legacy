package me.giverplay.focalib.command

import me.giverplay.focalib.utils.Messages.Companion.msg

import me.giverplay.focalib.FocaPlugin
import org.apache.commons.lang.Validate
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.util.logging.Level

class CommandManager(plugin: FocaPlugin) : CommandExecutor
{
    private val plugin: FocaPlugin = plugin
    private val commands = HashMap<String, FocaCommand>();

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean
    {
        val cmd = commands[command.name]
        val source = CommandSource(sender);

        if(cmd == null) return false;

        if (!cmd.isEnabled)
        {
            source.sendMessage(msg("error.command-disabled"))
            return true
        }

        if(!sender.hasPermission(cmd.basePermission))
        {
            source.sendMessage(msg("error.no-perm"))
            return true;
        }

        if(!cmd.isAllowConsole && !source.isPlayer)
        {
            source.sendMessage(msg("error.console"))
            return true;
        }

        try {
            cmd.execute(source, args)
        }
        catch (ex: Throwable) {
            source.sendMessage(msg("error.unexpected"))
            plugin.logger.log(Level.WARNING, msg("error.internal.unexpected", cmd.name), ex);
        }

        return true
    }

    fun registerCommand(command: FocaCommand)
    {
        Validate.notNull(command, msg("error.internal.nullcommand"))
        Validate.isTrue(!commands.containsKey(command.name), msg("error.internal"));

        val cmd = plugin.getCommand(command.name)
            ?: throw IllegalStateException(msg("error.internal.cmdnotfound", command.name))

        cmd.setExecutor(this)
        cmd.tabCompleter = command
        commands[command.name] = command
        plugin.logger.info(msg("debug.internal.cmdtoggle", msg("info.internal.reg")))
    }

    fun registerCommands(vararg cmds: FocaCommand) = cmds.forEach { c -> registerCommand(c) }

    fun toggleCommand(command: FocaCommand, enabled: Boolean)
    {
        Validate.notNull(command, msg("error.internal.nullcommand"))
        command.isEnabled = enabled
        val status = if (enabled) msg("info.internal.enabled") else msg("info.internal.disabled")
        plugin.logger.info(msg("debug.internal.cmdtoggle", command.name, status))
    }

    fun unregisterCommand(command: FocaCommand)
    {
        Validate.notNull(command, msg("error.internal.nullcommand"));

        val cmd = plugin.getCommand(command.name) ?: return
        cmd.setExecutor(null)
        cmd.tabCompleter = null
        commands.remove(command.name)
        plugin.logger.info(msg("debug.internal.cmdtoggle", msg("info.internal.unreg")))
    }
}