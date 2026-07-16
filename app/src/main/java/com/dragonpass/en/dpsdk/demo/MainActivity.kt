package com.dragonpass.en.dpsdk.demo

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dragonpass.en.dpsdk.demo.sso.SSOClient
import com.dragonpass.spark.dpweb.web.DPSDK
import com.google.android.material.card.MaterialCardView
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
        val subtitle: String,
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
            subtitle = "Launch lounge discovery and booking flows.",
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
            subtitle = "Open priority airport security experiences.",
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

        // Hero title with indigo highlight on "travel experiences"
        setupHeroTitle()

        // Register custom delegates
        apps.forEach { config ->
            DPSDK.getDPApp(config.appId).setCustomEventListener { activity, data, event, callback ->
                ShowroomCustomDelegate.handle(activity, data, event, callback)
            }
        }

        setupButtons()
    }

    private fun setupHeroTitle() {
        val titleView = findViewById<TextView>(R.id.hero_title)
        val fullText = "Build travel experiences with DP SDK"
        val span = SpannableString(fullText)

        // Highlight "travel experiences" in indigo
        val highlightText = "travel experiences"
        val start = fullText.indexOf(highlightText)
        if (start >= 0) {
            val end = start + highlightText.length
            span.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.home_indigo, null)),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        titleView.text = span
    }

    private fun setupButtons() {
        val loungeCard = findViewById<MaterialCardView>(R.id.btn_lounge)
        val fastTrackCard = findViewById<MaterialCardView>(R.id.btn_fast_track)

        setupButton(
            card = loungeCard,
            iconResId = R.drawable.ic_lounge,
            config = apps[0],
            appIdLabel = "lounge",
        )

        setupButton(
            card = fastTrackCard,
            iconResId = R.drawable.ic_fast_track,
            config = apps[1],
            appIdLabel = "fastTrack",
        )
    }

    private fun setupButton(
        card: MaterialCardView,
        iconResId: Int,
        config: CategoryConfig,
        appIdLabel: String,
    ) {
        card.findViewById<ImageView>(R.id.card_icon).setImageResource(iconResId)
        card.findViewById<TextView>(R.id.card_title).text = config.title
        card.findViewById<TextView>(R.id.card_subtitle).text = config.subtitle
        card.findViewById<TextView>(R.id.card_badge).text = "SDK appId: $appIdLabel"

        card.setOnClickListener { launchApp(config) }
    }

    private fun launchApp(config: CategoryConfig) {
        loadingBar.visibility = ProgressBar.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
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
