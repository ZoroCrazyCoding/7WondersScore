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
    DatabaseReference statsRef;

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

        Log.d("FirebaseStats", "=== NewGameActivity onCreate ===");
        Log.d("FirebaseStats", "Game Type received: " + gameType);
        Log.d("FirebaseStats", "Is Duel: " + isDuel);

        // Riferimento Firebase per questo tipo di gioco
        // IMPORTANTE: Usa il database europeo!
        FirebaseDatabase europeanDatabase = FirebaseDatabase.getInstance(
                "https://sevenwondersscore-default-rtdb.europe-west1.firebasedatabase.app"
        );

        statsRef = europeanDatabase
                .getReference("statistics")
                .child(gameType != null ? gameType : "7wonders");

        Log.d("FirebaseStats", "Firebase Reference created: " + statsRef.toString());
        Log.d("FirebaseStats", "Firebase Database URL: " + europeanDatabase.getReference().toString());

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

        // Header
        TableRow headerRow = new TableRow(this);
        TextView tvHeaderSymbol = new TextView(this);
        tvHeaderSymbol.setText(" ");
        tvHeaderSymbol.setGravity(Gravity.CENTER);
        tvHeaderSymbol.setLayoutParams(centeredParams());
        headerRow.addView(tvHeaderSymbol);

        for(int j=0;j<numPlayers;j++){
            EditText etName = new EditText(this);
            etName.setHint("Player "+(j+1));
            etName.setGravity(Gravity.CENTER);
            etName.setLayoutParams(centeredParams());
            headerRow.addView(etName);
        }

        if(!isDuel && numPlayers>4){
            TextView tvHeaderSymbolEnd = new TextView(this);
            tvHeaderSymbolEnd.setText(" ");
            tvHeaderSymbolEnd.setGravity(Gravity.CENTER);
            tvHeaderSymbolEnd.setLayoutParams(centeredParams());
            headerRow.addView(tvHeaderSymbolEnd);
        }

        tableMain.addView(headerRow);

        // Righe simboli + celle punteggio
        for(int i=0;i<rows;i++){
            TableRow row = new TableRow(this);
            TextView tvSymbol = new TextView(this);
            tvSymbol.setText(symbols[i]);
            tvSymbol.setTextSize(24);
            tvSymbol.setGravity(Gravity.CENTER);
            tvSymbol.setLayoutParams(centeredParams());
            row.addView(tvSymbol);

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

        if(!isDuel && numPlayers>4){
            TextView tvPlaceholder = new TextView(this);
            tvPlaceholder.setText(" ");
            totalRow.addView(tvPlaceholder);
        }

        tableMain.addView(totalRow);

        // Checkbox per Duel
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
        Log.d("FirebaseStats", "=== finishGame() called ===");

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

        int winnerColumn = -1;
        int winnerCheckIndex = -1;

        if (isDuel) {
            Log.d("FirebaseStats", "Checking Duel checkboxes...");
            for (int j = 0; j < duelCheckboxes.size(); j++) {
                CheckBox cb = duelCheckboxes.get(j);
                if (cb.isChecked()) {
                    winnerColumn = j / 2;
                    winnerCheckIndex = j % 2;
                    missingData = false;
                    Log.d("FirebaseStats", "Duel winner found: column " + winnerColumn + ", type " + winnerCheckIndex);
                    break;
                }
            }
        }

        if (missingData) {
            Log.w("FirebaseStats", "Missing data detected - showing error dialog");
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Missing data")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Determina il vincitore
        String winner = "";
        TableRow headerRow = (TableRow) tableMain.getChildAt(0);
        int maxScore = -1;

        Log.d("FirebaseStats", "Determining winner...");
        for (int j = 0; j < numPlayers; j++) {
            TextView tvTotal = findViewById(1000 + j);
            int total = Integer.parseInt(tvTotal.getText().toString());

            EditText etName = (EditText) headerRow.getChildAt(j + 1);
            String name = etName.getText().toString().isEmpty()
                    ? "Player " + (j + 1)
                    : etName.getText().toString();

            Log.d("FirebaseStats", "Player " + (j+1) + ": " + name + " - Score: " + total);

            if (isDuel && winnerColumn != -1) {
                if (j == winnerColumn) {
                    winner = name;
                    Log.d("FirebaseStats", "Duel winner: " + winner);
                }
            } else if (total > maxScore) {
                maxScore = total;
                winner = name;
                Log.d("FirebaseStats", "New high score: " + winner + " with " + maxScore);
            }
        }

        Log.d("FirebaseStats", "Final winner: " + winner);

        String message;
        if (isDuel && winnerColumn != -1) {
            String type = (winnerCheckIndex == 0) ? "military supremacy" : "scientific supremacy";
            message = "The winner is: " + winner + " (" + type + ")";
        } else {
            message = "The winner is: " + winner;
        }

        CheckBox cbSaveStats = new CheckBox(this);
        cbSaveStats.setText("Save statistics");
        cbSaveStats.setChecked(true);
        Log.d("FirebaseStats", "Save statistics checkbox created and checked");

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 20, 50, 10);
        dialogLayout.addView(cbSaveStats);

        String finalWinner = winner;
        new AlertDialog.Builder(this)
                .setTitle("Game finished")
                .setMessage(message)
                .setView(dialogLayout)
                .setPositiveButton("OK", (d, w) -> {
                    Log.d("FirebaseStats", "OK button clicked");
                    Log.d("FirebaseStats", "Save stats checkbox is checked: " + cbSaveStats.isChecked());
                    if (cbSaveStats.isChecked()) {
                        Log.d("FirebaseStats", "Calling saveStatisticsToFirebase...");
                        saveStatisticsToFirebase(headerRow, finalWinner);
                    } else {
                        Log.d("FirebaseStats", "Statistics NOT saved (checkbox unchecked)");
                    }
                    finish();
                })
                .setNegativeButton("Cancel", (d, w) -> {
                    Log.d("FirebaseStats", "Cancel button clicked - not saving stats");
                    finish();
                })
                .setCancelable(false)
                .show();

        Log.d("FirebaseStats", "Dialog shown to user");
    }

    private void saveStatisticsToFirebase(TableRow headerRow, String winner) {
        Log.d("FirebaseStats", "=== START SAVING STATISTICS ===");
        Log.d("FirebaseStats", "Game Type: " + gameType);
        Log.d("FirebaseStats", "Number of Players: " + numPlayers);
        Log.d("FirebaseStats", "Winner: " + winner);
        Log.d("FirebaseStats", "Stats Reference Path: " + statsRef.toString());

        // Conta quanti salvataggi devono completare
        final int[] completedSaves = {0};
        final int totalSaves = numPlayers;

        // Salva le statistiche per ogni giocatore
        for (int j = 0; j < numPlayers; j++) {
            EditText etName = (EditText) headerRow.getChildAt(j + 1);
            String name = etName.getText().toString().isEmpty()
                    ? "Player " + (j + 1)
                    : etName.getText().toString();

            boolean isWinner = name.equals(winner);

            Log.d("FirebaseStats", "Processing player " + (j+1) + ": " + name + " (isWinner: " + isWinner + ")");

            DatabaseReference playerRef = statsRef.child(name);
            Log.d("FirebaseStats", "Player Reference Path: " + playerRef.toString());

            // Prima leggi i dati esistenti
            playerRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("FirebaseStats", "Read completed for: " + name);

                    PlayerStats stats;
                    DataSnapshot snapshot = task.getResult();

                    if (snapshot.exists()) {
                        stats = snapshot.getValue(PlayerStats.class);
                        Log.d("FirebaseStats", "Existing stats for " + name +
                                " - Played: " + stats.gamesPlayed +
                                ", Won: " + stats.gamesWon +
                                ", Lost: " + stats.gamesLost);
                    } else {
                        Log.d("FirebaseStats", "Creating new PlayerStats for: " + name);
                        stats = new PlayerStats();
                    }

                    // Incrementa le statistiche
                    stats.gamesPlayed++;
                    if (isWinner) {
                        stats.gamesWon++;
                    } else {
                        stats.gamesLost++;
                    }

                    Log.d("FirebaseStats", "Updated stats for " + name +
                            " - Played: " + stats.gamesPlayed +
                            ", Won: " + stats.gamesWon +
                            ", Lost: " + stats.gamesLost);

                    // Salva con setValue
                    playerRef.setValue(stats).addOnCompleteListener(saveTask -> {
                        if (saveTask.isSuccessful()) {
                            Log.d("FirebaseStats", "✅ Statistics SAVED for " + name);
                            completedSaves[0]++;

                            if (completedSaves[0] == totalSaves) {
                                Log.d("FirebaseStats", "🎉 ALL STATISTICS SAVED SUCCESSFULLY!");
                                runOnUiThread(() ->
                                        Toast.makeText(NewGameActivity.this,
                                                "Statistics saved successfully!",
                                                Toast.LENGTH_SHORT).show()
                                );
                            }
                        } else {
                            Log.e("FirebaseStats", "❌ ERROR saving stats for " + name, saveTask.getException());
                            if (saveTask.getException() != null) {
                                Log.e("FirebaseStats", "Exception: " + saveTask.getException().getMessage());
                            }
                            runOnUiThread(() ->
                                    Toast.makeText(NewGameActivity.this,
                                            "Error saving statistics for " + name,
                                            Toast.LENGTH_SHORT).show()
                            );
                        }
                    });

                } else {
                    Log.e("FirebaseStats", "❌ ERROR reading existing stats for " + name, task.getException());
                    if (task.getException() != null) {
                        Log.e("FirebaseStats", "Read Exception: " + task.getException().getMessage());
                    }
                }
            });
        }

        Log.d("FirebaseStats", "=== ALL SAVE OPERATIONS INITIATED ===");
    }

    public static class PlayerStats {
        public int gamesPlayed = 0;
        public int gamesWon = 0;
        public int gamesLost = 0;

        public PlayerStats() {}

        // Getter / Setter necessari per Firebase
        public int getGamesPlayed() { return gamesPlayed; }
        public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }
        public int getGamesWon() { return gamesWon; }
        public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }
        public int getGamesLost() { return gamesLost; }
        public void setGamesLost(int gamesLost) { this.gamesLost = gamesLost; }
    }
}