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

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.edge.media.Media
import com.adobe.marketing.mobile.edge.media.MediaConstants
import com.adobe.marketing.mobile.edge.media.internal.EdgeEventHelper.Companion.generateEdgeEvent
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEventType
import org.junit.Before
import org.junit.Test

class TrackerScenarioAdPlaybackTests : TrackerScenarioTestBase() {
    private val backendSessionId = "backendSessionId"

    private val mediaSharedState = mapOf(
        "edgeMedia.channel" to "test_channel",
        "edgeMedia.playerName" to "test_playerName",
        "edgeMedia.appVersion" to "test_appVersion"
    )

    private val mediaInfo = MediaInfo.create(
        "mediaID",
        "mediaName",
        MediaConstants.StreamType.AOD,
        MediaType.Audio,
        30,
        false,
        0,
        false
    )

    private val mediaInfoWithDefaultPreroll = MediaInfo.create(
        "mediaID",
        "mediaName",
        MediaConstants.StreamType.AOD,
        MediaType.Audio,
        30
    )

    private val mediaMetadata = mapOf(
        "media.show" to "sampleshow",
        "key1" to "value1",
        "key2" to "мểŧẳđαţả"
    )

    private val adBreakInfo = AdBreakInfo.create("adBreakName", 1, 1)
    private val adBreakInfo2 = AdBreakInfo.create("adBreakName2", 2, 2)

    private val adInfo = AdInfo.create("adID", "adName", 1, 15)
    private val adMetadata = mapOf(
        "media.ad.advertiser" to "sampleAdvertiser",
        "key1" to "value1",
        "key2" to "мểŧẳđαţả"
    )

    private val adInfo2 = AdInfo.create("adID2", "adName2", 2, 20)
    private val adMetadata2 = mapOf(
        "media.ad.advertiser" to "sampleAdvertiser2",
        "key2" to "value2",
        "key3" to "мểŧẳđαţả"
    )

    private val chapterInfo = ChapterInfo.create("chapterName", 1, 1, 30)
    private val chapterMetadata = mapOf(
        "media.artist" to "sampleArtist",
        "key1" to "value1",
        "key2" to "мểŧẳđαţả"
    )

    private val chapterInfo2 = ChapterInfo.create("chapterName2", 2, 2, 40)
    private val chapterMetadata2 = mapOf(
        "media.artist" to "sampleArtist2",
        "key2" to "value2",
        "key3" to "мểŧẳđαţả"
    )

    @Before
    override fun setup() {
        super.setup()
    }

    /**
     * Create a single [MediaSession] in the [MediaTrackerEventGenerator] and track the session start event.
     */
    private fun setupSessionAndStart(mediaMediaInfo: MediaInfo) {
        // Set Media State
        mediaEventProcessor.updateMediaState(mediaSharedState)

        // Create new MediaSession
        mediaTracker.trackSessionStart(mediaMediaInfo.toObjectMap(), mediaMetadata)

        // Set backend server ID to MediaSession
        mediaEventProcessor.notifyBackendSessionId(dispatchedEvents[0].uniqueIdentifier, backendSessionId)
    }

    @Test
    fun testMultipleAdChapter_usingRealTimeTracker_shouldDispatchAdBreakAdAndChapterEventsProperly() {
        setupSessionAndStart(mediaInfoWithDefaultPreroll)
        mediaTracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo.toObjectMap(), null)
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo.toObjectMap(), adMetadata) // will send play since adStart triggers trackPlay internally
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.AdComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakComplete, null, null)
        // should switch to play state
        mediaTracker.trackEvent(Media.Event.ChapterStart, chapterInfo.toObjectMap(), chapterMetadata)
        incrementTrackerTime(15, true) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.ChapterComplete, null, null)
        mediaTracker.trackEvent(Media.Event.ChapterStart, chapterInfo2.toObjectMap(), chapterMetadata2)
        incrementTrackerTime(15, true) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.ChapterComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo2.toObjectMap(), null)
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo2.toObjectMap(), adMetadata2)
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.AdComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakComplete, null, null)
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata, mediaState),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_START, 0, 0, backendSessionId, adBreakInfo.toObjectMap()),
            generateEdgeEvent(XDMMediaEventType.AD_START, 0, 0, backendSessionId, adInfo.toObjectMap(), adMetadata, mediaState),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 0, 10, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_COMPLETE, 0, 15, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_COMPLETE, 0, 15, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 15, backendSessionId),

            generateEdgeEvent(XDMMediaEventType.CHAPTER_START, 0, 15, backendSessionId, chapterInfo.toObjectMap(), chapterMetadata),
            generateEdgeEvent(XDMMediaEventType.PLAY, 1, 16, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 11, 26, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.CHAPTER_COMPLETE, 15, 30, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.CHAPTER_START, 15, 30, backendSessionId, chapterInfo2.toObjectMap(), chapterMetadata2),
            generateEdgeEvent(XDMMediaEventType.PING, 21, 36, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.CHAPTER_COMPLETE, 30, 45, backendSessionId),

            generateEdgeEvent(XDMMediaEventType.AD_BREAK_START, 30, 45, backendSessionId, adBreakInfo2.toObjectMap()),
            generateEdgeEvent(XDMMediaEventType.AD_START, 30, 45, backendSessionId, adInfo2.toObjectMap(), adMetadata2, mediaState),
            generateEdgeEvent(XDMMediaEventType.PLAY, 30, 45, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 30, 55, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_COMPLETE, 30, 60, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_COMPLETE, 30, 60, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 30, 60, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 30, 60, backendSessionId)
        )

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testPrerollAd_usingRealTimeTracker_shouldSendAdBreakAndAdEventsInProperOrder() {
        setupSessionAndStart(mediaInfoWithDefaultPreroll)
        mediaTracker.trackPlay()
        mediaTracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo.toObjectMap(), null)
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo.toObjectMap(), adMetadata) // will send play since adStart triggers trackPlay internally
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.AdComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakComplete, null, null)
        // should switch to play state
        incrementTrackerTime(15, true) // will send ping since interval > 10 seconds
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata, mediaState),

            generateEdgeEvent(XDMMediaEventType.AD_BREAK_START, 0, 0, backendSessionId, adBreakInfo.toObjectMap()),
            generateEdgeEvent(XDMMediaEventType.AD_START, 0, 0, backendSessionId, adInfo.toObjectMap(), adMetadata, mediaState),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 0, 10, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_COMPLETE, 0, 15, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_COMPLETE, 0, 15, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 15, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 1, 16, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 11, 26, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 15, 30, backendSessionId)
        )

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testMultipleAdBreakMultipleAds_usingRealTimeTracker_shouldSendMultipleAdBreakAndAdEventsInProperOrder() {
        setupSessionAndStart(mediaInfo)
        mediaTracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo.toObjectMap(), null)
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo.toObjectMap(), adMetadata) // will send play since adStart triggers trackPlay internally
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.AdComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo2.toObjectMap(), adMetadata2)
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.AdComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakComplete, null, null)
        // explicitly switch to play state
        mediaTracker.trackPlay()
        incrementTrackerTime(15, true) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo2.toObjectMap(), null)
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo.toObjectMap(), adMetadata)
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.AdComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo2.toObjectMap(), adMetadata2)
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.AdComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakComplete, null, null)
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfo.toObjectMap(), mediaMetadata, mediaState),

            generateEdgeEvent(XDMMediaEventType.AD_BREAK_START, 0, 0, backendSessionId, adBreakInfo.toObjectMap()),
            generateEdgeEvent(XDMMediaEventType.AD_START, 0, 0, backendSessionId, adInfo.toObjectMap(), adMetadata, mediaState),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 0, 10, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_COMPLETE, 0, 15, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_START, 0, 15, backendSessionId, adInfo2.toObjectMap(), adMetadata2, mediaState),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 15, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 0, 25, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_COMPLETE, 0, 30, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_COMPLETE, 0, 30, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 30, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 1, 31, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 11, 41, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_START, 15, 45, backendSessionId, adBreakInfo2.toObjectMap()),
            generateEdgeEvent(XDMMediaEventType.AD_START, 15, 45, backendSessionId, adInfo.toObjectMap(), adMetadata, mediaState),
            generateEdgeEvent(XDMMediaEventType.PLAY, 15, 45, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 15, 55, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_COMPLETE, 15, 60, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_START, 15, 60, backendSessionId, adInfo2.toObjectMap(), adMetadata2, mediaState),
            generateEdgeEvent(XDMMediaEventType.PLAY, 15, 60, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 15, 70, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_COMPLETE, 15, 75, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_COMPLETE, 15, 75, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 15, 75, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 15, 75, backendSessionId)
        )

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testDelayedAds_usingRealTimeTracker_willSendPingEventsBeforeDelayedAdStartEvents() {
        // Create new MediaSession
        mediaTracker.trackSessionStart(mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata)

        // Set Media State
        mediaEventProcessor.updateMediaState(mediaSharedState)

        // Track session start a second time
        mediaTracker.trackSessionStart(mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata)

        // Set backend server ID to MediaSession
        mediaEventProcessor.notifyBackendSessionId(dispatchedEvents[0].uniqueIdentifier, backendSessionId)

        mediaTracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo.toObjectMap(), null)
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo.toObjectMap(), adMetadata)
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.AdComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakComplete, null, null)
        // should switch to play state
        mediaTracker.trackEvent(Media.Event.ChapterStart, chapterInfo.toObjectMap(), chapterMetadata)
        incrementTrackerTime(15, true) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.ChapterComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo2.toObjectMap(), null)
        incrementTrackerTime(25, false) // will send 2 pings since interval > 20 seconds
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo2.toObjectMap(), adMetadata2)
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.AdComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakComplete, null, null)
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata, mediaState),

            generateEdgeEvent(XDMMediaEventType.AD_BREAK_START, 0, 0, backendSessionId, adBreakInfo.toObjectMap()),
            generateEdgeEvent(XDMMediaEventType.PING, 0, 10, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_START, 0, 15, backendSessionId, adInfo.toObjectMap(), adMetadata, mediaState),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 15, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 0, 25, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_COMPLETE, 0, 30, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_COMPLETE, 0, 30, backendSessionId),

            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 30, backendSessionId),

            generateEdgeEvent(XDMMediaEventType.CHAPTER_START, 0, 30, backendSessionId, chapterInfo.toObjectMap(), chapterMetadata),
            generateEdgeEvent(XDMMediaEventType.PLAY, 1, 31, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 11, 41, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.CHAPTER_COMPLETE, 15, 45, backendSessionId),

            generateEdgeEvent(XDMMediaEventType.AD_BREAK_START, 15, 45, backendSessionId, adBreakInfo2.toObjectMap()),
            generateEdgeEvent(XDMMediaEventType.PING, 15, 51, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 15, 61, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_START, 15, 70, backendSessionId, adInfo2.toObjectMap(), adMetadata2, mediaState),
            generateEdgeEvent(XDMMediaEventType.PLAY, 15, 70, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 15, 80, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_COMPLETE, 15, 85, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_COMPLETE, 15, 85, backendSessionId),

            generateEdgeEvent(XDMMediaEventType.PLAY, 15, 85, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 15, 85, backendSessionId)
        )

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testAdWithSeek_usingRealTimeTracker_shouldSendPauseStartEventForAdSection() {
        // Create new MediaSession
        mediaTracker.trackSessionStart(mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata)

        // Set Media State
        mediaEventProcessor.updateMediaState(mediaSharedState)

        // Track session start a second time
        mediaTracker.trackSessionStart(mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata)

        // Set backend server ID to MediaSession
        mediaEventProcessor.notifyBackendSessionId(dispatchedEvents[0].uniqueIdentifier, backendSessionId)

        mediaTracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo.toObjectMap(), null)
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo.toObjectMap(), adMetadata)
        incrementTrackerTime(5, false)
        // seek out of ad into main content chapter
        mediaTracker.trackEvent(Media.Event.SeekStart, null, null)
        incrementTrackerTimestamp(1)
        incrementTrackerPlayhead(5)
        mediaTracker.trackEvent(Media.Event.SeekComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdSkip, null, null) // seeking from ad to main section
        mediaTracker.trackEvent(Media.Event.AdBreakComplete, null, null)
        // should switch to play state
        mediaTracker.trackEvent(Media.Event.ChapterStart, chapterInfo.toObjectMap(), chapterMetadata)
        incrementTrackerTime(15, true)
        // seek out of chapter into Ad
        mediaTracker.trackEvent(Media.Event.SeekStart, null, null)
        incrementTrackerTimestamp(1)
        incrementTrackerPlayhead(5)
        mediaTracker.trackEvent(Media.Event.ChapterSkip, null, null) // Seeking from chapter to ad section
        mediaTracker.trackEvent(Media.Event.SeekComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo2.toObjectMap(), null)
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo2.toObjectMap(), adMetadata2)
        incrementTrackerTime(15, false)
        mediaTracker.trackSessionEnd()

        val expected: List<Event> = listOf(
            generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata, mediaState),

            generateEdgeEvent(XDMMediaEventType.AD_BREAK_START, 0, 0, backendSessionId, adBreakInfo.toObjectMap()),
            generateEdgeEvent(XDMMediaEventType.AD_START, 0, 0, backendSessionId, adInfo.toObjectMap(), adMetadata, mediaState),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PAUSE_START, 0, 5, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 5, 6, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_SKIP, 5, 6, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_COMPLETE, 5, 6, backendSessionId),

            generateEdgeEvent(XDMMediaEventType.PLAY, 5, 6, backendSessionId),

            generateEdgeEvent(XDMMediaEventType.CHAPTER_START, 5, 6, backendSessionId, chapterInfo.toObjectMap(), chapterMetadata),
            generateEdgeEvent(XDMMediaEventType.PLAY, 6, 7, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 16, 17, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PAUSE_START, 20, 21, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.CHAPTER_SKIP, 25, 22, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 25, 22, backendSessionId),

            generateEdgeEvent(XDMMediaEventType.AD_BREAK_START, 25, 22, backendSessionId, adBreakInfo2.toObjectMap()),
            generateEdgeEvent(XDMMediaEventType.AD_START, 25, 22, backendSessionId, adInfo2.toObjectMap(), adMetadata2, mediaState),
            generateEdgeEvent(XDMMediaEventType.PLAY, 25, 22, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 25, 32, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_SKIP, 25, 37, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_COMPLETE, 25, 37, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.SESSION_END, 25, 37, backendSessionId)
        )

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testAdWithBuffer_usingRealtimeTracker_shouldSendBufferEventsForAdSection() {
        // Create new MediaSession
        mediaTracker.trackSessionStart(mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata)

        // Set Media State
        mediaEventProcessor.updateMediaState(mediaSharedState)

        // Track session start a second time
        mediaTracker.trackSessionStart(mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata)

        // Set backend server ID to MediaSession
        mediaEventProcessor.notifyBackendSessionId(dispatchedEvents[0].uniqueIdentifier, backendSessionId)

        mediaTracker.trackEvent(Media.Event.BufferStart, null, null)
        incrementTrackerTime(5, false)
        mediaTracker.trackEvent(Media.Event.BufferComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo.toObjectMap(), null)
        mediaTracker.trackEvent(Media.Event.BufferStart, null, null)
        incrementTrackerTime(5, false)
        mediaTracker.trackEvent(Media.Event.BufferComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo.toObjectMap(), adMetadata)
        incrementTrackerTime(15, false)
        mediaTracker.trackEvent(Media.Event.BufferStart, null, null)
        incrementTrackerTime(5, false)
        mediaTracker.trackEvent(Media.Event.BufferComplete, null, null)
        incrementTrackerTime(5, false)
        mediaTracker.trackEvent(Media.Event.AdComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakComplete, null, null)
        mediaTracker.trackPlay()
        incrementTrackerTime(5, true)
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata, mediaState),

            generateEdgeEvent(XDMMediaEventType.BUFFER_START, 0, 0, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PAUSE_START, 0, 5, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_START, 0, 5, backendSessionId, adBreakInfo.toObjectMap()),
            generateEdgeEvent(XDMMediaEventType.AD_START, 0, 10, backendSessionId, adInfo.toObjectMap(), adMetadata, mediaState),
            generateEdgeEvent(XDMMediaEventType.PAUSE_START, 0, 10, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 0, 20, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.BUFFER_START, 0, 25, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PAUSE_START, 0, 30, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_COMPLETE, 0, 35, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_COMPLETE, 0, 35, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PAUSE_START, 0, 35, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 35, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 1, 36, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 5, 40, backendSessionId)
        )

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testTrackSimplePlayBackWithAd_usingRealTimeTracker_withValidCustomPingInterval_dispatchesPingAfterCustomInterval() {
        // Set Media State
        mediaEventProcessor.updateMediaState(mediaSharedState)

        // Create a new MediaTracker with the given config, overwrites `mediaTracker` variable
        createTracker(mapOf(MediaConstants.TrackerConfig.MAIN_PING_INTERVAL to 15, MediaConstants.TrackerConfig.AD_PING_INTERVAL to 1))

        // Create new MediaSession
        mediaTracker.trackSessionStart(mediaInfo.toObjectMap(), mediaMetadata)

        // Set backend server ID to MediaSession
        mediaEventProcessor.notifyBackendSessionId(dispatchedEvents[0].uniqueIdentifier, backendSessionId)

        mediaTracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo.toObjectMap(), null)
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo.toObjectMap(), adMetadata) // will send play since adStart triggers trackPlay internally
        incrementTrackerTime(5, false) // will send ping since interval > custom ad interval (1) seconds
        mediaTracker.trackEvent(Media.Event.AdComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakComplete, null, null)
        incrementTrackerTime(31, true) // will send ping since interval > custom main interval (15) seconds
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfo.toObjectMap(), mediaMetadata, mediaState),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_START, 0, 0, backendSessionId, adBreakInfo.toObjectMap()),
            generateEdgeEvent(XDMMediaEventType.AD_START, 0, 0, backendSessionId, adInfo.toObjectMap(), adMetadata, mediaState),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 0, 1, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 0, 2, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 0, 3, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 0, 4, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 0, 5, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_COMPLETE, 0, 5, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_COMPLETE, 0, 5, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 5, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 1, 6, backendSessionId),

            generateEdgeEvent(XDMMediaEventType.PING, 16, 21, backendSessionId),

            generateEdgeEvent(XDMMediaEventType.PING, 31, 36, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 31, 36, backendSessionId)
        )

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testTrackSimplePlayBackWithAd_usingRealTimeTracker_withInvalidValidCustomPingDuration_dispatchesPingAfterDefaultInterval() {
        // Set Media State
        mediaEventProcessor.updateMediaState(mediaSharedState)

        // Create a new MediaTracker with the given config, overwrites `mediaTracker` variable
        createTracker(mapOf(MediaConstants.TrackerConfig.MAIN_PING_INTERVAL to 1, MediaConstants.TrackerConfig.AD_PING_INTERVAL to 11))

        // Create new MediaSession
        mediaTracker.trackSessionStart(mediaInfo.toObjectMap(), mediaMetadata)

        // Set backend server ID to MediaSession
        mediaEventProcessor.notifyBackendSessionId(dispatchedEvents[0].uniqueIdentifier, backendSessionId)

        mediaTracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo.toObjectMap(), null)
        mediaTracker.trackEvent(Media.Event.AdStart, adInfo.toObjectMap(), adMetadata) // will send play since adStart triggers trackPlay internally
        incrementTrackerTime(5, false) // will not send ping since interval < default ad interval (10) seconds
        mediaTracker.trackEvent(Media.Event.AdComplete, null, null)
        mediaTracker.trackEvent(Media.Event.AdBreakComplete, null, null)
        incrementTrackerTime(31, true) // will send ping since interval > custom main interval (10) seconds
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfo.toObjectMap(), mediaMetadata, mediaState),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_START, 0, 0, backendSessionId, adBreakInfo.toObjectMap()),
            generateEdgeEvent(XDMMediaEventType.AD_START, 0, 0, backendSessionId, adInfo.toObjectMap(), adMetadata, mediaState),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_COMPLETE, 0, 5, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.AD_BREAK_COMPLETE, 0, 5, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 0, 5, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PLAY, 1, 6, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 11, 16, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 21, 26, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.PING, 31, 36, backendSessionId),
            generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 31, 36, backendSessionId)
        )

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }
}
