package com.example.sevenwondersscore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    ImageButton btn7Wonders, btn7WondersDuel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase europeanDatabase = FirebaseDatabase.getInstance(
                "https://sevenwondersscore-default-rtdb.europe-west1.firebasedatabase.app"
        );

        Log.d("Firebase", "Using European database: " + europeanDatabase.getReference().toString());

        btn7Wonders = findViewById(R.id.btn7Wonders);
        btn7WondersDuel = findViewById(R.id.btn7WondersDuel);

        // Bottone 7 Wonders
        btn7Wonders.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameMenuActivity.class);
            intent.putExtra("gameType", "7wonders");
            startActivity(intent);
        });

        // Bottone 7 Wonders Duel
        btn7WondersDuel.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameMenuActivity.class);
            intent.putExtra("gameType", "7wondersduel");
            startActivity(intent);
        });

        // 🔹 Test Firebase con l'URL corretto
        DatabaseReference testRef = europeanDatabase.getReference("test_connection");
        testRef.setValue("ok").addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d("Firebase", "✅ Firebase connection SUCCESS");
                Toast.makeText(MainActivity.this, "Firebase OK", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Firebase", "❌ Firebase connection FAILED", task.getException());
                Toast.makeText(MainActivity.this, "Firebase KO: " + task.getException(), Toast.LENGTH_LONG).show();
            }
        });
    }
}