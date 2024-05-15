package com.filloax.fxlib.codec

import com.filloax.fxlib.platform.ServiceUtil
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import java.util.*

// Cross ver handling

interface CodecCrossVer {
    fun <T> optionalFromDataResult(dataResult: DataResult<T>): Optional<T>
    fun <T> validateCodec(codec: MapCodec<T>, checker: (T) -> DataResult<T>): MapCodec<T>

    companion object {
        val inst = ServiceUtil.findService(CodecCrossVer::class.java)
    }
}