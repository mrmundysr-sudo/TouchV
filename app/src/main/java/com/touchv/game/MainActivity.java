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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * TouchV! - single Activity skeleton.
 *
 * DeepSeek owns rules, game loop, interaction and placeholder UI only.
 * Grok replaces the drawables named in the handoff (section 9) 1:1; the IDs in
 * res/values/ids.xml are the stable hooks. Nothing here is a visual polish pass.
 */
public class MainActivity extends Activity {

    private GameEngine engine;
    private final Random rnd = new Random();

    private FrameLayout root;
    private FrameLayout screenTitle, screenTable, screenResult;
    private LinearLayout screenSetup;
    private FrameLayout overlayScores, overlaySort, overlayMelds;

    private LinearLayout fanTop, fanBottom;
    private FrameLayout handArea;
    private FrameLayout drawPile, discardPile;
    private TextView labelRound, labelWild, labelStatus, labelResult, labelScores;
    private LinearLayout meldPlayer, meldBoy, meldCat, meldGirl;
    private FrameLayout slotBoy, slotCat, slotGirl;
    private TextView cloudPlayer, cloudBoy, cloudCat, cloudGirl;
    private TextView countPlayer, countBoy, countCat, countGirl;
    private Button btnDeclareOut, btnLayMelds, btnParkEnd, btnContinue, btnPlayAgain;
    private View btnOpp1, btnOpp2, btnOpp3;

    private int selectedHandIndex = -1;
    private int drawnHandIndex = -1;
    private boolean sortOverlayOpen = false;
    private int sortSelected = -1;
    private int opponentCount = 1;

    private final GestureDetector fanGesture = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
            if (e1 == null || e2 == null) return false;
            float dy = e2.getY() - e1.getY();
            if (dy < -60 && Math.abs(vy) > Math.abs(vx)) {
                openSortOverlay();
                return true;
            }
            return false;
        }
    });

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        engine = new GameEngine(rnd);
        buildUi();
        showTitle();
    }

    @Override
    public void onBackPressed() {
        if (overlaySort.getVisibility() == View.VISIBLE) { closeSortOverlay(); return; }
        if (overlayMelds.getVisibility() == View.VISIBLE) { hideOverlays(); return; }
        if (overlayScores.getVisibility() == View.VISIBLE) { return; }
        if (screenTable.getVisibility() == View.VISIBLE) { showTitle(); return; }
        super.onBackPressed();
    }

    // ------------------------------------------------------------------
    // UI construction
    // ------------------------------------------------------------------

    private void buildUi() {
        root = new FrameLayout(this);
        root.setBackgroundResource(R.drawable.bg_table);
        setContentView(root);

        buildTitleScreen();
        buildTableScreen();
        buildScoreOverlay();
        buildResultScreen();
        buildSortOverlay();
        buildMeldOverlay();
    }

    private void buildTitleScreen() {
        screenTitle = new FrameLayout(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(screenTitle, lp);

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        screenTitle.addView(column, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View spacerTop = new View(this);
        column.addView(spacerTop, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        View plate = new View(this);
        plate.setId(R.id.bg_title_plate);
        plate.setBackgroundResource(R.drawable.bg_title);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(dp(300), dp(360));
        column.addView(plate, plp);

        TextView title = new TextView(this);
        title.setText("TouchV!");
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44);
        title.setGravity(Gravity.CENTER);
        column.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(70)));

        // screen_setup is the opponent pick panel; it lives on the title screen.
        screenSetup = new LinearLayout(this);
        screenSetup.setOrientation(LinearLayout.VERTICAL);
        screenSetup.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        column.addView(screenSetup, slp);

        TextView prompt = new TextView(this);
        prompt.setText("CHOOSE OPPONENTS");
        prompt.setTextColor(Color.WHITE);
        prompt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        prompt.setGravity(Gravity.CENTER);
        screenSetup.addView(prompt, rowParams(dp(48)));

        btnOpp1 = makeButton(R.id.btn_opp_1, "1 OPPONENT");
        btnOpp2 = makeButton(R.id.btn_opp_2, "2 OPPONENTS");
        btnOpp3 = makeButton(R.id.btn_opp_3, "3 OPPONENTS");
        screenSetup.addView(btnOpp1, rowParams(dp(60)));
        screenSetup.addView(btnOpp2, rowParams(dp(60)));
        screenSetup.addView(btnOpp3, rowParams(dp(60)));

        View spacerBottom = new View(this);
        column.addView(spacerBottom, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        btnOpp1.setOnClickListener(v -> startGame(1));
        btnOpp2.setOnClickListener(v -> startGame(2));
        btnOpp3.setOnClickListener(v -> startGame(3));
    }

    private void buildTableScreen() {
        screenTable = new FrameLayout(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(screenTable, lp);

        // Portrait 720x1600 -> ~360x800dp. Lay the table out as weighted vertical
        // sections so it fits without relying on a landscape pixel budget.
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        screenTable.addView(column, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // --- header -------------------------------------------------------
        FrameLayout header = new FrameLayout(this);
        column.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(64)));

        labelRound = new TextView(this);
        labelRound.setId(R.id.label_round);
        labelRound.setTextColor(Color.WHITE);
        labelRound.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        FrameLayout.LayoutParams rlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(26));
        rlp.leftMargin = dp(10);
        rlp.topMargin = dp(4);
        header.addView(labelRound, rlp);

        labelWild = new TextView(this);
        labelWild.setId(R.id.label_wild);
        labelWild.setTextColor(Color.WHITE);
        labelWild.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        labelWild.setGravity(Gravity.END);
        FrameLayout.LayoutParams wlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(26));
        wlp.gravity = Gravity.TOP | Gravity.END;
        wlp.rightMargin = dp(10);
        wlp.topMargin = dp(4);
        header.addView(labelWild, wlp);

        labelStatus = new TextView(this);
        labelStatus.setId(R.id.label_status);
        labelStatus.setTextColor(Color.WHITE);
        labelStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        labelStatus.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams stlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(30));
        stlp.topMargin = dp(30);
        header.addView(labelStatus, stlp);

        // --- opponent seats: left (boy), top (cat), right (girl) -----------
        FrameLayout seats = new FrameLayout(this);
        column.addView(seats, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.15f));

        slotBoy = buildSeat(Player.SeatKind.BOY, R.id.slot_opp_boy, R.id.cloud_opp_boy, R.id.meld_opp_boy);
        FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(
                dimen(R.dimen.seat_w), dimen(R.dimen.seat_h));
        blp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        blp.leftMargin = dp(6);
        seats.addView(slotBoy, blp);

        slotCat = buildSeat(Player.SeatKind.CAT, R.id.slot_opp_cat, R.id.cloud_opp_cat, R.id.meld_opp_cat);
        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(
                dimen(R.dimen.seat_w), dimen(R.dimen.seat_h));
        clp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        clp.topMargin = dp(4);
        seats.addView(slotCat, clp);

        slotGirl = buildSeat(Player.SeatKind.GIRL, R.id.slot_opp_girl, R.id.cloud_opp_girl, R.id.meld_opp_girl);
        FrameLayout.LayoutParams glp = new FrameLayout.LayoutParams(
                dimen(R.dimen.seat_w), dimen(R.dimen.seat_h));
        glp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        glp.rightMargin = dp(6);
        seats.addView(slotGirl, glp);

        // --- center piles -------------------------------------------------
        FrameLayout center = new FrameLayout(this);
        column.addView(center, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.75f));

        drawPile = buildPile(R.id.draw_pile, R.drawable.card_back, "DRAW");
        FrameLayout.LayoutParams dlp = new FrameLayout.LayoutParams(
                dimen(R.dimen.pile_w), dimen(R.dimen.pile_h));
        dlp.gravity = Gravity.CENTER;
        dlp.leftMargin = -(dp(46));
        center.addView(drawPile, dlp);
        drawPile.setOnClickListener(v -> onDrawTapped());

        discardPile = buildPile(R.id.discard_pile, R.drawable.card_face_green, "DISC");
        FrameLayout.LayoutParams klp = new FrameLayout.LayoutParams(
                dimen(R.dimen.pile_w), dimen(R.dimen.pile_h));
        klp.gravity = Gravity.CENTER;
        klp.leftMargin = dp(46);
        center.addView(discardPile, klp);
        discardPile.setOnClickListener(v -> onDiscardTapped());

        // --- player area: cloud, meld tray, fan ---------------------------
        FrameLayout playerArea = new FrameLayout(this);
        column.addView(playerArea, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 2.4f));

        LinearLayout playerSlot = new LinearLayout(this);
        playerSlot.setId(R.id.slot_player);
        playerSlot.setOrientation(LinearLayout.HORIZONTAL);
        playerSlot.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams pslp = new FrameLayout.LayoutParams(dp(340), dp(46));
        pslp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        pslp.topMargin = dp(2);
        playerArea.addView(playerSlot, pslp);

        cloudPlayer = new TextView(this);
        cloudPlayer.setId(R.id.cloud_player);
        cloudPlayer.setBackgroundResource(R.drawable.cloud_plate);
        cloudPlayer.setTextColor(Color.WHITE);
        cloudPlayer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        cloudPlayer.setPadding(dp(8), dp(6), dp(8), dp(6));
        playerSlot.addView(cloudPlayer, new LinearLayout.LayoutParams(dp(230), dimen(R.dimen.cloud_h)));
        cloudPlayer.setOnClickListener(v -> onOwnCloudTapped());

        countPlayer = new TextView(this);
        countPlayer.setId(R.id.count_player);
        countPlayer.setTextColor(Color.WHITE);
        countPlayer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        countPlayer.setPadding(dp(8), dp(6), dp(8), dp(6));
        // Human seat shows a score bubble only; the fan itself shows the hand.
        countPlayer.setVisibility(View.GONE);
        playerSlot.addView(countPlayer, new LinearLayout.LayoutParams(dp(100), dimen(R.dimen.cloud_h)));

        meldPlayer = new LinearLayout(this);
        meldPlayer.setId(R.id.meld_player);
        meldPlayer.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams mplp = new FrameLayout.LayoutParams(dp(348), dp(26));
        mplp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        mplp.topMargin = dp(50);
        playerArea.addView(meldPlayer, mplp);

        handArea = new FrameLayout(this);
        FrameLayout.LayoutParams halp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(212));
        halp.gravity = Gravity.TOP;
        halp.topMargin = dp(78);
        playerArea.addView(handArea, halp);

        fanTop = new LinearLayout(this);
        fanTop.setId(R.id.fan_top_row);
        fanTop.setOrientation(LinearLayout.HORIZONTAL);
        fanTop.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams ftlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(104));
        ftlp.topMargin = dp(0);
        handArea.addView(fanTop, ftlp);

        fanBottom = new LinearLayout(this);
        fanBottom.setId(R.id.fan_bottom_row);
        fanBottom.setOrientation(LinearLayout.HORIZONTAL);
        fanBottom.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams fblp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(104));
        fblp.topMargin = dp(96);
        handArea.addView(fanBottom, fblp);

        // Swipe-up anywhere on the fan (or the player cloud) opens the sort overlay.
        handArea.setOnTouchListener((v, e) -> { fanGesture.onTouchEvent(e); return false; });
        fanTop.setOnTouchListener((v, e) -> { fanGesture.onTouchEvent(e); return false; });
        fanBottom.setOnTouchListener((v, e) -> { fanGesture.onTouchEvent(e); return false; });
        // Tap empty felt inside the hand area (not on a card) deselects the locked card.
        handArea.setOnClickListener(v -> {
            if (engine.phase() == GameEngine.Phase.WAIT_DISCARD && selectedHandIndex >= 0) {
                selectedHandIndex = -1;
                refreshTable();
            }
        });
        cloudPlayer.setOnTouchListener((v, e) -> {
            fanGesture.onTouchEvent(e);
            return false;
        });

        // --- action buttons ----------------------------------------------
        btnDeclareOut = makeButton(R.id.btn_declare_out, "DECLARE OUT");
        btnLayMelds = makeButton(R.id.btn_lay_melds, "LAY MELDS");
        btnParkEnd = makeButton(R.id.btn_park_end, "PARK END");
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);
        column.addView(actions, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));
        actions.addView(btnParkEnd, new LinearLayout.LayoutParams(0, dp(48), 1f));
        actions.addView(btnDeclareOut, new LinearLayout.LayoutParams(0, dp(48), 1f));
        actions.addView(btnLayMelds, new LinearLayout.LayoutParams(0, dp(48), 1f));

        btnParkEnd.setOnClickListener(v -> onParkEnd());
        btnDeclareOut.setOnClickListener(v -> onDeclareOut());
        btnLayMelds.setOnClickListener(v -> onLayMelds());
    }

    private FrameLayout buildSeat(Player.SeatKind kind, int slotId, int cloudId, int meldId) {
        FrameLayout slot = new FrameLayout(this);
        slot.setId(slotId);

        View portrait = new View(this);
        portrait.setId(portraitIdFor(kind));
        portrait.setBackgroundResource(portraitDrawableFor(kind));
        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(dp(62), dp(62));
        plp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        slot.addView(portrait, plp);

        TextView cloud = new TextView(this);
        cloud.setId(cloudId);
        cloud.setBackgroundResource(R.drawable.cloud_plate);
        cloud.setTextColor(Color.WHITE);
        cloud.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        cloud.setPadding(dp(4), dp(2), dp(4), dp(2));
        cloud.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(
                dimen(R.dimen.cloud_w), dimen(R.dimen.cloud_h));
        clp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        clp.topMargin = dp(62);
        slot.addView(cloud, clp);

        TextView count = new TextView(this);
        count.setId(countIdFor(kind));
        count.setTextColor(Color.WHITE);
        count.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        count.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams ctlp = new FrameLayout.LayoutParams(dp(116), dp(20));
        ctlp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        ctlp.topMargin = dp(102);
        slot.addView(count, ctlp);

        LinearLayout melds = new LinearLayout(this);
        melds.setId(meldId);
        melds.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams mlp = new FrameLayout.LayoutParams(dp(116), dp(24));
        mlp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        mlp.topMargin = dp(122);
        slot.addView(melds, mlp);

        cloud.setOnClickListener(v -> onOpponentCloud(kind));
        if (kind == Player.SeatKind.BOY) cloudBoy = cloud;
        if (kind == Player.SeatKind.CAT) cloudCat = cloud;
        if (kind == Player.SeatKind.GIRL) cloudGirl = cloud;
        if (kind == Player.SeatKind.BOY) meldBoy = melds;
        if (kind == Player.SeatKind.CAT) meldCat = melds;
        if (kind == Player.SeatKind.GIRL) meldGirl = melds;
        if (kind == Player.SeatKind.BOY) countBoy = count;
        if (kind == Player.SeatKind.CAT) countCat = count;
        if (kind == Player.SeatKind.GIRL) countGirl = count;
        return slot;
    }

    private FrameLayout buildPile(int id, int bg, String label) {
        FrameLayout pile = new FrameLayout(this);
        pile.setId(id);
        pile.setBackgroundResource(bg);
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextColor(Color.WHITE);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        t.setGravity(Gravity.CENTER);
        pile.addView(t, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return pile;
    }

    private void buildScoreOverlay() {
        overlayScores = buildModal(R.id.overlay_scores, "ROUND SCORES");
        LinearLayout panel = panelOf(overlayScores);
        labelScores = new TextView(this);
        labelScores.setId(R.id.label_scores);
        labelScores.setTextColor(Color.WHITE);
        labelScores.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        labelScores.setGravity(Gravity.CENTER);
        panel.addView(labelScores, rowParams(dp(220)));
        Button cont = new Button(this);
        cont.setId(R.id.btn_continue);
        cont.setTag("continue");
        cont.setText("CONTINUE");
        cont.setOnClickListener(null);
        panel.addView(cont, rowParams(dp(60)));
        overlayScores.setVisibility(View.GONE);
    }

    private void buildResultScreen() {
        FrameLayout modal = new FrameLayout(this);
        modal.setId(R.id.screen_result);
        modal.setBackgroundColor(0xE6000000);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(modal, lp);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        plp.gravity = Gravity.CENTER;
        modal.addView(panel, plp);

        labelResult = new TextView(this);
        labelResult.setId(R.id.label_result);
        labelResult.setTextColor(Color.WHITE);
        labelResult.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
        labelResult.setGravity(Gravity.CENTER);
        panel.addView(labelResult, rowParams(dp(90)));

        btnPlayAgain = makeButton(R.id.btn_play_again, "PLAY AGAIN");
        panel.addView(btnPlayAgain, rowParams(dp(64)));
        btnPlayAgain.setOnClickListener(v -> showTitle());

        screenResult = modal;
        screenResult.setVisibility(View.GONE);
    }

    private void buildSortOverlay() {
        overlaySort = buildModal(R.id.overlay_sort, "SORT HAND");
        overlaySort.setVisibility(View.GONE);
    }

    private void buildMeldOverlay() {
        overlayMelds = buildModal(R.id.overlay_melds, "MELD REVIEW");
        overlayMelds.setVisibility(View.GONE);
    }

    // ------------------------------------------------------------------
    // Modal helpers
    // ------------------------------------------------------------------

    private FrameLayout buildModal(int id, String title) {
        FrameLayout modal = new FrameLayout(this);
        modal.setId(id);
        modal.setBackgroundColor(0xE6000000);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(modal, lp);

        ScrollView scroll = new ScrollView(this);
        FrameLayout.LayoutParams slp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.gravity = Gravity.CENTER;
        modal.addView(scroll, slp);

        LinearLayout panel = new LinearLayout(this);
        panel.setTag("panel");
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(panel);

        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        heading.setGravity(Gravity.CENTER);
        panel.addView(heading, rowParams(dp(54)));
        return modal;
    }

    private LinearLayout panelOf(View modal) {
        return (LinearLayout) modal.findViewWithTag("panel");
    }

    // ------------------------------------------------------------------
    // Screen flow
    // ------------------------------------------------------------------

    private void showTitle() {
        hideOverlays();
        screenResult.setVisibility(View.GONE);
        screenTable.setVisibility(View.GONE);
        screenTitle.setVisibility(View.VISIBLE);
    }

    private void startGame(int opponents) {
        opponentCount = opponents;
        engine.newGame(opponents);
        screenTitle.setVisibility(View.GONE);
        screenResult.setVisibility(View.GONE);
        screenTable.setVisibility(View.VISIBLE);
        screenTable.setBackgroundResource(tableBackground(opponents));
        configureSeats(opponents);
        selectedHandIndex = -1;
        drawnHandIndex = -1;
        refreshTable();
        maybeRunAi();
    }

    private int tableBackground(int opponents) {
        switch (opponents) {
            case 1: return R.drawable.bg_table_1;
            case 2: return R.drawable.bg_table_2;
            default: return R.drawable.bg_table_3;
        }
    }

    private void configureSeats(int opponents) {
        slotBoy.setVisibility(opponents >= 1 ? View.VISIBLE : View.GONE);
        slotCat.setVisibility(opponents >= 2 ? View.VISIBLE : View.GONE);
        slotGirl.setVisibility(opponents >= 3 ? View.VISIBLE : View.GONE);
    }

    private void hideOverlays() {
        overlayScores.setVisibility(View.GONE);
        overlaySort.setVisibility(View.GONE);
        overlayMelds.setVisibility(View.GONE);
        sortOverlayOpen = false;
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    private void refreshTable() {
        labelRound.setText("Round " + engine.roundNumber() + " of " + GameEngine.TOTAL_ROUNDS
                + (engine.isTiebreak() ? " (tiebreak)" : ""));
        labelWild.setText("Dealt " + engine.dealtCount() + "  |  Wild: " + engine.wildLabel()
                + " + Jokers");
        labelStatus.setText(engine.statusLine());

        // Piles
        updatePile(drawPile, engine.drawPile().isEmpty() ? null : engine.drawPile().get(0),
                "DRAW (" + engine.drawPile().size() + ")", R.drawable.card_back);
        Card top = engine.topDiscard();
        updatePile(discardPile, top, top == null ? "DISC" : top.shortLabel(),
                top == null ? R.drawable.card_face_green : faceDrawable(top));

        // Clouds
        updateCloud(cloudPlayer, countPlayer, engine.human(), "You");
        if (opponentCount >= 1) updateCloud(cloudBoy, countBoy, engine.player(1), "Boy");
        if (opponentCount >= 2) updateCloud(cloudCat, countCat, engine.player(2), "Cat");
        if (opponentCount >= 3) updateCloud(cloudGirl, countGirl, engine.player(3), "Girl");

        // Meld trays
        renderMelds(meldPlayer, engine.meldsFor(0));
        if (opponentCount >= 1) renderMelds(meldBoy, engine.meldsFor(1));
        if (opponentCount >= 2) renderMelds(meldCat, engine.meldsFor(2));
        if (opponentCount >= 3) renderMelds(meldGirl, engine.meldsFor(3));

        renderFan();
        updateButtons();
    }

    private void updateCloud(TextView cloud, TextView count, Player p, String label) {
        cloud.setText(label + " score " + p.cumulativeScore
                + (p.roundScore != 0 ? " (" + p.roundScore + ")" : ""));
        if (p.isHuman()) {
            count.setText("hand " + p.handCount() + " | best " + engine.previewScore(0));
        } else {
            count.setText("hand " + p.handCount());
        }
    }

    private void updatePile(FrameLayout pile, Card card, String label, int bg) {
        pile.setBackgroundResource(bg);
        TextView t = (TextView) pile.getChildAt(0);
        t.setText(label);
    }

    private void renderMelds(LinearLayout tray, List<Meld> melds) {
        tray.removeAllViews();
        for (Meld m : melds) {
            TextView tv = new TextView(this);
            tv.setText(m.type == Meld.Type.BOOK ? "B" : "R");
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            tv.setPadding(dp(3), dp(2), dp(3), dp(2));
            tv.setBackgroundResource(R.drawable.slot_plate);
            tray.addView(tv, new LinearLayout.LayoutParams(dp(34), dp(26)));
        }
    }

    private void renderFan() {
        fanTop.removeAllViews();
        fanBottom.removeAllViews();
        View stale = handArea.findViewWithTag("raised_card");
        if (stale != null) handArea.removeView(stale);
        List<Card> hand = engine.handOf(0);

        // Two-row fan: top row up to 7, bottom row up to 6. Round 11's 14th card is the
        // raised insert, never a third row.
        boolean insertPending = engine.phase() == GameEngine.Phase.INSERT_PENDING && drawnHandIndex >= 0;
        List<Card> parked = new ArrayList<>();
        Card raised = null;
        for (int i = 0; i < hand.size(); i++) {
            if (insertPending && i == drawnHandIndex) {
                raised = hand.get(i);
            } else {
                parked.add(hand.get(i));
            }
        }
        int topCount = Math.min(7, parked.size());
        addFanCards(fanTop, parked, 0, topCount, 0);
        if (parked.size() > 7) {
            addFanCards(fanBottom, parked, 7, parked.size(), 7);
        }
        if (raised != null) {
            // Raise the drawn card above the end of the fan so it reads as insert-pending.
            addRaisedCard(raised, drawnHandIndex);
        }
    }

    private void addRaisedCard(Card card, int handIndex) {
        FrameLayout overlay = new FrameLayout(this);
        overlay.setTag("raised_card");
        View cv = buildCardView(card);
        cv.setOnClickListener(v -> onHandCardTapped(handIndex));
        overlay.addView(cv, new FrameLayout.LayoutParams(
                dimen(R.dimen.card_w_raised), dimen(R.dimen.card_h_raised)));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                dimen(R.dimen.card_w_raised), dimen(R.dimen.card_h_raised));
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lp.bottomMargin = dp(28);
        handArea.addView(overlay, lp);
    }

    private void addFanCards(LinearLayout row, List<Card> hand, int from, int to, int offset) {
        int cardW = dimen(R.dimen.card_w);
        int cardH = dimen(R.dimen.card_h);
        int minShift = dimen(R.dimen.fan_top_overlap);
        int avail = getResources().getDisplayMetrics().widthPixels - dp(20);
        int n = to - from;
        int shift = cardW;
        if (n > 1) {
            shift = Math.min((int) (cardW * 0.72f), (avail - cardW) / (n - 1));
            shift = Math.max(shift, Math.min(minShift, cardW - dp(8)));
        }
        for (int i = from; i < to; i++) {
            int idx = offset + (i - from);
            View cv = buildCardView(hand.get(i));
            cv.setOnClickListener(v -> onHandCardTapped(idx));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(cardW, cardH);
            if (i > from) lp.leftMargin = -(cardW - shift);
            row.addView(cv, lp);
            if (idx == selectedHandIndex) cv.setTranslationY(-dimen(R.dimen.fan_lift));
        }
    }

    private View findCardView(int handIndex) {
        int i = 0;
        for (int r = 0; r < 2; r++) {
            LinearLayout row = r == 0 ? fanTop : fanBottom;
            for (int c = 0; c < row.getChildCount(); c++) {
                if (i == handIndex) return row.getChildAt(c);
                i++;
            }
        }
        return null;
    }

    private View buildCardView(Card card) {
        FrameLayout f = new FrameLayout(this);
        f.setBackgroundResource(card.isJoker() ? R.drawable.card_face_joker : faceDrawable(card));

        TextView pip = new TextView(this);
        pip.setText(card.isJoker() ? "JOKER" : card.rankLabel());
        pip.setTextColor(Color.WHITE);
        pip.setTextSize(TypedValue.COMPLEX_UNIT_SP, card.isJoker() ? 11 : 20);
        pip.setTypeface(Typeface.DEFAULT_BOLD);
        pip.setGravity(Gravity.CENTER);
        f.addView(pip, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView corner = new TextView(this);
        corner.setText(card.shortLabel());
        corner.setTextColor(Color.WHITE);
        corner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.leftMargin = dp(3);
        clp.topMargin = dp(2);
        f.addView(corner, clp);
        return f;
    }

    private void updateButtons() {
        boolean myTurn = engine.currentIndex() == 0;
        boolean canOut = engine.canDeclareOut(0);
        boolean canLay = myTurn && engine.isFinalTurn(0);
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
    // Interaction (handoff section 7)
    // ------------------------------------------------------------------

    private void onHandCardTapped(int index) {
        GameEngine.Phase ph = engine.phase();

        if (ph == GameEngine.Phase.INSERT_PENDING) {
            if (index == drawnHandIndex) {
                // Tap the raised card again to park it at the end of the fan.
                engine.parkPendingAtEnd(0);
            } else {
                // Tap a gap to slide the drawn card into that slot.
                engine.parkPending(0, index);
            }
            drawnHandIndex = -1;
            selectedHandIndex = -1;
            refreshTable();
            return;
        }

        if (ph != GameEngine.Phase.WAIT_DISCARD) {
            if (ph == GameEngine.Phase.WAIT_DRAW) {
                engine.setStatusLine("Draw before acting. Tap the draw pile or the discard pile.");
                refreshTable();
            }
            return;
        }

        if (selectedHandIndex == index) {
            selectedHandIndex = -1;      // tap the locked card again = deselect
        } else {
            selectedHandIndex = index;   // lock (hop the lock across the fan)
        }
        refreshTable();
    }

    private void onDiscardTapped() {
        if (engine.currentIndex() != 0) return;
        GameEngine.Phase ph = engine.phase();

        if (ph == GameEngine.Phase.WAIT_DRAW) {
            engine.drawHuman(0, true);
            afterHumanDraw();
            return;
        }
        if (ph == GameEngine.Phase.INSERT_PENDING) {
            engine.setStatusLine("Park the drawn card first (tap it or PARK END).");
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
                selectedHandIndex = -1;
                drawnHandIndex = -1;
                finishTurnAndRefresh();
                return;
            }
            engine.discardCard(0, selectedHandIndex);
            selectedHandIndex = -1;
            drawnHandIndex = -1;
            finishTurnAndRefresh();
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
        afterHumanDraw();
    }

    private void afterHumanDraw() {
        drawnHandIndex = engine.pendingHandIndex();
        selectedHandIndex = -1;
        refreshTable();
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

    private void onOpponentCloud(Player.SeatKind kind) {
        // Short press: cumulative score only, never cards or hidden melds.
        for (Player p : engine.players()) {
            if (p.kind == kind) {
                engine.setStatusLine(p.name + " cumulative score: " + p.cumulativeScore);
                break;
            }
        }
        refreshTable();
    }

    private void onOwnCloudTapped() {
        openSortOverlay();
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
    // Overlays
    // ------------------------------------------------------------------

    private void showScoreOverlay() {
        StringBuilder sb = new StringBuilder();
        if (engine.isGameOver() && engine.winners().size() == 1) {
            Player w = engine.player(engine.winners().get(0));
            sb.append(w.name).append(" wins the game.\n\n");
        }
        // Fixed four-seat card: unused seats read N/A rather than disappearing.
        String[] seatLabels = {"You", "Boy", "Cat", "Girl"};
        for (int i = 0; i < seatLabels.length; i++) {
            if (i < engine.players().size()) {
                Player p = engine.player(i);
                sb.append(seatLabels[i]).append(":  round ").append(p.roundScore)
                        .append("   total ").append(p.cumulativeScore).append('\n');
            } else {
                sb.append(seatLabels[i]).append(":  N/A\n");
            }
        }
        if (engine.isGameOver()) {
            sb.append("\nFinal totals above.");
        } else if (engine.isTiebreak()) {
            sb.append("\nTiebreak round (6 cards).");
        } else {
            sb.append("\nNext: round ").append(engine.roundNumber() + 1);
        }
        labelScores.setText(sb.toString());

        Button cont = overlayScores.findViewWithTag("continue");
        cont.setText("CONTINUE");
        cont.setOnClickListener(v -> {
            overlayScores.setVisibility(View.GONE);
            // Dismiss the round card first: this is where the engine decides whether the
            // 11th round ends the game or starts an Option 2 tiebreak hand.
            engine.acknowledgeRoundSummary();
            selectedHandIndex = -1;
            drawnHandIndex = -1;
            if (engine.isGameOver()) {
                showResult();
                return;
            }
            if (engine.phase() == GameEngine.Phase.ROUND_OVER) {
                engine.startRound();
            }
            refreshTable();
            maybeRunAi();
        });
        overlayScores.setVisibility(View.VISIBLE);
    }

    private void showResult() {
        boolean humanWon = engine.winners().size() == 1 && engine.winners().get(0) == 0;
        labelResult.setText(humanWon ? "YOU WIN" : "TRY AGAIN");
        screenTable.setVisibility(View.GONE);
        screenResult.setVisibility(View.VISIBLE);
    }

    private void showMeldOverlay() {
        LinearLayout panel = panelOf(overlayMelds);
        panel.removeAllViews();
        TextView title = new TextView(this);
        title.setText("MELD REVIEW");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setGravity(Gravity.CENTER);
        panel.addView(title, rowParams(dp(50)));

        for (Player p : engine.players()) {
            TextView h = new TextView(this);
            h.setText(p.name + (p.wentOut ? " (went out)" : ""));
            h.setTextColor(Color.WHITE);
            h.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            panel.addView(h, rowParams(dp(34)));
            for (Meld m : p.laidMelds) {
                TextView t = new TextView(this);
                t.setText("  " + m.type + ": " + m.cards.toString());
                t.setTextColor(Color.WHITE);
                t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                panel.addView(t, rowParams(dp(30)));
            }
        }
        Button close = new Button(this);
        close.setText("CLOSE");
        close.setOnClickListener(v -> {
            overlayMelds.setVisibility(View.GONE);
            refreshTable();
            maybeRunAi();
        });
        panel.addView(close, rowParams(dp(60)));
        overlayMelds.setVisibility(View.VISIBLE);
    }

    // ------------------------------------------------------------------
    // Sort overlay: tap-select, tap-other-swap, tap-same-deselect
    // ------------------------------------------------------------------

    private void openSortOverlay() {
        // Sorting is private mid-hand organize. It is unsafe while a drawn card is raised
        // (the pending index would go stale), so block only that phase and round-over.
        GameEngine.Phase ph = engine.phase();
        if (ph == GameEngine.Phase.INSERT_PENDING
                || ph == GameEngine.Phase.ROUND_OVER
                || ph == GameEngine.Phase.GAME_OVER) {
            engine.setStatusLine("Park the drawn card before sorting.");
            refreshTable();
            return;
        }
        sortOverlayOpen = true;
        sortSelected = -1;
        renderSortOverlay();
        overlaySort.setVisibility(View.VISIBLE);
    }

    private void renderSortOverlay() {
        LinearLayout panel = panelOf(overlaySort);
        panel.removeAllViews();

        TextView title = new TextView(this);
        title.setText("SORT HAND");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setGravity(Gravity.CENTER);
        panel.addView(title, rowParams(dp(46)));

        TextView hint = new TextView(this);
        hint.setText("tap a card to pick it, tap another to swap, tap it again to deselect");
        hint.setTextColor(Color.WHITE);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        hint.setGravity(Gravity.CENTER);
        panel.addView(hint, rowParams(dp(40)));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        panel.addView(row, rowParams(dp(116)));

        List<Card> hand = engine.handOf(0);
        int avail = getResources().getDisplayMetrics().widthPixels - dp(24);
        int cardW = dp(56);
        int shift = Math.max(dp(30), Math.min(cardW, (avail - cardW) / Math.max(1, hand.size() - 1)));
        for (int i = 0; i < hand.size(); i++) {
            final int idx = i;
            View cv = buildCardView(hand.get(i));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(cardW, dp(84));
            if (i > 0) lp.leftMargin = -(cardW - shift);
            if (idx == sortSelected) cv.setTranslationY(-dp(14));
            cv.setOnClickListener(v -> onSortTap(idx));
            row.addView(cv, lp);
        }

        Button exit = new Button(this);
        exit.setText("EXIT (freeze order)");
        exit.setOnClickListener(v -> closeSortOverlay());
        panel.addView(exit, rowParams(dp(58)));
    }

    private void onSortTap(int index) {
        if (sortSelected < 0) {
            sortSelected = index;
        } else if (sortSelected == index) {
            sortSelected = -1;
        } else {
            java.util.Collections.swap(engine.handOf(0), sortSelected, index);
            sortSelected = -1;
        }
        renderSortOverlay();
    }

    private void closeSortOverlay() {
        overlaySort.setVisibility(View.GONE);
        sortOverlayOpen = false;
        sortSelected = -1;
        refreshTable();
    }

    // ------------------------------------------------------------------
    // Small helpers
    // ------------------------------------------------------------------

    private Button makeButton(int id, String text) {
        Button b = new Button(this);
        b.setId(id);
        b.setText(text);
        b.setAllCaps(false);
        b.setBackgroundResource(R.drawable.btn_plate);
        b.setTextColor(Color.WHITE);
        return b;
    }

    private LinearLayout.LayoutParams rowParams(int height) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        return lp;
    }

    private int portraitIdFor(Player.SeatKind kind) {
        switch (kind) {
            case BOY: return R.id.portrait_opp_boy;
            case CAT: return R.id.portrait_opp_cat;
            default: return R.id.portrait_opp_girl;
        }
    }

    private int portraitDrawableFor(Player.SeatKind kind) {
        switch (kind) {
            case BOY: return R.drawable.opp_boy;
            case CAT: return R.drawable.opp_cat;
            default: return R.drawable.opp_girl;
        }
    }

    private int countIdFor(Player.SeatKind kind) {
        switch (kind) {
            case BOY: return R.id.count_opp_boy;
            case CAT: return R.id.count_opp_cat;
            default: return R.id.count_opp_girl;
        }
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    private int dimen(int resId) {
        return getResources().getDimensionPixelSize(resId);
    }
}
