# MetaAgent

MetaAgent is an Android AI assistant project migrated from Operit.

## Status

This repository is under migration.
The current goal is to retain the original feature architecture except for:
- built-in Ubuntu or terminal module
- local model modules

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

## Current State

The project has not finished migration cleanup yet.
Known remaining work includes:
- removing residual Operit naming
- removing references to deleted terminal, MNN, and llama modules
- restoring full build success

## Build

Open the project root in Android Studio, or run:

```powershell
.\gradlew.bat assembleDebug --no-daemon --stacktrace --console plain
```

## Notes

This README is intentionally minimal during migration.
