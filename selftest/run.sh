#!/usr/bin/env bash
# Headless engine tests. No Android SDK required: compiles the pure-Java engine
# classes (Card, Deck, Meld, Player, GameEngine) and runs the assertions.
#
#   ./selftest/run.sh
#
set -u
cd "$(dirname "$0")/.."

JAVAC="${JAVAC:-javac}"
JAVA="${JAVA:-java}"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT

SRC="app/src/main/java/com/touchv/game"
"$JAVAC" -d "$OUT" \
  "$SRC/Card.java" "$SRC/Deck.java" "$SRC/Meld.java" \
  "$SRC/Player.java" "$SRC/GameEngine.java" || exit 1

"$JAVAC" -cp "$OUT" -d "$OUT" selftest/*.java || exit 1

status=0
for t in MeldTest GameSim HumanPathTest TiebreakTest; do
  echo "== $t"
  "$JAVA" -cp "$OUT" "$t" || status=1
done
exit $status
