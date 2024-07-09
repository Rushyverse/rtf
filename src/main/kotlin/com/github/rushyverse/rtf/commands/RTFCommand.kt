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
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

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

            // OP-ONLY
            subcommand("create") {
                withArguments(IntegerArgument("gameID"))
                withPermission("rtf.command.create")

                anyExecutor { commandSender, args ->
                    val gameIndex = args[0] as Int
                    var game = games.getGame(gameIndex)

                    plugin.launch {
                        if (game == null) {
                            game = games.createAndSave(gameIndex)
                        } else {
                            commandSender.sendMessage("game.already.exists")
                            return@launch
                        }
                        if (commandSender is Player) {
                            game?.clientSpectate(clients.getClient(commandSender) as ClientRTF)
                        }
                    }
                }
            }

            // OP-ONLY
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

            // OP-ONLY
            subcommand("start") {
                withPermission("rtf.command.start")
                withOptionalArguments(StringArgument("force"))

                playerExecutor { player, args ->
                    val game = games.getGame(player.world) ?: return@playerExecutor
                    val force = args[0] as String?

                    if (game.state() == GameState.WAITING) {
                        val forceStart= force == "force"
                        plugin.launch {
                            game.start(forceStart)
                        }
                    } else {
                        player.sendMessage("The game has already started.")
                    }
                }
            }

            // OP-ONLY
            subcommand("end") {
                withPermission("rtf.end")
                withArguments(IntegerArgument("gameID"))

                anyExecutor { player, args ->
                    val gameId = args[0] as Int
                    player.sendMessage("game.ask.delete")
                    val game = games.getGame(gameId)
                    plugin.launch {
                        game?.end(null)
                        player.sendMessage("game.deleted")
                    }
                }
            }

            // OP-ONLY
            subcommand("win") {
                withPermission("rtf.win")
                stringArgument("team")

                playerExecutor { player, arg ->
                    val teamName = arg[0].toString()
                    val type = TeamType.valueOf(teamName.uppercase())
                    val game = games.getGame(player.world) ?: return@playerExecutor

                    if (game.state() != GameState.STARTED) {
                        player.sendMessage("La game n'a pas commencé.")
                        return@playerExecutor
                    }

                    val team = game.teams.firstOrNull { it.type == type }

                    if (team == null) {
                        player.sendMessage("The team '$teamName' does not exist.")
                        return@playerExecutor
                    }

                    plugin.launch { game.end(team) }
                }
            }

            subcommand("spectate") {
                withArguments(IntegerArgument("gameID"))
                // withPermission("rtf.command.spectate")

                playerExecutor { player, args ->
                    val gameIndex = args[0] as Int
                    val game = games.getGame(gameIndex)

                    if (game == null) {
                        player.sendMessage("game.not.exists")
                    } else {
                        plugin.launch { game.clientSpectate(clients.getClient(player) as ClientRTF) }
                    }
                }
            }

            subcommand("join") {
                // withPermission("rtf.command.join")

                playerExecutor { player, _ ->
                    val game = games.getGame(player.world) ?: return@playerExecutor

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

            subcommand("kits") {
                //  withPermission("rtf.kits")

                playerExecutor { player, args ->
                    val game = games.getGame(player.world)
                    if (game != null) {
                        plugin.launch {
                            plugin.kitsGui.open(clients.getClient(player))
                        }
                    }
                }
            }
        }
    }
}
