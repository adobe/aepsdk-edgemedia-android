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
import com.adobe.marketing.mobile.services.HttpMethod.POST
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.util.MockNetworkService
import com.adobe.marketing.mobile.util.MonitorExtension
import com.adobe.marketing.mobile.util.TestHelper
import com.adobe.marketing.mobile.util.TestHelper.LogOnErrorRule
import com.adobe.marketing.mobile.util.TestHelper.SetupCoreRule
import com.adobe.marketing.mobile.util.TestHelper.assertExpectedEvents
import com.adobe.marketing.mobile.util.TestHelper.setExpectationEvent
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class MediaEdgeLocationHintIntegrationTests {
    private val mockNetworkService = MockNetworkService()
    companion object {
        private const val EDGE_CONFIG_ID = "1234abcd-abcd-1234-5678-123456abcdef"
        private const val SUCCESS_RESPONSE_STRING =
            "\u0000{\"handle\":[{\"payload\":[{\"sessionId\":\"99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d\"}],\"type\":\"media-analytics:new-session\",\"eventIndex\":0}]}"
        private val mediaInfo =
            Media.createMediaObject("testName", "testId", 30, "VOD", Media.MediaType.Audio)
        private val metadata = mutableMapOf("testKey" to "testValue")
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
        assertExpectedEvents(false)
        resetTestExpectations()
    }

    @After
    fun tearDown() {
        resetTestExpectations()
    }

    @Test
    fun testMediaEdgeRequests_whenLocationHintSet_urlPathContainsLocationHint() {
        val testLocationHint = "or2"
        Edge.setLocationHint(testLocationHint)

        val sessionStartUrlWithLocationHint = "https://edge.adobedc.net/ee/$testLocationHint/va/v1/sessionStart"

        val responseConnection = mockNetworkService.createMockNetworkResponse(SUCCESS_RESPONSE_STRING, 200)
        mockNetworkService.setMockResponseFor(sessionStartUrlWithLocationHint, POST, responseConnection)

        // test
        val tracker = Media.createTracker()
        tracker.trackSessionStart(mediaInfo, metadata)
        tracker.trackPlay()
        tracker.updateCurrentPlayhead(7)
        tracker.trackPause()
        tracker.trackComplete()

        // verify
        val networkRequests = mockNetworkService.getAllNetworkRequests()
        Assert.assertEquals(4, networkRequests.size)
        Assert.assertTrue(networkRequests[0].url.contains("https://edge.adobedc.net/ee/$testLocationHint/va/v1/sessionStart"))
        Assert.assertTrue(networkRequests[1].url.contains("https://edge.adobedc.net/ee/$testLocationHint/va/v1/play"))
        Assert.assertTrue(networkRequests[2].url.contains("https://edge.adobedc.net/ee/$testLocationHint/va/v1/pauseStart"))
        Assert.assertTrue(networkRequests[3].url.contains("https://edge.adobedc.net/ee/$testLocationHint/va/v1/sessionComplete"))
    }

    @Test
    fun testMediaEdgeRequests_noLocationHintSet_urlPathDoesNotContainLocationHint() {
        Edge.setLocationHint(null)

        val sessionStartUrlWithoutLocationHint = "https://edge.adobedc.net/ee/va/v1/sessionStart"

        val responseConnection = mockNetworkService.createMockNetworkResponse(SUCCESS_RESPONSE_STRING, 200)
        mockNetworkService.setMockResponseFor(sessionStartUrlWithoutLocationHint, POST, responseConnection)

        // test
        val tracker = Media.createTracker()
        tracker.trackSessionStart(mediaInfo, metadata)
        tracker.trackPlay()
        tracker.updateCurrentPlayhead(7)
        tracker.trackPause()
        tracker.trackComplete()

        // verify
        val networkRequests = mockNetworkService.getAllNetworkRequests()
        Assert.assertEquals(4, networkRequests.size)
        Assert.assertTrue(networkRequests[0].url.contains("https://edge.adobedc.net/ee/va/v1/sessionStart"))
        Assert.assertTrue(networkRequests[1].url.contains("https://edge.adobedc.net/ee/va/v1/play"))
        Assert.assertTrue(networkRequests[2].url.contains("https://edge.adobedc.net/ee/va/v1/pauseStart"))
        Assert.assertTrue(networkRequests[3].url.contains("https://edge.adobedc.net/ee/va/v1/sessionComplete"))
    }

    private fun resetTestExpectations() {
        mockNetworkService.reset()
        TestHelper.resetTestExpectations()
    }
}
