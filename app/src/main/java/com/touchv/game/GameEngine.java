package com.touchv.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * TouchV! game loop. Headless and deterministic when given a seeded Random,
 * so it can be driven by the Activity or exercised by tests.
 *
 * 11 rounds deal 3..13; the dealt count is also the Wild rank that round
 * (11=Jack, 12=Queen, 13=King). Jokers are wild every round.
 * Option 2 tiebreak only: 6-card round, 6s wild, repeat until one lowest.
 */
public final class GameEngine {

    public enum Phase { WAIT_DRAW, INSERT_PENDING, WAIT_DISCARD, NOT_MY_TURN, ROUND_OVER, GAME_OVER }

    public static final int TOTAL_ROUNDS = 11;
    public static final int TIEBREAK_DEALT = 6;

    private final Random rnd;
    private final List<Player> players = new ArrayList<>();
    private final List<Card> drawPile = new ArrayList<>();
    private final List<Card> discardPile = new ArrayList<>();

    private final List<Integer> active = new ArrayList<>();
    private final Set<Integer> finalTurnPending = new LinkedHashSet<>();

    private int dealer = 0;
    private int current = 0;
    private int roundNumber = 0;       // 1..11
    private boolean tiebreak = false;
    private int wentOutIndex = -1;
    private Phase phase = Phase.NOT_MY_TURN;

    private Card pendingCard = null;
    private int pendingHandIndex = -1;
    private boolean drawnThisTurn = false;

    private boolean gameOver = false;
    private final List<Integer> winners = new ArrayList<>();
    private String statusLine = "";
    private final java.util.Map<Integer, Boolean> lastDrawWasDiscard = new java.util.HashMap<>();
    private boolean roundSummaryPending = false;

    public GameEngine(Random rnd) {
        this.rnd = rnd;
    }

    // ------------------------------------------------------------------
    // Setup
    // ------------------------------------------------------------------

    /** Builds human + opponentCount AI seats. Opponents fill boy, cat, girl. */
    public void newGame(int opponentCount) {
        players.clear();
        players.add(new Player(0, Player.SeatKind.HUMAN, "You"));
        Player.SeatKind[] order = {Player.SeatKind.BOY, Player.SeatKind.CAT, Player.SeatKind.GIRL};
        String[] names = {"Boy", "Cat", "Girl"};
        for (int i = 0; i < opponentCount; i++) {
            players.add(new Player(i + 1, order[i], names[i]));
        }
        for (Player p : players) p.cumulativeScore = 0;
        roundNumber = 0;
        tiebreak = false;
        gameOver = false;
        winners.clear();
        dealer = players.size() - 1;   // first turn belongs to seat 0 (human)
        startRound();
    }

    public int opponentCount() {
        return players.size() - 1;
    }

    public List<Player> players() {
        return players;
    }

    public Player human() {
        return players.get(0);
    }

    public Player player(int i) {
        return players.get(i);
    }

    public List<Card> drawPile() {
        return drawPile;
    }

    public List<Card> discardPile() {
        return discardPile;
    }

    public Card topDiscard() {
        return discardPile.isEmpty() ? null : discardPile.get(discardPile.size() - 1);
    }

    public int currentIndex() {
        return current;
    }

    public Player currentPlayer() {
        return players.get(current);
    }

    public Phase phase() {
        return phase;
    }

    public int roundNumber() {
        return roundNumber;
    }

    public boolean isTiebreak() {
        return tiebreak;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public List<Integer> winners() {
        return winners;
    }

    public int wentOutIndex() {
        return wentOutIndex;
    }

    public String statusLine() {
        return statusLine;
    }

    public void setStatusLine(String s) {
        this.statusLine = s;
    }

    public Card pendingCard() {
        return pendingCard;
    }

    public int pendingHandIndex() {
        return pendingHandIndex;
    }

    public boolean hasDrawnThisTurn() {
        return drawnThisTurn;
    }

    public List<Integer> activePlayers() {
        return active;
    }

    public boolean isActive(int playerIndex) {
        return active.contains(playerIndex);
    }

    /** Cards dealt this round (also the Wild rank). */
    public int dealtCount() {
        if (tiebreak) return TIEBREAK_DEALT;
        return roundNumber + 2;    // round 1 -> 3 ... round 11 -> 13
    }

    public int wildRank() {
        return dealtCount();       // 3..13 maps directly to rank; 11=J,12=Q,13=K
    }

    public String wildLabel() {
        int r = wildRank();
        switch (r) {
            case 11: return "Jack";
            case 12: return "Queen";
            case 13: return "King";
            default: return Integer.toString(r);
        }
    }

    // ------------------------------------------------------------------
    // Round setup
    // ------------------------------------------------------------------

    public void startRound() {
        roundNumber++;
        if (roundNumber > TOTAL_ROUNDS) {
            gameOver = true;
            phase = Phase.GAME_OVER;
            return;
        }
        if (roundNumber > 1) dealer = nextActiveAfter(dealer);  // deal moves left each round
        beginHand(Arrays.asList(range(players.size())));
    }

    private static Integer[] range(int n) {
        Integer[] a = new Integer[n];
        for (int i = 0; i < n; i++) a[i] = i;
        return a;
    }

    private void beginHand(List<Integer> activePlayers) {
        active.clear();
        active.addAll(activePlayers);
        finalTurnPending.clear();
        wentOutIndex = -1;
        pendingCard = null;
        pendingHandIndex = -1;
        drawnThisTurn = false;
        statusLine = tiebreak ? "Tiebreak round" : "Round " + roundNumber;

        for (Player p : players) p.resetForRound();

        List<Card> deck = Deck.buildDeck();
        Deck.shuffle(deck, rnd);

        int dealt = dealtCount();
        // Deal one at a time clockwise starting left of dealer.
        for (int i = 1; i <= dealt; i++) {
            for (int k = 1; k <= active.size(); k++) {
                int idx = active.get(k % active.size());
                players.get(idx).hand.add(deck.remove(deck.size() - 1));
            }
        }
        drawPile.clear();
        drawPile.addAll(deck);
        discardPile.clear();
        if (!drawPile.isEmpty()) {
            discardPile.add(drawPile.remove(drawPile.size() - 1));
        }

        current = nextActiveAfter(dealer);
        drawnThisTurn = false;
        phase = current == 0 ? Phase.WAIT_DRAW : Phase.NOT_MY_TURN;
    }

    private int nextActiveAfter(int from) {
        int n = players.size();
        for (int step = 1; step <= n; step++) {
            int idx = ((from + step) % n + n) % n;
            if (active.contains(idx)) return idx;
        }
        return from;
    }

    // ------------------------------------------------------------------
    // Draw / discard
    // ------------------------------------------------------------------

    public boolean canDraw() {
        return phase == Phase.WAIT_DRAW && !drawnThisTurn;
    }

    private boolean canAct(int p) {
        return current == p && (phase == Phase.WAIT_DRAW || phase == Phase.NOT_MY_TURN);
    }

    /** Draw the top facedown card. */
    public Card drawFromStock(int p) {
        if (!canAct(p) || drawnThisTurn) return null;
        if (drawPile.isEmpty()) reshuffleDiscardIntoDraw();
        if (drawPile.isEmpty()) return null;
        Card c = drawPile.remove(drawPile.size() - 1);
        takeDrawn(p, c);
        return c;
    }

    /** Take the top card of the discard pile as the draw. */
    public Card drawFromDiscard(int p) {
        if (!canAct(p) || drawnThisTurn) return null;
        if (discardPile.isEmpty()) return null;
        Card c = discardPile.remove(discardPile.size() - 1);
        takeDrawn(p, c);
        return c;
    }

    /**
     * Human draw that always succeeds while it is a legal draw: prefers the
     * requested source, falls back to the other, and refills from the discard pile
     * when the draw pile is exhausted.
     */
    public Card drawHuman(int p, boolean preferDiscard) {
        if (p != current || phase != Phase.WAIT_DRAW || drawnThisTurn) return null;
        if (preferDiscard && !discardPile.isEmpty()) return drawFromDiscard(p);
        if (!drawPile.isEmpty()) return drawFromStock(p);
        reshuffleDiscardIntoDraw();
        if (!drawPile.isEmpty()) return drawFromStock(p);
        if (!discardPile.isEmpty()) return drawFromDiscard(p);
        return null;
    }

    private void takeDrawn(int p, Card c) {
        Player pl = players.get(p);
        pl.hand.add(c);
        pendingCard = c;
        pendingHandIndex = pl.hand.size() - 1;
        drawnThisTurn = true;
        phase = Phase.INSERT_PENDING;
        statusLine = pl.name + " drew. Insert the card or park it.";
    }

    /** Moves the just-drawn card into a gap index of the fan. */
    public void parkPending(int p, int insertIndex) {
        if (phase != Phase.INSERT_PENDING || pendingCard == null) return;
        Player pl = players.get(p);
        int from = pendingHandIndex;
        if (from < 0 || from >= pl.hand.size()) {
            phase = Phase.WAIT_DISCARD;
            return;
        }
        Card c = pl.hand.remove(from);
        int to = Math.max(0, Math.min(insertIndex, pl.hand.size()));
        pl.hand.add(to, c);
        pendingCard = null;
        pendingHandIndex = -1;
        phase = Phase.WAIT_DISCARD;
        statusLine = pl.name + " must discard one card.";
    }

    /** Skip insert: park the drawn card at the end of the fan. */
    public void parkPendingAtEnd(int p) {
        parkPending(p, players.get(p).hand.size());
    }

    /**
     * Discard a card from hand, ending the turn. If the player is going out this
     * call also performs the laydown.
     */
    public boolean discardCard(int p, int handIndex) {
        if (phase != Phase.WAIT_DISCARD) return false;
        Player pl = players.get(p);
        if (handIndex < 0 || handIndex >= pl.hand.size()) return false;
        Card c = pl.hand.remove(handIndex);
        discardPile.add(c);
        pendingCard = null;
        pendingHandIndex = -1;
        statusLine = pl.name + " discarded " + c.shortLabel() + ".";
        endTurn(p);
        return true;
    }

    // ------------------------------------------------------------------
    // Going out / final laydown
    // ------------------------------------------------------------------

    public boolean canDeclareOut(int p) {
        Player pl = players.get(p);
        if (wentOutIndex != -1) return false;
        if (!drawnThisTurn) return false;
        return Meld.canGoOut(pl.hand, dealtCount(), wildRank());
    }

    /**
     * Declare out: lay exactly dealtCount cards as melds, discard the one leftover.
     * Uses the shared validator through Meld.findPartition.
     */
    public boolean declareOut(int p) {
        if (!canDeclareOut(p)) return false;
        Player pl = players.get(p);
        Meld.Partition part = Meld.findPartition(pl.hand, dealtCount(), wildRank());
        if (part == null || part.leftover.size() != 1) return false;

        pl.laidMelds.clear();
        pl.laidMelds.addAll(part.melds);
        pl.laidDownThisRound = true;
        pl.wentOut = true;
        wentOutIndex = p;

        // The single leftover is the final discard.
        Card discard = part.leftover.get(0);
        pl.hand.clear();
        discardPile.add(discard);

        statusLine = pl.name + " went out!";
        finalTurnPending.clear();
        for (int idx : active) if (idx != p) finalTurnPending.add(idx);

        pendingCard = null;
        pendingHandIndex = -1;
        advanceAfterTurn(p);
        return true;
    }

    /** True during the one final turn after someone went out. */
    public boolean isFinalTurn(int p) {
        return wentOutIndex != -1 && finalTurnPending.contains(p);
    }

    /**
     * Final-turn laydown: meld as many cards as possible, always leaving one card
     * to discard. Uses the shared validator.
     */
    public boolean layMelds(int p) {
        if (!(phase == Phase.WAIT_DISCARD || phase == Phase.INSERT_PENDING || phase == Phase.NOT_MY_TURN)) return false;
        if (current != p) return false;
        if (!isFinalTurn(p)) return false;
        Player pl = players.get(p);
        if (pl.hand.size() < 2) return false;

        Meld.Partition best = null;
        int bestScore = Integer.MAX_VALUE;
        // Leave exactly one card behind to satisfy the mandatory final discard.
        for (int skip = 0; skip < pl.hand.size(); skip++) {
            List<Card> rest = new ArrayList<>(pl.hand);
            Card reserved = rest.remove(skip);
            Meld.Partition part = Meld.bestPartition(rest, wildRank());
            int score = Meld.scoreLeftovers(part.leftover, wildRank()) + reserved.pointValue(wildRank());
            if (score < bestScore) {
                bestScore = score;
                best = part;
            }
        }
        if (best == null) return false;
        pl.laidMelds.clear();
        pl.laidMelds.addAll(best.melds);
        pl.laidDownThisRound = true;

        // Remove the melded cards from the hand so only the reserved discard remains.
        Set<Integer> used = new java.util.HashSet<>();
        for (Meld m : best.melds) for (Card c : m.cards) used.add(c.uid);
        List<Card> remaining = new ArrayList<>();
        for (Card c : pl.hand) if (!used.contains(c.uid)) remaining.add(c);
        pl.hand.clear();
        pl.hand.addAll(remaining);

        statusLine = pl.name + " laid " + best.melds.size() + " meld(s).";
        return true;
    }

    /** Best (lowest) score for a player's current hand, via the shared validator. */
    public int previewScore(int p) {
        return Meld.bestScore(players.get(p).hand, wildRank());
    }

    // ------------------------------------------------------------------
    // Turn advance / round end
    // ------------------------------------------------------------------

    private void endTurn(int p) {
        advanceAfterTurn(p);
    }

    private void advanceAfterTurn(int p) {
        if (wentOutIndex != -1) {
            finalTurnPending.remove(p);
            if (finalTurnPending.isEmpty()) {
                finishRound();
                return;
            }
        }
        current = nextActiveAfter(p);
        drawnThisTurn = false;
        pendingCard = null;
        pendingHandIndex = -1;
        phase = current == 0 ? Phase.WAIT_DRAW : Phase.NOT_MY_TURN;
        if (wentOutIndex != -1 && isFinalTurn(current)) {
            statusLine = players.get(current).name + " final turn.";
        }
    }

    private void finishRound() {
        for (int idx : active) {
            Player pl = players.get(idx);
            if (pl.wentOut) {
                pl.roundScore = 0;   // laid melds already recorded; hand is empty
            } else if (pl.laidDownThisRound) {
                // Final-turn laydown: keep the recorded melds, score the leftover card(s).
                pl.roundScore = Meld.scoreLeftovers(pl.hand, wildRank());
            } else {
                Meld.Partition part = Meld.bestPartition(pl.hand, wildRank());
                pl.laidMelds.clear();
                pl.laidMelds.addAll(part.melds);
                pl.roundScore = Meld.scoreLeftovers(part.leftover, wildRank());
            }
            if (!tiebreak) {
                pl.cumulativeScore += pl.roundScore;   // tiebreak decides the winner only
            }
        }
        phase = Phase.ROUND_OVER;

        // Defer tiebreak/end-of-game resolution until the round score card is dismissed,
        // so the summary always shows the hand that was just played.
        roundSummaryPending = true;
    }

    /** Called when the round score card is dismissed. Resolves tiebreak / game end. */
    public void acknowledgeRoundSummary() {
        if (!roundSummaryPending) return;
        roundSummaryPending = false;
        if (tiebreak) {
            resolveTiebreak();
        } else if (roundNumber >= TOTAL_ROUNDS) {
            resolveEndOfGame();
        }
        if (gameOver) phase = Phase.GAME_OVER;
    }

    public boolean isRoundSummaryPending() {
        return roundSummaryPending;
    }

    private void resolveEndOfGame() {
        int lowest = Integer.MAX_VALUE;
        for (Player p : players) lowest = Math.min(lowest, p.cumulativeScore);
        List<Integer> tied = new ArrayList<>();
        for (Player p : players) if (p.cumulativeScore == lowest) tied.add(p.index);
        if (tied.size() == 1) {
            gameOver = true;
            winners.clear();
            winners.add(tied.get(0));
        } else {
            startTiebreak(tied);
        }
    }

    private void resolveTiebreak() {
        int lowest = Integer.MAX_VALUE;
        for (int idx : active) lowest = Math.min(lowest, players.get(idx).roundScore);
        List<Integer> tied = new ArrayList<>();
        for (int idx : active) if (players.get(idx).roundScore == lowest) tied.add(idx);
        if (tied.size() == 1) {
            gameOver = true;
            winners.clear();
            winners.add(tied.get(0));
        } else {
            startTiebreak(tied);
        }
    }

    private void startTiebreak(List<Integer> tied) {
        tiebreak = true;
        dealer = tied.get(tied.size() - 1);   // first turn goes to the first tied seat
        beginHand(tied);
    }

    // ------------------------------------------------------------------
    // Draw pile refill
    // ------------------------------------------------------------------

    /** Empty draw pile: leave the top discard, shuffle the rest into a new draw pile. */
    public void reshuffleDiscardIntoDraw() {
        if (discardPile.size() <= 1) return;
        Card top = discardPile.remove(discardPile.size() - 1);
        List<Card> rest = new ArrayList<>(discardPile);
        discardPile.clear();
        discardPile.add(top);
        Deck.shuffle(rest, rnd);
        drawPile.addAll(rest);
        statusLine = "Draw pile empty - discard pile reshuffled.";
    }

    // ------------------------------------------------------------------
    // AI
    // ------------------------------------------------------------------

    /** Plays a complete AI turn (draw, optional lay/out, discard). */
    public void playAiTurn() {
        int p = current;
        if (players.get(p).isHuman()) return;
        playAiTurnFor(p);
    }

    /** Drives one greedy turn for seat {@code p}. Used by AI seats and simulations. */
    public void playAiTurnFor(int p) {
        if (!(phase == Phase.WAIT_DRAW || phase == Phase.NOT_MY_TURN) || current != p) return;
        if (drawnThisTurn) return;
        boolean finalTurn = isFinalTurn(p);

        // Choose draw source. Prefer the discard only when it actually helps: it lets
        // the AI go out now, or it joins a meld AND the AI did not just take a discard.
        // Otherwise draw from the stock so the round always makes progress (a pure
        // "take any discard that fits a meld" greedy can trade the same card forever).
        Card top = topDiscard();
        boolean takeDiscard = false;
        if (top != null && !lastDrawWasDiscard.getOrDefault(p, false)) {
            List<Card> trial = new ArrayList<>(players.get(p).hand);
            trial.add(top);
            if (Meld.canGoOut(trial, dealtCount(), wildRank())) {
                takeDiscard = true;
            } else {
                Meld.Partition trialPart = Meld.bestPartition(trial, wildRank());
                for (Meld m : trialPart.melds) {
                    boolean hasTop = false;
                    for (Card c : m.cards) if (c.uid == top.uid) { hasTop = true; break; }
                    if (hasTop && m.cards.size() >= 3) { takeDiscard = true; break; }
                }
            }
        }
        lastDrawWasDiscard.put(p, takeDiscard);
        if (takeDiscard) {
            drawFromDiscard(p);
        } else {
            drawFromStock(p);
        }
        parkPendingAtEnd(p);

        // Going out.
        if (canDeclareOut(p)) {
            declareOut(p);
            return;
        }

        // Final-turn laydown.
        if (finalTurn) {
            layMelds(p);
        }

        // Discard the highest-value card that is not part of a meld we keep.
        Player pl = players.get(p);
        Meld.Partition part = Meld.bestPartition(pl.hand, wildRank());
        Set<Integer> melded = new java.util.HashSet<>();
        for (Meld m : part.melds) {
            for (Card c : m.cards) melded.add(c.uid);
        }
        int discardIdx = -1;
        int worst = -1;
        for (int i = 0; i < pl.hand.size(); i++) {
            Card c = pl.hand.get(i);
            if (melded.contains(c.uid)) continue;
            int v = c.pointValue(wildRank());
            if (v > worst) {
                worst = v;
                discardIdx = i;
            }
        }
        if (discardIdx == -1) {
            // Everything melds: dump the highest-value card.
            for (int i = 0; i < pl.hand.size(); i++) {
                int v = pl.hand.get(i).pointValue(wildRank());
                if (v > worst) {
                    worst = v;
                    discardIdx = i;
                }
            }
        }
        if (discardIdx >= 0) discardCard(p, discardIdx);
    }

    /** True when the current seat is an AI that should act. */
    public boolean aiShouldAct() {
        return !gameOver && phase != Phase.ROUND_OVER && phase != Phase.GAME_OVER
                && current != 0 && active.contains(current);
    }

    public List<Meld> meldsFor(int p) {
        return players.get(p).laidMelds;
    }

    public List<Card> handOf(int p) {
        return players.get(p).hand;
    }

    /** Debug/summary helper. */
    public String scoreboard() {
        StringBuilder sb = new StringBuilder();
        for (Player p : players) {
            sb.append(p.name).append(": ").append(p.cumulativeScore).append('\n');
        }
        return sb.toString();
    }

    public static List<Card> safeCopy(List<Card> in) {
        return Collections.unmodifiableList(new ArrayList<>(in));
    }
}
