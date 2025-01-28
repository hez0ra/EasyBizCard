package volosyuk.easybizcard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.blongho.country_data.World;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import volosyuk.easybizcard.utils.UserRepository;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    TextView toRegister, resetPassword;
    SignInButton loginGoogle;
    Button loginBtn;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    FirebaseAuth mAuth;
    FirebaseUser user;
    private long lastResetRequestTime = 0; // Время последнего запроса
    private final long cooldownTime = 30_000; // Время ожидания (30 секунд)
    private Handler handler = new Handler(); // Для обновления интерфейса
    private Runnable countdownRunnable;
    private static final int RC_SIGN_IN = 9001;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(FirebaseFirestore.getInstance(), mAuth);

        user = mAuth.getCurrentUser();
        if(user != null){
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
            finish();
        }

        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_button);
        toRegister = findViewById(R.id.login_to_registration);
        resetPassword = findViewById(R.id.login_reset_password);
        loginGoogle = findViewById(R.id.login_google);

        resetPassword.setOnClickListener(v -> showResetPassword());

        oneTapClient = Identity.getSignInClient(this);
        signInRequest = new BeginSignInRequest.Builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(getString(R.string.default_web_client_id))
                                .setFilterByAuthorizedAccounts(false)
                                .build()
                )
                .build();

        findViewById(R.id.login_google).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGoogleSignIn();
            }
        });

        toRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        String email = this.email.getText().toString().trim();
        String password = this.password.getText().toString().trim();

        // Проверка на пустые поля
        if (email.isEmpty()) {
            this.email.setError("Введите email");
            this.email.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            this.password.setError("Введите пароль");
            this.password.requestFocus();
            return;
        }

        // Попытка входа
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Неверный email или пароль", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    public void resetPassword(String email) {
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Введите адрес электронной почты", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Инструкции по восстановлению пароля отправлены", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Ошибка", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Метод для запуска обратного отсчета
    private void startCountdown(TextView countdownText) {
        if (countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable); // Сбрасываем предыдущий таймер
        }

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long remainingTime = cooldownTime - (System.currentTimeMillis() - lastResetRequestTime);
                if (remainingTime > 0) {
                    countdownText.setText("Подождите: " + (remainingTime / 1000) + " секунд");
                    handler.postDelayed(this, 500); // Обновляем каждые 500 мс
                } else {
                    countdownText.setText(""); // Скрываем текст, если таймер истек
                }
            }
        };
        handler.post(countdownRunnable); // Запуск таймера
    }

    private void showResetPassword(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_reset_password, null);
        builder.setView(dialogView);

        EditText email = dialogView.findViewById(R.id.dialog_reset_password_edit_text);
        TextView countdownText = dialogView.findViewById(R.id.dialog_reset_password_timer); // Поле для таймера

        builder.setTitle("Сброс пароля")
                .setPositiveButton("Отправить", (dialog, id) -> {
                    String emailValue = email.getText().toString().trim();
                    long currentTime = System.currentTimeMillis();
                    if (emailValue.isEmpty()) {
                        Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show();
                    } else if (currentTime - lastResetRequestTime < cooldownTime) {
                        long remainingTime = cooldownTime - (currentTime - lastResetRequestTime);
                        Toast.makeText(this, "Попробуйте снова через " + remainingTime / 1000 + " секунд", Toast.LENGTH_SHORT).show();
                    } else {
                        resetPassword(emailValue);
                        lastResetRequestTime = currentTime;
                        startCountdown(countdownText); // Запуск таймера
                        Toast.makeText(this, "Письмо отправлено", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", (dialog, id) -> dialog.dismiss())
                .create()
                .show();

        // Инициализация таймера, если запрос уже отправлен
        long timeSinceLastRequest = System.currentTimeMillis() - lastResetRequestTime;
        if (timeSinceLastRequest < cooldownTime) {
            startCountdown(countdownText);
        }
    }

    private void startGoogleSignIn() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    try {
                        startIntentSenderForResult(result.getPendingIntent().getIntentSender(),
                                RC_SIGN_IN, null, 0, 0, 0);
                    } catch (Exception e) {
                        Log.e("EasyBizCard", "Error starting sign-in intent", e);
                    }
                })
                .addOnFailureListener(this, e -> Log.e("EasyBizCard", "One Tap Sign-In failed", e));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken);
                }
            } catch (ApiException e) {
                Log.e("EasyBizCard", "Google Sign-In failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            userRepository.isUserExistByEmail(user.getEmail()).thenAccept(result -> {
                                if (result == null || !result) { // Если результат null или false
                                    userRepository.createUser().thenAccept(v -> {
                                        Intent intent = new Intent(this, ProfileActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                } else {
                                    Intent intent = new Intent(this, ProfileActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).exceptionally(e -> {
                                Log.e("EasyBizCard", "Error checking user existence", e);
                                return null;
                            });
                        }
                    } else {
                        Log.e("EasyBizCard", "SignInWithCredential failed", task.getException());
                    }
                });
    }


}