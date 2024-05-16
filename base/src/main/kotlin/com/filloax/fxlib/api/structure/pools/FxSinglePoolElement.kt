package com.filloax.fxlib.api.structure.pools

import com.filloax.fxlib.api.codec.codec
import com.filloax.fxlib.api.json.BlockPosSerializer
import com.filloax.fxlib.api.json.BoundingBoxSerializer
import com.filloax.fxlib.api.optional
import com.filloax.fxlib.structure.FXLibStructurePoolElements
import com.google.common.collect.Lists
import com.mojang.datafixers.util.Either
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.serialization.Serializable
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import java.util.*
import kotlin.jvm.optionals.getOrNull

class FxSinglePoolElement(
    template: Either<ResourceLocation, StructureTemplate>,
    processors: Holder<StructureProcessorList>,
    projection: StructureTemplatePool.Projection,
    placeSettingsDefaults: Optional<StructurePlaceSettingsDefaults>
) : SinglePoolElement(template, processors, projection) {
    val placeSettingsDefaults = placeSettingsDefaults.getOrNull()

    companion object {
        val CODEC: MapCodec<FxSinglePoolElement> = RecordCodecBuilder.mapCodec { builder ->
            builder.group(
                templateCodec(),
                processorsCodec(),
                projectionCodec(),
                StructurePlaceSettingsDefaults.serializer().codec().optionalFieldOf("placeSettingsDefaults").forGetter(FxSinglePoolElement::placeSettingsDefaults.optional()),
            ).apply(builder, ::FxSinglePoolElement)
        }
    }

    override fun getType(): StructurePoolElementType<*> = FXLibStructurePoolElements.FX_SINGLE_POOL_ELEMENT

    override fun getSettings(rotation: Rotation, boundingBox: BoundingBox, offset: Boolean): StructurePlaceSettings {
        val settings = super.getSettings(rotation, boundingBox, offset)

        placeSettingsDefaults?.let { defaults ->
            defaults.mirror?.let { settings.mirror = it }
            defaults.rotation?.let { settings.rotation = it }
            defaults.rotationPivot?.let { settings.rotationPivot = it }
            defaults.isIgnoreEntities?.let { settings.isIgnoreEntities = it }
            defaults.keepLiquids?.let { settings.setKeepLiquids(it) }
            defaults.knownShape?.let { settings.knownShape = it }
            defaults.finalizeEntities?.let { settings.setFinalizeEntities(it) }
        }

        return settings
    }

    @Serializable
    data class StructurePlaceSettingsDefaults(
        val mirror: Mirror? = null,
        val rotation: Rotation? = null,
        @Serializable(with = BlockPosSerializer::class)
        val rotationPivot: BlockPos? = null,
        val isIgnoreEntities: Boolean? = null,
//        @Serializable(with = BoundingBoxSerializer::class)
//        val boundingBox: BoundingBox? = null,
        val keepLiquids: Boolean? = null,
        // Unused
//        val palette: Int? = null,
        // Not worth time to make serializer for
//        val processors: List<StructureProcessor>? = null,
        val knownShape: Boolean? = null,
        val finalizeEntities: Boolean? = null
    )
}