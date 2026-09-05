package com.gameora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gameora.app.navigation.GameoraNavGraph
import com.gameora.app.ui.theme.GameoraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GameoraTheme {
                GameoraNavGraph()
            }
        }
    }
}