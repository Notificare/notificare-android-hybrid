package re.notifica.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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

                mPager = (ViewPager) findViewById(R.id.pager);
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
            if (!Notificare.shared().hasLocationPermissionGranted()) {
                Log.i(TAG, "permission not granted");
                if (Notificare.shared().didRequestLocationPermission()) {
                    if (Notificare.shared().shouldShowRequestPermissionRationale(OnboardingActivity.this)) {
                        // Here we should show a dialog explaining location updates
                        new android.support.v7.app.AlertDialog.Builder(OnboardingActivity.this)
                                .setTitle(R.string.app_name)
                                .setMessage(R.string.alert_location_permission_rationale)

                                .setCancelable(true)
                                .setPositiveButton(R.string.button_location_permission_rationale_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Notificare.shared().requestLocationPermission(OnboardingActivity.this, LOCATION_PERMISSION_REQUEST_CODE);
                                    }
                                })
                                .show();
                    }
                } else {
                    Notificare.shared().requestLocationPermission(OnboardingActivity.this, LOCATION_PERMISSION_REQUEST_CODE);
                }
            } else {
                finishOnBoarding(true);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                finishOnBoarding(Notificare.shared().checkRequestLocationPermissionResult(permissions, grantResults));
                break;
        }
    }


    private void finishOnBoarding(Boolean status){

        AppBaseApplication.setLocationEnabled(status);

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
        public OnboardingPagerAdapter(FragmentManager fm) {
            super(fm);
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

