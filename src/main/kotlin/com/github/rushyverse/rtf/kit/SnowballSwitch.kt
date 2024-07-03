package com.github.rushyverse.rtf.kit

import com.github.rushyverse.api.extension.ItemStack
import com.github.rushyverse.rtf.client.ClientRTF
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.koin.core.qualifier.named

class SnowballSwitch : KitFeature("snowballswitch") {
    override fun item() = ItemStack {
        type = Material.SNOWBALL
        named("Snowballswitch")
    }

    override fun onGiveKit(client: ClientRTF) {
        client.player?.inventory?.addItem(item())
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.damager.type == EntityType.SNOWBALL && event.entity is Player) {
            val snowball = event.damager as Snowball
            val hitPlayer = event.entity as Player
            val shooter = snowball.shooter

            if (event.isCancelled) return
            if (!isRTFWorld(snowball.world)) return

            if (shooter is Player) {
                val shooterLocation = shooter.location
                val hitPlayerLocation = hitPlayer.location

                // Swap the positions
                shooter.teleport(hitPlayerLocation)
                hitPlayer.teleport(shooterLocation)

                hitPlayer.sendMessage("${shooter.name} has switched places with you!")
                shooter.sendMessage("You have switched places with ${hitPlayer.name}!")
            }
        }
    }
}