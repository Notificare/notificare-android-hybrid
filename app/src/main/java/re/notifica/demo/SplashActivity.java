package re.notifica.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;
import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.model.NotificareApplicationInfo;
import re.notifica.model.NotificareAsset;
import re.notifica.support.NotificareSupport;
import re.notifica.util.AssetLoader;
import re.notifica.util.Log;

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Notificare.shared().addNotificareReadyListener(this);
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

                for (NotificareAsset asset : notificareAssets) {

                    AssetLoader.loadJSON(asset.getUrl(), new NotificareCallback<JSONObject>() {
                        @Override
                        public void onSuccess(JSONObject jsonObject) {
                            AppBaseApplication.setConfigJSONString(jsonObject.toString());
                        }

                        @Override
                        public void onError(NotificareError notificareError) {
                            Log.w(TAG, notificareError.getMessage());
                        }
                    });
                }

                Notificare.shared().fetchAssets("CUSTOMJS", new NotificareCallback<List<NotificareAsset>>() {
                    @Override
                    public void onSuccess(List<NotificareAsset> notificareAssets) {

                        for (NotificareAsset asset : notificareAssets) {

                            AssetLoader.loadString(asset.getUrl(), new NotificareCallback<String>() {

                                @Override
                                public void onSuccess(String asset) {
                                    AppBaseApplication.setCustomJSString(asset);
                                    fetchPassTemplate();
                                }

                                @Override
                                public void onError(NotificareError notificareError) {
                                    Log.w(TAG, notificareError.getMessage());
                                }
                            });
                        }

                    }

                    @Override
                    public void onError(NotificareError notificareError) {
                        fetchConfig();
                    }
                });

            }

            @Override
            public void onError(NotificareError notificareError) {

                fetchConfig();
            }
        });
    }

    public void fetchPassTemplate(){

        final Config config = new Config(this);
        final JSONObject memberCardTemplate = config.getObject("memberCard");

        Notificare.shared().doCloudRequest("GET", "/api/passbook", null, null, new NotificareCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                try {
                    JSONArray templates = jsonObject.getJSONArray("passbooks");

                    for (int i = 0; i < templates.length(); i++) {
                        JSONObject template = (JSONObject) templates.get(i);
                        if (memberCardTemplate.getString("templateId").equals(template.getString("_id"))) {
                            AppBaseApplication.setMemberCardTemplate(template.toString());
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                continueToApp();
            }

            @Override
            public void onError(NotificareError notificareError) {
                continueToApp();
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
