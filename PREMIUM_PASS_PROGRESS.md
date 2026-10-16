# Touch V — Premium / Play Store Polish Pass

Branch: `claude-premium-playstore-pass`
Scope: **source-only**. No APK was built, no emulator was run, no Android SDK
tools were installed, and no paid services were invoked during this pass.
GitHub remains the source of truth. `main`/`master` were not touched.

## How this pass was verified locally

The Android SDK / emulator is not available in this environment, so no device
or emulator test was performed. The following *source-level* checks were run:

- Pure-Java engine self-tests (`selftest/run.sh`) — **all pass**
  (`MeldTest`, `GameSim` 3 opponents × 30 seeds, `HumanPathTest`, `TiebreakTest`).
- Full `javac` type-check of every `com.touchv.game` source file against a real
  Android API jar (Robolectric `android-all`), with a generated `R` stub.
  **Compiles clean** (only a pre-existing deprecation note).

These prove the code is well-formed and the rules engine is unchanged in
behaviour. They do **not** substitute for a device/emulator run.

## Completed work

### 1. Result screen — corrupt "Play Again" artwork (asset bug)
- `app/src/main/res/drawable/btn_play_again.png` is **corrupt** in the
  repository: 48 bytes of junk precede the real PNG signature, two IDAT chunk
  headers are destroyed, and the deflate stream is truncated.
  - Confirmed the same bytes ship inside the CI debug APK
    (`res/drawable/btn_play_again.png`, 600,061 bytes) and fail CRC.
  - Confirmed the damage is *inside* the compressed stream (only 246 of 646
    scanlines at 1499×646 RGBA decompress before `Error -3`), so the image is
    **not losslessly recoverable**. It has been corrupt since the asset was
    first added (commit `77de218`).
- Because a broken bitmap would otherwise surface as an empty or crashing
  control, `resultPlate(...)` now loads the drawable defensively. If it fails
  to decode it falls back to a normal labelled **PLAY AGAIN** button, so the
  result screen is always usable.
- **Still needs a fix from whoever owns the approved art**: replace
  `btn_play_again.png` with the original correct PNG. This is the one hard
  Play Store blocker found in the source.

### 2. Turn progression and visibility (1 / 2 / 3 AI opponents)
- The active seat is now always named in the status line
  (`turnLine()`): the human's turn reads `Your turn. …`, and each AI turn reads
  `<Name>'s turn`, so an opponent turn can no longer look like it was skipped.
- Status ink turns gold while it is the human's turn and white otherwise, making
  the hand-off obvious at a glance.
- Existing per-opponent pacing is preserved (one AI action per delayed
  callback, with the opponent given spotlight time). Only the readability of the
  turn changed; the engine call sequence is untouched.

### 3. Gold turn-indicator ring
- Verified the ring is drawn in plate-relative coordinates on a full-screen
  overlay above the plate, so 1/2/3 opponent layouts share one code path.
- The pulse animation is now frame-scheduled via a `Runnable` (`pulseTick`)
  that **stops** when the screen is hidden or no AI is active, instead of an
  unconditional `postInvalidateOnAnimation()`. This removes a continuous redraw
  loop running while the table is off-screen.

### 4. Feedback (sound + haptics)
- Added light haptic ticks on selection/lock/park, and a firmer vibration plus
  a system confirm tone on committed actions (draw, discard, declare out, meld
  laydown). Haptics/sound are wrapped so they can never break a turn.
- No new audio assets were introduced (none exist in the approved pack); the
  tones are the device's standard UI effects and respect the ringer mode.

### 5. Lifecycle
- `onPause()` cancels the pending AI turn and stops the glow pulse so the game
  cannot advance while backgrounded.
- `onResume()` restores fullscreen and re-evaluates the flow.

### 6. Layout / code quality
- `PlateLayout` now skips `GONE` children in measure/layout and clamps sizes to
  `>= 0`, so the many hidden seat hooks no longer force a full plate-sized
  measure pass.
- `startGame()` no longer assumes the table plate is `getChildAt(0)`; it holds a
  direct `tablePlate` reference. Ordering changes can no longer swap the wrong
  asset.
- Accessibility: content descriptions added to buttons, draw/discard piles,
  and every card face (`card.fullLabel()`), plus the existing opponent-count
  descriptions on the title screen.

## Files changed
- `app/src/main/java/com/touchv/game/MainActivity.java`
- `app/src/main/java/com/touchv/game/PlateLayout.java`
- `PREMIUM_PASS_PROGRESS.md` (this file)

Engine classes (`Card`, `Deck`, `Meld`, `Player`, `GameEngine`) were **not**
modified.

## Unverified items (no device/emulator available)
- Actual on-device rendering of the turn ring position/size for 1/2/3
  opponents.
- Sound/haptic behaviour on real hardware.
- The fallback Play Again button appearance on the win/lose plates.
- That no other runtime issue exists beyond the compile + engine checks.

## Blockers / next steps
1. **Replace `btn_play_again.png`** with the correct original artwork
   (asset owner). Until then the app shows the labelled fallback button.
2. **GitHub Actions APK compile**: run the existing
   `.github/workflows/android.yml` (`assembleDebug`/`assembleRelease`) on
   `claude-premium-playstore-pass`. CI already passed before this pass; re-run
   to confirm the source compiles and packages with these changes.
3. **Device/emulator QA**: verify turn sequencing for 1/2/3 opponents, gold ring
   placement, and haptics on a real device or emulator.
4. Optional follow-up: author an original audio pack if branded sound is
   desired (deliberately not invented here, to preserve identity).
