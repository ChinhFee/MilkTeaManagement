package com.example.milkteamanagement;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            View logo = findViewById(R.id.ivSplashLogo);
            
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this, logo, "logo_shared_element");
            
            startActivity(intent, options.toBundle());
            
            new Handler().postDelayed(this::finish, 1000);
        }, 2000);
    }
}
