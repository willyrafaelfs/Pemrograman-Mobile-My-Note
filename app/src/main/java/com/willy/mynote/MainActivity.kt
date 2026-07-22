package com.willy.mynote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.willy.mynote.navigation.MyNoteNavGraph
import com.willy.mynote.ui.theme.MyNoteTheme

/**
 * MainActivity sengaja dibuat SANGAT TIPIS: tugasnya hanya
 * "menyalakan" Compose lalu menyerahkan semuanya ke NavGraph.
 * Semua logika ada di ViewModel, semua tampilan ada di Screen.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // UI modern: menggambar hingga di balik status bar
        setContent {
            MyNoteTheme {
                MyNoteNavGraph()
            }
        }
    }
}
