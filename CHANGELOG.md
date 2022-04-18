# Changelog

## 5.0.0

Major breaking update - [Migration guide](https://github.com/optimove-tech/Optimove-SDK-Android/wiki/Migration-guide-from-4.x-to-5.x)

- Deprecated OptiPush and implemented OptiMobile integration
- Added In-App Messaging, Message Inbox and Deferred Deep Linking features

## 4.2.0

- Changed the target sdk version to 31

## 4.1.2

- Fixed a background initialization crash when the SDK tries to start from a corrupted local configuration file

## 4.1.1

- Fixed a crash on start due to missing r8 rules

## 4.1.0

- Updated FCM and Play Services dependencies

## 4.0.1

- Migrated to Maven Central
- Fixed a crash when an event reported without params

## 4.0.0

Major breaking update - [Migration guide](https://github.com/optimove-tech/Optimove-SDK-Android/wiki/Migration-guide-from-3.x-to-4.x)
- Added requestId to events.
- Added Optimove proguard rules.
- Fully migrated to AndroidX dependencies.
- Changed the SDK initialization process.
- Changed the token retrieval process.
- Removed the secondary firebase project init.

## 3.4.2
- Fixed a SecurityException crash of the support library by introducing a workaround.

## 3.4.1
- Fixed a bug when a secondary app doesnt receive the token when     upgrading from an APK without Optimove SDK, to an APK with it.

## 3.4.0
- Added support for totally custom notification channels.
- Changed the way we handle deep links.
- Changed the way events are handled - warning events are sent to Realtime.


## 3.3.1
- Added sdk_platform and sdk_version to the metadata of the Optistream event.

## 3.3.0
- Added support for passing failed events to the backend.
- Added eventId to each OptistreamEvent.

## 3.2.2
- Fixed a crash when no webview installed.

## 3.2.1
- Added new channelId mechanism.

## 3.2.0
- Added support for channelId.

## 3.1.0
- Added support for non defined params.
- Fixed a collision between App Links and Deep Links.
- Fixed a potential crash in Samsung devices with Android 5.x.

## 3.0.0

Major breaking update - [Migration guide](https://github.com/optimove-tech/Optimove-SDK-Android/wiki/Migration-guide-from-2.x-to-3.x)

- Added a support of the new Optistream events.
- Added Airship integration.
- Changed persistent layer for events.
- Removed the Matomo SDK dependency.
- Removed deprecated API.
