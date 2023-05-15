package com.cocahonka.comfywhitelist.commands

import com.cocahonka.comfywhitelist.ComfyWhitelist
import com.cocahonka.comfywhitelist.commands.sub.*
import com.cocahonka.comfywhitelist.config.general.GeneralConfig
import com.cocahonka.comfywhitelist.config.message.MessageConfig
import com.cocahonka.comfywhitelist.config.message.MessageTagResolvers
import com.cocahonka.comfywhitelist.storage.Storage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

/**
 * Handles subcommands and their execution for the ComfyWhitelist plugin.
 *
 * @param storage The [Storage] instance to interact with whitelist data.
 * @param generalConfig The [GeneralConfig] instance to manage plugin configuration.
 * @param plugin The [ComfyWhitelist] plugin instance.
 */
class CommandHandler(
    storage: Storage,
    generalConfig: GeneralConfig,
    plugin: ComfyWhitelist,
) : CommandExecutor {

    companion object {
        const val identifier = "comfywhitelist"
        const val usage = "/comfywl <command>"
        val aliases = listOf("comfywl")
    }

    val subCommands: List<SubCommand>

    init {
        val commands = listOf(
            AddCommand(storage),
            RemoveCommand(storage),
            ListCommand(storage),
            EnableCommand(generalConfig),
            DisableCommand(generalConfig),
            ClearCommand(storage),
            ReloadCommand(plugin),
        )
        val helpCommand = HelpCommand(commands)
        subCommands = commands + helpCommand
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (args.isNullOrEmpty()) {
            val message = MessageConfig.invalidUsage
            val messageComponent = MiniMessage.miniMessage().deserialize(
                message,
                MessageTagResolvers.warning,
                MessageTagResolvers.insertUsage(usage),
            )
            sender.sendMessage(messageComponent)
            return false
        }

        val subCommandName = args[0]
        val subCommand = subCommands.find { it.identifier.equals(subCommandName, true) }
        if (subCommand == null) {
            val message = MessageConfig.unknownSubcommand
            val messageComponent = MiniMessage.miniMessage().deserialize(
                message,
                MessageTagResolvers.warning,
            )
            sender.sendMessage(messageComponent)
            return false
        }

        if (sender !is ConsoleCommandSender && !sender.hasPermission(subCommand.permission)) {
            val message = MessageConfig.noPermission
            val messageComponent = MiniMessage.miniMessage().deserialize(
                message,
                MessageTagResolvers.warning,
            )
            sender.sendMessage(messageComponent)
            return false
        }

        return subCommand.execute(sender, args.drop(1).toTypedArray())
    }
}