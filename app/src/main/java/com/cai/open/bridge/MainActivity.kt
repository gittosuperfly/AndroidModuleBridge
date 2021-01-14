package com.cai.open.bridge

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cai.bridge.ModuleBridge

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ModuleBridge.get(IBridge::class.java).function()
    }
}