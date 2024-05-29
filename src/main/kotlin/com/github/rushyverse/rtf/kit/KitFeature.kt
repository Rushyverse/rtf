package com.github.rushyverse.rtf.kit

import com.github.rushyverse.rtf.client.ClientRTF
import com.github.rushyverse.rtf.listener.ListenerRTF
import org.bukkit.inventory.ItemStack

abstract class KitFeature(val name: String) : ListenerRTF() {

    companion object {
        /**
         * Static map of the registered KitFeatures.
         * Each element is mapped automatically during the object initialization.
         */
        val featureMap = mutableMapOf<String, KitFeature>()
    }

    init {
        featureMap[name] = this
    }

    abstract fun item() : ItemStack

    abstract fun onGiveKit(client: ClientRTF)
}