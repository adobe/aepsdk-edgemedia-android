/*
  Copyright 2021 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.media.testappkotlin.player;

public enum PlayerEvent {
    VIDEO_LOAD("video_load"),
    VIDEO_UNLOAD("video_unload"),
    PLAY("play"),
    PAUSE("pause"),
    SEEK_START("seek_start"),
    SEEK_COMPLETE("seek_complete"),
    BUFFER_START("buffer_start"),
    BUFFER_COMPLETE("buffer_complete"),
    AD_START("ad_start"),
    AD_COMPLETE("ad_complete"),
    CHAPTER_START("chapter_start"),
    CHAPTER_COMPLETE("chapter_complete"),
    COMPLETE("complete"),
    PLAYHEAD_UPDATE("playhead_update"),
    PLAYER_STATE_MUTE_START("player_state_mute_start"),
    PLAYER_STATE_MUTE_END("player_state_mute_end");

    private final String _type;

    PlayerEvent(String type) {
        _type = type;
    }

    public String getType() {
        return _type;
    }
}
