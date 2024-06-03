package com.github.rushyverse.rtf.listener

import com.github.rushyverse.rtf.client.ClientRTF
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent

class AuthenticationListener : ListenerRTF() {

    @EventHandler
    suspend fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val world = player.world
        val game = games.getGame(world)

        game?.apply {
            clientLeave(clients.getClient(player) as ClientRTF)
        }

        // Save the rtf client stats to the database

        event.quitMessage(null)
    }
}
