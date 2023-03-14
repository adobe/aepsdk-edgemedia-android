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
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMAdvertisingDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaCollection
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEvent
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEventType
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaSchema
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMSessionDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MediaRealTimeSessionTests {
    private val id = "testSessionId"
    private var mockState: MediaState = Mockito.mock(MediaState::class.java)
    private val dispatcher: (Event) -> Unit = { }

    @Test
    fun `mediaBackendSessionId set with non-null non-empty value updates id`() {
        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.mediaBackendSessionId = "validId"
        assertEquals("validId", session.mediaBackendSessionId)
    }

    @Test
    fun `mediaBackendSessionId set with null value does not update id`() {
        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.mediaBackendSessionId = null
        assertNull(session.mediaBackendSessionId)
    }

    @Test
    fun `mediaBackendSessionId set with empty value does not update id`() {
        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.mediaBackendSessionId = ""
        assertNull(session.mediaBackendSessionId)
    }

    @Test
    fun `handleSessionUpdate() sets backend session id and processes media events`() {
        val requestEventId = "edgeRequestId"
        val sessionId = "backendSessionId"

        // MediaState needs to be valid to process event queue
        `when`(mockState.isValid).thenReturn(true)

        // Set event type to media.sessionStart to allow event processing
        val event = getXDMMediaEvent(XDMMediaEventType.SESSION_START)

        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.sessionStartEdgeRequestId = requestEventId // current request ID must match passed ID
        session.events.add(event)

        session.handleSessionUpdate(requestEventId, sessionId)

        assertEquals(sessionId, session.mediaBackendSessionId)
        assertTrue(session.isSessionActive) // verify session is still active
        assertTrue(session.events.isEmpty())
    }

    @Test
    fun `handleSessionUpdate() sets backend session id while aborts if backend session id is null`() {
        val requestEventId = "edgeRequestId"

        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.sessionStartEdgeRequestId = requestEventId // current request ID must match passed ID
        session.events.add(XDMMediaEvent())
        session.mediaBackendSessionId = "sessionId" // set valid backend session ID

        session.handleSessionUpdate(requestEventId, "") // pass invalid session ID

        assertNull(session.mediaBackendSessionId)
        assertFalse(session.isSessionActive) // verify session is inactive from abort() call
        assertTrue(session.events.isEmpty()) // event queue is cleared from abort() call
    }

    @Test
    fun `handleSessionUpdate() performs no operation if edge request id does not match`() {
        val requestEventId = "edgeRequestId"
        val sessionId = "backendSessionId"

        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.sessionStartEdgeRequestId = "otherEdgeRequestID"
        session.events.add(XDMMediaEvent())
        session.mediaBackendSessionId = sessionId // set valid backend session ID

        session.handleSessionUpdate(requestEventId, "otherSessionId")

        assertEquals(sessionId, session.mediaBackendSessionId) // verify backend session id unchanged
        assertTrue(session.isSessionActive) // verify session is still active
        assertFalse(session.events.isEmpty()) // event queue not processed
    }

    @Test
    fun `handleErrorResponse() with VA Edge 400 error aborts session`() {
        val requestId = "edgeRequestId"
        val data = mapOf<String, Any>("status" to 400L, "type" to "https://ns.adobe.com/aep/errors/va-edge-0400-400")

        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.events.add(getXDMMediaEvent(XDMMediaEventType.SESSION_START))
        session.sessionStartEdgeRequestId = requestId // current request id must match to process request

        val latch = CountDownLatch(1)
        session.sessionEndHandler = { latch.countDown() }

        session.handleErrorResponse(requestId, data)

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertTrue(session.events.isEmpty())
        assertFalse(session.isSessionActive)
    }

    @Test
    fun `handleErrorResponse() with VA Edge 400 error and extra fields aborts session`() {
        val requestId = "edgeRequestId"
        val data = mapOf(
            "status" to 400L,
            "type" to "https://ns.adobe.com/aep/errors/va-edge-0400-400",
            "title" to "Not Found",
            "detail" to "The requested resource could not be found by may be available again in the future.",
            "report" to mapOf("details" to "Error processing request.")
        )

        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.events.add(getXDMMediaEvent(XDMMediaEventType.SESSION_START))
        session.sessionStartEdgeRequestId = requestId // current request id must match to process request

        val latch = CountDownLatch(1)
        session.sessionEndHandler = { latch.countDown() }

        session.handleErrorResponse(requestId, data)

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertTrue(session.events.isEmpty())
        assertFalse(session.isSessionActive)
    }

    @Test
    fun `handleErrorResponse() with error code not 400 does not abort session`() {
        val requestId = "edgeRequestId"
        val data = mapOf<String, Any>("status" to 200L, "type" to "https://ns.adobe.com/aep/errors/va-edge-0400-400")

        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.events.add(getXDMMediaEvent(XDMMediaEventType.SESSION_START))
        session.sessionStartEdgeRequestId = requestId // current request id must match to process request

        session.sessionEndHandler = { fail("Session end handler should not be called!") }

        session.handleErrorResponse(requestId, data)

        // Add delay to ensure session end handler is not called
        runBlocking {
            delay(TimeUnit.SECONDS.toMillis(1))
        }

        assertFalse(session.events.isEmpty())
        assertTrue(session.isSessionActive)
    }

    @Test
    fun `handleErrorResponse() with error type not VA Edge 400 does not abort session`() {
        val requestId = "edgeRequestId"
        val data = mapOf<String, Any>("status" to 400L, "type" to "https://ns.adobe.com/aep/errors/va-edge-0200-200")

        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.events.add(getXDMMediaEvent(XDMMediaEventType.SESSION_START))
        session.sessionStartEdgeRequestId = requestId // current request id must match to process request

        session.sessionEndHandler = { fail("Session end handler should not be called!") }

        session.handleErrorResponse(requestId, data)

        // Add delay to ensure session end handler is not called
        runBlocking {
            delay(TimeUnit.SECONDS.toMillis(1))
        }

        assertFalse(session.events.isEmpty())
        assertTrue(session.isSessionActive)
    }

    @Test
    fun `handleErrorResponse() with incorrect error data does not abort session`() {
        val requestId = "edgeRequestId"
        // Data should contain "type" and "status" fields
        val data = mapOf<String, Any>("hello" to 400L, "world" to "https://ns.adobe.com/aep/errors/va-edge-0200-200")

        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.events.add(getXDMMediaEvent(XDMMediaEventType.SESSION_START))
        session.sessionStartEdgeRequestId = requestId // current request id must match to process request

        session.sessionEndHandler = { fail("Session end handler should not be called!") }

        session.handleErrorResponse(requestId, data)

        // Add delay to ensure session end handler is not called
        runBlocking {
            delay(TimeUnit.SECONDS.toMillis(1))
        }

        assertFalse(session.events.isEmpty())
        assertTrue(session.isSessionActive)
    }

    @Test
    fun `handleErrorResponse() when passed wrong request event ID does not abort session`() {
        val requestId = "edgeRequestId"
        val data = mapOf<String, Any>("status" to 400L, "type" to "https://ns.adobe.com/aep/errors/va-edge-0400-400")

        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.events.add(getXDMMediaEvent(XDMMediaEventType.SESSION_START))
        session.sessionStartEdgeRequestId = requestId // current request id must match to process request

        session.sessionEndHandler = { fail("Session end handler should not be called!") }

        session.handleErrorResponse("otherRequestId", data)

        // Add delay to ensure session end handler is not called
        runBlocking {
            delay(TimeUnit.SECONDS.toMillis(1))
        }

        assertFalse(session.events.isEmpty())
        assertTrue(session.isSessionActive)
    }

    @Test
    fun `handleMediaStateUpdate() processes event queue`() {
        // MediaState needs to be valid to process event queue
        `when`(mockState.isValid).thenReturn(true)

        // Set event type to media.sessionStart to allow event processing
        val event = getXDMMediaEvent(XDMMediaEventType.SESSION_START)

        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.events.add(event)

        // Verify events queue is empty after calling handleMediaStateUpdate
        session.handleMediaStateUpdate()
        assertTrue(session.events.isEmpty())
    }

    @Test
    fun `end() processes event queue and calls session end handler when queue is empty`() {
        `when`(mockState.isValid).thenReturn(true)
        // Set event type to media.sessionStart to allow event processing
        val event = getXDMMediaEvent(XDMMediaEventType.SESSION_START)

        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.events.add(event)

        val latch = CountDownLatch(1)

        session.end { latch.countDown() }

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertTrue(session.events.isEmpty())
    }

    @Test
    fun `end() processes event queue but does not call session end handler when queue is not empty`() {
        `when`(mockState.isValid).thenReturn(false) // stop event processing
        // Set event type to media.sessionStart to allow event processing
        val event = getXDMMediaEvent(XDMMediaEventType.SESSION_START)

        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.events.add(event)

        session.end { fail("Session end handler should not be called!") }

        // Add delay to ensure session end handler is not called
        runBlocking {
            delay(TimeUnit.SECONDS.toMillis(1))
        }
        assertFalse(session.events.isEmpty())
    }

    @Test
    fun `abort() clears queue and calls session end handler`() {
        val session = MediaRealTimeSession(id, mockState, dispatcher)
        session.events.add(getXDMMediaEvent(XDMMediaEventType.SESSION_START))

        val latch = CountDownLatch(1)

        session.abort { latch.countDown() }

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertTrue(session.events.isEmpty())
    }

    @Test
    fun `queue() adds event to queue`() {
        // Set invalid MediaState to prevent processing event queue
        `when`(mockState.isValid).thenReturn(false)
        // Set event type to media.sessionStart to allow event processing
        val event1 = getXDMMediaEvent(XDMMediaEventType.SESSION_START)
        val event2 = getXDMMediaEvent(XDMMediaEventType.SESSION_END)

        val session = MediaRealTimeSession(id, mockState, dispatcher)

        session.queue(event1)
        session.queue(event2)

        // MediaState was set to invalid so event will still be in queue
        assertEquals(2, session.events.size)
        assertEquals(event1, session.events[0])
        assertEquals(event2, session.events[1])
    }

    @Test
    fun `queue() processes event and dispatches experience event`() {
        // MediaState needs to be valid to process event queue
        `when`(mockState.isValid).thenReturn(true)

        // Set event type to media.sessionStart to allow event processing
        val event = getXDMMediaEvent(XDMMediaEventType.SESSION_START)

        val latch = CountDownLatch(1)

        var dispatchedEvent: Event? = null
        val session = MediaRealTimeSession(id, mockState) {
            dispatchedEvent = it
            latch.countDown()
        }

        session.queue(event)

        assertTrue("Timeout waiting for dispatcher.", latch.await(2, TimeUnit.SECONDS))
        assertNotNull(dispatchedEvent)

        dispatchedEvent?.let { dispatched ->
            assertEquals("Edge Media - media.sessionStart", dispatched.name)
            assertEquals(EventType.EDGE, dispatched.type)
            assertEquals(EventSource.REQUEST_CONTENT, dispatched.source)
            assertNotNull(dispatched.eventData)
        }
    }

    @Test
    fun `queue() does not process non-sessionStart events if backend session id is null`() {
        // MediaState needs to be valid to process event queue
        `when`(mockState.isValid).thenReturn(true)

        // Set event type to media.sessionStart to allow event processing
        val event = getXDMMediaEvent(XDMMediaEventType.PLAY)

        val session = MediaRealTimeSession(id, mockState) {
            fail("Session end handler should not be called!")
        }

        session.mediaBackendSessionId = null

        session.queue(event)

        // Add delay to ensure dispatcher is not called
        runBlocking {
            delay(TimeUnit.SECONDS.toMillis(1))
        }
        assertFalse(session.events.isEmpty())
    }

    @Test
    fun `queue() processes session start event and attaches media state info`() {
        // MediaState needs to be valid to process event queue
        `when`(mockState.isValid).thenReturn(true)
        `when`(mockState.mediaPlayerName).thenReturn("testPlayer")
        `when`(mockState.mediaChannel).thenReturn("testChannel")
        `when`(mockState.mediaAppVersion).thenReturn("testVersion")

        // Set event type to media.sessionStart to allow event processing
        val event = getXDMMediaEvent(XDMMediaEventType.SESSION_START)

        val latch = CountDownLatch(1)

        var dispatchedEvent: Event? = null
        val session = MediaRealTimeSession(id, mockState) {
            dispatchedEvent = it
            latch.countDown()
        }

        session.queue(event)

        assertTrue("Timeout waiting for dispatcher.", latch.await(2, TimeUnit.SECONDS))
        assertNotNull(dispatchedEvent)

        dispatchedEvent?.let { dispatched ->
            val flatMap = flatten(dispatched.eventData)
            assertEquals("media.sessionStart", flatMap["xdm.eventType"])
            assertEquals("testPlayer", flatMap["xdm.mediaCollection.sessionDetails.playerName"])
            assertEquals("testChannel", flatMap["xdm.mediaCollection.sessionDetails.channel"])
            assertEquals("testVersion", flatMap["xdm.mediaCollection.sessionDetails.appVersion"])
        }
    }

    @Test
    fun `queue() processes session start event but does not overwrite session channel value`() {
        // MediaState needs to be valid to process event queue
        `when`(mockState.isValid).thenReturn(true)
        `when`(mockState.mediaPlayerName).thenReturn("testPlayer")
        `when`(mockState.mediaChannel).thenReturn("testChannel")
        `when`(mockState.mediaAppVersion).thenReturn("testVersion")

        // Set event type to media.sessionStart to allow event processing
        val event = getXDMMediaEvent(XDMMediaEventType.SESSION_START)
        event.xdmData?.mediaCollection?.sessionDetails?.channel = "myChannel"

        val latch = CountDownLatch(1)

        var dispatchedEvent: Event? = null
        val session = MediaRealTimeSession(id, mockState) {
            dispatchedEvent = it
            latch.countDown()
        }

        session.queue(event)

        assertTrue("Timeout waiting for dispatcher.", latch.await(2, TimeUnit.SECONDS))
        assertNotNull(dispatchedEvent)

        dispatchedEvent?.let { dispatched ->
            val flatMap = flatten(dispatched.eventData)
            assertEquals("testPlayer", flatMap["xdm.mediaCollection.sessionDetails.playerName"])
            assertEquals("myChannel", flatMap["xdm.mediaCollection.sessionDetails.channel"]) // channel is same from event
            assertEquals("testVersion", flatMap["xdm.mediaCollection.sessionDetails.appVersion"])
        }
    }

    @Test
    fun `queue() processes ad start event and attaches media state info and session id`() {
        // MediaState needs to be valid to process event queue
        `when`(mockState.isValid).thenReturn(true)
        `when`(mockState.mediaPlayerName).thenReturn("testPlayer")

        val event = getXDMMediaEvent(XDMMediaEventType.AD_START)

        val latch = CountDownLatch(1)

        var dispatchedEvent: Event? = null
        val session = MediaRealTimeSession(id, mockState) {
            dispatchedEvent = it
            latch.countDown()
        }
        session.mediaBackendSessionId = "sessionId"

        session.queue(event)

        assertTrue("Timeout waiting for dispatcher.", latch.await(2, TimeUnit.SECONDS))
        assertNotNull(dispatchedEvent)

        dispatchedEvent?.let { dispatched ->
            val flatMap = flatten(dispatched.eventData)
            assertEquals("media.adStart", flatMap["xdm.eventType"])
            assertEquals("testPlayer", flatMap["xdm.mediaCollection.advertisingDetails.playerName"])
            assertEquals("sessionId", flatMap["xdm.mediaCollection.sessionID"])
        }
    }

    @Test
    fun `queue() processes session complete event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.SESSION_COMPLETE)
    }

    @Test
    fun `queue() processes session end event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.SESSION_END)
    }

    @Test
    fun `queue() processes play event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.PLAY)
    }

    @Test
    fun `queue() processes pause start event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.PAUSE_START)
    }

    @Test
    fun `queue() processes ping event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.PING)
    }

    @Test
    fun `queue() processes error event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.ERROR)
    }

    @Test
    fun `queue() processes buffer start event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.BUFFER_START)
    }

    @Test
    fun `queue() processes bitrate change event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.BITRATE_CHANGE)
    }

    @Test
    fun `queue() processes ad break start event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.AD_BREAK_START)
    }

    @Test
    fun `queue() processes ad break complete event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.AD_BREAK_COMPLETE)
    }

    @Test
    fun `queue() processes ad skip event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.AD_SKIP)
    }

    @Test
    fun `queue() processes ad complete event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.AD_COMPLETE)
    }

    @Test
    fun `queue() processes chapter skip event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.CHAPTER_SKIP)
    }

    @Test
    fun `queue() processes chapter start event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.CHAPTER_START)
    }

    @Test
    fun `queue() processes chapter complete event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.CHAPTER_COMPLETE)
    }

    @Test
    fun `queue() processes states update event and attaches session id`() {
        assertQueueAddsSessionId(XDMMediaEventType.STATES_UPDATE)
    }

    private fun assertQueueAddsSessionId(forType: XDMMediaEventType) {
        // MediaState needs to be valid to process event queue
        `when`(mockState.isValid).thenReturn(true)

        val event = getXDMMediaEvent(forType)

        val latch = CountDownLatch(1)

        var dispatchedEvent: Event? = null
        val session = MediaRealTimeSession(id, mockState) {
            dispatchedEvent = it
            latch.countDown()
        }
        session.mediaBackendSessionId = "sessionId"

        session.queue(event)

        assertTrue("[${forType.value}] Timeout waiting for dispatcher.", latch.await(2, TimeUnit.SECONDS))
        assertNotNull("[${forType.value}] Did not dispatch event!", dispatchedEvent)

        dispatchedEvent?.let { dispatched ->
            val flatMap = flatten(dispatched.eventData)
            assertEquals(
                "[${forType.value}] event name does not match.",
                "Edge Media - ${XDMMediaEventType.getTypeString(forType)}",
                dispatched.name
            )
            assertEquals(
                "[${forType.value}] event type does not match.",
                XDMMediaEventType.getTypeString(forType),
                flatMap["xdm.eventType"]
            )
            assertEquals(
                "[${forType.value}] backend session ID does not match.",
                "sessionId",
                flatMap["xdm.mediaCollection.sessionID"]
            )
            assertEquals(
                "[${forType.value}] overwrite path does not match.",
                "/va/v1/${forType.value}",
                flatMap["request.path"]
            )
        }
    }

    private fun getXDMMediaEvent(forType: XDMMediaEventType): XDMMediaEvent {
        val schema = XDMMediaSchema()
        schema.eventType = forType
        schema.timestamp = Date()
        schema.mediaCollection = XDMMediaCollection()

        when (forType) {
            XDMMediaEventType.SESSION_START -> {
                val sessionDetails = XDMSessionDetails()
                sessionDetails.playerName = "myPlayer"
                sessionDetails.name = "mySession"
                schema.mediaCollection?.sessionDetails = sessionDetails
            }
            XDMMediaEventType.AD_START -> {
                val advertisingDetails = XDMAdvertisingDetails()
                schema.mediaCollection?.advertisingDetails = advertisingDetails
            }
            else -> {
                // nothing more needed
            }
        }

        val event = XDMMediaEvent()
        event.xdmData = schema
        return event
    }

    private fun flatten(map: Map<String, Any>, prefix: String = ""): Map<String, Any> {
        val keyPrefix = if (prefix.isEmpty()) prefix else "$prefix."
        val flattened = mutableMapOf<String, Any>()
        map.forEach { (key, value) ->
            val expandedKey = "$keyPrefix$key"
            if (value is Map<*, *>) {
                flattened += flatten(value as Map<String, Any>, expandedKey)
            } else {
                flattened[expandedKey] = value
            }
        }
        return flattened
    }
}
