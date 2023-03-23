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

internal enum class MediaRuleName {
    Invalid,
    MediaStart,
    MediaComplete,
    MediaSkip,
    AdBreakStart,
    AdBreakComplete,
    AdStart,
    AdComplete,
    AdSkip,
    ChapterStart,
    ChapterComplete,
    ChapterSkip,
    Play,
    Pause,
    SeekStart,
    SeekComplete,
    BufferStart,
    BufferComplete,
    BitrateChange,
    Error,
    QoEUpdate,
    PlayheadUpdate,
    StateStart,
    StateEnd;

    companion object {
        @JvmStatic
        fun getRuleName(eventName: String): MediaRuleName {
            when (eventName) {
                MediaInternalConstants.EventDataKeys.MediaEventName.SESSION_START -> return MediaStart
                MediaInternalConstants.EventDataKeys.MediaEventName.COMPLETE -> return MediaComplete
                MediaInternalConstants.EventDataKeys.MediaEventName.SESSION_END -> return MediaSkip
                MediaInternalConstants.EventDataKeys.MediaEventName.PLAY -> return Play
                MediaInternalConstants.EventDataKeys.MediaEventName.PAUSE -> return Pause
                MediaInternalConstants.EventDataKeys.MediaEventName.ADBREAK_START -> return AdBreakStart
                MediaInternalConstants.EventDataKeys.MediaEventName.ADBREAK_COMPLETE -> return AdBreakComplete
                MediaInternalConstants.EventDataKeys.MediaEventName.AD_START -> return AdStart
                MediaInternalConstants.EventDataKeys.MediaEventName.AD_COMPLETE -> return AdComplete
                MediaInternalConstants.EventDataKeys.MediaEventName.AD_SKIP -> return AdSkip
                MediaInternalConstants.EventDataKeys.MediaEventName.CHAPTER_START -> return ChapterStart
                MediaInternalConstants.EventDataKeys.MediaEventName.CHAPTER_COMPLETE -> return ChapterComplete
                MediaInternalConstants.EventDataKeys.MediaEventName.CHAPTER_SKIP -> return ChapterSkip
                MediaInternalConstants.EventDataKeys.MediaEventName.SEEK_START -> return SeekStart
                MediaInternalConstants.EventDataKeys.MediaEventName.SEEK_COMPLETE -> return SeekComplete
                MediaInternalConstants.EventDataKeys.MediaEventName.BUFFER_START -> return BufferStart
                MediaInternalConstants.EventDataKeys.MediaEventName.BUFFER_COMPLETE -> return BufferComplete
                MediaInternalConstants.EventDataKeys.MediaEventName.BITRATE_CHANGE -> return BitrateChange
                MediaInternalConstants.EventDataKeys.MediaEventName.QOE_UPDATE -> return QoEUpdate
                MediaInternalConstants.EventDataKeys.MediaEventName.ERROR -> return Error
                MediaInternalConstants.EventDataKeys.MediaEventName.PLAYHEAD_UPDATE -> return PlayheadUpdate
                MediaInternalConstants.EventDataKeys.MediaEventName.STATE_START -> return StateStart
                MediaInternalConstants.EventDataKeys.MediaEventName.STATE_END -> return StateEnd
                else -> return Invalid
            }
        }
    }
}
