# Windows Dev Bootstrap

This folder contains helper scripts for setting up MetaAgent on Windows.

## Scripts

- `windows-doctor.ps1`
  - Checks Java, Android SDK, `sdkmanager`, `adb`, network reachability, and project files.
- `windows-bootstrap.ps1`
  - Chooses JDK 17
  - Verifies Android SDK and installs required packages
  - Generates/updates `local.properties`
  - Syncs git submodules
  - Builds `assembleDebug`
  - Optionally installs and launches the APK on a connected device

## Quick Start

Run from repo root (`D:\MetaAgent`):

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\dev\windows-doctor.ps1
powershell -ExecutionPolicy Bypass -File .\tools\dev\windows-bootstrap.ps1
```

With APK install and launch:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\dev\windows-bootstrap.ps1 -InstallApk -LaunchApp
```

## Required Android SDK Packages

The bootstrap script installs these packages:

- `platform-tools`
- `platforms;android-34`
- `build-tools;34.0.0`
- `ndk;27.0.12077973`
- `cmake;3.22.1`

