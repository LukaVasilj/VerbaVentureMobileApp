// ChallengeDetailActivity.java
package ba.sum.fsre.hackaton.user.adventure;

import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import ba.sum.fsre.hackaton.R;
import ba.sum.fsre.hackaton.utils.TranslationUtil;
import java.util.Locale;

public class ChallengeDetailActivity extends AppCompatActivity {

    private static final String TAG = "ChallengeDetailActivity";
    private TextToSpeech textToSpeech;
    private Locale selectedLanguage;
    private Button startChallengeButton;

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
        double currentLat = getIntent().getDoubleExtra("latitude", 0.0);
        double currentLng = getIntent().getDoubleExtra("longitude", 0.0);
        double placeLat = getIntent().getDoubleExtra("placeLat", 0.0);
        double placeLng = getIntent().getDoubleExtra("placeLng", 0.0);

        Log.d(TAG, "Place Name: " + placeName);
        Log.d(TAG, "Challenge Title: " + challengeTitle);
        Log.d(TAG, "Challenge Description: " + challengeDescription);
        Log.d(TAG, "Learning Language: " + learningLanguage);
        Log.d(TAG, "Native Language: " + nativeLanguage);
        Log.d(TAG, "Category: " + category);
        Log.d(TAG, "Current Location: " + currentLat + ", " + currentLng);
        Log.d(TAG, "Place Location: " + placeLat + ", " + placeLng);

        // Set data to views
        TextView placeNameTextView = findViewById(R.id.placeNameTextView);
        TextView categoryTextView = findViewById(R.id.categoryTextView);
        TextView titleTextView = findViewById(R.id.challengeTitleTextView);
        TextView descriptionTextView = findViewById(R.id.nativeTextView);
        TextView translatedTextView = findViewById(R.id.translatedTextView);
        TextView distanceTextView = findViewById(R.id.distanceTextView);
        Button ttsButton = findViewById(R.id.ttsButton);
        startChallengeButton = findViewById(R.id.startChallengeButton);

        placeNameTextView.setText(placeName);
        categoryTextView.setText("Kategorija: " + category);
        titleTextView.setText(challengeTitle);
        descriptionTextView.setText(challengeDescription);

        // Calculate distance
        float[] results = new float[1];
        Location.distanceBetween(currentLat, currentLng, placeLat, placeLng, results);
        float distance = results[0];
        Log.d(TAG, "Calculated Distance: " + distance + " meters");
        distanceTextView.setText("Distance: " + distance + " meters");

        // Enable or disable the start challenge button based on distance
        if (distance <= 15) {
            startChallengeButton.setEnabled(true);
        } else {
            startChallengeButton.setEnabled(false);
        }

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    setTTSLanguage(learningLanguage);
                }
            }
        });

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

        // Set up TTS button
        ttsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textToSpeak = translatedTextView.getText().toString();
                textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });
    }

    private void setTTSLanguage(String languageCode) {
        switch (languageCode) {
            case "en":
                selectedLanguage = Locale.US;
                break;
            case "es":
                selectedLanguage = new Locale("es", "ES");
                break;
            case "fr":
                selectedLanguage = Locale.FRANCE;
                break;
            case "hr":
                selectedLanguage = new Locale("hr", "HR");
                break;
            // Add more languages as needed
            default:
                selectedLanguage = Locale.US;
                break;
        }
        textToSpeech.setLanguage(selectedLanguage);
        textToSpeech.setSpeechRate(0.75f); // Set slower speech rate if needed
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}