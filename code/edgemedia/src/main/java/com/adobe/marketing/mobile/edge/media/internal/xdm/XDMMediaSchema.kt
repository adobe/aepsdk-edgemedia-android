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
import java.util.Date

internal data class XDMMediaSchema(
    var timestamp: Date? = null,
    var eventType: XDMMediaEventType? = null,
    var mediaCollection: XDMMediaCollection? = null
) : XDMProperty {

    override fun serializeToXDM(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        eventType?.let {
            map.put("eventType", it.value)
        }

        timestamp?.let {
            map.put("timestamp", TimeUtils.getISO8601UTCDateWithMilliseconds(it))
        }

        mediaCollection?.let {
            map.put("mediaCollection", it)
        }

        return map.toMap()
    }
}