package com.filloax.fxlib.structure

import com.google.common.collect.Lists
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.filloax.fxlib.*
import com.filloax.fxlib.nbt.*
import com.filloax.fxlib.codec.*
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.WorldGenerationContext
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece
import net.minecraft.world.level.levelgen.structure.Structure.GenerationContext
import net.minecraft.world.level.levelgen.structure.Structure.GenerationStub
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.Shapes
import java.util.*

/**
 * Variant of Jigsaw structure that forces it to be placed at the exact position instead of chunk start when placed
 * via /place or Remote Structures, use by specifying fxlib:jigsaw_forced_pos as structure type
 * (Defined in FXLibStructures)
 *
 * Allows these additional json params when compared to normal (jigsaw) structures, all optional:
 * - `force_pos_uses_y`: when spawned with a forced pos, also use its y instead of determining it from world height + offset (normally not desirable)
 * - `force_pos_offset`: offset the spawn of the structure from the base position (which is lowest xyz corner of its bounding box)
 * - `default_rotation`: rotate the structure this way by default in remotestructures and other functionality that might use this value from outside
 * - `normal_placement_uses_default_rotation`: always use default_rotation as rotation when spawning naturally
 */
class ForcePosJigsawStructure(
    settings: StructureSettings,
    startPool: Holder<StructureTemplatePool>,
    startJigsawName: Optional<ResourceLocation>,
    maxDepth: Int,
    startHeight: HeightProvider,
    useExpansionHack: Boolean,
    projectStartToHeightmap: Optional<Heightmap.Types>,
    maxDistanceToCenter: Int,
    forcePosUseY: Boolean = false,
    val forcePosOffset: Vec3i = Vec3i.ZERO,
    override val defaultRotation: Rotation? = null,
    val useRotationInDefaultPlacement: Boolean = false,
) : JigsawStructure(
    settings, startPool, startJigsawName, maxDepth, startHeight, useExpansionHack, projectStartToHeightmap,
    maxDistanceToCenter
), FixablePosition, FixableRotation {
    private var nextPlacePosition: BlockPos? = null
    private var nextPlaceRotation: Rotation? = null

    companion object {
        val CODEC: Codec<ForcePosJigsawStructure> = ExtraCodecs.validate(
            // Kotlin compilation fails without <ForcePosJigsawStructure> below, even if Intellij says it can be removed
            RecordCodecBuilder.mapCodec<ForcePosJigsawStructure> { builder ->
                builder.group(
                    settingsCodec(builder),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(JigsawStructure::startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(JigsawStructure::startJigsawName),
                    Codec.intRange(0, 7).fieldOf("size").forGetter(JigsawStructure::maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(JigsawStructure::startHeight),
                    Codec.BOOL.fieldOf("use_expansion_hack").forGetter(JigsawStructure::useExpansionHack),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(JigsawStructure::projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(JigsawStructure::maxDistanceFromCenter),
                    Codec.BOOL.optionalFieldOf("force_pos_uses_y").forGetter{ Optional.of(it.nextPlaceUseY) },
                    Vec3i.CODEC.optionalFieldOf("force_pos_offset").forGetter { Optional.of(it.forcePosOffset) },
                    Rotation.CODEC.optionalFieldOf("default_rotation").forNullableGetter(ForcePosJigsawStructure::defaultRotation),
                    Codec.BOOL.optionalFieldOf("normal_placement_uses_default_rotation").forGetter{ Optional.of(it.useRotationInDefaultPlacement) },
                ).apply(builder) {
                        settings, startPool, startJigsawName, maxDepth, startHeight,
                        useExpansionHack, projectStartToHeightmap, maxDistanceToCenter,
                        forcePosUseYOpt, forcePosOffsetOpt, defaultRotationOpt, useRotationInDefaultPlacementOpt,
                    ->
                    ForcePosJigsawStructure(
                        settings, startPool, startJigsawName, maxDepth, startHeight,
                        useExpansionHack, projectStartToHeightmap, maxDistanceToCenter,
                        forcePosUseYOpt.orElse(false),
                        forcePosOffsetOpt.orElse(Vec3i.ZERO),
                        defaultRotationOpt.orElse(Rotation.NONE),
                        useRotationInDefaultPlacementOpt.orElse(false),
                    )
                }
            }) { structure -> verifyJson(structure) }.codec()

        private fun verifyJson(structure: ForcePosJigsawStructure): DataResult<ForcePosJigsawStructure> {
            val i = when (structure.terrainAdaptation()) {
                TerrainAdjustment.NONE -> 0
                TerrainAdjustment.BURY, TerrainAdjustment.BEARD_THIN, TerrainAdjustment.BEARD_BOX -> 12
                null -> return DataResult.error { "Terrain adaptation must be specified!" }
            }
            if (structure.maxDistanceFromCenter + i > 128)
                return DataResult.error { "Structure size including terrain adaptation must not exceed 128" }
            else if (structure.defaultRotation == null && structure.useRotationInDefaultPlacement)
                return DataResult.error { "Must set a default_rotation if normal_placement_uses_default_rotation is true" }
            else
                return DataResult.success(structure)
        }
    }

    override fun findGenerationPoint(context: GenerationContext): Optional<GenerationStub> {
        val curNextPlacePosition = nextPlacePosition
        val curNextPlaceRotation = nextPlaceRotation
        return if (curNextPlacePosition == null && nextPlaceRotation == null && !useRotationInDefaultPlacement) {
            super.findGenerationPoint(context)
        } else {
            val chunkPos = context.chunkPos()
            val y = startHeight.sample(context.random(), WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()))
            val worldgenRandom = context.random()
            // defaultRotation cannot be null if useRotationInDefaultPlacement, see verify above
            val rotation = curNextPlaceRotation ?: if (useRotationInDefaultPlacement) defaultRotation!! else Rotation.getRandom(worldgenRandom)
            var position = curNextPlacePosition ?: BlockPos(chunkPos.minBlockX, y, chunkPos.minBlockZ)

            // Rotate offset too
            val offset = forcePosOffset.rotate(rotation)
            position = position.offset(offset)
            val pos = if (nextPlaceUseY) position else BlockPos(position.x, y + offset.y, position.z)
            nextPlacePosition = null
            nextPlaceRotation = null

            JigsawPlacementExtra.addPieces(
                context, startPool, startJigsawName, maxDepth, pos, useExpansionHack,
                if (nextPlaceUseY) Optional.empty() else projectStartToHeightmap,
                maxDistanceFromCenter,
                rotation,
            )
        }
    }

    override fun setNextPlacePosition(pos: BlockPos) {
        nextPlacePosition = pos
    }

    override val nextPlaceUseY = forcePosUseY

    override fun setNextPlaceRotation(rotation: Rotation) {
        nextPlaceRotation = rotation
    }

    override fun type(): StructureType<ForcePosJigsawStructure> = FXLibStructures.JIGSAW_FORCE_POS
}

object JigsawPlacementExtra {
    /**
     * Copy the vanilla code, + add a rotation param instead of randomly setting it inside the function,
     * easies way to do this as opposed to mixing it in the function if we call it ourselves anyways
     */
    fun addPieces(
        context: GenerationContext, holder: Holder<StructureTemplatePool>, startJigsawName: Optional<ResourceLocation>,
        maxDepth: Int, blockPos: BlockPos, useExpansionHack: Boolean,
        projectStartToHeightmap: Optional<Heightmap.Types>, maxDistanceFromCenter: Int,
        rotation: Rotation,
    ): Optional<GenerationStub> {
        //after this its more or less an exact copy of og function, just instead of setting rotation here its passed as arg
        //after vanilla updates, check that his still is the same
        val registryAccess = context.registryAccess()
        val chunkGenerator = context.chunkGenerator()
        val worldgenRandom = context.random()
        val structureTemplateManager = context.structureTemplateManager()
        val levelHeightAccessor = context.heightAccessor()
        val registry = registryAccess.registryOrThrow(Registries.TEMPLATE_POOL)
        val structureTemplatePool = holder.value()
        val structurePoolElement = structureTemplatePool.getRandomTemplate(worldgenRandom)
        return if (structurePoolElement === EmptyPoolElement.INSTANCE) {
            Optional.empty()
        } else {
            val blockPos2: BlockPos = if (startJigsawName.isPresent) {
                val resourceLocation = startJigsawName.get()
                val optional3 = JigsawPlacement.getRandomNamedJigsaw(
                    structurePoolElement, resourceLocation, blockPos, rotation, structureTemplateManager, worldgenRandom
                )
                if (optional3.isEmpty) {
                    FXLib.logger.error(
                        "No starting jigsaw {} found in start pool {}",
                        resourceLocation,
                        holder.unwrapKey().map { resourceKey: ResourceKey<StructureTemplatePool> ->
                            resourceKey.location().toString()
                        }.orElse("<unregistered>")
                    )
                    return Optional.empty()
                }
                optional3.get()
            } else {
                blockPos
            }
            val vec3i: Vec3i = blockPos2.subtract(blockPos)
            val blockPos3 = blockPos.subtract(vec3i)
            val poolElementStructurePiece = PoolElementStructurePiece(
                structureTemplateManager,
                structurePoolElement,
                blockPos3,
                structurePoolElement.groundLevelDelta,
                rotation,
                structurePoolElement.getBoundingBox(structureTemplateManager, blockPos3, rotation)
            )
            val boundingBox = poolElementStructurePiece.boundingBox
            val k = (boundingBox.maxX() + boundingBox.minX()) / 2
            val l = (boundingBox.maxZ() + boundingBox.minZ()) / 2
            val m = if (projectStartToHeightmap.isPresent) {
                blockPos.y + chunkGenerator.getFirstFreeHeight(
                    k, l, projectStartToHeightmap.get(), levelHeightAccessor, context.randomState()
                )
            } else {
                blockPos3.y
            }
            val n = boundingBox.minY() + poolElementStructurePiece.groundLevelDelta
            poolElementStructurePiece.move(0, m - n, 0)
            val o = m + vec3i.y
            Optional.of(GenerationStub(BlockPos(k, o, l)) { structurePiecesBuilder: StructurePiecesBuilder ->
                val list: MutableList<PoolElementStructurePiece> = Lists.newArrayList()
                list.add(poolElementStructurePiece)
                if (maxDepth > 0) {
                    val aABB = AABB(
                        (k - maxDistanceFromCenter).toDouble(), (o - maxDistanceFromCenter).toDouble(), (l - maxDistanceFromCenter).toDouble(), (k + maxDistanceFromCenter + 1).toDouble(),
                        (o + maxDistanceFromCenter + 1).toDouble(), (l + maxDistanceFromCenter + 1).toDouble()
                    )
                    val voxelShape = Shapes.join(
                        Shapes.create(aABB), Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST
                    )
                    JigsawPlacement.addPieces(
                        context.randomState(),
                        maxDepth,
                        useExpansionHack,
                        chunkGenerator,
                        structureTemplateManager,
                        levelHeightAccessor,
                        worldgenRandom,
                        registry,
                        poolElementStructurePiece,
                        list,
                        voxelShape
                    )
                    list.forEach(structurePiecesBuilder::addPiece)
                }
            })
        }
    }
}