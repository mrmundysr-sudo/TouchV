# TOUCHV! — OFFICIAL GAME RULES

STRICT GAME IMPLEMENTATION SPECIFICATION

TouchV! is a five-suit rummy-style card game played over 11 rounds. Players form Books and Runs while trying to finish the game with the lowest total score.

The rules below define the official TouchV! game. Do not simplify, reinterpret, or add house rules.

## OBJECTIVE

The objective of TouchV! is to have the lowest cumulative score after all 11 rounds are completed.

During each round, players try to arrange their entire hand into valid Books and Runs. Cards included in valid Books or Runs score zero. Any unused cards score points.

The player with the lowest total score at the end of the eleventh round wins.

## PLAYERS

TouchV! supports 1–4 players.

App mapping for this build: human + 1, 2, or 3 AI opponents. No solitaire.

## THE TOUCHV! DECK

TouchV! uses two identical 58-card decks combined into one 116-card deck.

Each 58-card deck contains:

- 3 Jokers
- 5 suits
- 11 cards in each suit

The five TouchV! suits are:

- Green Clubs
- Pink Hearts
- Purple Fleurs
- Orange Spades
- Gold Diamonds

Each suit contains:

- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10
- Jack
- Queen
- King

There are no Aces or Twos.

The complete combined TouchV! deck contains:

- 6 Jokers
- 22 Green Clubs cards
- 22 Pink Hearts cards
- 22 Purple Fleurs cards
- 22 Orange Spades cards
- 22 Gold Diamonds cards
- 116 cards total

## CARD VALUES

When a card is not included in a valid Book or Run, it scores as follows:

- Number cards score their printed value.
- Jacks are worth 11 points.
- Queens are worth 12 points.
- Kings are worth 13 points.
- The current round’s Wild cards are worth 20 points.
- Jokers are worth 50 points.

A Wild card’s scoring value is 20 points even if its printed rank would normally have a different value.

## ROUNDS AND ROTATING WILD CARDS

TouchV! contains exactly 11 rounds.

The number of cards dealt increases by one card each round. The card rank that matches the number of cards dealt becomes Wild for that round.

Round 1:
- 3 cards dealt to each player
- 3s are Wild

Round 2:
- 4 cards dealt to each player
- 4s are Wild

Round 3:
- 5 cards dealt to each player
- 5s are Wild

Round 4:
- 6 cards dealt to each player
- 6s are Wild

Round 5:
- 7 cards dealt to each player
- 7s are Wild

Round 6:
- 8 cards dealt to each player
- 8s are Wild

Round 7:
- 9 cards dealt to each player
- 9s are Wild

Round 8:
- 10 cards dealt to each player
- 10s are Wild

Round 9:
- 11 cards dealt to each player
- Jacks are Wild

Round 10:
- 12 cards dealt to each player
- Queens are Wild

Round 11:
- 13 cards dealt to each player
- Kings are Wild

Jokers are Wild in every round.

## DEALING

1. Shuffle both TouchV! decks together.
2. Select a dealer.
3. Deal the cards one at a time in clockwise order.
4. Deal the correct number of cards for the current round.
5. Place all remaining cards face down to create the Draw Pile.
6. Turn the top card of the Draw Pile face up beside it to begin the Discard Pile.
7. The player to the dealer’s left takes the first turn.
8. Play continues clockwise.

## NORMAL TURN

Every normal TouchV! turn must follow this exact sequence:

1. Draw one card.
2. The player chooses one of the following:
   - The top card of the face-down Draw Pile.
   - The top card of the face-up Discard Pile.
3. Only the top card of the Discard Pile may be taken.
4. The player organizes their hand privately.
5. The player discards one card face up onto the Discard Pile.
6. Discarding ends the player’s turn.

A player must draw before discarding.

During normal play, players keep their Books and Runs hidden in their hands.

Players do not place partial or completed Books and Runs on the table until they are going out or completing their final turn after another player has gone out.

## BOOKS

A Book consists of three or more cards with the same rank, regardless of suit.

Examples of valid TouchV! Books:

- 8 Green Clubs, 8 Pink Hearts, 8 Orange Spades
- King Orange Spades, King Gold Diamonds, King Pink Hearts, King Gold Diamonds

Duplicate cards are allowed because TouchV! uses two decks.

A Book may contain any number of Wild cards.

Wild cards may appear beside one another in the same Book.

A Joker or the current round’s Wild card may represent any required card.

A Book must contain at least three cards total.

## RUNS

A Run consists of three or more consecutive cards of the same suit.

Examples of valid TouchV! Runs:

- 5 Green Clubs, 6 Green Clubs, 7 Green Clubs
- 9 Purple Fleurs, 10 Purple Fleurs, Jack Purple Fleurs, Queen Purple Fleurs

A Joker or the current round’s Wild card may replace any missing card in a Run.

Wild cards may appear beside one another in the same Run.

A Run may contain any number of Wild cards.

A Run may not continue below 3 or above King.

The natural cards in a Run must belong to the same TouchV! suit.

## GOING OUT

A player may go out only during that player’s own turn.

To go out, the player must:

1. Begin the turn normally by drawing one card.
2. Arrange all but one card into valid Books and/or Runs.
3. Lay down the complete Books and Runs face up.
4. Discard the one remaining card.

The player must have exactly one card remaining to discard.

The final discard may be a card that could otherwise have been used in a Book or Run.

A player may not go out before drawing.

The player must use only the number of cards originally dealt for that round in their Books and Runs.

The extra card drawn at the beginning of the turn must be the card discarded when going out.

Example:

In a 7-card round, a player begins with 7 cards. The player draws one card and now has 8 cards. Seven cards must form valid Books and/or Runs, and one card must be discarded.

## AFTER A PLAYER GOES OUT

Once a player goes out:

1. The player who went out completes the turn.
2. Every other player receives exactly one final turn.
3. Each remaining player begins the final turn by drawing normally.
4. Each remaining player may lay down any valid Books and/or Runs they can make.
5. Each remaining player must discard one final card.
6. Any cards not included in valid Books or Runs count toward that player’s score.

Players may not take additional turns after completing their final turn.

Players may not add cards to another player’s Books or Runs.

## SCORING A ROUND

After every player completes their final turn:

1. Cards included in valid Books and Runs score zero.
2. Only unused cards are scored.
3. Add the values of all unused cards.
4. Record that amount as the player’s score for the round.
5. Add the round score to the player’s cumulative score.

The player who went out scores zero for that round.

## SCORING EXAMPLE

A player has the following unused cards:

- 6
- Queen
- Current Wild card
- Joker

The player receives:

- 6 points for the 6
- 12 points for the Queen
- 20 points for the current Wild card
- 50 points for the Joker

The player’s score for the round is 88 points.

Cards placed into valid Books or Runs score zero.

## EMPTY DRAW PILE

If the Draw Pile becomes empty:

1. Leave the current top card of the Discard Pile in place.
2. Take all remaining cards from the Discard Pile.
3. Shuffle those cards.
4. Turn them face down to create a new Draw Pile.
5. Continue play normally.

The current top Discard card remains available to be picked up.

## DISCARDING WILD CARDS

A player may discard a Joker or the current round’s Wild card.

This is legal even if the card could be used in a Book or Run.

If the discarded Wild card remains on top of the Discard Pile, the next player may take it.

## STARTING THE NEXT ROUND

After scoring a round:

1. Collect every player’s cards.
2. Collect all Books and Runs.
3. Collect the Discard Pile.
4. Collect the Draw Pile.
5. Shuffle all cards together.
6. Move the deal to the next dealer.
7. Deal one additional card to each player.
8. Change the Wild rank according to the new hand size.
9. Begin the next round.

The next dealer is the player to the previous dealer’s left.

## ENDING THE GAME

The game ends after Round 11 has been completed and scored.

Round 11 deals 13 cards to each player, and Kings are Wild.

After Round 11:

1. Add all 11 round scores together for every player.
2. Compare the cumulative scores.
3. The player with the lowest cumulative score wins TouchV!

## TIES

App lock for this build (Option 2 only):

Play a six-card tiebreaking round involving only the tied players. The player with the lowest score in that tiebreaking round wins.

If still tied, repeat the 6-card tiebreak until one player is lowest.

6s are Wild in the tiebreak (hand size 6). Jokers remain Wild.

The app must apply this method consistently. No joint-winner option. No setup toggle.

## MANDATORY TOUCHV! IMPLEMENTATION RULES

The TouchV! app must enforce all of the following:

- The game is named TouchV! everywhere.
- TouchV! supports 1–4 players.
- The TouchV! deck contains 116 cards.
- The deck is made from two 58-card decks.
- There are exactly 6 Jokers.
- There are exactly 5 suits.
- The suits are Green Clubs, Pink Hearts, Purple Fleurs, Orange Spades, and Gold Diamonds.
- Each suit contains cards 3 through 10, Jack, Queen, and King.
- There are no Aces.
- There are no Twos.
- There are exactly 11 rounds.
- The first round deals 3 cards.
- The final round deals 13 cards.
- The hand size increases by exactly one card each round.
- The rotating Wild rank matches the number of cards dealt.
- Jokers are always Wild.
- The current round’s Wild cards are worth 20 points.
- Jokers are worth 50 points.
- Books require at least 3 cards of the same rank.
- Book cards do not have to be different suits.
- Duplicate cards are legal in Books.
- Runs require at least 3 consecutive cards of the same suit.
- Natural cards in Runs must match the same suit.
- Wild cards may be used in Books.
- Wild cards may be used in Runs.
- Multiple Wild cards may be used in one Book or Run.
- Wild cards may be next to one another.
- Runs cannot extend below 3 or above King.
- Players must draw before discarding.
- Players may draw only from the Draw Pile or the top of the Discard Pile.
- Players may take only the top Discard card.
- Players must discard to end a turn.
- Players keep Books and Runs hidden during normal play.
- Players may not place partial combinations on the table during normal play.
- Players may not play on another player’s Books or Runs.
- A player going out must draw first.
- A player going out must leave one card to discard.
- The extra drawn card when going out must be discarded.
- The final discard may be a playable card.
- Once a player goes out, every other player receives exactly one final turn.
- Only unused cards are scored.
- Cards in valid Books and Runs score zero.
- The player who goes out scores zero for the round.
- The Discard Pile may be reshuffled when the Draw Pile becomes empty.
- The next round deals one additional card to each player.
- Kings are Wild in Round 11.
- The lowest cumulative score wins.
