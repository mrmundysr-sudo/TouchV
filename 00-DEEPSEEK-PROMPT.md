# TouchV! — Read this first (DeepSeek)

You are building the TouchV! Android skeleton only.

Java, single Activity, package com.touchv.game, min SDK 26.

Follow the two files in this repository exactly:

1. TOUCH-V-DEEPSEEK-HANDOFF.md — UX, screens, hook IDs, interaction, what you may and may not do
2. TOUCHV-OFFICIAL-RULES.md — official game rules. Do not simplify, reinterpret, or add house rules.

If the two files conflict on a rule, the official rules file wins. If they conflict on UI, hooks, or ownership, the handoff wins.

## Visuals are out of scope

Grok owns every pixel. Do not create card art, portraits, backgrounds, title art, felt, card faces, card backs, logos, clouds, or win/loss plates. Do not generate, download, or improve visuals. Grok will replace the placeholder drawables after the skeleton plays.

Ugly is correct: use solid color rectangles, labeled TextViews, simple shapes, and the named IDs from handoff section 8 unchanged. If a slot looks empty, leave a colored rectangle plus its ID. No concept art in res/. No polish pass.

## Non-negotiable

- 116-card deck: two 58-card decks, 5 suits, 6 Jokers, ranks 3 through King only
- Suits: Green Clubs, Pink Hearts, Purple Fleurs, Orange Spades, Gold Diamonds
- 11 rounds, deal 3 through 13, that rank is Wild, Jokers always Wild
- Every turn: draw one from the Draw Pile or top Discard Pile, then discard one
- Books require at least 3 of the same rank; Runs require at least 3 consecutive cards of the same suit; Wilds are allowed
- No playing on another player’s melds
- Melds stay hidden until going out or the one final turn after someone goes out
- Going out: after drawing, laid cards equal the number dealt; the extra card is discarded
- Startup: title, then choose 1, 2, or 3 opponents, then table
- Seats: 1 opponent = boy; 2 = boy plus cat; 3 = boy plus cat plus girl; hide unused seats
- After each round show a score overlay; after Round 11 show win/loss, then return to opponent selection
- Tie after Round 11: six-card tiebreak, Sixes Wild, repeat until one player has the lowest score; no joint winners and no toggle
- Hand UI: two-row fan, 7 plus 6; tap-lock one card; only the just-drawn card may be inserted into a gap
- Keep the TouchV! table layout: bottom fan, opponent seats and clouds, piles in the center
- Do not copy color-match discarding, a yell/call rule, or a +2 call penalty
- Use one meld validator for going out, final laydown, scoring, and AI
- Skeleton UI must auto-detect and confirm Declare Out and Lay Melds; keep that logic in one method so grouping UI can change later

## Deliverable

1. Compiling Android Studio project
2. Playable 2–4 seat game with human plus 1, 2, or 3 AI opponents
3. Complete 11-round game with placeholder cards
4. Rules and interactions enforced
5. README explaining how to run, hook IDs, and that Grok replaces drawables
6. Source ZIP

Build the skeleton only. Stop at playable plus hooks. Do not perform visual design or polish.
