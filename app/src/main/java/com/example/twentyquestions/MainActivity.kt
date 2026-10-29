package com.example.twentyquestions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.twentyquestions.ui.MainGameScreen
import com.example.twentyquestions.ui.TwentyQuestionsViewModel
import com.example.twentyquestions.ui.theme.TwentyQuestionsTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TwentyQuestionsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TwentyQuestionsTheme {
                MainGameScreen(viewModel = viewModel)
            }
        }
    }
}
