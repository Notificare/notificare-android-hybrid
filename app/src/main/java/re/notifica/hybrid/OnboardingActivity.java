package re.notifica.hybrid;

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
import re.notifica.model.NotificareAsset;

/**
 * Created by joel on 04/01/2017.
 */

public class OnboardingActivity extends FragmentActivity {

    protected static final String TAG = OnboardingActivity.class.getSimpleName();

    public List<NotificareAsset> assets;

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

        // Instantiate a ViewPager and a PagerAdapter.

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

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class OnboardingPagerAdapter extends FragmentStatePagerAdapter {
        public OnboardingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            OnboardingFragment frag = new OnboardingFragment();
            NotificareAsset asset = assets.get(position);
            Bundle bundle = new Bundle();
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

