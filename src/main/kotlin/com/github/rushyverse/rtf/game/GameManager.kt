package com.github.rushyverse.rtf.game

import com.github.rushyverse.api.game.GameData
import com.github.rushyverse.api.game.GameState
import com.github.rushyverse.api.game.SharedGameData
import com.github.rushyverse.api.koin.inject
import com.github.rushyverse.rtf.RTFPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import java.io.File
import kotlin.coroutines.CoroutineContext

class GameManager(
    private val plugin: RTFPlugin
) {

    val sharedGameData: SharedGameData by inject<SharedGameData>()
    val games: MutableList<Game> = mutableListOf()

    fun getGame(world: World): Game? {
        games.forEach {
            if (it.world == world) {
                return it
            }
        }

        return null
    }

    fun findNewGameIndex(): Int {
        return games.size + 1
    }

    suspend fun createAndSave(gameIndex: Int = findNewGameIndex()): Game {
        val worldName = "rtf$gameIndex"
        var world = plugin.server.getWorld(worldName)
        if (world == null) {
            world = createWorldFromTemplate(worldName)
            val mapConfig = plugin.configMaps[0].copy().apply {
                mapCuboid.apply {
                    min.world = world
                    max.world = world
                }
            }
            return Game(plugin, gameIndex, world, plugin.config, mapConfig).also { game ->
                games.add(game)

                sharedGameData.apply {

                    val gameData = games.find { it.id == gameIndex }

                    if (gameData == null) {
                        saveUpdate(game.data)
                    } else {
                        gameData.state = GameState.WAITING
                        game.data = gameData
                        callOnChange()
                    }
                }
            }
        } else {
            throw IllegalStateException("A game already exists for this world $worldName")
        }
    }

    /**
     * Should be executed in [Dispatcher.IO] coroutine context.
     */
    private suspend fun createWorldFromTemplate(
        worldName: String,
        coroutineContext: CoroutineContext = Dispatchers.IO
    ): World {
        val templateWorld = File(plugin.mapsDir, "classic")
        val target = File(plugin.tempDir, worldName)
        val correctPath = target.path.replace("\\", "/")
        val creator = WorldCreator(correctPath)
        withContext(coroutineContext) {
            templateWorld.copyRecursively(target, true)
            creator.apply {
                type(WorldType.FLAT)
                environment(World.Environment.NORMAL)
                generateStructures(false)
            }
        }

        return plugin.server.createWorld(creator) ?: throw IllegalStateException("Can't create world for $worldName")
    }

    /**
     * Unloads and deletes the world of the given game.
     * After the file deletion, game is removed from the list
     * and from [SharedGameData].
     * This method also calls [SharedGameData.callOnChange] to update related games services.
     *
     * May not work properly if players are still present in the world.
     */
    suspend fun removeGameAndDeleteWorld(
        game: Game,
        coroutineContext: CoroutineContext = Dispatchers.IO
    ) {
        val world = game.world
        val file = world.worldFolder

        Bukkit.unloadWorld(world, false)

        withContext(coroutineContext) {
            file.deleteRecursively()
        }

        sharedGameData.apply {
            val data = game.data

            if (data.permanent){
                data.state =  GameState.NOT_STARTED
            }

            games.removeIf { it.id == game.id && !game.data.permanent }
            callOnChange()
        }

        games.remove(game)
    }

    fun getGame(gameIndex: Int) = games.firstOrNull { it.id == gameIndex }
}