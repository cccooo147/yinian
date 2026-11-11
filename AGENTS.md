# Fast Delivery Protocol

Applies to `D:\zzz`, especially the Android app `D:\zzz\yinian`.
Keep this file short. It is injected into every request.

## Default mode

Use **Fast Lane** unless the user explicitly asks for deep design/architecture.

**For any visual/UI change, HTML first.** Build one self-contained HTML prototype, let the user sign off in the browser, then port it 1:1 to Compose. Do not use Compose/Paparazzi build loops to explore design. HTML is the visual source of truth: layout, color, type, spacing, radius, states, motion. Android-specific safe areas, fonts, IME, gestures, and performance are finalized natively.

Before writing code, produce at most 8 lines:

1. Restate the goal in one sentence.
2. Three acceptance checks.
3. Files likely to change (max 4 for S, 8 for M).
4. Ask at most 2 blocking questions; otherwise choose a default and state the assumption.

No long plan document for a small or medium task.

## Slices and budgets

| Size | Budget | Work |
|---|---|---|
| S | ≤ 30 min, ≤ 3 files | one behavior; compile; one test |
| M | ≤ 90 min, ≤ 8 files | one vertical feature; compile + test; max 1 screenshot round |
| L | split into S/M slices | each slice ends runnable |

- Implement one vertical slice, then compile/test before starting the next.
- Stop when the acceptance checks pass. Park polish in a list; do not implement it.
- If work exceeds 2 hours, stop and report what is done, what is blocked, and options.
- No README, design board, preview image, or marketing material until the code is green. Final handoff docs only when requested.

## Token diet

- Read `AGENTS.md`, the task-relevant files, and the last failure summary only.
- Use `grep`/`glob` for discovery; do not list or read the whole repo.
- Prefer `edit` over `write`. Rewrite a whole file only when it is new or substantially changed.
- Do not re-read files immediately after writing them; read only the changed region if needed.
- Use `scripts/gradle.ps1` for every build. It prints one line on success and the tail on failure. Do not paste environment blocks or full Gradle logs.
- One screenshot per screen, at most two visual rounds. Do not inspect intermediate screenshots or re-read images.
- Web search only for current API/docs; max 2 sources. Do not use web for general design opinions.
- Subagents only for independent research or parallel verification. Do not use them for small edits.
- No generated palette boards, extra design documents, duplicate previews, or source archives unless requested.
- After each slice, write a 3-line state summary: done / evidence / next.

## Definition of done

- Acceptance checks pass.
- `compile` and `test` pass; UI change has one final screenshot; release change has `verify`.
- No known blocker hidden. If no device/emulator is available, say **"真机未验证"**; never fake device results.
- Report exact file paths and evidence, not whole logs.
- If something fails twice, isolate the cause and report options instead of blind retries.

## Build commands

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/gradle.ps1 -Task compile
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/gradle.ps1 -Task test
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/gradle.ps1 -Task screens
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/gradle.ps1 -Task verify
```

`verify` runs tests, lint, release build, signature and zipalign checks.
The script owns JDK/SDK/Gradle/TEMP environment variables.

## Project facts

- App: `D:\zzz\yinian`, package `com.yinian.app`, minSdk 26.
- Current version: read `app/build.gradle.kts`; do not assume.
- APK: `app/build/outputs/apk/release/app-release.apk`.
- Keystore: `keystore/yinian-release.jks`; never print or commit the password.
- Tests: `app/src/test/java/com/yinian/app/AppSmokeTest.kt` and `ScreenshotTest.kt`.
- Snapshots: `app/src/test/snapshots/images/`.
- No git binary on this machine. For risky work, keep a copy of touched files or ask the user; never claim a commit exists.
