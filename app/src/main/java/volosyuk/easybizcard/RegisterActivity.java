package volosyuk.easybizcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import volosyuk.easybizcard.utils.UserRepository;

public class RegisterActivity extends AppCompatActivity {

    EditText email, password, passwordConfirm;
    Button register;
    TextView toLogin;
    private FirebaseAuth mAuth;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(FirebaseFirestore.getInstance(), mAuth);

        email = findViewById(R.id.register_email);
        password = findViewById(R.id.register_password);
        passwordConfirm = findViewById(R.id.register_confirm_password);
        register = findViewById(R.id.register_button);
        toLogin = findViewById(R.id.register_to_login);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        toLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void register() {
        final String emailValue = this.email.getText().toString().trim();
        final String passwordValue = this.password.getText().toString().trim();
        final String passwordConfirmValue = this.passwordConfirm.getText().toString().trim();

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(emailValue);

        if (emailValue.isEmpty()) {
            this.email.setError("Поле Email не может быть пустым");
            this.email.requestFocus();
            return;
        }

        if (!matcher.matches()) {
            this.email.setError("Некорректный формат Email");
            this.email.requestFocus();
            return;
        }

        if (passwordValue.isEmpty()) {
            this.password.setError("Поле Пароль не может быть пустым");
            this.password.requestFocus();
            return;
        }

        if (passwordValue.length() < 6) {
            this.password.setError("Пароль должен содержать не менее 6 символов");
            this.password.requestFocus();
            return;
        }

        if (!passwordConfirmValue.equals(passwordValue)) {
            this.passwordConfirm.setError("Пароли не совпадают");
            this.passwordConfirm.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailValue, passwordConfirmValue).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    userRepository.createUser();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    email.setError("Данный email уже используется");
                    email.requestFocus();
                }
            }
        });
    }

}