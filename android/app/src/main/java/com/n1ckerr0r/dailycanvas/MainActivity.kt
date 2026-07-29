package com.n1ckerr0r.dailycanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.n1ckerr0r.dailycanvas.ui.DailyCanvasApp
import com.n1ckerr0r.dailycanvas.ui.theme.AppBackground
import com.n1ckerr0r.dailycanvas.ui.theme.DailyCanvasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyCanvasTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppBackground),
                ) {
                    DailyCanvasApp()
                }
            }
        }
    }
}
