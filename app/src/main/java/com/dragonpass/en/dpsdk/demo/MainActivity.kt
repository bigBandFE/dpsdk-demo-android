package com.dragonpass.en.dpsdk.demo

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dragonpass.en.dpsdk.demo.sso.SSOClient
import com.dragonpass.spark.dpweb.web.DPSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ShowroomMain"
        var currentInstance: MainActivity? = null
            private set
    }

    // ── Per-category config (mirrors iOS DemoDPApp + DemoLoginConfig) ──

    data class CategoryConfig(
        val appId: String,
        val title: String,
        val productCode: String,
        val tenantCode: String,
        val memberShipCode: String,
        val module: String,
        val redirectURL: String,
        val pageType: String,
        val loginPath: String,
    )

    private val apps = listOf(
        CategoryConfig(
            appId = "lounge",
            title = "Lounge",
            productCode = "IL0494000001",
            tenantCode = "0494",
            memberShipCode = "8576376517994777",
            module = "1",
            redirectURL = "https://g-front-uat.dragonpass.com/standard-lfd/#/lounge/landing",
            pageType = "landing",
            loginPath = "/api/business/auth/visitor/login",
        ),
        CategoryConfig(
            appId = "fastTrack",
            title = "Fast Track",
            productCode = "QT0494000001",
            tenantCode = "0494",
            memberShipCode = "8576376517994777",
            module = "2",
            redirectURL = "https://g-front-uat.dragonpass.com/standard-lfd/#/fast-track/landing",
            pageType = "landing",
            loginPath = "/api/business/auth/visitor/login",
        ),
    )

    private lateinit var loadingBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadingBar = findViewById(R.id.loading_bar)

        // 为每个 DPApp 注册 Showroom Custom Delegate
        apps.forEach { config ->
            DPSDK.getDPApp(config.appId).setCustomEventListener { activity, data, event, callback ->
                ShowroomCustomDelegate.handle(activity, data, event, callback)
            }
        }

        val listView = findViewById<ListView>(R.id.list_apps)
        listView.adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, apps.map { it.title }
        )
        listView.setOnItemClickListener { _, _, position, _ ->
            val config = apps[position]
            launchApp(config)
        }
    }

    private fun launchApp(config: CategoryConfig) {
        loadingBar.visibility = ProgressBar.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Per-category SSO (mirrors iOS DemoDPApp.login())
                Log.d(TAG, "SSO start: appId=${config.appId}, productCode=${config.productCode}")
                val result = SSOClient.getAuthCode(
                    productCode = config.productCode,
                    tenantCode = config.tenantCode,
                    memberShipCode = config.memberShipCode,
                    module = config.module,
                    redirectURL = config.redirectURL,
                    pageType = config.pageType,
                    loginPath = config.loginPath,
                ).getOrThrow()
                Log.d(TAG, "SSO success: appId=${config.appId}, requestId=${result.requestId}")

                withContext(Dispatchers.Main) {
                    DPSDK.setAuthCode(result.token)
                    DPSDK.getDPApp(config.appId).open(this@MainActivity)
                }
            } catch (e: Exception) {
                Log.e(TAG, "SSO failed for ${config.appId}: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Login failed: ${config.title}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    loadingBar.visibility = ProgressBar.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currentInstance = this
    }

    override fun onPause() {
        super.onPause()
        if (currentInstance == this) {
            currentInstance = null
        }
    }
}
