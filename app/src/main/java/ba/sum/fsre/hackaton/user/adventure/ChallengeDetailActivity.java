package ba.sum.fsre.hackaton.user.adventure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ba.sum.fsre.hackaton.R;
import ba.sum.fsre.hackaton.user.MainPageActivity;
import ba.sum.fsre.hackaton.utils.TranslationUtil;

public class ChallengeDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ChallengeDetailActivity";
    private static final int SPEECH_REQUEST_CODE = 200;

    private TextToSpeech textToSpeech;
    private Locale selectedLanguage;
    private GoogleMap googleMap;
    private LatLng currentLocation;
    private LatLng placeLocation;

    private TextView translatedTextView;
    private ImageButton speechRecognitionButton;
    private String translatedSentence;
    private Button finishChallengeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_detail);

        // Get data from intent
        String placeName = getIntent().getStringExtra("placeName");
        String challengeTitle = getIntent().getStringExtra("challengeTitle");
        String challengeDescription = getIntent().getStringExtra("challengeDescription");
        long points = getIntent().getLongExtra("points", 0); // Retrieve points
        String learningLanguage = getIntent().getStringExtra("learningLanguage");
        String nativeLanguage = getIntent().getStringExtra("nativeLanguage");
        String category = getIntent().getStringExtra("category");
        double currentLat = getIntent().getDoubleExtra("latitude", 0.0);
        double currentLng = getIntent().getDoubleExtra("longitude", 0.0);
        double placeLat = getIntent().getDoubleExtra("placeLat", 0.0);
        double placeLng = getIntent().getDoubleExtra("placeLng", 0.0);

        currentLocation = new LatLng(currentLat, currentLng);
        placeLocation = new LatLng(placeLat, placeLng);

        // Set data to views
        TextView placeNameTextView = findViewById(R.id.placeNameTextView);
        TextView titleTextView = findViewById(R.id.challengeTitleTextView);
        TextView descriptionTextView = findViewById(R.id.nativeTextView);
        TextView pointsTextView = findViewById(R.id.pointsTextView); // Points TextView
        translatedTextView = findViewById(R.id.translatedTextView);
        TextView distanceTextView = findViewById(R.id.distanceTextView);
        ImageButton ttsButton = findViewById(R.id.ttsButton);
        speechRecognitionButton = findViewById(R.id.speechRecognitionButton);
        finishChallengeButton = findViewById(R.id.finishChallengeButton);

        placeNameTextView.setText(placeName);

        // Disable the finish button initially
        finishChallengeButton.setEnabled(false);

        // Set up speech recognition button
        speechRecognitionButton.setOnClickListener(v -> startSpeechRecognition());

        // Set challenge title in native language
        String challengeTitleText = getString(R.string.challenge_title, challengeTitle);
        titleTextView.setText(challengeTitleText);

        // Set challenge description in native language
        String challengeDescriptionText = getString(R.string.challenge_description, challengeDescription);
        descriptionTextView.setText(challengeDescriptionText);
        pointsTextView.setText(getString(R.string.challenge_points, points)); // Display points



        // Translate text
        TranslationUtil.translateText(challengeDescription, learningLanguage, new TranslationUtil.TranslationCallback() {
            @Override
            public void onTranslationCompleted(String translatedText) {
                String decodedText = HtmlCompat.fromHtml(translatedText, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                String translatedChallengeText = getString(R.string.translated_challenge, decodedText);
                translatedTextView.setText(translatedChallengeText);
                translatedSentence = decodedText;

                // Set up TTS button to read the translated text
                ttsButton.setOnClickListener(v -> {
                    if (textToSpeech != null && !textToSpeech.isSpeaking()) {
                        textToSpeech.speak(decodedText, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                });
            }
        });

        // Calculate distance
        float[] results = new float[1];
        Location.distanceBetween(currentLat, currentLng, placeLat, placeLng, results);
        float distance = results[0];
        String distanceText = getString(R.string.distance, distance);
        distanceTextView.setText(distanceText);

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                setTTSLanguage(learningLanguage);
            } else {
                Log.e(TAG, "Initialization of TextToSpeech failed");
            }
        });

        // Initialize SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up finish challenge button
        finishChallengeButton.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Use the points value from the intent
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            long currentPoints = documentSnapshot.getLong("points"); // Get current points
                            long updatedPoints = currentPoints + points; // Add the points from the card

                            // Update the points in Firestore
                            db.collection("users").document(userId)
                                    .update("points", updatedPoints)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Points updated! Total: " + updatedPoints, Toast.LENGTH_SHORT).show();

                                        // Navigate to MainPageActivity
                                        Intent intent = new Intent(ChallengeDetailActivity.this, MainPageActivity.class);
                                        startActivity(intent);
                                        finish(); // Close the current activity
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to update points.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch user points.", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void updateUserPoints(int challengePoints) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch the current user's points
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        long currentPoints = documentSnapshot.getLong("points"); // Get current points
                        long updatedPoints = currentPoints + challengePoints; // Add challenge points

                        // Update the points in Firestore
                        db.collection("users").document(userId)
                                .update("points", updatedPoints)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Points updated! Total: " + updatedPoints, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update points.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user points.", Toast.LENGTH_SHORT).show();
                });
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH); // Adjust language as needed
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please say: " + translatedSentence);

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String recognizedText = results.get(0);

                // Normalize and compare texts
                if (normalizeString(recognizedText).equalsIgnoreCase(normalizeString(translatedSentence))) {
                    Toast.makeText(this, "Correct! You said the sentence correctly.", Toast.LENGTH_SHORT).show();
                    // Enable the "Finish Challenge" button
                    finishChallengeButton.setEnabled(true);
                } else {
                    Toast.makeText(this, "Incorrect. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String normalizeString(String input) {
        return input.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
        googleMap.addMarker(new MarkerOptions().position(placeLocation).title("Place Location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));

        // Fetch and display route
        fetchRoute(currentLocation, placeLocation);
    }

    private void fetchRoute(LatLng origin, LatLng destination) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build();

        DirectionsApi.newRequest(context)
                .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(TravelMode.WALKING)
                .setCallback(new PendingResult.Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {
                        runOnUiThread(() -> {
                            if (result.routes != null && result.routes.length > 0) {
                                DirectionsRoute route = result.routes[0];
                                List<LatLng> path = new ArrayList<>();
                                if (route.overviewPolyline != null) {
                                    List<com.google.maps.model.LatLng> coords = route.overviewPolyline.decodePath();
                                    for (com.google.maps.model.LatLng coord : coords) {
                                        path.add(new LatLng(coord.lat, coord.lng));
                                    }
                                }
                                PolylineOptions polylineOptions = new PolylineOptions().addAll(path).color(getResources().getColor(R.color.colorPrimary)).width(10);
                                googleMap.addPolyline(polylineOptions);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(TAG, "Failed to fetch route", e);
                    }
                });
    }

    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "en");
        setLocale(language);
    }

    private void setLocale(String language) {
        Locale locale;
        switch (language) {
            case "hr":
                locale = new Locale("hr");
                break;
            case "es":
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
            textToSpeech.setSpeechRate(0.75f);
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