package com.touchv.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Builds and shuffles the exact 116-card TouchV! deck. */
public final class Deck {

    private Deck() {
    }

    /**
     * One 58-card deck = 5 suits x 11 ranks (3..K) + 3 Jokers.
     * Play deck = two of those = 116 cards, 6 Jokers, 22 cards per suit.
     */
    public static List<Card> buildDeck() {
        List<Card> cards = new ArrayList<>(116);
        int uid = 0;
        for (int deck = 0; deck < 2; deck++) {
            for (Card.Suit suit : new Card.Suit[]{Card.Suit.GREEN, Card.Suit.PINK,
                    Card.Suit.PURPLE, Card.Suit.ORANGE, Card.Suit.GOLD}) {
                for (int rank = Card.RANK_MIN; rank <= Card.RANK_MAX; rank++) {
                    cards.add(new Card(suit, rank, deck, uid++));
                }
            }
            for (int j = 0; j < 3; j++) {
                cards.add(new Card(Card.Suit.JOKER, 0, deck, uid++));
            }
        }
        if (cards.size() != 116) {
            throw new IllegalStateException("Deck must be 116 cards, was " + cards.size());
        }
        return cards;
    }

    public static void shuffle(List<Card> cards, Random rnd) {
        Collections.shuffle(cards, rnd);
    }
}
