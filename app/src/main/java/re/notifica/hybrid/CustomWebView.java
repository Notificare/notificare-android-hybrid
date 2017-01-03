package re.notifica.hybrid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;

import re.notifica.ui.UserPreferencesActivity;

/**
 * Created by joel on 03/01/2017.
 */

public class CustomWebView extends WebViewClient {

    private final WeakReference<Activity> mActivityRef;
    protected static final String TAG = CustomWebView.class.getSimpleName();
    protected  ProgressDialog dialog;
    protected  Boolean isLoading;
    protected Config config;

    public CustomWebView(Activity activity) {
        mActivityRef = new WeakReference<Activity>(activity);
        config = new Config(mActivityRef.get());
    }


    @Override
    public void onPageFinished(WebView view, final String url) {
        isLoading = false;
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        isLoading = true;
        dialog = ProgressDialog.show(mActivityRef.get(), "", mActivityRef.get().getString(R.string.loader), true);
        final Uri uri = Uri.parse(url);
        return handleUri(view, uri);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        isLoading = true;
        dialog = ProgressDialog.show(mActivityRef.get(), "", mActivityRef.get().getString(R.string.loader), true);
        final Uri uri = request.getUrl();
        return handleUri(view, uri);
    }

    private boolean handleUri(WebView view , final Uri uri) {

        final Uri configHost = Uri.parse(config.getProperty("url"));

        if (uri.getScheme().startsWith("mailto:")) {
            final Activity activity = mActivityRef.get();
            if (activity != null) {
                MailTo mt = MailTo.parse(uri.getHost());
                Intent i = newEmailIntent(activity.getApplicationContext(), mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
                activity.startActivity(i);
                view.reload();
                return true;
            }
        } else if (uri.getScheme().startsWith(config.getProperty("urlScheme"))) {
            isLoading = false;
            if (dialog != null) {
                dialog.dismiss();
            }
            final Activity activity = mActivityRef.get();
            if (activity != null) {
                if (uri.getPath().equals("/inbox")) {

                    activity.startActivity(new Intent(activity, InboxActivity.class));

                } else if (uri.getPath().equals("/settings")) {

                    activity.startActivity(new Intent(activity, UserPreferencesActivity.class));

                }
                return true;
            }
        } else if (! uri.getHost().equals(configHost.getHost()) && ! isLoading) {

            final Activity activity = mActivityRef.get();
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(intent);
            return true;

        }

        return false;
    }

    private Intent newEmailIntent(Context context, String address, String subject, String body, String cc) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        intent.setType("message/rfc822");
        return intent;
    }

}