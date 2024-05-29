package com.github.rushyverse.rtf.kit

import com.github.rushyverse.api.extension.ItemStack
import com.github.rushyverse.api.game.GameState
import com.github.rushyverse.rtf.client.ClientRTF
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.util.Vector
import org.koin.core.qualifier.named

class AutoBridge : KitFeature("autobridge") {
    override fun item() = ItemStack {
        type = Material.REPEATER
        named("Autobridge")
    }

    override fun onGiveKit(client: ClientRTF) {
        client.player?.inventory?.addItem(item())
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        val block = event.block
        if (event.isCancelled) return
        if (!isRTFWorld(block.world)) return
        if (games.getGame(block.world)?.state() == GameState.STARTED) {
            if (block.type == Material.REPEATER) {
                createBridge(block, Material.SMOOTH_SANDSTONE, 6, player.location.direction)
            }
        }
    }

    private fun createBridge(startBlock: Block, material: Material, length: Int, direction: Vector) {
        val horizontalDirections = listOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
        val delay = 3L // Delay in ticks (20 ticks = 1 second)

        // Check if the direction is horizontal
        val blockFaceDirection = direction.toHorizontalBlockFace()
        if (blockFaceDirection !in horizontalDirections) {
            return  // Exit function if direction is not horizontal
        }

        // Loop through each block in the bridge
        for (i in 1..length) {
            // Schedule the block placement with a delay
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                val nextBlock = startBlock.getRelative(blockFaceDirection, i).getRelative(BlockFace.DOWN)
                if (nextBlock.type == Material.AIR) {
                    nextBlock.type = material
                }
            }, delay * i)
        }
    }

    // Extension function to convert Vector to horizontal BlockFace
    private fun Vector.toHorizontalBlockFace(): BlockFace {
        val directions = listOf(
            BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
        )
        var closestDirection = BlockFace.NORTH
        var closestDot = Double.NEGATIVE_INFINITY
        for (direction in directions) {
            val dot = this.dot(direction.direction)
            if (dot > closestDot) {
                closestDot = dot
                closestDirection = direction
            }
        }
        return closestDirection
    }
}