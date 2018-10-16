package re.notifica.demo;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;

import re.notifica.Notificare;

/**
 * Created by joel on 03/01/2017.
 */

public class CustomWebView extends WebViewClient {

    private final WeakReference<MainActivity> mActivityRef;
    protected static final String TAG = CustomWebView.class.getSimpleName();
    protected  Boolean isLoading;
    protected Config config;

    public CustomWebView(MainActivity activity) {
        mActivityRef = new WeakReference<>(activity);
        config = new Config(mActivityRef.get());
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        isLoading = true;
    }

    @Override
    public void onPageFinished(WebView view, final String url) {
        isLoading = false;
        injectScriptFile(view, AppBaseApplication.getCustomJSString());
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        final Uri uri = Uri.parse(url);
        return handleUri(view, uri);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        final Uri uri = request.getUrl();
        return handleUri(view, uri);
    }

    private boolean handleUri(WebView view , final Uri uri) {

        final Uri configHost = Uri.parse(config.getProperty("url"));

        if (uri.getScheme().startsWith(config.getProperty("urlScheme"))) {

            //Handle recognizable url schemes
            final MainActivity activity = mActivityRef.get();
            if (activity != null) {
                activity.manageFragments(uri.getPath());
                return true;
            }
        } else if (uri.getHost() != null && !uri.getHost().equals(configHost.getHost())) {

            //Handle https urls not for this domain
            final MainActivity activity = mActivityRef.get();
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(intent);
            return true;

        } else if (uri.getScheme() != null && uri.getHost() == null)  {

            //Handle email links
            final MainActivity activity = mActivityRef.get();
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(intent);
            return true;
        }

        //Handle urls in this domain
        return false;
    }

    private void injectScriptFile(WebView view, String scriptFile) {

        String badge = "";
        if (Notificare.shared().getInboxManager().getUnreadCount() > 0) {
            int b = Notificare.shared().getInboxManager().getUnreadCount();
            badge = Integer.toString(b);
        }
        String js = scriptFile.replace("%@", badge);

        view.loadUrl("javascript:(function() {" +
                "var parent = document.getElementsByTagName('head').item(0);" +
                "var script = document.createElement('script');" +
                "script.type = 'text/javascript';" +
                "script.innerHTML = " + js + "" +
                "parent.appendChild(script)" +
                "})()");
    }

}