## Migrating from Media to Media for Edge Network

This is the complete migration guide from Media to Media for Edge Network extension.

| Quick Links |
| --- |
| [Configuration](#configuration)  |
| [Add extensions to your app](#add-the-edgemedia-extension-to-your-app) <ul> <li>[Dependencies](#dependencies)<li> [Download extension with dependencies](#download-extension-with-dependencies) <li> [Import and register extensions](#import-and-register-extensions) </ul> |
| [Granular ad tracking](#granular-ad-tracking)  |
| [Downloaded content tracking](#downloaded-content-tracking)  |
| [API Reference](#api-reference)|

------

## Configuration 

### Media
| Name | Key | Value | Required |
| --- | --- | --- | --- |
| Collection API Server | "media.trackingServer" | String | Yes |
| Channel | "media.channel" | String | No |
| Player Name | "media.playerName" | String | No |
| Application Version | "media.appVersion" | String | No |

### Media for Edge Network
| Name | Key | Value | Required |
| --- | --- | --- | --- |
| Channel | "edgeMedia.channel" | String | Yes |
| Player Name | "edgeMedia.playerName" | String | Yes |
| Application Version | "edgeMedia.appVersion" | String | No |

Please refer [Media for Edge Network configuration](getting-started.md/#configuration) for more details.

------

## Add the Media for Edge Network extension to your app

### Dependencies

| Media | EdgeMedia|
| --- | --- |
|```Core, Identity, Analytics```|```Core, Edge, EdgeIdentity```|

------

### Download extension with dependencies

#### Using [Maven](https://maven.apache.org/) & [Gradle](https://gradle.org/)

```diff
implementation 'com.adobe.marketing.mobile:core:2.+'
- implementation 'com.adobe.marketing.mobile:identity:2.+'
- implementation 'com.adobe.marketing.mobile:analytics:2.+'
- implementation 'com.adobe.marketing.mobile:media:3.+'
+ implementation 'com.adobe.marketing.mobile:edge:2.+'
+ implementation 'com.adobe.marketing.mobile:edgeidentity:2.+'
+ implementation 'com.adobe.marketing.mobile:edgemedia:2.+'
```

> **Warning**  
> Using dynamic dependency versions is not recommended for production apps. Refer to this [page](https://github.com/adobe/aepsdk-core-android/blob/main/Documentation/MobileCore/gradle-dependencies.md) for managing Gradle dependencies.

------

### Import and register extensions

##### Java

```diff
import com.adobe.marketing.mobile.MobileCore;
- import com.adobe.marketing.mobile.Identity;
- import com.adobe.marketing.mobile.Analytics;
- import com.adobe.marketing.mobile.Media;
+ import com.adobe.marketing.mobile.Edge;
+ import com.adobe.marketing.mobile.edge.identity.Identity;
+ import com.adobe.marketing.mobile.edge.media.Media;
```

##### Java

```diff
public class MainApp extends Application {
    private final String ENVIRONMENT_FILE_ID = "YOUR_APP_ENVIRONMENT_ID";

    @Override
    public void onCreate() {
        super.onCreate();

        MobileCore.setApplication(this);
        MobileCore.configureWithAppID(ENVIRONMENT_FILE_ID);

-        List<Class<? extends Extension>> extensions = Arrays.asList(
-                Media.EXTENSION, Analytics.EXTENSION, Identity.EXTENSION);
+       List<Class<? extends Extension>> extensions = Arrays.asList(
+                Media.EXTENSION, Edge.EXTENSION, Identity.EXTENSION);
        MobileCore.registerExtensions(extensions, o -> {
            Log.d(LOG_TAG, "AEP Mobile SDK is initialized");
        });
    }
}
```


##### Kotlin

```diff
import com.adobe.marketing.mobile.MobileCore
- import com.adobe.marketing.mobile.Identity
- import com.adobe.marketing.mobile.Analytics
- import com.adobe.marketing.mobile.Media
+ import com.adobe.marketing.mobile.Edge
+ import com.adobe.marketing.mobile.edge.identity.Identity
+ import com.adobe.marketing.mobile.edge.media.Media
```

##### Kotlin

```diff
class MyApp : Application() {
    val ENVIRONMENT_FILE_ID = "YOUR_APP_ENVIRONMENT_ID"
    
    override fun onCreate() {
        super.onCreate()
        MobileCore.setApplication(this)
        MobileCore.configureWithAppID(ENVIRONMENT_FILE_ID)

-        val extensions = listOf(Media.EXTENSION, Analytics.EXTENSION, Identity.EXTENSION)
+        val extensions = listOf(Media.EXTENSION, Edge.EXTENSION, Identity.EXTENSION)
        MobileCore.registerExtensions(extensions) {
            Log.d(LOG_TAG, "AEP Mobile SDK is initialized")
        }
    }
}
```

<details>
  <summary>Including both Media and Media for Edge Network in the same application?</summary>
  </br>
  <p>Both Media and Media for Edge Network extensions may be used in the same application if needed. Use the Java package name when referring to the extension class or public APIs to differentiate between the two extensions.</p>

**Example**

##### Java

```diff
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.Analytics;
import com.adobe.marketing.mobile.Media;
import com.adobe.marketing.mobile.Edge;
import com.adobe.marketing.mobile.edge.identity.Identity;
import com.adobe.marketing.mobile.edge.media.Media;
```

##### Java

```diff
public class MainApp extends Application {
    private final String ENVIRONMENT_FILE_ID = "YOUR_APP_ENVIRONMENT_ID";

    @Override
    public void onCreate() {
        super.onCreate();

        MobileCore.setApplication(this);
        MobileCore.configureWithAppID(ENVIRONMENT_FILE_ID);

        List<Class<? extends Extension>> extensions = Arrays.asList(
            com.adobe.marketing.mobile.Media.EXTENSION, 
            com.adobe.marketing.mobile.edge.media.Media.EXTENSION, 
            Analytics.EXTENSION, 
            Edge.EXTENSION, 
            com.adobe.marketing.mobile.Identity.EXTENSION, 
            com.adobe.marketing.mobile.edge.identity.Identity.EXTENSION
        );
        MobileCore.registerExtensions(extensions, o -> {
            Log.d(LOG_TAG, "AEP Mobile SDK is initialized");
        });
    }
}
```


##### Kotlin

```diff
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.Identity
import com.adobe.marketing.mobile.Analytics
import com.adobe.marketing.mobile.Media
import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.edge.identity.Identity
import com.adobe.marketing.mobile.edge.media.Media
```

##### Kotlin

```diff
class MyApp : Application() {
    val ENVIRONMENT_FILE_ID = "YOUR_APP_ENVIRONMENT_ID"
    
    override fun onCreate() {
        super.onCreate()
        MobileCore.setApplication(this)
        MobileCore.configureWithAppID(ENVIRONMENT_FILE_ID)

        val extensions = listOf(
            com.adobe.marketing.mobile.Media.EXTENSION, 
            com.adobe.marketing.mobile.edge.media.Media.EXTENSION, 
            Analytics.EXTENSION, 
            Edge.EXTENSION, 
            com.adobe.marketing.mobile.Identity.EXTENSION, 
            com.adobe.marketing.mobile.edge.identity.Identity.EXTENSION
        )
        MobileCore.registerExtensions(extensions) {
            Log.d(LOG_TAG, "AEP Mobile SDK is initialized")
        }
    }
}
```
</details>

------

### Public API changes

#### package name

The Media for Edge Network extension uses the same class names as the Media Analytics extension. The Java package, however, is changed to `com.adobe.marketing.mobile.edge.media`. 

```diff
- import com.adobe.marketing.mobile.Media;
- import com.adobe.marketing.mobile.MediaConstants;
- import com.adobe.marketing.mobile.MediaTracker;
+ import com.adobe.marketing.mobile.edge.media.Media;
+ import com.adobe.marketing.mobile.edge.media.MediaConstants;
+ import com.adobe.marketing.mobile.edge.media.MediaTracker;
```

All numeric parameters of the public APIs have been updated to type integer.

#### MediaConstants.TrackerConfig

The Media Analytics and Media for Edge Network extensions both provide convenience constants when setting the tracker configuration. The Media Analytics extension named this class `Config`, while the Media for Edge Network extension names this class `TrackerConfig`.
```diff
- MediaConstants.Config
+ MediaConstants.TrackerConfig
```

#### createMediaObject
```diff
- public static HashMap<String, Object> createMediaObject(String name,
-                                                         String id,
-                                                         double length,
-                                                         String streamType,
-                                                         MediaType mediaType);
+ public static HashMap<String, Object> createMediaObject(String name,
+                                                         String id,
+                                                         int length,
+                                                         String streamType,
+                                                         MediaType mediaType);
```

#### createAdBreakObject
```diff
- public static HashMap<String, Object> createAdBreakObject(String name, long position, double startTime);
+ public static HashMap<String, Object> createAdBreakObject(String name, int position, int startTime);
```

#### createAdObject
```diff
- public static HashMap<String, Object> createAdObject(String name, String id, long position, double length);
+ public static HashMap<String, Object> createAdObject(String name, String id, int position, int length);
```

#### createChapterObject
```diff
- public static HashMap<String, Object> createChapterObject(String name,
-                                                           long position,
-                                                           double length,
-                                                           double startTime);
+ public static HashMap<String, Object> createChapterObject(String name,
+                                                           int position,
+                                                           int length,
+                                                           int startTime);
```

#### createQoeObject
```diff
- public static HashMap<String, Object> createQoEObject(long bitrate,
-                                                       double startupTime,
-                                                       double fps,
-                                                       long droppedFrames);
+ public static HashMap<String, Object> createQoEObject(int bitrate,
+                                                       int startupTime,
+                                                       int fps,
+                                                       int droppedFrames);
```

#### updateCurrentPlayhead
```diff
- public void updateCurrentPlayhead(double time);
+ public void updateCurrentPlayhead(int time);
```

------

### Granular ad tracking

Media extension allowed for ad content tracking of `1 second` when setting the `MediaConstants.MediaObjectKey.GRANULAR_AD_TRACKING` key in the media object. Media for Edge Network extension is even more customizable and now the ad content tracking interval can be set using the tracker configuration to a value between `[1-10] seconds`. For more details, refer to the [createTrackerWithConfig API](api-reference.md/#createTrackerWithConfig).

```diff
- MediaTracker tracker = Media.createTracker()
+ HashMap<String, Object> trackerConfig = new HashMap<>();
+ trackerConfig.put(MediaConstants.TrackerConfig.AD_PING_INTERVAL, 1);
+ MediaTracker tracker = Media.createTrackerWith(trackerConfig);

HashMap<String, Object> mediaObject = Media.createMediaObject("name", "id", 30, "vod", Media.MediaType.Video);
- mediaObject.put(MediaConstants.MediaObjectKey.GRANULAR_AD_TRACKING, true)

HashMap<String, String> videoMetadata = new HashMap<String, String>();
videoMetadata.put(MediaConstants.VideoMetadataKeys.EPISODE, "Sample Episode");
videoMetadata.put(MediaConstants.VideoMetadataKeys.SHOW, "Sample Show");

tracker.trackSessionStart(mediaObject, videoMetadata)
```
------

### Downloaded content tracking

Media extension supports offline tracking for downloaded videos by setting the `MediaConstants.Config.DOWNLOADED_CONTENT` key in the tracker configuration and calling `createTrackerWithConfig` API. 

Media for Edge Network extension currently does not support this workflow. 

------

## API reference
The Media for Edge Network extension has similar APIs like Media extension. Please refer the [API reference docs](api-reference.md) to check out the APIs and their usage.

------