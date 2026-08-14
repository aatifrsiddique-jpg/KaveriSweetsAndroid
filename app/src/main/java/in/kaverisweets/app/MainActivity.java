package in.kaverisweets.app;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

    private FrameLayout rootLayout;
    private ImageView splashImage;

    private final Handler splashHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * ---------------------------------------------------------
         * MAIN APP LAYOUT
         * ---------------------------------------------------------
         *
         * activity_main.xml remains exactly as it is.
         * We simply put a splash ImageView on top of it.
         */

        rootLayout = new FrameLayout(this);

        View mainContent = getLayoutInflater()
                .inflate(R.layout.activity_main, rootLayout, false);

        rootLayout.addView(
                mainContent,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        /*
         * ---------------------------------------------------------
         * SPLASH SCREEN
         * ---------------------------------------------------------
         */

        splashImage = new ImageView(this);

        splashImage.setImageResource(R.drawable.kaveri_splash_1);

        /*
         * The splash image is designed as a mobile portrait image.
         * FIT_XY makes it cover the complete mobile screen.
         */
        splashImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        splashImage.setBackgroundColor(
                getResources().getColor(android.R.color.black)
        );

        rootLayout.addView(
                splashImage,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        setContentView(rootLayout);

        /*
         * Keep splash visible while website starts loading.
         */
        startSplash();

        /*
         * ---------------------------------------------------------
         * FIND MAIN VIEWS
         * ---------------------------------------------------------
         */

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        errorLayout = findViewById(R.id.errorLayout);

        Button retryButton = findViewById(R.id.retryButton);

        retryButton.setOnClickListener(v -> loadWebsite());

        /*
         * ---------------------------------------------------------
         * WEBVIEW SETTINGS
         * ---------------------------------------------------------
         */

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

        /*
         * ---------------------------------------------------------
         * COOKIES
         * ---------------------------------------------------------
         */

        CookieManager.getInstance().setAcceptCookie(true);

        CookieManager.getInstance()
                .setAcceptThirdPartyCookies(webView, true);

        /*
         * ---------------------------------------------------------
         * WEBVIEW CLIENT
         * ---------------------------------------------------------
         */

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
                super.onReceivedError(view, request, error);

                if (request.isForMainFrame()) {

                    progressBar.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
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

        /*
         * ---------------------------------------------------------
         * WEB CHROME CLIENT
         * ---------------------------------------------------------
         */

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

                } catch (ActivityNotFoundException e) {

                    fileChooserCallback = null;

                    Toast.makeText(
                            MainActivity.this,
                            "File picker not available",
                            Toast.LENGTH_SHORT
                    ).show();

                    return false;
                }

                return true;
            }
        });

        /*
         * ---------------------------------------------------------
         * DOWNLOAD LISTENER
         * ---------------------------------------------------------
         */

        webView.setDownloadListener(
                (url,
                 userAgent,
                 contentDisposition,
                 mimetype,
                 contentLength) -> {

                    Intent intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

                    try {

                        startActivity(intent);

                    } catch (ActivityNotFoundException ignored) {

                        Toast.makeText(
                                this,
                                "Unable to open download",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        /*
         * ---------------------------------------------------------
         * ANDROID BACK BUTTON
         * ---------------------------------------------------------
         */

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (webView.canGoBack()) {

                            webView.goBack();

                        } else {

                            finish();
                        }
                    }
                }
        );

        /*
         * ---------------------------------------------------------
         * START WEBSITE
         * ---------------------------------------------------------
         */

        loadWebsite();
    }

    /*
     * =============================================================
     * SPLASH SCREEN
     * =============================================================
     */

    private void startSplash() {

        /*
         * Splash duration:
         * 2500 milliseconds = 2.5 seconds
         */

        splashHandler.postDelayed(() -> {

            if (splashImage != null) {

                /*
                 * Smooth fade-out
                 */

                splashImage.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction(() -> {

                            if (rootLayout != null
                                    && splashImage != null) {

                                rootLayout.removeView(splashImage);
                            }
                        })
                        .start();
            }

        }, 2500);
    }

    /*
     * =============================================================
     * URL HANDLING
     * =============================================================
     */

    private boolean handleUrl(Uri uri) {

        String scheme = uri.getScheme();

        if (scheme == null) {
            return false;
        }

        /*
         * Website URLs
         */

        if (scheme.equals("http")
                || scheme.equals("https")) {

            String host = uri.getHost();

            /*
             * Keep Kaveri Sweets website inside WebView
             */

            if (host != null
                    && (
                    host.equals("kaverisweets.in")
                            || host.endsWith(".kaverisweets.in")
            )) {

                return false;
            }

            /*
             * External websites open in browser
             */

            try {

                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                uri
                        )
                );

            } catch (ActivityNotFoundException ignored) {
            }

            return true;
        }

        /*
         * Phone / WhatsApp / Email / UPI
         */

        if (scheme.equals("tel")
                || scheme.equals("mailto")
                || scheme.equals("whatsapp")
                || scheme.equals("upi")
                || scheme.equals("intent")) {

            try {

                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                uri
                        )
                );

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

    /*
     * =============================================================
     * LOAD WEBSITE
     * =============================================================
     */

    private void loadWebsite() {

        errorLayout.setVisibility(View.GONE);

        progressBar.setVisibility(View.VISIBLE);

        webView.loadUrl(HOME_URL);
    }

    /*
     * =============================================================
     * FILE PICKER RESULT
     * =============================================================
     */

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
                    WebChromeClient.FileChooserParams
                            .parseResult(
                                    resultCode,
                                    data
                            );

            fileChooserCallback.onReceiveValue(result);

            fileChooserCallback = null;
        }
    }

    /*
     * =============================================================
     * ACTIVITY DESTROY
     * =============================================================
     */

    @Override
    protected void onDestroy() {

        splashHandler.removeCallbacksAndMessages(null);

        if (splashImage != null) {
            splashImage.animate().cancel();
        }

        if (webView != null) {
            webView.destroy();
        }

        super.onDestroy();
    }
}
