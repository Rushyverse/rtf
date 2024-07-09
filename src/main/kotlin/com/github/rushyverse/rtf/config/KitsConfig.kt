package com.github.rushyverse.rtf.config

import com.github.rushyverse.api.serializer.ItemStackSerializer
import com.github.rushyverse.rtf.client.ClientRTF
import com.github.rushyverse.rtf.kit.KitFeature.Companion.featureMap
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bukkit.inventory.ItemStack

typealias ItemStackSerializable = @Contextual ItemStack

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class KitsConfig(
    val kits: Set<Kit>
)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Kit(
    val name: String,
    val description: String,
    val icon: ItemStackSerializable,
    val features: Set<String> = emptySet(),
    val armor: ArmorConfig? = null,
    val items: Set<@Serializable(with = ItemStackSerializer::class) ItemStack>,
) {

    fun giveKit(client: ClientRTF) {
        val inventory = client.player!!.inventory
        armor.let {
            if (it != null) {
                inventory.helmet = it.helmet
            }
            if (it != null) {
                inventory.chestplate = it.chestplate
            }
            if (it != null) {
                inventory.leggings = it.leggings
            }
            if (it != null) {
                inventory.boots = it.boots
            }
        }

        inventory.addItem(*items.toTypedArray())

        // Give items of features attributed to this kit
        features?.forEach { featureName ->
            val feature = featureMap[featureName]!!
            feature.item()?.apply { inventory.addItem(this) }
            // feature.onGiveKit(client) (not working properly)
        }
    }
}

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class ArmorConfig(
    val helmet: ItemStackSerializable,
    val chestplate: ItemStackSerializable,
    val leggings: ItemStackSerializable,
    val boots: ItemStackSerializable
)