package com.wuhongru.jini

import java.io.File

class WRegistry {

    external fun getAccountInfoFromReg(): HashMap<String, String>
    external fun setRegistryValue(key: String, value: String): Long

    companion object {
        const val key1 = "GENERAL_DATA_h2389025596"
        const val key2 = "MIHOYOSDK_ADL_PROD_CN_h3123967166"

        init {
            val libFile = File(System.getProperty("compose.application.resources.dir")).resolve("wregistry.bin")
            System.load(libFile.absolutePath)
        }
    }

}