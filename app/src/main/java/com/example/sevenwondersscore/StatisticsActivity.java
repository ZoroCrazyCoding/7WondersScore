package com.example.sevenwondersscore;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    TableLayout table;
    DatabaseReference statsRef;
    String gameType;
    ProgressBar progressBar;
    TextView tvNoData;

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
        if(gameType == null) gameType = "7wonders"; // fallback

        Log.d("FirebaseStats", "=== StatisticsActivity onCreate ===");
        Log.d("FirebaseStats", "Game Type: " + gameType);

        table = findViewById(R.id.tableStats);

        // Aggiungi ProgressBar e TextView per stato vuoto
        progressBar = new ProgressBar(this);
        tvNoData = new TextView(this);
        tvNoData.setText("No statistics available yet.\nPlay some games to see your stats!");
        tvNoData.setGravity(Gravity.CENTER);
        tvNoData.setTextSize(16);
        tvNoData.setPadding(32, 32, 32, 32);
        tvNoData.setVisibility(android.view.View.GONE);

        // Nodo Firebase specifico per tipo di gioco
        // IMPORTANTE: Usa il database europeo!
        FirebaseDatabase europeanDatabase = FirebaseDatabase.getInstance(
                "https://sevenwondersscore-default-rtdb.europe-west1.firebasedatabase.app"
        );

        statsRef = europeanDatabase
                .getReference("statistics")
                .child(gameType);

        Log.d("FirebaseStats", "Stats Reference: " + statsRef.toString());
        Log.d("FirebaseStats", "Loading statistics...");

        loadStatistics();
    }

    private void loadStatistics() {
        Log.d("FirebaseStats", "loadStatistics() - Adding value event listener");

        statsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("FirebaseStats", "=== onDataChange triggered ===");
                Log.d("FirebaseStats", "Snapshot exists: " + snapshot.exists());
                Log.d("FirebaseStats", "Snapshot has children: " + snapshot.hasChildren());
                Log.d("FirebaseStats", "Children count: " + snapshot.getChildrenCount());

                table.removeAllViews();

                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    // Nessun dato disponibile
                    Log.w("FirebaseStats", "No data available in Firebase");
                    tvNoData.setVisibility(android.view.View.VISIBLE);
                    return;
                }

                tvNoData.setVisibility(android.view.View.GONE);

                // Raccogli tutti i giocatori in una lista per poterli ordinare
                List<PlayerData> players = new ArrayList<>();

                for(DataSnapshot playerSnap : snapshot.getChildren()){
                    String name = playerSnap.getKey();
                    Log.d("FirebaseStats", "Processing player: " + name);
                    Log.d("FirebaseStats", "Player data: " + playerSnap.getValue());

                    NewGameActivity.PlayerStats stats =
                            playerSnap.getValue(NewGameActivity.PlayerStats.class);
                    if(stats != null) {
                        Log.d("FirebaseStats", name + " stats - Played: " + stats.gamesPlayed +
                                ", Won: " + stats.gamesWon + ", Lost: " + stats.gamesLost);
                        players.add(new PlayerData(name, stats));
                    } else {
                        Log.w("FirebaseStats", "Failed to parse stats for: " + name);
                    }
                }

                Log.d("FirebaseStats", "Total players loaded: " + players.size());

                // Ordina per numero di vittorie (decrescente)
                Collections.sort(players, new Comparator<PlayerData>() {
                    @Override
                    public int compare(PlayerData p1, PlayerData p2) {
                        // Prima per vittorie (decrescente)
                        int winCompare = Integer.compare(p2.stats.gamesWon, p1.stats.gamesWon);
                        if (winCompare != 0) return winCompare;

                        // Poi per partite giocate (decrescente)
                        int playedCompare = Integer.compare(p2.stats.gamesPlayed, p1.stats.gamesPlayed);
                        if (playedCompare != 0) return playedCompare;

                        // Infine alfabeticamente
                        return p1.name.compareTo(p2.name);
                    }
                });

                addHeader();

                // Aggiungi le righe ordinate
                for(PlayerData player : players) {
                    addRow(player.name, player.stats);
                }

                Log.d("FirebaseStats", "Statistics table updated successfully");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseStats", "❌ Error loading statistics", error.toException());
                Log.e("FirebaseStats", "Error code: " + error.getCode());
                Log.e("FirebaseStats", "Error message: " + error.getMessage());
                Log.e("FirebaseStats", "Error details: " + error.getDetails());

                Toast.makeText(StatisticsActivity.this,
                        "Error loading statistics: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addHeader() {
        TableRow header = new TableRow(this);
        header.setGravity(Gravity.CENTER);
        header.setBackgroundColor(Color.parseColor("#E0E0E0"));
        header.setPadding(0, 16, 0, 16);

        String[] headers = {"Player", "Played", "Won", "Lost", "Win %"};
        for(String h : headers){
            TextView tv = new TextView(this);
            tv.setText(h);
            tv.setPadding(24,16,24,16);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(16);
            header.addView(tv);
        }

        table.addView(header);
    }

    private void addRow(String name, NewGameActivity.PlayerStats stats){
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
        android.view.View divider = new android.view.View(this);
        divider.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#CCCCCC"));
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
            tv.setTextColor(Color.parseColor("#1976D2"));
        }

        return tv;
    }

    // Classe helper per ordinamento
    private static class PlayerData {
        String name;
        NewGameActivity.PlayerStats stats;

        PlayerData(String name, NewGameActivity.PlayerStats stats) {
            this.name = name;
            this.stats = stats;
        }
    }
}