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
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMCustomMetadata
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaCollection
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEvent
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEventType
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaSchema
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMPlayerStateData
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.util.Date

class MediaXDMEventGeneratorTests {
    private lateinit var mediaInfo: MediaInfo
    private lateinit var metadata: Map<String, String>
    private lateinit var mediaContext: MediaContext
    private lateinit var mockEventProcessor: MediaEventProcessor
    private lateinit var eventGenerator: MediaXDMEventGenerator
    private var currSessionId = 0
    private var mockTimestamp = 0L
    private var mockPlayhead = 0

    @Captor
    private lateinit var eventCaptor: ArgumentCaptor<XDMMediaEvent>

    @Captor
    private lateinit var sessionIdCaptor: ArgumentCaptor<String>

    // Used for function call with non-optional parameters to wrap the capture parameter in a non-optional type.
    private fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

    @Before
    fun setup() {
        mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60)
        metadata = mapOf("k1" to "v1", MediaConstants.VideoMetadataKeys.SHOW to "show")
        mediaContext = MediaContext(mediaInfo, metadata)

        eventCaptor = ArgumentCaptor.forClass(XDMMediaEvent::class.java)
        sessionIdCaptor = ArgumentCaptor.forClass(String::class.java)
        mockEventProcessor = Mockito.mock(MediaEventProcessor::class.java)
        Mockito.`when`(mockEventProcessor.createSession()).thenReturn((currSessionId++).toString())

        eventGenerator = MediaXDMEventGenerator(mediaContext, mockEventProcessor, mapOf(), 0)
    }

    @Test
    fun testProcessSessionStart() {
        // setup
        val sessionDetails = MediaXDMEventHelper.generateSessionDetails(mediaInfo, metadata)
        sessionDetails.show = "show"

        val customMetadata = listOf(XDMCustomMetadata("k1", "v1"))

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()
        mediaCollection.sessionDetails = sessionDetails
        mediaCollection.customMetadata = customMetadata

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.SESSION_START, Date(0), mediaCollection))

        // test
        eventGenerator.processSessionStart()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessSessionStart_withValidChannelValuePassedInTrackerConfig() {
        // setup
        eventGenerator = MediaXDMEventGenerator(
            mediaContext,
            mockEventProcessor,
            mapOf(MediaConstants.TrackerConfig.CHANNEL to "channel"),
            0
        )
        val sessionDetails = MediaXDMEventHelper.generateSessionDetails(mediaInfo, metadata)
        sessionDetails.show = "show"
        sessionDetails.channel = "channel"

        val customMetadata = listOf(XDMCustomMetadata("k1", "v1"))

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()
        mediaCollection.sessionDetails = sessionDetails
        mediaCollection.customMetadata = customMetadata

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.SESSION_START, Date(0), mediaCollection))

        // test
        eventGenerator.processSessionStart()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessSessionComplete() {
        // setup
        updateTs(10, reset = true)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.SESSION_COMPLETE, Date(10), mediaCollection))

        // test
        eventGenerator.processSessionComplete()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessSessionComplete_afterSessionEnd_getsIgnored() {
        // setup
        updateTs(10, reset = true)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.SESSION_END, Date(10), mediaCollection))

        // test
        eventGenerator.processSessionEnd()
        eventGenerator.processSessionComplete() // ignored

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val capturedEventValues = eventCaptor.allValues

        assertEquals(1, capturedEventValues.size)
        assertEquals(expectedEvent, capturedEventValues[0])
    }

    @Test
    fun testProcessSessionEnd() {
        // setup
        updateTs(10, reset = true)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.SESSION_END, Date(10), mediaCollection))

        // test
        eventGenerator.processSessionEnd()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessSessionEnd_afterSessionComplete_getsIgnored() {
        // setup
        updateTs(10, reset = true)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.SESSION_COMPLETE, Date(10), mediaCollection))

        // test
        eventGenerator.processSessionComplete()
        eventGenerator.processSessionEnd() // ignored

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val capturedEventValues = eventCaptor.allValues

        assertEquals(1, capturedEventValues.size)
        assertEquals(expectedEvent, capturedEventValues[0])
    }

    @Test
    fun testProcessAdBreakStart() {
        // setup
        val adBreakInfo = AdBreakInfo.create("adBreak", 1, 2)
        mediaContext.adBreakInfo = adBreakInfo
        val adBreakDetails = MediaXDMEventHelper.generateAdvertisingPodDetails(adBreakInfo)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()
        mediaCollection.advertisingPodDetails = adBreakDetails

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.AD_BREAK_START, Date(0), mediaCollection))

        // test
        eventGenerator.processAdBreakStart()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessAdBreakSkip() {
        // setup
        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.AD_BREAK_COMPLETE, Date(0), mediaCollection))

        // test
        eventGenerator.processAdBreakSkip()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessAdBreakComplete() {
        // setup
        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.AD_BREAK_COMPLETE, Date(0), mediaCollection))

        // test
        eventGenerator.processAdBreakComplete()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessAdStart() {
        // setup
        val adInfo = AdInfo.create("id", "ad", 1, 15)
        val metadata = mapOf(MediaConstants.AdMetadataKeys.SITE_ID to "testSiteID", "k1" to "v1")
        mediaContext.setAdInfo(adInfo, metadata)

        val adDetails = MediaXDMEventHelper.generateAdvertisingDetails(adInfo, metadata)
        val adMetadata = MediaXDMEventHelper.generateAdCustomMetadata(metadata)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()
        mediaCollection.advertisingDetails = adDetails
        mediaCollection.customMetadata = adMetadata

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.AD_START, Date(0), mediaCollection))

        // test
        eventGenerator.processAdStart()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessAdSkip() {
        // setup
        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.AD_SKIP, Date(0), mediaCollection))

        // test
        eventGenerator.processAdSkip()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessAdComplete() {
        // setup
        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.AD_COMPLETE, Date(0), mediaCollection))

        // test
        eventGenerator.processAdComplete()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessChapterStart() {
        // setup
        val chapterInfo = ChapterInfo.create("chapter", 1, 15, 30)
        val metadata = mapOf("k1" to "v1")
        mediaContext.setChapterInfo(chapterInfo, metadata)

        val chapterDetails = MediaXDMEventHelper.generateChapterDetails(chapterInfo)
        val chapterMetadata = MediaXDMEventHelper.generateChapterMetadata(metadata)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()
        mediaCollection.chapterDetails = chapterDetails
        mediaCollection.customMetadata = chapterMetadata

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.CHAPTER_START, Date(0), mediaCollection))

        // test
        eventGenerator.processChapterStart()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessChapterSkip() {
        // setup
        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.CHAPTER_SKIP, Date(0), mediaCollection))

        // test
        eventGenerator.processChapterSkip()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessChapterComplete() {
        // setup
        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.CHAPTER_COMPLETE, Date(0), mediaCollection))

        // test
        eventGenerator.processChapterComplete()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessSessionAbort() {
        // setup
        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.SESSION_END, Date(0), mediaCollection))

        // test
        eventGenerator.processSessionAbort()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessSessionRestart() {
        // setup
        val sessionDetails = MediaXDMEventHelper.generateSessionDetails(mediaInfo, metadata, forceResume = true)
        sessionDetails.show = "show"

        val customMetadata = listOf(XDMCustomMetadata("k1", "v1"))

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()
        mediaCollection.sessionDetails = sessionDetails
        mediaCollection.customMetadata = customMetadata

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.SESSION_START, Date(0), mediaCollection))

        // test
        eventGenerator.processSessionRestart()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessSessionRestart_withinChapterRestartsChapter() {
        // setup
        val chapterInfo = ChapterInfo.create("chapter", 1, 15, 30)
        val metadata = mapOf("k1" to "v1")
        mediaContext.setChapterInfo(chapterInfo, metadata)
        val chapterDetails = MediaXDMEventHelper.generateChapterDetails(chapterInfo)
        val chapterMetadata = MediaXDMEventHelper.generateChapterMetadata(metadata)

        val sessionDetails = MediaXDMEventHelper.generateSessionDetails(mediaInfo, metadata, forceResume = true)
        sessionDetails.show = "show"

        val customMetadata = listOf(XDMCustomMetadata("k1", "v1"))

        val mediaCollection1 = XDMMediaCollection()
        mediaCollection1.playhead = getPlayhead()
        mediaCollection1.sessionDetails = sessionDetails
        mediaCollection1.customMetadata = customMetadata

        val mediaCollection2 = XDMMediaCollection()
        mediaCollection2.playhead = getPlayhead()
        mediaCollection2.chapterDetails = chapterDetails
        mediaCollection2.customMetadata = chapterMetadata

        val expectedEvent1 = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.SESSION_START, Date(0), mediaCollection1))
        val expectedEvent2 = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.CHAPTER_START, Date(0), mediaCollection2))

        // test
        eventGenerator.processSessionRestart()

        // verify
        verify(mockEventProcessor, times(2)).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val capturedEventValues = eventCaptor.allValues
        assertEquals(expectedEvent1, capturedEventValues[0])
        assertEquals(expectedEvent2, capturedEventValues[1])
    }

    @Test
    fun testProcessSessionRestart_withinAdBreakAndAdRestartsAdBreakAndAd() {
        // setup
        val adBreakInfo = AdBreakInfo.create("adBreak", 1, 2)
        mediaContext.adBreakInfo = adBreakInfo
        val adBreakDetails = MediaXDMEventHelper.generateAdvertisingPodDetails(adBreakInfo)

        val adInfo = AdInfo.create("id", "ad", 1, 15)
        val metadata = mapOf(MediaConstants.AdMetadataKeys.SITE_ID to "testSiteID", "k1" to "v1")
        mediaContext.setAdInfo(adInfo, metadata)

        val adDetails = MediaXDMEventHelper.generateAdvertisingDetails(adInfo, metadata)
        val adMetadata = MediaXDMEventHelper.generateAdCustomMetadata(metadata)

        val sessionDetails = MediaXDMEventHelper.generateSessionDetails(mediaInfo, metadata, forceResume = true)
        sessionDetails.show = "show"

        val customMetadata = listOf(XDMCustomMetadata("k1", "v1"))

        val mediaCollection1 = XDMMediaCollection()
        mediaCollection1.playhead = getPlayhead()
        mediaCollection1.sessionDetails = sessionDetails
        mediaCollection1.customMetadata = customMetadata

        val mediaCollection2 = XDMMediaCollection()
        mediaCollection2.playhead = getPlayhead()
        mediaCollection2.advertisingPodDetails = adBreakDetails

        val mediaCollection3 = XDMMediaCollection()
        mediaCollection3.playhead = getPlayhead()
        mediaCollection3.advertisingDetails = adDetails
        mediaCollection3.customMetadata = adMetadata

        val expectedEvent1 = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.SESSION_START, Date(0), mediaCollection1))
        val expectedEvent2 = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.AD_BREAK_START, Date(0), mediaCollection2))
        val expectedEvent3 = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.AD_START, Date(0), mediaCollection3))
        // test
        eventGenerator.processSessionRestart()

        // verify
        verify(mockEventProcessor, times(3)).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val capturedEventValues = eventCaptor.allValues
        assertEquals(expectedEvent1, capturedEventValues[0])
        assertEquals(expectedEvent2, capturedEventValues[1])
        assertEquals(expectedEvent3, capturedEventValues[2])
    }

    @Test
    fun testProcessSessionRestart_withPlayerStatesActiveRestartsActivePlayerStates() {
        // setup
        mediaContext.startState(StateInfo.create(MediaConstants.PlayerState.FULLSCREEN))
        mediaContext.startState(StateInfo.create(MediaConstants.PlayerState.MUTE))
        mediaContext.startState(StateInfo.create("customPlayerState"))
        val playerStatesOrderedList = mediaContext.activeTrackedStates

        val sessionDetails = MediaXDMEventHelper.generateSessionDetails(mediaInfo, metadata, forceResume = true)
        sessionDetails.show = "show"

        val customMetadata = listOf(XDMCustomMetadata("k1", "v1"))

        val mediaCollection1 = XDMMediaCollection()
        mediaCollection1.playhead = getPlayhead()
        mediaCollection1.sessionDetails = sessionDetails
        mediaCollection1.customMetadata = customMetadata

        val mediaCollection2 = XDMMediaCollection()
        mediaCollection2.playhead = getPlayhead()
        mediaCollection2.statesStart = listOf(XDMPlayerStateData(playerStatesOrderedList[0].stateName))

        val mediaCollection3 = XDMMediaCollection()
        mediaCollection3.playhead = getPlayhead()
        mediaCollection3.statesStart = listOf(XDMPlayerStateData(playerStatesOrderedList[1].stateName))

        val mediaCollection4 = XDMMediaCollection()
        mediaCollection4.playhead = getPlayhead()
        mediaCollection4.statesStart = listOf(XDMPlayerStateData(playerStatesOrderedList[2].stateName))

        val expectedEvent1 = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.SESSION_START, Date(0), mediaCollection1))
        val expectedEvent2 = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.STATES_UPDATE, Date(0), mediaCollection2))
        val expectedEvent3 = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.STATES_UPDATE, Date(0), mediaCollection3))
        val expectedEvent4 = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.STATES_UPDATE, Date(0), mediaCollection4))
        // test
        eventGenerator.processSessionRestart()

        // verify
        verify(mockEventProcessor, times(4)).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val capturedEventValues = eventCaptor.allValues
        assertEquals(expectedEvent1, capturedEventValues[0])
        assertEquals(expectedEvent2, capturedEventValues[1])
        assertEquals(expectedEvent3, capturedEventValues[2])
        assertEquals(expectedEvent4, capturedEventValues[3])
    }

    @Test
    fun testProcessBitrateChange() {
        // setup
        val qoeInfo = QoEInfo.create(1234, 12, 123, 1)
        mediaContext.qoEInfo = qoeInfo

        val qoeDetails = MediaXDMEventHelper.generateQoEDataDetails(qoeInfo)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()
        mediaCollection.qoeDataDetails = qoeDetails

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.BITRATE_CHANGE, Date(0), mediaCollection))

        // test
        eventGenerator.processBitrateChange()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessError() {
        // setup
        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()
        mediaCollection.errorDetails = MediaXDMEventHelper.generateErrorDetails("errorID")

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.ERROR, Date(0), mediaCollection))

        // test
        eventGenerator.processError("errorID")

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessPlaybackPlay() {
        // setup
        mediaContext.enterState(MediaPlaybackState.Play)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PLAY, Date(0), mediaCollection))

        // test
        eventGenerator.processPlayback()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessPlaybackPause() {
        // setup
        mediaContext.enterState(MediaPlaybackState.Pause)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PAUSE_START, Date(0), mediaCollection))

        // test
        eventGenerator.processPlayback()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessPlaybackSeek() {
        // setup
        mediaContext.enterState(MediaPlaybackState.Seek)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PAUSE_START, Date(0), mediaCollection))

        // test
        eventGenerator.processPlayback()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessPlaybackBuffer() {
        // setup
        mediaContext.enterState(MediaPlaybackState.Buffer)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.BUFFER_START, Date(0), mediaCollection))

        // test
        eventGenerator.processPlayback()

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessPlaybackWithDoFlushSetToTrue() {
        // setup
        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PING, Date(0), mediaCollection))

        // test
        eventGenerator.processPlayback(doFlush = true)

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessPlayback_afterSessionEnd_getsIgnored() {
        // setup
        mediaContext.enterState(MediaPlaybackState.Play)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.SESSION_END, Date(0), mediaCollection))

        // test
        eventGenerator.processSessionEnd()
        eventGenerator.processPlayback() // ignored

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val capturedEventValues = eventCaptor.allValues

        assertEquals(1, capturedEventValues.size)
        assertEquals(expectedEvent, capturedEventValues[0])
    }

    @Test
    fun testProcessPlayback_afterSessionComplete_getsIgnored() {
        // setup
        mediaContext.enterState(MediaPlaybackState.Play)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.SESSION_COMPLETE, Date(0), mediaCollection))

        // test
        eventGenerator.processSessionComplete()
        eventGenerator.processPlayback() // ignored

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val capturedEventValues = eventCaptor.allValues

        assertEquals(1, capturedEventValues.size)
        assertEquals(expectedEvent, capturedEventValues[0])
    }

    @Test
    fun testProcessStateStart() {
        // setup
        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()
        mediaCollection.statesStart = listOf(XDMPlayerStateData(MediaConstants.PlayerState.FULLSCREEN))

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.STATES_UPDATE, Date(0), mediaCollection))

        // test
        eventGenerator.processStateStart(StateInfo.create(MediaConstants.PlayerState.FULLSCREEN))

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testProcessStateEnd() {
        // setup
        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()
        mediaCollection.statesEnd = listOf(XDMPlayerStateData(MediaConstants.PlayerState.FULLSCREEN))

        val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.STATES_UPDATE, Date(0), mediaCollection))

        // test
        eventGenerator.processStateEnd(StateInfo.create(MediaConstants.PlayerState.FULLSCREEN))

        // verify
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        val actualEvent = eventCaptor.value

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testCustomMainPingInterval_validRange_sendsPingWithCustomValue() {
        val validIntervals = listOf(10, 11, 22, 33, 44, 50)
        for (interval in validIntervals) {
            mockEventProcessor = Mockito.mock(MediaEventProcessor::class.java) // create new mock for each iteration

            val internalMS = interval * 1000
            val trackerConfig = mapOf<String, Any>(MediaConstants.TrackerConfig.MAIN_PING_INTERVAL to interval)
            eventGenerator = MediaXDMEventGenerator(mediaContext, mockEventProcessor, trackerConfig, 0)
            updateTs(internalMS, reset = true)

            val mediaCollection = XDMMediaCollection()
            mediaCollection.playhead = getPlayhead()
            val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PING, Date(mockTimestamp), mediaCollection))

            // test
            eventGenerator.processPlayback()

            // verify
            verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
            val actualEvent = eventCaptor.value

            assertEquals(expectedEvent, actualEvent)
        }
    }

    @Test
    fun testCustomMainPingInterval_invalidRange_sendsPingWithDefaultValue() {
        val invalidIntervals = listOf(0, 1, 9, 51, 400)
        for (interval in invalidIntervals) {
            mockEventProcessor = Mockito.mock(MediaEventProcessor::class.java) // create new mock for each iteration

            val trackerConfig = mapOf<String, Any>(MediaConstants.TrackerConfig.MAIN_PING_INTERVAL to interval)
            eventGenerator = MediaXDMEventGenerator(mediaContext, mockEventProcessor, trackerConfig, 0)
            updateTs(MediaInternalConstants.PingInterval.REALTIME_TRACKING_MS, reset = true)

            val mediaCollection = XDMMediaCollection()
            mediaCollection.playhead = getPlayhead()
            val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PING, Date(mockTimestamp), mediaCollection))

            // test
            eventGenerator.processPlayback()

            // verify
            verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
            val actualEvent = eventCaptor.value

            assertEquals(expectedEvent, actualEvent)
        }
    }

    @Test
    fun testCustomAdPingInterval_validRange_sendsPingWithCustomValue() {
        val validIntervals = listOf(1, 3, 9, 10)
        for (interval in validIntervals) {
            mockEventProcessor = Mockito.mock(MediaEventProcessor::class.java) // create new mock for each iteration

            val internalMS = interval * 1000
            val trackerConfig = mapOf<String, Any>(MediaConstants.TrackerConfig.AD_PING_INTERVAL to interval)
            eventGenerator = MediaXDMEventGenerator(mediaContext, mockEventProcessor, trackerConfig, 0)
            updateTs(internalMS, reset = true)
            // mock adStart
            mediaContext.setAdInfo(AdInfo.create("id", "ad", 1, 15), mapOf())

            val mediaCollection = XDMMediaCollection()
            mediaCollection.playhead = getPlayhead()
            val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PING, Date(mockTimestamp), mediaCollection))

            // test
            eventGenerator.processPlayback()

            // verify
            verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
            val actualEvent = eventCaptor.value

            assertEquals(expectedEvent, actualEvent)
        }
    }

    @Test
    fun testCustomAdPingInterval_invalidRange_sendsPingWithDefaultValue() {
        val invalidIntervals = listOf(0, 11, 50, 400)
        for (interval in invalidIntervals) {
            mockEventProcessor = Mockito.mock(MediaEventProcessor::class.java) // create new mock for each iteration

            val trackerConfig = mapOf<String, Any>(MediaConstants.TrackerConfig.AD_PING_INTERVAL to interval)
            eventGenerator = MediaXDMEventGenerator(mediaContext, mockEventProcessor, trackerConfig, 0)
            updateTs(MediaInternalConstants.PingInterval.REALTIME_TRACKING_MS, reset = true)
            // mock adStart
            mediaContext.setAdInfo(AdInfo.create("id", "ad", 1, 15), mapOf())

            val mediaCollection = XDMMediaCollection()
            mediaCollection.playhead = getPlayhead()
            val expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PING, Date(mockTimestamp), mediaCollection))

            // test
            eventGenerator.processPlayback()

            // verify
            verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
            val actualEvent = eventCaptor.value

            assertEquals(expectedEvent, actualEvent)
        }
    }

    @Test
    fun testCustomMainPingIntervalAndCustomAdPingInterval_validRange_sendsPingWithCustomValue() {
        // setup
        val trackerConfig = mapOf<String, Any>(
            MediaConstants.TrackerConfig.MAIN_PING_INTERVAL to 15,
            MediaConstants.TrackerConfig.AD_PING_INTERVAL to 3
        )
        eventGenerator = MediaXDMEventGenerator(mediaContext, mockEventProcessor, trackerConfig, 0)

        updateTs(15 * 1000)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()
        var expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PING, Date(mockTimestamp), mediaCollection))

        // test main content ping interval
        eventGenerator.processPlayback()

        // verify main content ping interval
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        assertEquals(expectedEvent, eventCaptor.value)

        // mock adStart
        mediaContext.setAdInfo(AdInfo.create("id", "ad", 1, 15), mapOf())
        updateTs(3 * 1000)

        mediaCollection.playhead = getPlayhead()
        expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PING, Date(mockTimestamp), mediaCollection))

        // test ad content ping interval
        eventGenerator.processPlayback()

        // verify ad content ping interval
        verify(mockEventProcessor, times(2)).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        assertEquals(expectedEvent, eventCaptor.value)
    }

    @Test
    fun testDefaultMainPingIntervalAndCustomAdPingInterval() {
        // setup
        val trackerConfig = mapOf<String, Any>(
            MediaConstants.TrackerConfig.AD_PING_INTERVAL to 3
        )
        eventGenerator = MediaXDMEventGenerator(mediaContext, mockEventProcessor, trackerConfig, 0)

        updateTs(MediaInternalConstants.PingInterval.REALTIME_TRACKING_MS)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()
        var expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PING, Date(mockTimestamp), mediaCollection))

        // test main content ping interval
        eventGenerator.processPlayback()

        // verify main content ping interval
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        assertEquals(expectedEvent, eventCaptor.value)

        // mock adStart
        mediaContext.setAdInfo(AdInfo.create("id", "ad", 1, 15), mapOf())
        updateTs(3 * 1000)

        mediaCollection.playhead = getPlayhead()
        expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PING, Date(mockTimestamp), mediaCollection))

        // test ad content ping interval
        eventGenerator.processPlayback()

        // verify ad content ping interval
        verify(mockEventProcessor, times(2)).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        assertEquals(expectedEvent, eventCaptor.value)

        // mock ad exit
        mediaContext.clearAdInfo()
        updateTs(MediaInternalConstants.PingInterval.REALTIME_TRACKING_MS)
        eventGenerator.processPlayback()

        mediaCollection.playhead = getPlayhead()
        expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PING, Date(mockTimestamp), mediaCollection))

        // verify main content ping interval after exiting from ad
        verify(mockEventProcessor, times(3)).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        assertEquals(expectedEvent, eventCaptor.value)
    }

    @Test
    fun testCustomMainPingIntervalAndDefaultAdPingInterval() {
        // setup
        val trackerConfig = mapOf<String, Any>(
            MediaConstants.TrackerConfig.MAIN_PING_INTERVAL to 15
        )
        eventGenerator = MediaXDMEventGenerator(mediaContext, mockEventProcessor, trackerConfig, 0)

        updateTs(15 * 1000)

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = getPlayhead()
        var expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PING, Date(mockTimestamp), mediaCollection))

        // test main content ping interval
        eventGenerator.processPlayback()

        // verify main content ping interval
        verify(mockEventProcessor).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        assertEquals(expectedEvent, eventCaptor.value)

        // mock adStart
        mediaContext.setAdInfo(AdInfo.create("id", "ad", 1, 15), mapOf())
        updateTs(MediaInternalConstants.PingInterval.REALTIME_TRACKING_MS)

        mediaCollection.playhead = getPlayhead()
        expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PING, Date(mockTimestamp), mediaCollection))

        // test ad content ping interval
        eventGenerator.processPlayback()

        // verify ad content ping interval
        verify(mockEventProcessor, times(2)).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        assertEquals(expectedEvent, eventCaptor.value)

        // mock ad exit
        mediaContext.clearAdInfo()
        updateTs(15 * 1000)
        eventGenerator.processPlayback()

        mediaCollection.playhead = getPlayhead()
        expectedEvent = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PING, Date(mockTimestamp), mediaCollection))

        // verify main content ping interval after exiting from ad
        verify(mockEventProcessor, times(3)).processEvent(capture(sessionIdCaptor), capture(eventCaptor))
        assertEquals(expectedEvent, eventCaptor.value)
    }

    private fun updateTs(interval: Int, updatePlayhead: Boolean = true, reset: Boolean = false) {
        if (reset) {
            mockPlayhead = 0
            mockTimestamp = 0
        }
        mockTimestamp += interval
        if (updatePlayhead) {
            mockPlayhead += (interval / 1000)
            mediaContext.playhead = mockPlayhead
        }

        mediaContext.playhead = mockPlayhead
        eventGenerator.setRefTS(mockTimestamp)
    }

    private fun getPlayhead(): Int {
        return mediaContext.playhead
    }
}
