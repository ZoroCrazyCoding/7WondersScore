package com.example.sevenwondersscore;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.*;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("7 Wonders Score");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(Color.parseColor("#FFD700"));
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        TableLayout table = findViewById(R.id.tableStats);

        // Recupera la mappa dal singleton o dalla classe
        Map<String, NewGameActivity.PlayerStats> statsMap = GameData.getInstance().getStatistics();

        // Header
        TableRow header = new TableRow(this);
        String[] headers = {"Player", "Games Played", "Games Won", "Games Lost"};
        for(String h: headers){
            TextView tv = new TextView(this);
            tv.setText(h);
            tv.setPadding(16,8,16,8);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            header.addView(tv);
        }
        table.addView(header);

        // Dati giocatori
        for(Map.Entry<String, NewGameActivity.PlayerStats> entry : statsMap.entrySet()){
            TableRow row = new TableRow(this);

            TextView name = new TextView(this);
            name.setText(entry.getKey());
            name.setPadding(16,8,16,8);
            row.addView(name);

            TextView played = new TextView(this);
            played.setText(String.valueOf(entry.getValue().gamesPlayed));
            played.setPadding(16,8,16,8);
            row.addView(played);

            TextView won = new TextView(this);
            won.setText(String.valueOf(entry.getValue().gamesWon));
            won.setPadding(16,8,16,8);
            row.addView(won);

            TextView lost = new TextView(this);
            lost.setText(String.valueOf(entry.getValue().gamesLost));
            lost.setPadding(16,8,16,8);
            row.addView(lost);

            table.addView(row);
        }
    }
}
