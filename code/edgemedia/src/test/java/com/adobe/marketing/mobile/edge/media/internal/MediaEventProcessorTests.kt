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
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaCollection
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEvent
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEventType
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.util.*

class MediaEventProcessorTests {
    private var mockState: MediaState = Mockito.mock(MediaState::class.java)
    private val dispatcher: (Event) -> Unit = { }

    private lateinit var mediaEventProcessor: MediaEventProcessor
    private lateinit var mediaSession1: SpyMediaSession
    private lateinit var mediaSession2: SpyMediaSession

    @Before
    fun setup() {
        mediaEventProcessor = MediaEventProcessor(mockState, dispatcher)
    }

    private fun setTestSessionsToProcessor() {
        mediaSession1 = SpyMediaSession("testSession1", mockState, dispatcher)
        mediaSession2 = SpyMediaSession("testSession2", mockState, dispatcher)

        mediaEventProcessor.mediaSessions["testSession1"] = mediaSession1
        mediaEventProcessor.mediaSessions["testSession2"] = mediaSession2
    }

    @Test
    fun `createSession() creates a new MediaRealTimeSession`() {
        val sessionId = mediaEventProcessor.createSession()

        assertNotNull(sessionId)
        assertEquals(1, mediaEventProcessor.mediaSessions.size)

        val session = mediaEventProcessor.mediaSessions[sessionId]
        assertNotNull(session)
        assertTrue(session is MediaRealTimeSession)

        // validate sessionId is UUID
        UUID.fromString(sessionId) // throws if parsing fails
    }

    @Test
    fun `createSession() create multiple Media Sessions`() {
        val sessionId1 = mediaEventProcessor.createSession()
        val sessionId2 = mediaEventProcessor.createSession()
        val sessionId3 = mediaEventProcessor.createSession()

        assertNotNull(sessionId1)
        assertNotNull(sessionId2)
        assertNotNull(sessionId3)

        assertEquals(3, mediaEventProcessor.mediaSessions.size)

        val session1 = mediaEventProcessor.mediaSessions[sessionId1]
        val session2 = mediaEventProcessor.mediaSessions[sessionId2]
        val session3 = mediaEventProcessor.mediaSessions[sessionId3]

        assertNotEquals(session1, session2)
        assertNotEquals(session1, session3)
        assertNotEquals(session2, session3)
    }

    @Test
    fun `endSession() ends Media Session and removes from sessions map`() {
        setTestSessionsToProcessor()

        mediaEventProcessor.endSession("testSession1")

        // verify session 1 ended
        assertFalse(mediaSession1.isSessionActive)
        assertTrue(mediaSession1.handleSessionEndCalled)

        // verify session 2 did not end
        assertTrue(mediaSession2.isSessionActive)
        assertFalse(mediaSession2.handleSessionEndCalled)

        // verify sessions map updated
        assertEquals(1, mediaEventProcessor.mediaSessions.size)
        assertNull(mediaEventProcessor.mediaSessions["testSession1"])
        assertNotNull(mediaEventProcessor.mediaSessions["testSession2"])
    }

    @Test
    fun `endSession() ends Media Session but does not remove from sessions map if events are in queue`() {
        setTestSessionsToProcessor()

        // SpyMediaSession doesn't remove events from queue even when end() is called
        mediaSession1.events.add(XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PLAY, Date(), XDMMediaCollection())))

        mediaEventProcessor.endSession("testSession1")

        // verify session 1 ended
        assertFalse(mediaSession1.isSessionActive) // media session is not active
        assertTrue(mediaSession1.handleSessionEndCalled)

        // verify session 2 did not end
        assertTrue(mediaSession2.isSessionActive)
        assertFalse(mediaSession2.handleSessionEndCalled)

        // verify sessions map not updated
        assertEquals(2, mediaEventProcessor.mediaSessions.size)
        assertNotNull(mediaEventProcessor.mediaSessions["testSession1"])
        assertNotNull(mediaEventProcessor.mediaSessions["testSession2"])
    }

    @Test
    fun `endSession() does nothing when passed invalid session id`() {
        setTestSessionsToProcessor()

        mediaEventProcessor.endSession("invalidId")

        // verify session 1 did not end
        assertTrue(mediaSession1.isSessionActive)
        assertFalse(mediaSession1.handleSessionEndCalled)

        // verify session 2 did not end
        assertTrue(mediaSession2.isSessionActive)
        assertFalse(mediaSession2.handleSessionEndCalled)

        // verify sessions map updated
        assertEquals(2, mediaEventProcessor.mediaSessions.size)
        assertNotNull(mediaEventProcessor.mediaSessions["testSession1"])
        assertNotNull(mediaEventProcessor.mediaSessions["testSession2"])
    }

    @Test
    fun `processEvent() queues XDMMediaEvent for valid session ID`() {
        setTestSessionsToProcessor()
        val event = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PLAY, Date(), XDMMediaCollection()))

        mediaEventProcessor.processEvent("testSession1", event)

        // verify session 1 queues event
        assertTrue(mediaSession1.handleQueueEventCalled)
        assertEquals(event, mediaSession1.handleQueueEventParamXDMMediaEvent)

        // verify session 2 did not queue event
        assertFalse(mediaSession2.handleQueueEventCalled)
        assertNull(mediaSession2.handleQueueEventParamXDMMediaEvent)
    }

    @Test
    fun `processEvent() does not queue XDMMediaEvent for invalid session ID`() {
        setTestSessionsToProcessor()
        val event = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PLAY, Date(), XDMMediaCollection()))

        mediaEventProcessor.processEvent("invalidSessionId", event)

        // verify session 1 did not queue event
        assertFalse(mediaSession1.handleQueueEventCalled)
        assertNull(mediaSession1.handleQueueEventParamXDMMediaEvent)

        // verify session 2 did not queue event
        assertFalse(mediaSession2.handleQueueEventCalled)
        assertNull(mediaSession2.handleQueueEventParamXDMMediaEvent)
    }

    @Test
    fun `abortAllSessions() calls abort on all sessions`() {
        setTestSessionsToProcessor()
        mediaEventProcessor.abortAllSessions()

        assertTrue(mediaSession1.handleSessionAbortCalled)
        assertTrue(mediaSession2.handleSessionAbortCalled)
        assertTrue(mediaEventProcessor.mediaSessions.isEmpty())
    }

    @Test
    fun `notifyBackendSessionId() calls handleSessionUpdate on all sessions`() {
        setTestSessionsToProcessor()
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
        setTestSessionsToProcessor()
        mediaSession1.isSessionActive = false
        mediaEventProcessor.notifyBackendSessionId("123", "backendSessionId")

        assertEquals(1, mediaEventProcessor.mediaSessions.size)
        assertFalse(mediaEventProcessor.mediaSessions.containsValue(mediaSession1))
        assertTrue(mediaEventProcessor.mediaSessions.containsValue(mediaSession2))
    }

    @Test
    fun `notifyErrorResponse() calls handleErrorResponse for all sessions`() {
        setTestSessionsToProcessor()
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
        setTestSessionsToProcessor()
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
    fun `updateMediaState() calls handleMediaStateUpdate for all sessions`() {
        setTestSessionsToProcessor()
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
        setTestSessionsToProcessor()
        val stateUpdate = mapOf(
            "edgemedia.channel" to "testChannel",
            "edgemedia.playerName" to "testPlayerName",
            "edgemedia.appVersion" to "testAppVersion"
        )
        mediaEventProcessor.updateMediaState(stateUpdate)
        verify(mockState, times(1)).updateState(eq(stateUpdate))
    }
}
