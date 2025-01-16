package com.example.assignment5;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class StatisticsActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    private Random random = new Random();

    private long gameTimeInSecondsP1 = 0;
    private long gameTimeInSecondsP2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mainLayout = findViewById(R.id.main_layout);
        TextView winnerTextView = findViewById(R.id.winner_text_view);
        TextView statsText = findViewById(R.id.stats_text_view);

        // Retrieve intent and statistics
        Intent intent = getIntent();
        int matchesMadeP1 = intent.getIntExtra("MatchesMadeP1", 0);
        int totalAttemptsP1 = intent.getIntExtra("TotalAttemptsP1", 0);
        int firstTryMatchesP1 = intent.getIntExtra("FirstTryMatchesP1", 0);
        int numberOfMovesP1 = intent.getIntExtra("NumberOfMovesP1", 0);
        gameTimeInSecondsP1 = intent.getLongExtra("GameTimeInSecondsP1", 0);

        int matchesMadeP2 = intent.getIntExtra("MatchesMadeP2", 0);
        int totalAttemptsP2 = intent.getIntExtra("TotalAttemptsP2", 0);
        int firstTryMatchesP2 = intent.getIntExtra("FirstTryMatchesP2", 0);
        int numberOfMovesP2 = intent.getIntExtra("NumberOfMovesP2", 0);
        gameTimeInSecondsP2 = intent.getLongExtra("GameTimeInSecondsP2", 0);

        String stats = String.format("Player 1:\nMatches Made: %d\nTotal Attempts: %d\nFirst Try Matches: " +
                        "%d\nNumber of Moves: %d\nGame Time: %ds\n\nPlayer 2:\nMatches Made: %d\nTotal Attempts: " +
                        "%d\nFirst Try Matches: %d\nNumber of Moves: %d\nGame Time: %ds",
                matchesMadeP1, totalAttemptsP1, firstTryMatchesP1, numberOfMovesP1, gameTimeInSecondsP1,
                matchesMadeP2, totalAttemptsP2, firstTryMatchesP2, numberOfMovesP2, gameTimeInSecondsP2);

        statsText.setText(stats);

        // Animate confetti
        if (gameTimeInSecondsP1 > 0 || gameTimeInSecondsP2 > 0) {
            animateConfetti();  // Call this method to start the confetti effect
        }

        // Determine the winner and display
        String winner = determineWinner();
        winnerTextView.setText(winner);
        winnerTextView.setVisibility(View.VISIBLE);
        animateWinnerAnnouncement(winnerTextView);
    }

    private void animateConfetti() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        for (int i = 0; i < 30; i++) {
            ImageView confetti = new ImageView(this);
            confetti.setImageResource(R.drawable.celebrate);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.leftMargin = random.nextInt(screenWidth); // Random start X position
            confetti.setLayoutParams(params);
            mainLayout.addView(confetti);

            ObjectAnimator fallAnimator = ObjectAnimator.ofFloat(confetti, "translationY", -100f, getResources().getDisplayMetrics().heightPixels + 100f);
            fallAnimator.setDuration(3000 + random.nextInt(3000)); // Random duration between 3 to 6 seconds
            fallAnimator.start();
        }
    }

    private String determineWinner() {
        if (gameTimeInSecondsP1 < gameTimeInSecondsP2) {
            return "Player 1 Wins!";
        } else if (gameTimeInSecondsP1 > gameTimeInSecondsP2) {
            return "Player 2 Wins!";
        } else {
            return "It's a Tie!";
        }
    }

    private void animateWinnerAnnouncement(TextView winnerTextView) {
        winnerTextView.setAlpha(0f); // Start fully transparent

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(winnerTextView, "alpha", 0f, 1f);
        fadeIn.setDuration(1000);

        ObjectAnimator moveUp = ObjectAnimator.ofFloat(winnerTextView, "translationY", 100, 0);
        moveUp.setDuration(1000);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeIn, moveUp);
        animatorSet.start();
    }
}
