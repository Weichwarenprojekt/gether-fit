package de.progresstinators.getherfit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val personalButton = findViewById<NavButton>(R.id.personal_space)
        personalButton.showHighlighting(true)
    }
}