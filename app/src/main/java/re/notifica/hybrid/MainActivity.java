package re.notifica.hybrid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView =  (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl("https://code.pt");
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.scrollTo(0.0, 100.0);");
            }
        });
    }
}
