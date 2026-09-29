package com.touchv.game;

import java.util.ArrayList;
import java.util.List;

/**
 * One seat in the game. Seat 0 is always the human (bottom). Opponents fill
 * left (boy), top (cat), right (girl) per the handoff seat table.
 */
public final class Player {

    public enum SeatKind { HUMAN, BOY, CAT, GIRL }

    public final int index;          // 0 = human
    public final SeatKind kind;
    public final String name;

    public final List<Card> hand = new ArrayList<>();
    public final List<Meld> laidMelds = new ArrayList<>();
    public int cumulativeScore = 0;
    public int roundScore = 0;
    public boolean wentOut = false;
    public boolean laidDownThisRound = false;

    public Player(int index, SeatKind kind, String name) {
        this.index = index;
        this.kind = kind;
        this.name = name;
    }

    public boolean isHuman() {
        return kind == SeatKind.HUMAN;
    }

    public int handCount() {
        return hand.size();
    }

    /** Drawable hook name for this seat's portrait, e.g. "opp_boy". */
    public String portraitDrawable() {
        switch (kind) {
            case BOY: return "opp_boy";
            case CAT: return "opp_cat";
            case GIRL: return "opp_girl";
            default: return null;
        }
    }

    public void resetForRound() {
        hand.clear();
        laidMelds.clear();
        roundScore = 0;
        wentOut = false;
        laidDownThisRound = false;
    }
}
