/*
  Copyright 2019 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edgemedia.internal;

import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MediaCollectionTracker implements MediaTrackerInterface {
    private static final String LOG_TAG = "MediaCollectionTracker";
    private static final String KEY_INFO = "key_info";
    private static final String KEY_METADATA = "key_metadata";
    private static final String KEY_EVENT_TS = "key_eventts";
    private static final String KEY_SESSIONID = "key_sessionid";
    private static final int INVALID_NUMERIC_VALUE = -1;
    private MediaCollectionHitGenerator mediaHitGenerator;
    private MediaContext mediaContext;
    private final MediaRuleEngine ruleEngine;
    private MediaHitProcessor hitProcessor;
    private Map<String, Object> trackerConfig;

    // Idle Detection
    private static final long IDLE_TIMEOUT = 1800000; // 30mins
    private static final long CONTENT_START_DURATION = 1000;
    private boolean isTrackerIdle, isMediaIdle;
    private long mediaIdleStartTS = 0;

    private static final int INVALID_TIMESTAMP = -1; // Indicates uninitialize value of timestamp.
    // Session Timeout
    private static final long SESSION_TIMEOUT_IN_MILLIS =
            TimeUnit.DAYS.toMillis(1); // Restart session after 24 hrs.
    private long sessionRefTs = INVALID_TIMESTAMP;

    // Preroll
    private boolean inPrerollInterval;
    private long prerollRefTS;
    private List<PrerollQueuedRule> prerollRulesQueue;
    private boolean contentStarted;
    private long contentStartRefTs;

    MediaCollectionTracker(final MediaHitProcessor hitProcessor, final Map<String, Object> config) {
        reset();

        this.hitProcessor = hitProcessor;
        trackerConfig = config;

        ruleEngine = new MediaRuleEngine();
        prerollRulesQueue = new ArrayList<>();

        setUpMediaRules();
    }

    void reset() {
        mediaHitGenerator = null;
        mediaContext = null;

        isTrackerIdle = false;
        isMediaIdle = false;

        inPrerollInterval = false;
        prerollRulesQueue = null;

        contentStarted = false;
        contentStartRefTs = INVALID_TIMESTAMP;

        sessionRefTs = INVALID_TIMESTAMP;
    }

    @Override
    public boolean track(final Event event) {

        if (event == null || event.getEventData() == null) {
            return false;
        }

        Map<String, Object> eventData = event.getEventData();

        String eventName =
                DataReader.optString(
                        eventData, MediaInternalConstants.EventDataKeys.Tracker.EVENT_NAME, null);
        if (eventName == null) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "track - Event name is missing in track event data");
            return false;
        }

        MediaRuleName rule = MediaRuleName.eventNameToRule(eventName);
        if (rule == MediaRuleName.Invalid) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "track - Invalid event name passed in track event data");
            return false;
        }

        Map<String, Object> context = new HashMap<>();

        Object eventTS =
                eventData.get(MediaInternalConstants.EventDataKeys.Tracker.EVENT_TIMESTAMP);
        if (eventTS != null) {
            context.put(KEY_EVENT_TS, eventTS);
        } else {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "track - Event timestamp is missing in track event data");
            return false;
        }

        String sessionId =
                DataReader.optString(
                        eventData, MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID, null);
        if (sessionId != null) {
            context.put(KEY_SESSIONID, sessionId);
        }

        Object params = eventData.get(MediaInternalConstants.EventDataKeys.Tracker.EVENT_PARAM);
        if (params != null) {
            context.put(KEY_INFO, params);
        }

        Map<String, String> metadata =
                DataReader.optStringMap(
                        eventData,
                        MediaInternalConstants.EventDataKeys.Tracker.EVENT_METADATA,
                        null);

        if (metadata != null) {
            Map<String, String> cleanedMetadata = cleanMetadata(metadata);
            context.put(KEY_METADATA, cleanedMetadata);
        }

        if (rule != MediaRuleName.PlayheadUpdate) {
            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "track - Processing event - %s",
                    eventName);
        }

        if (prerollDeferRule(rule.ordinal(), context)) {
            return true;
        }

        return processRule(rule.ordinal(), context);
    }

    boolean processRule(final int rule, final Map<String, Object> context) {
        MediaRuleResponse response = this.ruleEngine.processRule(rule, context);

        if (!response.isValid) {
            Log.warning(MediaInternalConstants.EXTENSION_LOG_TAG, LOG_TAG, response.message);
        }

        return response.isValid;
    }

    Map<String, String> getMetadata(final Map<String, Object> context) {
        return DataReader.optStringMap(context, KEY_METADATA, null);
    }

    String getSessionId(final Map<String, Object> context) {
        return DataReader.optString(context, KEY_SESSIONID, null);
    }

    double getPlayhead(final Map<String, Object> context) {
        Map<String, Object> info = DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
        if (info == null) {
            return INVALID_NUMERIC_VALUE;
        }

        return DataReader.optDouble(
                info, MediaInternalConstants.EventDataKeys.Tracker.PLAYHEAD, INVALID_NUMERIC_VALUE);
    }

    boolean isInPrerollInterval() {
        return inPrerollInterval;
    }

    boolean isTrackerIdle() {
        return isTrackerIdle;
    }

    long getRefTS(final Map<String, Object> context) {
        return DataReader.optLong(context, KEY_EVENT_TS, INVALID_NUMERIC_VALUE);
    }

    String getError(final Map<String, Object> context) {
        Map<String, Object> info = DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
        if (info == null) {
            return null;
        }

        return DataReader.optString(info, MediaInternalConstants.EventDataKeys.ErrorInfo.ID, null);
    }

    // Predicates
    IMediaRuleCallback isInMedia = (rule, context) -> mediaContext != null;

    IMediaRuleCallback isInAdBreak = (rule, context) -> mediaContext.isInAdBreak();

    IMediaRuleCallback isInAd = (rule, context) -> mediaContext.isInAd();

    IMediaRuleCallback isInChapter = (rule, context) -> mediaContext.isInChapter();

    IMediaRuleCallback isInBuffering =
            (rule, context) -> mediaContext.isInState(MediaPlayBackState.Buffer);

    IMediaRuleCallback isInSeeking =
            (rule, context) -> mediaContext.isInState(MediaPlayBackState.Seek);

    IMediaRuleCallback isValidMediaInfo =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                return info != null && MediaInfo.fromObjectMap(info) != null;
            };

    IMediaRuleCallback isValidAdBreakInfo =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                return info != null && AdBreakInfo.fromObjectMap(info) != null;
            };

    IMediaRuleCallback isValidAdInfo =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                return info != null && AdInfo.fromObjectMap(info) != null;
            };

    IMediaRuleCallback isValidChapterInfo =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                return info != null && ChapterInfo.fromObjectMap(info) != null;
            };

    IMediaRuleCallback isValidQoEInfo =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                return info != null && QoEInfo.fromObjectMap(info) != null;
            };

    IMediaRuleCallback isValidStateInfo =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                return info != null && StateInfo.fromObjectMap(info) != null;
            };

    IMediaRuleCallback isValidErrorInfo =
            (rule, context) -> {
                String errorId = getError(context);
                return errorId != null && !errorId.isEmpty();
            };

    IMediaRuleCallback isDifferentAdBreakInfo =
            (rule, context) -> {
                if (!mediaContext.isInAdBreak()) {
                    return true;
                }

                AdBreakInfo currentAdBreakInfo = mediaContext.getAdBreakInfo();

                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                AdBreakInfo newAdBreakInfo = AdBreakInfo.fromObjectMap(info);

                return !(currentAdBreakInfo.equals(newAdBreakInfo));
            };

    IMediaRuleCallback isDifferentAdInfo =
            (rule, context) -> {
                if (!mediaContext.isInAd()) {
                    return true;
                }

                AdInfo currentAdInfo = mediaContext.getAdInfo();
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                AdInfo newAdInfo = AdInfo.fromObjectMap(info);

                return !(currentAdInfo.equals(newAdInfo));
            };

    IMediaRuleCallback isDifferentChapterInfo =
            (rule, context) -> {
                if (!mediaContext.isInChapter()) {
                    return true;
                }

                ChapterInfo currentChapterInfo = mediaContext.getChapterInfo();
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                ChapterInfo newChapterInfo = ChapterInfo.fromObjectMap(info);

                return !(currentChapterInfo.equals(newChapterInfo));
            };

    IMediaRuleCallback allowPlaybackStateChange =
            (rule, context) -> {
                // Allow player state change only if we are in main content or if we are inside an
                // ad.
                return !mediaContext.isInAdBreak() || mediaContext.isInAd();
            };

    IMediaRuleCallback isInTrackedState =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                StateInfo stateInfo = StateInfo.fromObjectMap(info);
                return mediaContext.isInState(stateInfo);
            };

    IMediaRuleCallback allowStateTrack =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                StateInfo stateInfo = StateInfo.fromObjectMap(info);
                return mediaContext.hasTrackedState(stateInfo)
                        || !mediaContext.hasReachedStateLimit();
            };

    // Actions
    IMediaRuleCallback cmdIdleDetection =
            (rule, context) -> {
                if (mediaContext.isIdle()) {

                    long refTS = getRefTS(context);

                    // Media was already idle during previous call.
                    if (isMediaIdle
                            && (!isTrackerIdle && (refTS - mediaIdleStartTS) >= IDLE_TIMEOUT)) {
                        // We stop tracking if media has been idle for 30 mins.
                        mediaHitGenerator.processSessionAbort();
                        isTrackerIdle = true;
                    } else if (!isMediaIdle) {
                        // Set the media in Idle state and store the TS
                        isMediaIdle = true;
                        mediaIdleStartTS = refTS;
                    }
                } else {
                    // Media is not currently idle
                    if (isTrackerIdle) {
                        // We resume tracking if we have stopped tracking.
                        mediaHitGenerator.processSessionRestart();
                        isTrackerIdle = false;

                        sessionRefTs = getRefTS(context);
                        // if media is idle, reset content started flag
                        contentStarted = false;
                        contentStartRefTs = INVALID_TIMESTAMP;
                    }

                    isMediaIdle = false;
                }

                return true;
            };

    IMediaRuleCallback cmdContentStartDetection =
            (rule, context) -> {
                if (mediaContext.isIdle() || contentStarted) {
                    return true;
                }

                // We send content start ping after main content plays for one second.
                if (mediaContext.isInAdBreak()) {
                    contentStartRefTs = INVALID_TIMESTAMP;
                    return true;
                }

                if (contentStartRefTs == INVALID_TIMESTAMP) {
                    // update content_start_ref_ts_ when main content is playing
                    contentStartRefTs = getRefTS(context);
                }

                long refTS = getRefTS(context);

                if ((refTS - contentStartRefTs) >= CONTENT_START_DURATION) {
                    mediaHitGenerator.processPlayback(true);
                    contentStarted = true;
                }

                return true;
            };

    /**
     * Callback for determining If session has been running for {@link #SESSION_TIMEOUT_IN_MILLIS}.
     * If yes, restart the session.
     */
    IMediaRuleCallback cmdSessionTimeoutDetection =
            (rule, context) -> {
                final long refTs = getRefTS(context);

                if (!isTrackerIdle
                        && refTs - sessionRefTs
                                >= SESSION_TIMEOUT_IN_MILLIS) { // Session is playing for more than
                    // 24hrs. Restart session.
                    mediaHitGenerator.processSessionAbort();
                    mediaHitGenerator.processSessionRestart();
                    sessionRefTs = refTs;
                    contentStarted = false;
                    contentStartRefTs = INVALID_TIMESTAMP;
                }
                return true;
            };

    IMediaRuleCallback cmdEnterAction =
            (rule, context) -> {
                long refTS = getRefTS(context);

                if (mediaHitGenerator != null && getRefTS(context) != -1) {
                    mediaHitGenerator.setRefTS(refTS);
                }

                return true;
            };

    IMediaRuleCallback cmdExitAction =
            (rule, context) -> {
                if (mediaContext == null) {
                    return false;
                }

                // Additional hacks based on how the api processing backend works.

                // Force the state to play when we receive adstart before any play/pause.
                // Happens usually for preroll ad. We manually switch our state to play as the
                // backend
                // automatically swithces state to play after adstart.
                int ruleName = rule.getName();

                if (ruleName == MediaRuleName.AdStart.ordinal()) {
                    if (mediaContext.isInState(MediaPlayBackState.Init)
                            && !mediaContext.isInState(MediaPlayBackState.Buffer)
                            && !mediaContext.isInState(MediaPlayBackState.Seek)) {
                        mediaContext.enterState(MediaPlayBackState.Play);
                    }
                }

                // If we receive BufferComplete / SeekComplete before first play / pause,
                // we manually switch to pause as there is not way to go back to init state.
                if (ruleName == MediaRuleName.BufferComplete.ordinal()
                        || ruleName == MediaRuleName.SeekComplete.ordinal()) {
                    if (mediaContext.isInState(MediaPlayBackState.Init)) {
                        mediaContext.enterState(MediaPlayBackState.Pause);
                    }
                }

                cmdIdleDetection.call(rule, context);
                cmdSessionTimeoutDetection.call(rule, context);
                cmdContentStartDetection.call(rule, context);

                // Flush playback state after AdStart and AdBreakComplete
                boolean flushState =
                        (rule.getName() == MediaRuleName.AdStart.ordinal())
                                || (rule.getName() == MediaRuleName.AdBreakComplete.ordinal());
                mediaHitGenerator.processPlayback(flushState);

                return true;
            };

    IMediaRuleCallback cmdMediaStart =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                MediaInfo mediaInfo = MediaInfo.fromObjectMap(info);

                long refTS = getRefTS(context);
                Map<String, String> metadata = getMetadata(context);

                String refSessionId = getSessionId(context);

                mediaContext = new MediaContext(mediaInfo, metadata);

                mediaHitGenerator =
                        new MediaCollectionHitGenerator(
                                mediaContext, hitProcessor, trackerConfig, refTS, refSessionId);

                mediaHitGenerator.processMediaStart();
                sessionRefTs = refTS;

                inPrerollInterval = mediaInfo.getPrerollWaitTime() > 0;
                prerollRefTS = refTS;

                return true;
            };

    IMediaRuleCallback cmdMediaComplete =
            (rule, context) -> {
                mediaHitGenerator.processMediaComplete();

                mediaHitGenerator = null;
                mediaContext = null;

                return true;
            };

    IMediaRuleCallback cmdMediaSkip =
            (rule, context) -> {
                mediaHitGenerator.processMediaSkip();

                mediaHitGenerator = null;
                mediaContext = null;

                return true;
            };

    IMediaRuleCallback cmdAdBreakStart =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                AdBreakInfo adBreakInfo = AdBreakInfo.fromObjectMap(info);
                mediaContext.setAdBreakInfo(adBreakInfo);
                mediaHitGenerator.processAdBreakStart();

                return true;
            };

    IMediaRuleCallback cmdAdBreakComplete =
            (rule, context) -> {
                mediaHitGenerator.processAdBreakComplete();
                mediaContext.clearAdBreakInfo();

                return true;
            };

    IMediaRuleCallback cmdAdBreakSkip =
            (rule, context) -> {
                if (mediaContext.isInAdBreak()) {
                    mediaHitGenerator.processAdBreakSkip();
                    mediaContext.clearAdBreakInfo();
                }

                return true;
            };

    IMediaRuleCallback cmdAdStart =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                AdInfo adInfo = AdInfo.fromObjectMap(info);
                Map<String, String> metadata = getMetadata(context);
                mediaContext.setAdInfo(adInfo, metadata);
                mediaHitGenerator.processAdStart();

                return true;
            };

    IMediaRuleCallback cmdAdComplete =
            (rule, context) -> {
                mediaHitGenerator.processAdComplete();
                mediaContext.clearAdInfo();

                return true;
            };

    IMediaRuleCallback cmdAdSkip =
            (rule, context) -> {
                if (mediaContext.isInAd()) {
                    mediaHitGenerator.processAdSkip();
                    mediaContext.clearAdInfo();
                }

                return true;
            };

    IMediaRuleCallback cmdChapterStart =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                ChapterInfo chapterInfo = ChapterInfo.fromObjectMap(info);
                Map<String, String> metadata = getMetadata(context);
                mediaContext.setChapterInfo(chapterInfo, metadata);
                mediaHitGenerator.processChapterStart();

                return true;
            };

    IMediaRuleCallback cmdChapterComplete =
            (rule, context) -> {
                mediaHitGenerator.processChapterComplete();
                mediaContext.clearChapterInfo();

                return true;
            };

    IMediaRuleCallback cmdChapterSkip =
            (rule, context) -> {
                if (mediaContext.isInChapter()) {
                    mediaHitGenerator.processChapterSkip();
                    mediaContext.clearChapterInfo();
                }

                return true;
            };

    IMediaRuleCallback cmdPlay =
            (rule, context) -> {
                mediaContext.enterState(MediaPlayBackState.Play);
                return true;
            };

    IMediaRuleCallback cmdPause =
            (rule, context) -> {
                mediaContext.enterState(MediaPlayBackState.Pause);
                return true;
            };

    IMediaRuleCallback cmdBufferStart =
            (rule, context) -> {
                mediaContext.enterState(MediaPlayBackState.Buffer);
                return true;
            };

    IMediaRuleCallback cmdBufferComplete =
            (rule, context) -> {
                if (mediaContext.isInState(MediaPlayBackState.Buffer)) {
                    mediaContext.exitState(MediaPlayBackState.Buffer);
                }

                return true;
            };

    IMediaRuleCallback cmdSeekStart =
            (rule, context) -> {
                mediaContext.enterState(MediaPlayBackState.Seek);
                return true;
            };

    IMediaRuleCallback cmdSeekComplete =
            (rule, context) -> {
                if (mediaContext.isInState(MediaPlayBackState.Seek)) {
                    mediaContext.exitState(MediaPlayBackState.Seek);
                }

                return true;
            };

    IMediaRuleCallback cmdError =
            (rule, context) -> {
                String errorId = getError(context);

                if (errorId != null) {
                    mediaHitGenerator.processError(errorId);
                }

                return true;
            };

    IMediaRuleCallback cmdBitrateChange =
            (rule, context) -> {
                mediaHitGenerator.processBitrateChange();

                return true;
            };

    IMediaRuleCallback cmdQoEUpdate =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                QoEInfo qoeInfo = QoEInfo.fromObjectMap(info);
                mediaContext.setQoEInfo(qoeInfo);

                return true;
            };

    IMediaRuleCallback cmdStateStart =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                StateInfo stateInfo = StateInfo.fromObjectMap(info);
                mediaContext.startState(stateInfo);
                mediaHitGenerator.processStateStart(stateInfo);
                return true;
            };

    IMediaRuleCallback cmdStateEnd =
            (rule, context) -> {
                Map<String, Object> info =
                        DataReader.optTypedMap(Object.class, context, KEY_INFO, null);
                StateInfo stateInfo = StateInfo.fromObjectMap(info);
                mediaContext.endState(stateInfo);
                mediaHitGenerator.processStateEnd(stateInfo);
                return true;
            };

    IMediaRuleCallback cmdPlayheadUpdate =
            (rule, context) -> {
                double playhead = getPlayhead(context);

                if (playhead >= 0) {
                    mediaContext.setPlayhead(playhead);
                }

                return true;
            };

    void setUpMediaRules() {
        ruleEngine.onEnterRule(cmdEnterAction);
        ruleEngine.onExitRule(cmdExitAction);

        // MediaRule::trackSessionStart
        MediaRule mediaStart =
                new MediaRule(MediaRuleName.MediaStart.ordinal(), "API::trackSessionStart");
        mediaStart
                .addPredicate(isInMedia, false, ErrorMessage.ErrInMedia)
                .addPredicate(isValidMediaInfo, true, ErrorMessage.ErrInvalidMediaInfo)
                .addAction(cmdMediaStart);

        ruleEngine.addRule(mediaStart);

        // MediaRule::trackSessionComplete
        MediaRule mediaComplete =
                new MediaRule(MediaRuleName.MediaComplete.ordinal(), "API::trackSessionComplete");
        mediaComplete
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addAction(cmdAdSkip)
                .addAction(cmdAdBreakSkip)
                .addAction(cmdChapterSkip)
                .addAction(cmdMediaComplete);

        ruleEngine.addRule(mediaComplete);

        // MediaRule::trackSessionEnd
        MediaRule mediaSkip =
                new MediaRule(MediaRuleName.MediaSkip.ordinal(), "API::trackSessionEnd");
        mediaSkip
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addAction(cmdAdSkip)
                .addAction(cmdAdBreakSkip)
                .addAction(cmdChapterSkip)
                .addAction(cmdMediaSkip);

        ruleEngine.addRule(mediaSkip);

        // MediaRule::trackError
        MediaRule error = new MediaRule(MediaRuleName.Error.ordinal(), "API::trackError");
        error.addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(isValidErrorInfo, true, ErrorMessage.ErrInvalidErrorId)
                .addAction(cmdError);

        ruleEngine.addRule(error);

        // MediaRule::trackPlay
        MediaRule play = new MediaRule(MediaRuleName.Play.ordinal(), "API::trackPlay");
        play.addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(allowPlaybackStateChange, true, ErrorMessage.ErrInvalidPlaybackState)
                .addAction(cmdSeekComplete)
                .addAction(cmdBufferComplete)
                .addAction(cmdPlay);

        ruleEngine.addRule(play);

        // MediaRule::trackPause
        MediaRule pause = new MediaRule(MediaRuleName.Pause.ordinal(), "API::trackPause");
        pause.addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(allowPlaybackStateChange, true, ErrorMessage.ErrInvalidPlaybackState)
                .addPredicate(isInBuffering, false, ErrorMessage.ErrInBuffer)
                .addPredicate(isInSeeking, false, ErrorMessage.ErrInSeek)
                .addAction(cmdSeekComplete)
                .addAction(cmdBufferComplete)
                .addAction(cmdPause);

        ruleEngine.addRule(pause);

        // MediaRule::trackEvent(BufferStart)
        MediaRule bufferStart =
                new MediaRule(MediaRuleName.BufferStart.ordinal(), "API::trackEvent(BufferStart)");
        bufferStart
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(allowPlaybackStateChange, true, ErrorMessage.ErrInvalidPlaybackState)
                .addPredicate(isInBuffering, false, ErrorMessage.ErrInBuffer)
                .addPredicate(isInSeeking, false, ErrorMessage.ErrInSeek)
                .addAction(cmdBufferStart);

        ruleEngine.addRule(bufferStart);

        // MediaRule::trackEvent(BufferComplete)
        MediaRule bufferComplete =
                new MediaRule(
                        MediaRuleName.BufferComplete.ordinal(), "API::trackEvent(BufferComplete)");
        bufferComplete
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(allowPlaybackStateChange, true, ErrorMessage.ErrInvalidPlaybackState)
                .addPredicate(isInBuffering, true, ErrorMessage.ErrNotInBuffer)
                .addAction(cmdBufferComplete);

        ruleEngine.addRule(bufferComplete);

        // MediaRule::trackEvent(SeekStart)
        MediaRule seekStart =
                new MediaRule(MediaRuleName.SeekStart.ordinal(), "API::trackEvent(SeekStart)");
        seekStart
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(allowPlaybackStateChange, true, ErrorMessage.ErrInvalidPlaybackState)
                .addPredicate(isInSeeking, false, ErrorMessage.ErrInSeek)
                .addPredicate(isInBuffering, false, ErrorMessage.ErrInBuffer)
                .addAction(cmdSeekStart);

        ruleEngine.addRule(seekStart);

        // MediaRule::trackEvent(SeekComplete)
        MediaRule seekComplete =
                new MediaRule(
                        MediaRuleName.SeekComplete.ordinal(), "API::trackEvent(SeekComplete)");
        seekComplete
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(allowPlaybackStateChange, true, ErrorMessage.ErrInvalidPlaybackState)
                .addPredicate(isInSeeking, true, ErrorMessage.ErrNotInSeek)
                .addAction(cmdSeekComplete);

        ruleEngine.addRule(seekComplete);

        // MediaRule::trackEvent(AdBreakStart)
        MediaRule adBreakStart =
                new MediaRule(
                        MediaRuleName.AdBreakStart.ordinal(), "API::trackEvent(AdBreakStart)");
        adBreakStart
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(isValidAdBreakInfo, true, ErrorMessage.ErrInvalidAdBreakInfo)
                .addPredicate(isDifferentAdBreakInfo, true, ErrorMessage.ErrDuplicateAdBreakInfo)
                .addAction(cmdAdSkip)
                .addAction(cmdAdBreakSkip)
                .addAction(cmdAdBreakStart);

        ruleEngine.addRule(adBreakStart);

        // MediaRule::trackEvent(AdBreakComplete)
        MediaRule adBreakComplete =
                new MediaRule(
                        MediaRuleName.AdBreakComplete.ordinal(),
                        "API::trackEvent(AdBreakComplete)");
        adBreakComplete
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(isInAdBreak, true, ErrorMessage.ErrNotInAdBreak)
                .addAction(cmdAdSkip)
                .addAction(cmdAdBreakComplete);

        ruleEngine.addRule(adBreakComplete);

        // MediaRule::trackEvent(AdStart)
        MediaRule adStart =
                new MediaRule(MediaRuleName.AdStart.ordinal(), "API::trackEvent(AdStart)");
        adStart.addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(isInAdBreak, true, ErrorMessage.ErrNotInAdBreak)
                .addPredicate(isValidAdInfo, true, ErrorMessage.ErrInvalidAdInfo)
                .addPredicate(isDifferentAdInfo, true, ErrorMessage.ErrDuplicateAdInfo)
                .addAction(cmdAdSkip)
                .addAction(cmdAdStart);

        ruleEngine.addRule(adStart);

        // MediaRule::trackEvent(AdComplete)
        MediaRule adComplete =
                new MediaRule(MediaRuleName.AdComplete.ordinal(), "API::trackEvent(AdComplete)");
        adComplete
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(isInAdBreak, true, ErrorMessage.ErrNotInAdBreak)
                .addPredicate(isInAd, true, ErrorMessage.ErrNotInAd)
                .addAction(cmdAdComplete);

        ruleEngine.addRule(adComplete);

        // MediaRule::trackEvent(AdSkip)
        MediaRule adSkip = new MediaRule(MediaRuleName.AdSkip.ordinal(), "API::trackEvent(AdSkip)");
        adSkip.addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(isInAdBreak, true, ErrorMessage.ErrNotInAdBreak)
                .addPredicate(isInAd, true, ErrorMessage.ErrNotInAd)
                .addAction(cmdAdSkip);

        ruleEngine.addRule(adSkip);

        // MediaRule::trackEvent(ChapterStart)
        MediaRule chapterStart =
                new MediaRule(
                        MediaRuleName.ChapterStart.ordinal(), "API::trackEvent(ChapterStart)");
        chapterStart
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(isValidChapterInfo, true, ErrorMessage.ErrInvalidChapterInfo)
                .addPredicate(isDifferentChapterInfo, true, ErrorMessage.ErrDuplicateChapterInfo)
                .addAction(cmdChapterSkip)
                .addAction(cmdChapterStart);

        ruleEngine.addRule(chapterStart);

        // MediaRule::trackEvent(ChapterComplete)
        MediaRule chapterComplete =
                new MediaRule(
                        MediaRuleName.ChapterComplete.ordinal(),
                        "API::trackEvent(ChapterComplete)");
        chapterComplete
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(isInChapter, true, ErrorMessage.ErrNotInChapter)
                .addAction(cmdChapterComplete);

        ruleEngine.addRule(chapterComplete);

        // MediaRule::trackEvent(ChapterSkip)
        MediaRule chapterSkip =
                new MediaRule(MediaRuleName.ChapterSkip.ordinal(), "API::trackEvent(ChapterSkip)");
        chapterSkip
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(isInChapter, true, ErrorMessage.ErrNotInChapter)
                .addAction(cmdChapterSkip);

        ruleEngine.addRule(chapterSkip);

        // MediaRule::trackEvent(BitrateChange)
        MediaRule bitrateChange =
                new MediaRule(
                        MediaRuleName.BitrateChange.ordinal(), "API::trackEvent(BitrateChange)");
        bitrateChange
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addAction(cmdBitrateChange);

        ruleEngine.addRule(bitrateChange);

        // MediaRule::updateQoEInfo
        MediaRule qoeUpdate =
                new MediaRule(MediaRuleName.QoEUpdate.ordinal(), "API::updateQoEInfo");
        qoeUpdate
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(isValidQoEInfo, true, ErrorMessage.ErrInvalidQoEInfo)
                .addAction(cmdQoEUpdate);

        ruleEngine.addRule(qoeUpdate);

        // MediaRule::updatePlayhead
        MediaRule playheadUpdate =
                new MediaRule(MediaRuleName.PlayheadUpdate.ordinal(), "API::updatePlayhead");
        playheadUpdate
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addAction(cmdPlayheadUpdate);

        ruleEngine.addRule(playheadUpdate);

        // MediaRule::stateStart
        MediaRule stateStart = new MediaRule(MediaRuleName.StateStart.ordinal(), "API::stateStart");
        stateStart
                .addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(isValidStateInfo, true, ErrorMessage.ErrInvalidStateInfo)
                .addPredicate(isInTrackedState, false, ErrorMessage.ErrInTrackedState)
                .addPredicate(allowStateTrack, true, ErrorMessage.ErrTrackedStatesLimitReached)
                .addAction(cmdStateStart);

        ruleEngine.addRule(stateStart);

        // MediaRule::stateEnd
        MediaRule stateEnd = new MediaRule(MediaRuleName.StateEnd.ordinal(), "API::stateEnd");
        stateEnd.addPredicate(isInMedia, true, ErrorMessage.ErrNotInMedia)
                .addPredicate(isValidStateInfo, true, ErrorMessage.ErrInvalidStateInfo)
                .addPredicate(isInTrackedState, true, ErrorMessage.ErrNotInTrackedState)
                .addAction(cmdStateEnd);

        ruleEngine.addRule(stateEnd);
    }

    Map<String, String> cleanMetadata(final Map<String, String> metadata) {
        Map<String, String> cleanedMetadata = new HashMap<>();

        if (metadata.isEmpty()) {
            return cleanedMetadata;
        }

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value == null || key == null) {
                // drop the metadata with null values
                Log.debug(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "cleanMetadata - Dropping metadata entry key:%s, since the key/value is"
                                + " null.");
                continue;
            }

            Pattern metadataPattern = Pattern.compile("^[a-zA-Z0-9_.]+$");
            Matcher metadataMatcher = metadataPattern.matcher(key);

            if (!metadataMatcher.find()) {
                Log.debug(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "cleanMetadata - Dropping metadata entry key:%s value:%s. Key should"
                                + " contain only alphabets, digits, '_' and '.'.",
                        key,
                        value);
            } else {

                cleanedMetadata.put(key, value);
            }
        }

        return cleanedMetadata;
    }

    boolean prerollDeferRule(final int rule, final Map<String, Object> context) {
        if (inPrerollInterval && mediaContext != null) {
            long prerollWaitTime = mediaContext.getMediaInfo().getPrerollWaitTime();

            // We are going to queue the events and stop further downstream
            // processing for prerollWaitTime ms.
            prerollRulesQueue.add(new PrerollQueuedRule(rule, context));

            long refTS = getRefTS(context);

            if (((refTS - prerollRefTS) >= prerollWaitTime)
                    || (rule == MediaRuleName.AdBreakStart.ordinal())
                    || (rule == MediaRuleName.MediaComplete.ordinal())
                    || (rule == MediaRuleName.MediaSkip.ordinal())) {

                // If prerollWaitTime has elapsed or we get any of these rules
                // We start processing all the queued rules.
                List<PrerollQueuedRule> reorderedRules = reorderPrerollRules(prerollRulesQueue);

                for (PrerollQueuedRule prerollQueuedRule : reorderedRules) {
                    processRule(prerollQueuedRule.ruleName, prerollQueuedRule.ruleContext);
                }

                prerollRulesQueue.clear();
                inPrerollInterval = false;
            }

            return true;
        }

        return false;
    }

    List<PrerollQueuedRule> reorderPrerollRules(final List<PrerollQueuedRule> rules) {
        List<PrerollQueuedRule> reorderedRules = new ArrayList<>();
        int adBreakStartPosition = -1;

        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i).ruleName == MediaRuleName.AdBreakStart.ordinal()) {
                adBreakStartPosition = i;
                break;
            }
        }

        // We drop any play rule before adbreak start.
        boolean dropPlay = adBreakStartPosition > -1;

        for (PrerollQueuedRule eventRule : rules) {
            if (eventRule.ruleName == MediaRuleName.Play.ordinal() && dropPlay) {
                continue;
            }

            if (eventRule.ruleName == MediaRuleName.AdBreakStart.ordinal()) {
                dropPlay = false;
            }

            reorderedRules.add(eventRule);
        }

        return reorderedRules;
    }

    @VisibleForTesting
    MediaHitProcessor getHitProcessor() {
        return hitProcessor;
    }
}

enum MediaRuleName {
    Invalid,
    MediaStart,
    MediaComplete,
    MediaSkip,
    AdBreakStart,
    AdBreakComplete,
    AdStart,
    AdComplete,
    AdSkip,
    ChapterStart,
    ChapterComplete,
    ChapterSkip,
    Play,
    Pause,
    SeekStart,
    SeekComplete,
    BufferStart,
    BufferComplete,
    BitrateChange,
    Error,
    QoEUpdate,
    PlayheadUpdate,
    StateStart,
    StateEnd;

    static final Map<String, MediaRuleName> eventToRuleMapping;

    static {
        eventToRuleMapping = new HashMap<>();
        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.SESSION_START, MediaStart);
        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.COMPLETE, MediaComplete);
        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.SESSION_END, MediaSkip);

        eventToRuleMapping.put(MediaInternalConstants.EventDataKeys.MediaEventName.PLAY, Play);
        eventToRuleMapping.put(MediaInternalConstants.EventDataKeys.MediaEventName.PAUSE, Pause);

        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.ADBREAK_START, AdBreakStart);
        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.ADBREAK_COMPLETE,
                AdBreakComplete);

        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.AD_START, AdStart);
        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.AD_COMPLETE, AdComplete);
        eventToRuleMapping.put(MediaInternalConstants.EventDataKeys.MediaEventName.AD_SKIP, AdSkip);

        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.CHAPTER_START, ChapterStart);
        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.CHAPTER_COMPLETE,
                ChapterComplete);
        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.CHAPTER_SKIP, ChapterSkip);

        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.SEEK_START, SeekStart);
        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.SEEK_COMPLETE, SeekComplete);

        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.BUFFER_START, BufferStart);
        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.BUFFER_COMPLETE,
                BufferComplete);

        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.BITRATE_CHANGE, BitrateChange);
        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.QOE_UPDATE, QoEUpdate);
        eventToRuleMapping.put(MediaInternalConstants.EventDataKeys.MediaEventName.ERROR, Error);
        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.PLAYHEAD_UPDATE,
                PlayheadUpdate);

        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.STATE_START, StateStart);
        eventToRuleMapping.put(
                MediaInternalConstants.EventDataKeys.MediaEventName.STATE_END, StateEnd);
    }

    static MediaRuleName eventNameToRule(final String eventName) {
        if (eventToRuleMapping.containsKey(eventName)) {
            return eventToRuleMapping.get(eventName);
        }

        return Invalid;
    }
}

class ErrorMessage {

    static final String ErrNotInMedia =
            "Media tracker is not in tracking session, call 'API:trackSessionStart' to begin a new"
                    + " tracking session.";
    static final String ErrInMedia =
            "Media tracker is in active tracking session, call 'API:trackSessionEnd' or"
                    + " 'API:trackComplete' to end current tracking session.";
    static final String ErrInBuffer =
            "Media tracker is tracking buffer events, call 'API:trackEvent(BufferComplete)' first"
                    + " to stop tracking buffer events.";
    static final String ErrNotInBuffer =
            "Media tracker is not tracking buffer events, call 'API:trackEvent(BufferStart)'"
                    + " before 'API:trackEvent(BufferComplete)'.";
    static final String ErrInSeek =
            "Media tracker is tracking seek events, call 'API:trackEvent(SeekComplete)' first to"
                    + " stop tracking seek events.";
    static final String ErrNotInSeek =
            "Media tracker is not tracking seek events, call 'API:trackEvent(SeekStart)' before"
                    + " 'API:trackEvent(SeekComplete)'.";
    static final String ErrNotInAdBreak =
            "Media tracker is not tracking any AdBreak, call 'API:trackEvent(AdBreakStart)' to"
                    + " begin tracking AdBreak.";
    static final String ErrNotInAd =
            "Media tracker is not tracking any Ad, call 'API:trackEvent(AdStart)' to begin"
                    + " tracking Ad.";
    static final String ErrNotInChapter =
            "Media tracker is not tracking any Chapter, call 'API:trackEvent(ChapterStart)' to"
                    + " begin tracking Chapter.";
    static final String ErrInvalidMediaInfo =
            "MediaInfo passed into 'API:trackSessionStart' is invalid.";
    static final String ErrInvalidAdBreakInfo =
            "AdBreakInfo passed into 'API:trackEvent(AdBreakStart)' is invalid.";
    static final String ErrDuplicateAdBreakInfo =
            "Media tracker is currently tracking the AdBreak passed into"
                    + " 'API:trackEvent(AdBreakStart)'.";
    static final String ErrInvalidAdInfo =
            "AdInfo passed into 'API:trackEvent(AdStart)' is invalid.";
    static final String ErrDuplicateAdInfo =
            "Media tracker is currently tracking the Ad passed into 'API:trackEvent(AdStart)'.";
    static final String ErrInvalidChapterInfo =
            "ChapterInfo passed into 'API:trackEvent(ChapterStart)' is invalid.";
    static final String ErrDuplicateChapterInfo =
            "Media tracker is currently tracking the Chapter passed into"
                    + " 'API:trackEvent(ChapterStart)'.";
    static final String ErrInvalidQoEInfo = "QoEInfo passed into 'API:updateQoEInfo' is invalid.";
    static final String ErrInvalidPlaybackState =
            "Media tracker is tracking an AdBreak but not tracking any Ad and will drop any calls"
                    + " to track playback state (Play, Pause, Buffer or Seek) in this state.";
    static final String ErrInvalidStateInfo =
            "StateInfo passed into 'API:trackEvent(StateStart)' or 'API:trackEvent(StateEnd)' is"
                    + " invalid.";
    static final String ErrInTrackedState =
            "Media tracker is already tracking the State passed into 'API:trackEvent(StateStart)'.";
    static final String ErrNotInTrackedState =
            "Media tracker is not tracking the State passed into 'API:trackEvent(StateEnd)'.";
    static final String ErrTrackedStatesLimitReached =
            "Media tracker is already tracking maximum allowed states (10) per session.";
    static final String ErrInvalidErrorId =
            "ErrorId passed into 'API:trackError' is invalid. Please pass valid non-empty non-null"
                    + " string for ErrorId.";
}

class PrerollQueuedRule {
    int ruleName;
    Map<String, Object> ruleContext;

    PrerollQueuedRule(final int ruleName, final Map<String, Object> ruleContext) {
        this.ruleName = ruleName;
        this.ruleContext = ruleContext;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof PrerollQueuedRule)) {
            return false;
        }

        PrerollQueuedRule other = (PrerollQueuedRule) o;

        return (ruleName == other.ruleName && ruleContext.equals(other.ruleContext));
    }
}
