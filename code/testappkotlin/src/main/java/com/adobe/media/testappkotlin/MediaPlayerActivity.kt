package com.adobe.media.testappkotlin

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.adobe.media.testappkotlin.analytics.MediaAnalyticsProvider
import com.adobe.media.testappkotlin.player.PlayerEvent
import com.adobe.media.testappkotlin.player.VideoPlayer
import java.util.*

class MediaPlayerActivity: Activity(), Observer{
    var _player:VideoPlayer? = null
    var _analyticsProvider:MediaAnalyticsProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activitymedia)

        // Create the VideoPlayer instance.
        _player = VideoPlayer(this)
        _player?.addObserver(this)

        // Create the MediaAnalyticsProvider instance and
        // attach it to the VideoPlayer instance.
        _analyticsProvider = MediaAnalyticsProvider(_player)

        // Load the main video content.
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.video)
        _player?.loadContent(uri)
    }

    override fun update(o: Observable?, arg: Any?) {
        val playerEvent = arg as? PlayerEvent

        when (playerEvent) {
            PlayerEvent.AD_START -> _onEnterAd()
            PlayerEvent.AD_COMPLETE -> _onExitAd()
            PlayerEvent.SEEK_COMPLETE -> if (_player?.getAdInfo() == null) {
                // The user seek outside the ad.
                _onExitAd()
            }
            else -> return
        }
    }

    override fun onDestroy() {
        _analyticsProvider?.destroy()
        _analyticsProvider = null
        _player = null
        super.onDestroy()
    }
    private fun _onEnterAd() {
        runOnUiThread {
            findViewById<View>(R.id.adOverlayView).visibility = View.VISIBLE
        }
    }

    private fun _onExitAd() {
        runOnUiThread {
            findViewById<View>(R.id.adOverlayView).visibility = View.INVISIBLE
        }
    }
}