package volosyuk.easybizcard;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    TextView toRegister;
    Button loginBtn;
    FirebaseAuth mAuth;
    FirebaseUser user;

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

    //TODO: добавить уведомление о пустом поле

    private void login(){
        String email = this.email.getText().toString();
        String password = this.password.getText().toString();

        if(!email.isEmpty() && !password.isEmpty()){
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Toast toast = Toast.makeText(getApplicationContext(), "Неверный email или пароль", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });
            }
    }
}