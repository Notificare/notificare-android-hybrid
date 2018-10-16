package re.notifica.demo;


import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import java.net.MalformedURLException;
import java.net.URL;

import re.notifica.Notificare;
import re.notifica.model.NotificareNotification;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    protected Config config;
    public WebView webView;
    private ProgressBar spinner;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String url = getArguments().getString("url");

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        config = new Config(getActivity());

        spinner = rootView.findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        webView = rootView.findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (url != null && !url.isEmpty()) {
            URL mainURL = null;
            try {
                mainURL = new URL(config.getProperty("url"));
                Log.d("URL", mainURL.getProtocol().concat("://").concat(config.getProperty("host").concat(url)));
                webView.loadUrl(mainURL.getProtocol().concat("://").concat(config.getProperty("host").concat(url)));
            } catch (MalformedURLException e) {
                Log.d("URL", config.getProperty("url"));
                webView.loadUrl(config.getProperty("url"));
            }
        } else {
            Log.d("URL", config.getProperty("url"));
            webView.loadUrl(config.getProperty("url"));
        }


        LiveData<Integer> unreadCount = Notificare.shared().getInboxManager().getObservableUnreadCount();
        unreadCount.observe(this, count -> {
            String scriptFile = AppBaseApplication.getCustomJSString();
            String badge = "";
            if (count > 0) {
                badge = Integer.toString(count);
            }
            String js = scriptFile.replace("%@", badge);

            webView.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var script = document.createElement('script');" +
                    "script.type = 'text/javascript';" +
                    "script.innerHTML = " + js + "" +
                    "parent.appendChild(script)" +
                    "})()");

        });

        webView.setWebViewClient(new CustomWebView((MainActivity) getActivity()) {

            @Override
            public void onPageFinished(WebView view, final String url) {
                super.onPageFinished(view, url);
                spinner.setVisibility(View.GONE);
            }
        });
        return rootView;
    }
}
