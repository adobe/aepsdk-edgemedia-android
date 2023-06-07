# Adobe Streaming Media for Edge Network extension - Android

[![Maven Central](https://img.shields.io/maven-metadata/v.svg?label=EdgeMedia&logo=android&logoColor=white&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fcom%2Fadobe%2Fmarketing%2Fmobile%2Fedgemedia%2Fmaven-metadata.xml)](https://mvnrepository.com/artifact/com.adobe.marketing.mobile/edgemedia)
[![CircleCI](https://img.shields.io/circleci/project/github/adobe/aepsdk-edgemedia-android/main.svg?label=Build&logo=circleci)](https://circleci.com/gh/adobe/workflows/aepsdk-edgemedia-android)
[![Code Coverage](https://img.shields.io/codecov/c/github/adobe/aepsdk-edgemedia-android/main.svg?label=Coverage&logo=codecov)](https://codecov.io/gh/adobe/aepsdk-edgemedia-android/branch/main)

## About this project

The Adobe Streaming Media for Edge Network extension sends data about audio and video consumption on your streaming applications to the Adobe Experience Platform Edge Network. This enables capabilities for measurement, analysis, and activation with media data across the Adobe Experience Cloud solutions when using the [Adobe Experience Platform Mobile SDK](https://developer.adobe.com/client-sdks) and the Edge Network extension.

## Installation

To install and start using the Media for Edge Network extension, check out the [getting started guide](Documentation/getting-started.md) and the [API reference](Documentation/api-reference.md).

## Migrating from Media Analytics

Please refer to the [Migrating from AEPMedia to AEPEdgeMedia](Documentation/migration-guide.md) guide.

### Development

**Open the project**

To open and run the project, open the `code/settings.gradle` file in Android Studio

### Development

#### Run the test application

To configure and run the test app for this project, follow the [getting started guide for the test app](Documentation/getting-started-test-app.md).

#### Code format

This project uses the code formatting tools [Spotless](https://github.com/diffplug/spotless/tree/main/plugin-gradle) with [Prettier](https://prettier.io/). Formatting is applied when the project is built from Gradle and is checked when changes are submitted to the CI build system.

Prettier requires [Node version](https://nodejs.org/en/download/releases/) 10+
To enable the Git pre-commit hook to apply code formatting on each commit, run the following to update the project's git config `core.hooksPath`:
```
make init
```

## Related Projects

| Project                                                      | Description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| [Core extensions](https://github.com/adobe/aepsdk-core-android)                                    | The Mobile Core represents the foundation of the Adobe Experience Platform Mobile SDK. |
| [Edge Network extension](https://github.com/adobe/aepsdk-edge-android) | The Edge Network extension allows you to send data to the Adobe Edge Network from a mobile application. |
| [Assurance extension](https://github.com/adobe/aepsdk-assurance-android) | The Assurance extension enables validation workflows for your SDK implementation.                |

## Documentation

Information about Adobe Streaming Media for Edge Network implementation, API usage, and architecture can be found in the [Documentation](Documentation) directory.

Learn more about Media for Edge Network and all other Mobile SDK extensions in the official [Adobe Experience Platform Mobile SDK documentation](https://developer.adobe.com/client-sdks).

## Contributing

Contributions are welcomed! Read the [Contributing Guide](./.github/CONTRIBUTING.md) for more information.

## Licensing

This project is licensed under the Apache V2 License. See [LICENSE](LICENSE) for more information.
