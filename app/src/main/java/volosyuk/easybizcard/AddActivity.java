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

    ImageButton toProfile;
    Button sample1, sample2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toProfile = findViewById(R.id.add_to_profile);
        sample1 = findViewById(R.id.add_sample_1);
        sample2 = findViewById(R.id.add_sample_2);

        sample1.setOnClickListener(v ->{
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra(EditActivity.EXTRA_LAYOUT, 1);
            startActivity(intent);
            finish();
        });

        toProfile.setOnClickListener(v ->{
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

    }
}