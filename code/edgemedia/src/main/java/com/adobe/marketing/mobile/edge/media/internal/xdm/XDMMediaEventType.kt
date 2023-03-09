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

enum class XDMMediaEventType(val value: String) {
    SESSION_START("media.sessionStart"),
    SESSION_COMPLETE("media.sessionComplete"),
    SESSION_END("media.sessionEnd"),
    PLAY("media.play"),
    PAUSE_START("media.pauseStart"),
    PING("media.ping"),
    ERROR("media.error"),
    BUFFER_START("media.bufferStart"),
    BITRATE_CHANGE("media.bitrateChange"),
    AD_BREAK_START("media.adBreakStart"),
    AD_BREAK_COMPLETE("media.adBreakComplete"),
    AD_START("media.adStart"),
    AD_SKIP("media.adSkip"),
    AD_COMPLETE("media.adComplete"),
    CHAPTER_SKIP("media.chapterSkip"),
    CHAPTER_START("media.chapterStart"),
    CHAPTER_COMPLETE("media.chapterComplete"),
    STATES_UPDATE("media.statesUpdate")
}
