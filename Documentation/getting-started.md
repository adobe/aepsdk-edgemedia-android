## Getting started

The Adobe Experience Platform Media for Edge Network mobile extension has the following dependencies, which must be installed prior to installing the extension:
- [MobileCore](https://github.com/adobe/aepsdk-core-android)
- [Edge](https://github.com/adobe/aepsdk-edge-android)
- [EdgeIdentity](https://github.com/adobe/aepsdk-edgeidentity-android)

## Configuration

### Configure Dependencies
Configure the Edge, EdgeIdentity extensions in the mobile property using the Data Collection UI.

> **Note** 
> If this is your first time setting up Edge extensions and using Data Collection UI, please follow this [tutorial](https://github.com/adobe/aepsdk-edge-android/tree/main/Documentation/Tutorials) to learn about Adobe Experience Platform and how to setup required schemas, datasets, datastreams and creating mobile property etc.

### Configure Media for Edge Network extension
Currently Media for Edge Network extension doesn't have a Data Collection extension and needs to be configured programmatically.

#### Configuration Keys
| Name | Key | Value | Required |
| --- | --- | --- | --- |
| Channel | "edgemedia.channel" | String | **Yes** |
| Player Name | "edgemedia.playerName" | String | **Yes** |
| Application Version | "edgemedia.appVersion" | String | **No** |

##### Java 
    ```java
    Map<String, Object> mediaConfiguration = new HashMap<>();
    mediaConfiguration.put("edgemedia.channel", "<YOUR_CHANNEL_NAME>");
    mediaConfiguration.put("edgemedia.playerName", "<YOUR_PLAYER_NAME>");
    mediaConfiguration.put("edgemedia.appVersion", "<YOUR_APP_VERSION>");

    MobileCore.updateConfiguration(mediaConfiguration);
    ```

##### Kotlin
    ```koltin
    val mediaConfiguration = mapOf<String, Any>(
        "edgemedia.channel" to "<YOUR_CHANNEL_NAME>", 
        "edgemedia.playerName" to "<YOUR_PLAYER_NAME>", 
        "edgemedia.appVersion" to "<YOUR_APP_VERSION>"
    )
    MobileCore.updateConfiguration(mediaConfiguration)
    ```

## Add Media extension to your app

1. Installation via [Maven](https://maven.apache.org/) & [Gradle](https://gradle.org/) is the easiest and recommended way to get the AEP SDK into your Android app. Add the Mobile Core, Edge, EdgeIdentity, and EdgeMedia extensions to your project using the app's Gradle file:


    ```gradle
    implementation 'com.adobe.marketing.mobile:core:2.+'
    implementation 'com.adobe.marketing.mobile:edge:2.+'
    implementation 'com.adobe.marketing.mobile:edgeidentity:2.+'
    implementation 'com.adobe.marketing.mobile:edgemedia:2.+'
    ```

> **Warning**  
> Using dynamic dependency versions is not recommended for production apps. Refer to this [page](https://github.com/adobe/aepsdk-core-android/blob/main/Documentation/MobileCore/gradle-dependencies.md) for managing Gradle dependencies.

2. Import the libraries:

   ### Java

   ```java
   import com.adobe.marketing.mobile.MobileCore;
   import com.adobe.marketing.mobile.Edge;
   import com.adobe.marketing.mobile.edge.identity.Identity;
   import com.adobe.marketing.mobile.edge.media.Media;
   ```

   ### Kotlin

   ```kotlin
   import com.adobe.marketing.mobile.MobileCore
   import com.adobe.marketing.mobile.Edge
   import com.adobe.marketing.mobile.edge.identity.Identity
   import com.adobe.marketing.mobile.edge.media.Media
   ```

3. Import the Media library into your project and register it with `MobileCore`

   ### Java

   ```java
   public class MainApp extends Application {
        private final String ENVIRONMENT_FILE_ID = "YOUR_APP_ENVIRONMENT_ID";

        @Override
        public void onCreate() {
            super.onCreate();

            MobileCore.setApplication(this);
            MobileCore.configureWithAppID(ENVIRONMENT_FILE_ID);

            List<Class<? extends Extension>> extensions = Arrays.asList(
                    Media.EXTENSION, Edge.EXTENSION, Identity.EXTENSION);
            MobileCore.registerExtensions(extensions, o -> {
                Log.d(LOG_TAG, "AEP Mobile SDK is initialized");
            });
        }
    }
   ```

   ### Kotlin

   ```kotlin
   class MyApp : Application() {
       val ENVIRONMENT_FILE_ID = "YOUR_APP_ENVIRONMENT_ID"
       
       override fun onCreate() {
           super.onCreate()
           MobileCore.setApplication(this)
           MobileCore.configureWithAppID(ENVIRONMENT_FILE_ID)

           val extensions = listOf(Media.EXTENSION, Edge.EXTENSION, Identity.EXTENSION)
           MobileCore.registerExtensions(extensions) {
               Log.d(LOG_TAG, "AEP Mobile SDK is initialized")
           }
       }
   }
   ```

## Next Steps

Get familiar with the various APIs offered by the AEP SDK by checking out the [Media API reference](./api-reference.md).