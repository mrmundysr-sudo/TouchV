package com.touchv.game;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * TouchV! - single Activity, eight-plate skin contract.
 *
 * Rules, game loop and interaction live in GameEngine / Meld and are unchanged.
 * Everything visual here is a file in res/drawable/ referenced by a locked name;
 * overlays sit on top of the plate bitmaps. Swap the file, keep the name.
 */
public class MainActivity extends Activity {

    // ------------------------------------------------------------------
    // Plate mark coordinates as fractions of a 720x1600 portrait plate.
    // Derived from the supplied marked plates; see README "marks" table.
    // ------------------------------------------------------------------
    private static final float[] TITLE_BOX = {0.182f, 0.772f, 0.699f, 0.116f};   // dirt box under wagon
    private static final float[] FAN_BOX    = {0.066f, 0.660f, 0.872f, 0.210f};  // mark 1: your cards
    private static final float[] DRAW_BOX   = {0.377f, 0.540f, 0.146f, 0.090f};  // mark 2: draw pile
    private static final float[] DISC_BOX   = {0.588f, 0.540f, 0.146f, 0.090f};  // mark 3: discard pile
    private static final float[] BUBBLE_BOX = {0.031f, 0.462f, 0.222f, 0.086f};  // mark 4: your score bubble
    private static final float[] PA_WIN_BOX = {0.010f, 0.706f, 0.340f, 0.112f};  // you-win mark 1
    private static final float[] PA_LOSE_BOX= {0.020f, 0.700f, 0.340f, 0.112f};  // try-again mark 1
    private static final float[] SORT_CARDS = {0.150f, 0.180f, 0.700f, 0.640f};  // sort mark 1
    private static final float[] SC_GIRL    = {0.120f, 0.028f, 0.215f, 0.092f};  // score-card mark 1
    private static final float[] SC_BOY     = {0.632f, 0.033f, 0.215f, 0.092f};  // score-card mark 2
    private static final float[] SC_CAT     = {0.124f, 0.529f, 0.215f, 0.092f};  // score-card mark 3
    private static final float[] SC_ME      = {0.653f, 0.543f, 0.215f, 0.092f};  // score-card mark 4

    private GameEngine engine;
    private final Random rnd = new Random();

    private FrameLayout root;
    private PlateLayout screenTitle, screenTable, screenYouWin, screenTryAgain;
    private PlateLayout overlayScores, overlaySort, overlayMelds;

    private PlateLayout fanBox;
    private LinearLayout fanTop, fanBottom;
    private FrameLayout drawPile, discardPile;
    private TextView labelRound, labelWild, labelStatus, labelScores;
    private TextView cloudPlayer;
    private LinearLayout meldPlayer;
    private Button btnDeclareOut, btnLayMelds, btnParkEnd;
    private ImageButton btnPlayAgain;
    private View btnOpp1, btnOpp2, btnOpp3;
    private TextView scoreGirl, scoreBoy, scoreCat, scorePlayer;

    private int selectedHandIndex = -1;
    private int drawnHandIndex = -1;
    private int sortSelected = -1;

    private GestureDetector swipeUp;

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        swipeUp = createSwipeUp();
        engine = new GameEngine(rnd);
        buildUi();
        showTitle();
    }

    /** Built after attachBaseContext: a field initialiser would run with no context. */
    private GestureDetector createSwipeUp() {
        return new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (e1 == null || e2 == null) return false;
                return (e2.getY() - e1.getY()) < -70 && Math.abs(vy) >= Math.abs(vx);
            }
        });
    }

    private void goFullscreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) goFullscreen();
    }

    @Override
    public void onBackPressed() {
        if (overlaySort.getVisibility() == View.VISIBLE) { closeSortOverlay(); return; }
        if (overlayMelds.getVisibility() == View.VISIBLE) { closeMeldOverlay(); return; }
        if (overlayScores.getVisibility() == View.VISIBLE) { return; }
        if (screenTable.getVisibility() == View.VISIBLE) { showTitle(); return; }
        super.onBackPressed();
    }

    // ------------------------------------------------------------------
    // UI construction
    // ------------------------------------------------------------------

    private void buildUi() {
        goFullscreen();
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);
        buildTitleScreen();
        buildTableScreen();
        buildResultPlates();
        buildSortOverlay();
        buildScoreOverlay();
        buildMeldOverlay();
    }

    /** Full-screen plate bitmap, fitXY, behind every overlay. */
    private ImageView plate(int drawableId) {
        ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        iv.setImageResource(drawableId);
        return iv;
    }

    /** Invisible tap target with a locked id. */
    private View hit(int id) {
        View v = new View(this);
        v.setId(id);
        v.setClickable(true);
        return v;
    }

    private Button makeButton(int id, String text) {
        Button b = new Button(this);
        b.setId(id);
        b.setText(text);
        b.setAllCaps(false);
        b.setBackgroundResource(R.drawable.btn_plate);
        b.setTextColor(Color.WHITE);
        return b;
    }

    private void add(PlateLayout parent, View child, float[] box) {
        parent.add(child, box[0], box[1], box[2], box[3]);
    }

    // --- title: the plate IS the 1/2/3 pick ---------------------------

    private void buildTitleScreen() {
        screenTitle = new PlateLayout(this);
        screenTitle.setId(R.id.screen_title);
        root.addView(screenTitle, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        screenTitle.add(plate(R.drawable.bg_title), 0f, 0f, 1f, 1f);

        btnOpp1 = hit(R.id.btn_opp_1);
        btnOpp2 = hit(R.id.btn_opp_2);
        btnOpp3 = hit(R.id.btn_opp_3);

        float w = TITLE_BOX[2] / 3f;
        add(screenTitle, btnOpp1, new float[]{TITLE_BOX[0], TITLE_BOX[1], w, TITLE_BOX[3]});
        add(screenTitle, btnOpp2, new float[]{TITLE_BOX[0] + w, TITLE_BOX[1], w, TITLE_BOX[3]});
        add(screenTitle, btnOpp3, new float[]{TITLE_BOX[0] + 2f * w, TITLE_BOX[1], w, TITLE_BOX[3]});

        btnOpp1.setOnClickListener(v -> startGame(1));
        btnOpp2.setOnClickListener(v -> startGame(2));
        btnOpp3.setOnClickListener(v -> startGame(3));
    }

    // --- table: three plates, same four overlays -----------------------

    private void buildTableScreen() {
        screenTable = new PlateLayout(this);
        screenTable.setId(R.id.screen_table);
        root.addView(screenTable, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ImageView tablePlate = plate(R.drawable.bg_table_1);
        screenTable.add(tablePlate, 0f, 0f, 1f, 1f);

        labelRound = chrome(R.id.label_round);
        labelWild = chrome(R.id.label_wild);
        labelRound.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        labelWild.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        add(screenTable, labelRound, new float[]{0.020f, 0.008f, 0.55f, 0.028f});
        add(screenTable, labelWild, new float[]{0.45f, 0.008f, 0.53f, 0.028f});

        labelStatus = chrome(R.id.label_status);
        labelStatus.setGravity(Gravity.CENTER);
        add(screenTable, labelStatus, new float[]{0.020f, 0.038f, 0.96f, 0.028f});

        // Mark 2: draw pile. Mark 3: discard pile.
        drawPile = new FrameLayout(this);
        drawPile.setId(R.id.draw_pile);
        drawPile.setBackgroundResource(R.drawable.card_back);
        drawPile.addView(centreLabel("DRAW"), match());
        add(screenTable, drawPile, DRAW_BOX);
        drawPile.setOnClickListener(v -> onDrawTapped());

        discardPile = new FrameLayout(this);
        discardPile.setId(R.id.discard_pile);
        discardPile.setBackgroundResource(R.drawable.card_back);
        discardPile.addView(centreLabel("DISC"), match());
        add(screenTable, discardPile, DISC_BOX);
        discardPile.setOnClickListener(v -> onDiscardTapped());

        // Mark 4: your score bubble only. No opponent clouds this pass.
        cloudPlayer = new TextView(this);
        cloudPlayer.setId(R.id.cloud_player);
        cloudPlayer.setBackgroundResource(R.drawable.cloud_plate);
        cloudPlayer.setTextColor(Color.WHITE);
        cloudPlayer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        cloudPlayer.setGravity(Gravity.CENTER);
        cloudPlayer.setPadding(dp(6), dp(2), dp(6), dp(2));
        cloudPlayer.setOnClickListener(v -> onOwnBubbleTapped());
        add(screenTable, cloudPlayer, BUBBLE_BOX);

        // Hidden opponent-cloud / seat hooks kept for a later pass.
        add(screenTable, hidden(R.id.cloud_opp_boy), new float[]{0f, 0f, 0f, 0f});
        add(screenTable, hidden(R.id.cloud_opp_cat), new float[]{0f, 0f, 0f, 0f});
        add(screenTable, hidden(R.id.cloud_opp_girl), new float[]{0f, 0f, 0f, 0f});
        add(screenTable, hidden(R.id.slot_opp_boy), new float[]{0f, 0f, 0f, 0f});
        add(screenTable, hidden(R.id.slot_opp_cat), new float[]{0f, 0f, 0f, 0f});
        add(screenTable, hidden(R.id.slot_opp_girl), new float[]{0f, 0f, 0f, 0f});
        add(screenTable, hidden(R.id.count_player), new float[]{0f, 0f, 0f, 0f});
        add(screenTable, hidden(R.id.portrait_player), new float[]{0f, 0f, 0f, 0f});

        // Mark 1: your cards. Two-row fan (7 + 6) plus the raised drawn card.
        fanBox = new PlateLayout(this);
        fanBox.setId(R.id.fan);
        screenTable.add(fanBox, FAN_BOX[0], FAN_BOX[1], FAN_BOX[2], FAN_BOX[3]);

        fanTop = new LinearLayout(this);
        fanTop.setId(R.id.fan_top_row);
        fanTop.setOrientation(LinearLayout.HORIZONTAL);
        fanTop.setGravity(Gravity.CENTER_HORIZONTAL);
        fanBox.add(fanTop, 0f, 0f, 1f, 0.52f);

        fanBottom = new LinearLayout(this);
        fanBottom.setId(R.id.fan_bottom_row);
        fanBottom.setOrientation(LinearLayout.HORIZONTAL);
        fanBottom.setGravity(Gravity.CENTER_HORIZONTAL);
        fanBox.add(fanBottom, 0f, 0.50f, 1f, 0.52f);

        fanBox.setOnClickListener(v -> onEmptyFanTapped());

        // Own meld tray (hidden until a laydown).
        meldPlayer = new LinearLayout(this);
        meldPlayer.setId(R.id.meld_player);
        meldPlayer.setOrientation(LinearLayout.HORIZONTAL);
        meldPlayer.setGravity(Gravity.CENTER);
        add(screenTable, meldPlayer, new float[]{0.30f, 0.628f, 0.40f, 0.026f});
        add(screenTable, hidden(R.id.meld_opp_boy), new float[]{0f, 0f, 0f, 0f});
        add(screenTable, hidden(R.id.meld_opp_cat), new float[]{0f, 0f, 0f, 0f});
        add(screenTable, hidden(R.id.meld_opp_girl), new float[]{0f, 0f, 0f, 0f});

        // Plain rule buttons (no plate mark exists for these).
        btnDeclareOut = makeButton(R.id.btn_declare_out, "DECLARE OUT");
        btnLayMelds = makeButton(R.id.btn_lay_melds, "LAY MELDS");
        btnParkEnd = makeButton(R.id.btn_park_end, "PARK END");
        add(screenTable, btnDeclareOut, new float[]{0.020f, 0.902f, 0.30f, 0.055f});
        add(screenTable, btnLayMelds, new float[]{0.350f, 0.902f, 0.30f, 0.055f});
        add(screenTable, btnParkEnd, new float[]{0.680f, 0.902f, 0.30f, 0.055f});
        btnDeclareOut.setOnClickListener(v -> onDeclareOut());
        btnLayMelds.setOnClickListener(v -> onLayMelds());
        btnParkEnd.setOnClickListener(v -> onParkEnd());

        // Swipe up on the felt or the fan opens the sort overlay (hand office).
        // The buttons sit below the fan, so the sort gesture never fights a tap.
        installSwipeUp(tablePlate, this::openSortOverlay);
        installSwipeUp(fanBox, this::openSortOverlay);

        screenTable.setVisibility(View.GONE);
    }

    private void installSwipeUp(View v, Runnable onSwipe) {
        v.setOnTouchListener((view, e) -> {
            // Consume the whole gesture: returning false on ACTION_DOWN would
            // drop the stream and onFling would never see the move events.
            if (swipeUp.onTouchEvent(e)) onSwipe.run();
            return true;
        });
    }

    // --- you-win / try-again: plate + transparent Play Again ----------

    private void buildResultPlates() {
        screenYouWin = resultPlate(R.id.screen_you_win, R.drawable.bg_you_win, PA_WIN_BOX, true);
        screenTryAgain = resultPlate(R.id.screen_try_again, R.drawable.bg_try_again, PA_LOSE_BOX, false);
    }

    private PlateLayout resultPlate(int screenId, int plateId, float[] paBox, boolean isWin) {
        PlateLayout p = new PlateLayout(this);
        p.setId(screenId);
        root.addView(p, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        p.add(plate(plateId), 0f, 0f, 1f, 1f);

        ImageButton pa = new ImageButton(this);
        pa.setId(R.id.btn_play_again);
        pa.setScaleType(ImageView.ScaleType.FIT_CENTER);
        pa.setImageResource(R.drawable.btn_play_again);
        pa.setBackgroundColor(Color.TRANSPARENT);
        pa.setPadding(0, 0, 0, 0);
        p.add(pa, paBox[0], paBox[1], paBox[2], paBox[3]);
        pa.setOnClickListener(v -> showTitle());
        if (isWin) btnPlayAgain = pa;

        p.setVisibility(View.GONE);
        return p;
    }

    // --- overlays -----------------------------------------------------

    private void buildSortOverlay() {
        overlaySort = new PlateLayout(this);
        overlaySort.setId(R.id.overlay_sort);
        root.addView(overlaySort, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlaySort.add(plate(R.drawable.bg_sort), 0f, 0f, 1f, 1f);

        LinearLayout row = new LinearLayout(this);
        row.setTag("sort_row");
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        overlaySort.add(row, SORT_CARDS[0], SORT_CARDS[1], SORT_CARDS[2], SORT_CARDS[3]);

        TextView hint = new TextView(this);
        hint.setTextColor(Color.WHITE);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        hint.setGravity(Gravity.CENTER);
        hint.setText("tap to pick / swap / deselect  -  swipe up to close (freezes order)");
        overlaySort.add(hint, 0.05f, 0.832f, 0.90f, 0.036f);

        overlaySort.setOnTouchListener((v, e) -> {
            if (swipeUp.onTouchEvent(e)) closeSortOverlay();
            return true;
        });
        overlaySort.setVisibility(View.GONE);
    }

    private void buildScoreOverlay() {
        overlayScores = new PlateLayout(this);
        overlayScores.setId(R.id.overlay_scores);
        root.addView(overlayScores, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayScores.add(plate(R.drawable.bg_score_card), 0f, 0f, 1f, 1f);

        scoreGirl = sticky(R.id.score_girl);
        scoreBoy = sticky(R.id.score_boy);
        scoreCat = sticky(R.id.score_cat);
        scorePlayer = sticky(R.id.score_player);
        add(overlayScores, scoreGirl, SC_GIRL);
        add(overlayScores, scoreBoy, SC_BOY);
        add(overlayScores, scoreCat, SC_CAT);
        add(overlayScores, scorePlayer, SC_ME);

        labelScores = new TextView(this);
        labelScores.setId(R.id.label_scores);
        labelScores.setTextColor(Color.WHITE);
        labelScores.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        labelScores.setGravity(Gravity.CENTER);
        overlayScores.add(labelScores, 0.05f, 0.938f, 0.90f, 0.040f);

        overlayScores.setOnTouchListener((v, e) -> {
            if (swipeUp.onTouchEvent(e)) dismissScores();
            return true;
        });
        overlayScores.setVisibility(View.GONE);
    }

    private void buildMeldOverlay() {
        overlayMelds = new PlateLayout(this);
        overlayMelds.setId(R.id.overlay_melds);
        root.addView(overlayMelds, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View scrim = new View(this);
        scrim.setBackgroundColor(0xF0000000);
        overlayMelds.add(scrim, 0f, 0f, 1f, 1f);

        ScrollView scroll = new ScrollView(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setTag("panel");
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(panel);
        overlayMelds.add(scroll, 0.06f, 0.16f, 0.88f, 0.68f);

        overlayMelds.setOnTouchListener((v, e) -> {
            if (swipeUp.onTouchEvent(e)) closeMeldOverlay();
            return true;
        });
        overlayMelds.setVisibility(View.GONE);
    }

    private TextView sticky(int id) {
        TextView t = new TextView(this);
        t.setId(id);
        t.setTextColor(Color.BLACK);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private TextView chrome(int id) {
        TextView t = new TextView(this);
        t.setId(id);
        t.setTextColor(Color.WHITE);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        t.setShadowLayer(3f, 1f, 1f, Color.BLACK);
        return t;
    }

    private View hidden(int id) {
        View v = new View(this);
        v.setId(id);
        v.setVisibility(View.GONE);
        return v;
    }

    private TextView centreLabel(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private FrameLayout.LayoutParams match() {
        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    // ------------------------------------------------------------------
    // Screen flow
    // ------------------------------------------------------------------

    private void showTitle() {
        hideOverlays();
        screenTable.setVisibility(View.GONE);
        screenYouWin.setVisibility(View.GONE);
        screenTryAgain.setVisibility(View.GONE);
        screenTitle.setVisibility(View.VISIBLE);
    }

    private void startGame(int opponents) {
        engine.newGame(opponents);
        screenTitle.setVisibility(View.GONE);
        screenYouWin.setVisibility(View.GONE);
        screenTryAgain.setVisibility(View.GONE);
        screenTable.setVisibility(View.VISIBLE);
        ((ImageView) screenTable.getChildAt(0)).setImageResource(tablePlate(opponents));
        selectedHandIndex = -1;
        drawnHandIndex = -1;
        refreshTable();
        maybeRunAi();
    }

    private int tablePlate(int opponents) {
        switch (opponents) {
            case 1: return R.drawable.bg_table_1;
            case 2: return R.drawable.bg_table_2;
            default: return R.drawable.bg_table_3;
        }
    }

    private void showResult() {
        boolean humanWon = engine.winners().size() == 1 && engine.winners().get(0) == 0;
        screenTable.setVisibility(View.GONE);
        screenYouWin.setVisibility(humanWon ? View.VISIBLE : View.GONE);
        screenTryAgain.setVisibility(humanWon ? View.GONE : View.VISIBLE);
    }

    private void hideOverlays() {
        overlayScores.setVisibility(View.GONE);
        overlaySort.setVisibility(View.GONE);
        overlayMelds.setVisibility(View.GONE);
        sortSelected = -1;
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    private void refreshTable() {
        labelRound.setText("Round " + engine.roundNumber() + " of " + GameEngine.TOTAL_ROUNDS
                + (engine.isTiebreak() ? "  (tiebreak)" : ""));
        labelWild.setText("Dealt " + engine.dealtCount() + "  |  Wild " + engine.wildLabel()
                + " + Jokers");
        labelStatus.setText(engine.statusLine());

        updatePile(drawPile, engine.drawPile().isEmpty() ? null
                : engine.drawPile().get(0), "DRAW (" + engine.drawPile().size() + ")",
                R.drawable.card_back);
        Card top = engine.topDiscard();
        updatePile(discardPile, top, top == null ? "DISC" : top.shortLabel(),
                top == null ? R.drawable.card_back : faceDrawable(top));

        Player h = engine.human();
        cloudPlayer.setText("You  " + h.cumulativeScore
                + (h.roundScore != 0 ? " (" + h.roundScore + ")" : ""));

        renderMelds(meldPlayer, engine.meldsFor(0));
        renderFan();
        updateButtons();
    }

    private void updatePile(FrameLayout pile, Card card, String label, int bg) {
        pile.setBackgroundResource(bg);
        ((TextView) pile.getChildAt(0)).setText(label);
    }

    private void renderMelds(LinearLayout tray, List<Meld> melds) {
        tray.removeAllViews();
        for (Meld m : melds) {
            TextView tv = new TextView(this);
            tv.setText(m.type == Meld.Type.BOOK ? "B" : "R");
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundResource(R.drawable.slot_plate);
            tray.addView(tv, new LinearLayout.LayoutParams(dp(26), dp(20)));
        }
    }

    private void renderFan() {
        fanTop.removeAllViews();
        fanBottom.removeAllViews();
        View stale = fanBox.findViewWithTag("raised_card");
        if (stale != null) fanBox.removeView(stale);

        List<Card> hand = engine.handOf(0);
        boolean insertPending = engine.phase() == GameEngine.Phase.INSERT_PENDING
                && drawnHandIndex >= 0 && drawnHandIndex < hand.size();

        List<Card> parked = new ArrayList<>();
        Card raised = null;
        for (int i = 0; i < hand.size(); i++) {
            if (insertPending && i == drawnHandIndex) raised = hand.get(i);
            else parked.add(hand.get(i));
        }
        int topCount = Math.min(7, parked.size());
        addFanCards(fanTop, parked, 0, topCount, 0);
        if (parked.size() > 7) addFanCards(fanBottom, parked, 7, parked.size(), 7);

        if (raised != null) addRaisedCard(raised, drawnHandIndex);
    }

    private void addRaisedCard(Card card, int handIndex) {
        FrameLayout overlay = new FrameLayout(this);
        overlay.setTag("raised_card");
        View cv = buildCardView(card);
        cv.setOnClickListener(v -> onHandCardTapped(handIndex));
        overlay.addView(cv, match());
        fanBox.add(overlay, 0.430f, -0.06f, 0.140f, 0.280f);
    }

    private void addFanCards(LinearLayout row, List<Card> hand, int from, int to, int offset) {
        int cardH = fanRowHeight();
        int cardW = (int) (cardH * 0.72f);
        int avail = Math.max(cardW, rowWidth());
        int n = to - from;
        int shift = cardW;
        if (n > 1) {
            shift = Math.min((int) (cardW * 0.72f), Math.max(1, (avail - cardW) / (n - 1)));
        }
        for (int i = from; i < to; i++) {
            int idx = offset + (i - from);
            View cv = buildCardView(hand.get(i));
            cv.setOnClickListener(v -> onHandCardTapped(idx));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(cardW, cardH);
            if (i > from) lp.leftMargin = -(cardW - shift);
            row.addView(cv, lp);
            if (idx == selectedHandIndex) cv.setTranslationY(-dp(14));
        }
    }

    private int fanRowHeight() {
        return Math.max(dp(40), (int) (getResources().getDisplayMetrics().heightPixels
                * FAN_BOX[3] * 0.46f));
    }

    private int rowWidth() {
        return (int) (getResources().getDisplayMetrics().widthPixels * FAN_BOX[2]);
    }

    private View buildCardView(Card card) {
        FrameLayout f = new FrameLayout(this);
        f.setBackgroundResource(card.isJoker() ? R.drawable.card_face_joker : faceDrawable(card));
        f.setElevation(dp(2));

        TextView pip = new TextView(this);
        pip.setText(card.isJoker() ? "JK" : card.rankLabel());
        pip.setTextColor(Color.BLACK);
        pip.setTextSize(TypedValue.COMPLEX_UNIT_SP, card.isJoker() ? 11 : 17);
        pip.setTypeface(Typeface.DEFAULT_BOLD);
        pip.setGravity(Gravity.CENTER);
        f.addView(pip, match());

        TextView corner = new TextView(this);
        corner.setText(card.shortLabel());
        corner.setTextColor(Color.BLACK);
        corner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.leftMargin = dp(3);
        clp.topMargin = dp(1);
        f.addView(corner, clp);
        return f;
    }

    private void updateButtons() {
        boolean canOut = engine.canDeclareOut(0);
        boolean canLay = engine.currentIndex() == 0 && engine.isFinalTurn(0);
        boolean insertPending = engine.phase() == GameEngine.Phase.INSERT_PENDING;
        btnDeclareOut.setVisibility(canOut ? View.VISIBLE : View.GONE);
        btnLayMelds.setVisibility(canLay ? View.VISIBLE : View.GONE);
        btnParkEnd.setVisibility(insertPending ? View.VISIBLE : View.GONE);
    }

    private int faceDrawable(Card card) {
        switch (card.suit) {
            case GREEN: return R.drawable.card_face_green;
            case PINK: return R.drawable.card_face_pink;
            case PURPLE: return R.drawable.card_face_purple;
            case ORANGE: return R.drawable.card_face_orange;
            case GOLD: return R.drawable.card_face_gold;
            default: return R.drawable.card_face_joker;
        }
    }

    // ------------------------------------------------------------------
    // Interaction
    // ------------------------------------------------------------------

    private void onHandCardTapped(int index) {
        GameEngine.Phase ph = engine.phase();

        if (ph == GameEngine.Phase.INSERT_PENDING) {
            if (index == drawnHandIndex) engine.parkPendingAtEnd(0);
            else engine.parkPending(0, index);
            drawnHandIndex = -1;
            selectedHandIndex = -1;
            refreshTable();
            return;
        }
        if (ph != GameEngine.Phase.WAIT_DISCARD) {
            if (ph == GameEngine.Phase.WAIT_DRAW) {
                engine.setStatusLine("Draw first: tap the draw pile, or the discard pile with no card locked.");
                refreshTable();
            }
            return;
        }
        selectedHandIndex = (selectedHandIndex == index) ? -1 : index;
        refreshTable();
    }

    private void onEmptyFanTapped() {
        if (engine.phase() == GameEngine.Phase.WAIT_DISCARD && selectedHandIndex >= 0) {
            selectedHandIndex = -1;
            refreshTable();
        }
    }

    private void onDrawTapped() {
        if (engine.currentIndex() != 0) return;
        if (engine.phase() != GameEngine.Phase.WAIT_DRAW) {
            engine.setStatusLine("You already drew this turn.");
            refreshTable();
            return;
        }
        engine.drawHuman(0, false);
        drawnHandIndex = engine.pendingHandIndex();
        selectedHandIndex = -1;
        refreshTable();
    }

    private void onDiscardTapped() {
        if (engine.currentIndex() != 0) return;
        GameEngine.Phase ph = engine.phase();
        if (ph == GameEngine.Phase.WAIT_DRAW) {
            engine.drawHuman(0, true);
            drawnHandIndex = engine.pendingHandIndex();
            selectedHandIndex = -1;
            refreshTable();
            return;
        }
        if (ph == GameEngine.Phase.INSERT_PENDING) {
            engine.setStatusLine("Park the drawn card first.");
            refreshTable();
            return;
        }
        if (ph == GameEngine.Phase.WAIT_DISCARD) {
            if (selectedHandIndex < 0) {
                engine.setStatusLine("Tap a card to lock it, then tap the discard pile.");
                refreshTable();
                return;
            }
            if (engine.canDeclareOut(0)) {
                engine.declareOut(0);
            } else {
                engine.discardCard(0, selectedHandIndex);
            }
            selectedHandIndex = -1;
            drawnHandIndex = -1;
            finishTurnAndRefresh();
        }
    }

    private void onParkEnd() {
        if (engine.phase() == GameEngine.Phase.INSERT_PENDING) {
            engine.parkPendingAtEnd(0);
            drawnHandIndex = -1;
            refreshTable();
        }
    }

    private void onDeclareOut() {
        if (engine.canDeclareOut(0)) {
            engine.declareOut(0);
            selectedHandIndex = -1;
            drawnHandIndex = -1;
            finishTurnAndRefresh();
        }
    }

    private void onLayMelds() {
        if (engine.isFinalTurn(0)) {
            engine.layMelds(0);
            showMeldOverlay();
        }
    }

    private void onOwnBubbleTapped() {
        showScoreOverlay();
    }

    private void finishTurnAndRefresh() {
        refreshTable();
        if (engine.phase() == GameEngine.Phase.ROUND_OVER || engine.isGameOver()) {
            showScoreOverlay();
            return;
        }
        maybeRunAi();
    }

    // ------------------------------------------------------------------
    // AI driving
    // ------------------------------------------------------------------

    private void maybeRunAi() {
        int guard = 0;
        while (guard++ < 5000) {
            GameEngine.Phase ph = engine.phase();
            if (ph == GameEngine.Phase.ROUND_OVER || engine.isGameOver()) {
                showScoreOverlay();
                return;
            }
            if ((ph == GameEngine.Phase.WAIT_DRAW || ph == GameEngine.Phase.NOT_MY_TURN)
                    && engine.currentIndex() != 0) {
                engine.playAiTurnFor(engine.currentIndex());
                continue;
            }
            break;
        }
        refreshTable();
    }

    // ------------------------------------------------------------------
    // Score card
    // ------------------------------------------------------------------

    private void showScoreOverlay() {
        // Sticky per seat: girl, boy, cat, you. A seat not in this match reads N/A.
        scoreGirl.setText(seatScore(Player.SeatKind.GIRL));
        scoreBoy.setText(seatScore(Player.SeatKind.BOY));
        scoreCat.setText(seatScore(Player.SeatKind.CAT));
        scorePlayer.setText(seatScore(Player.SeatKind.HUMAN));

        StringBuilder sb = new StringBuilder();
        if (engine.isGameOver() && engine.winners().size() == 1) {
            sb.append(engine.player(engine.winners().get(0)).name).append(" wins the game.  ");
        } else if (engine.isTiebreak()) {
            sb.append("Tiebreak round (6 cards).  ");
        } else {
            sb.append("Next: round ").append(engine.roundNumber() + 1).append(".  ");
        }
        sb.append("Swipe up to continue.");
        labelScores.setText(sb.toString());

        overlayScores.setVisibility(View.VISIBLE);
    }

    private String seatScore(Player.SeatKind kind) {
        Player p = findSeat(kind);
        if (p == null) return "N/A";
        return "R " + p.roundScore + "\nT " + p.cumulativeScore;
    }

    private Player findSeat(Player.SeatKind kind) {
        for (Player p : engine.players()) if (p.kind == kind) return p;
        return null;
    }

    private void dismissScores() {
        overlayScores.setVisibility(View.GONE);
        engine.acknowledgeRoundSummary();
        selectedHandIndex = -1;
        drawnHandIndex = -1;
        if (engine.isGameOver()) {
            showResult();
            return;
        }
        if (engine.phase() == GameEngine.Phase.ROUND_OVER) engine.startRound();
        refreshTable();
        maybeRunAi();
    }

    // ------------------------------------------------------------------
    // Sort overlay: tap-select, tap-other-swap, tap-same-deselect
    // ------------------------------------------------------------------

    private void openSortOverlay() {
        GameEngine.Phase ph = engine.phase();
        if (ph == GameEngine.Phase.INSERT_PENDING
                || ph == GameEngine.Phase.ROUND_OVER
                || ph == GameEngine.Phase.GAME_OVER) {
            engine.setStatusLine("Park the drawn card before sorting.");
            refreshTable();
            return;
        }
        sortSelected = -1;
        renderSortOverlay();
        overlaySort.setVisibility(View.VISIBLE);
    }

    private void renderSortOverlay() {
        LinearLayout row = (LinearLayout) overlaySort.findViewWithTag("sort_row");
        row.removeAllViews();

        List<Card> hand = engine.handOf(0);
        int cardH = Math.max(dp(50), (int) (getResources().getDisplayMetrics().heightPixels
                * SORT_CARDS[3] * 0.42f));
        int cardW = (int) (cardH * 0.72f);
        int avail = Math.max(cardW, (int) (getResources().getDisplayMetrics().widthPixels
                * SORT_CARDS[2]));
        int n = hand.size();
        int shift = n > 1
                ? Math.min((int) (cardW * 0.7f), Math.max(1, (avail - cardW) / (n - 1)))
                : cardW;

        for (int i = 0; i < n; i++) {
            final int idx = i;
            View cv = buildCardView(hand.get(i));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(cardW, cardH);
            if (i > 0) lp.leftMargin = -(cardW - shift);
            if (idx == sortSelected) cv.setTranslationY(-dp(16));
            cv.setOnClickListener(v -> onSortTap(idx));
            row.addView(cv, lp);
        }
    }

    private void onSortTap(int index) {
        if (sortSelected < 0) {
            sortSelected = index;
        } else if (sortSelected == index) {
            sortSelected = -1;
        } else {
            Collections.swap(engine.handOf(0), sortSelected, index);
            sortSelected = -1;
        }
        renderSortOverlay();
    }

    private void closeSortOverlay() {
        overlaySort.setVisibility(View.GONE);
        sortSelected = -1;
        refreshTable();
    }

    // ------------------------------------------------------------------
    // Meld review overlay
    // ------------------------------------------------------------------

    private void showMeldOverlay() {
        LinearLayout panel = (LinearLayout) overlayMelds.findViewWithTag("panel");
        panel.removeAllViews();

        TextView title = new TextView(this);
        title.setText("MELD REVIEW");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setGravity(Gravity.CENTER);
        panel.addView(title, rowParams(dp(44)));

        for (Player p : engine.players()) {
            TextView h = new TextView(this);
            h.setText(p.name + (p.wentOut ? "  (went out)" : "")
                    + "   round " + p.roundScore + " / total " + p.cumulativeScore);
            h.setTextColor(Color.WHITE);
            h.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            panel.addView(h, rowParams(dp(30)));
            for (Meld m : p.laidMelds) {
                TextView t = new TextView(this);
                t.setText("   " + m.type + ": " + m.cards.toString());
                t.setTextColor(0xFFCCCCCC);
                t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                panel.addView(t, rowParams(dp(26)));
            }
        }

        Button close = makeButton(R.id.btn_continue, "CONTINUE");
        close.setOnClickListener(v -> closeMeldOverlay());
        panel.addView(close, rowParams(dp(54)));
        overlayMelds.setVisibility(View.VISIBLE);
    }

    private void closeMeldOverlay() {
        overlayMelds.setVisibility(View.GONE);
        refreshTable();
        maybeRunAi();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private LinearLayout.LayoutParams rowParams(int height) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        return lp;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }
}
