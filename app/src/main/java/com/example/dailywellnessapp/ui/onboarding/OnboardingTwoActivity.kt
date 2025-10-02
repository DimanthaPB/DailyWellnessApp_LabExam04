package com.example.dailywellnessapp.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dailywellnessapp.MainActivity
import com.example.dailywellnessapp.R

class OnboardingTwoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_two)

        val nextButton = findViewById<Button>(R.id.next_button)
        nextButton.setOnClickListener {
            startActivity(Intent(this, OnboardingThreeActivity::class.java))
            finish()
        }

        val skipText = findViewById<TextView>(R.id.skip_text)
        skipText.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}