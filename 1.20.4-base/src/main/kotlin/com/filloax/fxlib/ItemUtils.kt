package com.filloax.fxlib

import com.filloax.fxlib.nbt.getOrPut
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

fun itemFromId(id: String): Item {
    return itemFromId(ResourceLocation(id))
}

fun itemFromId(id: ResourceLocation): Item {
    return BuiltInRegistries.ITEM.get(id)
}

/**
 * Get lore lines as mutable list string, whose changes are reflected on the item tags
 */
fun ItemStack.loreLines(): MutableList<Component> {
    val listTag = getOrCreateTagElement("display").getOrPut("Lore", ListTag())

    return StringListTagProxy(listTag)
}

private class StringListTagProxy(private val listTag: ListTag): AbstractMutableList<Component>() {
    private val LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true)

    override val size: Int
        get() = listTag.size

    private fun fromString(string: String): Component {
        return ComponentUtils.mergeStyles(
            Component.Serializer.fromJson(string)
                ?: throw Exception("Wrong component format $string"),
            LORE_STYLE
        )
    }

    override fun get(index: Int): Component = fromString(listTag.getString(index))
    override fun removeAt(index: Int): Component = fromString(listTag.removeAt(index).asString)
    override fun set(index: Int, element: Component): Component {
        return fromString(listTag.set(index, StringTag.valueOf(Component.Serializer.toJson(element))).asString)
    }
    override fun add(index: Int, element: Component) = listTag.add(index, StringTag.valueOf(Component.Serializer.toJson(element)))
}

fun createWrittenBook(title: Component, author: Component, pages: List<Component>): ItemStack {
    val book = ItemStack(Items.WRITTEN_BOOK)
    setBookTags(book, title, author, pages)
    return book
}

fun setBookTags(book: ItemStack, title: String?, author: String?, pages: List<Component>) {
    val tag = book.getOrCreateTag()

    val pagesTag = ListTag()
    if (book.`is`(Items.WRITABLE_BOOK)) {
        pages.forEach { page ->
            pagesTag.add(StringTag.valueOf(page.string))
        }
    } else {
        pages.forEach { page ->
            pagesTag.add(StringTag.valueOf(Component.Serializer.toJson(page)))
        }
    }
    tag.put("pages", pagesTag)
    if (book.`is`(Items.WRITTEN_BOOK)) {
        tag.putString("author", author ?: "???")
        tag.putString("title", title ?: Items.WRITTEN_BOOK.descriptionId)
    }
}

fun setBookTags(book: ItemStack, pages: List<Component>) {
    setBookTags(book, null, null, pages)
}

fun setBookTags(book: ItemStack, title: Component, author: Component, pages: List<Component>) {
    setBookTags(book, title.string, author.string, pages)
}
