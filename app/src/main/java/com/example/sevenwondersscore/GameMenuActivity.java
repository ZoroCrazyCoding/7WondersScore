package com.example.sevenwondersscore;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class GameMenuActivity extends AppCompatActivity {

    ImageButton btnNewGame, btnStatistics, btnResults;
    String gameType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);

        // Ricevi il tipo di gioco passato da MainActivity
        gameType = getIntent().getStringExtra("gameType");

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            if ("7wondersduel".equals(gameType))
                getSupportActionBar().setTitle("7 Wonders Duel");
            else
                getSupportActionBar().setTitle("7 Wonders");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if(toolbar.getNavigationIcon() != null){
            toolbar.getNavigationIcon().setTint(Color.parseColor("#FFD700"));
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        btnNewGame = findViewById(R.id.btnNewGame);
        btnStatistics = findViewById(R.id.btnStatistics);
        btnResults = findViewById(R.id.btnResults);

        // Apri NewGameActivity
        btnNewGame.setOnClickListener(v -> {
            Intent intent = new Intent(GameMenuActivity.this, NewGameActivity.class);
            intent.putExtra("gameType", gameType);
            startActivity(intent);
        });

        // Apri StatisticsActivity
        btnStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(GameMenuActivity.this, StatisticsActivity.class);
            intent.putExtra("gameType", gameType);
            startActivity(intent);
        });

        // Apri ResultsActivity
        btnResults.setOnClickListener(v -> {
            Intent intent = new Intent(GameMenuActivity.this, ResultsActivity.class);
            intent.putExtra("gameType", gameType);
            startActivity(intent);
        });
    }
}