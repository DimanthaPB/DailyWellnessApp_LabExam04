package com.example.dailywellnessapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.dailywellnessapp.R
import com.example.dailywellnessapp.ui.onboarding.OnboardingOneActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, OnboardingOneActivity::class.java))
            finish()
        }, 2000) // 2 seconds splash
    }
}