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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class MediaEventProcessorTests {
    private var mockState: MediaState = Mockito.mock(MediaState::class.java)
    private val dispatcher: (Event) -> Unit = { }

    private lateinit var mediaEventProcessor: MediaEventProcessor
    private lateinit var mediaSession1: SpyMediaSession
    private lateinit var mediaSession2: SpyMediaSession

    @Before
    fun setup() {
        mediaSession1 = SpyMediaSession("testSession1", mockState, dispatcher)
        mediaSession2 = SpyMediaSession("testSession2", mockState, dispatcher)

        mediaEventProcessor = MediaEventProcessor(mockState, dispatcher)
        mediaEventProcessor.mediaSessions["testSession1"] = mediaSession1
        mediaEventProcessor.mediaSessions["testSession2"] = mediaSession2
    }

    @Test
    fun `abortAllSessions() calls abort on all sessions`() {
        mediaEventProcessor.abortAllSessions()

        assertTrue(mediaSession1.handleSessionAbortCalled)
        assertTrue(mediaSession2.handleSessionAbortCalled)
        assertTrue(mediaEventProcessor.mediaSessions.isEmpty())
    }

    @Test
    fun `notifyBackendSessionId() calls handleSessionUpdate on all sessions`() {
        mediaEventProcessor.notifyBackendSessionId("123", "backendSessionId")

        assertTrue(mediaSession1.handleSessionUpdateCalled)
        assertEquals("123", mediaSession1.handleSessionUpdateParamRequestEventId)
        assertEquals("backendSessionId", mediaSession1.handleSessionUpdateParamBackendSessionId)

        assertTrue(mediaSession2.handleSessionUpdateCalled)
        assertEquals("123", mediaSession2.handleSessionUpdateParamRequestEventId)
        assertEquals("backendSessionId", mediaSession2.handleSessionUpdateParamBackendSessionId)

        assertEquals(2, mediaEventProcessor.mediaSessions.size)
    }

    @Test
    fun `notifyBackendSessionId() removes inactive sessions`() {
        mediaSession1.isSessionActive = false
        mediaEventProcessor.notifyBackendSessionId("123", "backendSessionId")

        assertEquals(1, mediaEventProcessor.mediaSessions.size)
        assertFalse(mediaEventProcessor.mediaSessions.containsValue(mediaSession1))
        assertTrue(mediaEventProcessor.mediaSessions.containsValue(mediaSession2))
    }

    @Test
    fun `notifyErrorResponse() calls handleErrorResponse for all sessions`() {
        val errorHandle = mapOf(
            "status" to 400L,
            "type" to "https://ns.adobe.com/aep/errors/va-edge-0400-400"
        )
        mediaEventProcessor.notifyErrorResponse("123", errorHandle)

        assertTrue(mediaSession1.handleErrorResponseCalled)
        assertEquals("123", mediaSession1.handleErrorResponseParamRequestEventId)
        assertEquals(errorHandle, mediaSession1.handleErrorResponseParamData)

        assertTrue(mediaSession2.handleErrorResponseCalled)
        assertEquals("123", mediaSession2.handleErrorResponseParamRequestEventId)
        assertEquals(errorHandle, mediaSession2.handleErrorResponseParamData)

        assertEquals(2, mediaEventProcessor.mediaSessions.size)
    }

    @Test
    fun `notifyErrorResponse() removes inactive sessions`() {
        mediaSession1.isSessionActive = false
        mediaEventProcessor.notifyErrorResponse(
            "123",
            mapOf(
                "status" to 400L,
                "type" to "https://ns.adobe.com/aep/errors/va-edge-0400-400"
            )
        )

        assertEquals(1, mediaEventProcessor.mediaSessions.size)
        assertFalse(mediaEventProcessor.mediaSessions.containsValue(mediaSession1))
        assertTrue(mediaEventProcessor.mediaSessions.containsValue(mediaSession2))
    }

    @Test
    fun `updateMediaState() calls handleErrorResponse for all sessions`() {
        val stateUpdate = mapOf(
            "edgemedia.channel" to "testChannel",
            "edgemedia.playerName" to "testPlayerName",
            "edgemedia.appVersion" to "testAppVersion"
        )
        mediaEventProcessor.updateMediaState(stateUpdate)

        assertTrue(mediaSession1.handleMediaStateUpdateCalled)
        assertTrue(mediaSession2.handleMediaStateUpdateCalled)
        assertEquals(2, mediaEventProcessor.mediaSessions.size)
    }

    @Test
    fun `updateMediaState() updates MediaState`() {
        val stateUpdate = mapOf(
            "edgemedia.channel" to "testChannel",
            "edgemedia.playerName" to "testPlayerName",
            "edgemedia.appVersion" to "testAppVersion"
        )
        mediaEventProcessor.updateMediaState(stateUpdate)
        verify(mockState, times(1)).updateState(eq(stateUpdate))
    }
}
