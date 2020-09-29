package me.cepi.gameplay.util.menu

import me.cepi.gameplay.GameplayPlugin
import me.cepi.gameplay.modules.MenuItemListener
import me.cepi.gameplay.util.items.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.*

/**
 * Basic Menu class for creating advanced Menus with multiple click events
 * easily
 *
 * @author LeoDog896, jeremy
 */
class Menu(player: Player, inventoryRows: Int, inventoryTitle: String) {

    val inventory: Inventory

    val player: Player

    val title: String

    private val menuItems: MutableList<MenuItem> = ArrayList()

    private val dataStorage: MutableMap<Int, Any> = HashMap()

    private var closeConsumer: (InventoryCloseEvent.Reason) -> Unit = { }

    fun setBorder(border: ItemStack) {
        setRow(0, border).onClick(true)
        setRow(rows - 1, border).onClick(true)
        setColumn(0, border).onClick(true)
        setColumn(8, border).onClick(true)
    }

    fun setBorder(mainBorder: Material) = setBorder(ItemBuilder(mainBorder).setName(" ").toItem())

    /**
     * Basic method for setting items
     *
     * @param slot Slot starting at 0s
     * @param item basic ItemStack
     * @return An editable MenuItem class for click events and editing
     */
    fun setItem(slot: Int, item: ItemStack): MenuItem {
        inventory.setItem(slot, item)
        val menuItem = MenuItem(slot, this)
        menuItems.add(menuItem)
        return menuItem
    }

    fun setItem(slot: Int, item: ItemBuilder) = setItem(slot, item.toItem())

    fun setItems(slotFrom: Int, slotTo: Int, item: ItemStack): MenuItems {
        val items: MutableList<MenuItem> = ArrayList()
        for (i in slotFrom..slotTo) {
            items.add(setItem(i, item))
        }
        return MenuItems(items)
    }

    fun setItems(slotFrom: Int, slotTo: Int, slotSpacing: Int, item: ItemStack): MenuItems {
        val items: MutableList<MenuItem> = ArrayList()
        var i = slotFrom
        while (i <= slotTo) {
            items.add(setItem(i, item))
            i += slotSpacing
        }
        return MenuItems(items)
    }

    private val rows: Int
        get() = inventory.size / 9

    /**
     * Clears the menu without removing any data
     */
    fun clearMenu() {
        killAllClickers()
        inventory.clear()
        menuItems.clear()
    }

    /**
     * Clears the menu with an optional data removal
     *
     * @param trueWipe If data will be removed or not
     */
    private fun clearMenu(trueWipe: Boolean = true) {
        killAllClickers()
        menuItems.clear()
        inventory.clear()
        if (trueWipe) dataStorage.clear()
    }

    /**
     * Updates an item for refreshing
     *
     * @param item The item to refresh (checks by slot)
     */
    fun reputItemBySlot(item: MenuItem) {
        for (i in menuItems.indices) {
            if (menuItems[i].slot == item.slot) {
                menuItems[i] = item
            }
        }
    }

    /**
     * Finds and removes all clickers in the current menu
     */
    private fun killAllClickers() {
        menuItems.forEach { item -> MenuItemListener.remove(item) }
    }

    /**
     * Finds and removes all clickers at a specific slot
     *
     * @param slot The slot to remove clickers from
     */
    fun killClickerAtSlot(slot: Int) {
        menuItems.stream()
                .filter { item: MenuItem -> item.slot == slot }
                .forEach { item: MenuItem -> MenuItemListener.remove(item) }
    }

    /**
     * Set the row of a menu with an ItemStack.
     *
     * @param row   The row, starting from 0
     * @param stack The ItemStack to use
     * @return [MenuItems] (a group of [MenuItem] for quick handling)
     */
    fun setRow(row: Int, stack: ItemStack): MenuItems {
        var startIndex = row * 9
        if (startIndex > inventory.size) throw ArrayIndexOutOfBoundsException()
        val menuItems: MutableList<MenuItem> = ArrayList()
        val endIndex = startIndex + 9
        startIndex = row * 9
        while (startIndex < endIndex) {
            val menuItem = setItem(startIndex, stack)
            this.menuItems.add(menuItem)
            menuItems.add(menuItem)
            startIndex++
        }
        return MenuItems(menuItems)
    }

    fun setRow(row: Int, builder: ItemBuilder) = setRow(row, builder.toItem())

    private fun setColumn(column: Int, stack: ItemStack): MenuItems {
        if (column > 8) throw ArrayIndexOutOfBoundsException("Too many columns!")
        val menuItems: MutableList<MenuItem> = ArrayList()
        val endIndex = 9 * inventory.size / 9 + column
        var i = column
        while (i < endIndex) {
            val menuItem = setItem(i, stack)
            this.menuItems.add(menuItem)
            menuItems.add(menuItem)
            i += 9
        }
        return MenuItems(menuItems)
    }

    fun setRows(stack: ItemStack, vararg rows: Int): MenuItems? {
        var initialMenuItems = setRow(rows[0], stack)
        for (i in 1 until rows.size) {
            initialMenuItems = initialMenuItems.concat(setRow(rows[i], stack))
        }
        return initialMenuItems
    }

    fun setColumns(stack: ItemStack, vararg columns: Int): MenuItems? {
        var initialMenuItems = setColumn(columns[0], stack)
        for (i in 1 until columns.size) {
            initialMenuItems = initialMenuItems.concat(setColumn(columns[i], stack))
        }
        return initialMenuItems
    }

    /**
     * Get an item from a slot
     *
     * @param slot The slot to get an item from
     * @return the [ItemStack] from there.
     */
    fun getItem(slot: Int): ItemStack {
        return if (inventory.getItem(slot) == null) ItemStack(Material.AIR) else inventory.getItem(slot)!!
    }

    /**
     * Close the menu
     */
    fun close() {
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN)
        clearMenu()
    }

    fun setData(slot: Int, `object`: Any) {
        dataStorage[slot] = `object`
    }

    fun getData(slot: Int): Any? {
        return dataStorage[slot]
    }

    fun setSlotMeta(slot: Int, meta: ItemMeta) {
        val item = inventory.getItem(slot)
        (item ?: return).itemMeta = meta
        inventory.setItem(slot, item)
    }

    /**
     * Clear a specific value of a specific slot
     *
     * @param slot  The slot to clear data from
     * @param value The value to remove
     */
    fun clearData(slot: Int, value: String) = dataStorage.remove(slot, value)

    /**
     * Clears a specific value
     *
     * @param slot The slot to remove
     */
    fun clearData(slot: Int) = dataStorage.remove(slot)

    /**
     * Set function to run when the menu closes.
     *
     * @param consumer The consumer to run when the menu closes.
     */
    fun onClose(consumer: (InventoryCloseEvent.Reason) -> Unit) {
        closeConsumer = consumer
    }

    fun triggerClose(reason: InventoryCloseEvent.Reason) {
        closeConsumer.invoke(reason)
    }

    fun open() {
        Bukkit.getScheduler().runTask(GameplayPlugin.plugin, Runnable { player.openInventory(inventory) })
    }

    fun open(player: Player) {
        Bukkit.getScheduler().runTask(GameplayPlugin.plugin, Runnable { player.openInventory(inventory) })
    }

    init {
        val inventorySlots = inventoryRows * 9
        this.player = player
        title = inventoryTitle
        inventory = Bukkit.createInventory(null, inventorySlots, inventoryTitle)
    }
}