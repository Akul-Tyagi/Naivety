import androidx.compose.runtime.Composable
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun SetSystemBarsColor() {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = androidx.compose.ui.graphics.Color.Black,
        darkIcons = false
    )
}