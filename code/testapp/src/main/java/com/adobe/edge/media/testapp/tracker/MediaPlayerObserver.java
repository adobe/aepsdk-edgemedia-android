/*
  Copyright 2018 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.edge.media.testapp.tracker;

import android.util.Log;
import com.adobe.edge.media.testapp.Configuration;
import com.adobe.edge.media.testapp.player.PlayerEvent;
import com.adobe.edge.media.testapp.player.VideoPlayer;
import com.adobe.marketing.mobile.edge.media.Media;
import com.adobe.marketing.mobile.edge.media.MediaConstants;
import com.adobe.marketing.mobile.edge.media.MediaTracker;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class MediaPlayerObserver implements Observer {
    private static final String LOG_TAG = "MediaPlayerObserver";
    private VideoPlayer _player;
    private MediaTracker _tracker;

    public MediaPlayerObserver(VideoPlayer player) {
        if (player == null) {
            throw new IllegalArgumentException("Player reference cannot be null.");
        }

        _player = player;
        HashMap<String, Object> config = new HashMap<>();
        config.put(
                MediaConstants.TrackerConfig.CHANNEL,
                "android_v5_sample"); // Overwrites channel configured from remote configuration
        // config.put(MediaConstants.TrackerConfig.AD_PING_INTERVAL, 1);  // Overwrites ad content
        // ping interval to 1 second
        // config.put(MediaConstants.TrackerConfig.MAIN_PING_INTERVAL, 30); // Overwrites main
        // content ping interval to 30 seconds.
        _tracker = Media.createTracker(config);

        _player.addObserver(this);
    }

    public void destroy() {
        if (_player != null) {
            _tracker.trackSessionEnd();
            _tracker = null;

            _player.destroy();
            _player.deleteObserver(this);
            _player = null;
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        PlayerEvent playerEvent = (PlayerEvent) data;

        switch (playerEvent) {
            case VIDEO_LOAD:
                Log.d(LOG_TAG, "Video loaded.");
                HashMap<String, String> videoMetadata = new HashMap<String, String>();
                videoMetadata.put("isUserLoggedIn", "false");
                videoMetadata.put("tvStation", "Sample TV Station");
                videoMetadata.put("programmer", "Sample programmer");
                // Set Standard Video Metadata as context data
                videoMetadata.put(MediaConstants.VideoMetadataKeys.EPISODE, "Sample Episode");
                videoMetadata.put(MediaConstants.VideoMetadataKeys.SHOW, "Sample Show");

                HashMap<String, Object> mediaInfo =
                        Media.createMediaObject(
                                Configuration.VIDEO_NAME,
                                Configuration.VIDEO_ID,
                                Configuration.VIDEO_LENGTH,
                                MediaConstants.StreamType.VOD,
                                Media.MediaType.Video);

                // Set to true if this is a resume playback scenario (not starting from playhead 0)
                // mediaInfo.put(MediaConstants.MediaObjectKey.RESUMED, true);

                _tracker.trackSessionStart(mediaInfo, videoMetadata);
                break;

            case VIDEO_UNLOAD:
                Log.d(LOG_TAG, "Video unloaded.");
                _tracker.trackSessionEnd();
                break;

            case PLAY:
                Log.d(LOG_TAG, "Playback started.");
                _tracker.trackPlay();
                break;

            case PAUSE:
                Log.d(LOG_TAG, "Playback paused.");
                _tracker.trackPause();
                break;

            case SEEK_START:
                Log.d(LOG_TAG, "Seek started.");
                _tracker.trackEvent(Media.Event.SeekStart, null, null);
                break;

            case SEEK_COMPLETE:
                Log.d(LOG_TAG, "Seek completed.");
                _tracker.trackEvent(Media.Event.SeekComplete, null, null);
                break;

            case BUFFER_START:
                Log.d(LOG_TAG, "Buffer started.");
                _tracker.trackEvent(Media.Event.BufferStart, null, null);
                break;

            case BUFFER_COMPLETE:
                Log.d(LOG_TAG, "Buffer completed.");
                _tracker.trackEvent(Media.Event.BufferComplete, null, null);
                break;

            case AD_START:
                Log.d(LOG_TAG, "Ad started.");
                HashMap<String, String> adMetadata = new HashMap<String, String>();
                adMetadata.put("affiliate", "Sample affiliate");
                adMetadata.put("campaign", "Sample ad campaign");
                // Setting standard Ad Metadata
                adMetadata.put(MediaConstants.AdMetadataKeys.ADVERTISER, "Sample Advertiser");
                adMetadata.put(MediaConstants.AdMetadataKeys.CAMPAIGN_ID, "Sample Campaign");

                // Ad Break Info
                Map<String, Object> adBreakData = _player.getAdBreakInfo();
                String name = (String) adBreakData.get("name");
                Integer position = (Integer) adBreakData.get("position");
                Integer startTime = (Integer) adBreakData.get("startTime");

                HashMap<String, Object> adBreakInfo =
                        Media.createAdBreakObject(name, position, startTime);

                // Ad Info
                Map<String, Object> adData = _player.getAdInfo();
                String adName = (String) adData.get("name");
                String adId = (String) adData.get("id");
                Integer adPosition = (Integer) adData.get("position");
                Integer adLength = (Integer) adData.get("length");

                HashMap<String, Object> adInfo =
                        Media.createAdObject(adName, adId, adPosition, adLength);
                _tracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo, null);
                _tracker.trackEvent(Media.Event.AdStart, adInfo, adMetadata);
                break;

            case AD_COMPLETE:
                Log.d(LOG_TAG, "Ad completed.");
                _tracker.trackEvent(Media.Event.AdComplete, null, null);
                _tracker.trackEvent(Media.Event.AdBreakComplete, null, null);
                break;

            case CHAPTER_START:
                Log.d(LOG_TAG, "Chapter started.");
                HashMap<String, String> chapterMetadata = new HashMap<String, String>();
                chapterMetadata.put("segmentType", "Sample Segment Type");

                // Chapter Info
                Map<String, Object> chapterData = _player.getChapterInfo();
                String chapterName = (String) chapterData.get("name");
                Integer chapterPosition = (Integer) chapterData.get("position");
                Integer chapterLength = (Integer) chapterData.get("length");
                Integer chapterStartTime = (Integer) chapterData.get("startTime");

                HashMap<String, Object> chapterDataInfo =
                        Media.createChapterObject(
                                chapterName, chapterPosition, chapterLength, chapterStartTime);

                _tracker.trackEvent(Media.Event.ChapterStart, chapterDataInfo, chapterMetadata);
                break;

            case CHAPTER_COMPLETE:
                Log.d(LOG_TAG, "Chapter completed.");
                _tracker.trackEvent(Media.Event.ChapterComplete, null, null);
                break;

            case COMPLETE:
                Log.d(LOG_TAG, "Playback completed.");

                _tracker.trackComplete();
                break;

            case PLAYHEAD_UPDATE:
                // Log.d(LOG_TAG, "Playhead update.");
                _tracker.updateCurrentPlayhead(_player.getCurrentPlaybackTime());
                break;

            case PLAYER_STATE_MUTE_START:
                Log.d(LOG_TAG, "Player State(Mute).");
                HashMap<String, Object> stateInfo =
                        Media.createStateObject(MediaConstants.PlayerState.MUTE);
                _tracker.trackEvent(Media.Event.StateStart, stateInfo, null);
                break;

            case PLAYER_STATE_MUTE_END:
                Log.d(LOG_TAG, "Player State End.");
                stateInfo = Media.createStateObject(MediaConstants.PlayerState.MUTE);
                _tracker.trackEvent(Media.Event.StateEnd, stateInfo, null);
                break;

            default:
                Log.d(LOG_TAG, "Unhandled player event: " + playerEvent.toString());
                break;
        }
    }
}
