package com.filloax.fxlib.client

abstract class FxLibClient  {
    fun initialize() {
        InternalUtilsClient.initClientUtils()
    }
}