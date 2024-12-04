package com.github.rushyverse.rtf.runnable

import com.github.rushyverse.api.extension.withBold
import com.github.rushyverse.api.koin.inject
import com.github.rushyverse.api.translation.Translator
import com.github.rushyverse.api.translation.getComponent
import com.github.rushyverse.rtf.RTFPlugin
import com.github.rushyverse.rtf.client.ClientRTF
import com.github.rushyverse.rtf.game.Game
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.scheduler.BukkitRunnable

class RespawnState(
    val client: ClientRTF,
    val game: Game,
    time: Int,
) : BukkitRunnable() {

    val translator: Translator by inject(RTFPlugin.ID)
    var remainingTime = time;
    val savedWalkSpeed = client.requirePlayer().walkSpeed

    init {
        client.requirePlayer().walkSpeed = 0F
    }

    override fun run() {
        val player = client.player
        if (player == null || !player.isOnline || !player.world.name.contains("rtf")) {
            cancel()
            return
        }

        runBlocking {
            if (remainingTime == 0) {
                val title = Title.title(
                    translator.getComponent("respawn.end.go", client.lang().locale)
                        .color(NamedTextColor.RED).withBold(),
                    text("")
                )
                client.requirePlayer().showTitle(title)
                client.respawnState = false
                client.requirePlayer().walkSpeed = savedWalkSpeed
                cancel()
            } else {
                val title = Title.title(
                    translator.getComponent("respawn.time.remaining", client.lang().locale, arrayOf("<green>$remainingTime</green>"))
                        .color(NamedTextColor.GOLD).withBold(),
                    text("")
                )
                client.requirePlayer().showTitle(title)
            }
        }

        remainingTime--
    }
}