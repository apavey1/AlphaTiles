package org.friendsoftonga.akosipela;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.text.util.LinkifyCompat;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class About extends AppCompatActivity {

    Context context;
    String scriptDirection = Start.langInfoList.find("Script direction (LTR or RTL)");
    String hideSILlogoSetting = Start.settingsList.find("Hide SIL logo");
    String hidePrivacyPolicySetting = Start.settingsList.find("Hide privacy policy");
    Boolean hideSILlogo;
    Boolean hidePrivacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.about);        ActivityLayouts.applyEdgeToEdge(this, R.id.aboutCL);
        ActivityLayouts.setStatusAndNavColors(this);

        TextView photoAudioCredits = findViewById(R.id.photoAudioCredits);
        photoAudioCredits.setText(Start.langInfoList.find("Audio and image credits"));
        photoAudioCredits.setMovementMethod(new ScrollingMovementMethod());

        TextView email = findViewById(R.id.email);
        String contactEmail = Start.langInfoList.find("Email");
        if (contactEmail == null || contactEmail.equals("none") || contactEmail.isEmpty()) {
            email.setText("");
        } else {
            email.setText(contactEmail);
            LinkifyCompat.addLinks(email, Linkify.EMAIL_ADDRESSES);
            email.setMovementMethod(LinkMovementMethod.getInstance());
        }

        TextView privacyPolicy = findViewById(R.id.privacyPolicy);

        String httpText = Start.langInfoList.find("Privacy Policy");
        String linkText = "<a href=\"" + httpText + "\">" + "Privacy Policy" + "</a>";
        CharSequence styledText;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            styledText = Html.fromHtml(linkText, Html.FROM_HTML_MODE_LEGACY);
        } else {
            styledText = Html.fromHtml(linkText);
        }
        privacyPolicy.setText(styledText);
        privacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        hidePrivacyPolicy = Boolean.parseBoolean(hidePrivacyPolicySetting);
        if (hidePrivacyPolicy) {
            privacyPolicy.setVisibility(View.GONE);
        } else{
            privacyPolicy.setVisibility(View.VISIBLE);
        }

        String verName = BuildConfig.VERSION_NAME;
        TextView verInfo = findViewById(R.id.appVersionInEnglish);
        verInfo.setText(String.format("%s: %s (%s)", "Alpha Tiles", verName, BuildConfig.FLAVOR));

        if (scriptDirection.equals("RTL")) {
            forceRTLIfSupported();
        } else {
            forceLTRIfSupported();
        }

        // SIMPLIFIED LOGIC: Just hide the view if needed, don't move other things
        if (!hideSILlogoSetting.equals("0")) {
            hideSILlogo = Boolean.parseBoolean(hideSILlogoSetting);
            if (hideSILlogo) {
                ImageView SILlogoImage = findViewById(R.id.logoSILImage);
                if(SILlogoImage != null) SILlogoImage.setVisibility(View.GONE);
            }
        }

        // SIMPLIFIED LOGIC: Just hide the view if needed, don't move other things
        int resID = context.getResources().getIdentifier("zzz_about", "raw", context.getPackageName());
        if (resID == 0) {
            ImageView instructionsButton = findViewById(R.id.instructions);
            if(instructionsButton != null) instructionsButton.setVisibility(View.GONE);
        }
    }

    public void goBackToEarth(View view) {
        Intent intent = getIntent();
        intent.setClass(context, Earth.class);
        startActivity(intent);
        finish();
    }

    public void playAudioInstructionsAbout(View view) {
        setAllElemsUnclickable();
        int resID = context.getResources().getIdentifier("zzz_about", "raw", context.getPackageName());
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
        ConstraintLayout parentLayout = findViewById(R.id.aboutCL);

        // Disable clickability of all child views
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            child.setClickable(false);
        }
    }

    protected void setAllElemsClickable() {
        // Get reference to the parent layout container
        ConstraintLayout parentLayout = findViewById(R.id.aboutCL);

        // Disable clickability of all child views
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            child.setClickable(true);
        }
    }

    private void forceRTLIfSupported() {
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    }

    private void forceLTRIfSupported() {
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
    }

}
