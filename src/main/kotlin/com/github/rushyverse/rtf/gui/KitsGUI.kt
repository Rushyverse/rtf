package com.github.rushyverse.rtf.gui

import com.github.rushyverse.api.extension.withoutItalic
import com.github.rushyverse.api.player.Client
import com.github.rushyverse.api.translation.getComponent
import com.github.rushyverse.rtf.client.ClientRTF
import com.github.rushyverse.rtf.config.Kit
import com.github.rushyverse.rtf.config.KitsConfig
import com.github.rushyverse.rtf.gui.commons.GUI
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*

class KitsGUI(
    private val config: KitsConfig
) : GUI("menu.kits.title", 9) {

    private fun buildKitIcon(kit: Kit, locale: Locale) = kit.icon.clone().apply {
        editMeta { meta ->
            meta.displayName(
                translator.getComponent(kit.name, locale)
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .withoutItalic()
            )
            meta.lore(
                listOf(
                    translator.getComponent(kit.description, locale)
                        .color(NamedTextColor.GRAY)
                        .withoutItalic()
                )
            )
            meta.addItemFlags(*ItemFlag.entries.toTypedArray())
        }
    }

    override suspend fun applyItems(client: Client, inv: Inventory) {
        config.kits.forEach {
            inv.addItem(
                buildKitIcon(it, client.lang().locale)
            )
        }
    }

    override fun onClick(client: Client, item: ItemStack, clickType: ClickType) {
        val type = item.type
        val selectedKit = config.kits.firstOrNull { it.icon.type == type } ?: return

        client.requirePlayer().inventory.apply {
            clear()
            selectedKit.giveKit(client as ClientRTF)
        }
    }
}
