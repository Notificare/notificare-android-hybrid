package re.notifica.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;

import java.util.List;

import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.model.NotificareApplicationInfo;
import re.notifica.model.NotificareAsset;

/**
 * Created by joel on 04/01/2017.
 */

public class OnboardingActivity extends FragmentActivity implements Notificare.OnNotificareReadyListener {

    protected static final String TAG = OnboardingActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private AlertDialog.Builder builder;
    public List<NotificareAsset> assets;
    private boolean isNotificareReady = false;
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

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
                mPagerAdapter = new OnboardingPagerAdapter(getSupportFragmentManager());
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
                            .setNegativeButton(R.string.button_location_permission_rationale_cancel, (dialog, id) -> {
                                Log.i(TAG, "foreground location not agreed");
                            })
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
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                finishOnBoarding(Notificare.shared().checkRequestForegroundLocationPermissionResult(permissions, grantResults));
                break;
        }
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


    private class OnboardingPagerAdapter extends FragmentStatePagerAdapter {
        OnboardingPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
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
        public int getCount() {
            return assets.size();
        }
    }
}

