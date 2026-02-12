package org.friendsoftonga.akosipela;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static org.friendsoftonga.akosipela.Start.*;

import java.util.Scanner;


public class Earth extends AppCompatActivity {
    Context context;
    String scriptDirection = Start.langInfoList.find("Script direction (LTR or RTL)");

    int playerNumber = -1;
    String playerString;
    char grade;
    int pageNumber; // Games 001 to 033 are displayed on page 1, games 034 to 066 are displayed on page 2, etc.
    int globalPoints;
    int doorsPerPage = 35;
    ConstraintLayout earthCL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Disable back navigation
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Intentionally empty — do nothing
            }
        });

        context = this;
        playerNumber = getIntent().getIntExtra("playerNumber", -1);
        playerString = Util.returnPlayerStringToAppend(playerNumber);
        setContentView(R.layout.earth);
        earthCL = findViewById(R.id.earthCL);

        ActivityLayouts.applyEdgeToEdge(this, R.id.earthCL);
        ActivityLayouts.setStatusAndNavColors(this);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (scriptDirection.equals("RTL")) {
            ImageView goForwardImage = (ImageView) findViewById(R.id.goForward);
            ImageView goBackImage = (ImageView) findViewById(R.id.goBack);
            ImageView activePlayerImage = (ImageView) findViewById(R.id.activePlayerImage);

            goForwardImage.setRotationY(180);
            goBackImage.setRotationY(180);
            activePlayerImage.setRotationY(180);
        }

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        String playerGlobalPointsKey = "storedPoints_player" + playerString;
        globalPoints = prefs.getInt(playerGlobalPointsKey, 0);

        TextView pointsEarned = findViewById(R.id.pointsTextView);
        pointsEarned.setText(String.valueOf(globalPoints));

        ImageView avatar = findViewById(R.id.activePlayerImage);
        int resID = getResources().getIdentifier(String.valueOf(ChoosePlayer.AVATAR_JPG_IDS[playerNumber - 1]), "drawable", getPackageName());
        avatar.setImageResource(resID);

        String defaultName;
        String playerName;
        String localWordForName = langInfoList.find("NAME in local language");
        if (localWordForName.equals("custom")) {
            defaultName = Start.nameList.get(playerNumber - 1);
        } else {
            defaultName = localWordForName + " " + playerNumber;
        }
        playerName = prefs.getString("storedName" + playerString, defaultName);

        // find one-digit grade level of student (ASSUMES GRADE IS ONLY ONE DIGIT LONG AND ONLY DIGIT IN NAME)
        for (int i = 0; i < playerName.length(); i++) {
            char nameChar = playerName.charAt(i);
            if (Character.isDigit(nameChar)) grade = nameChar;
        }

        TextView name = findViewById(R.id.avatarName);
        name.setText(playerName);

        pageNumber = getIntent().getIntExtra("pageNumber", 0);

        updateDoors();

        if (scriptDirection.equals("RTL")) {
            forceRTLIfSupported();
        } else {
            forceLTRIfSupported();
        }

        resID = context.getResources().getIdentifier("zzz_earth", "raw", context.getPackageName());
        if (resID == 0) {
            // hide audio instructions icon
            ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
            instructionsButton.setVisibility(View.GONE);

            ConstraintLayout constraintLayout = findViewById(R.id.earthCL);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.centerHorizontally(R.id.resourcePromo, R.id.earthCL);
            constraintSet.applyTo(constraintLayout);
        }

        boolean noShareIcon = false;
        if (context.getResources().getIdentifier("aa_share", "raw", context.getPackageName()) == 0) {
            noShareIcon = true;
        } else {
            Scanner shareScanner = new Scanner(getResources().openRawResource(R.raw.aa_share));
            if (shareScanner.hasNext()) {
                shareScanner.nextLine(); // skip the header line
                if (!shareScanner.hasNext())
                    noShareIcon = true;
                else if (shareScanner.next().isEmpty())
                    noShareIcon = true;
            } else {
                noShareIcon = true;
            }
        }

        boolean noResources = true;
        if (context.getResources().getIdentifier("aa_resources", "raw", context.getPackageName()) != 0) { // Checks if resource file exists
            Scanner resourceScanner = new Scanner(getResources().openRawResource(R.raw.aa_resources));
            if (resourceScanner.hasNext()) { // See if there is anything in resource file
                resourceScanner.nextLine(); // Skips the header line
                if (resourceScanner.hasNext() && !resourceScanner.next().isEmpty()) { // If there is a line after the header that is not an empty string ""
                    noResources = false;
                }
            }
        }
        if (noResources) {
            ImageView resourcesIcon = findViewById(R.id.resourcePromo);
            resourcesIcon.setVisibility(View.GONE);
            resourcesIcon.setOnClickListener(null);
        }
    }

    public void updateDoors() {

        SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
        int trackerCount;

        for (int j = 0; j < earthCL.getChildCount(); j++) {View child = earthCL.getChildAt(j);
            if (child instanceof TextView && child.getTag() != null) {
                try {
                    int doorIndex = Integer.parseInt((String) child.getTag()) - 1;
                    String doorText = String.valueOf((pageNumber * doorsPerPage) + doorIndex + 1);
                    ((TextView) child).setText(doorText);

                    if (((pageNumber * doorsPerPage) + doorIndex) >= Start.gameList.size()) {
                        ((TextView) child).setVisibility(View.INVISIBLE);
                        continue; // Skip doors that don't correspond to a game
                    }

                    int absoluteGameNumber = (pageNumber * doorsPerPage) + doorIndex + 1;
                    String doorStyle;

                    if (absoluteGameNumber >= 1 && absoluteGameNumber <= 3) {
                        doorStyle = "_tutorial";
                        ((TextView) child).setTextColor(Color.parseColor("#000000")); // SET TEXT TO BLACK

                    } else {
                        String country = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).country;
                        String challengeLevel = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).level;
                        String syllableGame = gameList.get((pageNumber * doorsPerPage) + doorIndex).mode;
                        String stage;
                        if (gameList.get((pageNumber * doorsPerPage) + doorIndex).stage.equals("-")) {
                            stage = "1";
                        } else {
                            stage = gameList.get((pageNumber * doorsPerPage) + doorIndex).stage;
                        }
                        String project = "org.friendsoftonga.akosipela.";
                        String uniqueGameLevelPlayerModeStageID = project + country + challengeLevel + playerString + syllableGame + stage;
                        trackerCount = prefs.getInt(uniqueGameLevelPlayerModeStageID + "_trackerCount", 0);

                        if (country.equals("Romania") || country.equals("Sudan") || country.equals("Malaysia") || country.equals("Iraq")) {
                            doorStyle = "_inprocess";
                            ((TextView) child).setTextColor(Color.parseColor("#FFFFFF"));
                        } else if (trackerCount > 0 && trackerCount < 12) {
                            doorStyle = "_inprocess";
                            ((TextView) child).setTextColor(Color.parseColor("#FFFFFF"));
                        } else if (trackerCount >= 12) {
                            doorStyle = "_mastery";
                            ((TextView) child).setTextColor(Color.parseColor("#000000"));
                        } else { // 0
                            doorStyle = ""; // Default circle
                            ((TextView) child).setTextColor(Color.parseColor("#FFFFFF"));
                        }
                    }

                    String drawableBase = "zz_door";
                    String drawableEntryName = drawableBase + doorStyle;
                    int resId = getResources().getIdentifier(drawableEntryName, "drawable", getPackageName());
                    ((TextView) child).setBackgroundResource(resId);

                    ((TextView) child).setVisibility(View.VISIBLE);

                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }

        // This part for the arrows is correct, leave it as is
        ImageView backArrow = findViewById(R.id.goBack);
        if (pageNumber == 0) {
            backArrow.setImageResource(R.drawable.zz_backward_inactive);
            backArrow.setClickable(false);
        } else {
            backArrow.setImageResource(R.drawable.zz_backward);
            backArrow.setClickable(true);
        }

        ImageView forwardArrow = findViewById(R.id.goForward);
        if (((pageNumber + 1) * doorsPerPage) < Start.gameList.size()) {
            forwardArrow.setImageResource(R.drawable.zz_forward);
            forwardArrow.setClickable(true);
        } else {
            forwardArrow.setImageResource(R.drawable.zz_forward_inactive);
            forwardArrow.setClickable(false);
        }
    }
    public void goToAboutPage(View view) {

        Intent intent = getIntent();
        intent.setClass(context, About.class);
        startActivity(intent);

    }

    public void goBackToChoosePlayer(View view) {

        startActivity(new Intent(context, ChoosePlayer.class));
        finish();

    }

    public void goToResources(View view) {

        Intent intent = getIntent();
        intent.setClass(context, Resources.class);
        startActivity(intent);

    }

    public void goToShare(View view) {
        Intent intent = getIntent();
        intent.setClass(context, Share.class);
        startActivity(intent);
    }

    public void goToDoor(View view) {

        finish();
        int doorIndex = Integer.parseInt((String) view.getTag()) - 1;
        String project = "org.friendsoftonga.akosipela.";  // how to call this with code? It seemed to produce variable results
        String country = Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).country;
        String activityClass = project + country;

        int challengeLevel = Integer.parseInt(Start.gameList.get((pageNumber * doorsPerPage) + doorIndex).level);
        int gameNumber = (pageNumber * doorsPerPage) + doorIndex + 1;
        String syllableGame = gameList.get((pageNumber * doorsPerPage) + doorIndex).mode;
        int stage;
        if (gameList.get((pageNumber * doorsPerPage) + doorIndex).stage.equals("-")) {
            stage = 1;
        } else {
            stage = Integer.parseInt(gameList.get((pageNumber * doorsPerPage) + doorIndex).stage);
        }

        Intent intent = getIntent();    // preserve Extras
        try {
            intent.setClass(context, Class.forName(activityClass));    // so we retain the Extras
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        intent.putExtra("challengeLevel", challengeLevel);
        intent.putExtra("globalPoints", globalPoints);
        intent.putExtra("gameNumber", gameNumber);
        intent.putExtra("pageNumber", pageNumber);
        intent.putExtra("country", country);
        intent.putExtra("syllableGame", syllableGame);
        intent.putExtra("stage", stage);
        intent.putExtra("studentGrade", grade);
        startActivity(intent);
        finish();

    }

    public void goBackward(View view) {

        if (pageNumber > 0) {
            pageNumber--;
        }
        updateDoors();

    }

    public void goForward(View view) {

        if (((pageNumber + 1) * doorsPerPage) < Start.gameList.size()) {
            pageNumber++;
        }
        updateDoors();

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceLTRIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    public void playAudioInstructionsEarth(View view) {
        setAllElemsUnclickable();
        int resID = context.getResources().getIdentifier("zzz_earth", "raw", context.getPackageName());
        MediaPlayer mp3 = MediaPlayer.create(this, resID);
        mp3.start();
        mp3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp3) {
                setAllElemsClickable();
                mp3.release();
            }
        });

    }

    protected void setAllElemsUnclickable() {
        // Get reference to the parent layout container
        ConstraintLayout parentLayout = findViewById(R.id.earthCL);

        // Disable clickability of all child views
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            child.setClickable(false);
        }
    }

    protected void setAllElemsClickable() {
        // Get reference to the parent layout container
        ConstraintLayout parentLayout = findViewById(R.id.earthCL);

        // Disable clickability of all child views
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            child.setClickable(true);
        }
    }

    protected void updateScore(int pointsIncrease) {
        if (pointsIncrease > 0) {
            try {
                SharedPreferences prefs = getSharedPreferences(ChoosePlayer.SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                String playerGlobalPointsKey = "storedPoints_player" + playerString;

                int oldGlobalPoints = prefs.getInt(playerGlobalPointsKey, 0);
                int newTotalGlobalPoints = oldGlobalPoints + pointsIncrease;

                editor.putInt(playerGlobalPointsKey, newTotalGlobalPoints);
                editor.apply();

                globalPoints = newTotalGlobalPoints;
                getIntent().putExtra("globalPoints", globalPoints);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}