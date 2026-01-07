package com.example.sevenwondersscore;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.*;
import android.graphics.Color;

public class NewGameActivity extends AppCompatActivity {

    TableLayout tableMain;
    LinearLayout layoutContainer, layoutNumPlayers;
    EditText[][] scoreCells;
    Button btnFinished;
    int numPlayers;
    boolean isDuel = false;

    List<CheckBox> duelCheckboxes = new ArrayList<>();
    Map<String, PlayerStats> statistics = new HashMap<>();

    String[] symbols7Wonders = {"⛰","🪙","⚔️","🟦","🟧","🟩","🟪"};
    String[] symbolsDuel = {"🟦","🟩","🟨","🟪","⛰","🟢","🪙","⚔️"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game_activity);

        statistics = GameData.getInstance().getStatistics();
        if (statistics == null) {
            statistics = new HashMap<>();
            GameData.getInstance().setStatistics(statistics);
        }

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(Color.parseColor("#FFD700"));
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        layoutContainer = findViewById(R.id.layoutContainer);
        layoutNumPlayers = findViewById(R.id.layoutNumPlayers);
        tableMain = findViewById(R.id.tableMain);
        EditText etNumPlayers = findViewById(R.id.etNumPlayers);
        Button btnConfirmPlayers = findViewById(R.id.btnConfirmPlayers);

        String gameType = getIntent().getStringExtra("gameType");
        isDuel = "7wondersduel".equals(gameType);

        if (isDuel) {
            numPlayers = 2;
            layoutNumPlayers.setVisibility(View.GONE);
            btnConfirmPlayers.setVisibility(View.GONE);
            createTable();
            setupFinishedButton();
        } else {
            layoutNumPlayers.setVisibility(View.VISIBLE);
            btnConfirmPlayers.setVisibility(View.VISIBLE);

            btnConfirmPlayers.setOnClickListener(v -> {
                String s = etNumPlayers.getText().toString();
                if(s.isEmpty()) { Toast.makeText(this,"Insert number of players",Toast.LENGTH_SHORT).show(); return; }
                numPlayers = Integer.parseInt(s);
                if(numPlayers < 3 || numPlayers > 7) { Toast.makeText(this,"Number of players must be between 3 and 7",Toast.LENGTH_SHORT).show(); return; }

                layoutNumPlayers.setVisibility(View.GONE);
                btnConfirmPlayers.setVisibility(View.GONE);

                createTable();
                setupFinishedButton();
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (!isDuel) { // solo 7 Wonders normale
            // Se la tabella è visibile e l'inserimento giocatori è nascosto, torniamo all'inserimento
            if (layoutNumPlayers.getVisibility() == View.GONE && tableMain.getChildCount() > 0) {
                // Mostra layout inserimento giocatori
                layoutNumPlayers.setVisibility(View.VISIBLE);
                findViewById(R.id.btnConfirmPlayers).setVisibility(View.VISIBLE);

                // Nascondi tabella e pulsante finished
                tableMain.removeAllViews();
                if (btnFinished != null) btnFinished.setVisibility(View.GONE);
                return; // evita di chiamare super.onBackPressed()
            }
        }
        // Altrimenti comportamento normale
        super.onBackPressed();
    }

    private TableRow.LayoutParams centeredParams(){
        TableRow.LayoutParams lp = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER;
        return lp;
    }

    private void createTable() {
        tableMain.removeAllViews();
        duelCheckboxes.clear();

        String[] symbols = isDuel ? symbolsDuel : symbols7Wonders;
        int rows = symbols.length;
        scoreCells = new EditText[rows][numPlayers];

        // Header
        TableRow headerRow = new TableRow(this);

        // Prima colonna simboli
        TextView tvHeaderSymbol = new TextView(this);
        tvHeaderSymbol.setText(" ");
        tvHeaderSymbol.setGravity(Gravity.CENTER);
        tvHeaderSymbol.setLayoutParams(centeredParams());
        headerRow.addView(tvHeaderSymbol);

        // Nomi giocatori
        for(int j=0;j<numPlayers;j++){
            EditText etName = new EditText(this);
            etName.setHint("Player "+(j+1));
            etName.setGravity(Gravity.CENTER);
            etName.setLayoutParams(centeredParams());
            headerRow.addView(etName);
            statistics.put("Player "+(j+1), new PlayerStats());
        }

        // Ultima colonna simboli (solo 7 Wonders)
        if(!isDuel && numPlayers>4){
            TextView tvHeaderSymbolEnd = new TextView(this);
            tvHeaderSymbolEnd.setText(" ");
            tvHeaderSymbolEnd.setGravity(Gravity.CENTER);
            tvHeaderSymbolEnd.setLayoutParams(centeredParams());
            headerRow.addView(tvHeaderSymbolEnd);
        }

        tableMain.addView(headerRow);

        // Righe simboli + campi
        for(int i=0;i<rows;i++){
            TableRow row = new TableRow(this);

            // Prima colonna simboli
            TextView tvSymbol = new TextView(this);
            tvSymbol.setText(symbols[i]);
            tvSymbol.setTextSize(24);
            tvSymbol.setGravity(Gravity.CENTER);
            tvSymbol.setLayoutParams(centeredParams());
            row.addView(tvSymbol);

            // Celle punteggio
            for(int j=0;j<numPlayers;j++){
                EditText etScore = new EditText(this);
                etScore.setHint("0");
                etScore.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                etScore.setEms(2);
                etScore.setGravity(Gravity.CENTER);
                etScore.setLayoutParams(centeredParams());
                final int r=i, c=j;
                etScore.addTextChangedListener(new android.text.TextWatcher(){
                    public void afterTextChanged(android.text.Editable s){ updateTotals(); }
                    public void beforeTextChanged(CharSequence s,int start,int count,int after){}
                    public void onTextChanged(CharSequence s,int start,int before,int count){}
                });
                row.addView(etScore);
                scoreCells[i][j] = etScore;
            }

            // Ultima colonna simboli (solo 7 Wonders)
            if(!isDuel){
                TextView tvSymbolEnd = new TextView(this);
                tvSymbolEnd.setText(symbols[i]);
                tvSymbolEnd.setTextSize(24);
                tvSymbolEnd.setGravity(Gravity.CENTER);
                tvSymbolEnd.setLayoutParams(centeredParams());
                row.addView(tvSymbolEnd);
            }

            tableMain.addView(row);
        }

        // Totale
        TableRow totalRow = new TableRow(this);
        TextView tvSum = new TextView(this);
        tvSum.setText("∑");
        tvSum.setTextSize(22);
        tvSum.setTypeface(null, android.graphics.Typeface.BOLD);
        tvSum.setGravity(Gravity.CENTER);
        tvSum.setLayoutParams(centeredParams());
        totalRow.addView(tvSum);

        for(int j=0;j<numPlayers;j++){
            TextView tvTotal = new TextView(this);
            tvTotal.setText("0");
            tvTotal.setGravity(Gravity.CENTER);
            tvTotal.setTextSize(22);
            tvTotal.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTotal.setLayoutParams(centeredParams());
            tvTotal.setId(1000+j);
            totalRow.addView(tvTotal);
        }

        // Ultima colonna simboli (solo 7 Wonders)
        if(!isDuel){
            TextView tvSumEnd = new TextView(this);
            tvSumEnd.setText("∑");
            tvSumEnd.setTextSize(22);
            tvSumEnd.setTypeface(null, android.graphics.Typeface.BOLD);
            tvSumEnd.setGravity(Gravity.CENTER);
            tvSumEnd.setLayoutParams(centeredParams());
            totalRow.addView(tvSumEnd);
        }

        tableMain.addView(totalRow);


        // Duel: checkbox sotto la somma
        if(isDuel){
            TableRow cbRow = new TableRow(this);
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText(" ");
            cbRow.addView(tvEmpty);

            for(int j=0;j<numPlayers;j++){
                LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.setGravity(Gravity.CENTER);
                ll.setLayoutParams(centeredParams());

                CheckBox cbRed = new CheckBox(this);
                //cbRed.setBackgroundColor(Color.parseColor("#B90E0A"));
                CheckBox cbGreen = new CheckBox(this);
                //cbGreen.setBackgroundColor(Color.parseColor("#02971F"));

                cbRed.setButtonTintList(android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#B90E0A")));
                cbGreen.setButtonTintList(android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#02971F")));

                cbRed.setOnClickListener(v -> { if(cbRed.isChecked()) { for(CheckBox cb:duelCheckboxes) if(cb!=cbRed) cb.setChecked(false); } });
                cbGreen.setOnClickListener(v -> { if(cbGreen.isChecked()) { for(CheckBox cb:duelCheckboxes) if(cb!=cbGreen) cb.setChecked(false); } });

                ll.addView(cbRed);
                ll.addView(cbGreen);
                cbRow.addView(ll);

                duelCheckboxes.add(cbRed);
                duelCheckboxes.add(cbGreen);
            }
            tableMain.addView(cbRow);
        }
    }

    private void updateTotals(){
        for(int j=0;j<numPlayers;j++){
            int sum=0;
            for(int i=0;i<scoreCells.length;i++){
                String s = scoreCells[i][j].getText().toString();
                if(!s.isEmpty()) sum+=Integer.parseInt(s);
            }
            TextView tv = findViewById(1000+j);
            tv.setText(String.valueOf(sum));
        }
    }

    private void setupFinishedButton(){
        btnFinished = new Button(this);
        btnFinished.setText("Finished");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.topMargin = 20;
        btnFinished.setLayoutParams(params);
        layoutContainer.addView(btnFinished);
        btnFinished.setOnClickListener(v-> finishGame());
    }

    private void finishGame() {
        // Controllo dati mancanti
        boolean missingData = false;

        for (int i = 0; i < scoreCells.length; i++) {
            for (int j = 0; j < numPlayers; j++) {
                if (scoreCells[i][j].getText().toString().isEmpty()) {
                    missingData = true;
                    break;
                }
            }
            if (missingData) break;
        }

        // Controllo checkbox Duel
        int winnerColumn = -1; // -1 indica che non stiamo usando checkbox
        int winnerCheckIndex = -1; // 0 = prima checkbox, 1 = seconda checkbox
        if (isDuel) {
            for (int j = 0; j < duelCheckboxes.size(); j++) {
                CheckBox cb = duelCheckboxes.get(j);
                if (cb.isChecked()) {
                    winnerColumn = j / 2; // colonna vincente
                    winnerCheckIndex = j % 2; // tipo di vittoria
                    missingData = false; // ignoriamo eventuali campi vuoti
                    break;
                }
            }
        }

        // Mostra popup solo se necessario
        if (missingData) {
            new AlertDialog.Builder(this)
                    .setTitle("Error:")
                    .setMessage("Missing data")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Determina il vincitore
        String winner = "";
        TableRow headerRow = (TableRow) tableMain.getChildAt(0);
        int maxScore = -1;
        for (int j = 0; j < numPlayers; j++) {
            TextView tvTotal = findViewById(1000 + j);
            int total = Integer.parseInt(tvTotal.getText().toString());
            EditText etName = (EditText) headerRow.getChildAt(j + 1);
            String name = etName.getText().toString().isEmpty() ? "Giocatore " + (j + 1) : etName.getText().toString();

            PlayerStats stats = statistics.get(name);
            if (stats == null) stats = new PlayerStats();
            stats.gamesPlayed++;
            statistics.put(name, stats);

            if (isDuel && winnerColumn != -1) {
                if (j == winnerColumn) winner = name; // vincitore scelto dalla checkbox
            } else {
                // 7 Wonders normale o Duel senza checkbox selezionata → chi ha il punteggio più alto
                if (total > maxScore) {
                    maxScore = total;
                    winner = name;
                }
            }
        }

        // Aggiorna statistiche vincitore
        for (int j = 0; j < numPlayers; j++) {
            EditText etName = (EditText) headerRow.getChildAt(j + 1);
            String name = etName.getText().toString().isEmpty() ? "Player " + (j + 1) : etName.getText().toString();

            PlayerStats stats = statistics.get(name);
            if (stats == null) stats = new PlayerStats();
            stats.gamesPlayed++;
            if (name.equals(winner)) {
                stats.gamesWon++;
            } else {
                stats.gamesLost++; // aggiorna sconfitte
            }
            statistics.put(name, stats);
        }

        // Costruisci messaggio popup
        String message;
        if (isDuel && winnerColumn != -1) {
            String type = (winnerCheckIndex == 0) ? "military supremacy" : "scientific supremacy";
            message = "Il vincitore è: " + winner + " (" + type + ")";
        } else {
            message = "Il vincitore è: " + winner;
        }

        // Mostra popup
        new AlertDialog.Builder(this)
                .setTitle("Game finished")
                .setMessage(message)
                .setPositiveButton("OK", (d, w) -> finish())
                .setNegativeButton("Risultati", (d, w) -> {})
                .setCancelable(false)
                .show();

        GameData.getInstance().setStatistics(statistics);
    }

    public static class PlayerStats{
        int gamesPlayed=0;
        int gamesWon=0;
        int gamesLost=0;
    }
}
