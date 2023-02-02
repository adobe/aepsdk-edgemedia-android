# Getting Started with Media SDK

## Before starting

Media extension depends on the following extensions:
* [Mobile Core and Identity](https://github.com/adobe/aepsdk-core-android)
* [Analytics](https://github.com/adobe/aepsdk-analytics-android) (peer dependency)

## Add Media extension to your app

1. Installation via [Maven](https://maven.apache.org/) & [Gradle](https://gradle.org/) is the easiest and recommended way to get the AEP SDK into your Android app. Add the Mobile Core, Identity, Analytics, and Media extensions to your project using the app's Gradle file:


   ```
   implementation 'com.adobe.marketing.mobile:core:2.+'
   implementation 'com.adobe.marketing.mobile:identity:2.+'
   implementation 'com.adobe.marketing.mobile:analytics:2.+'
   implementation 'com.adobe.marketing.mobile:media:2.+'
   ```

> **Warning**  
> Using dynamic dependency versions is not recommended for production apps. Refer to this [page](https://github.com/adobe/aepsdk-core-android/blob/main/Documentation/MobileCore/gradle-dependencies.md) for managing Gradle dependencies.

2. Import the libraries:

   ### Java

   ```java
   import com.adobe.marketing.mobile.MobileCore;
   import com.adobe.marketing.mobile.Identity;
   import com.adobe.marketing.mobile.Analytics;
   import com.adobe.marketing.mobile.Media;
   ```

   ### Kotlin

   ```kotlin
   import com.adobe.marketing.mobile.MobileCore
   import com.adobe.marketing.mobile.Identity
   import com.adobe.marketing.mobile.Analytics
   import com.adobe.marketing.mobile.Media;
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
                    Media.EXTENSION, Analytics.EXTENSION, Identity.EXTENSION);
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

           val extensions = listOf(Media.EXTENSION, Analytics.EXTENSION, Identity.EXTENSION)
           MobileCore.registerExtensions(extensions) {
               Log.d(LOG_TAG, "AEP Mobile SDK is initialized")
           }
       }
   }
   ```

## Next Steps

Get familiar with the various APIs offered by the AEP SDK by checking out the [Media API reference](./api-reference.md).