package com.example.sevenwondersscore;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity {

    LinearLayout layoutResults;
    DatabaseReference resultsRef;
    String gameType;
    ProgressBar progressBar;
    TextView tvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Games List");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if(toolbar.getNavigationIcon() != null){
            toolbar.getNavigationIcon().setTint(Color.parseColor("#FFD700"));
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Ricevi tipo di gioco
        gameType = getIntent().getStringExtra("gameType");
        if(gameType == null) gameType = "7wonders";

        layoutResults = findViewById(R.id.layoutResults);
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);

        // Riferimento Firebase
        FirebaseDatabase europeanDatabase = FirebaseDatabase.getInstance(
                "https://sevenwondersscore-default-rtdb.europe-west1.firebasedatabase.app"
        );

        resultsRef = europeanDatabase
                .getReference("game_results")
                .child(gameType);

        Log.d("ResultsActivity", "Loading results for: " + gameType);

        loadResults();
    }

    private void loadResults() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);

        resultsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                layoutResults.removeAllViews();

                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    tvNoData.setVisibility(View.VISIBLE);
                    return;
                }

                tvNoData.setVisibility(View.GONE);

                // Raccogli tutti i risultati
                List<GameResult> results = new ArrayList<>();
                for(DataSnapshot gameSnap : snapshot.getChildren()){
                    GameResult result = gameSnap.getValue(GameResult.class);
                    if(result != null) {
                        results.add(result);
                    }
                }

                // Ordina per data (più recenti prima)
                Collections.sort(results, (r1, r2) ->
                        Long.compare(r2.getTimestamp(), r1.getTimestamp()));

                // Crea le card per ogni risultato
                for(GameResult result : results) {
                    addResultCard(result);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Log.e("ResultsActivity", "Error loading results", error.toException());
                Toast.makeText(ResultsActivity.this,
                        "Error loading results: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addResultCard(GameResult result) {
        // Card container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        card.setPadding(32, 24, 32, 24);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(16, 16, 16, 16);
        card.setLayoutParams(cardParams);
        card.setClickable(true);
        card.setFocusable(true);

        // Data
        TextView tvDate = new TextView(this);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(result.getTimestamp())));
        tvDate.setTextSize(14);
        tvDate.setTextColor(Color.GRAY);
        card.addView(tvDate);

        // Winner
        TextView tvWinner = new TextView(this);
        String winnerText = "🏆 Winner: " + result.getWinnerName();
        tvWinner.setText(winnerText);
        tvWinner.setTextSize(18);
        tvWinner.setTextColor(Color.parseColor("#1976D2"));
        tvWinner.setTypeface(null, android.graphics.Typeface.BOLD);
        tvWinner.setPadding(0, 8, 0, 0);
        card.addView(tvWinner);

        // Victory type (se non è vittoria ai punti)
        if(result.getVictoryType() != null && !result.getVictoryType().equals("points")) {
            TextView tvVictoryType = new TextView(this);
            String typeText = "";
            if(result.getVictoryType().equals("military")) {
                typeText = "⚔️ Military Supremacy";
            } else if(result.getVictoryType().equals("scientific")) {
                typeText = "🔬 Scientific Supremacy";
            }
            tvVictoryType.setText(typeText);
            tvVictoryType.setTextSize(14);
            tvVictoryType.setTextColor(Color.parseColor("#FF6B6B"));
            tvVictoryType.setPadding(0, 4, 0, 0);
            card.addView(tvVictoryType);
        }

        // Numero giocatori - MOSTRA SOLO SE NON È DUEL (2 giocatori)
        if(result.getNumPlayers() != 2) {
            TextView tvPlayers = new TextView(this);
            tvPlayers.setText(result.getNumPlayers() + " players");
            tvPlayers.setTextSize(14);
            tvPlayers.setTextColor(Color.GRAY);
            tvPlayers.setPadding(0, 4, 0, 0);
            card.addView(tvPlayers);
        }

        // Click listener per aprire i dettagli
        card.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, GameDetailActivity.class);
            intent.putExtra("gameResult", result);
            intent.putExtra("gameType", gameType);
            startActivity(intent);
        });

        layoutResults.addView(card);
    }
}