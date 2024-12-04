package com.github.rushyverse.rtf.game

import com.github.rushyverse.api.extension.asComponent
import com.github.rushyverse.api.extension.withBold
import com.github.rushyverse.api.game.GameState
import com.github.rushyverse.api.game.team.TeamType
import com.github.rushyverse.api.koin.inject
import com.github.rushyverse.api.translation.Translator
import com.github.rushyverse.api.translation.getComponent
import com.github.rushyverse.rtf.RTFPlugin
import com.github.rushyverse.rtf.client.ClientRTF
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import java.util.*

object GameScoreboard {

    private val emptyLine = Component.empty()
    private val scoreboardTitle = "<gradient:yellow:gold:red>RushTheFlag"
        .asComponent().withBold()
    private val serverIpAddress = "<gradient:light_purple:dark_purple:red>play.rushy.space"
        .asComponent().withBold()

    private val translator: Translator by inject(RTFPlugin.ID)

    suspend fun update(
        client: ClientRTF,
        game: Game,
        timeFormatted: String = ""
    ) {
        val locale = client.lang().locale
        val lines = mutableListOf<Component>()
        val team = game.getClientTeam(client)
        val state = game.state()
        val stats = client.stats

        lines.add(emptyLine)
        lines.add(translateStateLine(timeFormatted, game, locale))
        lines.add(emptyLine)

        if (state == GameState.STARTED) {

            game.teams
                .forEach {
                    lines.add(translateTeamFlagLine(it, locale))
                }

            lines.add(emptyLine)
        }

        if (team != null) {
            lines.add(translateTeamLine(locale, team.type))

            if (state == GameState.STARTED) {
                lines.add(translateKillsLine(locale, stats.kills()))
                lines.add(translateDeathsLine(locale, stats.deaths()))
                lines.add(translateAttemptsLine(locale, stats.flagAttempts))
            }
        } else {
            lines.add(translateSpectateLine(locale))
            lines.add(translateJoinUsage(locale, "<yellow>/rtf join"))
        }

        lines.add(emptyLine)
        lines.add(serverIpAddress)

        client.scoreboard().apply {
            updateTitle(scoreboardTitle)
            updateLines(lines)
        }
    }


    private fun translateKillsLine(locale: Locale, kills: Int) =
        translateLine("kills", locale, arrayOf("<green>$kills"))

    private fun translateDeathsLine(locale: Locale, deaths: Int) =
        translateLine("deaths", locale, arrayOf("<red>$deaths"))

    private fun translateAttemptsLine(locale: Locale, attempts: Int) =
        translateLine("attempts", locale, arrayOf("<light_purple>$attempts"))

    private fun translateTeamLine(locale: Locale, type: TeamType): Component {
        val translatedName = type.name(translator, locale)
        val color = type.name.lowercase()

        return translateLine("team", locale, arrayOf("<$color>$translatedName"))
    }

    private fun translateLine(
        key: String,
        locale: Locale,
        args: Array<Any> = emptyArray(),
        color: NamedTextColor? = null
    ) =
        translator.getComponent("scoreboard.$key", locale, args).color(color ?: NamedTextColor.GRAY)

    private fun translateTeamFlagLine(team: TeamRTF, locale: Locale): Component {
        val key: String
        val stateColor: TextColor

        if (team.flagStolenState) {
            key = "flag.stolen"
            stateColor = NamedTextColor.GOLD
        } else {
            key = "flag.placed"
            stateColor = NamedTextColor.GREEN
        }

        val teamColor = team.type.name
        val teamName = team.type.name(translator)

        return translateLine(key, locale, arrayOf("<$teamColor>$teamName</$teamColor>"), stateColor)
    }

    private fun translateStateLine(timeFormatted: String, game: Game, locale: Locale) = when (game.state()) {
        GameState.NOT_STARTED -> translateLine("notStarted", locale)
        GameState.WAITING -> translateLine("waiting", locale, arrayOf(game.playersInTeams(), game.mapConfig.minPlayers))
        GameState.STARTING -> translateLine("starting", locale, arrayOf(timeFormatted))
        GameState.STARTED -> translateLine(
            "started",
            locale,
            arrayOf("<yellow>$timeFormatted"),
            NamedTextColor.LIGHT_PURPLE
        )
        GameState.ENDED -> translateLine(
            "ending", locale, arrayOf(
                "<${game.teamWon.type.name.lowercase()}>${game.teamWon.type.name(translator, locale)}"
            ),
            NamedTextColor.LIGHT_PURPLE
        )
            .withBold()
    }

    private fun translateJoinUsage(locale: Locale, joinCommandUsage: String) =
        translateLine("spectate.join.usage", locale, arrayOf(joinCommandUsage), NamedTextColor.LIGHT_PURPLE)

    private fun translateSpectateLine(locale: Locale) =
        translateLine("spectate.mode", locale, color = NamedTextColor.LIGHT_PURPLE).withBold()
}
