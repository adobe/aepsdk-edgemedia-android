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

internal interface XDMProperty {

    /**
     * Serializes this [XDMProperty] object to a map equivalent of its XDM schema.
     * @return XDM formatted map of this [XDMProperty] object
     */
    fun serializeToXDM(): Map<String, Any>

    /**
     * Serialize a list of [XDMProperty] elements to a list of XDM formatted [Map]s.
     * Calls [XDMProperty.serializeToXDM] on each element in the list.
     *
     * @param properties list of [XDMProperty] elements
     * @return list of [XDMProperty] elements serialized to XDM formatted [Map]
     */
    fun serializeFromList(properties: List<XDMProperty>): List<Map<String, Any>> {
        val list = mutableListOf<Map<String, Any>>()
        for (property in properties) {
            list.add(property.serializeToXDM())
        }

        return list.toList()
    }
}
