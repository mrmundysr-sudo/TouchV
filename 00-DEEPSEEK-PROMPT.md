# TouchV! — Read this first (DeepSeek)

You are building the **TouchV!** Android **skeleton only**.

Java, single Activity, package `com.touchv.game`, min SDK 26.

Follow the two files in this zip **exactly**:

1. `TOUCH-V-DEEPSEEK-HANDOFF.md` — UX, screens, hook IDs, interaction, what you may and may not do
2. `TOUCHV-OFFICIAL-RULES.md` — official game rules. Do not simplify, reinterpret, or add house rules.

If the two files ever seem to conflict on a **rule**, the official rules file wins.  
If they conflict on **UI / hooks / ownership**, the handoff wins.

---

## Visuals are out of scope. Grok owns every pixel.

Do not create card art, portraits, or backgrounds.  
Do not generate, download, or “improve” title art, felt, card faces, card backs, logos, clouds, or win/loss plates.  
Grok will replace the placeholder drawables after the skeleton plays.

Ugly is correct:

- Solid color rects
- Labeled TextViews
- Simple shapes
- Named IDs from the handoff §8, unchanged

If a slot looks empty, leave a colored rect + ID. That is the handoff, not a defect.  
No concept art in `res/`. No polish pass.

---

## Non-negotiable

- 116-card deck (two 58-card decks), 5 suits + 6 jokers, ranks 3–K only
- Suits: green clubs, pink hearts, purple fleurs, orange spades, gold diamonds
- 11 rounds, deal 3→13, that rank is wild, jokers always wild
- Every turn: draw one (stock **or** top discard), then discard one
- Books ≥3 same rank; runs ≥3 same suit consecutive; wilds allowed
- No playing on other players’ melds
- Melds stay hidden until going out or the one final turn after someone goes out
- Going out: after draw, laid cards = dealt count, extra card is the discard
- Startup: Title → pick opponents **1 / 2 / 3** → table
- Fill seats: 1 = boy, 2 = boy+cat, 3 = boy+cat+girl. Hide unused seats
- After each round: score overlay. After round 11: win/loss, then back to opponent pick
- Tie after round 11 = 6-card tiebreak (6s wild), repeat until one lowest score. No joint winners. No toggle
- Hand UI: two-row fan (7 + 6), tap-lock one card, only the just-drawn card can be inserted into a gap
- Keep Touch One **table layout**: bottom fan, opponent seats + clouds, piles in the center
- Different rules than Touch One / Uno. Do not copy color-match discard, “TOUCH ONE” yell, or +2 call penalty
- One meld validator used for going-out, final lay, score, and AI
- Skeleton UI: auto-detect + confirm (`Declare Out` / `Lay Melds`). Keep that in one method so grouping UI can change later

---

## Deliverable

1. Compiling Android Studio project
2. Playable 2–4 seat (human + 1/2/3 AI), 11-round game with placeholder cards
3. Interactions and rules enforced
4. Project README: how to run, hook ID list, note that Grok will replace drawables
5. Source zip. No visual polish

Build the skeleton. Stop at playable + hooks.
