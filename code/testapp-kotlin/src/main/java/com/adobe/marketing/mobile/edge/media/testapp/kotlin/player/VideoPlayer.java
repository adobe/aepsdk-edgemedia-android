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

package com.adobe.marketing.mobile.edge.media.testapp.kotlin.player;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.widget.MediaController;

import com.adobe.marketing.mobile.edge.media.testapp.kotlin.Configuration;
import com.adobe.marketing.mobile.edge.media.testapp.kotlin.R;
import com.adobe.marketing.mobile.edge.media.MediaConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class VideoPlayer extends Observable {
    private static final String LOG_TAG = "VideoPlayer";

    // This sample VideoPlayer simulates a mid-roll ad at time 15:
    private static final Integer AD_START_POS = 15;
    private static final Integer AD_END_POS = 30;
    private static final Integer AD_LENGTH = 15;

    private static final Integer CHAPTER1_START_POS = 0;
    private static final Integer CHAPTER1_END_POS = 15;
    private static final Integer CHAPTER1_LENGTH = 15;

    private static final Integer CHAPTER2_START_POS = 30;
    private static final Integer CHAPTER2_LENGTH = 30;

    private static final Long MONITOR_TIMER_INTERVAL = 500L;

    private final MediaController _mediaController;
    private final ObservableVideoView _videoView;

    private Boolean _videoLoaded = false;
    private Boolean _seeking = false;
    private Boolean _buffering = false;
    private Boolean _paused = true;
    private Boolean _isMute = false;

    private AudioManager _audio;

    private Map<String, Object> _adBreakInfo, _adInfo, _chapterInfo, _qoeInfo;

    private Clock _clock;

    private final String _videoId;
    private final String _videoName;
    private final String _streamType;

    public VideoPlayer(Activity parentActivity) {

        _audio = (AudioManager) parentActivity.getSystemService(Context.AUDIO_SERVICE);

        _videoView = (ObservableVideoView) parentActivity.findViewById(R.id.videoView);

        _videoView.setVideoPlayer(this);

        _mediaController = new MediaController(parentActivity);
        _mediaController.setMediaPlayer(_videoView);

        _videoView.setMediaController(_mediaController);
        _videoView.requestFocus();

        _videoView.setOnPreparedListener(_onPreparedListener);
        _videoView.setOnInfoListener(_onInfoListener);
        _videoView.setOnCompletionListener(_onCompletionListener);

        _videoId = Configuration.VIDEO_ID;
        _videoName = Configuration.VIDEO_NAME;
        _streamType = MediaConstants.StreamType.VOD;

        _adBreakInfo = null;
        _adInfo = null;
        _chapterInfo = null;

        // Build a static/hard-coded QoE info here.
        _qoeInfo = new HashMap<String, Object>();
        _qoeInfo.put("bitrate", 50000);
        _qoeInfo.put("fps", 24);
        _qoeInfo.put("droppedFrames", 10);
        _qoeInfo.put("startupTime", 2);

        _clock = null;
    }

    public void destroy() {
        if (_clock != null) {
            _clock.quit();
            _clock = null;
        }
    }

    public Integer getCurrentPlaybackTime() {
        Integer playhead;
        Integer vTime = getPlayhead();

        if (vTime > AD_START_POS + AD_LENGTH) {
            playhead = vTime - AD_LENGTH;
        } else if (vTime > AD_START_POS) {
            playhead = AD_START_POS;
        } else {
            playhead = vTime;
        }

        return playhead;
    }

    public Map<String, Object> getAdBreakInfo() {
        return _adBreakInfo;
    }

    public Map<String, Object> getAdInfo() {
        return _adInfo;
    }

    public Map<String, Object> getChapterInfo() {
        return _chapterInfo;
    }

    public Map<String, Object> getQoEInfo() {
        return _qoeInfo;
    }

    public void loadContent(Uri uri) {
        if (_videoLoaded) {
            _unloadVideo();
        }

        _videoView.setVideoURI(uri);
    }

    void resumePlayback() {
        Log.d(LOG_TAG, "Resuming playback.");

        _openVideoIfNecessary();
        _paused = false;

        setChanged();
        notifyObservers(PlayerEvent.PLAY);
    }

    void pausePlayback() {
        Log.d(LOG_TAG, "Pausing playback.");

        _paused = true;

        setChanged();
        notifyObservers(PlayerEvent.PAUSE);
    }

    void seekStart() {
        Log.d(LOG_TAG, "Starting seek.");

        _openVideoIfNecessary();
        _seeking = true;

        setChanged();
        notifyObservers(PlayerEvent.SEEK_START);
    }

    private Integer getDuration() {
        return _videoView.getDuration() / 1000 - AD_LENGTH;
    }

    private Integer getPlayhead() {
        return _videoView.getCurrentPosition() / 1000;
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final MediaPlayer.OnInfoListener _onInfoListener =
            new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            Log.d(
                                    LOG_TAG,
                                    "#onInfo(what=MEDIA_INFO_BUFFERING_START, extra="
                                            + extra
                                            + ")");

                            _buffering = true;

                            setChanged();
                            notifyObservers(PlayerEvent.BUFFER_START);

                            break;

                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            Log.d(
                                    LOG_TAG,
                                    "#onInfo(what=MEDIA_INFO_BUFFERING_END, extra=" + extra + ")");

                            _buffering = false;

                            setChanged();
                            notifyObservers(PlayerEvent.BUFFER_COMPLETE);

                            break;

                        default:
                            Log.d(LOG_TAG, "#onInfo(what=" + what + ", extra=" + extra + ")");
                            break;
                    }

                    return true;
                }
            };

    @SuppressWarnings("FieldCanBeLocal")
    private final MediaPlayer.OnPreparedListener _onPreparedListener =
            new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Log.d(LOG_TAG, "#onPrepared()");

                    _mediaController.show(0);

                    mediaPlayer.setOnSeekCompleteListener(
                            new MediaPlayer.OnSeekCompleteListener() {
                                @Override
                                public void onSeekComplete(MediaPlayer mediaPlayer) {
                                    Log.d(LOG_TAG, "#onSeekComplete()");

                                    _seeking = false;

                                    _doPostSeekComputations();

                                    setChanged();
                                    notifyObservers(PlayerEvent.SEEK_COMPLETE);
                                }
                            });
                }
            };

    @SuppressWarnings("FieldCanBeLocal")
    private final MediaPlayer.OnCompletionListener _onCompletionListener =
            new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d(LOG_TAG, "#onCompletion()");

                    _mediaController.show(0);

                    _completeVideo();
                }
            };

    private void _openVideoIfNecessary() {
        if (!_videoLoaded) {
            _resetInternalState();

            _startVideo();

            _clock = new Clock();
        }
    }

    private void _completeVideo() {
        if (_videoLoaded) {
            // Complete the second chapter
            _completeChapter();

            setChanged();
            notifyObservers(PlayerEvent.COMPLETE);

            _unloadVideo();
        }
    }

    private void _unloadVideo() {
        setChanged();
        notifyObservers(PlayerEvent.VIDEO_UNLOAD);

        _clock.invalidate();

        _resetInternalState();
    }

    private void _resetInternalState() {
        _videoLoaded = false;
        _seeking = false;
        _buffering = false;
        _paused = true;

        if (_clock != null) {
            _clock.quit();
            _clock = null;
        }
    }

    private void _startVideo() {

        _videoLoaded = true;

        setChanged();
        notifyObservers(PlayerEvent.VIDEO_LOAD);
    }

    private void _startChapter1() {
        // Prepare the chapter info.
        _chapterInfo = new HashMap<String, Object>();
        _chapterInfo.put("length", CHAPTER1_LENGTH);
        _chapterInfo.put("startTime", CHAPTER1_START_POS);
        _chapterInfo.put("position", 1);
        _chapterInfo.put("name", "First chapter");

        setChanged();
        notifyObservers(PlayerEvent.CHAPTER_START);
    }

    private void _startChapter2() {
        // Prepare the chapter info.
        _chapterInfo = new HashMap<String, Object>();
        _chapterInfo.put("length", CHAPTER2_LENGTH);
        _chapterInfo.put("startTime", CHAPTER2_START_POS);
        _chapterInfo.put("position", 2);
        _chapterInfo.put("name", "Second chapter");

        setChanged();
        notifyObservers(PlayerEvent.CHAPTER_START);
    }

    private void _completeChapter() {
        // Reset the chapter info.
        _chapterInfo = null;

        setChanged();
        notifyObservers(PlayerEvent.CHAPTER_COMPLETE);
    }

    private void _startAd() {
        // Prepare the ad break info.
        _adBreakInfo = new HashMap<String, Object>();
        _adBreakInfo.put("name", "First Ad-Break");
        _adBreakInfo.put("position", 1);
        _adBreakInfo.put("startTime", AD_START_POS);

        // Prepare the ad info.
        _adInfo = new HashMap<String, Object>();
        _adInfo.put("id", "001");
        _adInfo.put("name", "Sample ad");
        _adInfo.put("length", AD_LENGTH);
        _adInfo.put("position", 1);

        // Start the ad.
        setChanged();
        notifyObservers(PlayerEvent.AD_START);
    }

    private void _completeAd() {
        // Complete the ad.
        setChanged();
        notifyObservers(PlayerEvent.AD_COMPLETE);

        // Clear the ad and ad-break info.
        _adInfo = null;
        _adBreakInfo = null;
    }

    private void _doPostSeekComputations() {
        Integer vTime = getPlayhead();

        // Seek inside the first chapter.
        if (vTime < CHAPTER1_END_POS) {
            // If we were not inside the first chapter before, trigger a chapter start
            if (_chapterInfo == null || (Integer) _chapterInfo.get("position") != 1) {
                _startChapter1();

                // If we were in the ad, clear the ad and ad-break info, but don't send the
                // AD_COMPLETE event.
                if (_adInfo != null) {
                    _adInfo = null;
                    _adBreakInfo = null;
                }
            }
        }

        // Seek inside the ad.
        else if (vTime >= AD_START_POS && vTime < AD_END_POS) {
            // If we were not inside the ad before, trigger an ad-start
            if (_adInfo == null) {
                _startAd();

                // Also, clear the chapter info, without sending the CHAPTER_COMPLETE event.
                _chapterInfo = null;
            }
        }

        // Seek inside the second chapter.
        else {
            // If we were not inside the 2nd chapter before, trigger a chapter start
            if (_chapterInfo == null || (Integer) _chapterInfo.get("position") != 2) {
                _startChapter2();

                // If we were in the ad, clear the ad and ad-break info, but don't send the
                // AD_COMPLETE event.
                if (_adInfo != null) {
                    _adInfo = null;
                    _adBreakInfo = null;
                }
            }
        }
    }

    private void _detectMute() {
        boolean mute = _audio.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;

        if (_isMute != mute) {
            _isMute = mute;
            PlayerEvent state =
                    mute ? PlayerEvent.PLAYER_STATE_MUTE_START : PlayerEvent.PLAYER_STATE_MUTE_END;
            setChanged();
            notifyObservers(state);
        }
    }

    private void _onTick() {
        _detectMute();

        if (_seeking || _buffering || _paused) {
            return;
        }

        Integer vTime = getPlayhead();

        // If we're inside the ad content:
        if (vTime >= AD_START_POS && vTime < AD_END_POS) {
            if (_chapterInfo != null) {
                // If we were inside a chapter, complete it.
                _completeChapter();
            }

            if (_adInfo == null) {
                // Start the ad (if not already started).
                _startAd();
            }
        }

        // Otherwise, we're outside the ad content:
        else {
            if (_adInfo != null) {
                // Complete the ad (if needed).
                _completeAd();
            }

            if (vTime < CHAPTER1_END_POS) {
                if (_chapterInfo != null && (Integer) _chapterInfo.get("position") != 1) {
                    // If we were inside another chapter, complete it.
                    _completeChapter();
                }

                if (_chapterInfo == null) {
                    // Start the first chapter.
                    _startChapter1();
                }
            } else {
                if (_chapterInfo != null && (Integer) _chapterInfo.get("position") != 2) {
                    // If we were inside another chapter, complete it.
                    _completeChapter();
                }

                if (_chapterInfo == null) {
                    // Start the second chapter.
                    _startChapter2();
                }
            }
        }

        setChanged();
        notifyObservers(PlayerEvent.PLAYHEAD_UPDATE);
    }

    private class Clock extends HandlerThread {
        private Handler _handler;
        private Boolean _shouldStop = false;

        Clock() {
            super("VideoPlayerClock");
            start();
            Looper looper = getLooper();

            if (looper == null) {
                Log.e(LOG_TAG, "Unable to obtain looper thread.");
                return;
            }

            _handler = new Handler(getLooper());
            final Handler handler = _handler;

            handler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (!_shouldStop) {
                                _onTick();
                                handler.postDelayed(this, MONITOR_TIMER_INTERVAL);
                            }
                        }
                    });
        }

        public void invalidate() {
            _shouldStop = true;
        }
    }
}
