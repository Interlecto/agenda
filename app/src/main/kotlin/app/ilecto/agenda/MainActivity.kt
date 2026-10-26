package app.ilecto.agenda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.ilecto.agenda.core.designsystem.theme.AgendaTheme
import app.ilecto.agenda.navigation.AgendaNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgendaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AgendaNavHost()
                }
            }
        }
    }
}
