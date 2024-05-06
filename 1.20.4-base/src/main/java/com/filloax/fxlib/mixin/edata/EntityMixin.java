package com.filloax.fxlib.mixin.edata;

import com.filloax.fxlib.interfaces.WithPersistentData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements WithPersistentData {
    @Unique
    private CompoundTag persistentData;

    @Override
    public CompoundTag ruins_of_fxlib$getPersistentData() {
        if(this.persistentData == null) {
            this.persistentData = new CompoundTag();
        }
        return persistentData;
    }

    @Inject(method = "saveWithoutId", at = @At("HEAD"))
    protected void injectWriteMethod(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> info) {
        if(persistentData != null) {
            nbt.put("fxlib.entdata", persistentData);
        }
    }

    @Inject(method = "load", at = @At("HEAD"))
    protected void injectReadMethod(CompoundTag nbt, CallbackInfo info) {
        if (nbt.contains("fxlib.entdata", Tag.TAG_COMPOUND)) {
            persistentData = nbt.getCompound("fxlib.entdata");
        }
    }
}
