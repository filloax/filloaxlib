package com.filloax.fxlib.api.codec

import com.filloax.fxlib.api.platform.ServiceUtil
import com.filloax.fxlib.codec.CodecCrossVerImpl
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import java.util.*

// Cross ver handling

interface CodecCrossVer {
    fun <T> optionalFromDataResult(dataResult: DataResult<T>): Optional<T>
    fun <T> validateCodec(codec: MapCodec<T>, checker: (T) -> DataResult<T>): MapCodec<T>

    companion object {
        val inst = CodecCrossVerImpl()
    }
}