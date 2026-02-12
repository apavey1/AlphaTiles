package org.friendsoftonga.akosipela;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

// Static imports from Start.java
import static org.friendsoftonga.akosipela.Start.gameList;
import static org.friendsoftonga.akosipela.Start.gameSounds;
import static org.friendsoftonga.akosipela.Start.correctFinalSoundID;

public class Celebration extends AppCompatActivity {

    Context context;
    SharedPreferences prefs;
    String playerString;
    int currentGameNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.celebration);
        context = this;

        // Retrieve player and game info exactly like GameActivity
        int playerNumber = getIntent().getIntExtra("playerNumber", -1);
        playerString = Util.returnPlayerStringToAppend(playerNumber);
        prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        currentGameNumber = getIntent().getIntExtra("gameNumber", 0);

        // UI Setup using your specific IDs
        ActivityLayouts.applyEdgeToEdge(this, R.id.celebrationCL);
        ActivityLayouts.setStatusAndNavColors(this);

        // Disable back button to force use of on-screen choices
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing
            }
        });

        // Play the celebration sound
        gameSounds.play(correctFinalSoundID, 1.0f, 1.0f, 2, 0, 1.0f);
    }

    public void replayLevel(View view) {
        // Relaunch the current game by resetting the intent class
        launchGame(getIntent(), getIntent().getStringExtra("country"));
    }

    public void goToNextLevel(View view) {
        Intent intent = getIntent();
        String project = "org.friendsoftonga.akosipela.";
        boolean foundNextUncompletedGame = false;
        int nextGameNum = currentGameNumber;
        int repeat = 0;

        // Search logic from GameActivity.java
        while (!foundNextUncompletedGame && repeat < gameList.size()) {
            nextGameNum++;

            // Loop back to first game if at the end
            if (nextGameNum > gameList.size()) {
                nextGameNum = 1;
            }

            // Pulling info from gameList without needing GameNext class
            int nextChallengeLevel = Integer.parseInt(gameList.get(nextGameNum - 1).level);
            int nextStage;
            if (gameList.get(nextGameNum - 1).stage.equals("-")) {
                nextStage = 1;
            } else {
                nextStage = Integer.parseInt(gameList.get(nextGameNum - 1).stage);
            }
            String nextSyllableGame = gameList.get(nextGameNum - 1).mode;
            String nextCountry = gameList.get(nextGameNum - 1).country;
            String activityClass = project + nextCountry;

            // Check if player has already finished this specific game
            String nextID = activityClass + nextChallengeLevel + playerString + nextSyllableGame + nextStage;
            boolean isDone = prefs.getBoolean(nextID + "_hasChecked12Trackers", false);

            if (!isDone) {
                foundNextUncompletedGame = true;
                intent.putExtra("challengeLevel", nextChallengeLevel);
                intent.putExtra("stage", nextStage);
                intent.putExtra("syllableGame", nextSyllableGame);
                intent.putExtra("gameNumber", nextGameNum);
                intent.putExtra("country", nextCountry);
                launchGame(intent, nextCountry);
            }
            repeat++;
        }

        // If all games are complete, return to Earth
        if (!foundNextUncompletedGame) {
            goBackToEarth(null);
        }
    }

    public void goBackToEarth(View view) {
        Intent intent = getIntent();
        intent.setClass(context, Earth.class);
        startActivity(intent);
        finish();
    }

    private void launchGame(Intent intent, String country) {
        try {
            String activityClass = "org.friendsoftonga.akosipela." + country;
            intent.setClass(context, Class.forName(activityClass));
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException e) {
            goBackToEarth(null);
        }
    }
}