# Changelog

## 7.10.1

- Bumped GSON version number to fix a vulnerability issue

## 7.10.0

- Integrated Google Play Install Referrer Library for reliable install source detection and improved DDL attribution

## 7.9.0

- Added retry logic for deferred deep links when clipboard description is unavailable on first attempt
- Removed fingerprinting code

## 7.8.1

- Update to fix memory leak in OptimoveInApp.setDeepLinkHandler

## 7.8.0

Add In-App Message Interceptor API `OptimoveInApp.getInstance().setInAppMessageInterceptor`, basic usage: 
```java
OptimoveInApp.getInstance().setInAppMessageInterceptor((message, decision) -> {
  // Example: decide based on your own logic
  if (/* show conditions */) {
    decision.show();
  } else {
    decision.suppress(); // permanently suppress this message
  }
});
```

Note: If no decision is made within 5s, the SDK auto-suppresses the message. Suppressed messages won’t reappear.

Optional: configure a custom timeout by overriding `getTimeoutMs()`:

```java
OptimoveInApp.getInstance().setInAppMessageInterceptor((message, decision) -> {
  //...
   @Override public long getTimeoutMs() { return 12000L; } // 12s custom timeout
});
```

## 7.7.0

- Updated to use Embedded Messaging V2 endpoints

## 7.6.0

- API to set push notification accent color

## 7.5.0

- Embedded messaging

## 7.4.1

- Fix duplicated events sent when multiple immediate events reported

## 7.4.0

- Add Preference Center feature

## 7.3.1

- Fix: don't set custom sound if provided resource doesn't exist

## 7.3.0

- Support late setting of credentials with partial initialisation

## 7.2.0

- Add ability to pause & resume in-app message display (#38)

## 7.1.1

- Fixed In-App Message null Intent handling
- Fixed the Optistream event to keep the original timestamp
- Updated the push open intent flags assignment to be overridable

## 7.1.0

- In order to support geofencing and beacons, add public API methods to track location and beacon proximity.

## 7.0.1

- Updated the in app web client to explicitly call `SslErrorHandler.cancel`

## 7.0.0

Major breaking update - [Migration guide](https://github.com/optimove-tech/Optimove-SDK-Android/wiki/Migration-guide-from-6.x-to-7.x)

- Updated the target sdk version to 33
- Updated and renamed the pushRegister API to pushRequestDeviceToken which tries to request a notification permission on devices running Android version >= 13
- Updated the push message handlers to return a boolean, indicating whether the message was handled by Optimove

## 6.1.1

- Fixed an error when links are not opening on Android versions higher than 11

## 6.1.0

- Added signOutUser API.
- Added pushUnregister API.

## 6.0.0

- Update OkHttp networking library to v4.

## 5.0.0

Major breaking update - [Migration guide](https://github.com/optimove-tech/Optimove-SDK-Android/wiki/Migration-guide-from-4.x-to-5.x)

- Deprecated OptiPush and implemented new messaging platform
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

- Fixed a bug when a secondary app doesnt receive the token when upgrading from an APK without Optimove SDK, to an APK with it.

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
