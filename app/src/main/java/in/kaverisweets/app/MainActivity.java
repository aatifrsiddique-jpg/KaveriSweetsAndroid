package in.kaverisweets.app;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String HOME_URL = "https://kaverisweets.in/";

    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;

    private ValueCallback<Uri[]> fileChooserCallback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);

            webView = findViewById(R.id.webView);
            progressBar = findViewById(R.id.progressBar);
            errorLayout = findViewById(R.id.errorLayout);

            Button retryButton = findViewById(R.id.retryButton);

            retryButton.setOnClickListener(v -> loadWebsite());

            setupWebView();
            setupBackButton();

            loadWebsite();

        } catch (Exception e) {
            showStartupError(e);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {

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

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    Bitmap favicon
            ) {
                super.onPageStarted(view, url, favicon);

                if (errorLayout != null) {
                    errorLayout.setVisibility(View.GONE);
                }

                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url
            ) {
                super.onPageFinished(view, url);

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error
            ) {
                super.onReceivedError(view, request, error);

                if (request.isForMainFrame()) {

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    if (errorLayout != null) {
                        errorLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {
                return handleUrl(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url
            ) {
                return handleUrl(Uri.parse(url));
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams
            ) {

                if (fileChooserCallback != null) {
                    fileChooserCallback.onReceiveValue(null);
                }

                fileChooserCallback = filePathCallback;

                Intent intent = fileChooserParams.createIntent();

                try {

                    startActivityForResult(
                            intent,
                            1001
                    );

                    return true;

                } catch (ActivityNotFoundException e) {

                    fileChooserCallback = null;

                    Toast.makeText(
                            MainActivity.this,
                            "File picker not available",
                            Toast.LENGTH_SHORT
                    ).show();

                    return false;
                }
            }
        });
    }

    private boolean handleUrl(Uri uri) {

        if (uri == null) {
            return false;
        }

        String scheme = uri.getScheme();

        if (scheme == null) {
            return false;
        }

        if (scheme.equalsIgnoreCase("http")
                || scheme.equalsIgnoreCase("https")) {

            String host = uri.getHost();

            if (host != null &&
                    (host.equalsIgnoreCase("kaverisweets.in")
                    || host.toLowerCase().endsWith(".kaverisweets.in"))) {

                return false;
            }

            try {

                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        uri
                );

                startActivity(intent);

            } catch (ActivityNotFoundException ignored) {
            }

            return true;
        }

        if (scheme.equalsIgnoreCase("tel")
                || scheme.equalsIgnoreCase("mailto")
                || scheme.equalsIgnoreCase("whatsapp")
                || scheme.equalsIgnoreCase("upi")
                || scheme.equalsIgnoreCase("intent")) {

            try {

                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        uri
                );

                startActivity(intent);

            } catch (ActivityNotFoundException e) {

                Toast.makeText(
                        this,
                        "Required app is not installed",
                        Toast.LENGTH_SHORT
                ).show();
            }

            return true;
        }

        return false;
    }

    private void loadWebsite() {

        if (webView == null) {
            return;
        }

        if (errorLayout != null) {
            errorLayout.setVisibility(View.GONE);
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        webView.loadUrl(HOME_URL);
    }

    private void setupBackButton() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (webView != null && webView.canGoBack()) {
                            webView.goBack();
                        } else {
                            finish();
                        }
                    }
                }
        );
    }

    private void showStartupError(Exception e) {

        e.printStackTrace();

        try {

            setContentView(R.layout.activity_main);

            LinearLayout error = findViewById(R.id.errorLayout);

            if (error != null) {
                error.setVisibility(View.VISIBLE);
            }

            ProgressBar progress = findViewById(R.id.progressBar);

            if (progress != null) {
                progress.setVisibility(View.GONE);
            }

            Button retry = findViewById(R.id.retryButton);

            if (retry != null) {
                retry.setOnClickListener(v -> recreate());
            }

        } catch (Exception ignored) {

            Toast.makeText(
                    this,
                    "Kaveri Sweets could not start",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == 1001
                && fileChooserCallback != null) {

            Uri[] result =
                    WebChromeClient.FileChooserParams.parseResult(
                            resultCode,
                            data
                    );

            fileChooserCallback.onReceiveValue(result);

            fileChooserCallback = null;
        }
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {

            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();

            webView = null;
        }

        super.onDestroy();
    }
}
