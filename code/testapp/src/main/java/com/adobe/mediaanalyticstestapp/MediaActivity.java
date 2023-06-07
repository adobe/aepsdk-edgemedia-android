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

package com.adobe.mediaanalyticstestapp;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import com.adobe.mediaanalyticstestapp.analytics.MediaAnalyticsProvider;
import com.adobe.mediaanalyticstestapp.player.PlayerEvent;
import com.adobe.mediaanalyticstestapp.player.VideoPlayer;
import java.util.Observable;
import java.util.Observer;

public class MediaActivity extends Activity implements Observer {
    private VideoPlayer _player;
    private MediaAnalyticsProvider _analyticsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitymedia);

        // Create the VideoPlayer instance.
        _player = new VideoPlayer(this);

        _player.addObserver(this);

        // Create the MediaAnalyticsProvider instance and
        // attach it to the VideoPlayer instance.
        _analyticsProvider = new MediaAnalyticsProvider(_player);

        // Load the main video content.
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video);
        _player.loadContent(uri);
    }

    @Override
    protected void onDestroy() {
        _analyticsProvider.destroy();
        _analyticsProvider = null;
        _player = null;

        super.onDestroy();
    }

    @Override
    public void update(Observable observable, Object o) {
        PlayerEvent playerEvent = (PlayerEvent) o;

        switch (playerEvent) {
            case AD_START:
                _onEnterAd();
                break;

            case AD_COMPLETE:
                _onExitAd();
                break;

            case SEEK_COMPLETE:
                if (_player.getAdInfo() == null) {
                    // The user seeked outside the ad.
                    _onExitAd();
                }

                break;
        }
    }

    private void _onEnterAd() {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.adOverlayView).setVisibility(View.VISIBLE);
                    }
                });
    }

    private void _onExitAd() {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.adOverlayView).setVisibility(View.INVISIBLE);
                    }
                });
    }
}
