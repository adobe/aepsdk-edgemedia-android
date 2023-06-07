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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.*

class MediaSessionTests {
    private val id = "testSessionId"
    private var mockState: MediaState = mock(MediaState::class.java)
    private val dispatcher: (Event) -> Unit = { }

    @Test
    fun `queue() with active session calls handleQueueEvent`() {
        val session = SpyMediaSession(id, mockState, dispatcher)
        session.isSessionActive = true

        val event = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PLAY, Date(), XDMMediaCollection()))
        session.queue(event)

        assertTrue(session.handleQueueEventCalled)
        assertEquals(event, session.handleQueueEventParamXDMMediaEvent)
    }

    @Test
    fun `queue() without active session does not call handleQueueEvent`() {
        val session = SpyMediaSession(id, mockState, dispatcher)
        session.isSessionActive = false

        val event = XDMMediaEvent(XDMMediaSchema(XDMMediaEventType.PLAY, Date(), XDMMediaCollection()))
        session.queue(event)

        assertFalse(session.handleQueueEventCalled)
        assertNull(session.handleQueueEventParamXDMMediaEvent)
    }

    @Test
    fun `end() with active session calls handleSessionEnd`() {
        val session = SpyMediaSession(id, mockState, dispatcher)
        session.isSessionActive = true

        session.end()

        assertTrue(session.handleSessionEndCalled)
        assertFalse(session.isSessionActive) // end() sets session active to false
    }

    @Test
    fun `end() without active session does not call handleSessionEnd`() {
        val session = SpyMediaSession(id, mockState, dispatcher)
        session.isSessionActive = false

        session.end()

        assertFalse(session.handleSessionEndCalled)
    }

    @Test
    fun `abort() with active session calls handleSessionAbort`() {
        val session = SpyMediaSession(id, mockState, dispatcher)
        session.isSessionActive = true

        session.abort()

        assertTrue(session.handleSessionAbortCalled)
        assertFalse(session.isSessionActive) // abort() sets session active to false
    }

    @Test
    fun `abort() without active session does not call handleSessionAbort`() {
        val session = SpyMediaSession(id, mockState, dispatcher)
        session.isSessionActive = false

        session.end()

        assertFalse(session.handleSessionAbortCalled)
    }
}
