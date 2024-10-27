package volosyuk.easybizcard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddActivity extends AppCompatActivity {

//    ImageButton toProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.sample_1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sample_1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        toProfile = findViewById(R.id.add_to_profile);
//
//        toProfile.setOnClickListener(v ->{
//            Intent intent = new Intent(this, ProfileActivity.class);
//            startActivity(intent);
//            finish();
//        });

    }
}