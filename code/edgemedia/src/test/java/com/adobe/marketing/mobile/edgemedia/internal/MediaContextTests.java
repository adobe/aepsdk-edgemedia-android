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

package com.adobe.marketing.mobile.edgemedia.internal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class MediaContextTests {

    Map<String, String> emptyMetadata;
    Map<String, String> metadata;
    String mediaID = "id";
    String mediaName = "name";
    String mediaStreamType = "vod";
    MediaType mediaType = MediaType.Video;
    double mediaLength = 30.0;
    MediaContext mediaContext;

    public MediaContextTests() {
        emptyMetadata = new HashMap<>();
        metadata = new HashMap<>();
        metadata.put("k1", "v1");
        MediaInfo mediaInfo =
                MediaInfo.create(mediaID, mediaName, mediaStreamType, mediaType, mediaLength);
        mediaContext = new MediaContext(mediaInfo, metadata);
    }

    @Test
    public void test_MediaInfo_setOnMediaContext() {
        MediaInfo mediaInfo =
                MediaInfo.create(mediaID, mediaName, mediaStreamType, mediaType, mediaLength);
        assertNotNull(mediaContext.getMediaInfo());
        assertEquals(mediaInfo, mediaContext.getMediaInfo());
        assertEquals(metadata, mediaContext.getMediaMetadata());
    }

    @Test
    public void test_AdBreakInfo_setOnMediaContext() {
        assertFalse(mediaContext.isInAdBreak());

        mediaContext.setAdBreakInfo(null);

        assertFalse(mediaContext.isInAdBreak());

        AdBreakInfo adBreakInfo = AdBreakInfo.create("name", 1, 20.0);

        mediaContext.setAdBreakInfo(adBreakInfo);

        assertTrue(mediaContext.isInAdBreak());
        assertNotNull(mediaContext.getAdBreakInfo());
        assertEquals(adBreakInfo, mediaContext.getAdBreakInfo());

        mediaContext.clearAdBreakInfo();
        assertFalse(mediaContext.isInAdBreak());
        assertNull(mediaContext.getAdBreakInfo());
    }

    @Test
    public void test_AdInfo_setOnMediaContext() {
        assertFalse(mediaContext.isInAd());

        mediaContext.setAdInfo(null, emptyMetadata);

        assertFalse(mediaContext.isInAd());

        AdInfo adInfo = AdInfo.create("id", "name", 1, 15.0);

        mediaContext.setAdInfo(adInfo, metadata);

        assertTrue(mediaContext.isInAd());
        assertNotNull(mediaContext.getAdInfo());
        assertEquals(adInfo, mediaContext.getAdInfo());
        assertEquals(metadata, mediaContext.getAdMetadata());

        mediaContext.clearAdInfo();
        assertFalse(mediaContext.isInAd());
        assertNull(mediaContext.getAdInfo());
        assertEquals(emptyMetadata, mediaContext.getAdMetadata());
    }

    @Test
    public void test_ChapterInfo_setOnMediaContext() {
        assertFalse(mediaContext.isInChapter());

        mediaContext.setChapterInfo(null, emptyMetadata);

        assertFalse(mediaContext.isInChapter());

        ChapterInfo chapterInfo = ChapterInfo.create("name", 1, 1.0, 30.0);

        mediaContext.setChapterInfo(chapterInfo, metadata);

        assertTrue(mediaContext.isInChapter());
        assertNotNull(mediaContext.getChapterInfo());
        assertEquals(chapterInfo, mediaContext.getChapterInfo());
        assertEquals(metadata, mediaContext.getChapterMetadata());

        mediaContext.clearChapterInfo();

        assertFalse(mediaContext.isInChapter());
        assertNull(mediaContext.getChapterInfo());
        assertEquals(emptyMetadata, mediaContext.getChapterMetadata());
    }

    @Test
    public void test_QoE_setOnMediaContext() {
        assertNull(mediaContext.getQoEInfo());

        mediaContext.setQoEInfo(null);

        assertNull(mediaContext.getQoEInfo());

        QoEInfo qoeInfo = QoEInfo.create(1.1, 2.2, 3.3, 4.4);
        mediaContext.setQoEInfo(qoeInfo);
        assertNotNull(mediaContext.getQoEInfo());
        assertEquals(qoeInfo, mediaContext.getQoEInfo());

        mediaContext.setQoEInfo(null);
        assertNull(mediaContext.getQoEInfo());
    }

    @Test
    public void test_Playhead_setOnMediaContext() {
        assertEquals(0, mediaContext.getPlayhead(), 0.0);

        mediaContext.setPlayhead(11.1);

        assertEquals(11.1, mediaContext.getPlayhead(), 0.0);
    }

    @Test
    public void test_BufferState_setOnMediaContext() {
        MediaPlayBackState state = MediaPlayBackState.Buffer;

        assertFalse(mediaContext.isInState(MediaPlayBackState.Buffer));

        mediaContext.enterState(state);

        assertTrue(mediaContext.isInState(MediaPlayBackState.Buffer));

        mediaContext.exitState(state);

        assertFalse(mediaContext.isInState(MediaPlayBackState.Buffer));
    }

    @Test
    public void test_SeekState_setOnMediaContext() {
        MediaPlayBackState state = MediaPlayBackState.Seek;

        assertFalse(mediaContext.isInState(MediaPlayBackState.Seek));

        mediaContext.enterState(state);

        assertTrue(mediaContext.isInState(MediaPlayBackState.Seek));

        mediaContext.exitState(state);

        assertFalse(mediaContext.isInState(MediaPlayBackState.Seek));
    }

    @Test
    public void test_PlayState_setOnMediaContext() {
        assertTrue(mediaContext.isInState(MediaPlayBackState.Init));

        mediaContext.enterState(MediaPlayBackState.Pause);

        assertTrue(mediaContext.isInState(MediaPlayBackState.Pause));

        mediaContext.enterState(MediaPlayBackState.Play);

        assertTrue(mediaContext.isInState(MediaPlayBackState.Play));

        mediaContext.enterState(MediaPlayBackState.Stall);

        assertTrue(mediaContext.isInState(MediaPlayBackState.Stall));

        // Never enter idle state again
        mediaContext.enterState(MediaPlayBackState.Init);

        assertTrue(mediaContext.isInState(MediaPlayBackState.Stall));
    }

    @Test
    public void test_IdleState_setOnMediaContext() {
        assertTrue(mediaContext.isIdle());

        mediaContext.enterState(MediaPlayBackState.Buffer);

        assertTrue(mediaContext.isIdle());

        mediaContext.enterState(MediaPlayBackState.Seek);

        assertTrue(mediaContext.isIdle());

        mediaContext.enterState(MediaPlayBackState.Pause);

        assertTrue(mediaContext.isIdle());

        mediaContext.enterState(MediaPlayBackState.Stall);

        assertTrue(mediaContext.isIdle());

        mediaContext.enterState(MediaPlayBackState.Play);
        mediaContext.exitState(MediaPlayBackState.Seek);
        mediaContext.exitState(MediaPlayBackState.Buffer);
        assertFalse(mediaContext.isIdle());
    }

    @Test
    public void test_stateInfo_simpleStateTracking() {
        StateInfo stateInfo = StateInfo.create("myCustomState");

        assertFalse(mediaContext.isInState(stateInfo));
        assertFalse(mediaContext.hasTrackedState(stateInfo));

        assertTrue(mediaContext.startState(stateInfo));
        assertTrue(mediaContext.isInState(stateInfo));

        assertFalse(mediaContext.startState(stateInfo));
        assertTrue(mediaContext.endState(stateInfo));

        assertFalse(mediaContext.isInState(stateInfo));
        assertTrue(mediaContext.hasTrackedState(stateInfo));
        assertFalse(mediaContext.endState(stateInfo));
    }

    @Test
    public void test_stateInfo_simpleStateTracking2() {
        StateInfo stateInfo = StateInfo.create("myCustomState");

        assertFalse(mediaContext.endState(stateInfo));
        assertFalse(mediaContext.isInState(stateInfo));
        assertFalse(mediaContext.hasTrackedState(stateInfo));

        assertTrue(mediaContext.startState(stateInfo));
        assertTrue(mediaContext.isInState(stateInfo));

        assertTrue(mediaContext.endState(stateInfo));
        assertTrue(mediaContext.hasTrackedState(stateInfo));
    }

    @Test
    public void test_stateInfo_stateTrackingLimit() {
        for (int i = 0; i < MediaTestConstants.EventDataKeys.StateInfo.STATE_LIMIT; i++) {
            StateInfo stateInfo = StateInfo.create(Integer.toString(i));
            assertTrue(mediaContext.startState(stateInfo));
            assertTrue(mediaContext.isInState(stateInfo));
        }

        StateInfo stateInfo = StateInfo.create("myCustomState");
        assertFalse(mediaContext.startState(stateInfo));
        assertFalse(mediaContext.isInState(stateInfo));
    }

    @Test
    public void test_stateInfo_stateTrackingLimit2() {
        for (int i = 0; i < MediaTestConstants.EventDataKeys.StateInfo.STATE_LIMIT; i++) {
            StateInfo stateInfo = StateInfo.create(Integer.toString(i));
            assertTrue(mediaContext.startState(stateInfo));
            assertTrue(mediaContext.isInState(stateInfo));
        }

        assertTrue(mediaContext.hasReachedStateLimit());

        StateInfo stateInfo = StateInfo.create("myCustomState");
        assertFalse(mediaContext.startState(stateInfo));
        assertFalse(mediaContext.isInState(stateInfo));

        StateInfo stateInfo1 = StateInfo.create("0");

        assertTrue(mediaContext.isInState(stateInfo1));
        assertTrue(mediaContext.endState(stateInfo1));
        assertFalse(mediaContext.isInState(stateInfo1));
        assertTrue(mediaContext.startState(stateInfo1));
        assertTrue(mediaContext.isInState(stateInfo1));
        assertTrue(mediaContext.hasTrackedState(stateInfo1));
    }

    // Only track 10 unique states per session
    @Test
    public void test_stateInfo_stateTrackingLimit3() {
        for (int i = 0; i < MediaTestConstants.EventDataKeys.StateInfo.STATE_LIMIT; i++) {
            StateInfo stateInfo = StateInfo.create(Integer.toString(i));
            assertTrue(mediaContext.startState(stateInfo));
            assertTrue(mediaContext.isInState(stateInfo));
        }

        assertTrue(mediaContext.hasReachedStateLimit());

        StateInfo newStateInfo = StateInfo.create("myCustomState");
        assertFalse(mediaContext.startState(newStateInfo));
        assertFalse(mediaContext.isInState(newStateInfo));

        for (int i = 9; i >= 0; i--) {
            StateInfo stateInfo = StateInfo.create(Integer.toString(i));
            assertTrue(mediaContext.endState(stateInfo));
            assertFalse(mediaContext.isInState(stateInfo));
        }

        assertTrue(mediaContext.hasReachedStateLimit());

        assertFalse(mediaContext.startState(newStateInfo));
        assertFalse(mediaContext.isInState(newStateInfo));
    }

    @Test
    public void test_stateInfo_stateTrackingLimitWithClear() {
        for (int i = 0; i < MediaTestConstants.EventDataKeys.StateInfo.STATE_LIMIT; i++) {
            StateInfo stateInfo = StateInfo.create(Integer.toString(i));
            assertTrue(mediaContext.startState(stateInfo));
            assertTrue(mediaContext.isInState(stateInfo));
        }

        assertTrue(mediaContext.hasReachedStateLimit());
        mediaContext.clearState();
        assertFalse(mediaContext.hasReachedStateLimit());

        StateInfo stateInfo = StateInfo.create("myCustomState");
        assertTrue(mediaContext.startState(stateInfo));
        assertTrue(mediaContext.isInState(stateInfo));

        StateInfo stateInfo1 = StateInfo.create("0");

        assertTrue(mediaContext.startState(stateInfo1));
        assertTrue(mediaContext.isInState(stateInfo1));
    }

    @Test
    public void test_stateInfo_getActiveStates() {
        StateInfo stateInfo = StateInfo.create("myCustomState");
        StateInfo stateInfo1 = StateInfo.create("myCustomState1");
        StateInfo stateInfo2 = StateInfo.create("myCustomState2");

        assertTrue(mediaContext.startState(stateInfo));
        assertTrue(mediaContext.startState(stateInfo1));
        assertTrue(mediaContext.startState(stateInfo2));

        assertTrue(mediaContext.endState(stateInfo1));

        ArrayList<StateInfo> active_states = mediaContext.getActiveTrackedStates();
        assertEquals(2, active_states.size());
    }
}
