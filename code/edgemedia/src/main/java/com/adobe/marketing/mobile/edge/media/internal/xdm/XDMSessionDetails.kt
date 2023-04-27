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

internal data class XDMSessionDetails(
    // Required fields sourced from APIs
    var contentType: String? = null,
    var friendlyName: String? = null,
    var hasResume: Boolean? = null,
    var length: Int? = null,
    var name: String? = null,
    var streamType: XDMStreamType? = null,

    // Required fields sourced from media configuration
    var channel: String? = null,
    var playerName: String? = null,

    // Optional field sourced from media configuration
    var appVersion: String? = null,

    // Optional metadata fields
    // Audio Standard Metadata
    var album: String? = null,
    var artist: String? = null,
    var author: String? = null,
    var label: String? = null,
    var publisher: String? = null,
    var station: String? = null,

    // Video Standard Metadata
    var adLoad: String? = null,
    var authorized: String? = null,
    var assetID: String? = null,
    var dayPart: String? = null,
    var episode: String? = null,
    var feed: String? = null,
    var firstAirDate: String? = null,
    var firstDigitalDate: String? = null,
    var genre: String? = null,
    var mvpd: String? = null,
    var network: String? = null,
    var originator: String? = null,
    var rating: String? = null,
    var season: String? = null,
    var segment: String? = null,
    var show: String? = null,
    var showType: String? = null,
    var streamFormat: String? = null
) : XDMProperty {

    override fun serializeToXDM(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        contentType?.let {
            map.put("contentType", it)
        }

        friendlyName?.let {
            map.put("friendlyName", it)
        }

        hasResume?.let {
            map.put("hasResume", it)
        }

        length?.let {
            map.put("length", it)
        }

        name?.let {
            map.put("name", it)
        }

        streamType?.let {
            map.put("streamType", it.value)
        }

        channel?.let {
            map.put("channel", it)
        }

        playerName?.let {
            map.put("playerName", it)
        }

        appVersion?.let {
            map.put("appVersion", it)
        }

        album?.let {
            map.put("album", it)
        }

        artist?.let {
            map.put("artist", it)
        }

        author?.let {
            map.put("author", it)
        }

        label?.let {
            map.put("label", it)
        }

        publisher?.let {
            map.put("publisher", it)
        }

        station?.let {
            map.put("station", it)
        }

        adLoad?.let {
            map.put("adLoad", it)
        }

        authorized?.let {
            map.put("authorized", it)
        }

        assetID?.let {
            map.put("assetID", it)
        }

        dayPart?.let {
            map.put("dayPart", it)
        }

        episode?.let {
            map.put("episode", it)
        }

        feed?.let {
            map.put("feed", it)
        }

        firstAirDate?.let {
            map.put("firstAirDate", it)
        }

        firstDigitalDate?.let {
            map.put("firstDigitalDate", it)
        }

        genre?.let {
            map.put("genre", it)
        }

        mvpd?.let {
            map.put("mvpd", it)
        }

        network?.let {
            map.put("network", it)
        }

        originator?.let {
            map.put("originator", it)
        }

        rating?.let {
            map.put("rating", it)
        }

        season?.let {
            map.put("season", it)
        }

        segment?.let {
            map.put("segment", it)
        }

        show?.let {
            map.put("show", it)
        }

        showType?.let {
            map.put("showType", it)
        }

        streamFormat?.let {
            map.put("streamFormat", it)
        }

        return map.toMap()
    }
}
