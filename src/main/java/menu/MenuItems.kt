package me.cepi.gameplay.util.menu

import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import java.util.function.Consumer

class MenuItems(val items: MutableList<MenuItem>) {

    /**
     * The parent menu
     */
    private val menu: Menu = items[0].menu

    /**
     * On click for all the items with a handler
     * @param code The consumer that handles the onClick event
     * @param toCancel Cancel any raw clicking (picking up, etc.)
     */
    fun onClick(code: (InventoryAction, ClickType) -> Unit, toCancel: Boolean) {
        items.forEach { item ->
            item.onClick(code, toCancel)
            menu.reputItemBySlot(item)
        }
    }

    fun concat(items: MenuItems): MenuItems {
        val finalItems: MutableList<MenuItem> = items.items
        finalItems.addAll(this.items)
        return MenuItems(finalItems)
    }

    /**
     * On click for all the items in this class
     * @param toCancel Cancel any raw clicking (picking up, etc.)
     */
    fun onClick(toCancel: Boolean?) {
        items.forEach(Consumer { item: MenuItem ->
            item.onClick(toCancel ?: return@Consumer)
            menu.reputItemBySlot(item)
        })
    }

}