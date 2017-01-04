package re.notifica.hybrid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;


public class OnboardingFragment extends Fragment {

    protected static final String TAG = OnboardingFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate( R.layout.fragment_onboarding, container, false);
        String title = getArguments().getString("title");
        String file = getArguments().getString("file");
        Uri myFile = Uri.parse(file);
        //TextView titleText = (TextView) rootView.findViewById(R.id.assetText);
        new DownloadImageTask((ImageView) rootView.findViewById(R.id.assetImage))
                .execute(file);

        //ImageView imageView = (ImageView) rootView.findViewById(R.id.assetImage);
        //titleText.setText(title);
        //imageView.setImageURI(myFile);
        return rootView;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
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
