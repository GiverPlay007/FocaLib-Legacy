package me.giverplay.focalib.command

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
            source.sendMessage("&cEste comando está desabilitado!")
            return true
        }

        if(!sender.hasPermission(cmd.basePermission))
        {
            source.sendMessage("&cSem permissão para executar este comando...")
            return true;
        }

        if(!cmd.isAllowConsole && !source.isPlayer)
        {
            source.sendMessage("&cEste comando só poder ser executado por jogadores...")
            return true;
        }

        try {
            cmd.execute(source, args)
        }
        catch (ex: Throwable) {
            source.sendMessage("&cErro interno na execução do comando... Por favor, contate um administrador!")
            plugin.logger.log(Level.WARNING, "O comando ${cmd.name} não foi executado corretamente", ex);
        }

        return true
    }

    fun registerCommand(command: FocaCommand)
    {
        Validate.notNull(command, "O comando não deve ser nulo!")
        Validate.isTrue(!commands.containsKey(command.name), "Este comando já foi registrado!");

        val cmd = plugin.getCommand(command.name)
            ?: throw IllegalStateException("O comando ${command.name} não foi registrado no plugin.yml!")

        cmd.setExecutor(this)
        cmd.tabCompleter = command
        commands[command.name] = command
    }

    fun toggleCommand(command: FocaCommand, enabled: Boolean)
    {
        Validate.notNull(command, "O comando não pode ser nulo!")
        command.isEnabled = enabled
        val status = if (enabled) "habilitado" else "desabilitado"
        plugin.logger.info("O comando ${command.name} foi $status")
    }

    fun unregisterCommand(command: FocaCommand)
    {
        Validate.notNull(command, "O comando não pode ser nulo!");

        val cmd = plugin.getCommand(command.name) ?: return
        cmd.setExecutor(null)
        cmd.tabCompleter = null
        commands.remove(command.name)
        plugin.logger.info("O comando ${command.name} foi desregistrado")
    }
}