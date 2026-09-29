import com.touchv.game.*;

import java.util.List;
import java.util.Random;

/**
 * Drives seat 0 through the exact call sequence MainActivity uses, so the human
 * UI path (drawHuman / parkPending / discardCard / declareOut) is exercised and
 * cannot deadlock. AI seats use playAiTurnFor.
 */
public class HumanPathTest {

    static int fails = 0;

    public static void main(String[] args) {
        for (int seed = 0; seed < 25; seed++) {
            runOne(seed, 1 + (seed % 3));
        }
        System.out.println(fails == 0 ? "HUMAN PATH PASSED" : ("HUMAN PATH FAILED: " + fails));
        if (fails != 0) System.exit(1);
    }

    static void runOne(int seed, int opponents) {
        Random rnd = new Random(seed * 7919L + opponents);
        GameEngine g = new GameEngine(rnd);
        g.newGame(opponents);

        int guard = 0;
        while (!g.isGameOver() && guard++ < 200000) {
            GameEngine.Phase ph = g.phase();

            if (ph == GameEngine.Phase.ROUND_OVER) {
                g.acknowledgeRoundSummary();
                if (!g.isGameOver() && g.phase() == GameEngine.Phase.ROUND_OVER) g.startRound();
                continue;
            }

            if (g.currentIndex() != 0) {
                g.playAiTurnFor(g.currentIndex());
                continue;
            }

            // ---- human seat, mirroring MainActivity handlers ----
            if (ph == GameEngine.Phase.WAIT_DRAW) {
                if (rnd.nextBoolean()) g.drawHuman(0, true);
                else g.drawHuman(0, false);
                if (g.phase() == GameEngine.Phase.INSERT_PENDING) {
                    int idx = g.pendingHandIndex();
                    if (rnd.nextInt(3) == 0) g.parkPendingAtEnd(0);
                    else g.parkPending(0, Math.max(0, idx - rnd.nextInt(3)));
                }
                continue;
            }
            if (ph == GameEngine.Phase.INSERT_PENDING) {
                g.parkPendingAtEnd(0);
                continue;
            }
            if (ph == GameEngine.Phase.WAIT_DISCARD) {
                if (g.canDeclareOut(0)) {
                    g.declareOut(0);
                    continue;
                }
                List<Card> hand = g.handOf(0);
                if (hand.isEmpty()) { fails++; return; }
                int pick = rnd.nextInt(hand.size());
                g.discardCard(0, pick);
                continue;
            }
            if (ph == GameEngine.Phase.NOT_MY_TURN) { fails++; return; }
            if (ph == GameEngine.Phase.GAME_OVER) break;
        }

        if (guard >= 200000) { fails++; System.out.println("seed " + seed + " STUCK"); return; }
        if (!g.isGameOver()) { fails++; System.out.println("seed " + seed + " not over"); return; }
        if (g.winners().size() != 1) { fails++; System.out.println("seed " + seed + " winners " + g.winners()); }
    }
}
