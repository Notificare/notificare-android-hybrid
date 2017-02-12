package re.notifica.hybrid;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import re.notifica.Notificare;
import re.notifica.model.NotificareNotification;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements Notificare.OnNotificationReceivedListener {

    protected Config config;
    public WebView webView;
    private ProgressBar spinner;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setShowHideAnimationEnabled(false);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        config = new Config(getActivity());

        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);

        webView =  (WebView) rootView.findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl(config.getProperty("url"));
        webView.setWebViewClient(new CustomWebView((MainActivity) getActivity()) {

            @Override
            public void onPageFinished(WebView view, final String url) {
                super.onPageFinished(view, url);
                spinner.setVisibility(View.GONE);
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Notificare.shared().addNotificationReceivedListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Notificare.shared().removeNotificationReceivedListener(this);
    }
    @Override
    public void onNotificationReceived(NotificareNotification notificareNotification) {

        String scriptFile = AppBaseApplication.getCustomJSString();
        String badge = "";
        if (Notificare.shared().getInboxManager().getUnreadCount() > 0) {
            int b = Notificare.shared().getInboxManager().getUnreadCount();
            badge = Integer.toString(b);
        }
        String js = scriptFile.replace("%@", badge);

        webView.loadUrl("javascript:(function() {" +
                "var parent = document.getElementsByTagName('head').item(0);" +
                "var script = document.createElement('script');" +
                "script.type = 'text/javascript';" +
                "script.innerHTML = " + js + "" +
                "parent.appendChild(script)" +
                "})()");
    }
}
