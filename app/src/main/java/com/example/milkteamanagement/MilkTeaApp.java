package com.example.milkteamanagement;

import android.app.Application;
import com.example.milkteamanagement.repositories.CartManager;
import com.google.android.gms.security.ProviderInstaller;
import android.util.Log;

public class MilkTeaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CartManager.getInstance(this);
        NotificationHelper.createNotificationChannel(this);

        ProviderInstaller.installIfNeededAsync(this, new ProviderInstaller.ProviderInstallListener() {
            @Override
            public void onProviderInstalled() {
                Log.d("MilkTeaApp", "GMS Provider installed successfully");
            }

            @Override
            public void onProviderInstallFailed(int errorCode, android.content.Intent recoveryIntent) {
                Log.e("MilkTeaApp", "GMS Provider installation failed: " + errorCode);
            }
        });
    }
}
