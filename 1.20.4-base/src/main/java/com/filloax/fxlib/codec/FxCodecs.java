package com.filloax.fxlib.codec;

import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class FxCodecs {
    public static final Codec<AABB> AABB = Codec.DOUBLE.listOf().<AABB>comapFlatMap(
            dl -> Util.fixedSize(dl, 6).map(
                l -> new AABB(l.get(0), l.get(1), l.get(2), l.get(3), l.get(4), l.get(5))
            ),
            aabb -> List.of(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ)
        )
        .stable();
}
