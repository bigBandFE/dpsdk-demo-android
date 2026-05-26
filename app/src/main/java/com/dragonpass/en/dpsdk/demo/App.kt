package com.dragonpass.en.dpsdk.demo

import android.app.Application
import com.dragonpass.spark.dpweb.web.DPSDK
import com.dragonpass.spark.dpweb.web.DPSDKConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DPSDK.init(this)
            .setDPSdkConfig(
                DPSDKConfig.Builder()
                    .setClientId("CLIENT_ID")
                    .build()
            )
    }
}
