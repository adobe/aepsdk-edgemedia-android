/*
  Copyright 2021 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.edge.media.testapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.adobe.marketing.mobile.Assurance;

public class MainActivity extends AppCompatActivity {
    Button startPlayerBtn;
    EditText assuranceURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Deep links handling
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            Assurance.startSession(data.toString());
        }

        startPlayerBtn = findViewById(R.id.startVideoPlayer);
        startPlayerBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startVideoPlayer();
                    }
                });
    }

    // Add button for Assurance
    public void setAssurance(View view) {
        // include Assurance url here
        assuranceURL = findViewById(R.id.assuranceUrl);
        String url = assuranceURL.getText().toString();
        Assurance.startSession(url);
    }

    public void startVideoPlayer() {
        Intent intent = new Intent(this, MediaActivity.class);
        startActivity(intent);
    }
}
