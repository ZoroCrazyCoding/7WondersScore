package com.example.sevenwondersscore;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GameDetailActivity extends AppCompatActivity {

    TableLayout tableDetail;
    TextView tvGameInfo;
    String gameType;
    GameResult currentGameResult;
    DatabaseReference resultsRef;

    String[] symbols7Wonders = {"⛰","🪙","⚔️","🟦","🟧","🟩","🟪"};
    String[] symbolsDuel = {"🟦","🟩","🟨","🟪","⛰","🟢","🪙","⚔️"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if(toolbar.getNavigationIcon() != null)
            toolbar.getNavigationIcon().setTint(Color.parseColor("#FFD700"));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tableDetail = findViewById(R.id.tableDetail);
        tvGameInfo = findViewById(R.id.tvGameInfo);

        // Bottone Delete dal layout XML
        Button btnDeleteGame = findViewById(R.id.btnDeleteGame);
        btnDeleteGame.setOnClickListener(v -> showDeleteConfirmation());

        // Ricevi i dati della partita
        currentGameResult = (GameResult) getIntent().getSerializableExtra("gameResult");
        gameType = getIntent().getStringExtra("gameType");

        // Riferimento Firebase
        FirebaseDatabase europeanDatabase = FirebaseDatabase.getInstance(
                "https://sevenwondersscore-default-rtdb.europe-west1.firebasedatabase.app"
        );
        resultsRef = europeanDatabase
                .getReference("game_results")
                .child(gameType);

        if(currentGameResult != null) {
            displayGameDetails(currentGameResult);
        }
    }

    private void displayGameDetails(GameResult result) {
        boolean isDuel = "7wondersduel".equals(gameType);
        String[] symbols = isDuel ? symbolsDuel : symbols7Wonders;

        // Info partita
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String infoText = "Date: " + sdf.format(new Date(result.getTimestamp())) + "\n";
        if(result.getWinnerName() != null && (result.getWinnerName().contains(" & ") || result.getWinnerName().contains(", "))) {
            infoText += "Winners: " + result.getWinnerName() + "\n";
        } else {
            infoText += "Winner: " + result.getWinnerName() + "\n";
        }

        if(isDuel) {
            if(result.getVictoryType() != null && !result.getVictoryType().equals("points")) {
                if(result.getVictoryType().equals("military")) {
                    infoText += "Victory Type: ⚔️ Military Supremacy\n";
                } else if(result.getVictoryType().equals("scientific")) {
                    infoText += "Victory Type: 🔬 Scientific Supremacy\n";
                }
            } else {
                infoText += "Victory Type: Points\n";
            }
        }

        if(result.getNumPlayers() != 2) {
            infoText += "Players: " + result.getNumPlayers();
        }

        tvGameInfo.setText(infoText);

        // Crea tabella
        tableDetail.removeAllViews();

        // Header con nomi giocatori
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.parseColor("#2C2C2C"));
        headerRow.setPadding(0, 16, 0, 16);
        headerRow.setGravity(Gravity.CENTER);

        TextView tvHeaderSymbol = new TextView(this);
        tvHeaderSymbol.setText("Category");
        tvHeaderSymbol.setGravity(Gravity.CENTER);
        tvHeaderSymbol.setPadding(16, 8, 16, 8);
        tvHeaderSymbol.setTypeface(null, android.graphics.Typeface.BOLD);
        tvHeaderSymbol.setTextColor(Color.parseColor("#FFD700"));
        headerRow.addView(tvHeaderSymbol);

        for(GameResult.PlayerResult player : result.getPlayers()) {
            TextView tvName = new TextView(this);
            tvName.setText(player.getName());
            tvName.setGravity(Gravity.CENTER);
            tvName.setPadding(16, 8, 16, 8);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            tvName.setTextSize(14);

            if(result.isWinner(player.getName())) {
                tvName.setTextColor(Color.parseColor("#FFD700"));
            } else {
                tvName.setTextColor(Color.parseColor("#FFFFFF"));
            }

            headerRow.addView(tvName);
        }

        tableDetail.addView(headerRow);

        // Righe con i punteggi per categoria
        for(int i = 0; i < symbols.length; i++) {
            TableRow row = new TableRow(this);
            row.setPadding(0, 8, 0, 8);
            row.setGravity(Gravity.CENTER);

            TextView tvSymbol = new TextView(this);
            tvSymbol.setText(symbols[i]);
            tvSymbol.setTextSize(24);
            tvSymbol.setGravity(Gravity.CENTER);
            tvSymbol.setPadding(16, 8, 16, 8);
            row.addView(tvSymbol);

            for(GameResult.PlayerResult player : result.getPlayers()) {
                TextView tvScore = new TextView(this);
                List<Integer> scores = player.getCategoryScores();
                int score = (i < scores.size()) ? scores.get(i) : 0;
                tvScore.setText(String.valueOf(score));
                tvScore.setGravity(Gravity.CENTER);
                tvScore.setPadding(16, 8, 16, 8);
                tvScore.setTextSize(16);
                tvScore.setTextColor(Color.parseColor("#FFFFFF"));
                row.addView(tvScore);
            }

            tableDetail.addView(row);

            View divider = new View(this);
            divider.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(Color.parseColor("#404040"));
            tableDetail.addView(divider);
        }

        // Riga totale
        TableRow totalRow = new TableRow(this);
        totalRow.setBackgroundColor(Color.parseColor("#1E1E1E"));
        totalRow.setPadding(0, 16, 0, 16);
        totalRow.setGravity(Gravity.CENTER);

        TextView tvTotalLabel = new TextView(this);
        tvTotalLabel.setText("∑");
        tvTotalLabel.setGravity(Gravity.CENTER);
        tvTotalLabel.setPadding(16, 8, 16, 8);
        tvTotalLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTotalLabel.setTextSize(22);
        tvTotalLabel.setTextColor(Color.parseColor("#FFD700"));
        totalRow.addView(tvTotalLabel);

        for(GameResult.PlayerResult player : result.getPlayers()) {
            TextView tvTotal = new TextView(this);
            tvTotal.setText(String.valueOf(player.getTotalScore()));
            tvTotal.setGravity(Gravity.CENTER);
            tvTotal.setPadding(16, 8, 16, 8);
            tvTotal.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTotal.setTextSize(18);

            if(result.isWinner(player.getName())) {
                tvTotal.setTextColor(Color.parseColor("#FFD700"));
            } else {
                tvTotal.setTextColor(Color.parseColor("#FFFFFF"));
            }

            totalRow.addView(tvTotal);
        }

        tableDetail.addView(totalRow);

        // CHECKBOX PER SUPREMAZIE (SOLO DUEL)
        if(isDuel) {
            TableRow cbRow = new TableRow(this);
            cbRow.setPadding(0, 16, 0, 0);
            cbRow.setGravity(Gravity.CENTER);

            TextView tvEmpty = new TextView(this);
            tvEmpty.setText(" ");
            tvEmpty.setPadding(16, 8, 16, 8);
            cbRow.addView(tvEmpty);

            String winnerName = result.getWinnerName();
            String victoryType = result.getVictoryType();
            boolean hasMilitaryVictory = "military".equals(victoryType);
            boolean hasScientificVictory = "scientific".equals(victoryType);

            for(GameResult.PlayerResult player : result.getPlayers()) {
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
                cbRed.setButtonTintList(android.content.res.ColorStateList.valueOf(
                        Color.parseColor("#B90E0A")
                ));
                cbRed.setEnabled(false);
                if(hasMilitaryVictory && player.getName().equals(winnerName)) {
                    cbRed.setChecked(true);
                }

                CheckBox cbGreen = new CheckBox(this);
                cbGreen.setButtonTintList(android.content.res.ColorStateList.valueOf(
                        Color.parseColor("#02971F")
                ));
                cbGreen.setEnabled(false);
                if(hasScientificVictory && player.getName().equals(winnerName)) {
                    cbGreen.setChecked(true);
                }

                ll.addView(cbRed);
                ll.addView(cbGreen);
                cbRow.addView(ll);
            }

            tableDetail.addView(cbRow);
        }
    }

    private void showDeleteConfirmation() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete Game")
                .setMessage("Do you want to delete this game?")
                .setPositiveButton("Yes", null) // gestito manualmente
                .setNegativeButton("No", (d, w) -> d.dismiss())
                .setCancelable(true)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
            if (titleView != null) titleView.setTextColor(Color.parseColor("#FFD700"));

            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) messageView.setTextColor(Color.parseColor("#FFFFFF"));

            android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            android.widget.Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(Color.parseColor("#FFD700"));
                positiveButton.setOnClickListener(v -> {
                    dialog.dismiss();
                    showPasswordDialog();
                });
            }
            if (negativeButton != null) {
                negativeButton.setTextColor(Color.parseColor("#B0B0B0"));
            }
        });

        dialog.show();
    }


    private void showPasswordDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText input = new EditText(this);
        input.setHint("Enter password");
        input.setHintTextColor(Color.parseColor("#888888"));
        input.setTextColor(Color.parseColor("#FFFFFF"));
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        inputParams.setMargins(20, 10, 20, 10);
        input.setLayoutParams(inputParams);

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

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            input.setSelection(input.getText().length());
        });

        layout.addView(input);
        layout.addView(checkBox);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Enter Password")
                .setMessage("Enter the password to confirm deletion:")
                .setView(layout)
                .setPositiveButton("Confirm", null) // gestito manualmente
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setCancelable(true)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
            if (titleView != null) titleView.setTextColor(Color.parseColor("#FFD700"));

            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) messageView.setTextColor(Color.parseColor("#FFFFFF"));

            android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            android.widget.Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(Color.parseColor("#FFD700"));
                positiveButton.setOnClickListener(v -> {
                    String enteredPassword = input.getText().toString();
                    if (enteredPassword.equals("Zoro5212!")) {
                        dialog.dismiss();
                        deleteGame();
                    } else {
                        dialog.dismiss();
                        showWrongPasswordDialog();
                    }
                });
            }
            if (negativeButton != null) {
                negativeButton.setTextColor(Color.parseColor("#B0B0B0"));
            }
        });

        dialog.show();
    }

    private void showWrongPasswordDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Wrong Password")
                .setMessage("The password you entered is incorrect.")
                .setPositiveButton("OK", (d, w) -> {
                    d.dismiss();
                    showPasswordDialog(); // Riprova
                })
                .setCancelable(false)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
            if (titleView != null) titleView.setTextColor(Color.parseColor("#FFD700"));

            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) messageView.setTextColor(Color.parseColor("#FFFFFF"));

            android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positiveButton != null) positiveButton.setTextColor(Color.parseColor("#FFD700"));
        });

        dialog.show();
    }

    private void deleteGame() {
        if(currentGameResult == null || currentGameResult.getGameId() == null) {
            Toast.makeText(this, "Error: Game ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String gameId = currentGameResult.getGameId();
        Log.d("GameDetailActivity", "Deleting game with ID: " + gameId);

        resultsRef.child(gameId).removeValue().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Log.d("GameDetailActivity", "Game deleted successfully");
                Toast.makeText(this, "Game deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e("GameDetailActivity", "Error deleting game", task.getException());
                Toast.makeText(this, "Error deleting game: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}