package com.yinian.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yinian.app.ui.AppViewModel
import com.yinian.app.ui.YinianApp
import com.yinian.app.ui.theme.LocalDarkTheme
import com.yinian.app.ui.theme.YinianTheme

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        splash.setKeepOnScreenCondition { !appViewModel.ready.value }

        val appWindow = window
        setContent {
            val state by appViewModel.state.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val view = LocalView.current

            YinianTheme(
                appearance = state.appearance,
                systemDark = systemDark,
            ) {
                val darkTheme = LocalDarkTheme.current
                SideEffect {
                    WindowCompat.getInsetsController(appWindow, view).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }
                YinianApp(viewModel = appViewModel, state = state)
            }
        }
    }
}
