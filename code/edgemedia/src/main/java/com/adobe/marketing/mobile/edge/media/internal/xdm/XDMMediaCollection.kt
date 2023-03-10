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

internal data class XDMMediaCollection(
    var advertisingDetails: XDMAdvertisingDetails? = null,
    var advertisingPodDetails: XDMAdvertisingPodDetails? = null,
    var chapterDetails: XDMChapterDetails? = null,
    var customMetadata: List<XDMCustomMetadata>? = null,
    var errorDetails: XDMErrorDetails? = null,
    var playHead: Long? = null,
    var qoeDataDetails: XDMQoeDataDetails? = null,
    var sessionDetails: XDMSessionDetails? = null,
    var sessionID: String? = null,
    var statesStart: List<XDMPlayerStateData>? = null,
    var statesEnd: List<XDMPlayerStateData>? = null
) : XDMProperty {

    override fun serializeToXDM(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        advertisingDetails?.let {
            map.put("advertisingDetails", it.serializeToXDM())
        }

        advertisingPodDetails?.let {
            map.put("advertisingPodDetails", it.serializeToXDM())
        }

        chapterDetails?.let {
            map.put("chapterDetails", it.serializeToXDM())
        }

        customMetadata?.let {
            map.put("customMetadata", serializeFromList(it))
        }

        errorDetails?.let {
            map.put("errorDetails", it.serializeToXDM())
        }

        playHead?.let {
            map.put("playHead", it)
        }

        qoeDataDetails?.let {
            map.put("qoeDataDetails", it.serializeToXDM())
        }

        sessionDetails?.let {
            map.put("sessionDetails", it.serializeToXDM())
        }

        sessionID?.let {
            map.put("sessionID", it)
        }

        statesStart?.let {
            map.put("statesStart", serializeFromList(it))
        }

        statesEnd?.let {
            map.put("statesEnd", serializeFromList(it))
        }

        return map.toMap()
    }
}
