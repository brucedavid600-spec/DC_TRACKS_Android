# DC TRACKS Android

A modern Android starter app for tracking activities, routes, and progress.

## What this app includes

- a dashboard/list of tracks
- add/edit/delete actions
- status badges
- form-based track management
- Jetpack Compose UI

## Open and run

1. Open this repository in Android Studio Ladybug or newer.
2. Let Gradle sync and install the Android SDK if prompted.
3. Select an emulator or connected device.
4. Click Run.

This starter is ready for adding real networking, persistence, geolocation, or analytics features.

## Main app concept

DC TRACKS is intended to manage activity entries such as:
- runs
- rides
- hikes
- route reviews
- training sessions

You can expand the app by connecting it to a local database, remote API, or map service.

## Default screen

The initial version includes:
- a list of sample tracks
- buttons to edit or delete entries
- a floating action button to add a new track
- a form screen for entering track details

## Next enhancements

- save data locally with Room
- add a search/filter feature
- add map or GPS support
- attach photos or notes
- export/import data
- connect to Firebase or backend API

## Project structure

- app/src/main/java/com/example/dctracks/MainActivity.kt
- app/src/main/AndroidManifest.xml
- app/build.gradle.kts

## Notes

This app is intentionally simple and easy to extend for a real product.
Software design and screens can be expanded quickly from here.
