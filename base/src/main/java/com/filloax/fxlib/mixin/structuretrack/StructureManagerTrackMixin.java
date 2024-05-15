package com.filloax.fxlib.mixin.structuretrack;

import com.filloax.fxlib.api.structure.tracking.CustomPlacedStructureTracker;
import com.filloax.fxlib.api.structure.tracking.PlacedStructureData;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Mixin(StructureManager.class)
public abstract class StructureManagerTrackMixin implements ServerLevelAccessor {
    /* Covers checking if inside a structure for achievements etc */

    @Unique
    private final static Map<Structure, LongSet> EMPTY_MAP = Map.of();

    @Shadow
    private @Final LevelAccessor level;
    @Shadow
    private @Final WorldOptions worldOptions;
    @Shadow
    private @Final StructureCheck structureCheck;


    // Also covers getStructureAt
    @Inject(method = "fillStartsForStructure", at = @At("RETURN"))
    // Structure references are chunk positions converted to longs,
    // more specifically the starting chunk positions of the structure overlapping that chunk
    private void fillStartsForStructure(Structure structure, LongSet structureRefs, Consumer<StructureStart> startConsumer, CallbackInfo ci) {
        for (long startingChunkRef : structureRefs) {
            List<PlacedStructureData> fixedData = tracker().getByStructure(structure);
            for (PlacedStructureData data : fixedData) {
                if (data.getStructureStart().getChunkPos().toLong() == startingChunkRef) {
                    startConsumer.accept(data.getStructureStart());
                }
            }
        }
    }

    @WrapOperation(
            method = "startsForStructure(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/Predicate;)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getAllReferences()Ljava/util/Map;")
    )
    private Map<Structure, LongSet> startsForStructurePredicate(ChunkAccess chunk, Operation<Map<Structure, LongSet>> original) {
        return mergeMaps(
                original.call(chunk),
                tracker().getChunkStructureRefs().getOrDefault(chunk.getPos().toLong(), EMPTY_MAP)
        );
    }

    // Also covers getStructureAt
    @WrapOperation(
            method = "startsForStructure(Lnet/minecraft/core/SectionPos;Lnet/minecraft/world/level/levelgen/structure/Structure;)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getReferencesForStructure(Lnet/minecraft/world/level/levelgen/structure/Structure;)Lit/unimi/dsi/fastutil/longs/LongSet;")
    )
    private LongSet startsForStructure(ChunkAccess chunk, Structure structure, Operation<LongSet> original) {
        return LongStream.concat(
                original.call(chunk, structure).longStream(),
                tracker().getChunkStructureRefs().getOrDefault(chunk.getPos().toLong(), EMPTY_MAP)
                        .getOrDefault(structure, LongSet.of()).longStream()
        ).collect(LongOpenHashSet::new, LongSet::add, LongSet::addAll);
    }

    // This in particular gets the structure start if it starts in the same chunk
    // Since we allow more structures in the same chunk, potentially, return the first one
    @Inject(method = "getStartForStructure", at = @At("HEAD"), cancellable = true)
    private void getStartForStructure(SectionPos sectionPos, Structure structure, StructureAccess structureAccess, CallbackInfoReturnable<StructureStart> cir) {
        List<PlacedStructureData> data = tracker().getStructuresAtChunkPos(sectionPos.chunk(), structure, true);
        if (data.size() > 0) {
            cir.setReturnValue(data.get(0).getStructureStart());
        }
    }

    @Inject(method = "hasAnyStructureAt", at = @At("HEAD"), cancellable = true)
    private void hasAnyStructureAt(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        List<PlacedStructureData> data = tracker().getByPos(pos);
        if (data.size() > 0) {
            cir.setReturnValue(true);
        }
    }

    // As with other methods here, this will return the first structure in the chunk
    // if more are present
    @ModifyReturnValue(method = "getAllStructuresAt", at = @At("RETURN"))
    private Map<Structure, LongSet> getAllStructuresAt(Map<Structure, LongSet> original, BlockPos pos) {
        List<PlacedStructureData> datas = tracker().getByPos(pos);
        return mergeMaps(
                original,
                datas.stream().collect(Collectors.toMap(
                        PlacedStructureData::getStructure,
                        data -> {
                            LongSet set = new LongOpenHashSet();
                            set.add(data.getChunkRef());
                            return set;
                        },
                        (set1, set2) -> {
                            set1.addAll(set2);
                            return set1;
                        },
                        HashMap::new
                ))
        );
    }

    @Unique
    private CustomPlacedStructureTracker tracker() {
        return CustomPlacedStructureTracker.get(getServerLevel());
    }

    @Override
    public @NotNull ServerLevel getLevel() {
        return getServerLevel();
    }

    @Unique
    private ServerLevel getServerLevel() {
        if (level instanceof ServerLevel l) {
            return l;
        } else if (level instanceof WorldGenRegion r) {
            return ((WorldGenRegionAccessor) r).getLevel();
        } else {
            // Should not happen unless like, the game adds a new class
            // If you want to be 100% sure, find usages of StructureManager with intellij
            // and check which classes StructureManager.level might be an instance of
            throw new IllegalStateException("Cannot get ServerLevel from " + level.getClass());
        }
    }

    @Unique
    private <T> Map<T, LongSet> mergeMaps(Map<T, LongSet> map1, Map<T, LongSet> map2) {
        return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                    (set1, set2) -> { // Merge function for sets
                        set1.addAll(set2); // Merge the sets by adding all elements from set2 to set1
                        return set1; // Return the merged set
                }, HashMap::new));
    }
}