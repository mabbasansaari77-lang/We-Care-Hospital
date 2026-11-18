# We Care — Hospital Appointment Booking App

An Android app for booking hospital appointments, with separate views for **patients**, **doctors** and **admins**.

Built with Kotlin and Jetpack Compose. Prototype created with Google AI Studio.

## Download

[![Download APK](https://img.shields.io/badge/Download-APK%20v1.0-2ea44f?style=for-the-badge&logo=android&logoColor=white)](https://github.com/mabbasansaari77-lang/We-Care-Hospital/releases/download/v1.0/WeCare-Hospital-v1.0.apk)

Download the APK on an Android phone (Android 7.0 or newer), open it, and allow "Install from unknown sources" if asked. All versions are on the [Releases page](https://github.com/mabbasansaari77-lang/We-Care-Hospital/releases).

## Features

**Patients**
- Sign up, log in, forgot password with OTP
- Search doctors by name, specialty or clinic
- View doctor details, working days and available time slots
- Book an appointment and describe symptoms
- See your appointments, notifications and profile

**Doctors**
- See booking requests and mark them complete or decline
- Write and issue digital prescriptions

**Admins**
- Dashboard with stats and monthly revenue
- Add and manage doctors
- Cancel or reassign appointments to another doctor

## Technology

| Part | Tool |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM (ViewModel + Repository) |
| Local database | Room |
| Network | Retrofit (API URL in `NetworkClient.kt` is a placeholder) |
| Android | minSdk 24, targetSdk 36 |

## Project structure

| Folder | Purpose |
| --- | --- |
| `app/src/main/java/com/example/data` | Room database, entities, DAO, repository |
| `app/src/main/java/com/example/network` | API service and network client |
| `app/src/main/java/com/example/ui/screens` | All app screens |
| `app/src/main/java/com/example/viewmodel` | Appointment ViewModel |

## Run locally

1. Install [Android Studio](https://developer.android.com/studio).
2. Open this folder in Android Studio and let Gradle sync.
3. Copy `.env.example` to `.env` and add your own `GEMINI_API_KEY`. Never commit `.env`.
4. Run the app on an emulator or an Android phone.

## Status

Demo / prototype. The backend API address is a placeholder, so app data is stored locally on the device.

## Author

Muhammad Abbas Ansaari
