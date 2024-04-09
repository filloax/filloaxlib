package com.filloax.fxlib;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public class MixinHelpers {
    public static final ServerHolder SERVER_HOLDER = new ServerHolder();

    public static class ServerHolder {
        private @Nullable MinecraftServer instance = null;

        public @Nullable MinecraftServer get() {
            return instance;
        }

        public void set(MinecraftServer inst) {
            this.instance = inst;
        }
    }

}
