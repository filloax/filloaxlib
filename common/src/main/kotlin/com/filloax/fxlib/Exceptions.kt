package com.filloax.fxlib

import net.minecraft.resources.Identifier
import java.lang.RuntimeException

class UnknownStructureIdException : RuntimeException {
    constructor(id: Identifier) : super("No such structure $id")
    constructor(id: Identifier, message: String) : super("$message | No such structure $id")
}

class SaveDataTypeException : RuntimeException {
    constructor(cause: Throwable) : super("FxSaveData doesn't self reference in type parameter!", cause)
    constructor() : super("FxSaveData doesn't self reference in type parameter!")
}