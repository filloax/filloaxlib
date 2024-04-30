package com.filloax.fxlib

import com.filloax.fxlib.nbt.getListOrNull
import com.filloax.fxlib.nbt.getOrPut
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
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
    book.setBookTags(title, author, pages)
    return book
}

fun ItemStack.setBookTags(title: String?, author: String?, pages: List<Component>) {
    val tag = getOrCreateTag()

    val pagesTag = ListTag()
    if (`is`(Items.WRITABLE_BOOK)) {
        pages.forEach { page ->
            pagesTag.add(StringTag.valueOf(page.string))
        }
    } else if (`is`(Items.WRITTEN_BOOK)) {
        pages.forEach { page ->
            pagesTag.add(StringTag.valueOf(Component.Serializer.toJson(page)))
        }
    } else {
        throw IllegalStateException("Cannot set pages of non-book item stack $this")
    }
    tag.put("pages", pagesTag)
    if (`is`(Items.WRITTEN_BOOK)) {
        tag.putString("author", author ?: "???")
        tag.putString("title", title ?: Items.WRITTEN_BOOK.descriptionId)
    }
}

fun ItemStack.setBookTags(pages: List<Component>) {
    setBookTags(null, null, pages)
}

fun ItemStack.setBookTags(title: Component, author: Component, pages: List<Component>) {
    setBookTags( title.string, author.string, pages)
}

fun ItemStack.getBookText(): List<Component> {
    val tag = getOrCreateTag()
    return if (`is`(Items.WRITABLE_BOOK)) {
        tag.getList("pages", Tag.TAG_STRING.toInt()).map{ Component.literal((it as StringTag).asString) }
    } else if (`is`(Items.WRITTEN_BOOK)) {
        tag.getList("pages", Tag.TAG_STRING.toInt()).map{ Component.Serializer.fromJson((it as StringTag).asString) as Component}
    } else {
        throw IllegalStateException("Cannot get pages of non-book item stack $this")
    }
}

fun ItemStack.getBookTitle() =
    if (`is`(Items.WRITTEN_BOOK)) {
        getOrCreateTag().getString("title")
    } else {
        throw IllegalStateException("Cannot get title of non-written book item stack $this")
    }

fun ItemStack.getBookAuthor() =
    if (`is`(Items.WRITTEN_BOOK)) {
        getOrCreateTag().getString("author")
    } else {
        throw IllegalStateException("Cannot get author of non-written book item stack $this")
    }