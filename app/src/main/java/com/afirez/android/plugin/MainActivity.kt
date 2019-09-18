package com.afirez.android.plugin

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvSeePlugin.setOnClickListener {
            startActivity(
                Intent().setComponent(
                    ComponentName(
                        "com.afirez.android.app.plugin",
                        "com.afirez.android.app.plugin.MainActivity"
                    )
                )
            )
        }
    }

}
