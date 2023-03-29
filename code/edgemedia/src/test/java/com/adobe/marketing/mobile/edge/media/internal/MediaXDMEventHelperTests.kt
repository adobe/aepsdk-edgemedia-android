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
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMAdvertisingDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMAdvertisingPodDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMChapterDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMCustomMetadata
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMErrorDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMPlayerStateData
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMQoeDataDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMSessionDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMStreamType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaXDMEventHelperTests {
    private val mediaStandardMetadata = mutableMapOf(
        MediaConstants.VideoMetadataKeys.AD_LOAD to "adLoad",
        MediaConstants.VideoMetadataKeys.ASSET_ID to "assetID",
        MediaConstants.VideoMetadataKeys.AUTHORIZED to "authorized",
        MediaConstants.VideoMetadataKeys.DAY_PART to "dayPart",
        MediaConstants.VideoMetadataKeys.EPISODE to "episode",
        MediaConstants.VideoMetadataKeys.FEED to "feed",
        MediaConstants.VideoMetadataKeys.FIRST_AIR_DATE to "firstAirDate",
        MediaConstants.VideoMetadataKeys.FIRST_DIGITAL_DATE to "firstDigitalDate",
        MediaConstants.VideoMetadataKeys.GENRE to "genre",
        MediaConstants.VideoMetadataKeys.MVPD to "mvpd",
        MediaConstants.VideoMetadataKeys.NETWORK to "network",
        MediaConstants.VideoMetadataKeys.ORIGINATOR to "originator",
        MediaConstants.VideoMetadataKeys.RATING to "rating",
        MediaConstants.VideoMetadataKeys.SEASON to "season",
        MediaConstants.VideoMetadataKeys.SHOW to "show",
        MediaConstants.VideoMetadataKeys.SHOW_TYPE to "showType",
        MediaConstants.VideoMetadataKeys.STREAM_FORMAT to "streamFormat",

        MediaConstants.AudioMetadataKeys.ALBUM to "album",
        MediaConstants.AudioMetadataKeys.ARTIST to "artist",
        MediaConstants.AudioMetadataKeys.AUTHOR to "author",
        MediaConstants.AudioMetadataKeys.LABEL to "label",
        MediaConstants.AudioMetadataKeys.PUBLISHER to "publisher",
        MediaConstants.AudioMetadataKeys.STATION to "station"
    )
    private val adStandardMetadata = mutableMapOf(
        MediaConstants.AdMetadataKeys.ADVERTISER to "advertiser",
        MediaConstants.AdMetadataKeys.CAMPAIGN_ID to "campaignID",
        MediaConstants.AdMetadataKeys.CREATIVE_ID to "creativeID",
        MediaConstants.AdMetadataKeys.CREATIVE_URL to "creativeURL",
        MediaConstants.AdMetadataKeys.PLACEMENT_ID to "placementID",
        MediaConstants.AdMetadataKeys.SITE_ID to "siteID"
    )

    private val mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 10.0)
    private val adBreakInfo = AdBreakInfo.create("name", 1, 2.3)
    private val adInfo = AdInfo.create("id", "name", 1, 10.0)
    private val chapterInfo = ChapterInfo.create("name", 1, 2.3, 10.0)
    private val qoeInfo = QoEInfo.create(1.1, 2.2, 3.3, 4.4)
    private val muteStateInfo = StateInfo.create(MediaConstants.PlayerState.MUTE)
    private val testStateInfo = StateInfo.create("testStateName")

    val metadata = mutableMapOf("key2" to "value2", "key1" to "value1")

    @Test
    fun generateSessionDetails() {
        val expectedSessionDetails = XDMSessionDetails()
        expectedSessionDetails.name = "id"
        expectedSessionDetails.friendlyName = "name"
        expectedSessionDetails.length = 10L
        expectedSessionDetails.streamType = XDMStreamType.VIDEO
        expectedSessionDetails.contentType = "vod"
        expectedSessionDetails.hasResume = false

        expectedSessionDetails.adLoad = "adLoad"
        expectedSessionDetails.assetID = "assetID"
        expectedSessionDetails.authorized = "authorized"
        expectedSessionDetails.dayPart = "dayPart"
        expectedSessionDetails.episode = "episode"
        expectedSessionDetails.feed = "feed"
        expectedSessionDetails.firstAirDate = "firstAirDate"
        expectedSessionDetails.firstDigitalDate = "firstDigitalDate"
        expectedSessionDetails.genre = "genre"
        expectedSessionDetails.mvpd = "mvpd"
        expectedSessionDetails.network = "network"
        expectedSessionDetails.originator = "originator"
        expectedSessionDetails.rating = "rating"
        expectedSessionDetails.season = "season"
        expectedSessionDetails.show = "show"
        expectedSessionDetails.showType = "showType"
        expectedSessionDetails.streamFormat = "streamFormat"

        expectedSessionDetails.album = "album"
        expectedSessionDetails.artist = "artist"
        expectedSessionDetails.author = "author"
        expectedSessionDetails.label = "label"
        expectedSessionDetails.publisher = "publisher"
        expectedSessionDetails.station = "station"

        // add standard metadata
        metadata.putAll(mediaStandardMetadata)

        val sessionDetails = MediaXDMEventHelper.generateSessionDetails(mediaInfo, metadata)
        assertEquals(expectedSessionDetails, sessionDetails)
    }

    @Test
    fun testGenerateMediaCustomMetadataDetails() {
        val expectedMetadata = listOf(
            XDMCustomMetadata("key1", "value1"),
            XDMCustomMetadata("key2", "value2")
        )
        // add standard metadata
        metadata.putAll(mediaStandardMetadata)

        val customMediaMetadata = MediaXDMEventHelper.generateMediaCustomMetadata(metadata)

        assertEquals(expectedMetadata, customMediaMetadata)
    }

    @Test
    fun testGenerateAdvertisingPodDetails() {
        val expectedAdvertisingPodDetails = XDMAdvertisingPodDetails("name", 1, 2)
        val advertisingPodDetails = MediaXDMEventHelper.generateAdvertisingPodDetails(adBreakInfo)

        assertEquals(expectedAdvertisingPodDetails, advertisingPodDetails)
    }

    @Test
    fun testGenerateAdvertisingPodDetails_withNullAdBreakInfo_returnsNull() {
        val advertisingPodDetails = MediaXDMEventHelper.generateAdvertisingPodDetails(null)

        assertNull(advertisingPodDetails)
    }

    @Test
    fun testGenerateAdvertisingDetails() {
        val expectedAdvertisingDetails = XDMAdvertisingDetails()
        expectedAdvertisingDetails.name = "id"
        expectedAdvertisingDetails.friendlyName = "name"
        expectedAdvertisingDetails.length = 10
        expectedAdvertisingDetails.podPosition = 1
        expectedAdvertisingDetails.advertiser = "advertiser"
        expectedAdvertisingDetails.campaignID = "campaignID"
        expectedAdvertisingDetails.creativeID = "creativeID"
        expectedAdvertisingDetails.creativeURL = "creativeURL"
        expectedAdvertisingDetails.placementID = "placementID"
        expectedAdvertisingDetails.siteID = "siteID"

        metadata.putAll(adStandardMetadata)
        val advertisingDetails = MediaXDMEventHelper.generateAdvertisingDetails(adInfo, metadata)

        assertEquals(expectedAdvertisingDetails, advertisingDetails)
    }

    @Test
    fun testGenerateAdvertisingDetails_withNullAdInfo_returnsNull() {
        val advertisingDetails = MediaXDMEventHelper.generateAdvertisingDetails(null, metadata)

        assertNull(advertisingDetails)
    }

    @Test
    fun testGenerateAdCustomMetadataDetails() {
        val expectedMetadata = listOf(
            XDMCustomMetadata("key1", "value1"),
            XDMCustomMetadata("key2", "value2")
        )
        // add standard metadata
        metadata.putAll(adStandardMetadata)

        val customAdMetadata = MediaXDMEventHelper.generateAdCustomMetadata(metadata)

        assertEquals(expectedMetadata, customAdMetadata)
    }

    @Test
    fun testGenerateChapterDetails() {
        val expectedChapterDetails = XDMChapterDetails("name", 1, 10, 2)
        val chapterDetails = MediaXDMEventHelper.generateChapterDetails(chapterInfo)

        assertEquals(expectedChapterDetails, chapterDetails)
    }

    @Test
    fun testGenerateChapterDetails_withNullChapterInfo_returnsNull() {
        val chapterDetails = MediaXDMEventHelper.generateChapterDetails(null)

        assertNull(chapterDetails)
    }

    @Test
    fun testGenerateChapterMetadataDetails() {
        val expectedMetadata = listOf(
            XDMCustomMetadata("key1", "value1"),
            XDMCustomMetadata("key2", "value2")
        )

        val customMediaMetadata = MediaXDMEventHelper.generateChapterMetadata(metadata)
        assertEquals(expectedMetadata, customMediaMetadata)
    }

    @Test
    fun testGenerateQoEDetails() {
        val expectedQoEDetails = XDMQoeDataDetails(1, 2, 3, 4)
        val qoeDetails = MediaXDMEventHelper.generateQoEDataDetails(qoeInfo)

        assertEquals(expectedQoEDetails, qoeDetails)
    }

    @Test
    fun testGenerateQoEDetails_withNullQoEInfo_returnsNull() {
        val qoeDetails = MediaXDMEventHelper.generateQoEDataDetails(null)

        assertNull(qoeDetails)
    }

    @Test
    fun testGenerateErrorDetails() {
        val expectedErrorDetails = XDMErrorDetails("testError", "player")
        val errorDetails = MediaXDMEventHelper.generateErrorDetails("testError")

        assertEquals(expectedErrorDetails, errorDetails)
    }

    @Test
    fun testGenerateStateDetails() {
        val expectedTestStateDetails = XDMPlayerStateData("testStateName")
        val expectedMuteStateDetails = XDMPlayerStateData("mute")
        val expectedStateDetailsList = listOf(expectedTestStateDetails, expectedMuteStateDetails)

        val stateDetails = MediaXDMEventHelper.generateStateDetails(listOf(testStateInfo, muteStateInfo))

        assertEquals(expectedStateDetailsList, stateDetails)
    }

    @Test
    fun testGenerateStateDetails_withNullPlayerStateInfoList_returnsNull() {
        val stateDetails = MediaXDMEventHelper.generateStateDetails(null)
        assertNull(stateDetails)
    }
}
