package com.example.naivety

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.naivety.ui.theme.NaivetyTheme
import com.example.naivety.ui.theme.SplashSystemBars

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Add any additional initialization logic here
        startMainActivity()
    }

    private fun startMainActivity() {
        // Show splash screen for 2 seconds
        window.decorView.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1000)
    }
}