# 20 Questions 64-Bit Neural Device (Android)

A retro round 64-bit handheld electronic device for Android built with modern **Kotlin** and **Jetpack Compose**. The device reads your mind by guessing what you're thinking within 20 questions or fewer using custom difference-penalty AI learning logic, minimax information-jump algorithms, and tactile hardware controls.

## Key Features

- **Retro Round Handheld Console Chassis**: Authentic circular physical hardware styling with dark metallic bezel, speaker grilles, screw rivets, and 4 backlight themes (Emerald, Amber, Cyan, Ruby).
- **Illuminated LCD Screen**: CRT scanlines, 8-bit dynamic dot-matrix avatar expressions (`[ • ~ • ]`, `[ ¬ ‿ ¬ ]`, `[ o _ O ]`, `[ ⌁ DEDUCING ⌁ ]`), real-time confidence gauge, and candidate telemetry.
- **Minimax Information-Jump Algorithm**: Evaluates unasked questions across plausible surviving candidates to select the optimal partition discriminator:
  $$\text{worst\_case} = \min(\text{yes\_weight}, \text{no\_weight})$$
- **Strict Branch Pruning**: Eliminates redundant questions. Answering NO to living prunes 30+ biological and animal questions immediately.
- **Difference-Penalty Math**:
  $$\text{scores}[obj] -= |\text{weight} - \text{obj\_attribute}|$$
- **Gated Neural Expansion (Learning Mode)**: When the device is stumped, users teach it their secret item and a distinguishing question. Gating ensures the new question is only asked for that specific target pair, preventing hallucinations and knowledge drift.
- **Integrated Memory Datasets**:
  - `MASTER`: Expanded 120-object knowledge core with custom neural learning.
  - `BIG.CSV`: 100-object benchmark dataset.
  - `MEDIUM.CSV`: 13-object benchmark dataset.
  - `SMALL.CSV`: 12-object benchmark dataset.
  - `MICRO.CSV`: 5-object minimal dataset.
- **8-Bit Synthetic AudioTrack Sound Engine**: Real-time procedural PCM synthesizer producing retro tactile clicks, affirmative rising YES tones, descending square NO buzzers, SOMETIMES warbles, UNKNOWN blips, and celebratory victory fanfares.
- **Neural Memory Inspector**: Inspect objects, question attribute weights, active gates, and candidate ranking penalties.
- **Accessible & Adaptive**: Window size class support for vertical handhelds, foldables, and tablets. Minimum 48dp touch targets and Compose `testTag` IDs throughout.

## Tech Stack & Architecture

- **Language**: Kotlin 2.1.0
- **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
- **Architecture**: Clean MVVM with StateFlow and Coroutines
- **Storage**: Asset-bundled datasets + SharedPreferences JSON persistence for learned memory
- **Audio**: Low-latency PCM 16-bit synthesis via Android `AudioTrack`
- **Build System**: Gradle 9.3.1 with Kotlin DSL (`build.gradle.kts`)
