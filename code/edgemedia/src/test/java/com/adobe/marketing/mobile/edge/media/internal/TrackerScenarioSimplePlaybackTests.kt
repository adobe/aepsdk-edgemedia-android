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
import com.adobe.marketing.mobile.edge.media.MediaConstants.StreamType
import com.adobe.marketing.mobile.edge.media.internal.MediaPublicTracker.TimestampSupplier
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEventType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TrackerScenarioSimplePlaybackTests {
    private var dispatchedEvents: MutableList<Event> = mutableListOf()
    private val dispatcher: (Event) -> Unit = { event -> dispatchedEvents.add(event) }
    private lateinit var mediaState: MediaState
    private lateinit var mediaEventProcessor: MediaEventProcessor
    private lateinit var mediaEventTracker: MediaEventTracker
    private lateinit var mediaTracker: MediaPublicTracker

    private val backendSessionId = "backendSessionId"

    private var currentTimestampMillis: Long = 0L
    private var currentPlayhead: Double = 0.0

    private val mediaSharedState = mutableMapOf(
        "edgemedia.channel" to "test_channel",
        "edgemedia.playerName" to "test_playerName",
        "edgemedia.appVersion" to "test_appVersion"
    )

    private val mediaInfo = MediaInfo.create(
        "mediaID",
        "mediaName",
        StreamType.AOD,
        MediaType.Audio,
        30.5
    )

    private val mediaMetadata = mutableMapOf(
        "media.show" to "sampleshow",
        "key1" to "value1"
    )

    @Before
    fun setup() {
        dispatchedEvents.clear()
        mediaState = MediaState()
        mediaEventProcessor = MediaEventProcessor(mediaState, dispatcher)
        mediaEventTracker = MediaEventTracker(mediaEventProcessor, null)
        mediaTracker = MediaPublicTracker("Simple Playback Tracker") { event ->
            mediaEventTracker.track(event)
        }

        currentTimestampMillis = 0L // Calendar.getInstance().timeInMillis
        mediaTracker.currentTimestamp = TimestampSupplier { currentTimestampMillis }

        // Set Media State
        mediaEventProcessor.updateMediaState(mediaSharedState)

        // Create new MediaSession
        mediaTracker.trackSessionStart(mediaInfo.toObjectMap(), mediaMetadata)

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
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfo.toObjectMap(), mediaMetadata, mediaState),
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
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfo.toObjectMap(), mediaMetadata, mediaState),
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
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfo.toObjectMap(), mediaMetadata, mediaState),
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
            EdgeEventHelper.generateEdgeEvent(XDMMediaEventType.SESSION_START, 0, 0, backendSessionId, mediaInfo.toObjectMap(), mediaMetadata, mediaState),
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

    private fun incrementTrackerTime(seconds: Int, updatePlayhead: Boolean) {
        for (i in 1..seconds) {
            currentTimestampMillis += 1000
            if (updatePlayhead) currentPlayhead += 1
            mediaTracker.updateCurrentPlayhead(currentPlayhead)
        }
    }

    private fun assertEqualEvents(expectedEvents: List<Event>, actualEvents: List<Event>) {
        assertEquals("Number of dispatched events must be equal.", expectedEvents.size, actualEvents.size)

        for (i in 1 until expectedEvents.size) {
            val expected = expectedEvents[i]
            val actual = actualEvents[i]

            assertEquals("Event name must match.", expected.name, actual.name)
            assertEquals("Event type must match.", expected.type, actual.type)
            assertEquals("Event source must match.", expected.source, actual.source)
            assertEquals("Event data must match.", expected.eventData, actual.eventData)
        }
    }
}
