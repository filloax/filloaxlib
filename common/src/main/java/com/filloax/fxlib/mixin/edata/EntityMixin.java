package com.filloax.fxlib.mixin.edata;

import com.filloax.fxlib.api.interfaces.WithPersistentData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    protected void injectWriteMethod(ValueOutput output, CallbackInfo ci) {
        if(persistentData != null) {
            output.store("fxlib.entdata", CompoundTag.CODEC, persistentData);
        }
    }

    @Inject(method = "load", at = @At("HEAD"))
    protected void injectReadMethod(ValueInput input, CallbackInfo ci) {
        persistentData = input.read("fxlib.entdata", CompoundTag.CODEC).get();
    }
}
