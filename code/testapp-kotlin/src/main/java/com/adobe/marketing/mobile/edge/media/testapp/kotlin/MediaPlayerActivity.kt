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

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.adobe.marketing.mobile.edge.media.testapp.kotlin.player.PlayerEvent
import com.adobe.marketing.mobile.edge.media.testapp.kotlin.player.VideoPlayer
import com.adobe.marketing.mobile.edge.media.testapp.kotlin.tracker.MediaPlayerObserver
import java.util.Observable
import java.util.Observer

class MediaPlayerActivity : Activity(), Observer {
    var player: VideoPlayer? = null
    var mediaPlayerObserver: MediaPlayerObserver? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activitymedia)

        // Create the VideoPlayer instance.
        val player = VideoPlayer(this)
        player.addObserver(this)

        // Create the MediaPlayerObserver instance and
        // attach it to the VideoPlayer instance.
        mediaPlayerObserver = MediaPlayerObserver(player)

        // Load the main video content.
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.video)
        player.loadContent(uri)
    }

    override fun update(o: Observable?, arg: Any?) {
        when (arg as? PlayerEvent) {
            PlayerEvent.AD_START -> onEnterAd()
            PlayerEvent.AD_COMPLETE -> onExitAd()
            PlayerEvent.SEEK_COMPLETE -> if (player?.adInfo == null) {
                // The user seek outside the ad.
                onExitAd()
            }
            else -> return
        }
    }

    override fun onDestroy() {
        mediaPlayerObserver?.destroy()
        super.onDestroy()
    }
    private fun onEnterAd() {
        runOnUiThread {
            findViewById<View>(R.id.adOverlayView).visibility = View.VISIBLE
        }
    }

    private fun onExitAd() {
        runOnUiThread {
            findViewById<View>(R.id.adOverlayView).visibility = View.INVISIBLE
        }
    }
}
