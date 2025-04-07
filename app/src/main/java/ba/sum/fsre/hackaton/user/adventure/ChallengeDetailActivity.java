package ba.sum.fsre.hackaton.user.adventure;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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
import ba.sum.fsre.hackaton.utils.TranslationUtil;

public class ChallengeDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ChallengeDetailActivity";
    private TextToSpeech textToSpeech;
    private Locale selectedLanguage;
    private GoogleMap googleMap;
    private LatLng currentLocation;
    private LatLng placeLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
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

        currentLocation = new LatLng(currentLat, currentLng);
        placeLocation = new LatLng(placeLat, placeLng);

        // Set data to views
        TextView placeNameTextView = findViewById(R.id.placeNameTextView);
        TextView titleTextView = findViewById(R.id.challengeTitleTextView);
        TextView descriptionTextView = findViewById(R.id.nativeTextView);
        TextView translatedTextView = findViewById(R.id.translatedTextView);
        TextView distanceTextView = findViewById(R.id.distanceTextView);
        ImageButton ttsButton = findViewById(R.id.ttsButton);

        placeNameTextView.setText(placeName);

        // Set challenge title in native language
        String challengeTitleText = getString(R.string.challenge_title, challengeTitle);
        titleTextView.setText(challengeTitleText);

        // Set challenge description in native language
        String challengeDescriptionText = getString(R.string.challenge_description, challengeDescription);
        descriptionTextView.setText(challengeDescriptionText);

        // Translate text
        TranslationUtil.translateText(challengeDescription, learningLanguage, new TranslationUtil.TranslationCallback() {
            @Override
            public void onTranslationCompleted(String translatedText) {
                String decodedText = HtmlCompat.fromHtml(translatedText, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                String translatedChallengeText = getString(R.string.translated_challenge, decodedText);
                translatedTextView.setText(translatedChallengeText);

                // Set up TTS button to read the translated text
                ttsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (textToSpeech != null && !textToSpeech.isSpeaking()) {
                            textToSpeech.speak(decodedText, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
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
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    setTTSLanguage(learningLanguage);
                } else {
                    Log.e(TAG, "Initialization of TextToSpeech failed");
                }
            }
        });

        // Initialize SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
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
                .mode(TravelMode.WALKING) // Set travel mode to WALKING
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