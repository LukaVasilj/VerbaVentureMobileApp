package ba.sum.fsre.hackaton.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import ba.sum.fsre.hackaton.R;
import ba.sum.fsre.hackaton.user.UserRole;
import ba.sum.fsre.hackaton.utils.InputSanitizer;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailField;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private EditText usernameField;
    private CheckBox termsCheckBox;
    private Button registerButton;
    private TextView loginLink;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputLayout usernameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        usernameLayout = findViewById(R.id.usernameLayout);

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirmPassword);
        usernameField = findViewById(R.id.username);
        termsCheckBox = findViewById(R.id.termsCheckBox);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);

        setCheckBoxTextWithLinks();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setCheckBoxTextWithLinks() {
        String text = getString(R.string.terms_and_conditions);
        SpannableString spannableString = new SpannableString(text);

        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com/terms"));
                startActivity(intent);
            }
        };

        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com/privacy"));
                startActivity(intent);
            }
        };

        String termsText = getString(R.string.terms_text); // "Uvjeti korištenja" in Croatian
        String privacyText = getString(R.string.privacy_text); // "Politika privatnosti" in Croatian

        int termsStart = text.indexOf(termsText);
        int termsEnd = termsStart + termsText.length();
        int privacyStart = text.indexOf(privacyText);
        int privacyEnd = privacyStart + privacyText.length();

        if (termsStart != -1) {
            spannableString.setSpan(termsSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (privacyStart != -1) {
            spannableString.setSpan(privacySpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        termsCheckBox.setText(spannableString);
        termsCheckBox.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void registerUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();
        String username = usernameField.getText().toString().trim();

        if (!isValidEmail(email)) {
            emailLayout.setError(getString(R.string.invalid_email));
            return;
        } else {
            emailLayout.setError(null);
        }

        if (!isValidPassword(password)) {
            passwordLayout.setError(getString(R.string.invalid_password));
            return;
        } else {
            passwordLayout.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError(getString(R.string.passwords_do_not_match));
            return;
        } else {
            confirmPasswordLayout.setError(null);
        }

        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError(getString(R.string.please_enter_username));
            return;
        } else {
            usernameLayout.setError(null);
        }

        if (!termsCheckBox.isChecked()) {
            Toast.makeText(this, getString(R.string.agree_terms_conditions), Toast.LENGTH_SHORT).show();
            return;
        }

        String sanitizedEmail = InputSanitizer.sanitize(email);
        String sanitizedPassword = InputSanitizer.sanitize(password);
        String sanitizedUsername = InputSanitizer.sanitize(username);

        mAuth.createUserWithEmailAndPassword(sanitizedEmail, sanitizedPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", sanitizedEmail);
                                userData.put("username", sanitizedUsername);
                                userData.put("role", UserRole.USER.name());
                                db.collection("users").document(user.getUid()).set(userData);

                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(RegisterActivity.this, getString(R.string.verification_email_sent), Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(RegisterActivity.this, getString(R.string.failed_to_send_verification_email), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, getString(R.string.registration_failed, task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        Pattern passwordPattern = Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$");
        return passwordPattern.matcher(password).matches();
    }
}