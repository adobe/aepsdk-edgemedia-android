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
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEventType
import org.junit.Before
import org.junit.Test

class TrackerScenarioPlaybackTests : TrackerScenarioTestBase() {

    private val backendSessionId = "backendSessionId"

    private val mediaSharedState = mutableMapOf(
        "edgemedia.channel" to "test_channel",
        "edgemedia.playerName" to "test_playerName",
        "edgemedia.appVersion" to "test_appVersion"
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
        MediaConstants.StreamType.VOD, // MediaConstants.StreamType.AOD,
        MediaType.Video, // MediaType.Audio,
        30
    )

    private val mediaMetadata = mapOf(
        "media.show" to "sampleshow",
        "key1" to "value1",
        "key2" to "мểŧẳđαţả"
    )

    private val chapterInfo = ChapterInfo.create("chapterName", 1, 1.1, 30.5)
    private val chapterMetadata = mapOf("media.artist" to "sampleArtist", "key1" to "value1", "key2" to "мểŧẳđαţả")

    private val chapterInfo2 = ChapterInfo.create("chapterName2", 2, 2.2, 40.5)
    private val chapterMetadata2 = mapOf("media.artist" to "sampleArtist2", "key2" to "value2", "key3" to "мểŧẳđαţả")

    private val customStateInfo = StateInfo.create("customStateName")
    private val standardStateMute = StateInfo.create(MediaConstants.PlayerState.MUTE)
    private val standardStateFullScreen = StateInfo.create(MediaConstants.PlayerState.FULLSCREEN)
    private val standardStateCC = StateInfo.create(MediaConstants.PlayerState.CLOSED_CAPTION)

    @Before
    override fun setup() {
        super.setup()

        // Set Media State
        mediaEventProcessor.updateMediaState(mediaSharedState)

        // Create new MediaSession
        mediaTracker.trackSessionStart(mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata)

        // Set backend server ID to MediaSession
        mediaEventProcessor.notifyBackendSessionId(dispatchedEvents[0].uniqueIdentifier, backendSessionId)
    }

    @Test
    fun testTrackSimplePlayBack_usingRealTimeTracker_dispatchesAllEventsInOrderWithCorrectPlayheadAndTS() {
        mediaTracker.trackPlay()
        incrementTrackerTime(5, true) // content start play ping at 1 second
        mediaTracker.trackPause()
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackPlay()
        incrementTrackerTime(15, true) // will send ping since interval > 10 seconds
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 1, 1, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PAUSE_START, 5, 5, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 5, 15, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 5, 20, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 15, 30, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 20, 35, backendSessionId)
        )

        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testTrackSimplePlayBack_withSessionEnd_usingRealTimeTracker_dispatchesAllEventsInOrderWithCorrectPlayheadAndTS() {
        mediaTracker.trackPlay()
        incrementTrackerTime(5, true) // content start play ping at 1 second
        mediaTracker.trackPause()
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackPlay()
        incrementTrackerTime(15, true) // will send ping since interval > 10 seconds
        mediaTracker.trackSessionEnd()

        val expected: List<Event> = listOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 1, 1, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PAUSE_START, 5, 5, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 5, 15, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 5, 20, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 15, 30, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_END, 20, 35, backendSessionId)
        )

        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testTrackSimplePlayBack_withBuffer_usingRealTimeTracker_dispatchesAllEventsInOrderWithCorrectPlayheadAndTS() {
        mediaTracker.trackEvent(Media.Event.BufferStart, null, null)
        incrementTrackerTime(5, false)
        mediaTracker.trackPlay()
        incrementTrackerTime(5, true)
        mediaTracker.trackEvent(Media.Event.BufferStart, null, null)
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.BufferComplete, null, null)
        incrementTrackerTime(15, true) // will send ping since interval > 10 seconds
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.BUFFER_START, 0, 0, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 0, 5, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 1, 6, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.BUFFER_START, 5, 10, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 5, 20, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 5, 25, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 15, 35, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 20, 40, backendSessionId)
        )

        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testTrackSimplePlayBack_withSeek_usingRealTimeTracker_dispatchesAllEventsInOrderWithCorrectPlayheadAndTS() {
        mediaTracker.trackEvent(Media.Event.SeekStart, null, null)
        incrementTrackerTime(5, false)
        mediaTracker.trackPlay()
        incrementTrackerTime(5, true)
        mediaTracker.trackEvent(Media.Event.SeekStart, null, null)
        incrementTrackerTime(15, false) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.SeekComplete, null, null)
        mediaTracker.trackPlay()
        incrementTrackerTime(15, true) // will send ping since interval > 10 seconds
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PAUSE_START, 0, 0, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 0, 5, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 1, 6, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PAUSE_START, 5, 10, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 5, 20, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 5, 25, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 15, 35, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 20, 40, backendSessionId)
        )

        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testChapter_usingRealTimeTracker_shouldSendChapterEvents() {
        // test
        mediaTracker.trackSessionStart(mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata)
        mediaTracker.trackEvent(Media.Event.ChapterStart, chapterInfo.toObjectMap(), chapterMetadata)
        mediaTracker.trackPlay()
        incrementTrackerTime(15, true) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.ChapterComplete, null, null)
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfo.toObjectMap(), mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.CHAPTER_START, 0, 0, backendSessionId, chapterInfo.toObjectMap(), chapterMetadata),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 1, 1, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 11, 11, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.CHAPTER_COMPLETE, 15, 15, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 15, 15, backendSessionId)
        )

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testMultipleChapter_usingRealTimeTracker_shouldSendMultipleChapterEventsInProperOrder() {
        mediaTracker.trackEvent(Media.Event.ChapterStart, chapterInfo.toObjectMap(), chapterMetadata)
        mediaTracker.trackPlay()
        incrementTrackerTime(15, true) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.ChapterComplete, null, null)
        mediaTracker.trackEvent(Media.Event.ChapterStart, chapterInfo2.toObjectMap(), chapterMetadata2)
        mediaTracker.trackPlay()
        incrementTrackerTime(15, true) // will send ping since interval > 10 seconds
        mediaTracker.trackEvent(Media.Event.ChapterComplete, null, null)
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfo.toObjectMap(), mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.CHAPTER_START, 0, 0, backendSessionId, chapterInfo.toObjectMap(), chapterMetadata),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 1, 1, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 11, 11, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.CHAPTER_COMPLETE, 15, 15, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.CHAPTER_START, 15, 15, backendSessionId, chapterInfo2.toObjectMap(), chapterMetadata2),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 21, 21, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.CHAPTER_COMPLETE, 30, 30, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 30, 30, backendSessionId)
        )

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testCustomState_usingRealTimeTracker_dispatchesStateStartAndEndEvents() {
        mediaTracker.trackPlay()
        mediaTracker.trackEvent(Media.Event.StateStart, customStateInfo.toObjectMap(), null)
        incrementTrackerTime(5, true)
        mediaTracker.trackEvent(Media.Event.StateEnd, customStateInfo.toObjectMap(), null)
        incrementTrackerTime(5, true)
        mediaTracker.trackEvent(Media.Event.StateStart, standardStateMute.toObjectMap(), null)
        mediaTracker.trackEvent(Media.Event.StateStart, standardStateFullScreen.toObjectMap(), null)
        incrementTrackerTime(5, true)
        mediaTracker.trackEvent(Media.Event.StateEnd, standardStateMute.toObjectMap(), null)
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfo.toObjectMap(), mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.STATES_UPDATE, 0, 0, backendSessionId, customStateInfo.toObjectMap(), null, null, true),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 1, 1, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.STATES_UPDATE, 5, 5, backendSessionId, customStateInfo.toObjectMap(), null, null, false),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.STATES_UPDATE, 10, 10, backendSessionId, standardStateMute.toObjectMap(), null, null, true),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.STATES_UPDATE, 10, 10, backendSessionId, standardStateFullScreen.toObjectMap(), null, null, true),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 11, 11, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.STATES_UPDATE, 15, 15, backendSessionId, standardStateMute.toObjectMap(), null, null, false),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 15, 15, backendSessionId)
        )

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testCustomState_withoutStateEnd_usingRealTimeTracker_dispatchesStateStartEvents() {
        mediaTracker.trackPlay()
        mediaTracker.trackEvent(Media.Event.StateStart, customStateInfo.toObjectMap(), null)
        incrementTrackerTime(5, true)
        incrementTrackerTime(5, true)
        mediaTracker.trackEvent(Media.Event.StateStart, standardStateMute.toObjectMap(), null)
        mediaTracker.trackEvent(Media.Event.StateStart, standardStateFullScreen.toObjectMap(), null)
        incrementTrackerTime(5, true)
        mediaTracker.trackComplete()

        val expected: List<Event> = listOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfo.toObjectMap(), mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.STATES_UPDATE, 0, 0, backendSessionId, customStateInfo.toObjectMap(), null, null, true),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 1, 1, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.STATES_UPDATE, 10, 10, backendSessionId, standardStateMute.toObjectMap(), null, null, true),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.STATES_UPDATE, 10, 10, backendSessionId, standardStateFullScreen.toObjectMap(), null, null, true),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 11, 11, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 15, 15, backendSessionId)
        )

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testCustomState_moreThanTenUniqueStates_usingRealTimeTracker_dispatchesFirstTenStates() {
        mediaTracker.trackPlay()
        for (i in 1..15) {
            val info = StateInfo.create("state_$i")
            mediaTracker.trackEvent(Media.Event.StateStart, info.toObjectMap(), null)
        }

        mediaTracker.trackComplete()

        val expectedStateStartEvents: MutableList<Event> = mutableListOf()
        // We will have states only till state_10
        for (i in 1..10) {
            val info = StateInfo.create("state_$i")
            expectedStateStartEvents.add(EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.STATES_UPDATE, 0, 0, backendSessionId, info.toObjectMap(), null, null, true))
        }

        val expectedEvents: MutableList<Event> = mutableListOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfo.toObjectMap(), mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId)
        )

        expectedEvents.addAll(expectedStateStartEvents)
        expectedEvents.add(EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 0, 0, backendSessionId))

        // verify
        assertEqualEvents(expectedEvents, dispatchedEvents)
    }

    @Test
    fun testCustomError_usingRealTimeTracker_dispatchesErrorEventWithSetErrorId() {
        mediaTracker.trackPlay()
        incrementTrackerTime(5, true)
        mediaTracker.trackError("1000.2000.3000")
        incrementTrackerTime(15, true)
        mediaTracker.trackError("custom.error.code")
        mediaTracker.trackError("") // ignored
        mediaTracker.trackComplete()

        val errorInfo1 = mapOf("error.id" to "1000.2000.3000", "error.source" to "player")
        val errorInfo2 = mapOf("error.id" to "custom.error.code", "error.source" to "player")

        val expected: List<Event> = listOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 1, 1, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.ERROR, 5, 5, backendSessionId, errorInfo1),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 11, 11, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.ERROR, 20, 20, backendSessionId, errorInfo2),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 20, 20, backendSessionId)
        )

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    // SDK automatically restarts the long running session >= 24 hours
    @Test
    fun testSessionActiveForMoreThan24Hours_usingRealTimeTracker_shouldEndAndResumeSessionAutomatically() {
        mediaTracker.trackPlay()
        // wait for 24 hours
        incrementTrackerTime(86400, true)

        // Set backend server ID to second MediaSession created after restart
        mediaEventProcessor.notifyBackendSessionId(dispatchedEvents[8643].uniqueIdentifier, backendSessionId)

        // wait for 20 seconds
        incrementTrackerTime(20, true)
        mediaTracker.trackComplete()

        val resumedMediaInfo = mediaInfoWithDefaultPreroll.toObjectMap()
        resumedMediaInfo["media.resumed"] = true

        val expected: MutableList<Event> = mutableListOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 1, 1, backendSessionId)
        )

        val pingList: MutableList<Event> = mutableListOf()
        for (i in 11..86400 step 10) {
            pingList.add(EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, i.toLong(), i.toLong(), backendSessionId))
        }

        expected.addAll(pingList)
        expected.add(EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_END, 86400, 86400, backendSessionId))
        // Session2

        val expected2: List<Event> = listOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 86400, 86400, backendSessionId, resumedMediaInfo, mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 86400, 86400, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 86401, 86401, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 86411, 86411, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 86420, 86420, backendSessionId)
        )
        expected.addAll(expected2)

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    @Test
    fun testIdleTimeOut_RealTimeTrackerShouldSendSessionEndAutomaticallyAfterIdleTimeout() {
        mediaTracker.trackSessionStart(mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata)
        mediaTracker.trackPlay()
        incrementTrackerTime(3, true)
        mediaTracker.trackPause()
        // wait for 30 mins
        incrementTrackerTime(1800, false)

        val expected: MutableList<Event> = mutableListOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 1, 1, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PAUSE_START, 3, 3, backendSessionId)
        )
        val pingList: MutableList<Event> = mutableListOf()
        for (i in 3 until 1793 step 10) {
            pingList.add(EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 3, (i + 10).toLong(), backendSessionId))
        }

        expected.addAll(pingList)
        expected.add(EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_END, 3, 1803, backendSessionId))

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }

    // trackPlay after sessionEnd because of idleTimeout will resume the session.
    // trackSessionStart with resume flag set to true is sent by the SDK automatically on receiving play on idle session
    @Test
    fun testPlay_afterIdleTimeOut_usingRealTimeTracker_shouldAutomaticallyStartNewSessionWithResumeFlagSet() {
        mediaTracker.trackSessionStart(mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata)
        mediaTracker.trackPlay()
        incrementTrackerTime(3, true)
        mediaTracker.trackPause()
        // wait for 30 mins
        incrementTrackerTime(600, false)
        mediaTracker.trackEvent(Media.Event.StateStart, standardStateCC.toObjectMap(), null)
        incrementTrackerTime(600, false)
        mediaTracker.trackEvent(Media.Event.StateEnd, standardStateCC.toObjectMap(), null)
        incrementTrackerTime(600, false)
        mediaTracker.trackPlay()

        // Set backend server ID to second MediaSession after restart
        mediaEventProcessor.notifyBackendSessionId(dispatchedEvents[186].uniqueIdentifier, backendSessionId)

        incrementTrackerTime(3, true)
        mediaTracker.trackComplete()

        val expected: MutableList<Event> = mutableListOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfoWithDefaultPreroll.toObjectMap(), mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 0, 0, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 1, 1, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PAUSE_START, 3, 3, backendSessionId)
        )

        val pingList1: MutableList<Event> = mutableListOf()
        for (i in 3 until 603 step 10) {
            pingList1.add(EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 3, (i + 10).toLong(), backendSessionId))
        }

        expected.addAll(pingList1)
        expected.add(EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.STATES_UPDATE, 3, 603, backendSessionId, standardStateCC.toObjectMap(), null, null, true))

        val pingList2: MutableList<Event> = mutableListOf()
        for (i in 603 until 1203 step 10) {
            pingList2.add(EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 3, (i + 10).toLong(), backendSessionId))
        }
        expected.addAll(pingList2)
        expected.add(EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.STATES_UPDATE, 3, 1203, backendSessionId, standardStateCC.toObjectMap(), null, null, false))

        val pingList3: MutableList<Event> = mutableListOf()
        for (i in 1203 until 1793 step 10) {
            pingList3.add(EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PING, 3, (i + 10).toLong(), backendSessionId))
        }
        expected.addAll(pingList3)
        expected.add(EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_END, 3, 1803, backendSessionId))

        val resumedMediaInfo = mediaInfoWithDefaultPreroll.toObjectMap()
        resumedMediaInfo["media.resumed"] = true

        val expected2 = listOf(
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 3, 1803, backendSessionId, resumedMediaInfo, mediaMetadata, mediaState),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 3, 1803, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.PLAY, 4, 1804, backendSessionId),
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_COMPLETE, 6, 1806, backendSessionId)
        )
        expected.addAll(expected2)

        // verify
        assertEqualEvents(expected, dispatchedEvents)
    }
}
