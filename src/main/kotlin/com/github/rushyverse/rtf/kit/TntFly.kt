package com.github.rushyverse.rtf.kit

import com.github.rushyverse.api.extension.ItemStack
import com.github.rushyverse.api.game.GameState
import com.github.rushyverse.rtf.client.ClientRTF
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.koin.core.qualifier.named

class TntFly : KitFeature("tntfly") {

    override fun item() = ItemStack {
        type = Material.TNT
        named("Tntfly")
    }

    override fun onGiveKit(client: ClientRTF) {
        client.player?.inventory?.addItem(item())
    }

    fun createTNTFlyItem(): ItemStack {
        val item = ItemStack(Material.TNT)
        val meta: ItemMeta = item.itemMeta!!
        meta.setDisplayName("${ChatColor.RED}TNT Fly")
        item.itemMeta = meta
        return item
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        val block = event.block
        if (event.isCancelled) return
        if (!isRTFWorld(block.world)) return
        if (games.getGame(block.world)?.state() != GameState.STARTED)
            return

        if (block.type == Material.TNT) {
            event.isCancelled = true // Prevent default block placement

            // Spawn a primed TNT with a delay of 60 ticks (3 seconds)
            val tnt = block.world.spawnEntity(block.location.add(0.5, 0.0, 0.5), EntityType.PRIMED_TNT) as TNTPrimed
            tnt.fuseTicks = 60

            // Apply damage resistance to the player
            player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 70, 4, true, false, false))

            // Create an armor stand for the hologram
            val hologram = createHologram(block.location.add(0.5, 1.5, 0.5), "3")

            // Schedule the countdown and player launch
            for (i in 1..3) {
                val countdown = 3 - i
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    if (tnt.isValid) {
                        hologram.customName = countdown.toString()
                        if (countdown == 0) {
                            hologram.remove()
                            val explosionLocation = tnt.location
                            val players = explosionLocation.world.getNearbyEntities(explosionLocation, 5.0, 5.0, 5.0)
                                .filterIsInstance<Player>()
                            for (p in players) {
                                val direction = p.location.toVector().subtract(explosionLocation.toVector()).normalize()
                                val launchVector = direction.multiply(1.5).setY(1.5)
                                p.velocity = launchVector
                            }
                        }
                    }
                }, (20L * i))
            }
        }
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        if (event.entity.type == EntityType.PRIMED_TNT) {
            event.blockList().clear() // Prevent block damage
        }
    }

    private fun createHologram(location: Location, text: String): ArmorStand {
        val hologram = location.world.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand
        hologram.isVisible = false
        hologram.isCustomNameVisible = true
        hologram.customName(text(text))
        hologram.isMarker = true
        hologram.isSmall = true
        hologram.setGravity(false)
        return hologram
    }
}