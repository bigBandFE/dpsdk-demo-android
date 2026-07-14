package com.dragonpass.en.dpsdk.demo

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dragonpass.spark.dpweb.web.DPSDK

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ShowroomMain"
        var currentInstance: MainActivity? = null
            private set
    }

    private val apps = listOf(
        "lounge" to "Lounge",
        "fastTrack" to "Fast Track",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 为每个 DPApp 注册 Showroom Custom Delegate
        apps.forEach { (appId, _) ->
            DPSDK.getDPApp(appId).setCustomEventListener { activity, data, event, callback ->
                ShowroomCustomDelegate.handle(activity, data, event, callback)
            }
        }

        val listView = findViewById<ListView>(R.id.list_apps)
        listView.adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, apps.map { it.second }
        )
        listView.setOnItemClickListener { _, _, position, _ ->
            val appId = apps[position].first
            val authCode = DPSDK.getAuthCode()
            if (authCode.isNullOrEmpty()) {
                Toast.makeText(this, "SSO not ready yet, please wait...", Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }
            DPSDK.getDPApp(appId).open(this)
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
