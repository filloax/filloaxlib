package com.filloax.fxlib.api.structure

import com.filloax.fxlib.*
import com.filloax.fxlib.api.codec.*
import com.filloax.fxlib.api.rotate
import com.filloax.fxlib.structure.FXLibStructures
import com.google.common.collect.Lists
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.WorldGenerationContext
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece
import net.minecraft.world.level.levelgen.structure.Structure.GenerationContext
import net.minecraft.world.level.levelgen.structure.Structure.GenerationStub
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.Shapes
import java.util.*
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

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
    poolAliases: List<PoolAliasBinding>,
    dimensionPadding: DimensionPadding,
    liquidSettings: LiquidSettings,
    private val defaultForcePosUsesY: Boolean = false,
    val forcePosOffset: Vec3i = Vec3i.ZERO,
    override val defaultRotation: Rotation? = null,
    val useRotationInDefaultPlacement: Boolean = false,
) : JigsawStructure(
    settings, startPool, startJigsawName, maxDepth, startHeight, useExpansionHack, projectStartToHeightmap,
    maxDistanceToCenter, poolAliases, dimensionPadding, liquidSettings
), FixablePosition, FixableRotation {
    private var nextPlacePosition: BlockPos? = null
    private var nextPlaceRotation: Rotation? = null
    private var forcePosUsesY = defaultForcePosUsesY

    companion object {
        /**
         * Alternative to constructor with default values for most args,
         * to be used for datagen
         */
        fun build(
            startPool: Holder<StructureTemplatePool>,
            biomes: HolderSet<Biome>,
            spawnOverrides: Map<MobCategory, StructureSpawnOverride> = mapOf(),
            step: GenerationStep.Decoration = GenerationStep.Decoration.SURFACE_STRUCTURES,
            terrainAdaptation: TerrainAdjustment = TerrainAdjustment.NONE,
            startJigsawName: ResourceLocation? = null,
            size: Int = 7,
            startHeight: HeightProvider = ConstantHeight.ZERO,
            useExpansionHack: Boolean = false,
            projectStartToHeightmap: Heightmap.Types? = Heightmap.Types.WORLD_SURFACE_WG,
            maxDistanceToCenter: Int = 80,
            poolAliases: List<PoolAliasBinding> = listOf(),
            dimensionPadding: DimensionPadding,
            liquidSettings: LiquidSettings,
            forcePosUsesY: Boolean = true,
            forcePosOffset: Vec3i = Vec3i.ZERO,
            defaultRotation: Rotation? = null,
            useRotationInDefaultPlacement: Boolean = false,
        ): ForcePosJigsawStructure {
            return ForcePosJigsawStructure(
                StructureSettings(biomes, spawnOverrides, step, terrainAdaptation),
                startPool, Optional.ofNullable(startJigsawName), size, startHeight, useExpansionHack,
                Optional.ofNullable(projectStartToHeightmap), maxDistanceToCenter, poolAliases,
                dimensionPadding, liquidSettings,
                forcePosUsesY, forcePosOffset, defaultRotation, useRotationInDefaultPlacement
            )
        }

        val CODEC: MapCodec<ForcePosJigsawStructure> = CodecCrossVer.inst.validateCodec(
            // Kotlin compilation fails without <ForcePosJigsawStructure> below, even if Intellij says it can be removed
            RecordCodecBuilder.mapCodec { builder ->
                builder.group(
                    settingsCodec(builder),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(JigsawStructure::startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(JigsawStructure::startJigsawName),
                    Codec.intRange(0, 7).fieldOf("size").forGetter(JigsawStructure::maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(JigsawStructure::startHeight),
                    Codec.BOOL.fieldOf("use_expansion_hack").forGetter(JigsawStructure::useExpansionHack),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(JigsawStructure::projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(JigsawStructure::maxDistanceFromCenter),
                    Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", listOf()).forGetter { it.poolAliases },
                    DimensionPadding.CODEC.optionalFieldOf("dimension_padding", DEFAULT_DIMENSION_PADDING).forGetter { it.dimensionPadding },
                    LiquidSettings.CODEC.optionalFieldOf("liquid_settings", DEFAULT_LIQUID_SETTINGS).forGetter { it.liquidSettings },
                    Codec.BOOL.optionalFieldOf("force_pos_uses_y").forGetter{ Optional.of(it.nextPlaceUseY) },
                    Vec3i.CODEC.optionalFieldOf("force_pos_offset").forGetter { Optional.of(it.forcePosOffset) },
                    Rotation.CODEC.optionalFieldOf("default_rotation").forNullableGetter(ForcePosJigsawStructure::defaultRotation),
                    Codec.BOOL.optionalFieldOf("normal_placement_uses_default_rotation").forGetter{ Optional.of(it.useRotationInDefaultPlacement) },
                ).apply(builder) {
                        settings, startPool, startJigsawName, maxDepth, startHeight,
                        useExpansionHack, projectStartToHeightmap, maxDistanceToCenter, poolAliases,
                        dimensionPadding, liquidSettings,
                        forcePosUseYOpt, forcePosOffsetOpt, defaultRotationOpt, useRotationInDefaultPlacementOpt,
                    ->
                    ForcePosJigsawStructure(
                        settings, startPool, startJigsawName, maxDepth, startHeight,
                        useExpansionHack, projectStartToHeightmap, maxDistanceToCenter, poolAliases,
                        dimensionPadding, liquidSettings,
                        forcePosUseYOpt.orElse(false),
                        forcePosOffsetOpt.orElse(Vec3i.ZERO),
                        defaultRotationOpt.orElse(Rotation.NONE),
                        useRotationInDefaultPlacementOpt.orElse(false),
                    )
                }
            }) { structure -> verifyJson(structure) }

        private fun verifyJson(structure: ForcePosJigsawStructure): DataResult<ForcePosJigsawStructure> {
            val i = when (structure.terrainAdaptation()) {
                TerrainAdjustment.NONE -> 0
                else -> 12
            }
            return if (structure.maxDistanceFromCenter + i > 128)
                DataResult.error { "Structure size including terrain adaptation must not exceed 128" }
            else if (structure.defaultRotation == null && structure.useRotationInDefaultPlacement)
                DataResult.error { "Must set a default_rotation if normal_placement_uses_default_rotation is true" }
            else
                DataResult.success(structure)
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
            val thisGenUsesForcedY = forcePosUsesY
            forcePosUsesY = defaultForcePosUsesY
            val pos = if (thisGenUsesForcedY) position else BlockPos(position.x, y + offset.y, position.z)
            nextPlacePosition = null
            nextPlaceRotation = null

            JigsawPlacementExtra.addPieces(
                context, startPool, startJigsawName, maxDepth, pos, useExpansionHack,
                if (thisGenUsesForcedY) Optional.empty() else projectStartToHeightmap,
                maxDistanceFromCenter,
                PoolAliasLookup.create(this.poolAliases, pos, context.seed()),
                dimensionPadding, liquidSettings,
                rotation,
            )
        }
    }

    override fun setNextPlacePosition(pos: BlockPos, useY: Boolean?) {
        nextPlacePosition = pos
        forcePosUsesY = useY ?: defaultForcePosUsesY
    }

    override val nextPlaceUseY = forcePosUsesY

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
        context: GenerationContext,
        startPool: Holder<StructureTemplatePool>,
        startJigsawName: Optional<ResourceLocation>,
        maxDepth: Int,
        pos: BlockPos,
        useExpansionHack: Boolean,
        projectStartToHeightmap: Optional<Heightmap.Types>,
        maxDistanceFromCenter: Int,
        aliasLookup: PoolAliasLookup,
        dimensionPadding: DimensionPadding,
        liquidSettings: LiquidSettings,
        rotation: Rotation,
    ): Optional<GenerationStub> {
        //after this its more or less an exact copy of og function, just instead of setting rotation here its passed as arg
        //after vanilla updates, check that his still is the same
        val registryAccess = context.registryAccess()
        val chunkGenerator = context.chunkGenerator()
        val structureTemplateManager = context.structureTemplateManager()
        val levelHeightAccessor = context.heightAccessor()
        val worldgenRandom = context.random()
        val registry = registryAccess.registryOrThrow(Registries.TEMPLATE_POOL)
        val structureTemplatePool = startPool.unwrapKey()
            .flatMap { resourceKey: ResourceKey<StructureTemplatePool?>? ->
                registry.getOptional(
                    aliasLookup.lookup(
                        resourceKey!!
                    )
                )
            }
            .orElse(startPool.value()) as StructureTemplatePool
        val structurePoolElement = structureTemplatePool.getRandomTemplate(worldgenRandom)
        if (structurePoolElement === EmptyPoolElement.INSTANCE) {
            return Optional.empty()
        } else {
            val blockPos: BlockPos
            if (startJigsawName.isPresent) {
                val resourceLocation = startJigsawName.get()
                val optional = JigsawPlacement.getRandomNamedJigsaw(
                    structurePoolElement, resourceLocation, pos, rotation, structureTemplateManager, worldgenRandom
                )
                if (optional.isEmpty) {
                    FxLib.logger.error(
                        "No starting jigsaw {} found in start pool {}",
                        resourceLocation,
                        startPool.unwrapKey().map { resourceKey: ResourceKey<StructureTemplatePool?> ->
                            resourceKey.location().toString()
                        }.orElse("<unregistered>")
                    )
                    return Optional.empty()
                }

                blockPos = optional.get()
            } else {
                blockPos = pos
            }

            val vec3i: Vec3i = blockPos.subtract(pos)
            val blockPos2 = pos.subtract(vec3i)
            val poolElementStructurePiece = PoolElementStructurePiece(
                structureTemplateManager,
                structurePoolElement,
                blockPos2,
                structurePoolElement.groundLevelDelta,
                rotation,
                structurePoolElement.getBoundingBox(structureTemplateManager, blockPos2, rotation),
                liquidSettings
            )
            val boundingBox = poolElementStructurePiece.boundingBox
            val i = (boundingBox.maxX() + boundingBox.minX()) / 2
            val j = (boundingBox.maxZ() + boundingBox.minZ()) / 2
            val k: Int
            if (projectStartToHeightmap.isPresent) {
                k = pos.y + chunkGenerator.getFirstFreeHeight(
                    i, j, projectStartToHeightmap.get(), levelHeightAccessor, context.randomState()
                )
            } else {
                k = blockPos2.y
            }

            val l = boundingBox.minY() + poolElementStructurePiece.groundLevelDelta
            poolElementStructurePiece.move(0, k - l, 0)
            val m = k + vec3i.y
            return Optional.of<GenerationStub>(
                GenerationStub(
                    BlockPos(i, m, j)
                ) { structurePiecesBuilder: StructurePiecesBuilder ->
                    val list: MutableList<PoolElementStructurePiece> = Lists.newArrayList()
                    list.add(poolElementStructurePiece)
                    if (maxDepth > 0) {
                        val aABB = AABB(
                            (i - maxDistanceFromCenter).toDouble(),
                            max(
                                (m - maxDistanceFromCenter).toDouble(),
                                (levelHeightAccessor.getMinBuildHeight() + dimensionPadding.bottom()).toDouble()
                            ),
                            (j - maxDistanceFromCenter).toDouble(),
                            (i + maxDistanceFromCenter + 1).toDouble(),
                            min(
                                (m + maxDistanceFromCenter + 1).toDouble(),
                                (levelHeightAccessor.getMaxBuildHeight() - dimensionPadding.top()).toDouble()
                            ),
                            (j + maxDistanceFromCenter + 1).toDouble()
                        )
                        val voxelShape = Shapes.join(
                            Shapes.create(aABB),
                            Shapes.create(AABB.of(boundingBox)),
                            BooleanOp.ONLY_FIRST
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
                            voxelShape,
                            aliasLookup,
                            liquidSettings
                        )
                        list.forEach(
                            Consumer { `$$0`: PoolElementStructurePiece? ->
                                structurePiecesBuilder.addPiece(
                                    `$$0`!!
                                )
                            })
                    }
                }
            )
        }
    }
}