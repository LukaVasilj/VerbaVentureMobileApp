// LanguageSelectionActivity.java
package ba.sum.fsre.hackaton.user.adventure;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
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
import java.util.Map;

import ba.sum.fsre.hackaton.R;

public class LanguageSelectionActivity extends AppCompatActivity {

    private Spinner nativeLanguageSpinner;
    private Spinner learningLanguageSpinner;
    private Spinner citySpinner;
    private Button startAdventureButton;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final Map<String, String> LANGUAGE_MAP = new HashMap<>();
    static {
        LANGUAGE_MAP.put("Engleski", "en");
        LANGUAGE_MAP.put("Španjolski", "es");
        LANGUAGE_MAP.put("Njemački", "de");
        // Add other languages as needed
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        nativeLanguageSpinner = findViewById(R.id.nativeLanguageSpinner);
        learningLanguageSpinner = findViewById(R.id.learningLanguageSpinner);
        citySpinner = findViewById(R.id.citySpinner);
        startAdventureButton = findViewById(R.id.startAdventureButton);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up the spinners
        ArrayAdapter<CharSequence> nativeLanguageAdapter = ArrayAdapter.createFromResource(this,
                R.array.native_language_array, android.R.layout.simple_spinner_item);
        nativeLanguageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nativeLanguageSpinner.setAdapter(nativeLanguageAdapter);

        ArrayAdapter<CharSequence> learningLanguageAdapter = ArrayAdapter.createFromResource(this,
                R.array.learning_language_array, android.R.layout.simple_spinner_item);
        learningLanguageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        learningLanguageSpinner.setAdapter(learningLanguageAdapter);

        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(this,
                R.array.city_array, android.R.layout.simple_spinner_item);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);

        startAdventureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get selected values
                String nativeLanguage = nativeLanguageSpinner.getSelectedItem().toString();
                String learningLanguage = learningLanguageSpinner.getSelectedItem().toString();
                String city = citySpinner.getSelectedItem().toString();

                // Map selected languages to ISO codes
                String nativeLanguageCode = LANGUAGE_MAP.get(nativeLanguage);
                String learningLanguageCode = LANGUAGE_MAP.get(learningLanguage);

                if (city.equals("Use current location")) {
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

    private void getCurrentLocation(String nativeLanguageCode, String learningLanguageCode) {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Intent intent = new Intent(LanguageSelectionActivity.this, AdventureModeActivity.class);
                    intent.putExtra("nativeLanguage", nativeLanguageCode);
                    intent.putExtra("learningLanguage", learningLanguageCode);
                    intent.putExtra("city", "Use current location");
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    startActivity(intent);
                } else {
                    Toast.makeText(LanguageSelectionActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String nativeLanguage = nativeLanguageSpinner.getSelectedItem().toString();
                String learningLanguage = learningLanguageSpinner.getSelectedItem().toString();
                String nativeLanguageCode = LANGUAGE_MAP.get(nativeLanguage);
                String learningLanguageCode = LANGUAGE_MAP.get(learningLanguage);
                getCurrentLocation(nativeLanguageCode, learningLanguageCode);
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}