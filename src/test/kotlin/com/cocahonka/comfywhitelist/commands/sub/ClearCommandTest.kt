package com.cocahonka.comfywhitelist.commands.sub

import be.seeseemelk.mockbukkit.command.MessageTarget
import be.seeseemelk.mockbukkit.entity.PlayerMock
import com.cocahonka.comfywhitelist.commands.CommandTestBase
import com.cocahonka.comfywhitelist.config.message.Message
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ClearCommandTest : CommandTestBase() {
    
    private lateinit var clearCommand: ClearCommand
    private lateinit var anotherPlayer: PlayerMock
    private lateinit var label: String
    
    @BeforeEach
    override fun setUp() {
        super.setUp()
        clearCommand = ClearCommand(storage)
        label = clearCommand.identifier
        anotherPlayer = server.addPlayer()

        storage.addPlayer(player.name)
        storage.addPlayer(anotherPlayer.name)
    }

    private fun assertOnlyWhitelistClearedMessage(sender: MessageTarget) {
        assertEquals(
            sender.nextMessage(),
            Message.PlayerAdded.getDefault(locale)
        )
        sender.assertNoMoreSaid()
    }

    @Test
    fun `when console is sender`() {
        val result = handler.onCommand(
            sender = console,
            command = command,
            label = label,
            args = arrayOf(clearCommand.identifier)
        )

        assertTrue(result)
        assertStorageEmpty()
        assertOnlyWhitelistClearedMessage(console)
    }

    @Test
    fun `when player is sender without permission`() {
        val result = handler.onCommand(
            sender = player,
            command = command,
            label = label,
            args = arrayOf(clearCommand.identifier)
        )

        assertFalse(result)
        assertWhitelisted(player)
        assertWhitelisted(anotherPlayer)
        assertOnlyNoPermissionMessage(player)
    }

    @Test
    fun `when player is sender with permission`() {
        player.addAttachment(plugin, clearCommand.permission, true)
        val result = handler.onCommand(
            sender = player,
            command = command,
            label = label,
            args = arrayOf(clearCommand.identifier)
        )

        assertTrue(result)
        assertStorageEmpty()
        assertOnlyWhitelistClearedMessage(player)
    }

    @Test
    fun `when to many arguments`() {
        val result = handler.onCommand(
            sender = console,
            command = command,
            label = label,
            args = arrayOf(clearCommand.identifier)
        )

        assertFalse(result)
        assertWhitelisted(player)
        assertWhitelisted(anotherPlayer)
        assertOnlyInvalidUsageMessage(console, clearCommand.usage)
    }

}