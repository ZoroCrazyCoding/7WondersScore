package com.example.sevenwondersscore;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GameDetailActivity extends AppCompatActivity {

    TableLayout tableDetail;
    TextView tvGameInfo;
    String gameType;

    String[] symbols7Wonders = {"⛰","🪙","⚔️","🟦","🟧","🟩","🟪"};
    String[] symbolsDuel = {"🟦","🟩","🟨","🟪","⛰","🟢","🪙","⚔️"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Game Score");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if(toolbar.getNavigationIcon() != null)
            toolbar.getNavigationIcon().setTint(Color.parseColor("#FFD700"));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tableDetail = findViewById(R.id.tableDetail);
        tvGameInfo = findViewById(R.id.tvGameInfo);

        // Ricevi i dati della partita
        GameResult gameResult = (GameResult) getIntent().getSerializableExtra("gameResult");
        gameType = getIntent().getStringExtra("gameType");

        if(gameResult != null) {
            displayGameDetails(gameResult);
        }
    }

    private void displayGameDetails(GameResult result) {
        boolean isDuel = "7wondersduel".equals(gameType);
        String[] symbols = isDuel ? symbolsDuel : symbols7Wonders;

        // Info partita
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String infoText = "Date: " + sdf.format(new Date(result.getTimestamp())) + "\n";
        infoText += "Winner: " + result.getWinnerName() + "\n";

        if(result.getVictoryType() != null && !result.getVictoryType().equals("points")) {
            if(result.getVictoryType().equals("military")) {
                infoText += "Victory Type: ⚔️ Military Supremacy\n";
            } else if(result.getVictoryType().equals("scientific")) {
                infoText += "Victory Type: 🔬 Scientific Supremacy\n";
            }
        } else {
            infoText += "Victory Type: Points\n";
        }

        // Mostra numero giocatori SOLO SE NON È DUEL (2 giocatori)
        if(result.getNumPlayers() != 2) {
            infoText += "Players: " + result.getNumPlayers();
        }

        tvGameInfo.setText(infoText);

        // Crea tabella
        tableDetail.removeAllViews();

        // Header con nomi giocatori
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.parseColor("#E0E0E0"));
        headerRow.setPadding(0, 16, 0, 16);
        headerRow.setGravity(Gravity.CENTER);

        TextView tvHeaderSymbol = new TextView(this);
        tvHeaderSymbol.setText("Category");
        tvHeaderSymbol.setGravity(Gravity.CENTER);
        tvHeaderSymbol.setPadding(16, 8, 16, 8);
        tvHeaderSymbol.setTypeface(null, android.graphics.Typeface.BOLD);
        headerRow.addView(tvHeaderSymbol);

        for(GameResult.PlayerResult player : result.getPlayers()) {
            TextView tvName = new TextView(this);
            tvName.setText(player.getName());
            tvName.setGravity(Gravity.CENTER);
            tvName.setPadding(16, 8, 16, 8);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            tvName.setTextSize(14);

            // Evidenzia il vincitore
            if(player.getName().equals(result.getWinnerName())) {
                tvName.setTextColor(Color.parseColor("#1976D2"));
            }

            headerRow.addView(tvName);
        }

        tableDetail.addView(headerRow);

        // Righe con i punteggi per categoria
        for(int i = 0; i < symbols.length; i++) {
            TableRow row = new TableRow(this);
            row.setPadding(0, 8, 0, 8);
            row.setGravity(Gravity.CENTER);

            // Simbolo categoria
            TextView tvSymbol = new TextView(this);
            tvSymbol.setText(symbols[i]);
            tvSymbol.setTextSize(24);
            tvSymbol.setGravity(Gravity.CENTER);
            tvSymbol.setPadding(16, 8, 16, 8);
            row.addView(tvSymbol);

            // Punteggi per ogni giocatore
            for(GameResult.PlayerResult player : result.getPlayers()) {
                TextView tvScore = new TextView(this);
                List<Integer> scores = player.getCategoryScores();
                int score = (i < scores.size()) ? scores.get(i) : 0;
                tvScore.setText(String.valueOf(score));
                tvScore.setGravity(Gravity.CENTER);
                tvScore.setPadding(16, 8, 16, 8);
                tvScore.setTextSize(16);
                row.addView(tvScore);
            }

            tableDetail.addView(row);

            // Separatore
            View divider = new View(this);
            divider.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(Color.parseColor("#CCCCCC"));
            tableDetail.addView(divider);
        }

        // Riga totale
        TableRow totalRow = new TableRow(this);
        totalRow.setBackgroundColor(Color.parseColor("#F5F5F5"));
        totalRow.setPadding(0, 16, 0, 16);
        totalRow.setGravity(Gravity.CENTER);

        TextView tvTotalLabel = new TextView(this);
        tvTotalLabel.setText("∑");
        tvTotalLabel.setGravity(Gravity.CENTER);
        tvTotalLabel.setPadding(16, 8, 16, 8);
        tvTotalLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTotalLabel.setTextSize(22);
        totalRow.addView(tvTotalLabel);

        for(GameResult.PlayerResult player : result.getPlayers()) {
            TextView tvTotal = new TextView(this);
            tvTotal.setText(String.valueOf(player.getTotalScore()));
            tvTotal.setGravity(Gravity.CENTER);
            tvTotal.setPadding(16, 8, 16, 8);
            tvTotal.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTotal.setTextSize(18);

            // Evidenzia il vincitore
            if(player.getName().equals(result.getWinnerName())) {
                tvTotal.setTextColor(Color.parseColor("#1976D2"));
            }

            totalRow.addView(tvTotal);
        }

        tableDetail.addView(totalRow);

        // CHECKBOX PER SUPREMAZIE (SOLO DUEL)
        if(isDuel) {
            TableRow cbRow = new TableRow(this);
            cbRow.setPadding(0, 16, 0, 0);
            cbRow.setGravity(Gravity.CENTER);

            // Colonna vuota per allineare con la prima colonna
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText(" ");
            tvEmpty.setPadding(16, 8, 16, 8);
            cbRow.addView(tvEmpty);

            // Determina quale giocatore ha vinto per supremazia (se c'è una vittoria per supremazia)
            String winnerName = result.getWinnerName();
            String victoryType = result.getVictoryType();
            boolean hasMilitaryVictory = "military".equals(victoryType);
            boolean hasScientificVictory = "scientific".equals(victoryType);

            // Crea le checkbox per ogni giocatore
            for(GameResult.PlayerResult player : result.getPlayers()) {
                LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.setGravity(Gravity.CENTER);

                TableRow.LayoutParams llParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                llParams.gravity = Gravity.CENTER;
                ll.setLayoutParams(llParams);

                // Checkbox rossa (Military Supremacy)
                CheckBox cbRed = new CheckBox(this);
                cbRed.setButtonTintList(android.content.res.ColorStateList.valueOf(
                        Color.parseColor("#B90E0A")
                ));
                cbRed.setEnabled(false); // Disabilita l'interazione (solo visualizzazione)

                // Seleziona se questo giocatore ha vinto per supremazia militare
                if(hasMilitaryVictory && player.getName().equals(winnerName)) {
                    cbRed.setChecked(true);
                }

                // Checkbox verde (Scientific Supremacy)
                CheckBox cbGreen = new CheckBox(this);
                cbGreen.setButtonTintList(android.content.res.ColorStateList.valueOf(
                        Color.parseColor("#02971F")
                ));
                cbGreen.setEnabled(false); // Disabilita l'interazione (solo visualizzazione)

                // Seleziona se questo giocatore ha vinto per supremazia scientifica
                if(hasScientificVictory && player.getName().equals(winnerName)) {
                    cbGreen.setChecked(true);
                }

                ll.addView(cbRed);
                ll.addView(cbGreen);
                cbRow.addView(ll);
            }

            tableDetail.addView(cbRow);
        }
    }
}