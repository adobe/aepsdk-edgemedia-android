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

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class MediaCollectionHelperTests {
    Map<String, String> emptyMetadata;
    Map<String, Object> emptyParams;

    public MediaCollectionHelperTests() {
        emptyMetadata = new HashMap<>();
        emptyParams = new HashMap<>();
    }

    @Test
    public void test_extractMediaParams() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60.0, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        Map<String, Object> params = MediaCollectionHelper.extractMediaParams(mediaContext);

        Map<String, Object> expectedParams = new HashMap<>();
        expectedParams.put(MediaCollectionTestConstants.Media.NAME.key, "name");
        expectedParams.put(MediaCollectionTestConstants.Media.ID.key, "id");
        expectedParams.put(MediaCollectionTestConstants.Media.STREAM_TYPE.key, "video");
        expectedParams.put(MediaCollectionTestConstants.Media.CONTENT_TYPE.key, "vod");
        expectedParams.put(MediaCollectionTestConstants.Media.LENGTH.key, 60.0);
        expectedParams.put(MediaCollectionTestConstants.Media.RESUME.key, true);

        assertEquals(expectedParams, params);
    }

    @Test
    public void test_extractMediaParamsWithStandardMetadata() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("k1", "v1");
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SHOW,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SHOW);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SEASON,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SEASON);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.EPISODE,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.EPISODE);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ASSET_ID,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ASSET_ID);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.GENRE,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.GENRE);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FIRST_AIR_DATE,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FIRST_AIR_DATE);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FIRST_DIGITAL_DATE,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FIRST_DIGITAL_DATE);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.RATING,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.RATING);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ORIGINATOR,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ORIGINATOR);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.NETWORK,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.NETWORK);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SHOW_TYPE,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SHOW_TYPE);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AD_LOAD,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AD_LOAD);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.MVPD,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.MVPD);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AUTH,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AUTH);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.DAY_PART,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.DAY_PART);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FEED,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FEED);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.STREAM_FORMAT,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.STREAM_FORMAT);

        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ARTIST,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ARTIST);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ALBUM,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ALBUM);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.LABEL,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.LABEL);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AUTHOR,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AUTHOR);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.STATION,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.STATION);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.PUBLISHER,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.PUBLISHER);

        // Ignore standard ad metadata when parsing media params
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.ADVERTISER,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.ADVERTISER);

        MediaContext mediaContext = new MediaContext(mediaInfo, metadata);

        Map<String, Object> mediaParams = MediaCollectionHelper.extractMediaParams(mediaContext);

        Map<String, Object> expectedMediaParams = new HashMap<>();
        expectedMediaParams.put(MediaCollectionTestConstants.Media.NAME.key, "name");
        expectedMediaParams.put(MediaCollectionTestConstants.Media.ID.key, "id");
        expectedMediaParams.put(MediaCollectionTestConstants.Media.STREAM_TYPE.key, "video");
        expectedMediaParams.put(MediaCollectionTestConstants.Media.CONTENT_TYPE.key, "vod");
        expectedMediaParams.put(MediaCollectionTestConstants.Media.LENGTH.key, 60.0);
        expectedMediaParams.put(MediaCollectionTestConstants.Media.RESUME.key, false);
        expectedMediaParams.put(MediaCollectionTestConstants.Media.NAME.key, "name");
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.SHOW.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SHOW);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.SEASON.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SEASON);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.EPISODE.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.EPISODE);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.ASSET_ID.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ASSET_ID);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.GENRE.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.GENRE);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.FIRST_AIR_DATE.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FIRST_AIR_DATE);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.FIRST_DIGITAL_DATE.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FIRST_DIGITAL_DATE);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.RATING.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.RATING);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.ORIGINATOR.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ORIGINATOR);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.NETWORK.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.NETWORK);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.SHOW_TYPE.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SHOW_TYPE);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.AD_LOAD.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AD_LOAD);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.MVPD.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.MVPD);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.AUTH.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AUTH);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.DAY_PART.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.DAY_PART);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.FEED.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FEED);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.STREAM_FORMAT.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.STREAM_FORMAT);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.ARTIST.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ARTIST);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.ALBUM.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ALBUM);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.LABEL.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.LABEL);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.AUTHOR.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AUTHOR);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.STATION.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.STATION);
        expectedMediaParams.put(
                MediaCollectionTestConstants.StandardMediaMetadata.PUBLISHER.key,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.PUBLISHER);

        assertEquals(expectedMediaParams, mediaParams);
    }

    @Test
    public void test_extractMediaMetadataEmpty() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60.0, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        Map<String, String> mediaMetadata =
                MediaCollectionHelper.extractMediaMetadata(mediaContext);

        assertEquals(emptyMetadata, mediaMetadata);
    }

    @Test
    public void test_extractMediaMetadataNULL() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60.0, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, null);

        Map<String, String> mediaMetadata =
                MediaCollectionHelper.extractMediaMetadata(mediaContext);

        assertEquals(emptyMetadata, mediaMetadata);
    }

    @Test
    public void test_extractMediaMetadata() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60.0, true);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("k1", "v1");

        MediaContext mediaContext = new MediaContext(mediaInfo, metadata);

        Map<String, String> mediaMetadata =
                MediaCollectionHelper.extractMediaMetadata(mediaContext);

        assertEquals(metadata, mediaMetadata);
    }

    @Test
    public void test_extractMediaMetadataWithStandardMetadata() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60.0, true);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("k1", "v1");
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SHOW,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SHOW);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SEASON,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SEASON);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.EPISODE,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.EPISODE);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ASSET_ID,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ASSET_ID);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.GENRE,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.GENRE);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FIRST_AIR_DATE,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FIRST_AIR_DATE);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FIRST_DIGITAL_DATE,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FIRST_DIGITAL_DATE);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.RATING,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.RATING);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ORIGINATOR,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ORIGINATOR);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.NETWORK,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.NETWORK);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SHOW_TYPE,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.SHOW_TYPE);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AD_LOAD,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AD_LOAD);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.MVPD,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.MVPD);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AUTH,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AUTH);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.DAY_PART,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.DAY_PART);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FEED,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.FEED);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.STREAM_FORMAT,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.STREAM_FORMAT);

        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ARTIST,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ARTIST);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ALBUM,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.ALBUM);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.LABEL,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.LABEL);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AUTHOR,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.AUTHOR);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.STATION,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.STATION);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.PUBLISHER,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.PUBLISHER);

        // Don't ignore standard ad metadata when parsing media metadata
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.ADVERTISER,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.ADVERTISER);

        MediaContext mediaContext = new MediaContext(mediaInfo, metadata);

        Map<String, String> mediaMetadata =
                MediaCollectionHelper.extractMediaMetadata(mediaContext);

        Map<String, String> expectedMetadata = new HashMap<>();
        expectedMetadata.put("k1", "v1");
        expectedMetadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.ADVERTISER,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.ADVERTISER);

        assertEquals(expectedMetadata, mediaMetadata);
    }

    @Test
    public void test_extractAdBreakParamsAbsent() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        Map<String, Object> adbreakParams =
                MediaCollectionHelper.extractAdBreakParams(mediaContext);

        assertEquals(emptyMetadata, adbreakParams);
    }

    @Test
    public void test_extractAdBreakParams() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        AdBreakInfo adBreakInfo = AdBreakInfo.create("adBreakName", 1, 10);
        mediaContext.setAdBreakInfo(adBreakInfo);

        Map<String, Object> adbreakParams =
                MediaCollectionHelper.extractAdBreakParams(mediaContext);

        Map<String, Object> expectedParams = new HashMap<>();
        expectedParams.put(
                MediaCollectionTestConstants.AdBreak.POD_FRIENDLY_NAME.key, "adBreakName");
        expectedParams.put(MediaCollectionTestConstants.AdBreak.POD_INDEX.key, 1L);
        expectedParams.put(MediaCollectionTestConstants.AdBreak.POD_SECOND.key, 10.0);

        assertEquals(expectedParams, adbreakParams);
    }

    @Test
    public void test_extractAdParamsAbsent() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        Map<String, Object> adParams = MediaCollectionHelper.extractAdParams(mediaContext);

        assertEquals(emptyMetadata, adParams);
    }

    @Test
    public void test_extractAdParams() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        AdInfo adInfo = AdInfo.create("adid", "adname", 1, 30);
        mediaContext.setAdInfo(adInfo, emptyMetadata);

        Map<String, Object> adParams = MediaCollectionHelper.extractAdParams(mediaContext);

        Map<String, Object> expectedAdParams = new HashMap<>();
        expectedAdParams.put(MediaCollectionTestConstants.Ad.ID.key, "adid");
        expectedAdParams.put(MediaCollectionTestConstants.Ad.NAME.key, "adname");
        expectedAdParams.put(MediaCollectionTestConstants.Ad.POD_POSITION.key, 1L);
        expectedAdParams.put(MediaCollectionTestConstants.Ad.LENGTH.key, 30.0);

        assertEquals(expectedAdParams, adParams);
    }

    @Test
    public void test_extractAdParamsWithStandardMetadata() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        AdInfo adInfo = AdInfo.create("adid", "adname", 1, 30);
        Map<String, String> metadata = new HashMap<>();
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.ADVERTISER,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.ADVERTISER);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CAMPAIGN_ID,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CAMPAIGN_ID);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CREATIVE_ID,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CREATIVE_ID);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.SITE_ID,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.SITE_ID);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CREATIVE_URL,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CREATIVE_URL);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.PLACEMENT_ID,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.PLACEMENT_ID);

        // Ignore standard media metadata when parsing ad params
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.PUBLISHER,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.PUBLISHER);

        mediaContext.setAdInfo(adInfo, metadata);

        Map<String, Object> adParams = MediaCollectionHelper.extractAdParams(mediaContext);

        Map<String, Object> expectedAdParams = new HashMap<>();
        expectedAdParams.put(MediaCollectionTestConstants.Ad.ID.key, "adid");
        expectedAdParams.put(MediaCollectionTestConstants.Ad.NAME.key, "adname");
        expectedAdParams.put(MediaCollectionTestConstants.Ad.POD_POSITION.key, 1L);
        expectedAdParams.put(MediaCollectionTestConstants.Ad.LENGTH.key, 30.0);
        expectedAdParams.put(
                MediaCollectionTestConstants.StandardAdMetadata.ADVERTISER.key,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.ADVERTISER);
        expectedAdParams.put(
                MediaCollectionTestConstants.StandardAdMetadata.CAMPAIGN_ID.key,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CAMPAIGN_ID);
        expectedAdParams.put(
                MediaCollectionTestConstants.StandardAdMetadata.CREATIVE_ID.key,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CREATIVE_ID);
        expectedAdParams.put(
                MediaCollectionTestConstants.StandardAdMetadata.SITE_ID.key,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.SITE_ID);
        expectedAdParams.put(
                MediaCollectionTestConstants.StandardAdMetadata.CREATIVE_URL.key,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CREATIVE_URL);
        expectedAdParams.put(
                MediaCollectionTestConstants.StandardAdMetadata.PLACEMENT_ID.key,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.PLACEMENT_ID);

        assertEquals(expectedAdParams, adParams);
    }

    @Test
    public void test_extractAdMetadataAbsent() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        AdInfo adInfo = AdInfo.create("adid", "adname", 1, 30);
        mediaContext.setAdInfo(adInfo, emptyMetadata);

        Map<String, String> adMetadata = MediaCollectionHelper.extractAdMetadata(mediaContext);

        assertEquals(emptyMetadata, adMetadata);
    }

    @Test
    public void test_extractAdMetadataNULL() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, null);

        AdInfo adInfo = AdInfo.create("adid", "adname", 1, 30);
        mediaContext.setAdInfo(adInfo, null);

        Map<String, String> adMetadata = MediaCollectionHelper.extractAdMetadata(mediaContext);

        assertEquals(emptyMetadata, adMetadata);
    }

    @Test
    public void test_extractAdMetadata() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        AdInfo adInfo = AdInfo.create("adid", "adname", 1, 30);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("k1", "v1");

        mediaContext.setAdInfo(adInfo, metadata);

        Map<String, String> adMetadata = MediaCollectionHelper.extractAdMetadata(mediaContext);

        Map<String, String> expectedMetadata = new HashMap<>();
        expectedMetadata.put("k1", "v1");

        assertEquals(expectedMetadata, adMetadata);
    }

    @Test
    public void test_extractAdMetadataWithStandardMetadata() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        AdInfo adInfo = AdInfo.create("adid", "adname", 1, 30);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("k1", "v1");
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.ADVERTISER,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.ADVERTISER);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CAMPAIGN_ID,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CAMPAIGN_ID);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CREATIVE_ID,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CREATIVE_ID);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.SITE_ID,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.SITE_ID);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CREATIVE_URL,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.CREATIVE_URL);
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardAdMetadata.PLACEMENT_ID,
                MediaTestConstants.EventDataKeys.StandardAdMetadata.PLACEMENT_ID);

        // Don't ignore standard media metadata when parsing ad metadata
        metadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.PUBLISHER,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.PUBLISHER);

        mediaContext.setAdInfo(adInfo, metadata);

        Map<String, String> adMetadata = MediaCollectionHelper.extractAdMetadata(mediaContext);

        Map<String, String> expectedMetadata = new HashMap<>();
        expectedMetadata.put("k1", "v1");
        expectedMetadata.put(
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.PUBLISHER,
                MediaTestConstants.EventDataKeys.StandardMediaMetadata.PUBLISHER);

        assertEquals(expectedMetadata, adMetadata);
    }

    @Test
    public void test_extractChapterParamsAbsent() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        Map<String, Object> chapterParams =
                MediaCollectionHelper.extractChapterParams(mediaContext);

        assertEquals(emptyParams, chapterParams);
    }

    @Test
    public void test_extractChapterParams() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        ChapterInfo chapterInfo = ChapterInfo.create("chaptername", 1, 10, 30);
        mediaContext.setChapterInfo(chapterInfo, emptyMetadata);

        Map<String, Object> chapterParams =
                MediaCollectionHelper.extractChapterParams(mediaContext);

        Map<String, Object> expectedChapterParams = new HashMap<>();
        expectedChapterParams.put(
                MediaCollectionTestConstants.Chapter.FRIENDLY_NAME.key, "chaptername");
        expectedChapterParams.put(MediaCollectionTestConstants.Chapter.INDEX.key, 1L);
        expectedChapterParams.put(MediaCollectionTestConstants.Chapter.OFFSET.key, 10.0);
        expectedChapterParams.put(MediaCollectionTestConstants.Chapter.LENGTH.key, 30.0);

        assertEquals(expectedChapterParams, chapterParams);
    }

    @Test
    public void test_extractChapterMetadataAbsent() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        ChapterInfo chapterInfo = ChapterInfo.create("chaptername", 1, 10, 30);
        mediaContext.setChapterInfo(chapterInfo, emptyMetadata);

        Map<String, String> chapterMetadata =
                MediaCollectionHelper.extractChapterMetadata(mediaContext);

        assertEquals(emptyMetadata, chapterMetadata);
    }

    @Test
    public void test_extractChapterMetadataNULL() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, null);

        ChapterInfo chapterInfo = ChapterInfo.create("chaptername", 1, 10, 30);
        mediaContext.setChapterInfo(chapterInfo, null);

        Map<String, String> chapterMetadata =
                MediaCollectionHelper.extractChapterMetadata(mediaContext);

        assertEquals(emptyMetadata, chapterMetadata);
    }

    @Test
    public void test_extractChapterMetadata() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        ChapterInfo chapterInfo = ChapterInfo.create("chaptername", 1, 10, 30);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("k1", "v1");

        mediaContext.setChapterInfo(chapterInfo, metadata);

        Map<String, String> chapterMetadata =
                MediaCollectionHelper.extractChapterMetadata(mediaContext);

        Map<String, String> expectedChapterMetadata = new HashMap<>();
        expectedChapterMetadata.put("k1", "v1");

        assertEquals(expectedChapterMetadata, chapterMetadata);
    }

    @Test
    public void test_extractQoEAbsent() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        Map<String, Object> qoeParams = MediaCollectionHelper.extractQoEData(mediaContext);

        assertEquals(emptyParams, qoeParams);
    }

    @Test
    public void test_extractQoE_doubleToLong() {
        MediaInfo mediaInfo = MediaInfo.create("id", "name", "vod", MediaType.Video, 60, true);
        MediaContext mediaContext = new MediaContext(mediaInfo, emptyMetadata);

        QoEInfo qoeInfo = QoEInfo.create(1.1, 2.2, 3.3, 4.4);
        mediaContext.setQoEInfo(qoeInfo);

        Map<String, Object> qoeParams = MediaCollectionHelper.extractQoEData(mediaContext);

        Map<String, Object> expectedQoEParams = new HashMap<>();
        expectedQoEParams.put(MediaCollectionTestConstants.QoE.BITRATE.key, 1L);
        expectedQoEParams.put(MediaCollectionTestConstants.QoE.DROPPED_FRAMES.key, 2L);
        expectedQoEParams.put(MediaCollectionTestConstants.QoE.FPS.key, 3L);
        expectedQoEParams.put(MediaCollectionTestConstants.QoE.STARTUP_TIME.key, 4L);

        assertEquals(expectedQoEParams, qoeParams);
    }
}
