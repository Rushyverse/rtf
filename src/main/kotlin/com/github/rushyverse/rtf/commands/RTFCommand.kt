package com.github.rushyverse.rtf.commands

import com.github.rushyverse.api.game.GameState
import com.github.rushyverse.api.game.team.TeamType
import com.github.rushyverse.api.koin.inject
import com.github.rushyverse.api.player.ClientManager
import com.github.rushyverse.api.translation.Translator
import com.github.rushyverse.rtf.RTFPlugin
import com.github.rushyverse.rtf.client.ClientRTF
import com.github.rushyverse.rtf.game.GameManager
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor

class RTFCommand(
    private val plugin: RTFPlugin
) {

    private val clients: ClientManager by inject(RTFPlugin.ID)
    private val games: GameManager by inject(RTFPlugin.ID)
    private val translator: Translator by inject(RTFPlugin.ID)

    /**
     * rtf - spectate <id>
     * rtf - join - only in game
     * rtf - start - only in game
     */
    suspend fun register() {
        commandAPICommand("rtf") {

            subcommand("create") {
                withArguments(IntegerArgument("gameID"))
                withPermission("rtf.command.create")
                playerExecutor { player, args ->
                    val gameIndex = args[0] as Int
                    var game = games.getGame(gameIndex)

                    plugin.launch {

                        if (game == null) {
                            game = games.createAndSave(gameIndex)
                        } else {
                            player.sendMessage("game.already.exists")
                            return@launch
                        }

                        game?.clientSpectate(clients.getClient(player) as ClientRTF)
                    }
                }
            }

            subcommand("list") {
                withPermission("rtf.command.list")
                playerExecutor { player, _ ->
                    val length = games.games.size
                    player.sendMessage("List of games ($length):")
                    for (game in games.games) {
                        player.sendMessage("#${game.id} - ${game.players.size}/${game.config.game.maxGames} - ${game.state()}")
                    }
                }
            }

            subcommand("spectate") {
                withArguments(IntegerArgument("gameID"))
                withPermission("rtf.command.spectate")
                playerExecutor { player, args ->
                    val gameIndex = args[0] as Int
                    val game = games.getGame(gameIndex)

                    if (game == null) {
                        player.sendMessage("game.not.exists")
                    }
                }
            }

            subcommand("join") {
                withPermission("rtf.command.join")
                playerExecutor { player, _ ->
                    val game = games.getByWorld(player.world) ?: return@playerExecutor

                    plugin.launch {
                        val client = clients.getClient(player) as ClientRTF

                        if (game.getClientTeam(client) != null) {
                            client.send(
                                text(
                                    translator.get("join.already.in.team", client.lang().locale),
                                    NamedTextColor.RED
                                )
                            )
                            return@launch
                        }

                        game.clientJoin(client)
                    }
                }
            }

            subcommand("start") {
                withPermission("rtf.command.start")
                playerExecutor { player, _ ->
                    val game = games.getByWorld(player.world) ?: return@playerExecutor

                    if (game.state() != GameState.STARTED) {
                        plugin.launch { game.start(true) }
                    } else {
                        player.sendMessage("The game is already started.")
                    }
                }
            }

            // DEV
            subcommand("win") {
                withPermission("rtf.win")
                stringArgument("team")
                playerExecutor { player, arg ->
                    val teamName = arg[0].toString()
                    val type = TeamType.valueOf(teamName.uppercase())
                    val game = games.getByWorld(player.world) ?: return@playerExecutor
                    val team = game.teams.firstOrNull { it.type == type }

                    if (team == null) {
                        player.sendMessage("The team '$teamName' does not exist.")
                        return@playerExecutor
                    }

                    plugin.launch { game.end(team) }
                }
            }

            subcommand("end") {
                withPermission("rtf.end")
                withArguments(IntegerArgument("gameID"))
                playerExecutor { player, args ->
                    val gameId = args[0] as Int
                    player.sendMessage("game.ask.delete")
                    val game = games.getGame(gameId)
                    plugin.launch {
                        game?.end(null)
                        player.sendMessage("game.deleted")
                    }
                }
            }
        }
    }
}
