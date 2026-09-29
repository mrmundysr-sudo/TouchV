# TouchV!

Android build of **TouchV!** (launcher label `Touch V!`).

Java, single Activity, package `com.touchv.game`, minSdk 26, targetSdk 34,
portrait 720x1600 design.

- **Rules / game loop / interaction** come from the frozen milestone source
  (`MilestoneTouchV-source.zip`, the engine that passed 90 games). `Card`,
  `Deck`, `Meld`, `Player`, `GameEngine` are **unchanged**.
- **Visuals** are the supplied plate pack: eight full-screen photos plus a
  transparent Play Again PNG, dropped onto the locked drawable names. Every
  overlay (cards, 1/2/3, score bubble, Play Again, stickies) is a separate view
  on top of the plate, never painted into the bitmap.

This build was made in a copy of the source. The frozen checkpoint zip was not
touched.

---

## 1. Run steps

```bash
# point Gradle at your SDK (local.properties is git-ignored)
echo "sdk.dir=$ANDROID_HOME" > local.properties

export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew :app:assembleDebug
```

Debug APK:

```
app/build/outputs/apk/debug/app-debug.apk
```

Install and launch:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.touchv.game/.MainActivity
```

Open in Android Studio: **Open** -> select this folder -> sync (AGP 8.5.2,
Gradle 8.7, compileSdk 34).

Headless engine tests (no Android SDK needed):

```bash
./selftest/run.sh
```

---

## 2. Screens (eight plates, no ninth)

| Plate | Drawable | When it shows | Overlays on top |
|-------|----------|---------------|-----------------|
| title / wagon-chase | `bg_title` | Launch, and after Play Again | Three 1/2/3 taps in the dirt box under the wagon |
| girl-table | `bg_table_1` | 1 opponent | fan, draw, discard, your score bubble |
| picnic-two | `bg_table_2` | 2 opponents | same four overlays |
| picnic-cat | `bg_table_3` | 3 opponents | same four overlays |
| sort | `bg_sort` | Swipe up from the table | your cards (tap select / swap / deselect) |
| score-card | `bg_score_card` | Tap your score bubble, and at the end of every round | four stickies: girl, boy, cat, you |
| you-win | `bg_you_win` | Human is lowest after round 11 (+ tiebreak) | `btn_play_again` |
| try-again | `bg_try_again` | Human is not lowest | `btn_play_again` (same file) |

Flow:

```
Title (tap 1 / 2 / 3)
  -> matching table plate
  -> 11 rounds on that plate
       swipe up          -> sort overlay -> swipe up back
       tap score bubble  -> score-card   -> swipe up back
       end of round      -> score-card   -> swipe up -> next deal
  -> after round 11 (+ tiebreak if needed): You Win or Try Again
  -> Play Again -> Title
```

There is no separate opponent-pick screen, no generic three-seat felt, and no
opponent clouds. Characters are already in the photos.

---

## 3. Swap a plate (how visuals replace)

1. Replace the file in `app/src/main/res/drawable/` keeping **exactly** the
   locked filename. If you replace a `.jpg` with a `.png` (or the reverse),
   delete the old file, or the build fails on a duplicate resource.
2. Plates are drawn full-screen with `scaleType="fitXY"`. Supply 720x1600
   portrait (any aspect will stretch; 720x1600 keeps the marks aligned).
3. Overlays are positioned as fractions of the plate, so they track the bitmap
   at any screen size. If a new plate moves a hotspot, retune the matching
   `*_BOX` constant at the top of `MainActivity.java` (one place).
4. `btn_play_again` is a transparent PNG. Keep it wide (its natural ratio is
   about 2.3:1).

---

## 4. Every swappable drawable

All of these are files in `res/drawable/`. Swap the file, keep the name.

### Full-screen plates (JPEG, 720x1600 portrait)

| Drawable | Screen | Marks |
|----------|--------|-------|
| `bg_title` | title / wagon-chase | dirt box under wagon = 1/2/3 taps |
| `bg_table_1` | girl-table (1 opponent) | 1 fan, 2 draw, 3 discard, 4 your score bubble |
| `bg_table_2` | picnic-two (2 opponents) | same four |
| `bg_table_3` | picnic-cat (3 opponents) | same four |
| `bg_sort` | sort overlay | 1 your cards |
| `bg_score_card` | score card | 1 girl, 2 boy, 3 cat, 4 you |
| `bg_you_win` | win | 1 Play Again |
| `bg_try_again` | loss | 1 Play Again |

### Overlay bitmap

| Drawable | Notes |
|----------|-------|
| `btn_play_again` | transparent PNG, shared by You Win and Try Again |

### Card art (locked drawable names)

| Drawable | Notes |
|----------|-------|
| `card_back` | official purple/gold TouchV! card back for facedown cards |
| `card_face_green` | clubs |
| `card_face_pink` | hearts |
| `card_face_purple` | fleurs |
| `card_face_orange` | spades |
| `card_face_gold` | diamonds |
| `card_face_joker` | joker |

`card_back` is the approved TouchV! artwork. Card faces remain placeholder
`layer-list` XML (black border, colour field, cream oval); the code draws the
centre pip and corner pips. Replace faces with one bitmap per suit, or a full
named face set, keeping the names. Keep card art near 5:7 so it reads in the
fan.

### Sort-slot backing

| Drawable | Notes |
|----------|-------|
| `sort_slot_back` | landscape-readable TouchV! backing for each of the 13 fixed sort slots; rendered beneath face cards. Keep the portrait drawable name and aspect ratio when replacing it. |

### Chrome / seats / icon (placeholders)

| Drawable | Notes |
|----------|-------|
| `btn_plate` | background of the plain rule buttons |
| `cloud_plate` | your score bubble background |
| `slot_plate` | meld tray chip background |
| `bg_table` | unused fallback felt colour |
| `opp_boy`, `opp_cat`, `opp_girl` | opponent portrait slots (kept for a later pass; not drawn this pass) |
| `icon_fleur` | adaptive launcher icon (`mipmap-anydpi-v26/ic_launcher.xml`) |

---

## 5. Marks used (fractions of the 720x1600 plate)

Overlays are placed with `PlateLayout` using these fractional boxes. They were
read from the supplied marked plates (marked vs unmarked diff).

| Screen | Mark | Box fx, fy, fw, fh |
|--------|------|--------------------|
| title | 1/2/3 taps | 0.182, 0.772, 0.699, 0.116 (split into thirds) |
| table | 1 fan | 0.066, 0.660, 0.872, 0.210 |
| table | 2 draw | 0.377, 0.540, 0.146, 0.090 |
| table | 3 discard | 0.588, 0.540, 0.146, 0.090 |
| table | 4 your bubble | 0.031, 0.462, 0.222, 0.086 |
| sort | 1 cards | 0.150, 0.180, 0.700, 0.640 |
| score-card | 1 girl | 0.120, 0.028, 0.215, 0.092 |
| score-card | 2 boy | 0.632, 0.033, 0.215, 0.092 |
| score-card | 3 cat | 0.124, 0.529, 0.215, 0.092 |
| score-card | 4 you | 0.653, 0.543, 0.215, 0.092 |
| you-win | 1 Play Again | 0.010, 0.706, 0.340, 0.112 |
| try-again | 1 Play Again | 0.020, 0.700, 0.340, 0.112 |

---

## 6. Hook IDs (locked)

Drawables:

```
bg_title      bg_table      bg_table_1    bg_table_2    bg_table_3
bg_sort       bg_score_card bg_you_win    bg_try_again  btn_play_again
card_back     sort_slot_back
card_face_green card_face_pink card_face_purple
card_face_orange card_face_gold card_face_joker
opp_boy       opp_cat       opp_girl      icon_fleur
btn_plate     cloud_plate   slot_plate
```

View IDs:

```
screen_title  screen_setup  screen_table  screen_result
screen_you_win screen_try_again
overlay_scores overlay_melds overlay_sort
fan           fan_top_row   fan_bottom_row
draw_pile     discard_pile  cloud_player
cloud_opp_boy cloud_opp_cat cloud_opp_girl
meld_player   meld_opp_boy  meld_opp_cat  meld_opp_girl
label_round   label_wild    label_status  label_scores
slot_player   slot_opp_boy  slot_opp_cat  slot_opp_girl
portrait_player portrait_opp_boy portrait_opp_cat portrait_opp_girl
count_player  count_opp_boy count_opp_cat count_opp_girl
bg_title_plate
score_girl    score_boy     score_cat     score_player
btn_opp_1     btn_opp_2     btn_opp_3
btn_declare_out btn_lay_melds btn_park_end btn_continue btn_play_again
```

`cloud_opp_*`, `slot_opp_*`, `meld_opp_*`, `portrait_player`, `count_player`
exist and are hidden this pass (no opponent clouds). They are kept so a later
pass can fill them without renaming anything.

---

## 7. Interaction

- **wait_draw** - tap `draw_pile` for a facedown card; tap `discard_pile` with
  no card locked for the top discard. Locked-card taps are ignored until drawn.
- **insert_pending** - the drawn card arrives raised. Tap a gap to slide it in;
  tap the raised card again, or `btn_park_end`, to park it at the end.
- **wait_discard** - tap a parked card to lock/raise it; tap another to hop the
  lock; tap the same card again to deselect. Tap `discard_pile` with a lock to
  discard and end the turn (declaring out instead if the hand qualifies).
- **laying_out** - `btn_declare_out` (going out) and `btn_lay_melds` (final
  turn) auto-detect with the shared validator, then show `overlay_melds`.
- **sort** - swipe up on the felt or the fan. Tap-select, tap another to swap,
  tap the same to deselect. Swipe up to close; the order is frozen.
- **score card** - tap your score bubble, or it opens automatically at the end
  of every round. Swipe up to continue.
- No hold-to-reorder, no flick-to-discard, no second meaning on the same tap.

---

## 8. Rules (unchanged from the frozen engine)

- 116-card deck: two 58-card decks = 5 suits x ranks 3..K (11 ranks) + 3 jokers.
  6 jokers, 22 per suit, no Aces, no Twos, no Uno rules.
- 11 rounds. Round *n* deals *n+2* cards and that count is the Wild rank
  (11 = Jack, 12 = Queen, 13 = King). Jokers are wild every round.
- Values of unused cards: number = face value, J/Q/K = 11/12/13, current wild
  rank = 20, joker = 50.
- Books >= 3 same rank any suits; runs >= 3 consecutive same suit, 3..K, no
  wrap, wilds may fill gaps and sit adjacent.
- Going out: after drawing, exactly `dealt` cards form melds, the extra drawn
  card is the final discard. After someone goes out every other player gets
  exactly one final turn.
- Empty draw pile: keep the top discard, shuffle the rest into a new draw pile.
- Ties: Option 2 only - a 6-card tiebreak round with only the tied seats, 6s
  wild, lowest round score wins, repeat until one player is lowest.

`Meld.java` is the only meld validator, used by going-out, final lay, scoring
and the AI. There is no second copy of the book/run rules.

---

## 9. Source layout

```
app/src/main/java/com/touchv/game/
  Card.java         card data, suits, values, labels        (unchanged)
  Deck.java         116-card build + shuffle                (unchanged)
  Meld.java         the single book/run validator           (unchanged)
  Player.java       seat, hand, laid melds, scores          (unchanged)
  GameEngine.java   rounds, deal, turns, out, score, AI     (unchanged)
  PlateLayout.java  fractional overlay placement (new)
  MainActivity.java eight plates, overlays, interaction
app/src/main/res/
  drawable/         plates, Play Again, card art, chrome
  values/           colors, dimens, strings, ids
  mipmap-anydpi-v26/ic_launcher.xml
selftest/           headless engine tests (no Android SDK needed)
```

---

## 10. Verification performed

- `./gradlew :app:assembleDebug` builds clean (AGP 8.5.2 / Gradle 8.7 / JDK 21).
- `aapt2 dump badging`: package `com.touchv.game`, minSdk 26, targetSdk 34,
  label `TouchV!`.
- Every locked drawable name and view id above is present in the packaged APK.
- `./selftest/run.sh` passes: deck = 116 with 6 jokers and 22 per suit, book and
  run validation, values, going-out partitions, scoring example, 90 full-game
  simulations across 1/2/3 opponents, the human UI call path, and forced
  Option 2 tiebreaks confirming a 6-card deal and a single winner.
- On-device run (emulator, `system-images;android-30;google_apis;x86_64`):
  APK installs and `com.touchv.game/.MainActivity` resumes. Verified live:
  - title plate renders; tapping seat **1** starts a 1-opponent game
  - chrome reads `Round 1 of 11`, `Dealt 3  |  Wild 3 + Jokers`, `You  0`
  - tap draw: hand 3 -> 4, drawn card raised, `btn_park_end` appears,
    draw count 109 -> 108
  - park: status `You must discard one card.`
  - select + tap discard: card leaves the hand, AI turns auto-play
    (`Boy discarded JK.`)
  - tap score bubble: score-card overlay with `score_boy` = `R 0 / T 0`,
    `score_player` = `R 0 / T 0`, `score_girl` / `score_cat` = `N/A`
  - swipe up returns to the table (freezes nothing but closes the card)
  - swipe up on the felt opens `overlay_sort`; tap-select then tap-swap
    reorders `fan_top_row`; swipe up closes it
- The frozen engine and self-tests are byte-identical to the checkpoint
  (`sha256` of `Card/Deck/Meld/Player/GameEngine` matches `MilestoneTouchV-source.zip`).
- Every supplied plate is byte-identical to the pack (`drawable-dropins` vs
  `res/drawable`), so nothing was cropped, rescaled, or generated.
