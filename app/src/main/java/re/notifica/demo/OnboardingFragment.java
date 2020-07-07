package re.notifica.demo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

import re.notifica.Notificare;
import re.notifica.support.NotificareSupport;
import re.notifica.util.AssetLoader;


public class OnboardingFragment extends Fragment {

    protected static final String TAG = OnboardingFragment.class.getSimpleName();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate( R.layout.fragment_onboarding, container, false);

        String title = getArguments().getString("title");
        String file = getArguments().getString("file");
        String buttonLabel = getArguments().getString("buttonLabel");
        final String buttonAction = getArguments().getString("buttonAction");
        Log.i(TAG, "button action = " + buttonAction);
        final int pos = getArguments().getInt("pos");

        TextView titleText = rootView.findViewById(R.id.assetText);
        titleText.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaRegular"));
        titleText.setText(title);

        Button button = rootView.findViewById(R.id.assetButton);
        button.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaBold"));
        button.setText(buttonLabel);

        button.setOnClickListener(view -> {

            ((OnboardingActivity) getActivity()).goToFragment(pos + 1);

            if (buttonAction != null && buttonAction.equals("goToLocationServices")) {
                Notificare.shared().enableNotifications();
            }
            if (buttonAction != null && buttonAction.equals("goToApp")) {
                ((OnboardingActivity) getActivity()).tryRequestLocationPermission();
            }

        });

        ImageView bmImage = rootView.findViewById(R.id.assetImage);
        AssetLoader.loadImage(file, bmImage);

        return rootView;
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
