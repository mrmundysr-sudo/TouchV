# Touch V! — Skeleton Handoff for DeepSeek
**Status:** RULES + UX LOCK for skeleton build. Visual skins out of scope.  
**Date:** 2026-09-27  
**Name everywhere:** `TouchV!` (app label may show `Touch V!`)  
**Pipeline (do not blur):**
- **DeepSeek** = rules, game loop, interaction, placeholder UI only.
- **Grok** = all visuals. Title plate, felt, card faces/backs, opponents, clouds chrome, win/loss, icons. DeepSeek does not draw, generate, or restyle any of that.  
**Do not simplify, reinterpret, or add house rules.** Official rules are copied into §6.

---

## 0. What DeepSeek is allowed to do

- Java, single-Activity Android app.
- Placeholder drawables only: solid color rects, labeled TextViews, simple shapes. Ugly is correct.
- Named IDs Grok will replace 1:1 with bitmaps. Do not rename those IDs.
- Full 11-round loop that can be played with ugly cards.
- No Compose unless required. No pretty animation. No generated “concept art” in res/.
- **Do not** generate, download, or “improve” title art, portraits, felt, card faces, card backs, logos, or win/loss plates. Grok owns every pixel of those.
- If a slot looks empty, leave a colored rect + ID. That is the handoff, not a defect.
- Do **not** copy Touch One / Uno (no color-match discard, no “TOUCH ONE”, no +2 call penalty).

Package: `com.touchv.game`  
Min SDK: 26.

---

## 1. LOCKED — table and hand geometry

Landscape table. `@drawable/bg_table` (placeholder green felt).

**Player hand = two-row fan**

- Top row: up to 7 cards, shallow bow (center higher than wings).
- Bottom row: up to 6 cards, dropped so each card’s **top-left pip stays visible**.
- Round 11 = 13 cards + 1 drawn = 14 in hand until discard. Skeleton: show 13 in the fan; the 14th is the raised insert (or overflow at end of bottom row). Do not invent a third row.
- Early rounds (3–6 cards): use only as much of the fan as needed. Same two-row rules when the hand grows.
- Fan is **play-only** for parked cards. No drag-reseat of parked cards.
- One card locked at a time.
- First tap on a parked card: select + slight raise. Lock must feel like the tap.
- While locked: slide on *other* parked cards hops the lock. Tap raised card or empty felt = deselect.
- **No** hold-to-reorder. **No** flick-to-discard. **No** second meaning on the same-card tap.

**Insert (the card just drawn this turn)**

- Drawn card arrives **raised / unlocked**.
- Slide into a gap and drop to park. Skip parks it at **end of fan**.
- After park it is a normal parked card.
- Mid-hand organize = own cloud sorter (especially rounds 1–8). Mid-turn is insert + discard.

**Discard vs draw (not Uno)**

- Tap **draw pile** (only when it is your turn and you have not drawn yet) = take facedown card.
- Tap **discard pile** with **no card locked** and you have not drawn yet = take the **top discard** as your draw.
- Tap **discard pile** with a card **locked** and you **have already drawn** = discard that card and end the turn.
- You may take only the top discard card. Never bury/stock take.

---

## 2. LOCKED — clouds / hand office

**All clouds**
- Show that seat’s **card count**.
- Also show **cumulative score** (or score on short-press for opponents).
- No color flash. No yell.

**Opponent cloud (short press)**
- Cumulative score only. Never cards. Never their melds until they have laid down this round.

**Own cloud**
- Cumulative score + this-round preview if cheap.
- Text / color **bulk sorter** (five suits + jokers + ranks).
- Exit **freezes fan order** until cloud opened again.

**Round chrome (skeleton text OK)**
- Current round 1–11
- Cards dealt this round
- Current wild rank (3–10 / J / Q / K)
- Reminder: Jokers always wild

---

## 3. LOCKED — seats

Human is always bottom.  
Startup picks **how many opponents: 1, 2, or 3** (Touch One flow). Unused seats are hidden, not empty chairs.

Fill order when count < 3:
- 1 opponent → boy only (left)
- 2 opponents → boy + cat (left + top)
- 3 opponents → boy + cat + girl (all)

| Seat | Slot | Locked art later |
|------|------|------------------|
| Bottom | `slot_player` | human |
| Left | `slot_opp_boy` | boy, burgundy cap |
| Top | `slot_opp_cat` | Himalayan cat, ice-blue eyes, gold bell |
| Right | `slot_opp_girl` | teen girl, chestnut bob, freckles, navy collar |

Official 1–4 players maps to human + 1/2/3 AI. No solitaire.

Each seat: portrait, cloud, count, score.  
After someone goes out, laid-down Books/Runs for that seat appear in a **meld tray** under/beside that portrait (`R.id.meld_player`, `meld_opp_boy`, …). Empty during normal hidden play.

---

## 4. LOCKED — cards as data

Five suits. Color is the suit.

| Enum | Color | Suit |
|------|--------|------|
| `CLUBS` or `GREEN` | green | clubs |
| `HEARTS` or `PINK` | pink | hearts |
| `FLEURS` or `PURPLE` | purple | fleurs |
| `SPADES` or `ORANGE` | orange | spades |
| `DIAMONDS` or `GOLD` | gold | diamonds |
| `JOKER` | — | joker (not a suit) |

Ranks: `3,4,5,6,7,8,9,10,J,Q,K` only. **No Aces. No Twos.**

Deck construction (must be exact):

- One 58-card deck = 5 suits × 11 ranks + 3 jokers
- Play deck = **two** 58-card decks = **116** cards
- 6 jokers
- 22 cards per suit

Card face slots (placeholder rects): black border, color field, cream oval, center pip, corner pips.  
Jokers: `@drawable/card_face_joker`.  
Back: `@drawable/card_back` (official purple/gold TouchV! card-back artwork).
Sort placeholders: `@drawable/sort_slot_back` (landscape-readable TouchV!
artwork beneath the rotated face cards; keep this a separate swappable asset).

---

## 5. LOCKED — screens

| Slot | ID | Purpose |
|------|----|---------|
| Title | `screen_title` | Chrome fleur plate. One control: continue into opponent pick |
| Opponent pick | `screen_setup` | Pick opponents **1 / 2 / 3**. Then start. No tie toggle |
| Table | `screen_table` | Gameplay. Same family as Touch One |
| Round score | `overlay_scores` | After each of the 11 rounds. Totals + Continue |
| Win / loss | `screen_result` | After round 11 (and tiebreak if any). You won / you lost. Play again → opponent pick |
| Meld review | `overlay_melds` | Going-out / final-turn laydown |

**Screen flow (locked, Touch One shape):**
`Title → Pick opponents 1/2/3 → Table (11 rounds) → [round score after each hand] → Win/Loss → Pick opponents`

**Layout language:** Touch One table read — bottom fan, live opponent seats only, thought-clouds, draw/discard center. Different rules, same room.

Tiebreak: 6-card round, tied seats only; others stay on screen but skip turns (or hide). Hardcoded, no picker.

---

## 6. LOCKED — official rules (implement exactly)

### 6.1 Objective
11 rounds. Lowest **cumulative** score wins.  
Cards in valid Books/Runs = 0. Unused cards = points.

### 6.2 Rounds and wilds

| Round | Dealt | Wild rank |
|-------|-------|-----------|
| 1 | 3 | 3 |
| 2 | 4 | 4 |
| 3 | 5 | 5 |
| 4 | 6 | 6 |
| 5 | 7 | 7 |
| 6 | 8 | 8 |
| 7 | 9 | 9 |
| 8 | 10 | 10 |
| 9 | 11 | Jack |
| 10 | 12 | Queen |
| 11 | 13 | King |

Jokers are wild **every** round.  
Current-rank cards are wild **that round only**.

### 6.3 Values of unused cards
- Number card: face value (3–10)
- J = 11, Q = 12, K = 13
- Current wild rank (even if it is a 3–10/J/Q/K): **20**
- Joker: **50**

### 6.4 Deal
Shuffle 116. Dealer selected. Deal 1-at-a-time clockwise. Remainder = face-down draw pile. Flip top card to start discard. Player left of dealer starts. Play clockwise. Next round: deal moves left, hand size +1, wild rank updates.

### 6.5 Normal turn (exact order)
1. Draw **one** card: top of draw **or** top of discard. Only the top discard may be taken.
2. Organize privately (hand stays hidden). No laying Books/Runs on the table yet.
3. Discard **one** card face up. Discard ends the turn.
Must draw before discard. May discard a wild or joker.

### 6.6 Books
≥3 cards, **same rank**, any suits. Duplicates legal (two decks). Any number of wilds, wilds may be adjacent. Total size ≥3.

### 6.7 Runs
≥3 consecutive cards, **same suit**. Range is 3…K only. Cannot wrap. Cannot go below 3 or above King. Natural cards in the run must be that suit. Wilds may fill gaps, any count, may sit adjacent.

### 6.8 Going out
Only on your turn, after drawing.
- After the draw you have dealt+1 cards.
- Exactly **dealt** cards must form valid Books and/or Runs.
- The **extra drawn card is the discard** (it may have been playable).
- Lay Books/Runs face up, then discard the one remaining card.
May not go out before drawing. May not play on another player’s melds. No partial table melds during normal play.

### 6.9 After someone goes out
That player’s turn is done (they score 0 this round).  
Every other player gets **exactly one** final turn: draw, may lay any valid Books/Runs they can make, must discard one. Then stop. Unused cards score.

### 6.10 Empty draw pile
Leave current top discard. Shuffle the rest of the discard pile face down as new draw. Continue.

### 6.11 Ties
**Locked: Option 2 only.**  
If two or more players share the lowest cumulative score after round 11, play one **6-card** tiebreak round with **only the tied players**. 6s are wild (hand size 6). Jokers still wild. Lowest score that round wins. If still tied, repeat the 6-card tiebreak until one player is lowest.  
No joint-winner option in the app. No setup toggle.

### 6.12 AI skeleton
Legal random / greedy is enough: prefer taking discard if it completes a meld; otherwise draw; discard highest dead-wood; go out if able.

---

## 7. Interaction contract

```
phase: wait_draw | insert_pending | wait_discard | laying_out | not_my_turn

wait_draw (your turn, not yet drawn):
  tap draw pile              -> take facedown, insert_pending
  tap discard (no lock)      -> take top discard, insert_pending
  tap locked card / discard  -> IGNORE until drawn

insert_pending:
  slide into gap + drop      -> park, wait_discard
  skip / park end            -> park at end, wait_discard
  (optional) go straight to wait_discard with card still raised

wait_discard:
  tap parked card            -> lock/raise
  hop lock across fan
  tap discard with lock      -> if going_out_ok: laying_out then discard
                               else: discard, end turn
  tap own cloud              -> sorter; exit freezes order
  cannot draw again

laying_out (going out or final turn):
  group hand into books/runs (auto-detect + confirm)
  leftover must be exactly 1 card if going out on a normal out
  confirm -> table those melds, discard last card

not_my_turn:
  opponent anim placeholder
  opp cloud = score only
```

**Going-out check:** after draw, if there exists a partition of exactly `dealtCount` cards into valid books/runs and exactly 1 leftover, player may declare out (or final-turn lay any subset).

Validate books/runs in **one** engine. Use it for going-out, final lay, score, and AI.

Skeleton UI: auto-detect + confirm (`Declare Out` / `Lay Melds`).  
Later the player may group by hand; that UI must call the same validator. Auto-detect can stay as Suggest. No second rules copy.

---

## 8. Hook IDs (stable)

```
R.drawable.bg_title
R.drawable.bg_table
R.drawable.card_back
R.drawable.sort_slot_back  // fixed sort-slot backing, behind face cards
R.drawable.card_face_green     // clubs
R.drawable.card_face_pink      // hearts
R.drawable.card_face_purple    // fleurs
R.drawable.card_face_orange    // spades
R.drawable.card_face_gold      // diamonds
R.drawable.card_face_joker
R.drawable.opp_boy
R.drawable.opp_cat
R.drawable.opp_girl
R.drawable.icon_fleur

R.id.fan_top_row
R.id.fan_bottom_row
R.id.discard_pile
R.id.draw_pile
R.id.cloud_player
R.id.cloud_opp_boy
R.id.cloud_opp_cat
R.id.cloud_opp_girl
R.id.meld_player
R.id.meld_opp_boy
R.id.meld_opp_cat
R.id.meld_opp_girl
R.id.label_round
R.id.label_wild
```

Fan overlap / card size = dimen resources.

---

## 9. Out of scope for DeepSeek

- **All final visuals** (Grok only): title fleur, table felt, five suit faces, joker, card back, three opponent portraits, clouds, score/win-loss plates, store icon
- Touch One call rules
- Playing onto other players’ melds
- Store listing, ads, analytics

If you think a screen “needs art to be testable,” it does not. Use placeholders.

---

## 10. DeepSeek deliverable

1. Compiling Android Studio project.
2. Playable 2–4 seat, 11-round game with placeholder cards.
3. §7 interactions + §6 rules enforced.
4. README: run steps, hook ID list, note that Grok will replace drawables.
5. Source zip. No attempt at a visual polish pass.

If a UI hook is missing for lay-down, add a plain button `Declare Out` / `Lay Melds` — do not skip the rule.
