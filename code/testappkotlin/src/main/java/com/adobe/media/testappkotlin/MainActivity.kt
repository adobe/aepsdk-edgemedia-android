package com.adobe.media.testappkotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import com.adobe.marketing.mobile.Assurance

class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startPlayerButton = findViewById<Button>(R.id.startVideoPlayer)
        startPlayerButton.setOnClickListener {
            val intent = Intent(this, MediaPlayerActivity::class.java)
            startActivity(intent)
        }
    }

    fun setAssurance(view: View?) {
        // include Assurance url here
        val assuranceURL = findViewById<EditText>(R.id.assuranceUrl)
        val url: String = assuranceURL.getText().toString()
        Assurance.startSession(url)
    }
}