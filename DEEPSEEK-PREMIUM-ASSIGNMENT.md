# Touch V — DeepSeek Premium Play Store Pass (CURRENT ASSIGNMENT)

This file supersedes the historical skeleton-only instructions in `00-DEEPSEEK-PROMPT.md` and `TOUCH-V-DEEPSEEK-HANDOFF.md` **for scope and visual ownership only**. Those files document earlier development; use `TOUCHV-OFFICIAL-RULES.md` as the authoritative game rules. Preserve the existing approved art.

## Source of truth
- Repository: `mrmundysr-sudo/TouchV`
- Working branch: `deepseek-premium-pass`
- Baseline: the current source on this branch, copied from `claude-premium-playstore-pass`.
- Verify the checked-out repository and branch before changing anything. Never modify `main` or `claude-premium-playstore-pass`. Do not replace the game with a skeleton or placeholder UI.

## Same premium pass planned for Claude
Inspect the full existing Android game and complete a professional, Play Store-ready source polish:
1. Correct game rules, scoring, wilds, melds, draw/discard behavior, 11 rounds, tie-breakers and game-end flow. Preserve Five Crowns-style Touch V identity.
2. Make **1, 2 and 3 AI opponents** feel equally polished. Every AI turn must be perceptible: active portrait **gold turn ring**, brief natural pauses, correct card-count/score changes, audio/visual reactions and no overlapping or skipped turns. Never confuse the human's turn with AI turns.
3. Preserve approved card faces, backs, suit colors, portrait assets and screen backgrounds. Improve layering, sizing, legibility, alignment, clipping, selection, discard-face visibility and sorting. Do not invent or replace art.
4. Improve existing animations, restrained sound effects and haptics, transitions, feedback, and character expression where achievable with current assets.
5. Fix crashes, freezes, state/lifecycle issues, AI decision problems and other code-quality defects without rewriting working systems unnecessarily.
6. Review all screens and flows, especially opponent selection, multi-opponent play, sorting, round scoring, win/loss and restart.

## Work boundaries
- Source-only coding pass: **no APK builds, Android SDK installs, emulators, paid third-party services or paid model calls**. GitHub Actions will build separately.
- Work only on the DeepSeek branch. Commit coherent verified source changes to that branch, keep a clear change log and update `PREMIUM_PASS_PROGRESS.md` with findings, completed fixes, tests that genuinely ran, remaining risks and commit SHAs.
- Never claim a build, on-device test, or GitHub push that did not occur.
- If model/service charges, repository access, or permissions are uncertain, stop and report before proceeding.

## Deliverable
A documented, committed premium-polish source pass on `deepseek-premium-pass` ready for separate GitHub Actions build and APK testing.
