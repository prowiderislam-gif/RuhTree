package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.HomeScreen
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.RuhTreeTheme
import com.example.ui.viewmodel.FamilyTreeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FamilyTreeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RuhTreeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianBg
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
