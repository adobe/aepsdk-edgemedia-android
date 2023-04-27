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

internal data class XDMAdvertisingDetails(
    var friendlyName: String? = null,
    var length: Int? = null,
    var name: String? = null,
    var podPosition: Int? = null,
    var playerName: String? = null,
    var advertiser: String? = null,
    var campaignID: String? = null,
    var creativeID: String? = null,
    var creativeURL: String? = null,
    var placementID: String? = null,
    var siteID: String? = null
) : XDMProperty {

    override fun serializeToXDM(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        friendlyName?.let {
            map.put("friendlyName", it)
        }

        length?.let {
            map.put("length", it)
        }

        name?.let {
            map.put("name", it)
        }

        podPosition?.let {
            map.put("podPosition", it)
        }

        playerName?.let {
            map.put("playerName", it)
        }

        advertiser?.let {
            map.put("advertiser", it)
        }

        campaignID?.let {
            map.put("campaignID", it)
        }

        creativeID?.let {
            map.put("creativeID", it)
        }

        creativeURL?.let {
            map.put("creativeURL", it)
        }

        placementID?.let {
            map.put("placementID", it)
        }

        siteID?.let {
            map.put("siteID", it)
        }

        return map.toMap()
    }
}
