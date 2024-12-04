package com.github.rushyverse.rtf.game

import com.github.rushyverse.api.extension.copy
import com.github.rushyverse.api.world.CubeArea
import com.github.rushyverse.rtf.client.ClientRTF
import com.github.rushyverse.rtf.config.TeamRTFConfig
import org.bukkit.World

class TeamRTF(
    val config: TeamRTFConfig,
    val world: World
) {

    val type get() = config.type
    val spawnPoint = config.spawnPoint.copy(world = world)
    val spawnCuboid = CubeArea(
        config.spawnCuboid.min.copy(world = world),
        config.spawnCuboid.max.copy(world = world)
    )
    val flagPoint = config.flagPoint.copy(world = world)
    val flagCuboid = CubeArea(
        config.flagCuboid.min.copy(world = world),
        config.flagCuboid.max.copy(world = world)
    )

val flagMaterial = config.flagMaterial

val members = mutableListOf<ClientRTF>()
var flagStolenState = false
    set(value) {
        field = value
        if (!value) {
            flagPoint.block.type = flagMaterial
        }
    }
}