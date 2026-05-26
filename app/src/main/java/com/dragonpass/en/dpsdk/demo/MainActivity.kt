package com.dragonpass.en.dpsdk.demo

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.dragonpass.spark.dpweb.web.DPSDK

class MainActivity : AppCompatActivity() {

    private val apps = listOf(
        "lounge" to "Lounge",
        "fastTrack" to "Fast Track"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DPSDK.setAuthCode("AUTH_CODE")

        val listView = findViewById<ListView>(R.id.list_apps)
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, apps.map { it.second })
        listView.setOnItemClickListener { _, _, position, _ ->
            val appId = apps[position].first
            DPSDK.getDPApp(appId).open(this)
        }
    }
}
