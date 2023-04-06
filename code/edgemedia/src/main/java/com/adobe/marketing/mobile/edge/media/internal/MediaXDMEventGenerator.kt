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

package com.adobe.marketing.mobile.edge.media.internal

import com.adobe.marketing.mobile.edge.media.MediaConstants
import com.adobe.marketing.mobile.edge.media.internal.MediaInternalConstants.LOG_TAG
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMErrorDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaCollection
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEvent
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEventType
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaSchema
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMQoeDataDetails
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.util.DataReader
import com.adobe.marketing.mobile.util.StringUtils
import java.util.Date

internal class MediaXDMEventGenerator(
    private val mediaContext: MediaContext,
    private val mediaEventProcessor: MediaEventProcessor,
    private val trackerConfig: Map<String, Any>,
    private var refTS: Long
) {
    private val SOURCE_TAG = "MediaExperienceEventGenerator"
    private var lastReportedQoe: XDMQoeDataDetails? = null
    private var isTracking: Boolean = false
    private var currentPlaybackState: MediaPlaybackState? = null
    private var currentPlaybackStateStartRefTS: Long = refTS
    private val allowedAdPingIntervalRangeInSeconds = 1..10
    private val allowedMainPingIntervalRangeInSeconds = 10..50
    private var sessionId: String = ""

    init {
        startTrackingSession()
    }

    fun processSessionStart(forceResume: Boolean = false) {
        val sessionDetails = MediaXDMEventHelper.generateSessionDetails(mediaContext.mediaInfo, mediaContext.mediaMetadata, forceResume)
        val customMetadata = MediaXDMEventHelper.generateMediaCustomMetadata(mediaContext.mediaMetadata)

        val channel = DataReader.optString(trackerConfig, MediaConstants.TrackerConfig.CHANNEL, null)
        if (!StringUtils.isNullOrEmpty(channel)) {
            sessionDetails.channel = channel
        }

        val mediaCollection = XDMMediaCollection()
        mediaCollection.sessionDetails = sessionDetails
        mediaCollection.customMetadata = customMetadata

        addGenericDataAndProcess(XDMMediaEventType.SESSION_START, mediaCollection)
    }

    fun processSessionComplete() {
        addGenericDataAndProcess(XDMMediaEventType.SESSION_COMPLETE, null)
        endTrackingSession()
    }

    fun processSessionEnd() {
        addGenericDataAndProcess(XDMMediaEventType.SESSION_END, null)
        endTrackingSession()
    }

    fun processAdBreakStart() {
        val mediaCollection = XDMMediaCollection()
        mediaCollection.advertisingPodDetails = MediaXDMEventHelper.generateAdvertisingPodDetails(mediaContext.adBreakInfo)

        addGenericDataAndProcess(XDMMediaEventType.AD_BREAK_START, mediaCollection)
    }

    fun processAdBreakComplete() {
        addGenericDataAndProcess(XDMMediaEventType.AD_BREAK_COMPLETE, null)
    }

    fun processAdBreakSkip() {
        addGenericDataAndProcess(XDMMediaEventType.AD_BREAK_COMPLETE, null)
    }

    fun processAdStart() {
        val mediaCollection = XDMMediaCollection()
        mediaCollection.advertisingDetails = MediaXDMEventHelper.generateAdvertisingDetails(mediaContext.adInfo, mediaContext.adMetadata)
        mediaCollection.customMetadata = MediaXDMEventHelper.generateAdCustomMetadata(mediaContext.adMetadata)

        addGenericDataAndProcess(XDMMediaEventType.AD_START, mediaCollection)
    }

    fun processAdComplete() {
        addGenericDataAndProcess(XDMMediaEventType.AD_COMPLETE, null)
    }

    fun processAdSkip() {
        addGenericDataAndProcess(XDMMediaEventType.AD_SKIP, null)
    }

    fun processChapterStart() {
        val mediaCollection = XDMMediaCollection()
        mediaCollection.chapterDetails = MediaXDMEventHelper.generateChapterDetails(mediaContext.chapterInfo)
        mediaCollection.customMetadata = MediaXDMEventHelper.generateChapterMetadata(mediaContext.chapterMetadata)

        addGenericDataAndProcess(XDMMediaEventType.CHAPTER_START, mediaCollection)
    }

    fun processChapterComplete() {
        addGenericDataAndProcess(XDMMediaEventType.CHAPTER_COMPLETE, null)
    }

    fun processChapterSkip() {
        addGenericDataAndProcess(XDMMediaEventType.CHAPTER_SKIP, null)
    }

    // / End media session after 24 hr timeout or idle timeout(30 mins).
    fun processSessionAbort() {
        processSessionEnd()
    }

    // / Restart session again after 24 hr timeout or idle timeout recovered.
    fun processSessionRestart() {
        currentPlaybackState = MediaPlaybackState.Init
        currentPlaybackStateStartRefTS = refTS

        lastReportedQoe = null
        startTrackingSession()
        processSessionStart(forceResume = true)

        if (mediaContext.chapterInfo != null) {
            processChapterStart()
        }

        if (mediaContext.adBreakInfo != null) {
            processAdBreakStart()
        }

        if (mediaContext.adInfo != null) {
            processAdStart()
        }

        for (state in mediaContext.activeTrackedStates) {
            processStateStart(state)
        }

        processPlayback(doFlush = false)
    }

    fun processBitrateChange() {
        val mediaCollection = XDMMediaCollection()
        mediaCollection.qoeDataDetails = MediaXDMEventHelper.generateQoEDataDetails(mediaContext.qoEInfo)

        addGenericDataAndProcess(XDMMediaEventType.BITRATE_CHANGE, mediaCollection)
    }

    fun processError(errorId: String) {
        val mediaCollection = XDMMediaCollection()
        mediaCollection.errorDetails = XDMErrorDetails(errorId, MediaInternalConstants.ErrorSource.PLAYER)

        addGenericDataAndProcess(XDMMediaEventType.ERROR, mediaCollection)
    }

    fun processPlayback(doFlush: Boolean = false) {
        val reportingInterval = getReportingIntervalFromTrackerConfig(isAdStart = (mediaContext.adInfo != null))

        if (!isTracking) {
            return
        }

        val newPlaybackState = getPlaybackState()

        if (currentPlaybackState != newPlaybackState || doFlush) {
            val eventType = getMediaEventForPlaybackState(newPlaybackState)

            addGenericDataAndProcess(eventType, null)
            currentPlaybackState = newPlaybackState
            currentPlaybackStateStartRefTS = refTS
        } else if ((newPlaybackState == currentPlaybackState) && (refTS - currentPlaybackStateStartRefTS >= reportingInterval)) {
            // If the ts difference is more than interval we need to send it as multiple pings
            addGenericDataAndProcess(XDMMediaEventType.PING, null)
            currentPlaybackStateStartRefTS = refTS
        }
    }

    fun processStateStart(stateInfo: StateInfo) {
        val mediaCollection = XDMMediaCollection()
        mediaCollection.statesStart = MediaXDMEventHelper.generateStateDetails(listOf(stateInfo))

        addGenericDataAndProcess(XDMMediaEventType.STATES_UPDATE, mediaCollection)
    }

    fun processStateEnd(stateInfo: StateInfo) {
        val mediaCollection = XDMMediaCollection()
        mediaCollection.statesEnd = MediaXDMEventHelper.generateStateDetails(listOf(stateInfo))

        addGenericDataAndProcess(XDMMediaEventType.STATES_UPDATE, mediaCollection)
    }

    fun setRefTS(ts: Long) {
        this.refTS = ts
    }

    // / Signals event processor to start a new media session.
    private fun startTrackingSession() {
        sessionId = mediaEventProcessor.createSession()
        isTracking = true
        Log.debug(LOG_TAG, SOURCE_TAG, "Started a new session with id ($sessionId)")
    }

    private fun endTrackingSession() {
        if (isTracking) {
            Log.debug(LOG_TAG, SOURCE_TAG, "Ending the session with id ($sessionId).")
            mediaEventProcessor.endSession(sessionId)
            isTracking = false
        }
    }

    // / Prepares the XDM formatted data and creates a`MediaXDMEvent`, which is then sent to `MediaEventProcessor` for processing.
    // /  - Parameters:
    // /   - eventType: A `XDMMediaEventType` enum representing the XDM formatted name of the media event.
    // /   - mediaCollection: A  `XDMMediaCollection` object which is a XDM formatted object with some fields populated depending on the media event.
    private fun addGenericDataAndProcess(eventType: XDMMediaEventType, mediaCollection: XDMMediaCollection?) {
        if (!isTracking) {
            Log.debug(LOG_TAG, SOURCE_TAG, "Dropping hit as session ($sessionId) is no longer being actively tracked.")
            return
        }

        val mediaCollection = mediaCollection ?: XDMMediaCollection()

        // For bitrate change events and error events, use the qoe data in the current event being generated. For other events check MediaContext QoE object for latest QoE data updates.
        mediaCollection.qoeDataDetails = getQoEForCurrentEvent(mediaCollection.qoeDataDetails)
        // Add playhead details
        mediaCollection.playhead = mediaContext.playhead.toLong()

        val timestampAsDate = Date(refTS)
        val xdmEvent = XDMMediaEvent(XDMMediaSchema(eventType, timestampAsDate, mediaCollection))

        mediaEventProcessor.processEvent(sessionId, xdmEvent)
    }

    // / Gets the XDM formatted QoE data for the current event.
    // /  - Parameter qoe: A `XDMQoeDataDetails` object
    // /  - Returns:XDMFormatted QoE data if the current event has QoE Data or if the MediaContext has QoE data which is not yet reported to the backend. Otherwise it returns nil.
    private fun getQoEForCurrentEvent(qoe: XDMQoeDataDetails?): XDMQoeDataDetails? {
        // Cache and return the passed in QoE object if it is not nil
        if (qoe != null && qoe.isValid()) {
            lastReportedQoe = qoe
            return qoe
        }
        // If the passed QoE data object is nil, get the QoE data cached by the MediaContext class and convert to XDM formatted object.
        val mediaContextQoe = MediaXDMEventHelper.generateQoEDataDetails(mediaContext.qoEInfo)
        // If the QoE data cached by the MediaContext class is different than the last reported QoE data, return the MediaContext cached QoE data to be sent to the backend
        if (lastReportedQoe != mediaContextQoe) {
            lastReportedQoe = mediaContextQoe
            return mediaContextQoe
        }

        // Return null if the current event does not have any QoE data and the latest QoE data has been already reported
        return null
    }

    private fun getPlaybackState(): MediaPlaybackState {
        return if (mediaContext.isInState(MediaPlaybackState.Buffer)) {
            MediaPlaybackState.Buffer
        } else if (mediaContext.isInState(MediaPlaybackState.Seek)) {
            MediaPlaybackState.Pause
        } else if (mediaContext.isInState(MediaPlaybackState.Play)) {
            MediaPlaybackState.Play
        } else if (mediaContext.isInState(MediaPlaybackState.Pause)) {
            MediaPlaybackState.Pause
        } else {
            MediaPlaybackState.Init
        }
    }

    private fun getMediaEventForPlaybackState(state: MediaPlaybackState): XDMMediaEventType {
        return when (state) {
            MediaPlaybackState.Buffer -> XDMMediaEventType.BUFFER_START
            MediaPlaybackState.Seek -> XDMMediaEventType.PAUSE_START
            MediaPlaybackState.Pause -> XDMMediaEventType.PAUSE_START
            MediaPlaybackState.Play -> XDMMediaEventType.PLAY
            else -> XDMMediaEventType.PING
        }
    }

    // / Gets the custom reporting interval set in the tracker configuration. Valid custom main ping interval range is (10 seconds - 50 seconds) and valid ad ping interval is (1 second - 10 seconds)
    // / - Parameter isAdStart: A Boolean  when true denotes reporting interval is needed for Ad content or denotes Main content when false.
    // / - Return: the custom interval in `MILLISECONDS` if found in tracker configuration. Returns the default `MediaConstants.PingInterval.REALTIME_TRACKING` if the custom values are invalid or not found
    private fun getReportingIntervalFromTrackerConfig(isAdStart: Boolean = false): Int {
        if (isAdStart) {
            val customAdPingInterval = DataReader.optInt(trackerConfig, MediaConstants.TrackerConfig.AD_PING_INTERVAL, 0)
            if (allowedAdPingIntervalRangeInSeconds.contains(customAdPingInterval)) {
                return customAdPingInterval * 1000 // convert to Milliseconds
            }
        } else {
            val customMainPingInterval = DataReader.optInt(trackerConfig, MediaConstants.TrackerConfig.MAIN_PING_INTERVAL, 0)
            if (allowedMainPingIntervalRangeInSeconds.contains(customMainPingInterval)) {
                return customMainPingInterval * 1000 // convert to Milliseconds
            }
        }

        return MediaInternalConstants.PingInterval.REALTIME_TRACKING_MS
    }
}
