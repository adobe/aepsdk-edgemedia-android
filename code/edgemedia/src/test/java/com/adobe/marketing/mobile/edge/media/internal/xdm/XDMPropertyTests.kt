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

package com.adobe.marketing.mobile.edge.media.internal.xdm

import com.adobe.marketing.mobile.util.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import kotlin.reflect.full.memberProperties

class XDMPropertyTests {

    @Test
    fun `XDMAdvertisingDetails serializeToXDM all properties`() {
        val advertisingDetails = XDMAdvertisingDetails()
        advertisingDetails.name = "id"
        advertisingDetails.friendlyName = "name"
        advertisingDetails.length = 10
        advertisingDetails.podPosition = 1
        advertisingDetails.playerName = "testPlayer"
        advertisingDetails.advertiser = "testAdvertiser"
        advertisingDetails.campaignID = "testCampaignId"
        advertisingDetails.creativeID = "testCreativeId"
        advertisingDetails.creativeURL = "testCreativeUrl"
        advertisingDetails.placementID = "testPlacementId"
        advertisingDetails.siteID = "testSiteId"

        val xdm = advertisingDetails.serializeToXDM()
        val expected = mapOf<String, Any>(
            "name" to "id",
            "friendlyName" to "name",
            "length" to 10L,
            "podPosition" to 1L,
            "playerName" to "testPlayer",
            "advertiser" to "testAdvertiser",
            "campaignID" to "testCampaignId",
            "creativeID" to "testCreativeId",
            "creativeURL" to "testCreativeUrl",
            "placementID" to "testPlacementId",
            "siteID" to "testSiteId"
        )

        assertEquals(expected, xdm)
        // Assert testing all class member properties
        assertEquals(expected.size, XDMAdvertisingDetails::class.memberProperties.size)
    }

    @Test
    fun `XDMAdvertisingDetails serializeToXDM no properties returns empty map`() {
        val advertisingDetails = XDMAdvertisingDetails()
        val xdm = advertisingDetails.serializeToXDM()

        assertNotNull(xdm)
        assertTrue(xdm.isEmpty())
    }

    @Test
    fun `XDMAdvertisingPodDetails serializeToXDM all properties`() {
        val advertisingPodDetails = XDMAdvertisingPodDetails()
        advertisingPodDetails.friendlyName = "name"
        advertisingPodDetails.index = 1
        advertisingPodDetails.offset = 2

        val xdm = advertisingPodDetails.serializeToXDM()
        val expected = mapOf<String, Any>(
            "friendlyName" to "name",
            "index" to 1L,
            "offset" to 2L
        )

        assertEquals(expected, xdm)
        // Assert testing all class member properties
        assertEquals(expected.size, XDMAdvertisingPodDetails::class.memberProperties.size)
    }

    @Test
    fun `XDMAdvertisingPodDetails serializeToXDM no properties returns empty map`() {
        val advertisingPodDetails = XDMAdvertisingPodDetails()
        val xdm = advertisingPodDetails.serializeToXDM()

        assertNotNull(xdm)
        assertTrue(xdm.isEmpty())
    }

    @Test
    fun `XDMChapterDetails serializeToXDM all properties`() {
        val chapterDetails = XDMChapterDetails()
        chapterDetails.friendlyName = "test friendly name"
        chapterDetails.index = 1
        chapterDetails.length = 10
        chapterDetails.offset = 2

        val xdm = chapterDetails.serializeToXDM()
        val expected = mapOf<String, Any>(
            "friendlyName" to "test friendly name",
            "index" to 1L,
            "length" to 10L,
            "offset" to 2L
        )

        assertEquals(expected, xdm)
        // Assert testing all class member properties
        assertEquals(expected.size, XDMChapterDetails::class.memberProperties.size)
    }

    @Test
    fun `XDMChapterDetails serializeToXDM no properties returns empty map`() {
        val chapterDetails = XDMChapterDetails()
        val xdm = chapterDetails.serializeToXDM()

        assertNotNull(xdm)
        assertTrue(xdm.isEmpty())
    }

    @Test
    fun `XDMCustomMetadata serializeToXDM all properties`() {
        val customMetadata = XDMCustomMetadata()
        customMetadata.name = "testName"
        customMetadata.value = "testValue"

        val xdm = customMetadata.serializeToXDM()
        val expected = mapOf<String, Any>(
            "name" to "testName",
            "value" to "testValue"
        )

        assertEquals(expected, xdm)
        // Assert testing all class member properties
        assertEquals(expected.size, XDMCustomMetadata::class.memberProperties.size)
    }

    @Test
    fun `XDMCustomMetadata serializeToXDM no properties returns empty map`() {
        val customMetadata = XDMCustomMetadata()
        val xdm = customMetadata.serializeToXDM()

        assertNotNull(xdm)
        assertTrue(xdm.isEmpty())
    }

    @Test
    fun `XDMErrorDetails serializeToXDM all properties`() {
        val errorDetails = XDMErrorDetails()
        errorDetails.name = "testName"
        errorDetails.source = "testSource"

        val xdm = errorDetails.serializeToXDM()
        val expected = mapOf<String, Any>(
            "name" to "testName",
            "source" to "testSource"
        )

        assertEquals(expected, xdm)
        // Assert testing all class member properties
        assertEquals(expected.size, XDMErrorDetails::class.memberProperties.size)
    }

    @Test
    fun `XDMErrorDetails serializeToXDM no properties returns empty map`() {
        val errorDetails = XDMErrorDetails()
        val xdm = errorDetails.serializeToXDM()

        assertNotNull(xdm)
        assertTrue(xdm.isEmpty())
    }

    @Test
    fun `XDMPlayerStateData serializeToXDM all properties`() {
        val playerStateData = XDMPlayerStateData()
        playerStateData.name = "testName"

        val xdm = playerStateData.serializeToXDM()
        val expected = mapOf<String, Any>(
            "name" to "testName"
        )

        assertEquals(expected, xdm)
        // Assert testing all class member properties
        assertEquals(expected.size, XDMPlayerStateData::class.memberProperties.size)
    }

    @Test
    fun `XDMPlayerStateData serializeToXDM no properties returns empty map`() {
        val playerStateData = XDMPlayerStateData()
        val xdm = playerStateData.serializeToXDM()

        assertNotNull(xdm)
        assertTrue(xdm.isEmpty())
    }

    @Test
    fun `XDMQoeDataDetails serializeToXDM all properties`() {
        val qoeDataDetails = XDMQoeDataDetails()
        qoeDataDetails.bitrate = 128
        qoeDataDetails.droppedFrames = 10
        qoeDataDetails.framesPerSecond = 59
        qoeDataDetails.timeToStart = 5

        val xdm = qoeDataDetails.serializeToXDM()
        val expected = mapOf<String, Any>(
            "bitrate" to 128L,
            "droppedFrames" to 10L,
            "framesPerSecond" to 59L,
            "timeToStart" to 5L
        )

        assertEquals(expected, xdm)
        // Assert testing all class member properties
        assertEquals(expected.size, XDMQoeDataDetails::class.memberProperties.size)
    }

    @Test
    fun `XDMQoeDataDetails serializeToXDM no properties returns empty map`() {
        val qoeDataDetails = XDMQoeDataDetails()
        val xdm = qoeDataDetails.serializeToXDM()

        assertNotNull(xdm)
        assertTrue(xdm.isEmpty())
    }

    @Test
    fun `XDMSessionDetails serializeToXDM all properties`() {
        val sessionDetails = XDMSessionDetails()
        sessionDetails.contentType = "vod"
        sessionDetails.name = "id"
        sessionDetails.friendlyName = "name"
        sessionDetails.hasResume = false
        sessionDetails.length = 30
        sessionDetails.streamType = XDMStreamType.VIDEO

        sessionDetails.channel = "test_channel"
        sessionDetails.playerName = "test_playerName"
        sessionDetails.appVersion = "test_appVersion"

        // Audio Standard Metadata
        sessionDetails.album = "test_album"
        sessionDetails.artist = "test_artist"
        sessionDetails.author = "test_author"
        sessionDetails.label = "test_label"
        sessionDetails.publisher = "test_publisher"
        sessionDetails.station = "test_station"

        // Video Standard Metadata
        sessionDetails.adLoad = "preroll"
        sessionDetails.authorized = "false"
        sessionDetails.assetID = "test_assetID"
        sessionDetails.dayPart = "evening"
        sessionDetails.episode = "1"
        sessionDetails.feed = "test_feed"
        sessionDetails.firstAirDate = "test_firstAirDate"
        sessionDetails.firstDigitalDate = "test_firstAirDigitalDate"
        sessionDetails.genre = "test_genre"
        sessionDetails.mvpd = "test_mvpd"
        sessionDetails.network = "test_network"
        sessionDetails.originator = "test_originator"
        sessionDetails.rating = "test_rating"
        sessionDetails.season = "1"
        sessionDetails.segment = "test_segment"
        sessionDetails.show = "test_show"
        sessionDetails.showType = "test_showType"
        sessionDetails.streamFormat = "test_streamFormat"

        val xdm = sessionDetails.serializeToXDM()
        val expected = mapOf<String, Any>(
            "contentType" to "vod",
            "name" to "id",
            "friendlyName" to "name",
            "hasResume" to false,
            "length" to 30L,
            "streamType" to XDMStreamType.VIDEO.value,
            "channel" to "test_channel",
            "playerName" to "test_playerName",
            "appVersion" to "test_appVersion",
            "album" to "test_album",
            "artist" to "test_artist",
            "author" to "test_author",
            "label" to "test_label",
            "publisher" to "test_publisher",
            "station" to "test_station",
            "adLoad" to "preroll",
            "authorized" to "false",
            "assetID" to "test_assetID",
            "dayPart" to "evening",
            "episode" to "1",
            "feed" to "test_feed",
            "firstAirDate" to "test_firstAirDate",
            "firstDigitalDate" to "test_firstAirDigitalDate",
            "genre" to "test_genre",
            "mvpd" to "test_mvpd",
            "network" to "test_network",
            "originator" to "test_originator",
            "rating" to "test_rating",
            "season" to "1",
            "segment" to "test_segment",
            "show" to "test_show",
            "showType" to "test_showType",
            "streamFormat" to "test_streamFormat"
        )

        assertEquals(expected, xdm)
        // Assert testing all class member properties
        assertEquals(expected.size, XDMSessionDetails::class.memberProperties.size)
    }

    @Test
    fun `XDMSessionDetails serializeToXDM video properties`() {
        val sessionDetails = XDMSessionDetails()
        sessionDetails.contentType = "vod"
        sessionDetails.name = "id"
        sessionDetails.friendlyName = "name"
        sessionDetails.hasResume = false
        sessionDetails.length = 30
        sessionDetails.streamType = XDMStreamType.VIDEO

        sessionDetails.channel = "test_channel"
        sessionDetails.playerName = "test_playerName"
        sessionDetails.appVersion = "test_appVersion"

        // Video Standard Metadata
        sessionDetails.adLoad = "preroll"
        sessionDetails.authorized = "false"
        sessionDetails.assetID = "test_assetID"
        sessionDetails.dayPart = "evening"
        sessionDetails.episode = "1"
        sessionDetails.feed = "test_feed"
        sessionDetails.firstAirDate = "test_firstAirDate"
        sessionDetails.firstDigitalDate = "test_firstAirDigitalDate"
        sessionDetails.genre = "test_genre"
        sessionDetails.mvpd = "test_mvpd"
        sessionDetails.network = "test_network"
        sessionDetails.originator = "test_originator"
        sessionDetails.rating = "test_rating"
        sessionDetails.season = "1"
        sessionDetails.segment = "test_segment"
        sessionDetails.show = "test_show"
        sessionDetails.showType = "test_showType"
        sessionDetails.streamFormat = "test_streamFormat"

        val xdm = sessionDetails.serializeToXDM()
        val expected = mapOf<String, Any>(
            "contentType" to "vod",
            "name" to "id",
            "friendlyName" to "name",
            "hasResume" to false,
            "length" to 30L,
            "streamType" to XDMStreamType.VIDEO.value,
            "channel" to "test_channel",
            "playerName" to "test_playerName",
            "appVersion" to "test_appVersion",
            "adLoad" to "preroll",
            "authorized" to "false",
            "assetID" to "test_assetID",
            "dayPart" to "evening",
            "episode" to "1",
            "feed" to "test_feed",
            "firstAirDate" to "test_firstAirDate",
            "firstDigitalDate" to "test_firstAirDigitalDate",
            "genre" to "test_genre",
            "mvpd" to "test_mvpd",
            "network" to "test_network",
            "originator" to "test_originator",
            "rating" to "test_rating",
            "season" to "1",
            "segment" to "test_segment",
            "show" to "test_show",
            "showType" to "test_showType",
            "streamFormat" to "test_streamFormat"
        )

        assertEquals(expected, xdm)
    }

    @Test
    fun `XDMSessionDetails serializeToXDM audio properties`() {
        val sessionDetails = XDMSessionDetails()
        sessionDetails.contentType = "aod"
        sessionDetails.name = "id"
        sessionDetails.friendlyName = "name"
        sessionDetails.hasResume = false
        sessionDetails.length = 30
        sessionDetails.streamType = XDMStreamType.AUDIO

        sessionDetails.channel = "test_channel"
        sessionDetails.playerName = "test_playerName"
        sessionDetails.appVersion = "test_appVersion"

        // Audio Standard Metadata
        sessionDetails.album = "test_album"
        sessionDetails.artist = "test_artist"
        sessionDetails.author = "test_author"
        sessionDetails.label = "test_label"
        sessionDetails.publisher = "test_publisher"
        sessionDetails.station = "test_station"

        val xdm = sessionDetails.serializeToXDM()
        val expected = mapOf<String, Any>(
            "contentType" to "aod",
            "name" to "id",
            "friendlyName" to "name",
            "hasResume" to false,
            "length" to 30L,
            "streamType" to XDMStreamType.AUDIO.value,
            "channel" to "test_channel",
            "playerName" to "test_playerName",
            "appVersion" to "test_appVersion",
            "album" to "test_album",
            "artist" to "test_artist",
            "author" to "test_author",
            "label" to "test_label",
            "publisher" to "test_publisher",
            "station" to "test_station"
        )

        assertEquals(expected, xdm)
    }

    @Test
    fun `XDMSessionDetails serializeToXDM no properties returns empty map`() {
        val sessionDetails = XDMSessionDetails()
        val xdm = sessionDetails.serializeToXDM()

        assertNotNull(xdm)
        assertTrue(xdm.isEmpty())
    }

    @Test
    fun `XDMMediaCollection serializeToXDM all properties`() {
        val (advertisingDetails, expectedAdvertisingDetails) = getSampleAdvertisingDetails()
        val (advertisingPodDetails, expectedAdvertisingPodDetails) = getSampleAdvertisingPodDetails()
        val (chapterDetails, expectedChapterDetails) = getSampleChapterDetails()
        val (customMetadata, expectedCustomMetadata) = getSampleCustomMetadata()
        val (errorDetails, expectedErrorDetails) = getSampleErrorDetails()
        val (qoeDataDetails, expectedQoeDataDetails) = getSampleQoeDataDetails()
        val (sessionDetails, expectedSessionDetails) = getSampleSessionDetails()
        val (playerStateData, expectedPlayerStateData) = getSamplePlayerStateData()

        val mediaCollection = XDMMediaCollection()
        mediaCollection.advertisingDetails = advertisingDetails
        mediaCollection.advertisingPodDetails = advertisingPodDetails
        mediaCollection.chapterDetails = chapterDetails
        mediaCollection.customMetadata = customMetadata
        mediaCollection.errorDetails = errorDetails
        mediaCollection.playhead = 123
        mediaCollection.qoeDataDetails = qoeDataDetails
        mediaCollection.sessionDetails = sessionDetails
        mediaCollection.sessionID = "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"
        mediaCollection.statesStart = playerStateData
        mediaCollection.statesEnd = playerStateData

        val xdm = mediaCollection.serializeToXDM()
        val expected = mapOf(
            "advertisingDetails" to expectedAdvertisingDetails,
            "advertisingPodDetails" to expectedAdvertisingPodDetails,
            "chapterDetails" to expectedChapterDetails,
            "customMetadata" to expectedCustomMetadata,
            "errorDetails" to expectedErrorDetails,
            "playhead" to 123L,
            "qoeDataDetails" to expectedQoeDataDetails,
            "sessionDetails" to expectedSessionDetails,
            "sessionID" to "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d",
            "statesStart" to expectedPlayerStateData,
            "statesEnd" to expectedPlayerStateData
        )

        assertEquals(expected, xdm)
        // Assert testing all class member properties
        assertEquals(expected.size, XDMMediaCollection::class.memberProperties.size)
    }

    @Test
    fun `XDMMediaCollection serializeToXDM SessionStart properties`() {
        val (sessionDetails, expectedSessionDetails) = getSampleSessionDetails()

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = 0
        mediaCollection.sessionDetails = sessionDetails
        mediaCollection.sessionID = "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"

        val xdm = mediaCollection.serializeToXDM()
        val expected = mapOf(
            "playhead" to 0L,
            "sessionDetails" to expectedSessionDetails,
            "sessionID" to "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"
        )

        assertEquals(expected, xdm)
    }

    @Test
    fun `XDMMediaCollection serializeToXDM AdBreakStart properties`() {
        val (advertisingPodDetails, expectedAdvertisingPodDetails) = getSampleAdvertisingPodDetails()

        val mediaCollection = XDMMediaCollection()
        mediaCollection.advertisingPodDetails = advertisingPodDetails
        mediaCollection.playhead = 0
        mediaCollection.sessionID = "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"

        val xdm = mediaCollection.serializeToXDM()
        val expected = mapOf(
            "advertisingPodDetails" to expectedAdvertisingPodDetails,
            "playhead" to 0L,
            "sessionID" to "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"
        )

        assertEquals(expected, xdm)
    }

    @Test
    fun `XDMMediaCollection serializeToXDM AdStart properties`() {
        val (advertisingDetails, expectedAdvertisingDetails) = getSampleAdvertisingDetails()

        val mediaCollection = XDMMediaCollection()
        mediaCollection.advertisingDetails = advertisingDetails
        mediaCollection.playhead = 123
        mediaCollection.sessionID = "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"

        val xdm = mediaCollection.serializeToXDM()
        val expected = mapOf(
            "advertisingDetails" to expectedAdvertisingDetails,
            "playhead" to 123L,
            "sessionID" to "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"
        )

        assertEquals(expected, xdm)
    }

    @Test
    fun `XDMMediaCollection serializeToXDM ChapterStart properties`() {
        val (chapterDetails, expectedChapterDetails) = getSampleChapterDetails()

        val mediaCollection = XDMMediaCollection()
        mediaCollection.chapterDetails = chapterDetails
        mediaCollection.playhead = 123
        mediaCollection.sessionID = "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"

        val xdm = mediaCollection.serializeToXDM()
        val expected = mapOf(
            "chapterDetails" to expectedChapterDetails,
            "playhead" to 123L,
            "sessionID" to "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"
        )

        assertEquals(expected, xdm)
    }

    @Test
    fun `XDMMediaCollection serializeToXDM StateStart properties`() {
        val (playerStateData, expectedPlayerStateData) = getSamplePlayerStateData()

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = 123
        mediaCollection.sessionID = "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"
        mediaCollection.statesStart = playerStateData

        val xdm = mediaCollection.serializeToXDM()
        val expected = mapOf(
            "playhead" to 123L,
            "sessionID" to "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d",
            "statesStart" to expectedPlayerStateData
        )

        assertEquals(expected, xdm)
    }

    @Test
    fun `XDMMediaCollection serializeToXDM StateEnd properties`() {
        val (playerStateData, expectedPlayerStateData) = getSamplePlayerStateData()

        val mediaCollection = XDMMediaCollection()
        mediaCollection.playhead = 123
        mediaCollection.sessionID = "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"
        mediaCollection.statesEnd = playerStateData

        val xdm = mediaCollection.serializeToXDM()
        val expected = mapOf(
            "playhead" to 123L,
            "sessionID" to "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d",
            "statesEnd" to expectedPlayerStateData
        )

        assertEquals(expected, xdm)
    }

    @Test
    fun `XDMMediaCollection serializeToXDM no properties returns empty map`() {
        val mediaCollection = XDMMediaCollection()
        val xdm = mediaCollection.serializeToXDM()

        assertNotNull(xdm)
        assertTrue(xdm.isEmpty())
    }

    @Test
    fun `XDMMediaSchema serializeToXDM all properties`() {
        val dateNow = Date()
        val mediaCollection = XDMMediaCollection()
        mediaCollection.sessionID = "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"
        val schema = XDMMediaSchema(XDMMediaEventType.PLAY, dateNow, mediaCollection)

        val xdm = schema.serializeToXDM()
        val expected = mapOf(
            "eventType" to XDMMediaEventType.getTypeString(XDMMediaEventType.PLAY),
            "timestamp" to TimeUtils.getISO8601UTCDateWithMilliseconds(dateNow),
            "mediaCollection" to mapOf("sessionID" to "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d")
        )

        assertEquals(expected, xdm)
        // Assert testing all class member properties
        assertEquals(expected.size, XDMMediaSchema::class.memberProperties.size)
    }

    @Test
    fun `XDMMediaEvent serializeToXDM all properties`() {
        val dateNow = Date()
        val mediaCollection = XDMMediaCollection()
        mediaCollection.sessionID = "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d"
        val schema = XDMMediaSchema(XDMMediaEventType.PLAY, dateNow, mediaCollection)

        val mediaEvent = XDMMediaEvent(schema)
        val xdm = mediaEvent.serializeToXDM()
        val expected = mapOf(
            "xdm" to mapOf(
                "eventType" to XDMMediaEventType.getTypeString(XDMMediaEventType.PLAY),
                "timestamp" to TimeUtils.getISO8601UTCDateWithMilliseconds(dateNow),
                "mediaCollection" to mapOf("sessionID" to "99cf4e3e7145d8e2b8f4f1e9e1a08cd52518a74091c0b0c611ca97b259e03a4d")
            ),
            "request" to mapOf("path" to "/va/v1/${XDMMediaEventType.PLAY.value}")
        )

        assertEquals(expected, xdm)
        // Assert testing all class member properties
        // XDMMediaEvent "request.path" is not a class property but is generated during serializeToXDM
        assertEquals(expected.size - 1, XDMMediaEvent::class.memberProperties.size)
    }

    private fun getSampleAdvertisingDetails(): Pair<XDMAdvertisingDetails, Map<String, Any>> {
        val advertisingDetails = XDMAdvertisingDetails()
        advertisingDetails.name = "id"
        advertisingDetails.friendlyName = "name"
        advertisingDetails.length = 10
        advertisingDetails.podPosition = 1
        advertisingDetails.playerName = "testPlayer"
        advertisingDetails.advertiser = "testAdvertiser"
        advertisingDetails.campaignID = "testCampaignId"
        advertisingDetails.creativeID = "testCreativeId"
        advertisingDetails.creativeURL = "testCreativeUrl"
        advertisingDetails.placementID = "testPlacementId"
        advertisingDetails.siteID = "testSiteId"

        val expected = mapOf<String, Any>(
            "name" to "id",
            "friendlyName" to "name",
            "length" to 10L,
            "podPosition" to 1L,
            "playerName" to "testPlayer",
            "advertiser" to "testAdvertiser",
            "campaignID" to "testCampaignId",
            "creativeID" to "testCreativeId",
            "creativeURL" to "testCreativeUrl",
            "placementID" to "testPlacementId",
            "siteID" to "testSiteId"
        )

        return advertisingDetails to expected
    }

    private fun getSampleAdvertisingPodDetails(): Pair<XDMAdvertisingPodDetails, Map<String, Any>> {
        val advertisingPodDetails = XDMAdvertisingPodDetails()
        advertisingPodDetails.friendlyName = "name"
        advertisingPodDetails.index = 1
        advertisingPodDetails.offset = 2

        val expected = mapOf<String, Any>(
            "friendlyName" to "name",
            "index" to 1L,
            "offset" to 2L
        )

        return advertisingPodDetails to expected
    }

    private fun getSampleChapterDetails(): Pair<XDMChapterDetails, Map<String, Any>> {
        val chapterDetails = XDMChapterDetails()
        chapterDetails.friendlyName = "test friendly name"
        chapterDetails.index = 1
        chapterDetails.length = 10
        chapterDetails.offset = 2

        val expected = mapOf<String, Any>(
            "friendlyName" to "test friendly name",
            "index" to 1L,
            "length" to 10L,
            "offset" to 2L
        )

        return chapterDetails to expected
    }

    private fun getSampleCustomMetadata(): Pair<List<XDMCustomMetadata>, List<Map<String, Any>>> {
        val customMetadata1 = XDMCustomMetadata()
        customMetadata1.name = "customOne"
        customMetadata1.value = "valueOne"

        val customMetadata2 = XDMCustomMetadata()
        customMetadata2.name = "customTwo"
        customMetadata2.value = "valueTwo"

        val expected = listOf(
            mapOf<String, Any>(
                "name" to "customOne",
                "value" to "valueOne"
            ),
            mapOf<String, Any>(
                "name" to "customTwo",
                "value" to "valueTwo"
            )
        )

        return listOf(customMetadata1, customMetadata2) to expected
    }

    private fun getSampleErrorDetails(): Pair<XDMErrorDetails, Map<String, Any>> {
        val errorDetails = XDMErrorDetails()
        errorDetails.name = "testName"
        errorDetails.source = "testSource"

        val expected = mapOf<String, Any>(
            "name" to "testName",
            "source" to "testSource"
        )

        return errorDetails to expected
    }

    private fun getSampleQoeDataDetails(): Pair<XDMQoeDataDetails, Map<String, Any>> {
        val qoeDataDetails = XDMQoeDataDetails()
        qoeDataDetails.bitrate = 128
        qoeDataDetails.droppedFrames = 10
        qoeDataDetails.framesPerSecond = 59
        qoeDataDetails.timeToStart = 5

        val expected = mapOf<String, Any>(
            "bitrate" to 128L,
            "droppedFrames" to 10L,
            "framesPerSecond" to 59L,
            "timeToStart" to 5L
        )

        return qoeDataDetails to expected
    }

    private fun getSampleSessionDetails(): Pair<XDMSessionDetails, Map<String, Any>> {
        val sessionDetails = XDMSessionDetails()
        sessionDetails.contentType = "aod"
        sessionDetails.name = "id"
        sessionDetails.friendlyName = "name"
        sessionDetails.hasResume = false
        sessionDetails.length = 30
        sessionDetails.streamType = XDMStreamType.AUDIO

        sessionDetails.channel = "test_channel"
        sessionDetails.playerName = "test_playerName"
        sessionDetails.appVersion = "test_appVersion"

        // Audio Standard Metadata
        sessionDetails.album = "test_album"
        sessionDetails.artist = "test_artist"
        sessionDetails.author = "test_author"
        sessionDetails.label = "test_label"
        sessionDetails.publisher = "test_publisher"
        sessionDetails.station = "test_station"

        val expected = mapOf<String, Any>(
            "contentType" to "aod",
            "name" to "id",
            "friendlyName" to "name",
            "hasResume" to false,
            "length" to 30L,
            "streamType" to XDMStreamType.AUDIO.value,
            "channel" to "test_channel",
            "playerName" to "test_playerName",
            "appVersion" to "test_appVersion",
            "album" to "test_album",
            "artist" to "test_artist",
            "author" to "test_author",
            "label" to "test_label",
            "publisher" to "test_publisher",
            "station" to "test_station"
        )

        return sessionDetails to expected
    }

    private fun getSamplePlayerStateData(): Pair<List<XDMPlayerStateData>, List<Map<String, Any>>> {
        val muteState = XDMPlayerStateData()
        muteState.name = "mute"

        val fullscreenState = XDMPlayerStateData()
        fullscreenState.name = "fullscreen"

        val expected = listOf(
            mapOf<String, Any>(
                "name" to "mute"
            ),
            mapOf<String, Any>(
                "name" to "fullscreen"
            )
        )

        return listOf(muteState, fullscreenState) to expected
    }
}
