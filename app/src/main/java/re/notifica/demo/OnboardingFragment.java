package re.notifica.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;


public class OnboardingFragment extends Fragment {

    protected static final String TAG = OnboardingFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate( R.layout.fragment_onboarding, container, false);

        Typeface regularFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato-Regular.ttf");

        String title = getArguments().getString("title");
        String file = getArguments().getString("file");
        String buttonLabel = getArguments().getString("buttonLabel");
        final String buttonAction = getArguments().getString("buttonAction");
        final int pos = getArguments().getInt("pos");

        TextView titleText = (TextView) rootView.findViewById(R.id.assetText);
        titleText.setTypeface(regularFont);
        titleText.setText(title);

        Button button = (Button) rootView.findViewById(R.id.assetButton);
        button.setTypeface(regularFont);
        button.setText(buttonLabel);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((OnboardingActivity) getActivity()).goToFragment(pos + 1);

                if (buttonAction != null && buttonAction.equals("goToApp")) {
                    ((OnboardingActivity) getActivity()).tryRequestLocationPermission();
                }

            }
        });

        new DownloadImageTask((ImageView) rootView.findViewById(R.id.assetImage)).execute(file);

        return rootView;
    }

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
