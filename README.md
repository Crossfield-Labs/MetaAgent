# MetaAgent

MetaAgent is an Android AI assistant project migrated from Operit.

## Status

This repository is under migration.
The current goal is to retain the original feature architecture except for:
- local model modules
- built-in Ubuntu payloads in git history

## Current Scope

MetaAgent currently retains code for these areas:
- chat and agent interaction
- floating assistant and voice assistant
- AutoGLM mobile automation
- memory base
- workflow automation
- tools and package system
- MCP and Skill related features
- QuickJS runtime
- terminal core, with the Ubuntu runtime archive downloaded from GitHub Release on demand

## Current State

The project has not finished migration cleanup yet.
Known remaining work includes:
- removing residual Operit naming
- polishing migration leftovers and automation behavior

## Build

Open the project root in Android Studio, or run:

```powershell
.\gradlew.bat assembleDebug --no-daemon --stacktrace --console plain
```

## Notes

This README is intentionally minimal during migration.
Large runtime archives are not stored in git. The terminal module downloads the Ubuntu rootfs archive from the latest GitHub Release when it is first needed.
