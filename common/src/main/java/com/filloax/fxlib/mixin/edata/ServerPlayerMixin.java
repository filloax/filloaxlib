package com.filloax.fxlib.mixin.edata;

import com.filloax.fxlib.api.interfaces.WithPersistentData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The player entity is recreated on respawn/end exit, keep persistent data across it
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void copyPersistentData(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        CompoundTag oldData = ((WithPersistentData) oldPlayer).ruins_of_fxlib$getPersistentData();
        if (!oldData.isEmpty()) {
            ((WithPersistentData) (Object) this).ruins_of_fxlib$getPersistentData().merge(oldData.copy());
        }
    }
}
