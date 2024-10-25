package com.filloax.fxlib.api.chunk

import com.filloax.fxlib.api.alwaysTruePredicate
import com.filloax.fxlib.api.entity.entityTestForClass
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.phys.AABB
import java.util.stream.Stream


fun ChunkPos.isBlockPosInChunk(blockPos: BlockPos): Boolean {
    return (blockPos.x in minBlockX..maxBlockX) && (blockPos.z in minBlockZ .. maxBlockZ)
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

fun BoundingBox.chunkRange() = boundBoxChunkRange(this)

inline fun <reified T : Entity> LevelChunk.getEntities(
    output: MutableList<in T>,
    entityTypeTest: EntityTypeTest<Entity, T> = entityTestForClass<T>(),
    noinline predicate: (T) -> Boolean = { true },
    limit: Int? = null
) {
    val aabb = this.getAABB()
    if (limit != null)
        this.level!!.getEntities(entityTypeTest, aabb, predicate, output, limit)
    else
        this.level!!.getEntities(entityTypeTest, aabb, predicate, output)
}

inline fun <reified T : Entity> LevelChunk.getEntities(
    entityTypeTest: EntityTypeTest<Entity, T> = entityTestForClass<T>(),
    noinline predicate: (T) -> Boolean = { true },
    limit: Int? = null
): List<T> {
    val out = mutableListOf<T>()
    getEntities(out, entityTypeTest, predicate, limit)
    return out.toList()
}

inline fun <reified T : Entity> LevelChunk.getClassEntities(
    output: MutableList<in T>,
    noinline predicate: (T) -> Boolean = { true },
    limit: Int? = null
) {
    getEntities(output, entityTestForClass(), predicate, limit)
}

inline fun <reified T : Entity> LevelChunk.getClassEntities(
    noinline predicate: (T) -> Boolean = { true },
    limit: Int? = null
): List<T> {
    val out = mutableListOf<T>()
    getEntities(out, entityTestForClass(), predicate, limit)
    return out.toList()
}

fun LevelChunk.getEntities(
    predicate: (Entity) -> Boolean = alwaysTruePredicate(),
    limit: Int? = null,
): List<Entity> {
    return getEntities<Entity>(entityTestForClass(), predicate, limit)
}