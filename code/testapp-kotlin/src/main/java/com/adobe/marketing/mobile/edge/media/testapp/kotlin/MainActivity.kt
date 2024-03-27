/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edge.media.testapp.kotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.mobile.edge.media.Media

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Deep links handling
        val intent = intent
        val data = intent.data

        if (data != null) {
            Assurance.startSession(data.toString())
        }

        val versionTextView = findViewById<TextView>(R.id.version)
        versionTextView.text = Media.extensionVersion()

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
