package com.filloax.fxlib

import com.mojang.datafixers.util.Either
import net.minecraft.core.BlockPos
import net.minecraft.core.BlockPos.MutableBlockPos
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Predicate
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


private val defaultRandom = RandomSource.create()

/**
 * Misc utils that do not directly fit as extension methods,
 * also available as package functions
 */
object FxUtils {
    @JvmStatic
    fun getServer() = FxLibServices.platform.getServer()

    /**
     * Decode structure key/tag string as found in commands, data, etc.
     */
    @JvmStatic
    fun getStructTagOrKey(structureId: String): Either<TagKey<Structure>, ResourceKey<Structure>> {
        return if (structureId.startsWith("#")) {
            Either.left(TagKey.create(Registries.STRUCTURE, ResourceLocation(structureId.replaceFirst("#", ""))))
        }
        else {
            Either.right(ResourceKey.create(Registries.STRUCTURE, ResourceLocation(structureId)))
        }
    }

    @JvmStatic
    fun <T> concatIterators(firstIterator: Iterator<T>, vararg iterators: Iterator<T>): Iterator<T> {
        return ConcatenatedIterator(firstIterator, *iterators)
    }

    @JvmField
    val ALWAYS_TRUE: Predicate<Any> = Predicate<Any> { true }
    @JvmStatic
    fun <T> alwaysTruePredicate(): (T) -> Boolean = { true }
}

// Package functions

fun getServer() = FxUtils.getServer()
/**
 * Decode structure key/tag string as found in commands, data, etc.
 */
fun getStructTagOrKey(structureId: String) = FxUtils.getStructTagOrKey(structureId)
fun <T> concatIterators(firstIterator: Iterator<T>, vararg iterators: Iterator<T>) = FxUtils.concatIterators(firstIterator, *iterators)
val ALWAYS_TRUE: Predicate<Any> = Predicate<Any> { true }

fun <T> alwaysTruePredicate(): (T) -> Boolean = { true }


// Extension functions

fun Level.getYAtXZ(x: Int, z: Int, heightmap: Heightmap.Types = Heightmap.Types.MOTION_BLOCKING): Int {
    val samplePos = BlockPos(x, 64, z)
    return getChunk(samplePos).getHeight(heightmap, x, z)
}
fun Level.getPosAtXZ(x: Int, z: Int, heightmap: Heightmap.Types = Heightmap.Types.MOTION_BLOCKING): BlockPos {
    return BlockPos(x, getYAtXZ(x, z, heightmap), z)
}

/**
 * Find nearest free position from a block pos, stopping if nothing is found within 200 blocks
 */
fun Level.nearestFreePosition(from: BlockPos, aboveSolid: Boolean = false, onlyAbove: Boolean = false): BlockPos? {
    val maxRange = 200
    val visited = mutableSetOf<BlockPos>()
    val queue = PriorityQueue<BlockPos> { a, b ->
        val distA = distanceSquared(from, a)
        val distB = distanceSquared(from, b)
        distA.compareTo(distB)
    }

    visited.add(from)
    queue.add(from)

    while (queue.isNotEmpty()) {
        val current = queue.poll()

        if (
            getBlockState(current).isAir
            && (!aboveSolid || getBlockState(current.below()).blocksMotion())
        ) {
            return current
        }

        if (visited.size >= maxRange) {
            return null
        }

        for (dx in -1..1) {
            for (dy in -1..1) {
                for (dz in -1..1) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue
                    }

                    val neighbor = BlockPos(current.x + dx, current.y + dy, current.z + dz)
                    if (!visited.contains(neighbor)) {
                        if (onlyAbove && neighbor.y < from.y) {
                            continue
                        }

                        visited.add(neighbor)
                        queue.add(neighbor)
                    }
                }
            }
        }
    }

    return null
}

private fun distanceSquared(a: BlockPos, b: BlockPos): Int {
    val dx = a.x - b.x
    val dy = a.y - b.y
    val dz = a.z - b.z
    return dx * dx + dy * dy + dz * dz
}

fun <T> Collection<T>.weightedRandom(getWeight: (T) -> Float, random: RandomSource = defaultRandom): T {
    assert(this.isNotEmpty()) { "The collection must not be empty!" }

    val totalWeight = this.sumOf { getWeight(it).toDouble() }
    var randomValue = random.nextDouble() * totalWeight

    for (item in this) {
        val weight = getWeight(item)
        if (randomValue < weight) {
            return item
        }
        randomValue -= weight
    }

    throw IllegalStateException("Weighted random selection failed.")
}

fun BoundingBox.iterBlocks() = BlockVolumeIterator(this)
fun BoundingBox.iterBlocks(action: (BlockPos) -> Unit) = BlockVolumeIterator(this).forEach(action)

class BlockVolumeIterator(val volume: BoundingBox) : Iterator<BlockPos> {
    var current = MutableBlockPos(volume.minX(), volume.minY(), volume.minZ())

    override fun hasNext(): Boolean {
        return current.x <= volume.maxX() && current.y <= volume.maxY() && current.z <= volume.maxZ()
    }

    override fun next(): BlockPos {
        current.x++
        if (current.x > volume.maxX()) {
            current.x = volume.minX()
            current.z++
            if (current.z > volume.maxZ()) {
                current.z = volume.minZ()
                current.y++
            }
        }
        return current
    }
}

/**
 * Returns smallest bounding box contained in both bounds.
 */
fun BoundingBox.clip(minX: Int = Int.MIN_VALUE, minY: Int = Int.MIN_VALUE, minZ: Int = Int.MIN_VALUE, maxX: Int = Int.MAX_VALUE, maxY: Int = Int.MAX_VALUE, maxZ: Int = Int.MAX_VALUE): BoundingBox {
    val finalMinX = max(this.minX(), minX)
    val finalMinY = max(this.minY(), minY)
    val finalMinZ = max(this.minZ(), minZ)
    val finalMaxX = min(this.maxX(), maxX)
    val finalMaxY = min(this.maxY(), maxY)
    val finalMaxZ = min(this.maxZ(), maxZ)
    return BoundingBox(finalMinX, finalMinY, finalMinZ, finalMaxX, finalMaxY, finalMaxZ)
}

/**
 * Intersect this bounding box with another, containing the biggest box included in both.
 */
fun BoundingBox.clip(other: BoundingBox): BoundingBox {
    return clip(other.minX(), other.minY(), other.minZ(), other.maxX(), other.maxY(), other.maxZ())
}

/**
 * Add item in the first available position after another in the list with a lower priority
 */
fun <T> MutableList<T>.addByPriority(item: T, getPriority: (T) -> Int) {
    val priority = getPriority(item)
    val index = indexOfLast { getPriority(it) < priority }
    if (index == -1) {
        add(0, item)
    } else {
        add(index + 1, item)
    }
}

/**
 * Add all items in the first available position after another in the list with a lower priority
 * (thanks ChatGPT)
 */
fun <T> MutableList<T>.addAllByPriority(items: Collection<T>, getPriority: (T) -> Int) {
    for (item in items) {
        val priority = getPriority(item)
        val insertionIndex = indexOfLast { getPriority(it) < priority } + 1
        add(insertionIndex, item)
    }
}

fun <T> MutableCollection<T>.removeAllCountDuplicates(other: Collection<T>) {
    val occurrences = other.groupingBy { it }.eachCount().toMutableMap()
    this.removeAll { element ->
        val count = occurrences[element]
        if (count != null && count > 0) {
            occurrences[element] = count - 1
            true
        } else {
            false
        }
    }
}

fun <A, B, X, Y> Pair<A, B>.map(firstMap: (A) -> X, secondMap: (B) -> Y): Pair<X, Y> {
    return Pair(firstMap(first), secondMap(second))
}


class ConcatenatedIterator<T>(private vararg val iterators: Iterator<T>) : Iterator<T> {
    private var cur: Int = 0

    init {
        assert(iterators.isNotEmpty())
    }

    override fun hasNext(): Boolean {
        var currentIterator = iterators[cur]
        while(!currentIterator.hasNext()) {
            cur++
            currentIterator = if (cur < iterators.size)
                iterators[cur]
            else
                return false
        }
        return currentIterator.hasNext()
    }

    override fun next(): T {
        if (!hasNext()) throw NoSuchElementException()
        return iterators[cur].next()
    }
}

fun Long.ticksToSeconds(): Float {
    return this / 20f
}

fun Long.ticksToTimecode(): String {
    val seconds = this.ticksToSeconds()
    val hours = (seconds / 3600).toInt()
    val minutes = ((seconds % 3600) / 60).toInt()
    val secs = (seconds % 60).toInt()

    return String.format("%d:%02d:%02d", hours, minutes, secs)
}

fun Int.ticksToSeconds(): Float {
    return this / 20f
}

fun Float.secondsToTicks(): Int {
    return floor(this * 20).toInt()
}

fun Vec3i.rotate(rotation: Rotation): Vec3i {
    return when (rotation) {
        Rotation.COUNTERCLOCKWISE_90 -> Vec3i(z, y, -x)
        Rotation.CLOCKWISE_90 -> Vec3i(-z, y, x)
        Rotation.CLOCKWISE_180 -> Vec3i(-x, y, -z)
        Rotation.NONE -> Vec3i(x, y, z)
    }
}

fun Vec3i.vec3(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())

fun Vec3i.withX(x: Int): Vec3i = Vec3i(x, this.y, this.z)
fun Vec3i.withY(y: Int): Vec3i = Vec3i(this.x, y, this.z)
fun Vec3i.withZ(z: Int): Vec3i = Vec3i(this.x, this.y, z)
fun Vec3i.copy(x: Int = this.x, y: Int = this.y, z: Int = this.z) = Vec3i(x, y, z)

fun BlockPos.withX(x: Int): BlockPos = BlockPos(x, this.y, this.z)
fun BlockPos.withY(y: Int): BlockPos = BlockPos(this.x, y, this.z)
fun BlockPos.withZ(z: Int): BlockPos = BlockPos(this.x, this.y, z)
fun BlockPos.copy(x: Int = this.x, y: Int = this.y, z: Int = this.z) = BlockPos(x, y, z)