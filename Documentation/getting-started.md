## Getting started

## Before Starting

The Adobe Streaming Media for Edge Network extension has specific configuration requirements for including the Media Collection Details field group in the XDM schema, enabling Media Analytics in a datastream configuration. For more details, please refer to this guide to [configure and set up Adobe Streaming Media for  Edge Network](https://developer.adobe.com/client-sdks/documentation/media-for-edge-network/#configure-and-setup-adobe-streaming-media-for-edge-network).

### Configure and Install Dependencies

The Adobe Streaming Media for Edge Network mobile extension has the following dependencies, which must be installed prior to installing the extension:
- [AEPCore](https://github.com/adobe/aepsdk-core-android)
- [AEPEdge](https://github.com/adobe/aepsdk-edge-android)
- [AEPEdgeIdentity](https://github.com/adobe/aepsdk-edgeidentity-android)

### Configure Media for Edge Network extension in the Data Collection Tags

1. In the Data Collection Tags, select the **Extensions** tab in your mobile property.
2. On the **Catalog** tab, locate the **Adobe Streaming Media for Edge Network** extension, and select **Install**.
3. Type the extension settings for **Channel**, **Player Name**, and **Application Version**.
4. Select **Save**.
5. Follow the publishing process to update your SDK configuration.

### Configure Media for Edge Network extension
Optionally, the Media for Edge Network configuration may be set or changed programmatically.

#### Configuration Keys
| Name | Key | Value | Required |
| --- | --- | --- | --- |
| Channel | "edgeMedia.channel" | String | **Yes** |
| Player Name | "edgeMedia.playerName" | String | **Yes** |
| Application Version | "edgeMedia.appVersion" | String | **No** |

##### Java 
    ```java
    Map<String, Object> mediaConfiguration = new HashMap<>();
    mediaConfiguration.put("edgeMedia.channel", "<YOUR_CHANNEL_NAME>");
    mediaConfiguration.put("edgeMedia.playerName", "<YOUR_PLAYER_NAME>");
    mediaConfiguration.put("edgeMedia.appVersion", "<YOUR_APP_VERSION>");

    MobileCore.updateConfiguration(mediaConfiguration);
    ```

##### Kotlin
    ```koltin
    val mediaConfiguration = mapOf<String, Any>(
        "edgeMedia.channel" to "<YOUR_CHANNEL_NAME>", 
        "edgeMedia.playerName" to "<YOUR_PLAYER_NAME>", 
        "edgeMedia.appVersion" to "<YOUR_APP_VERSION>"
    )
    MobileCore.updateConfiguration(mediaConfiguration)
    ```

----

## Add Media for Edge Network extension to your app

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