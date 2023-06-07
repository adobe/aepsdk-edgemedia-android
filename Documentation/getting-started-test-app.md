# Getting started with the test app

## Data Collection mobile property prerequisites

The test app needs to be configured with a Data Collection mobile property with the following extensions before it can be used:

* [Mobile Core](https://github.com/adobe/aepsdk-core-android) (installed by default)
* [Edge Network](https://github.com/adobe/aepsdk-edge-android)
* [Identity for Edge Network](https://github.com/adobe/aepsdk-edgeidentity-android)
* [Assurance](https://github.com/adobe/aepsdk-assurance-android)

See the [Getting started](./getting-started.md) guide for instructions on setting up a mobile property with the required schema and datastream configurations. The test application is already implemented with the required SDK libraries, so only the first section of the "Getting started" guide is required.

## Run test application

1. In the test app, set your `ENVIRONMENT_FILE_ID` in `MediaTestApp.kt`, as found in the mobile property.
2. Select the `testappkotlin` runnable with the desired emulator and run the program.
