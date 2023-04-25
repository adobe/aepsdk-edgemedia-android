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

import com.adobe.marketing.mobile.AdobeCallback
import com.adobe.marketing.mobile.Event

/**
 * A testable [MediaTrackerEventGenerator], which allows modification of current timestamp and
 * disables the idle/ping timer.
 */
class TestableMediaTrackerEventGenerator(
    trackerId: String?,
    eventConsumer: AdobeCallback<Event>?
) : MediaTrackerEventGenerator(trackerId, eventConsumer) {

    var currentTimestampMillis: Long = 0L

    init {
        // Override timestamp supplier
        this.timestampSupplier = TimestampSupplier { currentTimestampMillis }
    }

    override fun getCurrentTimestamp(): Long {
        return currentTimestampMillis
    }

    fun setCurrentTimestamp(milliseconds: Long) {
        currentTimestampMillis = milliseconds
    }

    fun incrementCurrentTimestamp(milliseconds: Long) {
        currentTimestampMillis += milliseconds
    }

    // Disable timer
    override fun startTimer() { }

    // Disable timer
    override fun stopTimer() { }
}
