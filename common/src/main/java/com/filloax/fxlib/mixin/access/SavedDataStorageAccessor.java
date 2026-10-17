package com.filloax.fxlib.mixin.access;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;

@Mixin(SavedDataStorage.class)
public interface SavedDataStorageAccessor {
    @Invoker
    Path callGetDataFile(Identifier id);
}
