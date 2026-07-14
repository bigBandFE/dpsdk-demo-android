package com.dragonpass.en.dpsdk.demo

import android.app.Application
import android.util.Log
import com.dragonpass.en.dpsdk.demo.sso.SSOClient
import com.dragonpass.spark.dpweb.web.DPSDK
import com.dragonpass.spark.dpweb.web.DPSDKConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    companion object {
        private const val TAG = "ShowroomApp"
        const val CLIENT_ID = "SBSA-SDK"
    }

    override fun onCreate() {
        super.onCreate()
        startSDK()
    }

    private fun startSDK() {
        DPSDK.init(this)
            .setDPSdkConfig(
                DPSDKConfig.Builder()
                    .setClientId(CLIENT_ID)
                    .setIsLog(true)
                    .setDebug(true)
                    .setLanguage("en-US")
                    .setEnv(DPSDK.Env.UAT)
                    .build()
            )
            .setGlobalErrorListener { jsException ->
                Log.e(TAG, "DPSDK error: code=${jsException.code}, msg=${jsException.msg}")
            }

        requestAuthCode()
    }

    private fun requestAuthCode() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = SSOClient.getAuthCode().getOrThrow()
                Log.d(TAG, "SSO success: requestId=${result.requestId}")

                DPSDK.setAuthCode(result.token)

                DPSDK.updateConfig(
                    onSuccess = {
                        Log.d(TAG, "Config update success")
                        // 预加载 Lounge 和 Fast Track
                        DPSDK.setPreLoadUrl(
                            MainActivity.currentInstance ?: return@updateConfig,
                            listOf("lounge", "fastTrack")
                        )
                    },
                    onError = { code, msg ->
                        Log.e(TAG, "Config update failed: code=$code, msg=$msg")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "SSO failed: ${e.message}", e)
            }
        }
    }
}
