## Media API Reference

### extensionVersion

The extensionVersion() API returns the version of the Media extension that is registered with the Mobile Core extension.

#### Syntax
```java
public static String extensionVersion() {
```

#### Example

##### Java
```java
String mediaExtensionVersion = Media.extensionVersion();
```

##### Kotlin 
```kotlin 
val mediaExtensionVersion = Media.extensionVersion()
```

### EXTENSION

Represents a reference to AssuranceExtension.class that can be used to register with MobileCore via its registerExtensions api.

#### Syntax
```java
public static final Class<? extends Extension> EXTENSION = AssuranceExtension.class;
````

#### Example

##### Java
```java
MobileCore.registerExtensions(Arrays.asList(Assurance.EXTENSION, ...), new AdobeCallback<Object>() {
    // implement completion callback
});
```

##### Kotlin
```kotlin
MobileCore.registerExtensions(listOf(Assurance.EXTENSION, ...)){
    // implement completion callback
}
```

### createTracker

Creates a media tracker instance that tracks the playback session. The tracker created should be used to track the streaming content and it sends periodic pings to the media analytics backend.

#### Syntax

```java
public static MediaTracker createTracker()
```

#### Example

##### Java
```java
MediaTracker tracker = Media.createTracker()
```

##### Kotlin
```kotlin
val tracker = Media.createTracker()
```

### createTrackerWithConfig

Creates a media tracker instance based on the configuration to track the playback session.

| Key | Description | Value | Required |
| :--- | :--- | :--- | :---: |
| `config.channel` | Channel name for media. Set this to overwrite the channel name configured in the Data Collection UI for media tracked with this tracker instance. | String | No |
| `config.downloadedcontent` | Creates a tracker instance to track downloaded media. Instead of sending periodic pings, the tracker only sends one ping for the entire content. | Boolean | No |

#### Syntax

```java
public static MediaTracker createTracker(Map<String, Object> config)
```

#### Example

##### Java
```java
HashMap<String, Object> config = new HashMap<String, Object>();
config.put(MediaConstants.Config.CHANNEL, "custom-channel");  // Override channel configured in the Data Collection UI
config.put(MediaConstants.Config.DOWNLOADED_CONTENT, true);   // Creates downloaded content tracker
MediaTracker mediaTracker = Media.createTracker(config);  // Use the instance for tracking media.
```

##### Kotlin
```kotlin
val config = mapOf(
                MediaConstants.Config.CHANNEL to "custom-channel",
                MediaConstants.Config.DOWNLOADED_CONTENT to true
            )
val mediaTracker = Media.createTracker(config) // Use the instance for tracking media.
```

### createMediaObject

Creates an instance of the Media object.

| Variable Name | Description | Required |
| :--- | :--- | :---: |
| `name` | The name of the media | Yes |
| `mediaId` | The unqiue identifier for the media | Yes |
| `length` | The length of the media in seconds | Yes |
| `streamType` | Stream type | Yes |
| `mediaType` | Media type | Yes |


#### Syntax

```java
public static HashMap<String, Object> createMediaObject(String name,
                                                        String mediaId,
                                                        Double length,
                                                        String streamType,
                                                        MediaType mediaType);
```

#### Example

##### Java
```java
HashMap<String, Object> mediaInfo = Media.createMediaObject("video-name",
                                                            "video-id",
                                                            60D,
                                                            MediaConstants.StreamType.VOD,
                                                            Media.MediaType.Video);
```

##### Kotlin
```kotlin
var mediaInfo = Media.createMediaObject("video-name",
                                        "video-id",
                                        60D,
                                        MediaConstants.StreamType.VOD,
                                        Media.MediaType.Video)
```

### createAdBreakObject

Creates an instance of the AdBreak object.

| Variable Name | Description | Required |
| :--- | :--- | :---: |
| `name` | Ad break name such as pre-roll, mid-roll, and post-roll. | Yes |
| `position` | The number position of the ad break within the content, starting with 1. | Yes |
| `startTime` | Playhead value at the start of the ad break. | Yes |

#### Syntax

```java
public static HashMap<String, Object> createAdBreakObject(String name, Long position, Double startTime);
```

#### Example

##### Java
```java
HashMap<String, Object> adBreakObject = Media.createAdBreakObject("adbreak-name", 1L, 0D);
```

##### Kotlin
```kotlin
val adBreakObject = Media.createAdBreakObject("adbreak-name", 1L, 0D)
```

### createAdObject

Creates an instance of the Ad object.

| Variable Name | Description | Required |
| :--- | :--- | :---: |
| `name` | Friendly name of the ad. | Yes |
| `adId` | Unique identifier for the ad. | Yes |
| `position` | The number position of the ad within the ad break, starting with 1. | Yes |
| `length` | Ad length in seconds| Yes |

#### Syntax

```java
public static HashMap<String, Object> createAdObject(String name, String adId, Long position, Double length);
```

#### Example

##### Java
```java
HashMap<String, Object> adInfo = Media.createAdObject("ad-name", "ad-id", 1L, 15D);
```

##### Kotlin
```kotlin
val adInfo = Media.createAdObject("ad-name", "ad-id", 1L, 15D)
```

### createChapterObject

Creates an instance of the Chapter object.

| Variable Name | Description | Required |
| :--- | :--- | :---: |
| `name` | Chapter name | Yes |
| `position` | The number position of the chapter within the content, starting with 1. | Yes |
| `length` | Chapter length in seconds | Yes |
| `startTime` | Playhead value at the start of the chapter | Yes |

#### Syntax

```java
public static HashMap<String, Object> createChapterObject(String name,
                                                          Long position,
                                                          Double length,
                                                          Double startTime);
```

#### Example

##### Java
```java
HashMap<String, Object> chapterInfo = Media.createChapterObject("chapter-name", 1L, 60D, 0D);
```

##### Kotlin
```java
val chapterInfo = Media.createChapterObject("chapter-name", 1L, 60D, 0D)
```

### createQoEObject

Creates an instance of the QoE object.

| Variable Name | Description | Required |
| :--- | :--- | :---: |
| `bitrate` | The bitrate of media in bits per second | Yes |
| `startupTime` | The start up time of media in seconds | Yes |
| `fps` | The current frames per second information | Yes |
| `droppedFrames` | The number of dropped frames so far | Yes |

#### Syntax

```java
public static HashMap<String, Object> createQoEObject(Long bitrate,
                                                      Double startupTime,
                                                      Double fps,
                                                      Long droppedFrames);
```

#### Example

##### Java
```java
HashMap<String, Object> qoeInfo = Media.createQoEObject(10000000L, 2D, 23D, 10D);
```

##### Kotlin
```kotlin
val qoeInfo = Media.createQoEObject(10000000L, 2D, 23D, 10D)
```

### createStateObject

Creates an instance of the Player State object.

| Variable Name | Description | Required |
| :--- | :--- | :---: |
| `name` | State name\(Use Player State constants to track standard player states\) | Yes |

#### Syntax

```java
public static HashMap<String, Object> createStateObject(String stateName);
```

#### Example

##### Java
```java
HashMap<String, Object> playerStateInfo = Media.createStateObject("fullscreen");
```

##### Kotlin
```kotlin
val playerStateInfo = Media.createStateObject("fullscreen")
```

## Media tracker API reference

### trackSessionStart

Tracks the intention to start playback. This starts a tracking session on the media tracker instance. To learn how to resume a previously closed session.

| Variable Name | Description | Required |
| :--- | :--- | :---: |
| `mediaInfo` | Media information created using the createMediaObject method. | Yes |
| `contextData` | Optional Media context data. For standard metadata keys, use standard video constants or standard audio constants. | No |


#### Syntax

```java
public void trackSessionStart(Map<String, Object> mediaInfo, Map<String, String> contextData);
```

#### Example

##### Java
```java
HashMap<String, Object> mediaObject = Media.createMediaObject("media-name", "media-id", 60D, MediaConstants.StreamType.VOD, Media.MediaType.Video);

HashMap<String, String> mediaMetadata = new HashMap<String, String>();
// Standard metadata keys provided by adobe.
mediaMetadata.put(MediaConstants.VideoMetadataKeys.EPISODE, "Sample Episode");
mediaMetadata.put(MediaConstants.VideoMetadataKeys.SHOW, "Sample Show");

// Custom metadata keys
mediaMetadata.put("isUserLoggedIn", "false");
mediaMetadata.put("tvStation", "Sample TV Station");

tracker.trackSessionStart(mediaInfo, mediaMetadata);
```

##### Kotlin
```kotlin
val mediaObject = Media.createMediaObject(
                        "media-name",
                        "media-id",
                        60.0,
                        MediaConstants.StreamType.VOD,
                        Media.MediaType.Video
                    )

val mediaMetadata = HashMap<String, String>()
// Standard metadata keys provided by adobe.
mediaMetadata[MediaConstants.VideoMetadataKeys.EPISODE] = "Sample Episode" 
mediaMetadata[MediaConstants.VideoMetadataKeys.SHOW] = "Sample Show"
// Custom metadata keys
mediaMetadata["isUserLoggedIn"] = "false"
mediaMetadata["tvStation"] = "Sample TV Station"

tracker.trackSessionStart(mediaInfo, mediaMetadata)
```

### trackPlay

Tracks the media play, or resume, after a previous pause.

#### Syntax

```java
public void trackPause();
```

#### Example

##### Java
```java
tracker.trackPause();
```

##### Kotlin
```kotlin
tracker.trackPause()
```

### trackComplete

Tracks media complete. Call this method only when the media has been completely viewed.


#### Syntax

```java
public void trackComplete();
```

#### Example

##### Java
```java
tracker.trackComplete();
```

##### Kotlin
```kotlin
tracker.trackComplete()
```

### trackSessionEnd

Tracks the end of a viewing session. Call this method even if the user does not view the media to completion.

#### Syntax

```java
public void trackSessionEnd();
```

#### Example

##### Java
```java
tracker.trackSessionEnd();
```

##### Kotlin
```kotlin
tracker.trackSessionEnd()
```

### trackError

Tracks an error in media playback.

| Variable Name | Description | Required |
| :--- | :--- | :---: |
| `errorId` | Error Information | Yes |

#### Syntax

```java
public void trackError(String errorId);
```

#### Example

##### Java
```java
tracker.trackError("errorId");
```

##### Kotlin
```kotlin
tracker.trackError("errorId")
```

### trackEvent

Tracks media events.

| Variable Name | Description |
| :--- | :--- |
| `event` | Media event |
| `info` | For an `AdBreakStart` event, the `adBreak` information is created by using the createAdBreakObject method. <br />  For an `AdStart` event, the Ad information is created by using the createAdObject method. <br />  For `ChapterStart` event, the Chapter information is created by using the createChapterObject method. <br />  For `StateStart` and `StateEnd` event, the State information is created by using the createStateObject method. |
| `data` | Optional context data can be provided for `AdStart` and `ChapterStart` events. This is not required for other events. |


#### Syntax

```java
public void trackEvent(Media.Event event, Map<String, Object> info, Map<String, String> data);
```

#### Example

**Tracking player States**

##### Java

```java
// StateStart
  HashMap<String, Object> stateObject = Media.createStateObject("fullscreen");
  tracker.trackEvent(Media.Event.StateStart, stateObject, null);

// StateEnd
  HashMap<String, Object> stateObject = Media.createStateObject("fullscreen");
  tracker.trackEvent(Media.Event.StateEnd, stateObject, null);
```

##### Kotlin
```kotlin
// StateStart
    val stateObject = Media.createStateObject("fullscreen")
    tracker.trackEvent(Media.Event.StateStart, stateObject, null)

// StateEnd
    val stateObject = Media.createStateObject("fullscreen")
    tracker.trackEvent(Media.Event.StateEnd, stateObject, null)`
```


**Tracking ad breaks**

##### Java

```java
// AdBreakStart
  HashMap<String, Object> adBreakObject = Media.createAdBreakObject("adbreak-name", 1L, 0D);
  tracker.trackEvent(Media.Event.AdBreakStart, adBreakObject, null);

// AdBreakComplete
  tracker.trackEvent(Media.Event.AdBreakComplete, null, null);
```

##### Kotlin
```kotlin
// AdBreakStart
    val adBreakObject = Media.createAdBreakObject("adbreak-name", 1L, 0.0)
    tracker.trackEvent(Media.Event.AdBreakStart, adBreakObject, null)

// AdBreakComplete
    tracker.trackEvent(Media.Event.AdBreakComplete, null, null)
```

**Tracking ads**

##### Java

```java
// AdStart
  HashMap<String, Object> adObject = Media.createAdObject("ad-name", "ad-id", 1L, 15D);

  HashMap<String, String> adMetadata = new HashMap<String, String>();
  // Standard metadata keys provided by adobe.
  adMetadata.put(MediaConstants.AdMetadataKeys.ADVERTISER, "Sample Advertiser");
  adMetadata.put(MediaConstants.AdMetadataKeys.CAMPAIGN_ID, "Sample Campaign");
  // Custom metadata keys
  adMetadata.put("affiliate", "Sample affiliate");

  tracker.trackEvent(Media.Event.AdStart, adObject, adMetadata);

// AdComplete
  tracker.trackEvent(Media.Event.AdComplete, null, null);

// AdSkip
  tracker.trackEvent(Media.Event.AdSkip, null, null);
```

##### Kotlin
```kotlin
//AdStart
    val adObject = Media.createAdObject("ad-name", "ad-id", 1L, 15.0)

    val adMetadata = HashMap<String, String>()
    // Standard metadata keys provided by adobe.
    adMetadata[MediaConstants.AdMetadataKeys.ADVERTISER] = "Sample Advertiser"
    adMetadata[MediaConstants.AdMetadataKeys.CAMPAIGN_ID] = "Sample Campaign"
    // Custom metadata keys
    adMetadata["affiliate"] = "Sample affiliate"        
    tracker.trackEvent(Media.Event.AdStart, adObject, adMetadata)

// AdComplete
    tracker.trackEvent(Media.Event.AdComplete, null, null)

// AdSkip
    tracker.trackEvent(Media.Event.AdSkip, null, null)
```

**Tracking chapters**

##### Java

```java
// ChapterStart
  HashMap<String, Object> chapterObject = Media.createChapterObject("chapter-name", 1L, 60D, 0D);

  HashMap<String, String> chapterMetadata = new HashMap<String, String>();
  chapterMetadata.put("segmentType", "Sample segment type");

  tracker.trackEvent(Media.Event.ChapterStart, chapterObject, chapterMetadata);

// ChapterComplete
  tracker.trackEvent(Media.Event.ChapterComplete, null, null);

// ChapterSkip
  tracker.trackEvent(Media.Event.ChapterSkip, null, null);
```

##### Kotlin
```kotlin
// ChapterStart
  val chapterObject = Media.createChapterObject("chapter-name", 1L, 60.0, 0.0)

  val chapterMetadata = HashMap<String, String>()
  chapterMetadata["segmentType"] = "Sample segment type"

  tracker.trackEvent(Media.Event.ChapterStart, chapterObject, chapterMetadata)

// ChapterComplete
  tracker.trackEvent(Media.Event.ChapterComplete, null, null)

// ChapterSkip
  tracker.trackEvent(Media.Event.ChapterSkip, null, null)
```

**Tracking playback events**

##### Java

```java
// BufferStart
   tracker.trackEvent(Media.Event.BufferStart, null, null);

// BufferComplete
   tracker.trackEvent(Media.Event.BufferComplete, null, null);

// SeekStart
   tracker.trackEvent(Media.Event.SeekStart, null, null);

// SeekComplete
   tracker.trackEvent(Media.Event.SeekComplete, null, null);
```

##### Kotlin

```kotlin
// BufferStart
   tracker.trackEvent(Media.Event.BufferStart, null, null)

// BufferComplete
   tracker.trackEvent(Media.Event.BufferComplete, null, null)

// SeekStart
   tracker.trackEvent(Media.Event.SeekStart, null, null)

// SeekComplete
   tracker.trackEvent(Media.Event.SeekComplete, null, null)
```

**Tracking bitrate changes**

##### Java

```java
// If the new bitrate value is available provide it to the tracker.
  HashMap<String, Object> qoeObject = Media.createQoEObject(2000000L, 2D, 25D, 10D);
  tracker.updateQoEObject(qoeObject);

// Bitrate change
  tracker.trackEvent(Media.Event.BitrateChange, null, null);
```

##### Kotlin

```kotlin
// If the new bitrate value is available provide it to the tracker.
  val qoeObject = Media.createQoEObject(2000000L, 2D, 25D, 10D)
  tracker.updateQoEObject(qoeObject)

// Bitrate change
  tracker.trackEvent(Media.Event.BitrateChange, null, null)
```

### updateCurrentPlayhead

Provides the current media playhead to the media tracker instance. For accurate tracking, call this method everytime the playhead changes. If the player does not notify playhead changes, call this method once every second with the most recent playhead.

| Variable Name | Description |
| :--- | :--- |
| `time` | Current playhead in seconds. <br /> <br />For video-on-demand \(VOD\), the value is specified in seconds from the beginning of the media item.<br /> <br />For live streaming, if the player does not provide information about the content duration, the value can be specified as the number of seconds since midnight UTC of that day. <br /> Note: When using progress markers, the content duration is required and the playhead needs to be updated as number of seconds from the beginning of the media item, starting with 0. |

#### Syntax
```java
public void updateCurrentPlayhead(double time);
```

#### Example

##### Java
```java
tracker.updateCurrentPlayhead(1);
```

##### Kotlin
```kotlin
tracker.updateCurrentPlayhead(1)
}
```

**Live streaming example**

##### Java
```java
//Calculation for number of seconds since midnight UTC of the day
double timeFromMidnightInSecond = (System.currentTimeMillis()/1000) % 86400;
tracker.updateCurrentPlayhead(timeFromMidnightInSecond);
```

##### Kotlin
```kotlin
val timeFromMidnightInSecond = (System.currentTimeMillis() / 1000 % 86400).toDouble()
tracker.updateCurrentPlayhead(timeFromMidnightInSecond);
}
```

### updateQoEObject

Provides the media tracker with the current QoE information. For accurate tracking, call this method multiple times when the media player provides the updated QoE information.

| Variable Name | Description |
| :--- | :--- |
| `qoeObject` | Current QoE information that was created by using the createQoEObject method. |


#### Syntax
```java
public void updateQoEObject(Map<String, Object> qoeObject);
```

#### Example

##### Java
```java
HashMap<String, Object> qoeObject = Media.createQoEObject(1000000L, 2D, 25D, 10D);
tracker.updateQoEObject(qoeObject);
```

##### Kotlin
```kotlin
val qoeObject = Media.createQoEObject(1000000L, 2D, 25D, 10D)
tracker.updateQoEObject(qoeObject)
```

## Media Constants

Refer [MediaConstants.java](../code/media/src/phone/java/com/adobe/marketing/mobile/MediaConstants.java) to see the constants exposed by Media extension. 