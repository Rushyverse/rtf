package com.github.rushyverse.rtf.gui

import com.github.rushyverse.api.extension.asComponent
import com.github.rushyverse.api.player.Client
import com.github.rushyverse.rtf.RTFPlugin
import com.github.rushyverse.rtf.RTFPlugin.Companion.BUNDLE_RTF
import com.github.rushyverse.rtf.config.KitConfig
import com.github.rushyverse.rtf.config.KitsConfig
import com.github.rushyverse.rtf.gui.commons.GUI
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import java.util.*

class KitsGUI(
    private val config: KitsConfig
) : GUI("menu.kits.title", 9) {

    private fun buildKitIcon(kit: KitConfig, locale: Locale) = kit.icon.clone().apply {
        editMeta { meta ->
            RTFPlugin.translator.also {
                meta.displayName(it.translate(kit.name, locale, BUNDLE_RTF).asComponent().color(NamedTextColor.LIGHT_PURPLE))
                meta.lore(listOf(it.translate(kit.description, locale, BUNDLE_RTF).asComponent().color(NamedTextColor.GRAY)))
            }
            meta.removeItemFlags(*ItemFlag.entries.toTypedArray())
        }
    }

    override fun applyItems(client: Client, inv: Inventory) {
        config.kits.forEach {
            inv.addItem(
                buildKitIcon(it, client.lang.locale)
            )
        }
    }

    override fun onClick(client: Client, item: ItemStack, clickType: ClickType) {
        val type = item.type

        val selectedKit = config.kits.firstOrNull { it.icon.type == type } ?: return

        client.requirePlayer().inventory.apply {
            clear()
            sendKitItems(this, selectedKit)
        }
    }

    private fun sendKitItems(inventory: PlayerInventory, kit: KitConfig) {
        kit.armor.also {
            inventory.helmet = it.helmet
            inventory.chestplate = it.chestplate
            inventory.leggings = it.leggings
            inventory.boots = it.boots
        }

        inventory.addItem(*kit.items.toTypedArray())
    }
}