package re.notifica.hybrid;

import android.content.Intent;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;
import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.model.NotificareApplicationInfo;
import re.notifica.model.NotificareAsset;
import re.notifica.support.NotificareSupport;

/**
 * Created by joel on 04/01/2017.
 */

public class SplashActivity extends AppCompatActivity implements Notificare.OnNotificareReadyListener {
    private static final int SPLASH_TIME = 4000;
    protected static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        Notificare.shared().addNotificareReadyListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Notificare.shared().removeNotificareReadyListener(this);
    }

    @Override
    public void onNotificareReady(NotificareApplicationInfo notificareApplicationInfo) {

        if (AppBaseApplication.getNotificationsEnabled()) {
            int badgeCount = Notificare.shared().getInboxManager().getUnreadCount();
            ShortcutBadger.applyCount(this.getApplicationContext(), badgeCount);
        }

        fetchConfig();
    }

    public void fetchConfig(){

        Notificare.shared().fetchAssets("CONFIG", new NotificareCallback<List<NotificareAsset>>() {
            @Override
            public void onSuccess(List<NotificareAsset> notificareAssets) {

                for(NotificareAsset asset : notificareAssets){

                    Ion.with(getApplicationContext())
                            .load(asset.getUrl().toString()).asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {

                            if (e == null) {
                                AppBaseApplication.setConfigJSONString(result.toString());
                            }
                        }
                    });
                }

                Notificare.shared().fetchAssets("CUSTOMJS", new NotificareCallback<List<NotificareAsset>>() {
                    @Override
                    public void onSuccess(List<NotificareAsset> notificareAssets) {

                        for(NotificareAsset asset : notificareAssets){

                            Ion.with(getApplicationContext())
                                    .load(asset.getUrl().toString())
                                    .asString()
                                    .setCallback(new FutureCallback<String>() {
                                        @Override
                                        public void onCompleted(Exception e, String result) {

                                            if (e == null) {
                                                AppBaseApplication.setCustomJSString(result.toString());
                                                continueToApp();
                                            }
                                        }
                                    });
                        }

                    }

                    @Override
                    public void onError(NotificareError notificareError) {

                    }
                });

            }

            @Override
            public void onError(NotificareError notificareError) {

            }
        });
    }

    public void continueToApp(){
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
}
