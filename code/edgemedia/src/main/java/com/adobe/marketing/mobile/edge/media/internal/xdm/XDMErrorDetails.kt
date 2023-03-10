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

internal data class XDMErrorDetails(
    var name: String? = null,
    var source: String? = null
) : XDMProperty {

    override fun serializeToXDM(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        name?.let {
            map.put("name", it)
        }

        source?.let {
            map.put("source", it)
        }

        return map.toMap()
    }
}