package com.github.rushyverse.rtf.kit

import com.github.rushyverse.api.extension.ItemStack
import com.github.rushyverse.api.game.GameState
import com.github.rushyverse.rtf.client.ClientRTF
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector
import org.koin.core.qualifier.named

class LadderFly : KitFeature("ladderfly") {

    override fun item() = ItemStack {
        type = Material.LADDER
        named("Ladderfly")
    }

    override fun onGiveKit(client: ClientRTF) {
        client.player?.inventory?.addItem(item())
    }

    private val ladderflyCooldown = mutableMapOf<Player, Long>()

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val clickedBlock = event.clickedBlock ?: return
        if (event.material != Material.LADDER) return
        if (event.isCancelled) return
        if (!isRTFWorld(player.world)) return

        if (games.getGame(player.world)?.state() == GameState.STARTED)

        if (isLadderOnSide(event.blockFace)) {
            applyLadderfly(player)
        }
    }

    private fun isLadderOnSide(face: BlockFace): Boolean {
        // Check if the ladder is placed on the side of a block
        return face != BlockFace.UP && face != BlockFace.DOWN
    }

    private fun applyLadderfly(player: Player) {
        if (ladderflyCooldown.containsKey(player)) return // Check if cooldown is active
        ladderflyCooldown[player] = System.currentTimeMillis() // Set cooldown start time

        val velocity = Vector(0.0, 1.0, 0.0) // Upward velocity
        player.velocity = velocity

        // Schedule cooldown expiration
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            ladderflyCooldown.remove(player)
        }, 20L) // 20 ticks cooldown (1 second)
    }

}