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
        String placeName = getIntent().getStringExtra("placeName");
        String challengeTitle = getIntent().getStringExtra("challengeTitle");
        String challengeDescription = getIntent().getStringExtra("challengeDescription");
        String learningLanguage = getIntent().getStringExtra("learningLanguage");
        String nativeLanguage = getIntent().getStringExtra("nativeLanguage");
        String category = getIntent().getStringExtra("category");

        Log.d(TAG, "Place Name: " + placeName);
        Log.d(TAG, "Challenge Title: " + challengeTitle);
        Log.d(TAG, "Challenge Description: " + challengeDescription);
        Log.d(TAG, "Learning Language: " + learningLanguage);
        Log.d(TAG, "Native Language: " + nativeLanguage);
        Log.d(TAG, "Category: " + category);

        // Set data to views
        TextView placeNameTextView = findViewById(R.id.placeNameTextView);
        TextView categoryTextView = findViewById(R.id.categoryTextView);
        TextView titleTextView = findViewById(R.id.challengeTitleTextView);
        TextView descriptionTextView = findViewById(R.id.nativeTextView);
        TextView translatedTextView = findViewById(R.id.translatedTextView);

        placeNameTextView.setText(placeName);
        categoryTextView.setText("Kategorija: " + category);
        titleTextView.setText(challengeTitle);
        descriptionTextView.setText(challengeDescription);

        // Translate text
        TranslationUtil.translateText(challengeDescription, learningLanguage, new TranslationUtil.TranslationCallback() {
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