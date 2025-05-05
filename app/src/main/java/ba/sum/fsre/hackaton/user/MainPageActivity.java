package ba.sum.fsre.hackaton.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ba.sum.fsre.hackaton.MainActivity;
import ba.sum.fsre.hackaton.R;
import ba.sum.fsre.hackaton.user.adventure.LanguageSelectionActivity;
import ba.sum.fsre.hackaton.user.home.SelectLanguageActivity;

public class MainPageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView welcomeTextView;
    private TextView pointsTextView;
    private TextView levelTextView;
    private TextView badgesTextView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private String selectedLanguage;
    private MenuItem languageMenuItem;
    private Map<String, Integer> languageFlags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the saved language preference
        loadLocale();

        setContentView(R.layout.activity_main_page);

        mAuth = FirebaseAuth.getInstance();
        welcomeTextView = findViewById(R.id.welcomeTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        levelTextView = findViewById(R.id.levelTextView);
        badgesTextView = findViewById(R.id.badgesTextView);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Fetch and display username
                                    String username = document.getString("username");
                                    if (username != null) {
                                        String welcomeMessage = getString(R.string.welcome_message, username);
                                        welcomeTextView.setText(welcomeMessage);
                                    }

                                    // Fetch and update points and level
                                    Long points = document.getLong("points");
                                    if (points != null) {
                                        updateLevelAndPoints(points);
                                    }
                                }
                            } else {
                                Toast.makeText(MainPageActivity.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    // Handle home action
                    return true;
                } else if (id == R.id.navigation_map) {
                    // Handle map action
                    return true;
                } else if (id == R.id.navigation_settings) {
                    Intent intent = new Intent(MainPageActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.navigation_progress) {
                    // Handle progress action
                    return true;
                } else if (id == R.id.navigation_profile) {
                    // Handle profile action
                    return true;
                } else if (id == R.id.navigation_logout) {
                    mAuth.signOut();
                    Intent intent = new Intent(MainPageActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });

        // Initialize the language flags map
        languageFlags = new HashMap<>();
        languageFlags.put("en", R.drawable.flag_english);
        languageFlags.put("hr", R.drawable.flag_croatian);
        languageFlags.put("es", R.drawable.flag_spanish);

        // Initialize the Home Learning Mode button
        Button homeLearningModeButton = findViewById(R.id.homeLearningModeButton);
        homeLearningModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fetch the currently selected app language
                SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
                String appLanguage = prefs.getString("My_Lang", "hr"); // Default is "hr"

                // Start LanguageSelectionActivity and pass the app language
                Intent intent = new Intent(MainPageActivity.this, SelectLanguageActivity.class);
                intent.putExtra("appLanguage", appLanguage);
                startActivity(intent);
            }
        });

        // Update the Adventure Mode button logic
        Button adventureModeButton = findViewById(R.id.adventureModeButton);
        adventureModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPageActivity.this, LanguageSelectionActivity.class);
                startActivity(intent);
            }
        });
    }




    private void updateLevelAndPoints(long points) {
        final int[] currentLevel = {1}; // Initial level
        final long[] pointsForNextLevel = {100}; // Points required for level 2

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    final Long[] currentPoints = {document.getLong("points")};
                                    if (currentPoints[0] != null) {
                                        currentLevel[0] = document.getLong("level") != null ? document.getLong("level").intValue() : 1;

                                        // Calculate points required for the next level
                                        pointsForNextLevel[0] = currentLevel[0] * 100;

                                        // Check if the user has enough points to level up
                                        if (currentPoints[0] >= pointsForNextLevel[0]) {
                                            currentPoints[0] -= pointsForNextLevel[0]; // Deduct points for leveling up
                                            currentLevel[0]++; // Increment level
                                            pointsForNextLevel[0] = currentLevel[0] * 100; // Update points for the next level
                                        }

                                        // Update Firestore with the new level and points
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("level", currentLevel[0]);
                                        updates.put("points", currentPoints[0]);

                                        db.collection("users").document(user.getUid()).update(updates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            // Update UI
                                                            levelTextView.setText(getString(R.string.level, currentLevel[0]));
                                                            pointsTextView.setText(getString(R.string.points, currentPoints[0], pointsForNextLevel[0]));

                                                            // Update badge based on level
                                                            updateBadgeBasedOnLevel(currentLevel[0]);
                                                        } else {
                                                            Toast.makeText(MainPageActivity.this, "Failed to update level and points.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            } else {
                                Toast.makeText(MainPageActivity.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void updateBadgeBasedOnLevel(int userLevel) {
        ImageView badgeIconImageView = findViewById(R.id.badgeIconImageView);

        // Set the badge icon based on the user's level
        switch (userLevel) {
            case 6:
                badgeIconImageView.setImageResource(R.drawable.badges6_icon);
                break;
            case 5:
                badgeIconImageView.setImageResource(R.drawable.badges5_icon);
                break;
            case 4:
                badgeIconImageView.setImageResource(R.drawable.badges4_icon);
                break;
            case 3:
                badgeIconImageView.setImageResource(R.drawable.badges3_icon);
                break;
            case 2:
                badgeIconImageView.setImageResource(R.drawable.badges2_icon);
                break;
            case 1:
                badgeIconImageView.setImageResource(R.drawable.badges1_icon);
                break;
            default:
                badgeIconImageView.setImageResource(R.drawable.default_icon);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        languageMenuItem = menu.findItem(R.id.action_change_language);
        loadLocale();
        updateLanguageMenuItemIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_change_language) {
            showLanguageSelectionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private int selectedLanguageIndex = -1; // Store the selected index

    private void showLanguageSelectionDialog() {
        final String[] languages = {"English", "Hrvatski", "Español"};
        final String[] languageCodes = {"en", "hr", "es"};
        final int[] flags = {R.drawable.flag_english, R.drawable.flag_croatian, R.drawable.flag_spanish};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_language_selection, null);
        builder.setView(dialogView);

        ListView languageListView = dialogView.findViewById(R.id.languageListView);
        Button confirmButton = dialogView.findViewById(R.id.confirmButton);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, languages) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                Drawable drawable = getResources().getDrawable(flags[position]);
                int iconSize = (int) (textView.getTextSize() * 1.5); // Adjust the size as needed
                drawable.setBounds(0, 0, iconSize, iconSize);
                textView.setCompoundDrawables(drawable, null, null, null);
                textView.setCompoundDrawablePadding(16);
                return view;
            }
        };
        languageListView.setAdapter(adapter);

        // Restore the previously selected language
        if (selectedLanguageIndex != -1) {
            languageListView.setItemChecked(selectedLanguageIndex, true);
        }

        languageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedLanguageIndex = position; // Save the selected index
                selectedLanguage = languageCodes[position];
            }
        });

        final AlertDialog dialog = builder.create(); // Declare and initialize the dialog here

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLanguage != null) {
                    setLocale(selectedLanguage);
                    saveLanguagePreference(selectedLanguage);
                    dialog.dismiss(); // Use the dialog variable here
                    recreate(); // Restart activity to apply language change
                }
            }
        });

        dialog.show();
    }

    private void setLocale(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void saveLanguagePreference(String language) {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("My_Lang", language);
        editor.apply();
    }

    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "en");
        setLocale(language);
        selectedLanguage = language; // Set the selected language
    }

    private void updateLanguageMenuItemIcon() {
        if (languageMenuItem != null && selectedLanguage != null) {
            Integer flagResId = languageFlags.get(selectedLanguage);
            if (flagResId != null) {
                languageMenuItem.setIcon(flagResId);
            }
        }
    }
}