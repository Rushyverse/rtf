package com.github.rushyverse.rtf.listener

import com.github.rushyverse.api.extension.event.cancelIf
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.weather.WeatherChangeEvent

class UndesirableEventListener : ListenerRTF() {

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) = event.cancelIf {
        isRTFWorld(event.entity.world)
    }

    @EventHandler
    fun onWeatherChange(event: WeatherChangeEvent) = event.cancelIf {
        event.toWeatherState() && isRTFWorld(event.world)
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) = event.cancelIf {
        isRTFWorld(event.player.world)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        if (isRTFWorld(player.world) && player.lastDamageCause?.cause == EntityDamageEvent.DamageCause.FALL) {
            event.drops.clear()
        }
    }
}
