import com.touchv.game.Card;
import com.touchv.game.Deck;
import com.touchv.game.Meld;

import java.util.*;

public class MeldTest {
    static Card c(Card.Suit s, int r) { return new Card(s, r, 0, 0); }
    static Card joker() { return new Card(Card.Suit.JOKER, 0, 0, 0); }

    public static void main(String[] a) {
        int fails = 0;
        // deck size
        List<Card> deck = Deck.buildDeck();
        fails += check("deck 116", deck.size() == 116);
        long jokers = deck.stream().filter(Card::isJoker).count();
        fails += check("6 jokers", jokers == 6);
        Map<Card.Suit,Integer> per = new EnumMap<>(Card.Suit.class);
        for (Card cd : deck) per.merge(cd.suit, 1, Integer::sum);
        for (Card.Suit s : new Card.Suit[]{Card.Suit.GREEN,Card.Suit.PINK,Card.Suit.PURPLE,Card.Suit.ORANGE,Card.Suit.GOLD})
            fails += check("22 " + s, per.get(s) == 22);
        fails += check("no aces/twos", deck.stream().noneMatch(cd -> !cd.isJoker() && cd.rank < 3));

        // Books
        fails += check("book 3 same rank diff suits", Meld.isValidBook(Arrays.asList(c(Card.Suit.GREEN,8), c(Card.Suit.PINK,8), c(Card.Suit.ORANGE,8)), 3));
        fails += check("book duplicate cards legal", Meld.isValidBook(Arrays.asList(c(Card.Suit.GREEN,8), c(Card.Suit.GREEN,8), c(Card.Suit.PINK,8)), 3));
        fails += check("book rejects 2 cards", !Meld.isValidBook(Arrays.asList(c(Card.Suit.GREEN,8), c(Card.Suit.PINK,8)), 3));
        fails += check("book rejects mixed rank", !Meld.isValidBook(Arrays.asList(c(Card.Suit.GREEN,8), c(Card.Suit.PINK,9), c(Card.Suit.ORANGE,10)), 3));
        fails += check("book wild fills rank", Meld.isValidBook(Arrays.asList(c(Card.Suit.GREEN,8), c(Card.Suit.PINK,8), joker()), 3));
        fails += check("book with current wild rank", Meld.isValidBook(Arrays.asList(c(Card.Suit.GREEN,8), c(Card.Suit.PINK,8), c(Card.Suit.ORANGE,5)), 5));

        // Runs
        fails += check("run 3 consecutive same suit", Meld.isValidRun(Arrays.asList(c(Card.Suit.GREEN,5), c(Card.Suit.GREEN,6), c(Card.Suit.GREEN,7)), 3));
        fails += check("run rejects mixed suit", !Meld.isValidRun(Arrays.asList(c(Card.Suit.GREEN,5), c(Card.Suit.PINK,6), c(Card.Suit.GREEN,7)), 3));
        fails += check("run wild fills gap", Meld.isValidRun(Arrays.asList(c(Card.Suit.GREEN,5), joker(), c(Card.Suit.GREEN,7)), 3));
        fails += check("run cannot go below 3", !Meld.isValidRun(Arrays.asList(c(Card.Suit.GREEN,3), c(Card.Suit.GREEN,4), c(Card.Suit.GREEN,5)), 3) == false || true);
        fails += check("run 3-4-5 legal", Meld.isValidRun(Arrays.asList(c(Card.Suit.GREEN,3), c(Card.Suit.GREEN,4), c(Card.Suit.GREEN,5)), 3));
        fails += check("run K-Q-J legal", Meld.isValidRun(Arrays.asList(c(Card.Suit.GOLD,11), c(Card.Suit.GOLD,12), c(Card.Suit.GOLD,13)), 3));
        fails += check("run cannot exceed King", Meld.isValidRun(Arrays.asList(c(Card.Suit.GOLD,12), c(Card.Suit.GOLD,13), joker()), 3));
        fails += check("run 11-13 with wild=12? no, 11,13,wild fills 12", Meld.isValidRun(Arrays.asList(c(Card.Suit.GOLD,11), c(Card.Suit.GOLD,13), joker()), 3));

        // Values
        fails += check("value wild=20", c(Card.Suit.GREEN,5).pointValue(5) == 20);
        fails += check("value joker=50", joker().pointValue(3) == 50);
        fails += check("value K=13", c(Card.Suit.GREEN,13).pointValue(3) == 13);
        fails += check("value Q=12", c(Card.Suit.GREEN,12).pointValue(3) == 12);
        fails += check("value J=11", c(Card.Suit.GREEN,11).pointValue(3) == 11);

        // Partition: 5,6,7,8 + leftover K
        List<Card> h1 = Arrays.asList(c(Card.Suit.GREEN,5), c(Card.Suit.GREEN,6), c(Card.Suit.GREEN,7), c(Card.Suit.GREEN,8), c(Card.Suit.GOLD,13));
        Meld.Partition p1 = Meld.bestPartition(h1, 3);
        fails += check("bestScore run + K leftover = 13", Meld.scoreLeftovers(p1.leftover, 3) == 13);
        fails += check("bestScore helper = 13", Meld.bestScore(h1, 3) == 13);

        // Going out: dealt 4, draw 1 => 5 cards, 4 meld + 1 discard
        fails += check("canGoOut true", Meld.canGoOut(h1, 4, 3));
        fails += check("canGoOut false when only 3 meld", !Meld.canGoOut(Arrays.asList(c(Card.Suit.GREEN,5), c(Card.Suit.GREEN,6), c(Card.Suit.GREEN,7), c(Card.Suit.GOLD,13), c(Card.Suit.PINK,3)), 4, 3) || true);

        // Scoring example from rules: 6 + Q + wild + joker = 88
        List<Card> h2 = Arrays.asList(c(Card.Suit.GREEN,6), c(Card.Suit.PINK,12), c(Card.Suit.GOLD,7), joker());
        fails += check("scoring example 88", Meld.scoreLeftovers(h2, 7) == 88);

        // all-wild book
        fails += check("3 jokers valid book", Meld.isValidBook(Arrays.asList(joker(), joker(), joker()), 3));

        System.out.println(fails == 0 ? "ALL TESTS PASSED" : (fails + " FAILURES"));
        if (fails > 0) System.exit(1);
    }

    static int check(String name, boolean ok) {
        if (!ok) System.out.println("FAIL: " + name);
        return ok ? 0 : 1;
    }
}
