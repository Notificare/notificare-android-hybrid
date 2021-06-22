package re.notifica.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.RemoteException;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.model.NotificareApplicationInfo;
import re.notifica.model.NotificareAsset;
import re.notifica.util.Log;

/**
 * Created by joel on 04/01/2017.
 */

public class OnboardingActivity extends FragmentActivity implements Notificare.OnNotificareReadyListener {

    protected static final String TAG = OnboardingActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public List<NotificareAsset> assets;
    private boolean isNotificareReady = false;
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager2 mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private FragmentStateAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);



        Notificare.shared().addNotificareReadyListener(this);

        Notificare.shared().fetchAssets("ONBOARDING", new NotificareCallback<List<NotificareAsset>>() {
            @Override
            public void onSuccess(List<NotificareAsset> notificareAssets) {

                assets = notificareAssets;

                mPager = findViewById(R.id.pager);
                mPagerAdapter = new OnboardingPagerAdapter(OnboardingActivity.this);
                mPager.setAdapter(mPagerAdapter);
            }

            @Override
            public void onError(NotificareError notificareError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    public void goToFragment(int pos) {
        mPager.setCurrentItem(pos);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Notificare.shared().removeNotificareReadyListener(this);
    }

    @Override
    public void onNotificareReady(NotificareApplicationInfo notificareApplicationInfo) {
        isNotificareReady = true;
        //checkInstallReferrer();
    }

    public void tryRequestLocationPermission() {
        if (isNotificareReady) {
            if (!Notificare.shared().hasForegroundLocationPermissionGranted()) {
                Log.i(TAG, "permission not granted");
                if (Notificare.shared().shouldShowForegroundRequestPermissionRationale(this)) {
                    // Here we should show a dialog explaining location updates
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.alert_location_permission_rationale)

                            .setCancelable(true)
                            .setNegativeButton(R.string.button_location_permission_rationale_cancel, (dialog, id) -> Log.i(TAG, "foreground location not agreed"))
                            .setPositiveButton(R.string.button_location_permission_rationale_ok, (dialog, id) -> Notificare.shared().requestForegroundLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE))
                            .show();
                } else {
                    Notificare.shared().requestForegroundLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE);
                }
            } else {
                finishOnBoarding(true);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            finishOnBoarding(Notificare.shared().checkRequestForegroundLocationPermissionResult(permissions, grantResults));
        }
    }

    @SuppressWarnings("unused")
    private void checkInstallReferrer() {
        InstallReferrerClient referrerClient;

        referrerClient = InstallReferrerClient.newBuilder(this).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                switch (responseCode) {
                    case InstallReferrerClient.InstallReferrerResponse.OK:
                        // Connection established.
                        ReferrerDetails response;
                        try {
                            response = referrerClient.getInstallReferrer();
                            JSONObject data = new JSONObject();
                            data.put("installReferrer", response.getInstallReferrer());
                            data.put("referrerClickTimestampSeconds", response.getReferrerClickTimestampSeconds());
                            data.put("installBeginTimestampSeconds", response.getInstallBeginTimestampSeconds());
                            data.put("googlePlayInstantParam", response.getGooglePlayInstantParam());
                            data.put("installVersion", response.getInstallVersion());
                            Notificare.shared().getEventLogger().logCustomEvent("InstallReferrer", data, new NotificareCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {
                                    Log.i(TAG, "logged install referrer event");
                                    referrerClient.endConnection();
                                }

                                @Override
                                public void onError(NotificareError notificareError) {
                                    Log.w(TAG, "error logging install referrer event: " + notificareError.getMessage());
                                    referrerClient.endConnection();
                                }
                            });
                        } catch (RemoteException e) {
                            Log.w(TAG, "error getting install referrer: " + e.getMessage());
                            referrerClient.endConnection();
                        } catch (JSONException e) {
                            Log.w(TAG, "error logging install referrer: " + e.getMessage());
                            referrerClient.endConnection();
                        }
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        // API not available on the current Play Store app.
                        Log.w(TAG, "error getting install referrer: feature not supported");
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        // Connection couldn't be established.
                        Log.w(TAG, "error getting install referrer: service unavailable");
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    private void finishOnBoarding(Boolean status){

        if (status) {
            Notificare.shared().enableLocationUpdates();

            if (BuildConfig.ENABLE_BEACONS) {
                Notificare.shared().enableBeacons(30000);
            }
        }

        AppBaseApplication.setOnboardingStatus(true);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

    }


    private class OnboardingPagerAdapter extends FragmentStateAdapter {
        public OnboardingPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            OnboardingFragment frag = new OnboardingFragment();
            NotificareAsset asset = assets.get(position);
            Bundle bundle = new Bundle();
            bundle.putInt("pos", position);
            bundle.putString("title", asset.getTitle());
            bundle.putString("description", asset.getDescription());
            bundle.putString("buttonLabel", asset.getButtonLabel());
            bundle.putString("buttonAction", asset.getButtonAction());
            bundle.putString("file", asset.getUrl().toString());
            frag.setArguments(bundle);
            return frag;
        }

        @Override
        public int getItemCount() {
            return assets.size();
        }
    }
}

