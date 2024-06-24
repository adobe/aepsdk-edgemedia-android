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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.edge.identity.Identity
import com.adobe.marketing.mobile.edge.media.Media
import com.adobe.marketing.mobile.edge.media.MediaConstants
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEventType
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.HttpMethod.POST
import com.adobe.marketing.mobile.util.TestHelper
import com.adobe.marketing.mobile.util.TestHelper.LogOnErrorRule
import com.adobe.marketing.mobile.util.TestHelper.setExpectationEvent
import com.adobe.marketing.mobile.util.TestHelper.SetupCoreRule
import com.adobe.marketing.mobile.util.JsonTestUtils
import com.adobe.marketing.mobile.util.MockNetworkService
import com.adobe.marketing.mobile.util.MonitorExtension
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class MediaEdgeIntegrationTests {

    private val mockNetworkService = MockNetworkService()
    companion object {
        private const val EDGE_CONFIG_ID = "1234abcd-abcd-1234-5678-123456abcdef"
        private const val SESSION_START_URL = "https://edge.adobedc.net/ee/va/v1/sessionStart"
        private const val testBackendSessionId = "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"
        private const val SUCCESS_RESPONSE_STRING =
            "\u0000{\"handle\":[{\"payload\":[{\"sessionId\":\"99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d\"}],\"type\":\"media-analytics:new-session\",\"eventIndex\":0}]}"
        private const val ERROR_RESPONSE_STRING =
            "\u0000{\"errors\" : [{\"type\" : \"https://ns.adobe.com/aep/errors/va-edge-0400-400\", \"status\" : 400, \"title\": \"Invalid request\", \"report\":{\"eventIndex\":0,\"details\":[{\"name\":\"\$.xdm.mediaCollection.sessionDetails.name\",\"reason\":\"Missing required field\"}]}}]}"
        private val mediaInfo =
            Media.createMediaObject("testName", "testId", 30, "VOD", Media.MediaType.Audio)
        private val adBreakInfo = Media.createAdBreakObject("testName", 1, 1)
        private val adInfo = Media.createAdObject("testName", "testId", 1, 15)
        private val chapterInfo = Media.createChapterObject("testName", 1, 30, 2)
        private val qoeInfo = Media.createQoEObject(1, 2, 3, 4)
        private val muteStateInfo = Media.createStateObject(MediaConstants.PlayerState.MUTE)
        private val customStateInfo = Media.createStateObject("testStateName")
        private val metadata = mutableMapOf("testKey" to "testValue")
        private val configuration = mutableMapOf(
            "edge.configId" to "12345-example",
            "edgeMedia.channel" to "testChannel",
            "edgeMedia.playerName" to "testPlayerName"

        )
    }

    @Rule
    @JvmField
    var rule: RuleChain = RuleChain
        .outerRule(LogOnErrorRule())
        .around(SetupCoreRule())

    @Before
    fun setup() {
        ServiceProvider.getInstance().networkService = mockNetworkService
        setExpectationEvent(EventType.CONFIGURATION, EventSource.REQUEST_CONTENT, 1)
        setExpectationEvent(EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT, 1)
        setExpectationEvent(EventType.HUB, EventSource.SHARED_STATE, 4)

        MobileCore.updateConfiguration(
            mapOf(
                "edge.configId" to EDGE_CONFIG_ID,
                "edgeMedia.channel" to "testChannel",
                "edgeMedia.playerName" to "testPlayerName"
            )
        )

        val latch = CountDownLatch(1)
        MobileCore.registerExtensions(
            listOf(Edge.EXTENSION, Identity.EXTENSION, Media.EXTENSION, MonitorExtension.EXTENSION)
        ) {
            latch.countDown()
        }

        latch.await()
        TestHelper.assertExpectedEvents(false)
        resetTestExpectations()
    }

    @After
    fun tearDown() {
        mockNetworkService.reset();
    }

    @Test
    fun testPlayback_singleSession_play_pause_complete() {

        val responseConnection: HttpConnecting = mockNetworkService.createMockNetworkResponse(SUCCESS_RESPONSE_STRING, 200)

        mockNetworkService.setMockResponseFor(SESSION_START_URL, POST, responseConnection)

        // test
        val tracker = Media.createTracker()
        tracker.trackSessionStart(mediaInfo, metadata)
        tracker.trackPlay()
        tracker.updateCurrentPlayhead(7)
        tracker.trackPause()
        tracker.trackComplete()

        mockNetworkService.assertAllNetworkRequestExpectations()
        // verify
        val networkRequests = mockNetworkService.getAllNetworkRequests()
        assertEquals(4, networkRequests.size)
        assertXDMData(networkRequests[0], XDMMediaEventType.SESSION_START, mediaInfo, metadata, configuration)
        assertXDMData(networkRequests[1], XDMMediaEventType.PLAY, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[2], XDMMediaEventType.PAUSE_START, backendSessionId = testBackendSessionId, playhead = 7)
        assertXDMData(networkRequests[3], XDMMediaEventType.SESSION_COMPLETE, backendSessionId = testBackendSessionId, playhead = 7)
    }

    @Test
    fun testSessionStartErrorResponse_shouldNotSendAnyOtherNetworkRequests() {

        val responseConnection: HttpConnecting = mockNetworkService.createMockNetworkResponse(ERROR_RESPONSE_STRING, 400)

        mockNetworkService.setMockResponseFor(SESSION_START_URL, POST, responseConnection)

        // test
        val tracker = Media.createTracker()
        tracker.trackSessionStart(mediaInfo, metadata)
        tracker.trackPlay()
        tracker.updateCurrentPlayhead(7)
        tracker.trackPause()
        tracker.trackComplete()

        // verify
        mockNetworkService.assertAllNetworkRequestExpectations()

        val networkRequests = mockNetworkService.getAllNetworkRequests()
        assertEquals(1, networkRequests.size)
        assertXDMData(networkRequests[0], XDMMediaEventType.SESSION_START, mediaInfo, metadata, configuration)
    }

    @Test
    fun testPlayback_withPrerollAdBreak() {
        val responseConnection: HttpConnecting = mockNetworkService.createMockNetworkResponse(SUCCESS_RESPONSE_STRING, 200)
        mockNetworkService.setMockResponseFor(SESSION_START_URL, POST, responseConnection)

        // test
        val tracker = Media.createTracker()
        tracker.trackSessionStart(mediaInfo, metadata)
        tracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo, null)
        tracker.updateQoEObject(qoeInfo)
        tracker.trackEvent(Media.Event.AdStart, adInfo, metadata)
        tracker.trackPlay()
        tracker.trackEvent(Media.Event.AdComplete, null, null)
        tracker.trackEvent(Media.Event.AdBreakComplete, null, null)
        tracker.trackComplete()

        // verify
        mockNetworkService.assertAllNetworkRequestExpectations()

        // verify
        val networkRequests = mockNetworkService.getAllNetworkRequests()
        assertEquals(8, networkRequests.size)
        assertXDMData(networkRequests[0], XDMMediaEventType.SESSION_START, mediaInfo, metadata, configuration)
        assertXDMData(networkRequests[1], XDMMediaEventType.AD_BREAK_START, adBreakInfo, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[2], XDMMediaEventType.AD_START, adInfo, metadata, configuration, testBackendSessionId, qoeInfo)
        assertXDMData(networkRequests[3], XDMMediaEventType.PLAY, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[4], XDMMediaEventType.AD_COMPLETE, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[5], XDMMediaEventType.AD_BREAK_COMPLETE, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[6], XDMMediaEventType.PLAY, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[7], XDMMediaEventType.SESSION_COMPLETE, backendSessionId = testBackendSessionId)
    }

    @Test
    fun testPlayback_withSingleChapter() {
        val responseConnection: HttpConnecting = mockNetworkService.createMockNetworkResponse(SUCCESS_RESPONSE_STRING, 200)
        mockNetworkService.setMockResponseFor(SESSION_START_URL, POST, responseConnection)

        // test
        val tracker = Media.createTracker()
        tracker.trackSessionStart(mediaInfo, metadata)
        tracker.trackEvent(Media.Event.ChapterStart, chapterInfo, metadata)
        tracker.trackPlay()
        tracker.trackEvent(Media.Event.ChapterComplete, null, null)
        tracker.trackComplete()

        mockNetworkService.assertAllNetworkRequestExpectations()

        // verify
        val networkRequests = mockNetworkService.getAllNetworkRequests()
        assertEquals(5, networkRequests.size)
        assertXDMData(networkRequests[0], XDMMediaEventType.SESSION_START, mediaInfo, metadata, configuration)
        assertXDMData(networkRequests[1], XDMMediaEventType.CHAPTER_START, chapterInfo, metadata, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[2], XDMMediaEventType.PLAY, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[3], XDMMediaEventType.CHAPTER_COMPLETE, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[4], XDMMediaEventType.SESSION_COMPLETE, backendSessionId = testBackendSessionId)
    }

    @Test
    fun testPlayback_withBuffer_withSeek_withBitrate_withQoeUpdate_withError() {

        val responseConnection: HttpConnecting = mockNetworkService.createMockNetworkResponse(SUCCESS_RESPONSE_STRING, 200)
        mockNetworkService.setMockResponseFor(SESSION_START_URL, POST, responseConnection)

        // test
        val tracker = Media.createTracker()
        tracker.trackSessionStart(mediaInfo, metadata)
        tracker.trackPlay()
        tracker.updateCurrentPlayhead(5)
        tracker.trackEvent(Media.Event.BufferStart, null, null)
        tracker.trackEvent(Media.Event.BufferComplete, null, null)
        tracker.updateCurrentPlayhead(10)
        tracker.updateQoEObject(qoeInfo)
        tracker.trackEvent(Media.Event.BitrateChange, qoeInfo, null)
        tracker.updateCurrentPlayhead(15)
        tracker.trackEvent(Media.Event.SeekStart, null, null)
        tracker.trackEvent(Media.Event.SeekComplete, null, null)
        tracker.trackError("testError")
        tracker.updateCurrentPlayhead(20)
        tracker.trackComplete()

        mockNetworkService.assertAllNetworkRequestExpectations()

        // verify
        val networkRequests = mockNetworkService.getAllNetworkRequests()
        assertEquals(9, networkRequests.size)
        assertXDMData(networkRequests[0], XDMMediaEventType.SESSION_START, mediaInfo, metadata, configuration)
        assertXDMData(networkRequests[1], XDMMediaEventType.PLAY, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[2], XDMMediaEventType.BUFFER_START, backendSessionId = testBackendSessionId, playhead = 5)
        assertXDMData(networkRequests[3], XDMMediaEventType.PLAY, backendSessionId = testBackendSessionId, playhead = 5)
        assertXDMData(networkRequests[4], XDMMediaEventType.BITRATE_CHANGE, info = qoeInfo, backendSessionId = testBackendSessionId, playhead = 10)
        assertXDMData(networkRequests[5], XDMMediaEventType.PAUSE_START, backendSessionId = testBackendSessionId, playhead = 15)
        assertXDMData(networkRequests[6], XDMMediaEventType.PLAY, backendSessionId = testBackendSessionId, playhead = 15)
        assertXDMData(networkRequests[7], XDMMediaEventType.ERROR, info = mapOf("error.id" to "testError", "error.source" to "player"), backendSessionId = testBackendSessionId, playhead = 15)
        assertXDMData(networkRequests[8], XDMMediaEventType.SESSION_COMPLETE, backendSessionId = testBackendSessionId, playhead = 20)
    }

    @Test
    fun testPlayback_withPrerollAdBreak_noAdComplete_noAdbreakComplete_withSessionEnd() {
        val responseConnection: HttpConnecting = mockNetworkService.createMockNetworkResponse(SUCCESS_RESPONSE_STRING, 200)
        mockNetworkService.setMockResponseFor(SESSION_START_URL, POST, responseConnection)

        // test
        val tracker = Media.createTracker()
        tracker.trackSessionStart(mediaInfo, metadata)
        tracker.trackEvent(Media.Event.AdBreakStart, adBreakInfo, null)
        tracker.trackEvent(Media.Event.AdStart, adInfo, metadata)
        tracker.trackPlay()
        tracker.trackComplete()

        mockNetworkService.assertAllNetworkRequestExpectations()

        // verify
        val networkRequests = mockNetworkService.getAllNetworkRequests()
        assertEquals(7, networkRequests.size)
        assertXDMData(networkRequests[0], XDMMediaEventType.SESSION_START, mediaInfo, metadata, configuration)
        assertXDMData(networkRequests[1], XDMMediaEventType.AD_BREAK_START, adBreakInfo, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[2], XDMMediaEventType.AD_START, adInfo, metadata, configuration, testBackendSessionId)
        assertXDMData(networkRequests[3], XDMMediaEventType.PLAY, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[4], XDMMediaEventType.AD_SKIP, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[5], XDMMediaEventType.AD_BREAK_COMPLETE, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[6], XDMMediaEventType.SESSION_COMPLETE, backendSessionId = testBackendSessionId)
    }

    @Test
    fun testPlayback_withChapterStart_noChapterComplete_withSessionEnd() {
        val responseConnection: HttpConnecting = mockNetworkService.createMockNetworkResponse(SUCCESS_RESPONSE_STRING, 200)
        mockNetworkService.setMockResponseFor(SESSION_START_URL, POST, responseConnection)

        // test
        val tracker = Media.createTracker()
        tracker.trackSessionStart(mediaInfo, metadata)
        tracker.trackEvent(Media.Event.ChapterStart, chapterInfo, metadata)
        tracker.trackPlay()
        tracker.updateCurrentPlayhead(12)
        tracker.trackSessionEnd()

        mockNetworkService.assertAllNetworkRequestExpectations()

        // verify
        val networkRequests = mockNetworkService.getAllNetworkRequests()
        assertEquals(5, networkRequests.size)
        assertXDMData(networkRequests[0], XDMMediaEventType.SESSION_START, mediaInfo, metadata, configuration)
        assertXDMData(networkRequests[1], XDMMediaEventType.CHAPTER_START, chapterInfo, metadata, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[2], XDMMediaEventType.PLAY, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[3], XDMMediaEventType.CHAPTER_SKIP, backendSessionId = testBackendSessionId, playhead = 12)
        assertXDMData(networkRequests[4], XDMMediaEventType.SESSION_END, backendSessionId = testBackendSessionId, playhead = 12)
    }

    @Test
    fun testPlayback_withSingleChapter_withMuteState_withCustomState() {
        val responseConnection: HttpConnecting = mockNetworkService.createMockNetworkResponse(SUCCESS_RESPONSE_STRING, 200)
        mockNetworkService.setMockResponseFor(SESSION_START_URL, POST, responseConnection)

        // test
        val tracker = Media.createTracker()
        tracker.trackSessionStart(mediaInfo, metadata)
        tracker.trackEvent(Media.Event.ChapterStart, chapterInfo, metadata)
        tracker.trackPlay()
        tracker.trackEvent(Media.Event.StateStart, muteStateInfo, null)
        tracker.trackEvent(Media.Event.StateStart, customStateInfo, null)
        tracker.updateCurrentPlayhead(12)
        tracker.trackEvent(Media.Event.StateEnd, customStateInfo, null)
        tracker.trackEvent(Media.Event.StateEnd, muteStateInfo, null)
        tracker.trackEvent(Media.Event.ChapterComplete, null, null)
        tracker.trackComplete()

        mockNetworkService.assertAllNetworkRequestExpectations()

        // verify
        val networkRequests = mockNetworkService.getAllNetworkRequests()
        assertEquals(9, networkRequests.size)
        assertXDMData(networkRequests[0], XDMMediaEventType.SESSION_START, mediaInfo, metadata, configuration)
        assertXDMData(networkRequests[1], XDMMediaEventType.CHAPTER_START, chapterInfo, metadata, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[2], XDMMediaEventType.PLAY, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[3], XDMMediaEventType.STATES_UPDATE, muteStateInfo, backendSessionId = testBackendSessionId, stateStart = true)
        assertXDMData(networkRequests[4], XDMMediaEventType.STATES_UPDATE, customStateInfo, backendSessionId = testBackendSessionId, stateStart = true)
        assertXDMData(networkRequests[5], XDMMediaEventType.STATES_UPDATE, customStateInfo, backendSessionId = testBackendSessionId, playhead = 12, stateStart = false)
        assertXDMData(networkRequests[6], XDMMediaEventType.STATES_UPDATE, muteStateInfo, backendSessionId = testBackendSessionId, playhead = 12, stateStart = false)
        assertXDMData(networkRequests[7], XDMMediaEventType.CHAPTER_COMPLETE, backendSessionId = testBackendSessionId, playhead = 12)
        assertXDMData(networkRequests[8], XDMMediaEventType.SESSION_COMPLETE, backendSessionId = testBackendSessionId, playhead = 12)
    }

    @Test
    fun testPlayback_withChapterStart_noChapterComplete_withMuteStateStart_withCustomStateStart_noMuteStateEnd_noCustomStateEnd_withSessionEnd() {
        val responseConnection: HttpConnecting = mockNetworkService.createMockNetworkResponse(SUCCESS_RESPONSE_STRING, 200)
        mockNetworkService.setMockResponseFor(SESSION_START_URL, POST, responseConnection)

        // test
        val tracker = Media.createTracker()
        tracker.trackSessionStart(mediaInfo, metadata)
        tracker.trackEvent(Media.Event.ChapterStart, chapterInfo, metadata)
        tracker.trackPlay()
        tracker.trackEvent(Media.Event.StateStart, muteStateInfo, null)
        tracker.trackEvent(Media.Event.StateStart, customStateInfo, null)
        tracker.updateCurrentPlayhead(12)
        tracker.trackSessionEnd()

        mockNetworkService.assertAllNetworkRequestExpectations()

        // verify
        val networkRequests = mockNetworkService.getAllNetworkRequests()
        assertEquals(7, networkRequests.size)
        assertXDMData(networkRequests[0], XDMMediaEventType.SESSION_START, mediaInfo, metadata, configuration)
        assertXDMData(networkRequests[1], XDMMediaEventType.CHAPTER_START, chapterInfo, metadata, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[2], XDMMediaEventType.PLAY, backendSessionId = testBackendSessionId)
        assertXDMData(networkRequests[3], XDMMediaEventType.STATES_UPDATE, muteStateInfo, backendSessionId = testBackendSessionId, stateStart = true)
        assertXDMData(networkRequests[4], XDMMediaEventType.STATES_UPDATE, customStateInfo, backendSessionId = testBackendSessionId, stateStart = true)
        assertXDMData(networkRequests[5], XDMMediaEventType.CHAPTER_SKIP, backendSessionId = testBackendSessionId, playhead = 12)
        assertXDMData(networkRequests[6], XDMMediaEventType.SESSION_END, backendSessionId = testBackendSessionId, playhead = 12)
    }

    private fun assertXDMData(networkRequest: NetworkRequest, eventType: XDMMediaEventType, info: Map<String, Any> = mapOf(), metadata: Map<String, String> = mapOf(), configuration: Map<String, Any> = mapOf(), backendSessionId: String? = null, qoeInfo: Map<String, Any>? = null, playhead: Int = 0, stateStart: Boolean = false) {
        val expectedMediaCollectionData = EdgeEventHelper.generateMediaCollection(
            eventType,
            playhead,
            backendSessionId,
            info,
            metadata,
            getMediaStateFrom(configuration),
            qoeInfo,
            stateStart
        )

        val actualXDMData = getXDMDataFromNetworkRequest(networkRequest)

        assertEquals(XDMMediaEventType.getTypeString(eventType), actualXDMData["eventType"] as? String)
        assertNotNull(actualXDMData["timestamp"] as? String)
        assertNotNull(actualXDMData["_id"] as? String)

        val actualMediaCollectionData = actualXDMData["mediaCollection"] as? Map<String, Any> ?: mapOf()

        assertEquals(expectedMediaCollectionData, actualMediaCollectionData)
    }

    private fun getXDMDataFromNetworkRequest(request: NetworkRequest): Map<String, Any> {
        val body = URLDecoder.decode(String(request.body), "UTF-8")
        val requestBodyMap = JsonTestUtils.parseNetworkResponseAsMap(body) ?: return mapOf()
        val eventDataList = requestBodyMap["events"] as? List<Map<String, Any>> ?: return mapOf()

        val eventData = eventDataList[0]

        return eventData["xdm"] as? Map<String, Any> ?: return mapOf()
    }

    private fun getMediaStateFrom(config: Map<String, Any>): MediaState {
        val mediaState = MediaState()
        mediaState.updateState(config)
        return mediaState
    }
    private fun resetTestExpectations() {
        mockNetworkService.reset()
        TestHelper.resetTestExpectations()
    }
}
