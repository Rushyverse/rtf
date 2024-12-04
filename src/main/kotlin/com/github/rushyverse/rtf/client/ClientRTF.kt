package com.github.rushyverse.rtf.client

import com.github.rushyverse.api.player.Client
import com.github.rushyverse.rtf.config.Kit
import com.github.rushyverse.rtf.config.Reward
import kotlinx.coroutines.CoroutineScope
import java.util.*

class ClientRTF(
    val stats: RTFStats = RTFStats(),
    uuid: UUID,
    val coins: Int = 0,
    scope: CoroutineScope
) : Client(uuid, scope) {

    var respawnState: Boolean = false

    /**
     * Represents the current kit of the player.
     * Null by default.
     */
    var kit: Kit? = null

    /**
     * Gives instantly a reward to the client.
     * For each positive value in the [Reward], the client will receive
     * a notification message and sound.
     * @param reward The reward to apply on the client.
     */
    fun reward(reward: Reward) {
        if (reward.coins > 0) {
            // TODO: inc coins value

            send("<yellow>+${reward.coins} coins")
        }
        if (reward.xp > 0) {
            requirePlayer().giveExp(reward.xp)
            send("<green>+${reward.xp} xp")
        }
    }
}
