package com.filloax.fxlib.mixin.structuretrack;

import com.filloax.fxlib.FxLib;
import com.filloax.fxlib.api.structure.tracking.CustomPlacedStructureTracker;
import com.filloax.fxlib.api.structure.tracking.FixedStructurePlacement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.filloax.fxlib.api.structure.tracking.PlacedStructureData;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(ChunkGenerator.class)
// Currently DISABLED as this led to more issues than it was worth, meaning fixedstructures won't be findable with /locate and similar
// (most usecases will use it directly anyways)
public abstract class ChunkGeneratorTrackMixin {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGeneratorStructureState;getPlacementsForStructure(Lnet/minecraft/core/Holder;)Ljava/util/List;"),
            method = "findNearestMapStructure")
    private List<StructurePlacement> findNearestMapStructureChangePlacement(ChunkGeneratorStructureState instance, Holder<Structure> structure, Operation<List<StructurePlacement>> original, ServerLevel level) {
        List<StructurePlacement> basePlacements = original.call(instance, structure);
        Stream<StructurePlacement> fixedPlacements = structure.unwrap().map(
                key -> CustomPlacedStructureTracker.get(level).getByStructure(key),
                struct -> CustomPlacedStructureTracker.get(level).getByStructure(struct)
            ).stream().map(PlacedStructureData::getPlacement);

        return Stream.concat(basePlacements.stream(), fixedPlacements).toList();
    }

    @Inject(
        at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Ljava/util/Map$Entry;getKey()Ljava/lang/Object;", ordinal = 0),
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;structureManager()Lnet/minecraft/world/level/StructureManager;")),
        method = "findNearestMapStructure"
    )
    private void findNearestMapStructureCheckPlacement(
        ServerLevel level, HolderSet<Structure> structureToCheck, BlockPos pos, int searchRadius, boolean skipKnownStructures,
        CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir,
        @Local(ordinal = 0) LocalRef<Pair<BlockPos, Holder<Structure>>> pair,
        @Local(ordinal = 0) LocalDoubleRef d,
        @Local(ordinal = 0) Map.Entry<StructurePlacement, Set<Holder<Structure>>> entry
    ) {
        StructurePlacement placement = entry.getKey();

        if (placement instanceof FixedStructurePlacement fixedStructurePlacement) {
            double currentDist = pos.distSqr(fixedStructurePlacement.getPos());
            if (currentDist < d.get()) {
                d.set(currentDist);

                if (entry.getValue().size() > 1) {
                    FxLib.logger.log(Level.WARN, "Fixed structure holder set has more than one value for " + entry.getValue());
                }

                // Should only have one value per spawn
                Holder<Structure> structure = entry.getValue().stream().findFirst().orElseThrow();

                pair.set(new Pair<>(fixedStructurePlacement.getPos(), structure));
            }
        }
    }
}
