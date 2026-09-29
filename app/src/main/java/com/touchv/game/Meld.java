package com.touchv.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * THE single meld validator. Used by going out, final laydown, scoring, and AI.
 * Do not duplicate these rules anywhere else.
 *
 * Rules implemented here (official rules only, no house rules):
 *  - Book:  >= 3 cards, same rank, any suits. Duplicates legal (two decks).
 *           Any number of Wild cards; Wilds may be adjacent.
 *  - Run:   >= 3 consecutive cards of the same suit, range 3..K only (no wrap,
 *           cannot go below 3 or above King). Natural cards must share the suit.
 *           Wilds may fill gaps, any count, may be adjacent.
 *  - Wild:  Joker always; the current round's rank (hand size) this round only.
 *
 * The partition search is a bitmask DP over the hand (<= 14 cards => 16384 states).
 */
public final class Meld {

    public enum Type { BOOK, RUN }

    public final Type type;
    public final List<Card> cards;

    public Meld(Type type, List<Card> cards) {
        this.type = type;
        this.cards = cards;
    }

    public int size() {
        return cards.size();
    }

    @Override
    public String toString() {
        return type + cards.toString();
    }

    // ------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------

    public static boolean isValidMeld(List<Card> cards, int wildRank) {
        return validate(cards, wildRank) != null;
    }

    /** Returns the meld type if valid, otherwise null. */
    public static Type validate(List<Card> cards, int wildRank) {
        if (cards == null || cards.size() < 3) return null;
        if (isValidBook(cards, wildRank)) return Type.BOOK;
        if (isValidRun(cards, wildRank)) return Type.RUN;
        return null;
    }

    public static boolean isValidBook(List<Card> cards, int wildRank) {
        if (cards == null || cards.size() < 3) return false;
        int rank = -1;
        for (Card c : cards) {
            if (c.isWild(wildRank)) continue;
            if (rank == -1) {
                rank = c.rank;
            } else if (c.rank != rank) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidRun(List<Card> cards, int wildRank) {
        if (cards == null || cards.size() < 3) return false;

        Card.Suit suit = null;
        List<Integer> naturalRanks = new ArrayList<>();
        for (Card c : cards) {
            if (c.isWild(wildRank)) continue;
            if (suit == null) {
                suit = c.suit;
            } else if (c.suit != suit) {
                return false;
            }
            naturalRanks.add(c.rank);
        }
        if (suit == null) {
            return cards.size() <= (Card.RANK_MAX - Card.RANK_MIN + 1);
        }

        Collections.sort(naturalRanks);
        for (int i = 1; i < naturalRanks.size(); i++) {
            if (naturalRanks.get(i).intValue() == naturalRanks.get(i - 1).intValue()) {
                return false;                       // a Run cannot hold two of the same rank
            }
        }

        int low = naturalRanks.get(0);
        int high = naturalRanks.get(naturalRanks.size() - 1);
        int wilds = cards.size() - naturalRanks.size();
        int gaps = (high - low + 1) - naturalRanks.size();
        if (gaps > wilds) return false;
        int spare = wilds - gaps;
        int room = (low - Card.RANK_MIN) + (Card.RANK_MAX - high);
        return spare <= room;
    }

    public static boolean allValid(List<Meld> melds, int wildRank) {
        for (Meld m : melds) {
            if (!isValidMeld(m.cards, wildRank)) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Partition search (going out, final lay, scoring, AI)
    // ------------------------------------------------------------------

    public static final class Partition {
        public final List<Meld> melds;
        public final List<Card> leftover;

        Partition(List<Meld> melds, List<Card> leftover) {
            this.melds = melds;
            this.leftover = leftover;
        }

        public int meldedCount() {
            int n = 0;
            for (Meld m : melds) n += m.size();
            return n;
        }
    }

    /** Scratch for one hand: all valid melds as bitmasks, grouped by lowest bit. */
    private static final class HandMasks {
        final List<Card> cards;
        final int n;
        final int wildRank;
        final List<List<Integer>> meldsByLowestBit = new ArrayList<>();

        HandMasks(List<Card> cards, int wildRank) {
            this.cards = cards;
            this.wildRank = wildRank;
            this.n = cards.size();
            for (int i = 0; i < n; i++) meldsByLowestBit.add(new ArrayList<Integer>());
            int full = 1 << n;
            for (int mask = 1; mask < full; mask++) {
                if (Integer.bitCount(mask) < 3) continue;
                List<Card> sub = new ArrayList<>();
                for (int i = 0; i < n; i++) if ((mask & (1 << i)) != 0) sub.add(cards.get(i));
                if (isValidMeld(sub, wildRank)) {
                    int low = Integer.numberOfTrailingZeros(mask);
                    meldsByLowestBit.get(low).add(mask);
                }
            }
        }
    }

    /** Minimum achievable dead-wood score for the hand. */
    public static int bestScore(List<Card> hand, int wildRank) {
        if (hand.isEmpty()) return 0;
        HandMasks hm = new HandMasks(hand, wildRank);
        int[] memo = new int[1 << hm.n];
        Arrays.fill(memo, -1);
        return minScore(hm, (1 << hm.n) - 1, memo);
    }

    private static int minScore(HandMasks hm, int mask, int[] memo) {
        if (mask == 0) return 0;
        if (memo[mask] >= 0) return memo[mask];
        int i = Integer.numberOfTrailingZeros(mask);
        // Leave card i unused.
        int best = hm.cards.get(i).pointValue(hm.wildRank) + minScore(hm, mask & ~(1 << i), memo);
        // Or meld card i together with others.
        for (int m : hm.meldsByLowestBit.get(i)) {
            if ((m & mask) != m) continue;
            int v = minScore(hm, mask & ~m, memo);
            if (v < best) best = v;
        }
        memo[mask] = best;
        return best;
    }

    /**
     * Best laydown for scoring/final turn: the partition with the lowest leftover score.
     */
    public static Partition bestPartition(List<Card> hand, int wildRank) {
        if (hand.isEmpty()) return new Partition(new ArrayList<Meld>(), new ArrayList<Card>());
        HandMasks hm = new HandMasks(hand, wildRank);
        int[] memo = new int[1 << hm.n];
        Arrays.fill(memo, -1);
        int full = (1 << hm.n) - 1;
        minScore(hm, full, memo);
        List<Meld> melds = new ArrayList<>();
        List<Card> leftover = new ArrayList<>();
        rebuildMin(hm, full, memo, melds, leftover, wildRank);
        return new Partition(melds, leftover);
    }

    private static void rebuildMin(HandMasks hm, int mask, int[] memo,
                                   List<Meld> melds, List<Card> leftover, int wildRank) {
        while (mask != 0) {
            int i = Integer.numberOfTrailingZeros(mask);
            int leaveCost = hm.cards.get(i).pointValue(wildRank) + minScore(hm, mask & ~(1 << i), memo);
            boolean melded = false;
            for (int m : hm.meldsByLowestBit.get(i)) {
                if ((m & mask) != m) continue;
                if (minScore(hm, mask & ~m, memo) == memo[mask]) {
                    List<Card> sub = new ArrayList<>();
                    for (int b = 0; b < hm.n; b++) if ((m & (1 << b)) != 0) sub.add(hm.cards.get(b));
                    melds.add(new Meld(validate(sub, wildRank), sub));
                    mask &= ~m;
                    melded = true;
                    break;
                }
            }
            if (!melded && leaveCost == memo[mask]) {
                leftover.add(hm.cards.get(i));
                mask &= ~(1 << i);
            } else if (!melded) {
                // Defensive fallback: should not happen.
                leftover.add(hm.cards.get(i));
                mask &= ~(1 << i);
            }
        }
    }

    /**
     * Finds a partition covering exactly {@code exactCover} cards, or null if impossible.
     * Used for going out (exactCover = dealt count, exactly one leftover).
     */
    public static Partition findPartition(List<Card> hand, int exactCover, int wildRank) {
        if (exactCover > hand.size()) return null;
        if (hand.isEmpty()) return exactCover == 0
                ? new Partition(new ArrayList<Meld>(), new ArrayList<Card>()) : null;
        HandMasks hm = new HandMasks(hand, wildRank);
        int full = (1 << hm.n) - 1;
        List<Meld> melds = new ArrayList<>();
        List<Card> leftover = new ArrayList<>();
        if (!coverExactly(hm, full, exactCover, melds, leftover, wildRank)) return null;
        return new Partition(melds, leftover);
    }

    private static boolean coverExactly(HandMasks hm, int mask, int target,
                                        List<Meld> melds, List<Card> leftover, int wildRank) {
        if (target == 0) {
            for (int b = 0; b < hm.n; b++) if ((mask & (1 << b)) != 0) leftover.add(hm.cards.get(b));
            return true;
        }
        if (mask == 0) return false;
        int i = Integer.numberOfTrailingZeros(mask);

        for (int m : hm.meldsByLowestBit.get(i)) {
            if ((m & mask) != m) continue;
            int sz = Integer.bitCount(m);
            if (sz > target) continue;
            List<Card> sub = new ArrayList<>();
            for (int b = 0; b < hm.n; b++) if ((m & (1 << b)) != 0) sub.add(hm.cards.get(b));
            melds.add(new Meld(validate(sub, wildRank), sub));
            if (coverExactly(hm, mask & ~m, target - sz, melds, leftover, wildRank)) return true;
            melds.remove(melds.size() - 1);
        }

        // Leave card i unused (it can only be leftover when target < remaining cards).
        if (target < Integer.bitCount(mask)) {
            leftover.add(hm.cards.get(i));
            if (coverExactly(hm, mask & ~(1 << i), target, melds, leftover, wildRank)) return true;
            leftover.remove(leftover.size() - 1);
        }
        return false;
    }

    /** True when exactly {@code dealtCount} cards meld and exactly one card is left over. */
    public static boolean canGoOut(List<Card> handAfterDraw, int dealtCount, int wildRank) {
        if (handAfterDraw.size() != dealtCount + 1) return false;
        Partition p = findPartition(handAfterDraw, dealtCount, wildRank);
        return p != null && p.leftover.size() == 1;
    }

    public static int scoreLeftovers(List<Card> leftover, int wildRank) {
        int total = 0;
        for (Card c : leftover) total += c.pointValue(wildRank);
        return total;
    }
}
