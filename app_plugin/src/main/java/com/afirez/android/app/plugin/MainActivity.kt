package com.afirez.android.app.plugin

import android.app.Activity
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvPlugin.setOnClickListener {
            startActivity(Intent(this, TestActivity::class.java))
        }

    }

    override fun getResources(): Resources {
        return if (application != null && application.resources != null) {
            application.resources
        } else {
            super.getResources()
        }
    }

    override fun getAssets(): AssetManager {
        return if (application != null && application.assets != null) {
            application.assets
        } else {
            super.getAssets()
        }
    }

    override fun getTheme(): Resources.Theme {
        return if (application != null && application.theme != null) {
            application.theme
        } else {
            super.getTheme()
        }
    }
}
