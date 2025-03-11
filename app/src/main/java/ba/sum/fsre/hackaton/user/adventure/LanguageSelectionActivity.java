// LanguageSelectionActivity.java
package ba.sum.fsre.hackaton.user.adventure;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

import ba.sum.fsre.hackaton.R;

public class LanguageSelectionActivity extends AppCompatActivity {

    private Spinner nativeLanguageSpinner;
    private Spinner learningLanguageSpinner;
    private Spinner citySpinner;
    private Button startAdventureButton;

    private static final Map<String, String> LANGUAGE_MAP = new HashMap<>();
    static {
        LANGUAGE_MAP.put("Engleski", "en");
        LANGUAGE_MAP.put("Španjolski", "es");
        LANGUAGE_MAP.put("Njemački", "de");
        // Dodaj ostale jezike po potrebi
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        nativeLanguageSpinner = findViewById(R.id.nativeLanguageSpinner);
        learningLanguageSpinner = findViewById(R.id.learningLanguageSpinner);
        citySpinner = findViewById(R.id.citySpinner);
        startAdventureButton = findViewById(R.id.startAdventureButton);

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

                // Start AdventureModeActivity with selected values
                Intent intent = new Intent(LanguageSelectionActivity.this, AdventureModeActivity.class);
                intent.putExtra("nativeLanguage", nativeLanguageCode);
                intent.putExtra("learningLanguage", learningLanguageCode);
                intent.putExtra("city", city);
                startActivity(intent);
            }
        });
    }
}