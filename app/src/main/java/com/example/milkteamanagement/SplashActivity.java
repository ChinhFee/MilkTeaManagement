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

        // Đợi 2 giây để logo hiện ở giữa màn hình đen, sau đó chuyển sang Login
        new Handler().postDelayed(() -> {
            View logo = findViewById(R.id.ivSplashLogo);
            
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            
            // Tạo hiệu ứng Shared Element Transition cho logo
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this, logo, "logo_shared_element");
            
            startActivity(intent, options.toBundle());
            
            // Kết thúc SplashActivity sau khi chuyển cảnh để không quay lại được
            new Handler().postDelayed(this::finish, 1000);
        }, 2000);
    }
}
