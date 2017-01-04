package re.notifica.hybrid;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import re.notifica.support.NotificareSupport;

/**
 * Created by joel on 04/01/2017.
 */

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;

                if (!NotificareSupport.shared().getInternetConnectionManager().isNetworkAvailable()) {
                    finish();
                } else {
                    if (!AppBaseApplication.getOnboardingStatus()) {
                        intent = new Intent(SplashActivity.this, OnboardingActivity.class);
                    } else {
                        intent = new Intent(SplashActivity.this, MainActivity.class);
                    }

                    startActivity(intent);
                    finish();
                }
            }
        }, SPLASH_TIME);
    }

}
