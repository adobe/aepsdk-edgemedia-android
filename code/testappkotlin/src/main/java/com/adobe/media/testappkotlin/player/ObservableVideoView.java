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

package com.adobe.media.testappkotlin.player;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

public class ObservableVideoView extends VideoView {
    private static final String LOG_TAG = "ObservableVideoView";

    private VideoPlayer _player;

    public ObservableVideoView(Context context) {
        super(context);
    }

    public ObservableVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setVideoPlayer(VideoPlayer player) {
        _player = player;
    }

    @Override
    public void start() {
        super.start();

        Log.d(LOG_TAG, "Resuming playback.");

        if (_player != null) {
            _player.resumePlayback();
        }
    }

    @Override
    public void pause() {
        super.pause();

        Log.d(LOG_TAG, "Pausing playback.");

        if (_player != null) {
            _player.pausePlayback();
        }
    }

    @Override
    public void seekTo(int msec) {
        super.seekTo(msec);

        Log.d(LOG_TAG, "Starting seek.");

        if (_player != null) {
            _player.seekStart();
        }
    }
}
