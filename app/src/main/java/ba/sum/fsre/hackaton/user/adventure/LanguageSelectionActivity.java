// LanguageSelectionActivity.java
package ba.sum.fsre.hackaton.user.adventure;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ba.sum.fsre.hackaton.R;

public class LanguageSelectionActivity extends AppCompatActivity {

    private Spinner learningLanguageSpinner;
    private Spinner citySpinner;
    private Button startAdventureButton;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private String nativeLanguageCode;

    private static final Map<String, String> LANGUAGE_MAP = new HashMap<>();
    static {
        LANGUAGE_MAP.put("Engleski", "en");
        LANGUAGE_MAP.put("Španjolski", "es");
        LANGUAGE_MAP.put("Hrvatski", "hr");
        LANGUAGE_MAP.put("Inglés", "en");
        LANGUAGE_MAP.put("Español", "es");
        LANGUAGE_MAP.put("Croata", "hr");
        LANGUAGE_MAP.put("English", "en");
        LANGUAGE_MAP.put("Spanish", "es");
        LANGUAGE_MAP.put("Croatian", "hr");
        // Add other languages as needed
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        // Load the saved native language from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        nativeLanguageCode = prefs.getString("My_Lang", "en");

        learningLanguageSpinner = findViewById(R.id.learningLanguageSpinner);
        citySpinner = findViewById(R.id.citySpinner);
        startAdventureButton = findViewById(R.id.startAdventureButton);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up the spinners
        ArrayAdapter<CharSequence> learningLanguageAdapter = ArrayAdapter.createFromResource(this,
                R.array.learning_language_array, android.R.layout.simple_spinner_item);
        learningLanguageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        learningLanguageSpinner.setAdapter(learningLanguageAdapter);

        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(this,
                R.array.city_array, android.R.layout.simple_spinner_item);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);

        // Disable the button initially
        startAdventureButton.setEnabled(false);

        // Add listeners to the spinners
        learningLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkSelections();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkSelections();
            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkSelections();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkSelections();
            }
        });

        startAdventureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get selected values
                String learningLanguage = learningLanguageSpinner.getSelectedItem().toString();
                String city = citySpinner.getSelectedItem().toString();

                // Map selected learning language to ISO code
                String learningLanguageCode = LANGUAGE_MAP.get(learningLanguage);

                // Get the localized string for "Use current location"
                String useCurrentLocation = getString(R.string.use_current_location);

                if (learningLanguageCode == null || city == null) {
                    Toast.makeText(LanguageSelectionActivity.this, "Please select valid options", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (city.equals(useCurrentLocation)) {
                    if (ContextCompat.checkSelfPermission(LanguageSelectionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(LanguageSelectionActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    } else {
                        getCurrentLocation(nativeLanguageCode, learningLanguageCode);
                    }
                } else {
                    // Start AdventureModeActivity with selected values
                    Intent intent = new Intent(LanguageSelectionActivity.this, AdventureModeActivity.class);
                    intent.putExtra("nativeLanguage", nativeLanguageCode);
                    intent.putExtra("learningLanguage", learningLanguageCode);
                    intent.putExtra("city", city);
                    startActivity(intent);
                }
            }
        });
    }

    private void checkSelections() {
        String selectedLanguage = learningLanguageSpinner.getSelectedItem().toString();
        String selectedCity = citySpinner.getSelectedItem().toString();

        boolean isLanguageSelected = !selectedLanguage.equals(getString(R.string.select_language));
        boolean isCitySelected = !selectedCity.equals(getString(R.string.select_location));

        startAdventureButton.setEnabled(isLanguageSelected && isCitySelected);
    }

    private void getCurrentLocation(String nativeLanguageCode, String learningLanguageCode) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Intent intent = new Intent(LanguageSelectionActivity.this, AdventureModeActivity.class);
                        intent.putExtra("nativeLanguage", nativeLanguageCode);
                        intent.putExtra("learningLanguage", learningLanguageCode);
                        intent.putExtra("city", getString(R.string.use_current_location));
                        intent.putExtra("latitude", latitude);
                        intent.putExtra("longitude", longitude);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LanguageSelectionActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String learningLanguage = learningLanguageSpinner.getSelectedItem().toString();
                String learningLanguageCode = LANGUAGE_MAP.get(learningLanguage);
                getCurrentLocation(nativeLanguageCode, learningLanguageCode);
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}