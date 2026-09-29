import com.touchv.game.GameEngine;
import com.touchv.game.Meld;
import com.touchv.game.Player;

import java.util.Random;

public class GameSim {
    public static void main(String[] args) {
        int fails = 0;
        for (int opp = 1; opp <= 3; opp++) {
            for (int seed = 0; seed < 30; seed++) {
                GameEngine g = new GameEngine(new Random(seed));
                g.newGame(opp);
                int guard = 0;
                while (!g.isGameOver() && guard++ < 500000) {
                    GameEngine.Phase ph = g.phase();
                    if (ph == GameEngine.Phase.ROUND_OVER) {
                        g.acknowledgeRoundSummary();
                        if (!g.isGameOver() && g.phase() == GameEngine.Phase.ROUND_OVER) g.startRound();
                    } else if (ph == GameEngine.Phase.WAIT_DRAW || ph == GameEngine.Phase.NOT_MY_TURN) {
                        g.playAiTurnFor(g.currentIndex());
                    } else {
                        System.out.println("FAIL unexpected phase " + ph + " opp=" + opp + " seed=" + seed);
                        fails++;
                        break;
                    }
                }
                if (!g.isGameOver()) { System.out.println("FAIL stuck opp=" + opp + " seed=" + seed); fails++; continue; }
                if (g.winners().isEmpty()) { System.out.println("FAIL no winner opp=" + opp + " seed=" + seed); fails++; continue; }
                for (Player p : g.players()) {
                    if (p.cumulativeScore < 0) { System.out.println("FAIL negative score"); fails++; }
                }
            }
        }
        System.out.println(fails == 0 ? "SIM ALL PASSED" : (fails + " SIM FAILURES"));
        if (fails > 0) System.exit(1);
    }
}
