// ChallengeDetailActivity.java
package ba.sum.fsre.hackaton.user.adventure;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
        loadLocale(); // Add this line
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

        Log.d(TAG, "Setting TextView values");
        placeNameTextView.setText(placeName);
        categoryTextView.setText(getString(R.string.category, category));
        titleTextView.setText(challengeTitle);
        descriptionTextView.setText(challengeDescription);

        // Calculate distance
        float[] results = new float[1];
        Location.distanceBetween(currentLat, currentLng, placeLat, placeLng, results);
        float distance = results[0];
        Log.d(TAG, "Calculated Distance: " + distance + " meters");
        distanceTextView.setText(getString(R.string.distance, distance));

        // Enable or disable the start challenge button based on distance
        if (distance <= 15) {
            startChallengeButton.setEnabled(true);
            Log.d(TAG, "Start Challenge Button Enabled");
        } else {
            startChallengeButton.setEnabled(false);
            Log.d(TAG, "Start Challenge Button Disabled");
        }

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d(TAG, "TextToSpeech initialized successfully");
                    setTTSLanguage(learningLanguage);
                } else {
                    Log.e(TAG, "TextToSpeech initialization failed with status: " + status);
                }
            }
        });

        // Translate text
        TranslationUtil.translateText(challengeDescription, learningLanguage, new TranslationUtil.TranslationCallback() {
            @Override
            public void onTranslationCompleted(String translatedText) {
                if (translatedText != null) {
                    Log.d(TAG, "Translated Text: " + translatedText);
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
                Log.d(TAG, "TTS Button clicked, speaking text: " + textToSpeak);
                textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        // Set the header image based on the category
        ImageView headerImage = findViewById(R.id.headerImage);
        switch (category.toLowerCase()) {
            case "restaurant":
                headerImage.setImageResource(R.drawable.restaurant);
                Log.d(TAG, "Setting header image to restaurant");
                break;
            case "cafe":
                headerImage.setImageResource(R.drawable.caffe);
                Log.d(TAG, "Setting header image to cafe");
                break;
            case "museum":
                headerImage.setImageResource(R.drawable.museum);
                Log.d(TAG, "Setting header image to museum");
                break;
            case "pub":
                headerImage.setImageResource(R.drawable.pub);
                Log.d(TAG, "Setting header image to pub");
                break;
            default:
                headerImage.setImageResource(R.drawable.restaurant); // Default image
                Log.d(TAG, "Setting default header image");
                break;
        }
    }

    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "en");
        setLocale(language);
    }

    private void setLocale(String language) {
        Locale locale;
        switch (language) {
            case "Hrvatski":
                locale = new Locale("hr");
                break;
            case "Español":
                locale = new Locale("es");
                break;
            default:
                locale = new Locale("en");
                break;
        }
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getApplicationContext().getResources().updateConfiguration(config, getApplicationContext().getResources().getDisplayMetrics());
    }

    private void setTTSLanguage(String languageCode) {
        if (languageCode != null) {
            Log.d(TAG, "Setting TTS language: " + languageCode);
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
                default:
                    selectedLanguage = Locale.US;
                    break;
            }
            textToSpeech.setLanguage(selectedLanguage);
            textToSpeech.setSpeechRate(0.75f); // Set slower speech rate if needed
        } else {
            Log.e(TAG, "Language code is null");
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            Log.d(TAG, "Stopping and shutting down TextToSpeech");
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}