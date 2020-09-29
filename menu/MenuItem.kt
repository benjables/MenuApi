package me.cepi.gameplay.util.menu

import me.cepi.gameplay.modules.MenuItemListener
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction

class MenuItem(val slot: Int, val menu: Menu) {
    val title: String = menu.title

    val player: Player = menu.player

    var codeBlock: ((InventoryAction, ClickType) -> Unit?)? = null
        private set

    var shouldCancel = false
        private set

    /**
     * A click event for a single item
     *
     * @param code     the consumer to be run when a click happens
     * @param toCancel If to cancel the original event
     */
    fun onClick(code: (InventoryAction, ClickType) -> Unit?, toCancel: Boolean) {
        codeBlock = code
        shouldCancel = toCancel
        menu.reputItemBySlot(this)
        MenuItemListener.add(this)
    }
    /**
     * A click event for a single item
     *
     * @param toCancel If to cancel the original event
     */
    fun onClick(toCancel: Boolean) {
        shouldCancel = toCancel
        menu.reputItemBySlot(this)
        MenuItemListener.add(this)
    }

}