package com.touchv.game;

import java.util.Locale;

/**
 * A single TouchV! card. The deck is built from two identical 58-card decks:
 * 5 suits x ranks 3..K (11 ranks) + 3 Jokers = 58 per deck, 116 total.
 * There are no Aces and no Twos.
 */
public final class Card {

    public enum Suit {
        GREEN, PINK, PURPLE, ORANGE, GOLD, JOKER;

        public boolean isRealSuit() {
            return this != JOKER;
        }
    }

    public static final int RANK_MIN = 3;   // lowest rank (no Aces, no Twos)
    public static final int RANK_MAX = 13;  // King

    public final Suit suit;
    public final int rank;   // 3..13, or 0 for a Joker
    public final int deck;   // 0 or 1, which of the two 58-card decks
    public final int uid;    // unique per card instance across the whole game

    public Card(Suit suit, int rank, int deck, int uid) {
        this.suit = suit;
        this.rank = rank;
        this.deck = deck;
        this.uid = uid;
    }

    public boolean isJoker() {
        return suit == Suit.JOKER;
    }

    /** Wild = Joker always, or the current round's rank (hand size) this round only. */
    public boolean isWild(int wildRank) {
        return isJoker() || rank == wildRank;
    }

    /** Value of an unused card. Wild rank = 20, Joker = 50, otherwise printed value. */
    public int pointValue(int wildRank) {
        if (isJoker()) return 50;
        if (rank == wildRank) return 20;
        return rank;
    }

    public String rankLabel() {
        if (isJoker()) return "JOKER";
        switch (rank) {
            case 11: return "J";
            case 12: return "Q";
            case 13: return "K";
            default: return Integer.toString(rank);
        }
    }

    public String suitLabel() {
        switch (suit) {
            case GREEN: return "CLUBS";
            case PINK: return "HEARTS";
            case PURPLE: return "FLEURS";
            case ORANGE: return "SPADES";
            case GOLD: return "DIAMONDS";
            default: return "JOKER";
        }
    }

    public String shortLabel() {
        if (isJoker()) return "JK";
        String s;
        switch (suit) {
            case GREEN: s = "C"; break;
            case PINK: s = "H"; break;
            case PURPLE: s = "F"; break;
            case ORANGE: s = "S"; break;
            case GOLD: s = "D"; break;
            default: s = "?"; break;
        }
        return rankLabel() + s;
    }

    public String fullLabel() {
        if (isJoker()) return "JOKER";
        return rankLabel() + " " + suitLabel().substring(0, 1) + suitLabel().substring(1).toLowerCase(Locale.US);
    }

    @Override
    public String toString() {
        return shortLabel();
    }
}
