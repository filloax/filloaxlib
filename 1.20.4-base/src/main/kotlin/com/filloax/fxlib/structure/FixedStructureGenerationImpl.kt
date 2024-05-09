package com.filloax.fxlib.structure

import com.filloax.fxlib.FxLib
import com.filloax.fxlib.UnknownStructureIdException
import com.filloax.fxlib.codec.mutableSetCodec
import com.filloax.fxlib.ScheduledServerTask
import com.filloax.fxlib.savedata.FxSavedData
import com.filloax.fxlib.savedata.FxSavedData.Companion.loadData
import com.filloax.fxlib.structure.tracking.CustomPlacedStructureTracker
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.StructureStart

object FixedStructureGenerationImpl : FixedStructureGeneration {
    private val structsToSpawnById = mutableMapOf<String, StructureSpawnData>()
    private val structsToSpawn = mutableMapOf<Long, MutableList<StructureSpawnData>>()
    private val extraStructSpawnChunks = mutableMapOf<Long, MutableList<StructureSpawnData>>()
    private val alreadyGeneratedStructures = mutableMapOf<String, StructureStart>()

    override val registeredStructureSpawns: Map<String, StructureSpawnData>
        get() = structsToSpawnById

    override fun register(
        level: ServerLevel,
        id: String, pos: BlockPos,
        structureId: ResourceLocation,
        rotation: Rotation,
        force: Boolean,
    ) {
        val spawnData = StructureSpawnData(
            pos = pos,
            rotation = rotation,
            structure = structureId,
            spawnId = id, //?: "${it.structureId}-${it.x}-${it.y}-${it.z}",
            force = force,
        )
        // temporarily only support overworld
        tryQueueSpawnData(level.server, spawnData)
    }

    override fun spawnedQueuedStructure(structureSpawnId: String): Boolean? {
        return if (structureSpawnId in structureSpawnId)
            alreadyGeneratedStructures[structureSpawnId] != null
        else
            null
    }

    fun onServerStopped() {
        structsToSpawnById.clear()
        structsToSpawn.clear()
        extraStructSpawnChunks.clear()
        alreadyGeneratedStructures.clear()
    }

    fun onLoadChunk(level: ServerLevel, chunk: LevelChunk) {
        if (level.dimension() != Level.OVERWORLD) return

        val key = chunk.pos.toLong()
        structsToSpawn[key]?.let { list -> checkChunkSpawns(level, chunk, list) }
        extraStructSpawnChunks[key]?.let { list -> checkChunkSpawns(level, chunk, list) }
    }

    private fun tryQueueSpawnData(server: MinecraftServer, spawnData: StructureSpawnData) {
        val structureRef = server.registryAccess().registryOrThrow(Registries.STRUCTURE).get(spawnData.structure)
        if (structureRef == null) {
            throw UnknownStructureIdException(spawnData.structure, "Cannot queue non-existent structure")
        } else {
            val level = server.overworld()
            val savedData = level.loadData(Save.DEF)
            if (savedData.generatedSpawns.contains(spawnData.spawnId)) {
                return
            }

           structsToSpawnById.put(spawnData.spawnId, spawnData)

            val key = ChunkPos(spawnData.pos).toLong()
            structsToSpawn.computeIfAbsent(key) { mutableListOf() }.add(spawnData)
            FxLib.logger.info("Queued $spawnData for fixed structure generation...")

            checkAlreadyLoadedChunkSpawn(level, spawnData)
        }
    }

    private fun checkAlreadyLoadedChunkSpawn(level: ServerLevel, spawnData: StructureSpawnData) {
        if (level.isLoaded(spawnData.pos)) {
            FxLib.logger.info("Chunk of ${spawnData.spawnId} already loaded, trying spawn...")
            checkChunkSpawns(level, level.getChunkAt(spawnData.pos), listOf(spawnData))
        }
    }

    private fun checkChunkSpawns(level: ServerLevel, chunk: LevelChunk, chunkStructs: List<StructureSpawnData>) {
        FxLib.logger.info("Chunk ${chunk.pos} loaded, to spawn: ${chunkStructs.size}")
        val savedData = level.loadData(Save.DEF)
        chunkStructs.forEach { spawnData ->
            // Schedule with 0 ticks, so it runs asap but after chunk has finished loading,
            // as that lead to freezing when placing it on the load chunk callback
            // (multi-threading stuff)
            if (!savedData.generatedSpawns.contains(spawnData.spawnId)) {
                ScheduledServerTask.schedule(level.server, 0) {
                    // Check again just in case, as this is later in execution
                    if (savedData.generatedSpawns.contains(spawnData.spawnId)) {
                        FxLib.logger.info("Structure spawn ${spawnData.spawnId} already generated! [2]")
                    } else {
                        FxLib.logger.info("Trying to spawn structure $spawnData as chunk ${chunk.pos} was loaded...")
                        val success = trySpawnStructure(level.server, spawnData)
                        if (success) {
                            savedData.generatedSpawns.add(spawnData.spawnId)
                            savedData.setDirty()
                            FxLib.logger.info("Spawned structure $spawnData!")
                        }
                    }
                }
            } else {
                FxLib.logger.info("Structure spawn ${spawnData.spawnId} already generated!")
            }
        }
    }

    private fun trySpawnStructure(server: MinecraftServer, spawnData: StructureSpawnData): Boolean {
        // Only overworld for now
        val serverLevel = server.overworld()
        val structure = server.registryAccess().registry(Registries.STRUCTURE).get().get(spawnData.structure)
            ?: throw UnknownStructureIdException(spawnData.structure)

        if (structure is FixablePosition) {
            structure.setNextPlacePosition(spawnData.pos)
            FxLib.logger.info("Fixed structure place pos ${spawnData.pos} for ${spawnData.structure}")
        }

        if (structure is FixableRotation) {
            val rotation = spawnData.rotation ?: structure.defaultRotation
            val usedDefault = spawnData.rotation == null && structure.defaultRotation != null
            if (rotation != null) {
                structure.setNextPlaceRotation(rotation)
            }
            FxLib.logger.info("Fixed structure place rotation $rotation (default: $usedDefault) for ${spawnData.structure}")
        }

        val chunkGenerator = serverLevel.chunkSource.generator
        val structureStart = alreadyGeneratedStructures[spawnData.spawnId] ?: structure.generate(
            server.registryAccess(),
            chunkGenerator,
            chunkGenerator.biomeSource,
            serverLevel.chunkSource.randomState(),
            serverLevel.structureManager,
            serverLevel.seed,
            ChunkPos(spawnData.pos),
            0,
            serverLevel
        ) { true }

        if (!structureStart.isValid) {
            FxLib.logger.error("Failed to place ${spawnData.structure} at ${spawnData.pos}")
            return false
        }
        alreadyGeneratedStructures[spawnData.spawnId] = structureStart

        val boundingBox = structureStart.boundingBox
        val chunkPosMinBounds = ChunkPos(
            SectionPos.blockToSectionCoord(boundingBox.minX()),
            SectionPos.blockToSectionCoord(boundingBox.minZ())
        )
        val chunkPosMaxBounds = ChunkPos(
            SectionPos.blockToSectionCoord(boundingBox.maxX()),
            SectionPos.blockToSectionCoord(boundingBox.maxZ())
        )
        if (ChunkPos.rangeClosed(chunkPosMinBounds, chunkPosMaxBounds).filter { chunkPos: ChunkPos ->
                val isLoaded = serverLevel.isLoaded(chunkPos.worldPosition)
                if (!isLoaded) {
                    FxLib.logger.info("Chunk at $chunkPos (${chunkPos.worldPosition}) not loaded and required! Queueing for next spawn")
                    val key = chunkPos.toLong()
                    extraStructSpawnChunks
                        .computeIfAbsent(key) { mutableListOf() }
                        .add(spawnData)
                }
                !isLoaded
            }.findAny().isPresent) {
            FxLib.logger.info("Structure chunk positions aren't loaded")
            return false
        }

        ChunkPos.rangeClosed(chunkPosMinBounds, chunkPosMaxBounds).forEach { chunkPos ->
            structureStart.placeInChunk(
                serverLevel,
                serverLevel.structureManager(),
                chunkGenerator,
                serverLevel.getRandom(),
                BoundingBox(
                    chunkPos.minBlockX, serverLevel.minBuildHeight, chunkPos.minBlockZ,
                    chunkPos.maxBlockX, serverLevel.maxBuildHeight, chunkPos.maxBlockZ
                ),
                chunkPos
            )
        }
        alreadyGeneratedStructures.remove(spawnData.spawnId)
        CustomPlacedStructureTracker.get(serverLevel).registerStructure(structureStart, spawnData.pos)
        return true
    }

    // Used for service injection with ServiceLoader, as it expects a constructable class instead of an object
    class ServiceWrapper: FixedStructureGeneration by FixedStructureGenerationImpl

    class Save private constructor (
        val generatedSpawns: MutableSet<String> = HashSet()
    ) : FxSavedData<Save>(CODEC) {

        companion object {
            val CODEC: Codec<Save> = RecordCodecBuilder.create { builder ->
                builder.group(
                    mutableSetCodec(Codec.STRING).optionalFieldOf("generatedSpawns", mutableSetOf()).forGetter{it.generatedSpawns}
                ).apply(builder, FixedStructureGenerationImpl::Save)
            }
            val DEF = define("FixedStructureGeneration", FixedStructureGenerationImpl::Save, CODEC)
        }
    }
}