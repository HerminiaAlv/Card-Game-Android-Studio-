package com.example.assignment5;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.graphics.drawable.AnimationDrawable;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {
    private GridLayout gridLayout;
    private TextView textViewStats;
    private Button btnShowStats; // Button to show statistics
    private SharedPreferences preferences;
    private int[] images = {
            R.drawable.clover, R.drawable.clover, R.drawable.heart, R.drawable.heart,
            R.drawable.dog, R.drawable.dog, R.drawable.rainbow, R.drawable.rainbow,
            R.drawable.star, R.drawable.star
    };

    private ImageButton[] buttons = new ImageButton[10];
    private boolean[] attempted = new boolean[10];

    private int currentPlayer = 1;
    private int[] matchesMade = new int[3]; // Using index 1 and 2 for Player 1 and 2
    private int[] totalAttempts = new int[3];
    private int[] firstTryMatches = new int[3];
    private int[] numberOfMoves = new int[3];
    private long[] startTime = new long[3];
    private long[] gameTimeInSeconds = new long[3];

    private int firstCard = -1, secondCard = -1;
    private boolean isBusy = false;

    private AnimationDrawable winningAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridLayout = findViewById(R.id.gridLayout);
        textViewStats = findViewById(R.id.textViewStats);
        btnShowStats = findViewById(R.id.btnShowStats);
        Button btnSwitchPlayer = findViewById(R.id.btnSwitchPlayer);

        btnShowStats.setOnClickListener(v -> showStatistics());
        btnSwitchPlayer.setOnClickListener(v -> switchPlayer());

        preferences = getSharedPreferences("GameStats", MODE_PRIVATE);
        setupGame();
        updateStatsDisplay();
    }

    private void setupGame() {
        shuffleArray(images);
        for (int i = 0; i < images.length; i++) {
            ImageButton button = new ImageButton(this);
            button.setId(i);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(i / 2, GridLayout.FILL, 1f),
                    GridLayout.spec(i % 2, GridLayout.FILL, 1f));
            params.width = 0;
            params.height = 0;
            params.setMargins(10, 10, 10, 10);
            button.setLayoutParams(params);
            button.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            button.setImageResource(R.drawable.card_back);
            button.setOnClickListener(this::onCardClick);
            gridLayout.addView(button);
            buttons[i] = button;
        }
        startTime[currentPlayer] = System.currentTimeMillis();
    }

    private void switchPlayer() {
        if (currentPlayer == 1) {
            currentPlayer = 2;
        } else {
            currentPlayer = 1;
        }
        textViewStats.setText("Player " + currentPlayer + " Turn");
        resetGame();
    }

    private void resetGame() {
        for (ImageButton button : buttons) {
            button.setImageResource(R.drawable.card_back);
            button.setVisibility(View.VISIBLE);
            button.setEnabled(true);
        }
        attempted = new boolean[10]; // Reset attempts
        shuffleArray(images);
        firstCard = -1;
        secondCard = -1;
        startTime[currentPlayer] = System.currentTimeMillis();


        // Reset statistics for the current player
        matchesMade[currentPlayer] = 0;
        totalAttempts[currentPlayer] = 0;
        firstTryMatches[currentPlayer] = 0;
        numberOfMoves[currentPlayer] = 0;
        gameTimeInSeconds[currentPlayer] = 0;
        updateStatsDisplay();
    }

    private void onCardClick(View v) {
        if (isBusy) return;

        ImageButton selectedButton = (ImageButton) v;
        int clickedIndex = selectedButton.getId();

        if (firstCard == -1) {
            firstCard = clickedIndex;
            selectedButton.setImageResource(images[clickedIndex]);
            numberOfMoves[currentPlayer]++;
        } else if (secondCard == -1 && firstCard != clickedIndex) {
            secondCard = clickedIndex;
            selectedButton.setImageResource(images[clickedIndex]);
            totalAttempts[currentPlayer]++;
            isBusy = true;

            gridLayout.postDelayed(this::checkForMatch, 500);
        }
    }

    private void checkForMatch() {
        if (images[firstCard] == images[secondCard]) {
            buttons[firstCard].setVisibility(View.INVISIBLE);
            buttons[secondCard].setVisibility(View.INVISIBLE);
            matchesMade[currentPlayer]++;
            if (!attempted[firstCard] && !attempted[secondCard]) {
                firstTryMatches[currentPlayer]++;
            }

            if (matchesMade[currentPlayer] == images.length / 2) {
                long endTime = System.currentTimeMillis();
                gameTimeInSeconds[currentPlayer] = (endTime - startTime[currentPlayer]) / 1000;
                saveStats();
                if (currentPlayer == 2) {  // Only show stats button when both players have played
                    showWinner();
                }
            }
        } else {
            buttons[firstCard].setImageResource(R.drawable.card_back);
            buttons[secondCard].setImageResource(R.drawable.card_back);
            attempted[firstCard] = true;
            attempted[secondCard] = true;
        }
        firstCard = -1;
        secondCard = -1;
        isBusy = false;
        updateStatsDisplay();
    }

    private void showWinner() {
        String winnerMessage;
        if (gameTimeInSeconds[1] < gameTimeInSeconds[2]) {
            winnerMessage = "Player 1 wins!";
        } else if (gameTimeInSeconds[1] > gameTimeInSeconds[2]) {
            winnerMessage = "Player 2 wins!";
        } else {
            winnerMessage = "It's a tie!";
        }
        Toast.makeText(this, winnerMessage, Toast.LENGTH_LONG).show();

        ImageView gifImageView = findViewById(R.id.gifImageView);
        gifImageView.setVisibility(View.VISIBLE); // Make the ImageView visible
        Glide.with(MainActivity.this)
                .load(R.drawable.pointing)
                .into(gifImageView);

        btnShowStats.setVisibility(View.VISIBLE);
    }

    private void saveStats() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("MatchesMade" + currentPlayer, matchesMade[currentPlayer]);
        editor.putInt("TotalAttempts" + currentPlayer, totalAttempts[currentPlayer]);
        editor.putInt("FirstTryMatches" + currentPlayer, firstTryMatches[currentPlayer]);
        editor.putInt("NumberOfMoves" + currentPlayer, numberOfMoves[currentPlayer]);
        editor.putLong("GameTimeInSeconds" + currentPlayer, gameTimeInSeconds[currentPlayer]);
        editor.apply();
    }

    private void updateStatsDisplay() {
        textViewStats.setText(String.format("Player %d: Matches: %d, Attempts: %d, Time: %ds",
                currentPlayer, matchesMade[currentPlayer], totalAttempts[currentPlayer], gameTimeInSeconds[currentPlayer]));
    }

    private void shuffleArray(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = (int) (Math.random() * (i + 1));
            int a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }

    private void loadStats() {
        // Load statistics for player 1
        matchesMade[1] = preferences.getInt("MatchesMade1", 0);
        totalAttempts[1] = preferences.getInt("TotalAttempts1", 0);
        firstTryMatches[1] = preferences.getInt("FirstTryMatches1", 0);
        numberOfMoves[1] = preferences.getInt("NumberOfMoves1", 0);
        gameTimeInSeconds[1] = preferences.getLong("GameTimeInSeconds1", 0);

        // If there are no saved statistics for player 1, set them to 0
        if (matchesMade[1] == 0 && totalAttempts[1] == 0 && firstTryMatches[1] == 0 && numberOfMoves[1] == 0 && gameTimeInSeconds[1] == 0) {
            matchesMade[1] = 0;
            totalAttempts[1] = 0;
            firstTryMatches[1] = 0;
            numberOfMoves[1] = 0;
            gameTimeInSeconds[1] = 0;
        }

        // Load statistics for player 2
        matchesMade[2] = preferences.getInt("MatchesMade2", 0);
        totalAttempts[2] = preferences.getInt("TotalAttempts2", 0);
        firstTryMatches[2] = preferences.getInt("FirstTryMatches2", 0);
        numberOfMoves[2] = preferences.getInt("NumberOfMoves2", 0);
        gameTimeInSeconds[2] = preferences.getLong("GameTimeInSeconds2", 0);
    }

    private void showStatistics() {
        Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
        intent.putExtra("PlayerName", "Both Players");
        intent.putExtra("MatchesMadeP1", matchesMade[1]);
        intent.putExtra("TotalAttemptsP1", totalAttempts[1]);
        intent.putExtra("FirstTryMatchesP1", firstTryMatches[1]);
        intent.putExtra("NumberOfMovesP1", numberOfMoves[1]);
        intent.putExtra("GameTimeInSecondsP1", gameTimeInSeconds[1]);
        intent.putExtra("MatchesMadeP2", matchesMade[2]);
        intent.putExtra("TotalAttemptsP2", totalAttempts[2]);
        intent.putExtra("FirstTryMatchesP2", firstTryMatches[2]);
        intent.putExtra("NumberOfMovesP2", numberOfMoves[2]);
        intent.putExtra("GameTimeInSecondsP2", gameTimeInSeconds[2]);
        startActivity(intent);
    }
}
