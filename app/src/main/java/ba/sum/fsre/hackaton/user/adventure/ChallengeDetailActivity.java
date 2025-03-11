package ba.sum.fsre.hackaton.user.adventure;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import ba.sum.fsre.hackaton.R;
import ba.sum.fsre.hackaton.utils.TranslationUtil;

public class ChallengeDetailActivity extends AppCompatActivity {

    private static final String TAG = "ChallengeDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_detail);

        // Get data from intent
        String challengeTitle = getIntent().getStringExtra("challengeTitle");
        String challengeDescription = getIntent().getStringExtra("challengeDescription");
        String learningLanguage = getIntent().getStringExtra("learningLanguage");
        String nativeLanguage = getIntent().getStringExtra("nativeLanguage");

        // Set data to views
        TextView titleTextView = findViewById(R.id.challengeTitleTextView);
        TextView descriptionTextView = findViewById(R.id.challengeDescriptionTextView);
        TextView nativeTextView = findViewById(R.id.nativeTextView);
        TextView translatedTextView = findViewById(R.id.translatedTextView);

        titleTextView.setText(challengeTitle);
        descriptionTextView.setText(challengeDescription);

        // Translate text
        String nativeText = "Dobar dan želio bih naručiti espresso i bocu vodu molim vas";
        nativeTextView.setText(nativeText);

        TranslationUtil.translateText(nativeText, learningLanguage, new TranslationUtil.TranslationCallback() {
            @Override
            public void onTranslationCompleted(String translatedText) {
                if (translatedText != null) {
                    Log.d(TAG, "Translated text: " + translatedText);
                    translatedTextView.setText(translatedText);
                } else {
                    Log.e(TAG, "Translation failed");
                }
            }
        });
    }
}