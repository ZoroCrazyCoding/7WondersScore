package com.example.sevenwondersscore;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Aggiungi il pulsante Reset nella toolbar
        MenuItem resetItem = menu.add(Menu.NONE, 1, Menu.NONE, "Reset");
        resetItem.setIcon(android.R.drawable.ic_menu_delete);
        resetItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        // Colora l'icona di oro
        if(resetItem.getIcon() != null) {
            resetItem.getIcon().setTint(Color.parseColor("#FFD700"));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 1) {
            // Pulsante Reset premuto
            showFirstConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFirstConfirmationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete All Data")
                .setMessage("Do you want to delete all data in game results?")
                .setPositiveButton("Yes", null) // Gestito manualmente
                .setNegativeButton("No", (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .setCancelable(true)
                .create();

        // Personalizza i colori del dialog
        dialog.setOnShowListener(dialogInterface -> {
            // Colora il titolo
            TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
            if (titleView != null) {
                titleView.setTextColor(Color.parseColor("#FFD700"));
            }

            // Colora il messaggio
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTextColor(Color.parseColor("#FFFFFF"));
            }

            // Colora i pulsanti
            android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            android.widget.Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(Color.parseColor("#FFD700"));
                positiveButton.setOnClickListener(v -> {
                    dialog.dismiss();
                    showSecondConfirmationDialog();
                });
            }
            if (negativeButton != null) {
                negativeButton.setTextColor(Color.parseColor("#B0B0B0"));
            }
        });

        dialog.show();
    }

    private void showSecondConfirmationDialog() {
        // Crea un layout verticale per contenere EditText e CheckBox
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        // Crea un EditText per inserire la password
        final EditText input = new EditText(this);
        input.setHint("Enter password");
        input.setHintTextColor(Color.parseColor("#888888"));
        input.setTextColor(Color.parseColor("#FFFFFF"));
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Imposta una larghezza massima per l'EditText
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        inputParams.setMargins(20, 10, 20, 10);
        input.setLayoutParams(inputParams);

        // Crea una CheckBox per mostrare/nascondere la password
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText("Show password");
        checkBox.setTextColor(Color.parseColor("#B0B0B0"));
        checkBox.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700")));

        LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        checkBoxParams.setMargins(20, 5, 20, 10);
        checkBoxParams.gravity = Gravity.CENTER;
        checkBox.setLayoutParams(checkBoxParams);

        // Listener per mostrare/nascondere la password
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Mostra la password
                input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                // Nascondi la password
                input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            // Sposta il cursore alla fine del testo
            input.setSelection(input.getText().length());
        });

        // Aggiungi EditText e CheckBox al layout
        layout.addView(input);
        layout.addView(checkBox);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Enter Password")
                .setMessage("Enter the password to confirm deletion:")
                .setView(layout)
                .setPositiveButton("Confirm", null) // Lo impostiamo a null per gestirlo manualmente
                .setNegativeButton("Cancel", (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .setCancelable(true)
                .create();

        // Personalizza i colori del dialog
        dialog.setOnShowListener(dialogInterface -> {
            // Colora il titolo
            TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
            if (titleView != null) {
                titleView.setTextColor(Color.parseColor("#FFD700"));
            }

            // Colora il messaggio
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTextColor(Color.parseColor("#FFFFFF"));
            }

            // Colora i pulsanti
            android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            android.widget.Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(Color.parseColor("#FFD700"));
            }
            if (negativeButton != null) {
                negativeButton.setTextColor(Color.parseColor("#B0B0B0"));
            }

            // Gestisci il click del pulsante Confirm manualmente
            if (positiveButton != null) {
                positiveButton.setOnClickListener(v -> {
                    String enteredPassword = input.getText().toString();

                    if(enteredPassword.equals("Zoro5212!")) {
                        // Password corretta - elimina i dati
                        dialog.dismiss();
                        deleteAllGameResults();
                    } else {
                        // Password errata - mostra messaggio
                        dialog.dismiss();
                        showWrongPasswordDialog();
                    }
                });
            }
        });

        dialog.show();
    }

    private void showWrongPasswordDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Wrong Password")
                .setMessage("The password you entered is incorrect.")
                .setPositiveButton("OK", (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    // Torna all'inserimento della password
                    showSecondConfirmationDialog();
                })
                .setCancelable(false)
                .create();

        // Personalizza i colori del dialog
        dialog.setOnShowListener(dialogInterface -> {
            // Colora il titolo in rosso
            TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
            if (titleView != null) {
                titleView.setTextColor(Color.parseColor("#FFD700"));
            }

            // Colora il messaggio
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTextColor(Color.parseColor("#FFFFFF"));
            }

            // Colora il pulsante OK
            android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setTextColor(Color.parseColor("#FFD700"));
            }
        });

        dialog.show();
    }

    private void deleteAllGameResults() {
        progressBar.setVisibility(View.VISIBLE);

        resultsRef.removeValue().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);

            if(task.isSuccessful()) {
                Log.d("ResultsActivity", "All game results deleted successfully");

                // Mostra dialog di conferma eliminazione e torna al menu
                showDataDeletedDialog();
            } else {
                Log.e("ResultsActivity", "Error deleting game results", task.getException());
                Toast.makeText(this, "Error deleting data: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDataDeletedDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("All data has been deleted")
                .setPositiveButton("OK", (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    // Torna al GameMenuActivity
                    finish();
                })
                .setCancelable(false)
                .create();

        // Personalizza i colori del dialog
        dialog.setOnShowListener(dialogInterface -> {
            // Colora il titolo in verde
            TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
            if (titleView != null) {
                titleView.setTextColor(Color.parseColor("#FFD700"));
            }

            // Colora il messaggio
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTextColor(Color.parseColor("#FFFFFF"));
            }

            // Colora il pulsante OK
            android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setTextColor(Color.parseColor("#FFD700"));
            }
        });

        dialog.show();
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
        // Card container con sfondo scuro
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#2C2C2C"));
        card.setPadding(32, 24, 32, 24);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(16, 16, 16, 16);
        card.setLayoutParams(cardParams);
        card.setClickable(true);
        card.setFocusable(true);

        // Data - testo chiaro
        TextView tvDate = new TextView(this);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(result.getTimestamp())));
        tvDate.setTextSize(14);
        tvDate.setTextColor(Color.parseColor("#B0B0B0"));
        card.addView(tvDate);

        // Winner - testo oro
        TextView tvWinner = new TextView(this);
        String winnerText;
        // Se winnerName contiene "&" o "," significa che ci sono più vincitori
        if(result.getWinnerName() != null && (result.getWinnerName().contains(" & ") || result.getWinnerName().contains(", "))) {
            winnerText = "🏆 Winners: " + result.getWinnerName();
        } else {
            winnerText = "🏆 Winner: " + result.getWinnerName();
        }
        tvWinner.setText(winnerText);
        tvWinner.setTextSize(18);
        tvWinner.setTextColor(Color.parseColor("#FFD700"));
        tvWinner.setTypeface(null, android.graphics.Typeface.BOLD);
        tvWinner.setPadding(0, 8, 0, 0);
        card.addView(tvWinner);

        // Victory type (se non è vittoria ai punti)
        if(result.getVictoryType() != null && !result.getVictoryType().equals("points")) {
            TextView tvVictoryType = new TextView(this);
            String typeText = "";
            if(result.getVictoryType().equals("military")) {
                typeText = "⚔️ Military Supremacy";
                tvVictoryType.setTextColor(Color.parseColor("#FF6B6B"));
            } else if(result.getVictoryType().equals("scientific")) {
                typeText = "🔬 Scientific Supremacy";
                tvVictoryType.setTextColor(Color.parseColor("#008F39"));
            }
            tvVictoryType.setText(typeText);
            tvVictoryType.setTextSize(14);
            tvVictoryType.setPadding(0, 4, 0, 0);
            card.addView(tvVictoryType);
        }

        // Numero giocatori - MOSTRA SOLO SE NON È DUEL (2 giocatori)
        if(result.getNumPlayers() != 2) {
            TextView tvPlayers = new TextView(this);
            tvPlayers.setText(result.getNumPlayers() + " players");
            tvPlayers.setTextSize(14);
            tvPlayers.setTextColor(Color.parseColor("#B0B0B0"));
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