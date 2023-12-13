package com.wuhongru.jini

import java.io.File

class WRegistry {

    external fun getAccountInfoFromReg(): HashMap<String, String>
    external fun setRegistryValue(key: String, value: String): Long

    external fun searchYuanShenPath(): String?

    companion object {
        const val key1 = "GENERAL_DATA_h2389025596"
        const val key2 = "MIHOYOSDK_ADL_PROD_CN_h3123967166"

        init {
            val libFile = File(System.getProperty("compose.application.resources.dir")).resolve("wregistry.bin")
            System.load(libFile.absolutePath)
//            val path = "D:\\IntellijProject\\genshinAssistant\\resources\\windows\\wregistry.bin"
//            System.load(path)
        }
    }

}

