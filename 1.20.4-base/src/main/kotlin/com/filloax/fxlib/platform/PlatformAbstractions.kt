package com.filloax.fxlib.platform

import com.mojang.serialization.MapCodec
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType
import net.minecraft.world.level.portal.PortalInfo
import org.jetbrains.annotations.ApiStatus
import java.util.*

/**
 * Then used in exposed Kotlin utils
 */
interface PlatformAbstractions {
    fun getServer(): MinecraftServer?

    /**
     * Description adapted from Fabric API, applies here too mostly
     * Teleports an entity to a different dimension, placing it at the specified destination.
     *
     * <p>Using this method will circumvent Vanilla's portal placement code.
     *
     * <p>When teleporting to another dimension, the entity may be replaced with a new entity in the target
     * dimension. This is not the case for players, but needs to be accounted for by the caller.
     *
     * @param destination the dimension the entity will be teleported to
     * @param target      where the entity will be placed in the target world.
     *                    As in Vanilla, the target's velocity is not applied to players.
     * @param <E>         the type of the teleported entity
     * @return Returns the teleported entity in the target dimension, which may be a new entity or <code>teleported</code>,
     * depending on the entity type.
     * @apiNote this method must be called from the main server thread
     */
    fun fixedChangeDimension(entity: Entity, level: ServerLevel, target: PortalInfo)

    fun isDevEnvironment(): Boolean

    /**
     * Run the action on the entity immediately if loaded, as soon as it's loaded
     * otherwise. Will not persist on game reload.
     */
    @ApiStatus.Internal
    fun runOnEntityWhenPossible(level: ServerLevel, entityUUID: UUID, action: (Entity) -> Unit)

    /**
     * Run now if server started, or wait for server to start then run otherwise.
     */
    @ApiStatus.Internal
    fun runWhenServerStarted(server: MinecraftServer, action: (MinecraftServer) -> Unit)
        = runWhenServerStarted(server, false, action)

    /**
     * Run now if server started, or wait for server to start then run otherwise.
     * @param onServerThread If set, run on server thread, in case you want to be
     *  safe around multithreaded messing.
     */
    @ApiStatus.Internal
    fun runWhenServerStarted(server: MinecraftServer, onServerThread: Boolean, action: (MinecraftServer) -> Unit)

    @ApiStatus.Internal
    fun runAtServerTickEnd(action: (MinecraftServer) -> Unit)
    @ApiStatus.Internal
    fun runAtNextServerTickStart(action: (MinecraftServer) -> Unit)

    @ApiStatus.Internal
    fun runWhenChunkLoaded(level: ServerLevel, chunkPos: ChunkPos, action: (ServerLevel) -> Unit)

    /**
     * Execute code when all the chunks in the surrounding area are loaded, or immediately if loaded already.
     * Note that this isn't assured to ever run depending on area, as if big enough chunks on one end might be
     * unloaded when the other end is loaded; use forced chunks for this, in case.
     */
    @ApiStatus.Internal
    fun runWhenChunksLoaded(level: ServerLevel, minChunkPos: ChunkPos, maxChunkPos: ChunkPos, action: (ServerLevel) -> Unit)
}

fun getPlatformAbstractions(): PlatformAbstractions = ServiceUtil.findService(PlatformAbstractions::class.java)