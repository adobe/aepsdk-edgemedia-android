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

package com.adobe.marketing.mobile.edge.media.internal;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Event;
import java.util.Calendar;
import java.util.Map;

class EventCollector implements AdobeCallback<Event> {
    private Event lastEvent = null;

    @Override
    public void call(Event event) {
        lastEvent = event;
    }

    public Event getLastEvent() {
        return lastEvent;
    }

    public Map<String, Object> getLastEventData() {
        return (lastEvent != null) ? lastEvent.getEventData() : null;
    }
}

public class TestMediaPublicTracker extends MediaPublicTracker {
    private long userTS = 0;
    private boolean useUserTS;
    EventCollector eventCollector;

    public static TestMediaPublicTracker create(final String trackerID, final boolean doUseUserTS) {

        return new TestMediaPublicTracker(trackerID, doUseUserTS, new EventCollector());
    }

    TestMediaPublicTracker(
            String trackerId, final boolean useUserTS, EventCollector eventCollector) {
        super(trackerId, eventCollector);
        this.eventCollector = eventCollector;
        this.useUserTS = useUserTS;
    }

    public Event getEvent() {
        return eventCollector.getLastEvent();
    }

    @Override
    public long getCurrentTimestamp() {
        return useUserTS ? userTS : Calendar.getInstance().getTimeInMillis();
    }

    public void setCurrentTimeStamp(final long ts) {
        userTS = ts;
    }

    public void incrementCurrentTimeStamp(final long incValue) {
        if (useUserTS) {
            userTS += incValue;
        }
    }

    @Override
    protected void startTimer() {}

    @Override
    protected void stopTimer() {}
}
