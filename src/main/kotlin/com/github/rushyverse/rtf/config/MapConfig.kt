package com.github.rushyverse.rtf.config

import com.github.rushyverse.api.game.team.TeamType
import com.github.rushyverse.api.world.CubeArea
import kotlinx.serialization.*
import org.bukkit.Location
import org.bukkit.Material

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
@SerialName("map")
data class MapConfig(
    val worldTemplateName: String,
    val minPlayers: Int,
    val mapCuboid: CubeArea,
    val allowedBlocks: Set<Material>,
    val teams: List<TeamRTFConfig>
)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class TeamRTFConfig(
    val type: TeamType,
    @Contextual val spawnPoint: Location,
    val spawnCuboid: CubeArea,
    @Contextual val flagPoint: Location,
    val flagCuboid: CubeArea,
    val flagMaterial: Material,
)
