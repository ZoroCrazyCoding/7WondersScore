package com.example.sevenwondersscore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    ImageButton  btn7Wonders, btn7WondersDuel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn7Wonders = findViewById(R.id.btn7Wonders);
        btn7WondersDuel = findViewById(R.id.btn7WondersDuel);

        btn7Wonders.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameMenuActivity.class);
            intent.putExtra("gameType", "7wonders");
            startActivity(intent);
        });

        btn7WondersDuel.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameMenuActivity.class);
            intent.putExtra("gameType", "7wondersduel");
            startActivity(intent);
        });
    }
}
