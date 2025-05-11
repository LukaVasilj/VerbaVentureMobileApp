package ba.sum.fsre.hackaton;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Locale;


import ba.sum.fsre.hackaton.auth.RegisterActivity;
import ba.sum.fsre.hackaton.user.MainPageActivity;

public class MainActivity extends AppCompatActivity {
    private int selectedLanguageIndex = -1; // Initialize with -1 to indicate no selection
    private Button registerButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFirstTime()) {
            showLanguageSelectionDialog();
        } else {
            loadLocale();
        }

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, redirect to MainPageActivity
            Intent intent = new Intent(MainActivity.this, MainPageActivity.class);
            startActivity(intent);
            finish();
        }

        registerButton = findViewById(R.id.registerButton);


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    private boolean isFirstTime() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        return prefs.getBoolean("isFirstTime", true);
    }

    private void showLanguageSelectionDialog() {
        // Language codes
        final String[] languages = {"en", "hr", "es"};
        // Display names for the languages
        final String[] languageNames = {"Engleski", "Hrvatski", "Španjolski"};
        // Flag icons for the languages
        final int[] flags = {R.drawable.flag_english, R.drawable.flag_croatian, R.drawable.flag_spanish};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate custom layout
        View customView = getLayoutInflater().inflate(R.layout.dialog_language_selection_custom, null);
        builder.setView(customView);

        // Find ListView and Confirm Button in custom layout
        ListView languageListView = customView.findViewById(R.id.languageListView);
        Button confirmButton = customView.findViewById(R.id.confirmButton);

        // Temporary variable to store the selected language
        String[] selectedLanguage = {null}; // Remove `final` keyword

        // Set up the ListView with a custom adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item_language, languageNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item_language, parent, false);
                }
                TextView textView = convertView.findViewById(R.id.languageName);
                ImageView imageView = convertView.findViewById(R.id.languageFlag);

                textView.setText(languageNames[position]);
                imageView.setImageResource(flags[position]);

                return convertView;
            }
        };
        languageListView.setAdapter(adapter);
        languageListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Handle item selection
        languageListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedLanguageIndex = position; // Save the selected index
            selectedLanguage[0] = languages[position]; // Access the specific string at the given index
            languageListView.setItemChecked(position, true); // Mark the selected row
        });

        // Handle Confirm Button click
        confirmButton.setOnClickListener(v -> {
            if (selectedLanguage[0] != null) {
                // Apply the selected language
                setLocale(selectedLanguage[0]);
                saveLanguagePreference(selectedLanguage[0]);
                markFirstTimeComplete();
                recreate(); // Restart activity to apply language change
            }
        });

        builder.show();
    }

    private void setLocale(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void saveLanguagePreference(String language) {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("My_Lang", language);
        editor.apply();
    }

    private void markFirstTimeComplete() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isFirstTime", false);
        editor.apply();
    }

    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "en");
        setLocale(language);
    }
}