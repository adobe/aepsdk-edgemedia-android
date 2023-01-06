# Adobe Experience Platform - Media extension for Android

[![Maven Central](https://img.shields.io/maven-central/v/com.adobe.marketing.mobile/media.svg?logo=android&logoColor=white&label=media)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/media)

## About this project

The [`Media`](https://developer.adobe.com/client-sdks/documentation/adobe-media-analytics) extension represents the Adobe Experience Platform SDK's Media Analytics extension that provides clients with robust measurement for audio, video and advertisements.

## Prerequisites
The Media extension has the following peer dependencies, which must be installed prior to installing the Media Extension:

- [Mobile Core](https://developer.adobe.com/client-sdks/documentation/mobile-core)
- [Identity](https://developer.adobe.com/client-sdks/documentation/mobile-core/identity)
- [Analytics](https://developer.adobe.com/client-sdks/documentation/adobe-analytics)

## Installing the AEP Media SDK for Android

The AEP SDK supports Android API 19 (Kitkat) and newer.

Installation via [Maven](https://maven.apache.org/) & [Gradle](https://gradle.org/) is the easiest and recommended way to get the AEP SDK into your Android app.  In your `build.gradle` file, include the latest version of following dependencies:

```gradle
implementation 'com.adobe.marketing.mobile:core:2.x.x'
implementation 'com.adobe.marketing.mobile:identity:2.x.x'
implementation 'com.adobe.marketing.mobile:analytics:2.x.x'
implementation 'com.adobe.marketing.mobile:media:3.x.x'
```

## Development

To open and run the project, open the `code/build.gradle` file in Android Studio

## Documentation

Additional documentation for usage and SDK architecture can be found under the [Documentation](Documentation) directory.

## Contributing

Contributions are welcomed! Read the [Contributing Guide](./.github/CONTRIBUTING.md) for more information.

## Licensing

This project is licensed under the Apache V2 License. See [LICENSE](LICENSE) for more information.

