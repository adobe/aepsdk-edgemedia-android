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

package com.adobe.media.testappkotlin.analytics

import android.util.Log
import com.adobe.marketing.mobile.edge.media.Media
import com.adobe.marketing.mobile.edge.media.MediaConstants
import com.adobe.marketing.mobile.edge.media.MediaTracker
import com.adobe.media.testappkotlin.Configuration
import com.adobe.media.testappkotlin.player.PlayerEvent
import com.adobe.media.testappkotlin.player.VideoPlayer
import java.util.*

class MediaAnalyticsProvider(player: VideoPlayer) : Observer {
    private val LOG_TAG = "MediaAnalyticsProvider"
    private var player: VideoPlayer = player
    private var tracker: MediaTracker? = null

    init {
        val config = mutableMapOf<String, Any>(MediaConstants.TrackerConfig.CHANNEL to "android_kotlin_sample") // Overwrites channel configured from remote configuration
        // config[MediaConstants.TrackerConfig.AD_PING_INTERVAL] = 1  // Overwrites ad content ping interval to 1 second
        // config[MediaConstants.TrackerConfig.MAIN_PING_INTERVAL] = 30 // Overwrites main content ping interval to 30 seconds.

        tracker = Media.createTracker(config)
        player.addObserver(this)
    }

    fun destroy() {
        tracker?.trackSessionEnd()
        player.destroy()
        player.deleteObserver(this)
    }

    @Deprecated("Deprecated in Java")
    override fun update(observable: Observable?, data: Any) {
        when (data as PlayerEvent) {
            PlayerEvent.VIDEO_LOAD -> {
                Log.d(LOG_TAG, "Video loaded.")
                val videoMetadata = mutableMapOf(
                    "isUserLoggedIn" to "false",
                    "tvStation" to "Sample TV Station",
                    "programmer" to "Sample programmer"
                )

                // Set Standard Video Metadata as context data
                videoMetadata[MediaConstants.VideoMetadataKeys.EPISODE] = "Sample Episode"
                videoMetadata[MediaConstants.VideoMetadataKeys.SHOW] = "Sample Show"

                val mediaInfo = Media.createMediaObject(
                    Configuration.VIDEO_NAME,
                    Configuration.VIDEO_ID,
                    Configuration.VIDEO_LENGTH,
                    MediaConstants.StreamType.VOD,
                    Media.MediaType.Video
                )

                // Set to true if this is a resume playback scenario (not starting from playhead 0)
                // mediaInfo.put(MediaConstants.MediaObjectKey.RESUMED, true);
                tracker?.trackSessionStart(mediaInfo, videoMetadata)
            }
            PlayerEvent.VIDEO_UNLOAD -> {
                Log.d(LOG_TAG, "Video unloaded.")
                tracker?.trackSessionEnd()
            }
            PlayerEvent.PLAY -> {
                Log.d(LOG_TAG, "Playback started.")
                tracker?.trackPlay()
            }
            PlayerEvent.PAUSE -> {
                Log.d(LOG_TAG, "Playback paused.")
                tracker?.trackPause()
            }
            PlayerEvent.SEEK_START -> {
                Log.d(LOG_TAG, "Seek started.")
                tracker?.trackEvent(Media.Event.SeekStart, null, null)
            }
            PlayerEvent.SEEK_COMPLETE -> {
                Log.d(LOG_TAG, "Seek completed.")
                tracker?.trackEvent(Media.Event.SeekComplete, null, null)
            }
            PlayerEvent.BUFFER_START -> {
                Log.d(LOG_TAG, "Buffer started.")
                tracker?.trackEvent(Media.Event.BufferStart, null, null)
            }
            PlayerEvent.BUFFER_COMPLETE -> {
                Log.d(LOG_TAG, "Buffer completed.")
                tracker?.trackEvent(Media.Event.BufferComplete, null, null)
            }
            PlayerEvent.AD_START -> {
                Log.d(LOG_TAG, "Ad started.")
                val adMetadata = HashMap<String, String>()
                adMetadata["affiliate"] = "Sample affiliate"
                adMetadata["campaign"] = "Sample ad campaign"
                // Setting standard Ad Metadata
                adMetadata[MediaConstants.AdMetadataKeys.ADVERTISER] = "Sample Advertiser"
                adMetadata[MediaConstants.AdMetadataKeys.CAMPAIGN_ID] = "Sample Campaign"

                // Ad Break Info
                val adBreakData = player!!.adBreakInfo
                val name = adBreakData["name"] as String?
                val position = adBreakData["position"] as Int?
                val startTime = adBreakData["startTime"] as Int?
                val adBreakInfo = Media.createAdBreakObject(
                    name!!,
                    position!!,
                    startTime!!
                )

                // Ad Info
                val adData = player!!.adInfo
                val adName = adData["name"] as String?
                val adId = adData["id"] as String?
                val adPosition = adData["position"] as Int?
                val adLength = adData["length"] as Int?
                val adInfo = Media.createAdObject(
                    adName!!,
                    adId!!,
                    adPosition!!,
                    adLength!!
                )
                tracker?.trackEvent(Media.Event.AdBreakStart, adBreakInfo, null)
                tracker?.trackEvent(Media.Event.AdStart, adInfo, adMetadata)
            }
            PlayerEvent.AD_COMPLETE -> {
                Log.d(LOG_TAG, "Ad completed.")
                tracker?.trackEvent(Media.Event.AdComplete, null, null)
                tracker?.trackEvent(Media.Event.AdBreakComplete, null, null)
            }
            PlayerEvent.CHAPTER_START -> {
                Log.d(LOG_TAG, "Chapter started.")
                val chapterMetadata = HashMap<String, String>()
                chapterMetadata["segmentType"] = "Sample Segment Type"

                // Chapter Info
                val chapterData = player!!.chapterInfo
                val chapterName = chapterData["name"] as String?
                val chapterPosition = chapterData["position"] as Int?
                val chapterLength = chapterData["length"] as Int?
                val chapterStartTime = chapterData["startTime"] as Int?
                val chapterDataInfo = Media.createChapterObject(
                    chapterName!!,
                    chapterPosition!!,
                    chapterLength!!,
                    chapterStartTime!!
                )
                tracker?.trackEvent(Media.Event.ChapterStart, chapterDataInfo, chapterMetadata)
            }
            PlayerEvent.CHAPTER_COMPLETE -> {
                Log.d(LOG_TAG, "Chapter completed.")
                tracker?.trackEvent(Media.Event.ChapterComplete, null, null)
            }
            PlayerEvent.COMPLETE -> {
                Log.d(LOG_TAG, "Playback completed.")
                tracker?.trackComplete()
            }
            PlayerEvent.PLAYHEAD_UPDATE -> // Log.d(LOG_TAG, "Playhead update.");
                tracker?.updateCurrentPlayhead(player!!.currentPlaybackTime)
            PlayerEvent.PLAYER_STATE_MUTE_START -> {
                Log.d(LOG_TAG, "Player State(Mute).")
                val stateInfo = Media.createStateObject(MediaConstants.PlayerState.MUTE)
                tracker?.trackEvent(Media.Event.StateStart, stateInfo, null)
            }
            PlayerEvent.PLAYER_STATE_MUTE_END -> {
                Log.d(LOG_TAG, "Player State End.")
                val stateInfo = Media.createStateObject(MediaConstants.PlayerState.MUTE)
                tracker?.trackEvent(Media.Event.StateEnd, stateInfo, null)
            }
        }
    }
}
