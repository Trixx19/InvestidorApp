package com.example.investidorapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel // Importe o viewModel para usar no setContent
import com.example.investidorapp.ui.view.InvestidorScreen // Importe a sua tela Compose
import com.example.investidorapp.viewmodel.InvestimentosViewModel // Importe o seu ViewModel
import com.example.investidorapp.ui.theme.InvestidorappTheme // Importe o tema do seu aplicativo (se estiver usando)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES .TIRAMISU) {
            ActivityCompat .requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }
        setContent {
            val viewModel: InvestimentosViewModel = viewModel()
            InvestidorScreen (viewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    InvestidorappTheme {
        Greeting("Android")
    }
}