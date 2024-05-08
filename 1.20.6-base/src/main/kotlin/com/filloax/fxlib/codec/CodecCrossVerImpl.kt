package com.filloax.fxlib.codec

import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import java.util.*

// For DataFixerUpper 6.x
class CodecCrossVerImpl : CodecCrossVer {
    override fun <T> optionalFromDataResult(dataResult: DataResult<T>): Optional<T> {
        return dataResult.result()
    }

    override fun <T> validateCodec(codec: MapCodec<T>, checker: (T) -> DataResult<T>): MapCodec<T> {
        return codec.validate(checker)
    }
}