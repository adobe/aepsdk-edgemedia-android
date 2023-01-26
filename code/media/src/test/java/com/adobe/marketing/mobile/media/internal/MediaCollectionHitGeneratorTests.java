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

package com.adobe.marketing.mobile.media.internal;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class MediaCollectionHitGeneratorTests {
    Map<String, Object> emptyParams;
    Map<String, String> emptyMetadata;
    MediaInfo mediaInfo;
    Map<String, String> metadata;
    Map<String, Object> config;
    MediaContext mediaContext;
    FakeMediaHitProcessor hitProcessor;
    MediaCollectionHitGenerator hitGenerator;
    String refSessionId;

    public MediaCollectionHitGeneratorTests() {
        emptyParams = new HashMap<>();
        emptyMetadata = new HashMap<>();

        mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 58.8);
        metadata = new HashMap<>();
        metadata.put("k1", "v1");
        metadata.put("a.media.show", "show");

        refSessionId = "sessionID123";

        config = new HashMap<>();

        mediaContext = new MediaContext(mediaInfo, metadata);
        hitProcessor = new FakeMediaHitProcessor();

        config.put(MediaTestConstants.EventDataKeys.Config.DOWNLOADED_CONTENT, true);

        hitGenerator =
                new MediaCollectionHitGenerator(
                        mediaContext, hitProcessor, config, 0, refSessionId);
    }

    @Test
    public void test_processMediaStart() {
        hitGenerator.processMediaStart();
        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        Map<String, Object> params = MediaCollectionHelper.extractMediaParams(mediaContext);
        params.put(MediaCollectionTestConstants.Media.DOWNLOADED.key, true);
        params.put(MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID, refSessionId);

        metadata = MediaCollectionHelper.extractMediaMetadata(mediaContext);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.SESSION_START,
                        params,
                        metadata,
                        emptyParams,
                        0,
                        0);
        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processMediaStartOnline() {
        config.put(MediaTestConstants.EventDataKeys.Config.DOWNLOADED_CONTENT, false);
        hitGenerator =
                new MediaCollectionHitGenerator(
                        mediaContext, hitProcessor, config, 0, refSessionId);
        hitGenerator.processMediaStart();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        Map<String, Object> params = MediaCollectionHelper.extractMediaParams(mediaContext);
        params.put(MediaCollectionTestConstants.Media.DOWNLOADED.key, false);
        params.put(MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID, refSessionId);

        metadata = MediaCollectionHelper.extractMediaMetadata(mediaContext);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.SESSION_START,
                        params,
                        metadata,
                        emptyParams,
                        0,
                        0);
        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processMediaStart_withConfig() {
        config.put(MediaTestConstants.EventDataKeys.Config.CHANNEL, "test-channel");
        hitGenerator =
                new MediaCollectionHitGenerator(
                        mediaContext, hitProcessor, config, 0, refSessionId);
        hitGenerator.processMediaStart();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        Map<String, Object> params = MediaCollectionHelper.extractMediaParams(mediaContext);
        params.put(MediaCollectionTestConstants.Media.DOWNLOADED.key, true);
        params.put(MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID, refSessionId);

        metadata = MediaCollectionHelper.extractMediaMetadata(mediaContext);

        params.put(MediaCollectionTestConstants.Media.CHANNEL.key, "test-channel");
        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.SESSION_START,
                        params,
                        metadata,
                        emptyParams,
                        0,
                        0);
        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processMediaComplete() {
        hitGenerator.processMediaComplete();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.SESSION_COMPLETE,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processMediaSkip() {
        hitGenerator.processMediaSkip();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.SESSION_END,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processAdBreakStart() {
        AdBreakInfo adBreakInfo = AdBreakInfo.create("adbreakname", 0, 10);
        mediaContext.setAdBreakInfo(adBreakInfo);

        hitGenerator.processAdBreakStart();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        Map<String, Object> params = MediaCollectionHelper.extractAdBreakParams(mediaContext);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.ADBREAK_START,
                        params,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processAdBreakComplete() {
        hitGenerator.processAdBreakComplete();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.ADBREAK_COMPLETE,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processAdBreakSkip() {
        hitGenerator.processAdBreakSkip();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.ADBREAK_COMPLETE,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processAdStart() {
        AdInfo adInfo = AdInfo.create("adid", "adname", 1, 30.0);
        Map<String, String> adMetadata = new HashMap<>();
        adMetadata.put("k1", "v1");
        adMetadata.put("media.ad.advertiser", "advertiser");

        mediaContext.setAdInfo(adInfo, adMetadata);

        hitGenerator.processAdStart();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        Map<String, Object> params = MediaCollectionHelper.extractAdParams(mediaContext);

        Map<String, String> metadata = MediaCollectionHelper.extractAdMetadata(mediaContext);

        MediaHit expectedMediaHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.AD_START,
                        params,
                        metadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedMediaHit, hit);
    }

    @Test
    public void test_processAdComplete() {
        hitGenerator.processAdComplete();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.AD_COMPLETE,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processAdSkip() {
        hitGenerator.processAdSkip();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.AD_SKIP,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processChapterStart() {
        ChapterInfo chapterInfo = ChapterInfo.create("chaptername", 1, 15, 60);
        Map<String, String> chapterMetadata = new HashMap<>();
        chapterMetadata.put("k1", "v1");

        mediaContext.setChapterInfo(chapterInfo, chapterMetadata);

        hitGenerator.processChapterStart();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        Map<String, Object> params = MediaCollectionHelper.extractChapterParams(mediaContext);

        Map<String, String> metadata = MediaCollectionHelper.extractChapterMetadata(mediaContext);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.CHAPTER_START,
                        params,
                        metadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processChapterComplete() {
        hitGenerator.processChapterComplete();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.CHAPTER_COMPLETE,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processChapterSkip() {
        hitGenerator.processChapterSkip();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.CHAPTER_SKIP,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processIdleStart() {
        hitGenerator.processSessionAbort();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.SESSION_END,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processIdleComplete() {
        AdBreakInfo adBreakInfo = AdBreakInfo.create("adbreakname", 1, 10.0);
        mediaContext.setAdBreakInfo(adBreakInfo);

        AdInfo adInfo = AdInfo.create("adid", "adanme", 1, 15.0);
        Map<String, String> adMetadata = new HashMap<>();
        adMetadata.put("k1", "v1");
        adMetadata.put("a.media.ad.advertisier", "advertiser");

        mediaContext.setAdInfo(adInfo, adMetadata);

        ChapterInfo chapterInfo = ChapterInfo.create("chaptername", 1, 10, 30);
        Map<String, String> chapterMetadata = new HashMap<>();
        chapterMetadata.put("k1", "v1");

        mediaContext.setChapterInfo(chapterInfo, chapterMetadata);

        mediaContext.enterState(MediaPlayBackState.Play);

        hitGenerator.processSessionRestart();

        assertEquals(5, hitProcessor.hitCountfromActiveSession());

        // MediaStart
        {
            Map<String, Object> params = MediaCollectionHelper.extractMediaParams(mediaContext);
            params.put(MediaCollectionTestConstants.Media.DOWNLOADED.key, true);
            params.put(MediaCollectionTestConstants.Media.RESUME.key, true);
            params.put(MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID, refSessionId);

            Map<String, String> metadata = MediaCollectionHelper.extractMediaMetadata(mediaContext);

            MediaHit expectedhit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.SESSION_START,
                            params,
                            metadata,
                            emptyParams,
                            0,
                            0);

            MediaHit hit = hitProcessor.getHitFromActiveSession(0);

            assertEquals(expectedhit, hit);
        }

        // ChapterStart
        {
            Map<String, Object> params = MediaCollectionHelper.extractChapterParams(mediaContext);

            Map<String, String> metadata =
                    MediaCollectionHelper.extractChapterMetadata(mediaContext);

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.CHAPTER_START,
                            params,
                            metadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(1));
        }

        // AdBreakStart
        {
            Map<String, Object> params = MediaCollectionHelper.extractAdBreakParams(mediaContext);

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.ADBREAK_START,
                            params,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(2));
        }

        // AdStart
        {
            Map<String, Object> params = MediaCollectionHelper.extractAdParams(mediaContext);

            Map<String, String> metadata = MediaCollectionHelper.extractAdMetadata(mediaContext);

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.AD_START,
                            params,
                            metadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(3));
        }

        // Play
        {
            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.PLAY,
                            emptyParams,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(4));
        }
    }

    @Test
    public void test_processIdleCompleteOnline() {
        config.put(MediaTestConstants.EventDataKeys.Config.DOWNLOADED_CONTENT, false);
        hitGenerator =
                new MediaCollectionHitGenerator(
                        mediaContext, hitProcessor, config, 0, refSessionId);

        AdBreakInfo adBreakInfo = AdBreakInfo.create("adbreakname", 1, 10.0);
        mediaContext.setAdBreakInfo(adBreakInfo);

        AdInfo adInfo = AdInfo.create("adid", "adanme", 1, 15.0);
        Map<String, String> adMetadata = new HashMap<>();
        adMetadata.put("k1", "v1");
        adMetadata.put("a.media.ad.advertisier", "advertiser");

        mediaContext.setAdInfo(adInfo, adMetadata);

        ChapterInfo chapterInfo = ChapterInfo.create("chaptername", 1, 10, 30);
        Map<String, String> chapterMetadata = new HashMap<>();
        chapterMetadata.put("k1", "v1");

        mediaContext.setChapterInfo(chapterInfo, chapterMetadata);

        mediaContext.enterState(MediaPlayBackState.Play);

        hitGenerator.processSessionRestart();

        assertEquals(5, hitProcessor.hitCountfromActiveSession());

        // MediaStart
        {
            Map<String, Object> params = MediaCollectionHelper.extractMediaParams(mediaContext);
            params.put(MediaCollectionTestConstants.Media.DOWNLOADED.key, false);
            params.put(MediaCollectionTestConstants.Media.RESUME.key, true);
            params.put(MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID, refSessionId);

            Map<String, String> metadata = MediaCollectionHelper.extractMediaMetadata(mediaContext);

            MediaHit expectedhit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.SESSION_START,
                            params,
                            metadata,
                            emptyParams,
                            0,
                            0);

            MediaHit hit = hitProcessor.getHitFromActiveSession(0);

            assertEquals(expectedhit, hit);
        }

        // ChapterStart
        {
            Map<String, Object> params = MediaCollectionHelper.extractChapterParams(mediaContext);

            Map<String, String> metadata =
                    MediaCollectionHelper.extractChapterMetadata(mediaContext);

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.CHAPTER_START,
                            params,
                            metadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(1));
        }

        // AdBreakStart
        {
            Map<String, Object> params = MediaCollectionHelper.extractAdBreakParams(mediaContext);

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.ADBREAK_START,
                            params,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(2));
        }

        // AdStart
        {
            Map<String, Object> params = MediaCollectionHelper.extractAdParams(mediaContext);

            Map<String, String> metadata = MediaCollectionHelper.extractAdMetadata(mediaContext);

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.AD_START,
                            params,
                            metadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(3));
        }

        // Play
        {
            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.PLAY,
                            emptyParams,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(4));
        }
    }

    @Test
    public void test_ProcessIdleCompleteStateTrackingResumesAfterIdle() {
        StateInfo stateInfo = StateInfo.create("fullscreen");

        mediaContext.enterState(MediaPlayBackState.Play);

        mediaContext.startState(stateInfo);

        hitGenerator.processSessionRestart();

        assertEquals(3, hitProcessor.hitCountfromActiveSession());

        // MediaStart
        {
            Map<String, Object> params = MediaCollectionHelper.extractMediaParams(mediaContext);
            params.put(MediaCollectionTestConstants.Media.DOWNLOADED.key, true);
            params.put(MediaCollectionTestConstants.Media.RESUME.key, true);
            params.put(MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID, refSessionId);

            Map<String, String> metadata = MediaCollectionHelper.extractMediaMetadata(mediaContext);

            MediaHit expectedhit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.SESSION_START,
                            params,
                            metadata,
                            emptyParams,
                            0,
                            0);

            MediaHit hit = hitProcessor.getHitFromActiveSession(0);

            assertEquals(expectedhit, hit);
        }

        // StateStart
        {
            Map<String, Object> params = new HashMap<>();
            params.put(MediaCollectionTestConstants.State.STATE_NAME.key, stateInfo.getStateName());

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.STATE_START,
                            params,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(1));
        }

        // Play
        {
            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.PLAY,
                            emptyParams,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(2));
        }
    }

    @Test
    public void test_ProcessIdleCompleteNoActiveStates() {
        StateInfo stateInfo = StateInfo.create("fullscreen");

        mediaContext.enterState(MediaPlayBackState.Play);

        mediaContext.startState(stateInfo);

        mediaContext.endState(stateInfo);

        hitGenerator.processSessionRestart();

        assertEquals(2, hitProcessor.hitCountfromActiveSession());

        // MediaStart
        {
            Map<String, Object> params = MediaCollectionHelper.extractMediaParams(mediaContext);
            params.put(MediaCollectionTestConstants.Media.DOWNLOADED.key, true);
            params.put(MediaCollectionTestConstants.Media.RESUME.key, true);
            params.put(MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID, refSessionId);

            Map<String, String> metadata = MediaCollectionHelper.extractMediaMetadata(mediaContext);

            MediaHit expectedhit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.SESSION_START,
                            params,
                            metadata,
                            emptyParams,
                            0,
                            0);

            MediaHit hit = hitProcessor.getHitFromActiveSession(0);

            assertEquals(expectedhit, hit);
        }

        // Play
        {
            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.PLAY,
                            emptyParams,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(1));
        }
    }

    @Test
    public void test_ProcessIdleCompleteStateTrackingResumesAfterIdle2() {
        StateInfo stateInfo = StateInfo.create("fullscreen");

        mediaContext.enterState(MediaPlayBackState.Play);

        mediaContext.startState(stateInfo);

        mediaContext.endState(stateInfo);

        mediaContext.startState(stateInfo);

        hitGenerator.processSessionRestart();

        assertEquals(3, hitProcessor.hitCountfromActiveSession());

        // MediaStart
        {
            Map<String, Object> params = MediaCollectionHelper.extractMediaParams(mediaContext);
            params.put(MediaCollectionTestConstants.Media.DOWNLOADED.key, true);
            params.put(MediaCollectionTestConstants.Media.RESUME.key, true);
            params.put(MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID, refSessionId);

            Map<String, String> metadata = MediaCollectionHelper.extractMediaMetadata(mediaContext);

            MediaHit expectedhit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.SESSION_START,
                            params,
                            metadata,
                            emptyParams,
                            0,
                            0);

            MediaHit hit = hitProcessor.getHitFromActiveSession(0);

            assertEquals(expectedhit, hit);
        }

        // StateStart
        {
            Map<String, Object> params = new HashMap<>();
            params.put(MediaCollectionTestConstants.State.STATE_NAME.key, stateInfo.getStateName());

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.STATE_START,
                            params,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(1));
        }

        // Play
        {
            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.PLAY,
                            emptyParams,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(2));
        }
    }

    @Test
    public void test_processPlaybackStateDifferentState() {
        hitGenerator.processPlayback(false);
        assertEquals(0, hitProcessor.hitCountfromActiveSession());

        {
            mediaContext.enterState(MediaPlayBackState.Play);
            hitGenerator.processPlayback(false);
            assertEquals(1, hitProcessor.hitCountfromActiveSession());

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.PLAY,
                            emptyParams,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
            hitProcessor.clearHitsFromActionSession();
        }

        {
            mediaContext.enterState(MediaPlayBackState.Buffer);
            hitGenerator.processPlayback(false);
            assertEquals(1, hitProcessor.hitCountfromActiveSession());

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.BUFFER_START,
                            emptyParams,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
            hitProcessor.clearHitsFromActionSession();
        }

        {
            mediaContext.exitState(MediaPlayBackState.Buffer);
            hitGenerator.processPlayback(false);
            assertEquals(1, hitProcessor.hitCountfromActiveSession());

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.PLAY,
                            emptyParams,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
            hitProcessor.clearHitsFromActionSession();
        }

        {
            mediaContext.enterState(MediaPlayBackState.Seek);
            hitGenerator.processPlayback(false);
            assertEquals(1, hitProcessor.hitCountfromActiveSession());

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.PAUSE_START,
                            emptyParams,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
            hitProcessor.clearHitsFromActionSession();
        }

        {
            mediaContext.exitState(MediaPlayBackState.Seek);
            hitGenerator.processPlayback(false);
            assertEquals(1, hitProcessor.hitCountfromActiveSession());

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.PLAY,
                            emptyParams,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
            hitProcessor.clearHitsFromActionSession();
        }

        {
            mediaContext.enterState(MediaPlayBackState.Pause);
            hitGenerator.processPlayback(false);
            assertEquals(1, hitProcessor.hitCountfromActiveSession());

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.PAUSE_START,
                            emptyParams,
                            emptyMetadata,
                            emptyParams,
                            0,
                            0);

            assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
            hitProcessor.clearHitsFromActionSession();
        }
    }

    @Test
    public void test_ProcessStateStartFullscreen() {
        StateInfo stateInfo = StateInfo.create("fullscreen");
        hitGenerator.processStateStart(stateInfo);

        Map<String, Object> params = new HashMap<>();
        params.put(MediaCollectionTestConstants.State.STATE_NAME.key, stateInfo.getStateName());

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.STATE_START,
                        params,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_ProcessStateStart_ShouldNotSendQoEData() {

        QoEInfo qoeInfo = QoEInfo.create(1, 2, 3, 4);
        mediaContext.setQoEInfo(qoeInfo);

        StateInfo stateInfo = StateInfo.create("fullscreen");
        hitGenerator.processStateStart(stateInfo);

        Map<String, Object> params = new HashMap<>();
        params.put(MediaCollectionTestConstants.State.STATE_NAME.key, stateInfo.getStateName());

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.STATE_START,
                        params,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_ProcessStateStart_ShouldSendQoEDataInNextPingAfterStateStart() {

        QoEInfo qoeInfo = QoEInfo.create(1, 2, 3, 4);
        mediaContext.setQoEInfo(qoeInfo);

        StateInfo stateInfo = StateInfo.create("fullscreen");
        hitGenerator.processStateStart(stateInfo);

        Map<String, Object> params = new HashMap<>();
        params.put(MediaCollectionTestConstants.State.STATE_NAME.key, stateInfo.getStateName());

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.STATE_START,
                        params,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);

        hitProcessor.clearHitsFromActionSession();

        {
            mediaContext.enterState(MediaPlayBackState.Play);
            hitGenerator.processPlayback(false);
            Map<String, Object> qoeData = MediaCollectionHelper.extractQoEData(mediaContext);

            Map<String, Object> expectedQoEParams = new HashMap<>();
            expectedQoEParams.put(MediaCollectionTestConstants.QoE.BITRATE.key, 1L);
            expectedQoEParams.put(MediaCollectionTestConstants.QoE.DROPPED_FRAMES.key, 2L);
            expectedQoEParams.put(MediaCollectionTestConstants.QoE.FPS.key, 3L);
            expectedQoEParams.put(MediaCollectionTestConstants.QoE.STARTUP_TIME.key, 4L);

            assertEquals(expectedQoEParams, qoeData);

            assertEquals(1, hitProcessor.hitCountfromActiveSession());
            MediaHit expectedHit2 =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.PLAY,
                            emptyParams,
                            emptyMetadata,
                            qoeData,
                            0,
                            0);

            assertEquals(expectedHit2, hitProcessor.getHitFromActiveSession(0));
            hitProcessor.clearHitsFromActionSession();
        }
    }

    @Test
    public void test_ProcessStateEndFullscreen() {
        StateInfo stateInfo = StateInfo.create("fullscreen");
        hitGenerator.processStateEnd(stateInfo);

        Map<String, Object> params = new HashMap<>();
        params.put(MediaCollectionTestConstants.State.STATE_NAME.key, stateInfo.getStateName());

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.STATE_END,
                        params,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_ProcessStateEnd_ShouldNotSendQoEData() {

        QoEInfo qoeInfo = QoEInfo.create(1, 2, 3, 4);
        mediaContext.setQoEInfo(qoeInfo);

        StateInfo stateInfo = StateInfo.create("fullscreen");
        hitGenerator.processStateEnd(stateInfo);

        Map<String, Object> params = new HashMap<>();
        params.put(MediaCollectionTestConstants.State.STATE_NAME.key, stateInfo.getStateName());

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.STATE_END,
                        params,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_ProcessStateEnd_ShouldSendQoEDataInNextPingAfterStateStart() {

        QoEInfo qoeInfo = QoEInfo.create(1, 2, 3, 4);
        mediaContext.setQoEInfo(qoeInfo);

        StateInfo stateInfo = StateInfo.create("fullscreen");
        hitGenerator.processStateEnd(stateInfo);

        Map<String, Object> params = new HashMap<>();
        params.put(MediaCollectionTestConstants.State.STATE_NAME.key, stateInfo.getStateName());

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.STATE_END,
                        params,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);

        hitProcessor.clearHitsFromActionSession();

        {
            mediaContext.enterState(MediaPlayBackState.Play);
            hitGenerator.processPlayback(false);
            Map<String, Object> qoeData = MediaCollectionHelper.extractQoEData(mediaContext);

            Map<String, Object> expectedQoEParams = new HashMap<>();
            expectedQoEParams.put(MediaCollectionTestConstants.QoE.BITRATE.key, 1L);
            expectedQoEParams.put(MediaCollectionTestConstants.QoE.DROPPED_FRAMES.key, 2L);
            expectedQoEParams.put(MediaCollectionTestConstants.QoE.FPS.key, 3L);
            expectedQoEParams.put(MediaCollectionTestConstants.QoE.STARTUP_TIME.key, 4L);

            assertEquals(expectedQoEParams, qoeData);

            assertEquals(1, hitProcessor.hitCountfromActiveSession());
            MediaHit expectedHit2 =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.PLAY,
                            emptyParams,
                            emptyMetadata,
                            qoeData,
                            0,
                            0);

            assertEquals(expectedHit2, hitProcessor.getHitFromActiveSession(0));
            hitProcessor.clearHitsFromActionSession();
        }
    }

    @Test
    public void test_processPlaybackSameState() {
        mediaContext.enterState(MediaPlayBackState.Play);
        hitGenerator.processPlayback(false);

        assertEquals(1, hitProcessor.hitCountfromActiveSession());

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.PLAY,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
        hitProcessor.clearHitsFromActionSession();

        hitGenerator.processPlayback(false);
        assertEquals(0, hitProcessor.hitCountfromActiveSession());

        hitGenerator.setRefTS(1000);
        hitGenerator.processPlayback(false);
        assertEquals(0, hitProcessor.hitCountfromActiveSession());
    }

    @Test
    public void test_processPlaybackSameStateOnline() {
        config.put(MediaTestConstants.EventDataKeys.Config.DOWNLOADED_CONTENT, false);
        hitGenerator =
                new MediaCollectionHitGenerator(
                        mediaContext, hitProcessor, config, 0, refSessionId);

        mediaContext.enterState(MediaPlayBackState.Play);
        hitGenerator.processPlayback(false);

        assertEquals(1, hitProcessor.hitCountfromActiveSession());

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.PLAY,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
        hitProcessor.clearHitsFromActionSession();

        hitGenerator.processPlayback(false);
        assertEquals(0, hitProcessor.hitCountfromActiveSession());

        hitGenerator.setRefTS(10000);
        hitGenerator.processPlayback(false);
        assertEquals(1, hitProcessor.hitCountfromActiveSession());
    }

    @Test
    public void test_processPlaybackSameStateInit() {
        hitGenerator.processPlayback(false);

        assertEquals(0, hitProcessor.hitCountfromActiveSession());

        hitGenerator.setRefTS(10000);
        hitGenerator.processPlayback(false);
        assertEquals(0, hitProcessor.hitCountfromActiveSession());

        hitGenerator.setRefTS(50000);
        hitGenerator.processPlayback(false);
        assertEquals(1, hitProcessor.hitCountfromActiveSession());

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.PING,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        50000);

        assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
        hitProcessor.clearHitsFromActionSession();
    }

    @Test
    public void test_processPlaybackSameStateInitOnline() {
        config.put(MediaTestConstants.EventDataKeys.Config.DOWNLOADED_CONTENT, false);
        hitGenerator = new MediaCollectionHitGenerator(mediaContext, hitProcessor, config, 0, null);

        hitGenerator.processPlayback(false);

        assertEquals(0, hitProcessor.hitCountfromActiveSession());

        hitGenerator.setRefTS(10000);
        hitGenerator.processPlayback(false);
        assertEquals(1, hitProcessor.hitCountfromActiveSession());

        hitGenerator.setRefTS(50000);
        hitGenerator.processPlayback(false);
        assertEquals(2, hitProcessor.hitCountfromActiveSession());

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.PING,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        10000);

        MediaHit expectedHit2 =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.PING,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        50000);

        assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
        assertEquals(expectedHit2, hitProcessor.getHitFromActiveSession(1));
        hitProcessor.clearHitsFromActionSession();
    }

    @Test
    public void test_processPlaybackSameStateTimeout() {
        mediaContext.enterState(MediaPlayBackState.Play);
        hitGenerator.processPlayback(false);

        assertEquals(1, hitProcessor.hitCountfromActiveSession());

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.PLAY,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
        hitProcessor.clearHitsFromActionSession();

        hitGenerator.setRefTS(50001);
        hitGenerator.processPlayback(false);
        assertEquals(1, hitProcessor.hitCountfromActiveSession());

        expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.PING,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        50001);

        assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
    }

    @Test
    public void test_processPlaybackFlush() {
        mediaContext.enterState(MediaPlayBackState.Play);
        hitGenerator.processPlayback(true);

        assertEquals(1, hitProcessor.hitCountfromActiveSession());

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.PLAY,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
        hitProcessor.clearHitsFromActionSession();

        hitGenerator.processPlayback(true);
        assertEquals(1, hitProcessor.hitCountfromActiveSession());
        expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.PLAY,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
        hitProcessor.clearHitsFromActionSession();

        hitGenerator.setRefTS(10000);
        hitGenerator.processPlayback(true);

        assertEquals(1, hitProcessor.hitCountfromActiveSession());

        expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.PLAY,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        10000);

        assertEquals(expectedHit, hitProcessor.getHitFromActiveSession(0));
    }

    @Test
    public void test_processBitrateChange() {
        hitGenerator.processBitrateChange();

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.BITRATE_CHANGE,
                        emptyParams,
                        emptyMetadata,
                        emptyParams,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_processError() {
        QoEInfo qoeInfo = QoEInfo.create(1.1, 2.2, 3.3, 4.4);
        mediaContext.setQoEInfo(qoeInfo);

        hitGenerator.processError("error-id");

        MediaHit hit = hitProcessor.getHitFromActiveSession(0);

        Map<String, Object> qoeData = MediaCollectionHelper.extractQoEData(mediaContext);
        qoeData.put(MediaCollectionTestConstants.QoE.ERROR_ID.key, "error-id");
        // This is hardcoded
        qoeData.put(
                MediaCollectionTestConstants.QoE.ERROR_SOURCE.key,
                MediaCollectionTestConstants.QoE.ERROR_SOURCE_PLAYER.key);

        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.ERROR,
                        emptyParams,
                        emptyMetadata,
                        qoeData,
                        0,
                        0);

        assertEquals(expectedHit, hit);
    }

    @Test
    public void test_generateHit_processQoEChange() {
        QoEInfo qoeInfo = QoEInfo.create(1, 2, 3, 4);
        mediaContext.setQoEInfo(qoeInfo);

        {
            hitGenerator.processBitrateChange();
            MediaHit hit = hitProcessor.getHitFromActiveSession(0);
            Map<String, Object> qoeData = MediaCollectionHelper.extractQoEData(mediaContext);
            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.BITRATE_CHANGE,
                            emptyParams,
                            emptyMetadata,
                            qoeData,
                            0,
                            0);
            assertEquals(expectedHit, hit);
            hitProcessor.clearHitsFromActionSession();
        }
        {
            hitGenerator.processBitrateChange();
            MediaHit hit = hitProcessor.getHitFromActiveSession(0);
            Map<String, Object> qoeData = MediaCollectionHelper.extractQoEData(mediaContext);
            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.BITRATE_CHANGE,
                            emptyParams,
                            emptyMetadata,
                            qoeData,
                            0,
                            0);
            assertEquals(expectedHit, hit);
            hitProcessor.clearHitsFromActionSession();
        }

        qoeInfo = QoEInfo.create(2.2, 2.3, 3.1, 4.1);
        mediaContext.setQoEInfo(qoeInfo);
        {
            hitGenerator.processBitrateChange();
            MediaHit hit = hitProcessor.getHitFromActiveSession(0);

            Map<String, Object> qoeData = MediaCollectionHelper.extractQoEData(mediaContext);

            Map<String, Object> expectedQoEParams = new HashMap<>();
            expectedQoEParams.put(MediaCollectionTestConstants.QoE.BITRATE.key, 2L);
            expectedQoEParams.put(MediaCollectionTestConstants.QoE.DROPPED_FRAMES.key, 2L);
            expectedQoEParams.put(MediaCollectionTestConstants.QoE.FPS.key, 3L);
            expectedQoEParams.put(MediaCollectionTestConstants.QoE.STARTUP_TIME.key, 4L);

            assertEquals(expectedQoEParams, qoeData);

            MediaHit expectedHit =
                    new MediaHit(
                            MediaCollectionTestConstants.EventType.BITRATE_CHANGE,
                            emptyParams,
                            emptyMetadata,
                            qoeData,
                            0,
                            0);

            assertEquals(expectedHit, hit);
        }
    }

    @Test
    public void test_generateHit_updatedQoEInfo() {
        hitGenerator.generateHit(
                MediaCollectionTestConstants.EventType.PLAY,
                emptyParams,
                emptyMetadata,
                emptyParams);
        Map<String, Object> updatedQoE = new HashMap<>();
        updatedQoE.put(MediaTestConstants.EventDataKeys.QoEInfo.BITRATE, 10000d);
        hitGenerator.generateHit(
                MediaCollectionTestConstants.EventType.PLAY,
                emptyParams,
                emptyMetadata,
                updatedQoE);

        MediaHit actualHit = hitProcessor.getHitFromActiveSession(1);
        MediaHit expectedHit =
                new MediaHit(
                        MediaCollectionTestConstants.EventType.PLAY,
                        emptyParams,
                        emptyMetadata,
                        updatedQoE,
                        0,
                        0);

        assertEquals(expectedHit, actualHit);
    }

    @Test
    public void test_getPlaybackState_mediaContextState() {
        List<MediaPlayBackState> playbackStates = new ArrayList<>();
        playbackStates.add(MediaPlayBackState.Init);
        playbackStates.add(MediaPlayBackState.Play);
        playbackStates.add(MediaPlayBackState.Pause);
        playbackStates.add(MediaPlayBackState.Seek);
        playbackStates.add(MediaPlayBackState.Buffer);
        playbackStates.add(MediaPlayBackState.Stall);

        for (MediaPlayBackState state : playbackStates) {
            mediaContext.enterState(state);
            MediaPlayBackState playBackState = hitGenerator.getPlaybackState();
            assertEquals(state, playBackState);
            mediaContext.exitState(state);
        }
    }

    @Test
    public void test_getMediaCollectionEvent_forMediaPlaybackState() {
        Map<MediaPlayBackState, String> playBackStateToMediaCollectionEventMap = new HashMap<>();
        playBackStateToMediaCollectionEventMap.put(
                MediaPlayBackState.Init, MediaCollectionConstants.EventType.PING);
        playBackStateToMediaCollectionEventMap.put(
                MediaPlayBackState.Play, MediaCollectionConstants.EventType.PLAY);
        playBackStateToMediaCollectionEventMap.put(
                MediaPlayBackState.Pause, MediaCollectionConstants.EventType.PAUSE_START);
        playBackStateToMediaCollectionEventMap.put(
                MediaPlayBackState.Buffer, MediaCollectionConstants.EventType.BUFFER_START);
        playBackStateToMediaCollectionEventMap.put(
                MediaPlayBackState.Seek, MediaCollectionConstants.EventType.PAUSE_START);
        playBackStateToMediaCollectionEventMap.put(
                MediaPlayBackState.Stall, MediaCollectionConstants.EventType.PLAY);

        List<MediaPlayBackState> playbackStates = new ArrayList<>();
        playbackStates.add(MediaPlayBackState.Init);
        playbackStates.add(MediaPlayBackState.Play);
        playbackStates.add(MediaPlayBackState.Pause);
        playbackStates.add(MediaPlayBackState.Seek);
        playbackStates.add(MediaPlayBackState.Buffer);
        playbackStates.add(MediaPlayBackState.Stall);

        for (MediaPlayBackState state : playbackStates) {
            String actual = hitGenerator.getMediaCollectionEvent(state);
            String expected = playBackStateToMediaCollectionEventMap.get(state);
            assertEquals(expected, actual);
        }
    }
}
