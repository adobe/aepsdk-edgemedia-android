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
import com.adobe.marketing.mobile.edge.media.internal.MediaInternalConstants.LOG_TAG
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMAdvertisingDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMAdvertisingPodDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMChapterDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMCustomMetadata
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMErrorDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMPlayerStateData
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMQoeDataDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMSessionDetails
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMStreamType
import com.adobe.marketing.mobile.services.Log

internal class MediaXDMEventHelper {
    companion object {
        private val sourceTag = "MediaXDMEventHelper"

        private val standardMediaMetadataSet: Set<String> = setOf(
            MediaConstants.VideoMetadataKeys.AD_LOAD,
            MediaConstants.VideoMetadataKeys.ASSET_ID,
            MediaConstants.VideoMetadataKeys.AUTHORIZED,
            MediaConstants.VideoMetadataKeys.DAY_PART,
            MediaConstants.VideoMetadataKeys.EPISODE,
            MediaConstants.VideoMetadataKeys.FEED,
            MediaConstants.VideoMetadataKeys.FIRST_AIR_DATE,
            MediaConstants.VideoMetadataKeys.FIRST_DIGITAL_DATE,
            MediaConstants.VideoMetadataKeys.GENRE,
            MediaConstants.VideoMetadataKeys.MVPD,
            MediaConstants.VideoMetadataKeys.NETWORK,
            MediaConstants.VideoMetadataKeys.ORIGINATOR,
            MediaConstants.VideoMetadataKeys.RATING,
            MediaConstants.VideoMetadataKeys.SEASON,
            MediaConstants.VideoMetadataKeys.SHOW,
            MediaConstants.VideoMetadataKeys.SHOW_TYPE,
            MediaConstants.VideoMetadataKeys.STREAM_FORMAT,
            MediaConstants.AudioMetadataKeys.ALBUM,
            MediaConstants.AudioMetadataKeys.ARTIST,
            MediaConstants.AudioMetadataKeys.AUTHOR,
            MediaConstants.AudioMetadataKeys.LABEL,
            MediaConstants.AudioMetadataKeys.PUBLISHER,
            MediaConstants.AudioMetadataKeys.STATION
        )

        private val standardAdMetadataSet: Set<String> = setOf(
            MediaConstants.AdMetadataKeys.ADVERTISER,
            MediaConstants.AdMetadataKeys.CAMPAIGN_ID,
            MediaConstants.AdMetadataKeys.CREATIVE_ID,
            MediaConstants.AdMetadataKeys.CREATIVE_URL,
            MediaConstants.AdMetadataKeys.PLACEMENT_ID,
            MediaConstants.AdMetadataKeys.SITE_ID
        )

        @JvmStatic
        fun generateSessionDetails(mediaInfo: MediaInfo, metadata: Map<String, String>, forceResume: Boolean = false): XDMSessionDetails {
            val sessionDetails = XDMSessionDetails()
            sessionDetails.name = mediaInfo.id
            sessionDetails.friendlyName = mediaInfo.name
            sessionDetails.length = mediaInfo.length.toLong()
            sessionDetails.streamType = if (mediaInfo.mediaType == MediaType.Audio) XDMStreamType.AUDIO else XDMStreamType.VIDEO
            sessionDetails.contentType = mediaInfo.streamType
            sessionDetails.hasResume = forceResume || mediaInfo.isResumed // To also handle the internally triggered resume by the SDK for long running sessions >= 24 hours

            metadata.forEach { (key, value) ->
                run {
                    if (!standardMediaMetadataSet.contains(key)) {
                        return@forEach
                    }

                    when (key) {
                        // Video standard metadata cases
                        MediaConstants.VideoMetadataKeys.AD_LOAD -> sessionDetails.adLoad = value
                        MediaConstants.VideoMetadataKeys.ASSET_ID -> sessionDetails.assetID = value
                        MediaConstants.VideoMetadataKeys.AUTHORIZED -> sessionDetails.authorized = value
                        MediaConstants.VideoMetadataKeys.DAY_PART -> sessionDetails.dayPart = value
                        MediaConstants.VideoMetadataKeys.EPISODE -> sessionDetails.episode = value
                        MediaConstants.VideoMetadataKeys.FEED -> sessionDetails.feed = value
                        MediaConstants.VideoMetadataKeys.FIRST_AIR_DATE -> sessionDetails.firstAirDate = value
                        MediaConstants.VideoMetadataKeys.FIRST_DIGITAL_DATE -> sessionDetails.firstDigitalDate = value
                        MediaConstants.VideoMetadataKeys.GENRE -> sessionDetails.genre = value
                        MediaConstants.VideoMetadataKeys.MVPD -> sessionDetails.mvpd = value
                        MediaConstants.VideoMetadataKeys.NETWORK -> sessionDetails.network = value
                        MediaConstants.VideoMetadataKeys.ORIGINATOR -> sessionDetails.originator = value
                        MediaConstants.VideoMetadataKeys.RATING -> sessionDetails.rating = value
                        MediaConstants.VideoMetadataKeys.SEASON -> sessionDetails.season = value
                        MediaConstants.VideoMetadataKeys.SHOW -> sessionDetails.show = value
                        MediaConstants.VideoMetadataKeys.SHOW_TYPE -> sessionDetails.showType = value
                        MediaConstants.VideoMetadataKeys.STREAM_FORMAT -> sessionDetails.streamFormat = value

                        // Audio standard metadata cases
                        MediaConstants.AudioMetadataKeys.ALBUM -> sessionDetails.album = value
                        MediaConstants.AudioMetadataKeys.ARTIST -> sessionDetails.artist = value
                        MediaConstants.AudioMetadataKeys.AUTHOR -> sessionDetails.author = value
                        MediaConstants.AudioMetadataKeys.LABEL -> sessionDetails.label = value
                        MediaConstants.AudioMetadataKeys.PUBLISHER -> sessionDetails.publisher = value
                        MediaConstants.AudioMetadataKeys.STATION -> sessionDetails.station = value
                    }
                }
            }

            return sessionDetails
        }

        @JvmStatic
        fun generateMediaCustomMetadata(metadata: Map<String, String>): List<XDMCustomMetadata> {
            val customMetadataList = mutableListOf<XDMCustomMetadata>()

            metadata.forEach { (key, value) ->
                run {
                    if (!standardMediaMetadataSet.contains(key)) {
                        customMetadataList.add(XDMCustomMetadata(key, value))
                    }
                }
            }

            return customMetadataList.sortedBy { it.name }
        }

        @JvmStatic
        fun generateAdvertisingPodDetails(adBreakInfo: AdBreakInfo?): XDMAdvertisingPodDetails? {
            val adBreakInfo = adBreakInfo
            if (adBreakInfo !is AdBreakInfo) {
                Log.trace(LOG_TAG, sourceTag, "found empty ad break info.")
                return null
            }

            return XDMAdvertisingPodDetails(
                adBreakInfo.name,
                adBreakInfo.position,
                adBreakInfo.startTime.toLong()
            )
        }

        @JvmStatic
        fun generateAdvertisingDetails(adInfo: AdInfo?, metadata: Map<String, String>): XDMAdvertisingDetails? {
            val adInfo = adInfo
            if (adInfo !is AdInfo) {
                Log.trace(LOG_TAG, sourceTag, "found empty ad info.")
                return null
            }

            val advertisingDetails = XDMAdvertisingDetails()
            advertisingDetails.name = adInfo.id
            advertisingDetails.friendlyName = adInfo.name
            advertisingDetails.length = adInfo.length.toLong()
            advertisingDetails.podPosition = adInfo.position

            // Append standard metadata to advertisingDetails
            metadata.forEach { (key, value) ->
                run {
                    if (!standardAdMetadataSet.contains(key)) {
                        return@forEach
                    }

                    when (key) {
                        MediaConstants.AdMetadataKeys.ADVERTISER -> advertisingDetails.advertiser = value
                        MediaConstants.AdMetadataKeys.CAMPAIGN_ID -> advertisingDetails.campaignID = value
                        MediaConstants.AdMetadataKeys.CREATIVE_ID -> advertisingDetails.creativeID = value
                        MediaConstants.AdMetadataKeys.CREATIVE_URL -> advertisingDetails.creativeURL = value
                        MediaConstants.AdMetadataKeys.PLACEMENT_ID -> advertisingDetails.placementID = value
                        MediaConstants.AdMetadataKeys.SITE_ID -> advertisingDetails.siteID = value
                    }
                }
            }

            return advertisingDetails
        }

        @JvmStatic
        fun generateAdCustomMetadata(metadata: Map<String, String>): List<XDMCustomMetadata> {
            val customMetadataList = mutableListOf<XDMCustomMetadata>()

            metadata.forEach { (key, value) ->
                run {
                    if (!standardAdMetadataSet.contains(key)) {
                        customMetadataList.add(XDMCustomMetadata(key, value))
                    }
                }
            }

            return customMetadataList.sortedBy { it.name }
        }

        @JvmStatic
        fun generateChapterDetails(chapterInfo: ChapterInfo?): XDMChapterDetails? {
            val chapterInfo = chapterInfo
            if (chapterInfo !is ChapterInfo) {
                Log.trace(LOG_TAG, sourceTag, "found empty chapter info.")
                return null
            }

            return XDMChapterDetails(
                chapterInfo.name,
                chapterInfo.position,
                chapterInfo.length.toLong(),
                chapterInfo.startTime.toLong()
            )
        }

        @JvmStatic
        fun generateChapterMetadata(metadata: Map<String, String>): List<XDMCustomMetadata> {
            val customMetadataList = mutableListOf<XDMCustomMetadata>()

            metadata.forEach { (key, value) ->
                run {
                    customMetadataList.add(XDMCustomMetadata(key, value))
                }
            }

            return customMetadataList.sortedBy { it.name }
        }

        @JvmStatic
        fun generateQoEDataDetails(qoeInfo: QoEInfo?): XDMQoeDataDetails? {
            val qoeInfo = qoeInfo
            if (qoeInfo !is QoEInfo) {
                Log.trace(LOG_TAG, sourceTag, "found empty QoE info.")
                return null
            }

            return XDMQoeDataDetails(
                qoeInfo.bitrate.toLong(),
                qoeInfo.droppedFrames.toLong(),
                qoeInfo.fps.toLong(),
                qoeInfo.startupTime.toLong()
            )
        }

        @JvmStatic
        fun generateErrorDetails(errorID: String): XDMErrorDetails? {
            return XDMErrorDetails(
                errorID,
                MediaInternalConstants.ErrorSource.PLAYER
            )
        }

        @JvmStatic
        fun generateStateDetails(states: List<StateInfo>?): List<XDMPlayerStateData>? {
            val states = states
            if (states !is List<StateInfo> || states.isEmpty()) {
                return null
            }

            val playerStateDetailsList = mutableListOf<XDMPlayerStateData>()
            states.forEach { state ->
                run {
                    playerStateDetailsList.add(XDMPlayerStateData(state.stateName))
                }
            }

            return playerStateDetailsList
        }
    }
}
