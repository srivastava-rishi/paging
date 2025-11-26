package com.rsstudio.paginationpractice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rsstudio.paginationpractice.presentation.ScreenA
import com.rsstudio.paginationpractice.presentation.ScreenAActions
import com.rsstudio.paginationpractice.ui.theme.PaginationPracticeTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaginationPracticeTheme {
                ScreenA(
                    onAction = {
                        doSomething(it)
                    }
                )
            }
        }
    }

    fun doSomething(screenAActions: ScreenAActions) {
        when (screenAActions) {
            ScreenAActions.OnBack -> {

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PaginationPracticeTheme {
        Greeting("Android")
    }
}