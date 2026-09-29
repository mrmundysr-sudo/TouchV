import com.touchv.game.GameEngine;
import com.touchv.game.Player;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

/** Forces the round-11 tie path and verifies the 6-card Option 2 tiebreak resolves. */
public class TiebreakTest {
    public static void main(String[] args) throws Exception {
        int fails = 0;
        for (int seed = 0; seed < 10; seed++) {
            GameEngine g = new GameEngine(new Random(seed));
            g.newGame(2);
            // Jump to end of round 11 by forcing cumulative scores equal.
            setInt(g, "roundNumber", 11);
            for (Player p : g.players()) p.cumulativeScore = 50;
            Method resolve = GameEngine.class.getDeclaredMethod("resolveEndOfGame");
            resolve.setAccessible(true);
            resolve.invoke(g);
            if (!g.isTiebreak()) { System.out.println("FAIL expected tiebreak seed=" + seed); fails++; continue; }
            int guard = 0;
            java.util.Set<Integer> dealtSeen = new java.util.TreeSet<>();
            while (!g.isGameOver() && guard++ < 100000) {
                if (g.isTiebreak()) dealtSeen.add(g.dealtCount());
                GameEngine.Phase ph = g.phase();
                if (ph == GameEngine.Phase.ROUND_OVER) {
                    g.acknowledgeRoundSummary();
                    if (!g.isGameOver() && g.phase() == GameEngine.Phase.ROUND_OVER) g.startRound();
                } else if (ph == GameEngine.Phase.WAIT_DRAW || ph == GameEngine.Phase.NOT_MY_TURN) {
                    g.playAiTurnFor(g.currentIndex());
                } else break;
            }
            if (!g.isGameOver()) { System.out.println("FAIL tiebreak stuck seed=" + seed); fails++; continue; }
            List<Integer> w = g.winners();
            if (w.size() != 1) { System.out.println("FAIL expected single winner seed=" + seed + " got " + w); fails++; }
            if (!dealtSeen.isEmpty() && !dealtSeen.equals(java.util.Set.of(6))) {
                System.out.println("FAIL tiebreak dealt counts " + dealtSeen); fails++;
            }
            if (dealtSeen.isEmpty()) { System.out.println("FAIL tiebreak never played"); fails++; }
            System.out.println("seed " + seed + " winner=" + w + " tiebreak=" + g.isTiebreak());
        }
        System.out.println(fails == 0 ? "TIEBREAK PASSED" : (fails + " TIEBREAK FAILURES"));
        if (fails > 0) System.exit(1);
    }

    static void setInt(Object o, String name, int v) throws Exception {
        Field f = GameEngine.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setInt(o, v);
    }
}
