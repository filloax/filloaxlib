package com.filloax.fxlib

import com.mojang.datafixers.util.Either
//import com.ruslan.growsseth.interfaces.WithPersistentData
import net.minecraft.core.BlockPos
import net.minecraft.core.BlockPos.MutableBlockPos
import net.minecraft.core.SectionPos
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.phys.AABB
import java.util.*
import java.util.stream.Stream
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


//fun getData(entity: Entity): CompoundTag {
//    return (entity as WithPersistentData).persistentData
//}

fun getStructTagOrKey(structureId: String): Either<TagKey<Structure>, ResourceKey<Structure>> {
    return if (structureId.startsWith("#")) {
        Either.left(TagKey.create(Registries.STRUCTURE, ResourceLocation(structureId.replaceFirst("#", ""))))
    }
    else {
        Either.right(ResourceKey.create(Registries.STRUCTURE, ResourceLocation(structureId)))
    }
}

fun getYAtXZ(level: ServerLevel, x: Int, z: Int, heightmap: Heightmap.Types = Heightmap.Types.MOTION_BLOCKING): Int {
    val samplePos = BlockPos(x, 64, z)
    return level.getChunk(samplePos).getHeight(heightmap, x, z)
}

fun isBlockPosInChunk(chunkPos: ChunkPos, blockPos: BlockPos): Boolean {
    return (blockPos.x in chunkPos.minBlockX..chunkPos.maxBlockX) && (blockPos.z in chunkPos.minBlockZ .. chunkPos.maxBlockZ)
}

fun ChunkAccess.getBoundingBox(): BoundingBox {
    val pos = this.pos
    val minY = this.minBuildHeight
    val maxY = this.maxBuildHeight
    return BoundingBox(pos.minBlockX, minY, pos.minBlockZ, pos.maxBlockX, maxY, pos.maxBlockZ)
}

fun ChunkAccess.getAABB(): AABB {
    return AABB.of(this.getBoundingBox())
}

private val defaultRandom = RandomSource.create()

fun <T>weightedRandom(items: Collection<T>, getWeight: (T) -> Float, random: RandomSource = defaultRandom): T {
    assert(items.isNotEmpty()) { "The collection must not be empty!" }

    val totalWeight = items.sumOf { getWeight(it).toDouble() }
    var randomValue = random.nextDouble() * totalWeight

    for (item in items) {
        val weight = getWeight(item)
        if (randomValue < weight) {
            return item
        }
        randomValue -= weight
    }

    throw IllegalStateException("Weighted random selection failed.")
}

fun secondsToTicks(seconds: Float): Int {
    return floor(seconds * 20).toInt()
}

fun boundBoxChunkRange(boundingBox: BoundingBox): Stream<ChunkPos> {
    val chunkPosMinBounds = ChunkPos(
        SectionPos.blockToSectionCoord(boundingBox.minX()),
        SectionPos.blockToSectionCoord(boundingBox.minZ())
    )
    val chunkPosMaxBounds = ChunkPos(
        SectionPos.blockToSectionCoord(boundingBox.maxX()),
        SectionPos.blockToSectionCoord(boundingBox.maxZ())
    )

    return ChunkPos.rangeClosed(chunkPosMinBounds, chunkPosMaxBounds)
}

fun iterBlocks(volume: BoundingBox) = BlockVolumeIterator(volume)

fun iterBlocks(volume: BoundingBox, action: (BlockPos) -> Unit) {
    iterBlocks(volume).forEach(action)
}

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
 * Find nearest free position from a block pos, stopping if nothing is found within 200 blocks
 */
fun nearestFreePosition(level: Level, from: BlockPos, aboveSolid: Boolean = false, onlyAbove: Boolean = false): BlockPos? {
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
            level.getBlockState(current).isAir
            && (!aboveSolid || level.getBlockState(current.below()).blocksMotion())
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

fun <T> concatIterators(firstIterator: Iterator<T>, vararg iterators: Iterator<T>): Iterator<T> {
    return ConcatenatedIterator(firstIterator, *iterators)
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

fun Vec3i.rotate(rotation: Rotation): Vec3i {
    return when (rotation) {
        Rotation.COUNTERCLOCKWISE_90 -> Vec3i(z, y, -x)
        Rotation.CLOCKWISE_90 -> Vec3i(-z, y, x)
        Rotation.CLOCKWISE_180 -> Vec3i(-x, y, -z)
        Rotation.NONE -> Vec3i(x, y, z)
    }
}