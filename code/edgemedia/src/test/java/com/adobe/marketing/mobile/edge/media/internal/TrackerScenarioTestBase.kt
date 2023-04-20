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
import org.junit.Assert
import org.junit.Before

open class TrackerScenarioTestBase {
    // List of Experience Events dispatched from MediaEventProcessor
    internal var dispatchedEvents: MutableList<Event> = mutableListOf()

    // MediaEventProcessor dispatcher which captures Experience Events
    private val dispatcher: (Event) -> Unit = { event -> dispatchedEvents.add(event) }

    // Current timestamp of MediaTracker
    private var currentTimestampMillis: Long = 0L

    // Current playhead of MediaTracker
    private var currentPlayhead: Double = 0.0

    internal lateinit var mediaState: MediaState
    internal lateinit var mediaEventProcessor: MediaEventProcessor
    internal lateinit var mediaTracker: MediaPublicTracker

    @Before
    open fun setup() {
        dispatchedEvents.clear()
        mediaState = MediaState()
        mediaEventProcessor = MediaEventProcessor(mediaState, dispatcher)

        createTracker()
    }

    fun createTracker(trackerConfig: Map<String, Any>? = null) {
        val mediaEventTracker = MediaEventTracker(mediaEventProcessor, trackerConfig)
        mediaTracker = MediaPublicTracker("Scenario Test Tracker") { event ->
            mediaEventTracker.track(event)
        }

        currentTimestampMillis = 0L
        mediaTracker.currentTimestamp =
            MediaPublicTracker.TimestampSupplier { currentTimestampMillis }
    }

    /**
     * Increment the timestamp and playhead of the [MediaPublicTracker] under test.
     * @param seconds number of seconds to increment timestamp and playhead
     * @param updatePlayhead if true, the playhead is also incremented by `seconds`
     */
    fun incrementTrackerTime(seconds: Int, updatePlayhead: Boolean) {
        for (i in 1..seconds) {
            incrementTrackerTimestamp(1)
            incrementTrackerPlayhead(1, updatePlayhead)
        }
    }

    fun incrementTrackerTimestamp(seconds: Int) {
        currentTimestampMillis += seconds * 1000
    }

    fun incrementTrackerPlayhead(seconds: Int, updatePlayhead: Boolean = true) {
        if (updatePlayhead) {
            currentPlayhead += seconds
        }
        mediaTracker.updateCurrentPlayhead(currentPlayhead)
    }

    /**
     * Assert two lists of [Event]s are equal by comparing the Event name, type, source, and data.
     */
    fun assertEqualEvents(expectedEvents: List<Event>, actualEvents: List<Event>) {
        Assert.assertEquals(
            "Number of dispatched events must be equal.",
            expectedEvents.size,
            actualEvents.size
        )

        for (i in 1 until expectedEvents.size) {
            val expected = expectedEvents[i]
            val actual = actualEvents[i]

            Assert.assertEquals("Event name must match.", expected.name, actual.name)
            Assert.assertEquals("Event type must match.", expected.type, actual.type)
            Assert.assertEquals("Event source must match.", expected.source, actual.source)
            Assert.assertEquals("Event data must match.", expected.eventData, actual.eventData)
        }
    }
}
