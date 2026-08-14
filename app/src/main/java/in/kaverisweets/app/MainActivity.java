package in.kaverisweets.app;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String HOME_URL =
            "https://kaverisweets.in/";

    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // ------------------------------------------------
        // FIND VIEWS
        // ------------------------------------------------

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        errorLayout = findViewById(R.id.errorLayout);

        Button retryButton = findViewById(R.id.retryButton);

        retryButton.setOnClickListener(v -> loadWebsite());

        // ------------------------------------------------
        // WEBVIEW SETTINGS
        // ------------------------------------------------

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);

        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        settings.setMediaPlaybackRequiresUserGesture(true);

        // ------------------------------------------------
        // COOKIES
        // ------------------------------------------------

        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        cookieManager.setAcceptThirdPartyCookies(
                webView,
                true
        );

        // ------------------------------------------------
        // WEBVIEW CLIENT
        // ------------------------------------------------

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    Bitmap favicon
            ) {
                super.onPageStarted(view, url, favicon);

                errorLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url
            ) {
                super.onPageFinished(view, url);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error
            ) {
                super.onReceivedError(
                        view,
                        request,
                        error
                );

                if (request.isForMainFrame()) {

                    progressBar.setVisibility(
                            View.GONE
                    );

                    errorLayout.setVisibility(
                            View.VISIBLE
                    );
                }
            }
        });

        // ------------------------------------------------
        // LOAD WEBSITE
        // ------------------------------------------------

        loadWebsite();
    }

    // ------------------------------------------------
    // LOAD WEBSITE
    // ------------------------------------------------

    private void loadWebsite() {

        if (webView == null) {
            return;
        }

        errorLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        webView.loadUrl(HOME_URL);
    }

    // ------------------------------------------------
    // DESTROY
    // ------------------------------------------------

    @Override
    protected void onDestroy() {

        if (webView != null) {

            webView.stopLoading();
            webView.destroy();

            webView = null;
        }

        super.onDestroy();
    }
}
