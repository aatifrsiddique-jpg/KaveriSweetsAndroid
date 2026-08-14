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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String HOME_URL =
            "https://kaverisweets.in/";

    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private FrameLayout splashLayout;

    private ValueCallback<Uri[]> fileChooserCallback;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private boolean pageFinished = false;
    private boolean openingScreenFinished = false;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*
         * Android Native Splash
         */
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        /*
         * Find Views
         */

        webView = findViewById(R.id.webView);

        progressBar = findViewById(R.id.progressBar);

        errorLayout = findViewById(R.id.errorLayout);

        splashLayout = findViewById(R.id.splashLayout);

        Button retryButton =
                findViewById(R.id.retryButton);


        /*
         * Retry
         */

        retryButton.setOnClickListener(
                v -> loadWebsite()
        );


        /*
         * Android system bars
         *
         * Website gets safe area.
         * Opening screen stays FULL SCREEN.
         */

        ViewCompat.setOnApplyWindowInsetsListener(
                webView,
                (view, windowInsets) -> {

                    Insets systemBars =
                            windowInsets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    view.setPadding(
                            0,
                            systemBars.top,
                            0,
                            systemBars.bottom
                    );

                    return windowInsets;
                }
        );

        ViewCompat.requestApplyInsets(webView);


        /*
         * WebView settings
         */

        WebSettings settings =
                webView.getSettings();

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
         * Cookies
         */

        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        cookieManager.setAcceptThirdPartyCookies(
                webView,
                true
        );


        /*
         * WebView Client
         */

        webView.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public void onPageStarted(
                            WebView view,
                            String url,
                            Bitmap favicon
                    ) {

                        super.onPageStarted(
                                view,
                                url,
                                favicon
                        );

                        pageFinished = false;

                        errorLayout.setVisibility(
                                View.GONE
                        );

                        progressBar.setVisibility(
                                View.VISIBLE
                        );
                    }


                    @Override
                    public void onPageFinished(
                            WebView view,
                            String url
                    ) {

                        super.onPageFinished(
                                view,
                                url
                        );

                        pageFinished = true;

                        progressBar.setVisibility(
                                View.GONE
                        );

                        /*
                         * Website loaded.
                         * Opening screen can now disappear
                         * after minimum display time.
                         */

                        hideOpeningScreen();
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

                            hideOpeningScreenImmediately();
                        }
                    }


                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            WebResourceRequest request
                    ) {

                        return handleUrl(
                                request.getUrl()
                        );
                    }


                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            String url
                    ) {

                        return handleUrl(
                                Uri.parse(url)
                        );
                    }
                }
        );


        /*
         * Chrome Client
         */

        webView.setWebChromeClient(
                new WebChromeClient() {

                    @Override
                    public boolean onShowFileChooser(
                            WebView webView,
                            ValueCallback<Uri[]> filePathCallback,
                            FileChooserParams fileChooserParams
                    ) {

                        if (fileChooserCallback != null) {

                            fileChooserCallback
                                    .onReceiveValue(null);
                        }

                        fileChooserCallback =
                                filePathCallback;

                        Intent intent =
                                fileChooserParams
                                        .createIntent();

                        try {

                            startActivityForResult(
                                    intent,
                                    1001
                            );

                        } catch (
                                ActivityNotFoundException e
                        ) {

                            fileChooserCallback =
                                    null;

                            Toast.makeText(
                                    MainActivity.this,
                                    "File picker not available",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return false;
                        }

                        return true;
                    }
                }
        );


        /*
         * Downloads
         */

        webView.setDownloadListener(
                (url,
                 userAgent,
                 contentDisposition,
                 mimetype,
                 contentLength) -> {

                    Intent intent =
                            new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(url)
                            );

                    try {

                        startActivity(intent);

                    } catch (
                            ActivityNotFoundException ignored
                    ) {

                        Toast.makeText(
                                this,
                                "Unable to open download",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );


        /*
         * Back button
         */

        getOnBackPressedDispatcher()
                .addCallback(
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
         * Start Website
         */

        loadWebsite();


        /*
         * Custom Opening Screen
         *
         * Minimum 1.8 seconds.
         */

        handler.postDelayed(
                () -> {

                    openingScreenFinished = true;

                    hideOpeningScreen();

                },
                1800
        );
    }


    /*
     * URL handling
     */

    private boolean handleUrl(Uri uri) {

        String scheme =
                uri.getScheme();

        if (scheme == null) {
            return false;
        }


        if (
                scheme.equals("http") ||
                scheme.equals("https")
        ) {

            String host =
                    uri.getHost();

            if (
                    host != null &&
                    (
                            host.equals(
                                    "kaverisweets.in"
                            ) ||
                            host.endsWith(
                                    ".kaverisweets.in"
                            )
                    )
            ) {

                return false;
            }


            try {

                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                uri
                        )
                );

            } catch (
                    ActivityNotFoundException ignored
            ) {
            }

            return true;
        }


        if (
                scheme.equals("tel") ||
                scheme.equals("mailto") ||
                scheme.equals("whatsapp") ||
                scheme.equals("upi") ||
                scheme.equals("intent")
        ) {

            try {

                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                uri
                        )
                );

            } catch (
                    ActivityNotFoundException e
            ) {

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
     * Load Website
     */

    private void loadWebsite() {

        errorLayout.setVisibility(
                View.GONE
        );

        progressBar.setVisibility(
                View.VISIBLE
        );

        pageFinished = false;

        webView.loadUrl(
                HOME_URL
        );
    }


    /*
     * Hide Custom Opening Screen
     *
     * Only after:
     *
     * 1.8 sec completed
     * AND
     * website loaded
     */

    private void hideOpeningScreen() {

        if (
                openingScreenFinished &&
                pageFinished
        ) {

            if (
                    splashLayout != null &&
                    splashLayout.getVisibility()
                            == View.VISIBLE
            ) {

                splashLayout.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(
                                () -> {

                                    splashLayout
                                            .setVisibility(
                                                    View.GONE
                                            );
                                }
                        )
                        .start();
            }
        }
    }


    /*
     * Error case
     */

    private void hideOpeningScreenImmediately() {

        if (splashLayout != null) {

            splashLayout.setVisibility(
                    View.GONE
            );
        }
    }


    /*
     * File picker
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

        if (
                requestCode == 1001 &&
                fileChooserCallback != null
        ) {

            Uri[] result =
                    WebChromeClient.FileChooserParams
                            .parseResult(
                                    resultCode,
                                    data
                            );

            fileChooserCallback
                    .onReceiveValue(result);

            fileChooserCallback = null;
        }
    }


    /*
     * Destroy
     */

    @Override
    protected void onDestroy() {

        handler.removeCallbacksAndMessages(null);

        if (webView != null) {

            webView.destroy();
        }

        super.onDestroy();
    }
}
