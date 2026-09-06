package com.newcamera.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.newcamera.app.ui.CameraApp
import com.newcamera.app.ui.theme.NewCameraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewCameraTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    CameraApp()
                }
            }
        }
    }
}
