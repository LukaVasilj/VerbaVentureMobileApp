// MainPageActivity.java
package ba.sum.fsre.hackaton.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Locale;

import ba.sum.fsre.hackaton.MainActivity;
import ba.sum.fsre.hackaton.R;
import ba.sum.fsre.hackaton.user.adventure.LanguageSelectionActivity;

public class MainPageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView welcomeTextView;
    private TextView pointsTextView;
    private TextView levelTextView;
    private TextView badgesTextView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

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
                                    String username = document.getString("username");
                                    if (username != null) {
                                        String welcomeMessage = getString(R.string.welcome_message, username);
                                        welcomeTextView.setText(welcomeMessage);
                                    }
                                }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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

    private void showLanguageSelectionDialog() {
        final String[] languages = {"en", "hr", "es"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_language);
        builder.setSingleChoiceItems(languages, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setLocale(languages[which]);
                saveLanguagePreference(languages[which]);
                dialog.dismiss();
                restartActivity(); // Restart activity to apply language change
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

    private void restartActivity() {
        Intent intent = new Intent(this, MainPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "en");
        setLocale(language);
    }
}