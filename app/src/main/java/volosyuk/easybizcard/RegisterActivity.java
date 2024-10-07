package volosyuk.easybizcard;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText email, password, passwordConfirm;
    Button register;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.register_email);
        password = findViewById(R.id.register_password);
        passwordConfirm = findViewById(R.id.register_confirm_password);
        register = findViewById(R.id.register_button);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register(){
        final String email = this.email.getText().toString();
        final String password = this.password.getText().toString();
        final String passwordConfirm = this.passwordConfirm.getText().toString();

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        if(matcher.matches() && password.length() >= 6 && passwordConfirm.equals(password)){
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Аккаунт создан.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(RegisterActivity.this, "Ошибка авторизации.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}