package com.example.sevenwondersscore;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.graphics.Color;
import android.util.Log;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class NewGameActivity extends AppCompatActivity {

    TableLayout tableMain;
    LinearLayout layoutContainer, layoutNumPlayers;
    EditText[][] scoreCells;
    Button btnFinished;
    int numPlayers;
    boolean isDuel = false;
    String gameType;

    List<CheckBox> duelCheckboxes = new ArrayList<>();
    DatabaseReference resultsRef;

    String[] symbols7Wonders = {"⛰","🪙","⚔️","🟦","🟧","🟩","🟪"};
    String[] symbolsDuel = {"🟦","🟩","🟨","🟪","⛰","🟢","🪙","⚔️"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game_activity);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar.getNavigationIcon() != null) toolbar.getNavigationIcon().setTint(Color.parseColor("#FFD700"));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        layoutContainer = findViewById(R.id.layoutContainer);
        layoutNumPlayers = findViewById(R.id.layoutNumPlayers);
        tableMain = findViewById(R.id.tableMain);
        EditText etNumPlayers = findViewById(R.id.etNumPlayers);
        Button btnConfirmPlayers = findViewById(R.id.btnConfirmPlayers);

        gameType = getIntent().getStringExtra("gameType");
        isDuel = "7wondersduel".equals(gameType);

        Log.d("FirebaseResults", "=== NewGameActivity onCreate ===");
        Log.d("FirebaseResults", "Game Type received: " + gameType);
        Log.d("FirebaseResults", "Is Duel: " + isDuel);

        // Riferimento Firebase per questo tipo di gioco
        // IMPORTANTE: Usa il database europeo!
        FirebaseDatabase europeanDatabase = FirebaseDatabase.getInstance(
                "https://sevenwondersscore-default-rtdb.europe-west1.firebasedatabase.app"
        );

        resultsRef = europeanDatabase
                .getReference("game_results")
                .child(gameType != null ? gameType : "7wonders");

        Log.d("FirebaseResults", "Results Reference created: " + resultsRef.toString());

        if (isDuel) {
            numPlayers = 2;
            layoutNumPlayers.setVisibility(android.view.View.GONE);
            btnConfirmPlayers.setVisibility(android.view.View.GONE);
            createTable();
            setupFinishedButton();
        } else {
            layoutNumPlayers.setVisibility(android.view.View.VISIBLE);
            btnConfirmPlayers.setVisibility(android.view.View.VISIBLE);
            btnConfirmPlayers.setOnClickListener(v -> {
                String s = etNumPlayers.getText().toString();
                if (s.isEmpty()) { Toast.makeText(this,"Insert number of players",Toast.LENGTH_SHORT).show(); return; }
                numPlayers = Integer.parseInt(s);
                if(numPlayers < 3 || numPlayers > 7) { Toast.makeText(this,"Number of players must be between 3 and 7",Toast.LENGTH_SHORT).show(); return; }

                layoutNumPlayers.setVisibility(android.view.View.GONE);
                btnConfirmPlayers.setVisibility(android.view.View.GONE);
                createTable();
                setupFinishedButton();
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (!isDuel) {
            if (layoutNumPlayers.getVisibility() == android.view.View.GONE && tableMain.getChildCount() > 0) {
                layoutNumPlayers.setVisibility(android.view.View.VISIBLE);
                findViewById(R.id.btnConfirmPlayers).setVisibility(android.view.View.VISIBLE);
                tableMain.removeAllViews();
                if (btnFinished != null) btnFinished.setVisibility(android.view.View.GONE);
                return;
            }
        }
        super.onBackPressed();
    }

    private TableRow.LayoutParams centeredParams(){
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        return lp;
    }

    private void createTable() {
        tableMain.removeAllViews();
        duelCheckboxes.clear();

        String[] symbols = isDuel ? symbolsDuel : symbols7Wonders;
        int rows = symbols.length;
        scoreCells = new EditText[rows][numPlayers];

        // Header con sfondo grigio scuro come GameDetailActivity
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.parseColor("#2C2C2C"));
        headerRow.setPadding(0, 16, 0, 16);
        headerRow.setGravity(Gravity.CENTER);

        TextView tvHeaderSymbol = new TextView(this);
        tvHeaderSymbol.setText("Category");
        tvHeaderSymbol.setGravity(Gravity.CENTER);
        tvHeaderSymbol.setPadding(16, 8, 16, 8);
        tvHeaderSymbol.setTypeface(null, android.graphics.Typeface.BOLD);
        tvHeaderSymbol.setTextSize(16);
        tvHeaderSymbol.setTextColor(Color.parseColor("#FFD700"));
        headerRow.addView(tvHeaderSymbol);

        for(int j=0;j<numPlayers;j++){
            EditText etName = new EditText(this);
            etName.setHint("Player "+(j+1));
            etName.setGravity(Gravity.CENTER);
            etName.setPadding(16, 8, 16, 8);
            etName.setBackgroundColor(Color.TRANSPARENT);
            etName.setTypeface(null, android.graphics.Typeface.BOLD);
            etName.setTextSize(14);
            etName.setTextColor(Color.parseColor("#FFFFFF"));
            etName.setHintTextColor(Color.parseColor("#808080"));
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;
            etName.setLayoutParams(params);
            headerRow.addView(etName);
        }

        // Aggiungi colonna Category a destra per 7 Wonders (NON per Duel)
        if(!isDuel){
            TextView tvHeaderSymbolEnd = new TextView(this);
            tvHeaderSymbolEnd.setText("Category");
            tvHeaderSymbolEnd.setGravity(Gravity.CENTER);
            tvHeaderSymbolEnd.setPadding(16, 8, 16, 8);
            tvHeaderSymbolEnd.setTypeface(null, android.graphics.Typeface.BOLD);
            tvHeaderSymbolEnd.setTextSize(16);
            tvHeaderSymbolEnd.setTextColor(Color.parseColor("#FFD700"));
            headerRow.addView(tvHeaderSymbolEnd);
        }

        tableMain.addView(headerRow);

        // Righe simboli + celle punteggio con separatori
        for(int i=0;i<rows;i++){
            TableRow row = new TableRow(this);
            row.setPadding(0, 8, 0, 8);
            row.setGravity(Gravity.CENTER);

            TextView tvSymbol = new TextView(this);
            tvSymbol.setText(symbols[i]);
            tvSymbol.setTextSize(24);
            tvSymbol.setGravity(Gravity.CENTER);
            tvSymbol.setPadding(16, 8, 16, 8);
            row.addView(tvSymbol);

            for(int j=0;j<numPlayers;j++){
                EditText etScore = new EditText(this);
                etScore.setHint("0");

                // Permetti numeri negativi SOLO per la riga delle battaglie (⚔️) in 7 WONDERS
                // 7 Wonders: simbolo ⚔️ è in posizione 2 → numeri negativi OK
                // Duel: TUTTI i campi sono solo positivi
                boolean isBattleRowIn7Wonders = (!isDuel && i == 2);
                if(isBattleRowIn7Wonders) {
                    // Permetti numeri negativi (con segno) - SOLO 7 WONDERS
                    etScore.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                            android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
                } else {
                    // Solo numeri positivi (per Duel e per tutte le altre righe di 7 Wonders)
                    etScore.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                }

                etScore.setGravity(Gravity.CENTER);
                etScore.setPadding(16, 8, 16, 8);
                etScore.setBackgroundColor(Color.TRANSPARENT);
                etScore.setTextSize(16);
                etScore.setTextColor(Color.parseColor("#FFFFFF"));
                etScore.setHintTextColor(Color.parseColor("#808080"));
                TableRow.LayoutParams params = new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                params.gravity = Gravity.CENTER;
                etScore.setLayoutParams(params);

                final int r=i, c=j;
                etScore.addTextChangedListener(new android.text.TextWatcher(){
                    public void afterTextChanged(android.text.Editable s){ updateTotals(); }
                    public void beforeTextChanged(CharSequence s,int start,int count,int after){}
                    public void onTextChanged(CharSequence s,int start,int before,int count){}
                });
                row.addView(etScore);
                scoreCells[i][j] = etScore;
            }

            if(!isDuel){
                TextView tvSymbolEnd = new TextView(this);
                tvSymbolEnd.setText(symbols[i]);
                tvSymbolEnd.setTextSize(24);
                tvSymbolEnd.setGravity(Gravity.CENTER);
                tvSymbolEnd.setPadding(16, 8, 16, 8);
                row.addView(tvSymbolEnd);
            }

            tableMain.addView(row);
        }

        // Totale con sfondo grigio scuro
        TableRow totalRow = new TableRow(this);
        totalRow.setBackgroundColor(Color.parseColor("#1E1E1E"));
        totalRow.setPadding(0, 16, 0, 16);
        totalRow.setGravity(Gravity.CENTER);

        TextView tvSum = new TextView(this);
        tvSum.setText("∑");
        tvSum.setTextSize(22);
        tvSum.setTypeface(null, android.graphics.Typeface.BOLD);
        tvSum.setGravity(Gravity.CENTER);
        tvSum.setPadding(16, 8, 16, 8);
        tvSum.setTextColor(Color.parseColor("#FFD700"));
        totalRow.addView(tvSum);

        for(int j=0;j<numPlayers;j++){
            TextView tvTotal = new TextView(this);
            tvTotal.setText("0");
            tvTotal.setGravity(Gravity.CENTER);
            tvTotal.setPadding(16, 8, 16, 8);
            tvTotal.setTextSize(18);
            tvTotal.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTotal.setTextColor(Color.parseColor("#FFFFFF"));
            tvTotal.setId(1000+j);
            totalRow.addView(tvTotal);
        }

        // Aggiungi simbolo sommatoria a destra per 7 Wonders (NON per Duel)
        if(!isDuel){
            TextView tvSumEnd = new TextView(this);
            tvSumEnd.setText("∑");
            tvSumEnd.setTextSize(22);
            tvSumEnd.setTypeface(null, android.graphics.Typeface.BOLD);
            tvSumEnd.setGravity(Gravity.CENTER);
            tvSumEnd.setPadding(16, 8, 16, 8);
            tvSumEnd.setTextColor(Color.parseColor("#FFD700"));
            totalRow.addView(tvSumEnd);
        }

        tableMain.addView(totalRow);

        // Checkbox per Duel
        if(isDuel){
            TableRow cbRow = new TableRow(this);
            cbRow.setPadding(0, 16, 0, 0);
            cbRow.setGravity(Gravity.CENTER);

            TextView tvEmpty = new TextView(this);
            tvEmpty.setText(" ");
            tvEmpty.setPadding(16, 8, 16, 8);
            cbRow.addView(tvEmpty);

            for(int j=0;j<numPlayers;j++){
                LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.setGravity(Gravity.CENTER);
                TableRow.LayoutParams llParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                llParams.gravity = Gravity.CENTER;
                ll.setLayoutParams(llParams);

                CheckBox cbRed = new CheckBox(this);
                CheckBox cbGreen = new CheckBox(this);

                cbRed.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#B90E0A")));
                cbGreen.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#02971F")));

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
                String s = scoreCells[i][j].getText().toString().trim();
                if(!s.isEmpty() && !s.equals("-")) { // Ignora "-" da solo (mentre si digita)
                    try {
                        sum += Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        // Ignora errori di parsing (es. input incompleto)
                    }
                }
            }
            TextView tv = findViewById(1000+j);
            tv.setText(String.valueOf(sum));
        }
    }

    private void setupFinishedButton(){
        btnFinished = new Button(this);
        btnFinished.setText("Finished");
        btnFinished.setBackgroundColor(Color.parseColor("#FFD700"));
        btnFinished.setTextColor(Color.parseColor("#000000"));
        btnFinished.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.topMargin = 20;
        btnFinished.setLayoutParams(params);
        layoutContainer.addView(btnFinished);
        btnFinished.setOnClickListener(v-> finishGame());
    }

    private void finishGame() {
        TableRow headerRow = (TableRow) tableMain.getChildAt(0);

        // ===== VALIDAZIONE DATI =====

        // 1. Controlla che tutti i nomi siano compilati
        for (int j = 0; j < numPlayers; j++) {
            EditText etName = (EditText) headerRow.getChildAt(j + 1);
            if (etName.getText().toString().trim().isEmpty()) {
                showErrorDialog("Error: missing data\n\nPlease enter all player names.");
                return;
            }
        }

        // 2. Per 7 Wonders: controlla che TUTTI i campi punteggio siano compilati
        if (!isDuel) {
            for (int i = 0; i < scoreCells.length; i++) {
                for (int j = 0; j < numPlayers; j++) {
                    if (scoreCells[i][j].getText().toString().trim().isEmpty()) {
                        showErrorDialog("Error: missing data\n\nPlease fill in all score fields.");
                        return;
                    }
                }
            }
        }

        // 3. Per Duel: controlla vittoria per supremazia
        boolean hasSupremacy = false;
        if (isDuel) {
            for (CheckBox cb : duelCheckboxes) {
                if (cb.isChecked()) {
                    hasSupremacy = true;
                    break;
                }
            }

            // Se NON c'è supremazia, TUTTI i campi devono essere compilati
            if (!hasSupremacy) {
                for (int i = 0; i < scoreCells.length; i++) {
                    for (int j = 0; j < numPlayers; j++) {
                        if (scoreCells[i][j].getText().toString().trim().isEmpty()) {
                            showErrorDialog("Error: missing data\n\nPlease fill in all score fields or select a supremacy victory.");
                            return;
                        }
                    }
                }
            }
        }

        // ===== CALCOLO VINCITORE =====

        int maxScore = -1;
        String winner = "";
        int winnerColumn = -1;
        int winnerCheckIndex = -1;

        // Check supremacy for Duel
        if (isDuel) {
            for (int i = 0; i < duelCheckboxes.size(); i++) {
                if (duelCheckboxes.get(i).isChecked()) {
                    int playerIndex = i / 2;
                    winnerColumn = playerIndex;
                    winnerCheckIndex = i % 2;
                    EditText etName = (EditText) headerRow.getChildAt(playerIndex + 1);
                    winner = etName.getText().toString().trim();
                    Log.d("FirebaseResults", "Supremacy victory detected for column " + winnerColumn);
                    break;
                }
            }
        }

        // Determina vincitore per punteggio se non c'è supremazia
        if (winnerColumn == -1) {
            for (int j = 0; j < numPlayers; j++) {
                TextView tvTotal = findViewById(1000 + j);
                int total = Integer.parseInt(tvTotal.getText().toString());
                EditText etName = (EditText) headerRow.getChildAt(j + 1);
                String name = etName.getText().toString().trim();

                if (isDuel && j == winnerColumn) {
                    winner = name;
                    Log.d("FirebaseResults", "Duel winner: " + winner);
                } else if (total > maxScore) {
                    maxScore = total;
                    winner = name;
                    Log.d("FirebaseResults", "New high score: " + winner + " with " + maxScore);
                }
            }
        }

        Log.d("FirebaseResults", "Final winner: " + winner);

        String message;
        String victoryType = "points";
        if (isDuel && winnerColumn != -1) {
            String type = (winnerCheckIndex == 0) ? "military supremacy" : "scientific supremacy";
            victoryType = (winnerCheckIndex == 0) ? "military" : "scientific";
            message = "The winner is: " + winner + " (" + type + ")";
        } else {
            message = "The winner is: " + winner;
        }

        String finalWinner = winner;
        String finalVictoryType = victoryType;

        // Checkbox "Save results" (checked di default)
        CheckBox cbSaveResults = new CheckBox(this);
        cbSaveResults.setText("Save results");
        cbSaveResults.setTextColor(Color.parseColor("#FFFFFF"));
        cbSaveResults.setChecked(true);
        Log.d("FirebaseResults", "Save results checkbox created and checked");

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 20, 50, 10);
        dialogLayout.addView(cbSaveResults);

        new AlertDialog.Builder(this)
                .setTitle("Game finished")
                .setMessage(message)
                .setView(dialogLayout)
                .setPositiveButton("OK", (d, w) -> {
                    Log.d("FirebaseResults", "OK button clicked");
                    Log.d("FirebaseResults", "Save results checkbox is checked: " + cbSaveResults.isChecked());
                    if (cbSaveResults.isChecked()) {
                        Log.d("FirebaseResults", "Saving game result...");
                        saveGameResultToFirebase(headerRow, finalWinner, finalVictoryType);
                    } else {
                        Log.d("FirebaseResults", "Results NOT saved (checkbox unchecked)");
                    }
                    finish();
                })
                .setNegativeButton("Cancel", (d, w) -> {
                    Log.d("FirebaseResults", "Cancel button clicked - staying on page");
                    d.dismiss(); // Chiude solo il dialog, NON chiama finish()
                })
                .setCancelable(false)
                .show();

        Log.d("FirebaseResults", "Dialog shown to user");
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (d, w) -> d.dismiss())
                .setCancelable(true)
                .show();
    }

    private void saveGameResultToFirebase(TableRow headerRow, String winner, String victoryType) {
        Log.d("FirebaseResults", "=== START SAVING GAME RESULT ===");

        // Crea l'oggetto GameResult
        String gameId = resultsRef.push().getKey();
        long timestamp = System.currentTimeMillis();

        GameResult gameResult = new GameResult(gameId, timestamp, gameType, winner, victoryType, numPlayers);

        // Aggiungi i dati di ogni giocatore
        for (int j = 0; j < numPlayers; j++) {
            EditText etName = (EditText) headerRow.getChildAt(j + 1);
            String name = etName.getText().toString().isEmpty()
                    ? "Player " + (j + 1)
                    : etName.getText().toString();

            TextView tvTotal = findViewById(1000 + j);
            int totalScore = Integer.parseInt(tvTotal.getText().toString());

            // Raccogli i punteggi per categoria
            List<Integer> categoryScores = new ArrayList<>();
            for (int i = 0; i < scoreCells.length; i++) {
                String scoreText = scoreCells[i][j].getText().toString();
                int score = scoreText.isEmpty() ? 0 : Integer.parseInt(scoreText);
                categoryScores.add(score);
            }

            GameResult.PlayerResult playerResult = new GameResult.PlayerResult(name, totalScore, categoryScores);
            gameResult.getPlayers().add(playerResult);

            Log.d("FirebaseResults", "Added player: " + name + " with total: " + totalScore);
        }

        // Salva su Firebase
        if (gameId != null) {
            resultsRef.child(gameId).setValue(gameResult).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("FirebaseResults", "✅ Game result SAVED successfully!");
                    runOnUiThread(() ->
                            Toast.makeText(NewGameActivity.this,
                                    "Game result saved!",
                                    Toast.LENGTH_SHORT).show()
                    );
                } else {
                    Log.e("FirebaseResults", "❌ ERROR saving game result", task.getException());
                    if (task.getException() != null) {
                        Log.e("FirebaseResults", "Exception: " + task.getException().getMessage());
                    }
                }
            });
        }

        Log.d("FirebaseResults", "=== GAME RESULT SAVE INITIATED ===");
    }
}