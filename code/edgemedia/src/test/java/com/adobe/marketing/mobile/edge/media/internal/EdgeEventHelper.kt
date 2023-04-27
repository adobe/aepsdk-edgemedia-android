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
import com.adobe.marketing.mobile.edge.media.MediaConstants
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMCustomMetadata
import com.adobe.marketing.mobile.edge.media.internal.xdm.XDMMediaEventType
import com.adobe.marketing.mobile.util.TimeUtils
import java.util.*

internal class EdgeEventHelper {

    companion object {
        fun generateEdgeEvent(
            eventType: XDMMediaEventType,
            playhead: Long,
            timestamp: Long,
            backendSessionId: String? = null,
            info: Map<String, Any>? = null,
            metadata: Map<String, Any>? = null,
            mediaState: MediaState? = null,
            stateStart: Boolean = true
        ): Event {
            val overwritePath = "/va/v1/${eventType.value}"

            val mediaCollection = generateMediaCollection(
                eventType,
                playhead,
                backendSessionId,
                info,
                metadata,
                mediaState,
                stateStart
            )

            val xdmData = mapOf(
                "eventType" to XDMMediaEventType.getTypeString(eventType),
                "timestamp" to TimeUtils.getISO8601UTCDateWithMilliseconds(Date(timestamp * 1000)),
                "mediaCollection" to mediaCollection
            )

            val data = mapOf(
                "xdm" to xdmData,
                "request" to mapOf("path" to overwritePath)
            )

            return Event.Builder(
                "Edge Media - ${XDMMediaEventType.getTypeString(eventType)}",
                "com.adobe.eventType.edge",
                "com.adobe.eventSource.requestContent"
            )
                .setEventData(data)
                .build()
        }

        private fun generateMediaCollection(
            eventType: XDMMediaEventType,
            playhead: Long,
            backendSessionId: String?,
            info: Map<String, Any>?,
            metadata: Map<String, Any>?,
            mediaState: MediaState?,
            stateStart: Boolean
        ): Map<String, Any> {
            val mediaCollection: MutableMap<String, Any> = mutableMapOf()

            when (eventType) {
                XDMMediaEventType.SESSION_START ->
                    mediaCollection["sessionDetails"] = getSessionDetails(info, metadata, mediaState)

                XDMMediaEventType.AD_START ->
                    mediaCollection["advertisingDetails"] = getAdvertisingDetails(info, metadata, mediaState)

                XDMMediaEventType.AD_BREAK_START ->
                    mediaCollection["advertisingPodDetails"] = getAdvertisingPodDetails(info)

                XDMMediaEventType.CHAPTER_START ->
                    mediaCollection["chapterDetails"] = getChapterDetails(info)

                XDMMediaEventType.ERROR -> mediaCollection["errorDetails"] = getErrorDetails(info)

                XDMMediaEventType.STATES_UPDATE -> {
                    val key = if (stateStart) "statesStart" else "statesEnd"
                    mediaCollection[key] = getStatesUpdateList(info)
                }

                XDMMediaEventType.BITRATE_CHANGE -> mediaCollection["qoeDataDetails"] =
                    getQoeDetails(info)

                else -> {
                    // nothing more
                }
            }

            if (eventType != XDMMediaEventType.SESSION_START && backendSessionId != null) {
                mediaCollection["sessionID"] = backendSessionId
            }

            mediaCollection["playhead"] = playhead

            if (metadata != null && metadata.isNotEmpty()) {
                mediaCollection["customMetadata"] = getCustomMetadata(metadata)
            }

            return mediaCollection
        }

        private fun getCustomMetadata(metadata: Map<String, Any>): List<Map<String, Any>> {
            val customMetadata: MutableList<XDMCustomMetadata> = mutableListOf()
            metadata.forEach { (key, value) ->
                customMetadata.add(XDMCustomMetadata(key, value as? String))
            }

            return customMetadata.sortedBy { it.name }.map { it.serializeToXDM() }
        }

        private fun getSessionDetails(
            info: Map<String, Any>?,
            metadata: Map<String, Any>?,
            mediaState: MediaState?
        ): Any {
            val details: MutableMap<String, Any> = mutableMapOf(
                "name" to (info?.get("media.id") ?: ""),
                "friendlyName" to (info?.get("media.name") ?: ""),
                "length" to ((info?.get("media.length") as? Int) ?: -1),
                "streamType" to (info?.get("media.type") ?: ""),
                "contentType" to (info?.get("media.streamtype") ?: ""),
                "hasResume" to (info?.get("media.resumed") ?: "")
            )

            metadata?.forEach { (key, value) ->
                when (key) {
                    // Video standard metadata cases
                    MediaConstants.VideoMetadataKeys.AD_LOAD -> details["adLoad"] = value
                    MediaConstants.VideoMetadataKeys.ASSET_ID -> details["assetID"] = value
                    MediaConstants.VideoMetadataKeys.AUTHORIZED -> details["authorized"] = value
                    MediaConstants.VideoMetadataKeys.DAY_PART -> details["dayPart"] = value
                    MediaConstants.VideoMetadataKeys.EPISODE -> details["episode"] = value
                    MediaConstants.VideoMetadataKeys.FEED -> details["feed"] = value
                    MediaConstants.VideoMetadataKeys.FIRST_AIR_DATE -> details["firstAirDate"] =
                        value
                    MediaConstants.VideoMetadataKeys.FIRST_DIGITAL_DATE -> details["firstDigitalDate"] =
                        value
                    MediaConstants.VideoMetadataKeys.GENRE -> details["genre"] = value
                    MediaConstants.VideoMetadataKeys.MVPD -> details["mvpd"] = value
                    MediaConstants.VideoMetadataKeys.NETWORK -> details["network"] = value
                    MediaConstants.VideoMetadataKeys.ORIGINATOR -> details["originator"] = value
                    MediaConstants.VideoMetadataKeys.RATING -> details["rating"] = value
                    MediaConstants.VideoMetadataKeys.SEASON -> details["season"] = value
                    MediaConstants.VideoMetadataKeys.SHOW -> details["show"] = value
                    MediaConstants.VideoMetadataKeys.SHOW_TYPE -> details["showType"] = value
                    MediaConstants.VideoMetadataKeys.STREAM_FORMAT -> details["streamFormat"] =
                        value

                    // Audio standard metadata cases
                    MediaConstants.AudioMetadataKeys.ALBUM -> details["album"] = value
                    MediaConstants.AudioMetadataKeys.ARTIST -> details["artist"] = value
                    MediaConstants.AudioMetadataKeys.AUTHOR -> details["author"] = value
                    MediaConstants.AudioMetadataKeys.LABEL -> details["label"] = value
                    MediaConstants.AudioMetadataKeys.PUBLISHER -> details["publisher"] = value
                    MediaConstants.AudioMetadataKeys.STATION -> details["station"] = value
                }
            }

            if (mediaState?.mediaPlayerName != null) {
                details["playerName"] = mediaState.mediaPlayerName!!
            }

            if (mediaState?.mediaChannel != null) {
                details["channel"] = mediaState.mediaChannel!!
            }

            if (mediaState?.mediaAppVersion != null) {
                details["appVersion"] = mediaState.mediaAppVersion!!
            }

            return details
        }

        private fun getAdvertisingDetails(
            info: Map<String, Any>?,
            metadata: Map<String, Any>?,
            mediaState: MediaState?
        ): Map<String, Any> {
            val details: MutableMap<String, Any> = mutableMapOf(
                "name" to (info?.get("ad.id") ?: ""),
                "friendlyName" to (info?.get("ad.name") ?: ""),
                "podPosition" to ((info?.get("ad.position") as? Long) ?: -1).toLong(),
                "length" to ((info?.get("ad.length") as? Double) ?: -1).toLong()
            )

            metadata?.forEach { (key, value) ->
                when (key) {
                    MediaConstants.AdMetadataKeys.ADVERTISER -> details["advertiser"] = value
                    MediaConstants.AdMetadataKeys.CAMPAIGN_ID -> details["campaignID"] = value
                    MediaConstants.AdMetadataKeys.CREATIVE_ID -> details["creativeID"] = value
                    MediaConstants.AdMetadataKeys.CREATIVE_URL -> details["creativeURL"] = value
                    MediaConstants.AdMetadataKeys.PLACEMENT_ID -> details["placementID"] = value
                    MediaConstants.AdMetadataKeys.SITE_ID -> details["siteID"] = value
                }
            }

            if (mediaState?.mediaPlayerName != null) {
                details["playerName"] = mediaState.mediaPlayerName!!
            }

            return details
        }

        private fun getAdvertisingPodDetails(info: Map<String, Any>?): Map<String, Any> {
            return mapOf(
                "friendlyName" to (info?.get("adbreak.name") ?: ""),
                "index" to ((info?.get("adbreak.position") as? Long) ?: -1).toLong(),
                "offset" to ((info?.get("adbreak.starttime") as? Double) ?: -1).toLong()
            )
        }

        private fun getChapterDetails(info: Map<String, Any>?): Map<String, Any> {
            return mapOf(
                "friendlyName" to (info?.get("chapter.name") ?: ""),
                "index" to ((info?.get("chapter.position") as? Long) ?: -1).toLong(),
                "offset" to ((info?.get("chapter.starttime") as? Double) ?: -1).toLong(),
                "length" to ((info?.get("chapter.length") as? Double) ?: -1).toLong()
            )
        }

        private fun getErrorDetails(info: Map<String, Any>?): Map<String, Any> {
            return mapOf(
                "name" to (info?.get("error.id") ?: ""),
                "source" to (info?.get("error.source") ?: "")
            )
        }

        private fun getStatesUpdateList(info: Map<String, Any>?): List<Map<String, Any>> {
            return listOf(
                mapOf(
                    "name" to (info?.get("state.name") ?: "")
                )
            )
        }

        private fun getQoeDetails(info: Map<String, Any>?): Map<String, Any> {
            return mapOf(
                "bitrate" to ((info?.get("qoe.bitrate") as? Double) ?: -1).toLong(),
                "droppedFrames" to ((info?.get("qoe.droppedframes") as? Double) ?: -1).toLong(),
                "framesPerSecond" to ((info?.get("qoe.fps") as? Double) ?: -1).toLong(),
                "timeToStart" to ((info?.get("qoe.startuptime") as? Double) ?: -1).toLong()
            )
        }
    }
}
