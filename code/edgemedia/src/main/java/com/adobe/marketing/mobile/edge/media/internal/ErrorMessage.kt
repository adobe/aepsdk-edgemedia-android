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

internal enum class ErrorMessage(val value: String) {
    ErrNotInMedia(
        "Media tracker is not in tracking session, call 'API:trackSessionStart' to begin a new" +
            " tracking session."
    ),
    ErrInMedia(
        "Media tracker is in active tracking session, call 'API:trackSessionEnd' or" +
            " 'API:trackComplete' to end current tracking session."
    ),
    ErrInBuffer(
        "Media tracker is tracking buffer events, call 'API:trackEvent(BufferComplete)' first" +
            " to stop tracking buffer events."
    ),
    ErrNotInBuffer(
        "Media tracker is not tracking buffer events, call 'API:trackEvent(BufferStart)'" +
            " before 'API:trackEvent(BufferComplete)'."
    ),
    ErrInSeek(
        "Media tracker is tracking seek events, call 'API:trackEvent(SeekComplete)' first to" +
            " stop tracking seek events."
    ),
    ErrNotInSeek(
        "Media tracker is not tracking seek events, call 'API:trackEvent(SeekStart)' before" +
            " 'API:trackEvent(SeekComplete)'."
    ),
    ErrNotInAdBreak(
        "Media tracker is not tracking any AdBreak, call 'API:trackEvent(AdBreakStart)' to" +
            " begin tracking AdBreak."
    ),
    ErrNotInAd(
        "Media tracker is not tracking any Ad, call 'API:trackEvent(AdStart)' to begin" +
            " tracking Ad."
    ),
    ErrNotInChapter(
        "Media tracker is not tracking any Chapter, call 'API:trackEvent(ChapterStart)' to" +
            " begin tracking Chapter."
    ),
    ErrInvalidMediaInfo("MediaInfo passed into 'API:trackSessionStart' is invalid."),
    ErrInvalidAdBreakInfo("AdBreakInfo passed into 'API:trackEvent(AdBreakStart)' is invalid."),
    ErrDuplicateAdBreakInfo(
        "Media tracker is currently tracking the AdBreak passed into" +
            " 'API:trackEvent(AdBreakStart)'."
    ),
    ErrInvalidAdInfo("AdInfo passed into 'API:trackEvent(AdStart)' is invalid."),
    ErrDuplicateAdInfo("Media tracker is currently tracking the Ad passed into 'API:trackEvent(AdStart)'."),
    ErrInvalidChapterInfo("ChapterInfo passed into 'API:trackEvent(ChapterStart)' is invalid."),
    ErrDuplicateChapterInfo(
        "Media tracker is currently tracking the Chapter passed into" +
            " 'API:trackEvent(ChapterStart)'."
    ),
    ErrInvalidQoEInfo("QoEInfo passed into 'API:updateQoEInfo' is invalid."),
    ErrInvalidPlaybackState(
        "Media tracker is tracking an AdBreak but not tracking any Ad and will drop any calls" +
            " to track playback state (Play, Pause, Buffer or Seek) in this state."
    ),
    ErrInvalidStateInfo(
        "StateInfo passed into 'API:trackEvent(StateStart)' or 'API:trackEvent(StateEnd)' is" +
            " invalid."
    ),
    ErrInTrackedState("Media tracker is already tracking the State passed into 'API:trackEvent(StateStart)'."),
    ErrNotInTrackedState("Media tracker is not tracking the State passed into 'API:trackEvent(StateEnd)'."),
    ErrTrackedStatesLimitReached("Media tracker is already tracking maximum allowed states (10) per session."),
    ErrInvalidErrorId(
        "ErrorId passed into 'API:trackError' is invalid. Please pass valid non-empty non-null" +
            " string for ErrorId."
    )
}
