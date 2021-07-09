package com.android.jetpack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Greeting("World")
        }
    }

    @Composable
    fun Greeting(name: String) {
        Text (text = "Hello $name!")
    }

    @Preview
    @Composable
    fun PreviewGreeting() {
        Greeting("Android")
    }
}