/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile;

import java.util.Map;

public interface MediaTracker {
    /**
     * Tracking method to track the start of a viewing session.
     *
     * @param mediaInfo A Map instance created using createMediaObjectWithName method
     * @param contextData a map containing the context data to be tracked.
     */
    void trackSessionStart(Map<String, Object> mediaInfo, Map<String, String> contextData);

    /** Video playback tracking method to track Video Play triggers VIDEO_PLAY event */
    void trackPlay();

    /** Video playback tracking method to track Video Pause triggers VIDEO_PAUSE event */
    void trackPause();

    /**
     * Video playback tracking method to track Video Complete triggers VIDEO_COMPLETE event and
     * calls back the callback method.
     */
    void trackComplete();

    /**
     * Tracking method to track the end of a viewing session. This method must be called even if the
     * user does not watch the video to completion.
     */
    void trackSessionEnd();

    /**
     * Error tracking method to track Player Error
     *
     * @param errorId Error Id
     */
    void trackError(String errorId);

    /**
     * Video playback tracking method to track an event
     *
     * @param event Event constant to be tracked
     * @param info a MediaObject instance containing event info
     * @param contextData a Map containing the context data to be tracked.
     */
    void trackEvent(Media.Event event, Map<String, Object> info, Map<String, String> contextData);

    /**
     * Method to update current QoS information.
     *
     * @param qoeData the Map instance containing current QoS information. This method can be called
     *     multiple times during a playback session. Player implementation must always return the
     *     most recently available QoS data.
     */
    void updateQoEObject(Map<String, Object> qoeData);

    /**
     * Method to update current playhead.
     *
     * @param time Current position of the playhead. For VOD, value is specified in seconds from the
     *     beginning of the media item. For live streaming, return playhead position if available or
     *     the current UTC time in seconds otherwise.
     */
    void updateCurrentPlayhead(double time);
}
