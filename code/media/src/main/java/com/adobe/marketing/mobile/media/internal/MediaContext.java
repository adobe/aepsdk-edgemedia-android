/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.media.internal;

import com.adobe.marketing.mobile.services.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

enum MediaPlayBackState {
    Init,
    Play,
    Pause,
    Buffer,
    Seek,
    Stall;

    @Override
    public String toString() {
        switch (this) {
            case Init:
                return "Init";

            case Play:
                return "Play";

            case Pause:
                return "Pause";

            case Buffer:
                return "Buffer";

            case Seek:
                return "Seek";

            case Stall:
                return "Stall";

            default:
                return this.name();
        }
    }
}

class MediaContext {
    private static final String LOG_TAG = "MediaContext";
    private final MediaInfo mediaInfo;
    private AdInfo adInfo;
    private AdBreakInfo adBreakInfo;
    private ChapterInfo chapterInfo;
    private QoEInfo qoeInfo;
    private Map<String, String> mediaMetadata, adMetadata, chapterMetadata;
    private boolean buffering, seeking;
    private MediaPlayBackState playState;
    private double playhead;
    private final Map<String, Boolean> states;

    MediaContext(final MediaInfo mediaInfo, final Map<String, String> metadata) {
        mediaMetadata = new HashMap<>();
        adMetadata = new HashMap<>();
        chapterMetadata = new HashMap<>();
        states = new HashMap<>();

        if (mediaInfo != null) {
            this.mediaInfo =
                    MediaInfo.create(
                            mediaInfo.getId(),
                            mediaInfo.getName(),
                            mediaInfo.getStreamType(),
                            mediaInfo.getMediaType(),
                            mediaInfo.getLength(),
                            mediaInfo.isResumed(),
                            mediaInfo.getPrerollWaitTime(),
                            mediaInfo.isGranularAdTrackingEnabled());
        } else {
            this.mediaInfo = null;
        }

        if (metadata != null) {
            mediaMetadata = new HashMap<>(metadata);
        }

        playState = MediaPlayBackState.Init;
        playhead = 0;
    }

    boolean isInAdBreak() {
        return (adBreakInfo != null);
    }

    boolean isInAd() {
        return (adInfo != null);
    }

    boolean isInChapter() {
        return (chapterInfo != null);
    }

    void setAdInfo(final AdInfo adInfo, final Map<String, String> metadata) {
        if (adInfo != null) {
            this.adInfo =
                    AdInfo.create(
                            adInfo.getId(),
                            adInfo.getName(),
                            adInfo.getPosition(),
                            adInfo.getLength());
        } else {
            this.adInfo = null;
        }

        if (metadata != null) {
            adMetadata = new HashMap<>(metadata);
        }
    }

    void setAdBreakInfo(final AdBreakInfo adBreakInfo) {
        if (adBreakInfo != null) {
            this.adBreakInfo =
                    AdBreakInfo.create(
                            adBreakInfo.getName(),
                            adBreakInfo.getPosition(),
                            adBreakInfo.getStartTime());
        } else {
            this.adBreakInfo = null;
        }
    }

    void setChapterInfo(final ChapterInfo chapterInfo, final Map<String, String> metadata) {
        if (chapterInfo != null) {
            this.chapterInfo =
                    ChapterInfo.create(
                            chapterInfo.getName(),
                            chapterInfo.getPosition(),
                            chapterInfo.getStartTime(),
                            chapterInfo.getLength());
        } else {
            this.chapterInfo = null;
        }

        if (metadata != null) {
            chapterMetadata = metadata;
        }
    }

    void setQoEInfo(final QoEInfo qoeInfo) {
        if (qoeInfo != null) {
            this.qoeInfo =
                    QoEInfo.create(
                            qoeInfo.getBitrate(),
                            qoeInfo.getDroppedFrames(),
                            qoeInfo.getFPS(),
                            qoeInfo.getStartupTime());
        } else {
            this.qoeInfo = null;
        }
    }

    void setPlayhead(final double playhead) {
        this.playhead = playhead;
    }

    MediaInfo getMediaInfo() {
        return mediaInfo;
    }

    Map<String, String> getMediaMetadata() {
        return mediaMetadata;
    }

    AdInfo getAdInfo() {
        return adInfo;
    }

    Map<String, String> getAdMetadata() {
        return adMetadata;
    }

    AdBreakInfo getAdBreakInfo() {
        return adBreakInfo;
    }

    ChapterInfo getChapterInfo() {
        return chapterInfo;
    }

    Map<String, String> getChapterMetadata() {
        return chapterMetadata;
    }

    QoEInfo getQoEInfo() {
        return qoeInfo;
    }

    double getPlayhead() {
        return playhead;
    }

    void clearAdBreakInfo() {
        adBreakInfo = null;
    }

    void clearAdInfo() {
        adInfo = null;
        adMetadata.clear();
    }

    void clearChapterInfo() {
        chapterInfo = null;
        chapterMetadata.clear();
    }

    void enterState(final MediaPlayBackState state) {
        Log.trace(
                MediaInternalConstants.LOG_TAG,
                LOG_TAG,
                "enterState - " + state.toString());

        switch (state) {
            case Play:
            case Pause:
            case Stall:
                playState = state;
                break;

            case Buffer:
                buffering = true;
                break;

            case Seek:
                seeking = true;
                break;

            default:
                Log.trace(
                        MediaInternalConstants.LOG_TAG,
                        LOG_TAG,
                        "enterState - Invalid state passed to Enter State ",
                        state.toString());
                break;
        }
    }

    void exitState(final MediaPlayBackState state) {
        Log.trace(
                MediaInternalConstants.LOG_TAG,
                LOG_TAG,
                "exitState - " + state.toString());

        switch (state) {
            case Buffer:
                buffering = false;
                break;

            case Seek:
                seeking = false;
                break;

            default:
                Log.trace(
                        MediaInternalConstants.LOG_TAG,
                        LOG_TAG,
                        "exitState - Invalid state passed to Exit State",
                        state.toString());
                break;
        }
    }

    boolean isInState(final MediaPlayBackState state) {
        boolean retVal = false;

        switch (state) {
            case Init:
            case Play:
            case Pause:
            case Stall:
                retVal = (playState == state);
                break;

            case Buffer:
                retVal = buffering;
                break;

            case Seek:
                retVal = seeking;
                break;
        }

        // Log.debug(MediaInternalConstants.EXTENSION_LOG_TAG, LOG_TAG, "isInState " +
        // state.toString() + " - " + retVal);
        return retVal;
    }

    boolean isIdle() {
        return !isInState(MediaPlayBackState.Play)
                || isInState(MediaPlayBackState.Buffer)
                || isInState(MediaPlayBackState.Seek);
    }

    boolean startState(final StateInfo stateInfo) {
        if (!hasTrackedState(stateInfo) && hasReachedStateLimit()) {
            Log.debug(
                    MediaInternalConstants.LOG_TAG,
                    LOG_TAG,
                    "startState failed, already tracked max states (%d) during the current"
                            + " session.",
                    MediaInternalConstants.EventDataKeys.StateInfo.STATE_LIMIT);
            return false;
        }

        if (isInState(stateInfo)) {
            Log.debug(
                    MediaInternalConstants.LOG_TAG,
                    LOG_TAG,
                    "startState failed, state %s is already being tracked.",
                    stateInfo.getStateName());
            return false;
        }

        states.put(stateInfo.getStateName(), true);
        return true;
    }

    boolean endState(final StateInfo stateInfo) {
        if (!isInState(stateInfo)) {
            Log.debug(
                    MediaInternalConstants.LOG_TAG,
                    LOG_TAG,
                    "endState failed, state %s is not being tracked currently.",
                    stateInfo.getStateName());
            return false;
        }

        states.put(stateInfo.getStateName(), false);
        return true;
    }

    boolean isInState(final StateInfo stateInfo) {
        String stateName = stateInfo.getStateName();
        return states.containsKey(stateName) && states.get(stateName);
    }

    boolean hasTrackedState(final StateInfo stateInfo) {
        String stateName = stateInfo.getStateName();
        return states.containsKey(stateName);
    }

    ArrayList<StateInfo> getActiveTrackedStates() {
        ArrayList<StateInfo> activeStates = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : states.entrySet()) {
            if (entry.getValue()) {
                activeStates.add(StateInfo.create(entry.getKey()));
            }
        }

        return activeStates;
    }

    boolean hasReachedStateLimit() {
        return states.size() >= MediaInternalConstants.EventDataKeys.StateInfo.STATE_LIMIT;
    }

    void clearState() {
        states.clear();
    }
}
