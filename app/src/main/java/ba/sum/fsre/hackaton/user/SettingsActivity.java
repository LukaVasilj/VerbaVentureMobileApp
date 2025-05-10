package ba.sum.fsre.hackaton.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ba.sum.fsre.hackaton.R;
import ba.sum.fsre.hackaton.auth.LoginActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private TextView usernameTextView;
    private TextView emailTextView;
    private ImageView currentLanguageIcon;
    private Switch notificationsSwitch;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Map<String, Integer> languageFlags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        currentLanguageIcon = findViewById(R.id.currentLanguageIcon);
        notificationsSwitch = findViewById(R.id.notificationsSwitch);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        // Set up the drawer layout and toggle
        drawerLayout = findViewById(R.id.settings_drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize the language flags map
        languageFlags = new HashMap<>();
        languageFlags.put("en", R.drawable.flag_english);
        languageFlags.put("hr", R.drawable.flag_croatian);
        languageFlags.put("es", R.drawable.flag_spanish);

        // Set user information
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            fetchUserInfo(user.getUid());
        }

        // Set current language
        setCurrentLanguage();

        // Change password
        findViewById(R.id.changePasswordButton).setOnClickListener(v -> changePassword());

        // Toggle notifications
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleNotifications(isChecked));

        // Change language
        findViewById(R.id.changeLanguageButton).setOnClickListener(v -> showLanguageSelectionDialog());

        // Contact support
        findViewById(R.id.contactSupportButton).setOnClickListener(v -> contactSupport());

        // Handle navigation drawer menu item clicks
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                // Navigate to MainPageActivity
                Intent intent = new Intent(SettingsActivity.this, MainPageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Close SettingsActivity
                return true;
            }  else if (id == R.id.navigation_logout) {
                // Handle logout
                auth.signOut(); // Sign out the user
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Close SettingsActivity
                return true;
            }
            return false;
        });
    }

    private void fetchUserInfo(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String username = document.getString("username");
                    String email = document.getString("email");
                    usernameTextView.setText("Username: " + username);
                    emailTextView.setText("Email: " + email);
                }
            }
        });
    }

    private void setCurrentLanguage() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "en");

        // Set the icon based on the selected language
        Integer flagResId = languageFlags.get(language);
        if (flagResId != null) {
            currentLanguageIcon.setImageResource(flagResId);
        }
    }

    private void changePassword() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            auth.sendPasswordResetEmail(user.getEmail())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            new AlertDialog.Builder(this)
                                    .setMessage("Password reset email sent.")
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show();
                        } else {
                            new AlertDialog.Builder(this)
                                    .setMessage("Failed to send password reset email.")
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show();
                        }
                    });
        }
    }

    private void toggleNotifications(boolean isEnabled) {
        // Implement notification toggle logic
    }

    private void showLanguageSelectionDialog() {
        final String[] languages = {"English", "Hrvatski", "Español"};
        final String[] languageCodes = {"en", "hr", "es"};
        final int[] flags = {R.drawable.flag_english, R.drawable.flag_croatian, R.drawable.flag_spanish};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_language);
        builder.setSingleChoiceItems(languages, -1, (dialog, which) -> {
            setLocale(languageCodes[which]);
            saveLanguagePreference(languageCodes[which]);
            currentLanguageIcon.setImageResource(flags[which]);
            dialog.dismiss();
            restartMainPageActivity(); // Restart MainPageActivity to apply language change
        });
        builder.show();
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

    private void restartMainPageActivity() {
        Intent intent = new Intent(this, MainPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void contactSupport() {
        // Implement contact support logic
    }
}