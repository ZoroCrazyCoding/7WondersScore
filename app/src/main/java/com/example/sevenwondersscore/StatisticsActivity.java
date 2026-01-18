package com.example.sevenwondersscore;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    TableLayout table;
    DatabaseReference resultsRef;
    String gameType;
    ProgressBar progressBar;
    TextView tvNoData;

    // Variabili per ordinamento
    private List<PlayerData> currentPlayersList;
    private String currentSortColumn = "player"; // Default: ordina per nome
    private boolean currentSortAscending = true; // Default: ordine crescente

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Statistics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if(toolbar.getNavigationIcon() != null)
            toolbar.getNavigationIcon().setTint(Color.parseColor("#FFD700"));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Ricevi tipo di gioco passato da GameMenuActivity
        gameType = getIntent().getStringExtra("gameType");
        if(gameType == null) gameType = "7wonders";

        Log.d("StatisticsActivity", "=== StatisticsActivity onCreate ===");
        Log.d("StatisticsActivity", "Game Type: " + gameType);

        table = findViewById(R.id.tableStats);
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);

        // Verifica che le view esistano
        if(progressBar == null) {
            Log.e("StatisticsActivity", "ProgressBar not found in layout!");
        }
        if(tvNoData == null) {
            Log.e("StatisticsActivity", "tvNoData not found in layout!");
        }

        // Riferimento Firebase ai game_results (non più statistics)
        FirebaseDatabase europeanDatabase = FirebaseDatabase.getInstance(
                "https://sevenwondersscore-default-rtdb.europe-west1.firebasedatabase.app"
        );

        resultsRef = europeanDatabase
                .getReference("game_results")
                .child(gameType);

        Log.d("StatisticsActivity", "Results Reference: " + resultsRef.toString());
        Log.d("StatisticsActivity", "Loading statistics from game results...");

        loadStatistics();
    }

    private void loadStatistics() {
        Log.d("StatisticsActivity", "loadStatistics() - Adding value event listener");

        if(progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if(tvNoData != null) {
            tvNoData.setVisibility(View.GONE);
        }

        resultsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("StatisticsActivity", "=== onDataChange triggered ===");
                Log.d("StatisticsActivity", "Snapshot exists: " + snapshot.exists());
                Log.d("StatisticsActivity", "Snapshot has children: " + snapshot.hasChildren());

                if(progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                table.removeAllViews();

                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    Log.w("StatisticsActivity", "No data available in Firebase");
                    if(tvNoData != null) {
                        tvNoData.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                if(tvNoData != null) {
                    tvNoData.setVisibility(View.GONE);
                }

                // Mappa per raccogliere le statistiche di ogni giocatore
                Map<String, PlayerStats> statsMap = new HashMap<>();

                // Scorri tutti i game results e calcola le statistiche
                for(DataSnapshot gameSnap : snapshot.getChildren()) {
                    GameResult result = gameSnap.getValue(GameResult.class);
                    if(result == null) continue;

                    // Processa ogni giocatore in questa partita
                    List<GameResult.PlayerResult> players = result.getPlayers();
                    if(players == null) continue;

                    for(GameResult.PlayerResult player : players) {
                        String playerName = player.getName();
                        if(playerName == null) continue;

                        // Crea o recupera le statistiche del giocatore
                        PlayerStats stats = statsMap.get(playerName);
                        if(stats == null) {
                            stats = new PlayerStats();
                            statsMap.put(playerName, stats);
                        }

                        // Incrementa partite giocate
                        stats.gamesPlayed++;

                        // Verifica se questo giocatore è vincitore (anche in caso di parità)
                        boolean isWinner = result.isWinner(playerName);

                        // Incrementa vittorie o sconfitte
                        if(isWinner) {
                            stats.gamesWon++;
                        } else {
                            stats.gamesLost++;
                        }
                    }
                }

                Log.d("StatisticsActivity", "Total unique players: " + statsMap.size());

                // Converti la mappa in lista per poter ordinare
                List<PlayerData> players = new ArrayList<>();
                for(Map.Entry<String, PlayerStats> entry : statsMap.entrySet()) {
                    players.add(new PlayerData(entry.getKey(), entry.getValue()));
                }

                // Salva la lista corrente
                currentPlayersList = players;

                // Ordina con i criteri di default (per nome, crescente)
                sortPlayersList("player", true);

                // Mostra i dati
                refreshTable();

                Log.d("StatisticsActivity", "Statistics table updated successfully");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if(progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                Log.e("StatisticsActivity", "❌ Error loading statistics", error.toException());
                Log.e("StatisticsActivity", "Error code: " + error.getCode());
                Log.e("StatisticsActivity", "Error message: " + error.getMessage());

                Toast.makeText(StatisticsActivity.this,
                        "Error loading statistics: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sortPlayersList(String column, boolean ascending) {
        currentSortColumn = column;
        currentSortAscending = ascending;

        Collections.sort(currentPlayersList, new Comparator<PlayerData>() {
            @Override
            public int compare(PlayerData p1, PlayerData p2) {
                int result = 0;

                switch(column) {
                    case "player":
                        result = p1.name.compareToIgnoreCase(p2.name);
                        break;
                    case "played":
                        result = Integer.compare(p1.stats.gamesPlayed, p2.stats.gamesPlayed);
                        break;
                    case "won":
                        result = Integer.compare(p1.stats.gamesWon, p2.stats.gamesWon);
                        break;
                    case "lost":
                        result = Integer.compare(p1.stats.gamesLost, p2.stats.gamesLost);
                        break;
                    case "winpercent":
                        double p1Percent = p1.stats.gamesPlayed > 0 ?
                                (p1.stats.gamesWon * 100.0) / p1.stats.gamesPlayed : 0;
                        double p2Percent = p2.stats.gamesPlayed > 0 ?
                                (p2.stats.gamesWon * 100.0) / p2.stats.gamesPlayed : 0;
                        result = Double.compare(p1Percent, p2Percent);
                        break;
                }

                // Se decrescente, inverti il risultato
                return ascending ? result : -result;
            }
        });
    }

    private void refreshTable() {
        table.removeAllViews();
        addHeader();

        for(PlayerData player : currentPlayersList) {
            addRow(player.name, player.stats);
        }
    }

    private void addHeader() {
        TableRow header = new TableRow(this);
        header.setGravity(Gravity.CENTER);
        header.setBackgroundColor(Color.parseColor("#2C2C2C"));
        header.setPadding(0, 16, 0, 16);

        String[] headers = {"Player", "Played", "Won", "Lost", "Win %"};
        String[] columns = {"player", "played", "won", "lost", "winpercent"};

        for(int i = 0; i < headers.length; i++) {
            final String columnName = columns[i];

            TextView tv = new TextView(this);

            // Aggiungi freccia se questa colonna è quella ordinata
            String headerText = headers[i];
            if(currentSortColumn.equals(columnName)) {
                headerText += currentSortAscending ? " ▲" : " ▼";
            }

            tv.setText(headerText);
            tv.setPadding(24,16,24,16);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(16);
            tv.setTextColor(Color.parseColor("#FFD700"));

            // Rendi cliccabile
            tv.setClickable(true);
            tv.setOnClickListener(v -> {
                // Se è già ordinato per questa colonna, inverti l'ordine
                if(currentSortColumn.equals(columnName)) {
                    sortPlayersList(columnName, !currentSortAscending);
                } else {
                    // Altrimenti ordina per questa colonna in ordine crescente
                    sortPlayersList(columnName, true);
                }
                refreshTable();
            });

            header.addView(tv);
        }

        table.addView(header);
    }

    private void addRow(String name, PlayerStats stats){
        TableRow row = new TableRow(this);
        row.setGravity(Gravity.CENTER);
        row.setPadding(0, 8, 0, 8);

        // Calcola percentuale vittorie
        double winPercentage = 0.0;
        if (stats.gamesPlayed > 0) {
            winPercentage = (stats.gamesWon * 100.0) / stats.gamesPlayed;
        }
        String winPercentStr = String.format("%.1f%%", winPercentage);

        row.addView(createCell(name, true));
        row.addView(createCell(String.valueOf(stats.gamesPlayed), false));
        row.addView(createCell(String.valueOf(stats.gamesWon), false));
        row.addView(createCell(String.valueOf(stats.gamesLost), false));
        row.addView(createCell(winPercentStr, false));

        table.addView(row);

        // Aggiungi separatore visuale
        View divider = new View(this);
        divider.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#404040"));
        table.addView(divider);
    }

    private TextView createCell(String text, boolean isName){
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(24,12,24,12);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(14);

        if (isName) {
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setTextColor(Color.parseColor("#64B5F6"));
        } else {
            tv.setTextColor(Color.parseColor("#FFFFFF"));
        }

        return tv;
    }

    // Classe per statistiche giocatore
    private static class PlayerStats {
        int gamesPlayed = 0;
        int gamesWon = 0;
        int gamesLost = 0;
    }

    // Classe helper per ordinamento
    private static class PlayerData {
        String name;
        PlayerStats stats;

        PlayerData(String name, PlayerStats stats) {
            this.name = name;
            this.stats = stats;
        }
    }
}