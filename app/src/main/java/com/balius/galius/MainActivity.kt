package com.balius.galius

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.balius.galius.core.navigation.GaliusNavHost
import com.balius.galius.ui.theme.GaliusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GaliusAppRoot()
        }
    }
}

@Composable
fun GaliusAppRoot(modifier: Modifier = Modifier) {
    GaliusTheme {
        GaliusNavHost(modifier = modifier.fillMaxSize())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun GaliusAppRootPreview() {
    GaliusAppRoot()
}
