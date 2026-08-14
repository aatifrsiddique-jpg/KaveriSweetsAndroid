package in.kaverisweets.app;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String HOME_URL =
            "https://kaverisweets.in/";

    private WebView webView;

    private ValueCallback<Uri[]> fileChooserCallback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Edge-to-edge is enabled.
         * We will manually keep WebView between
         * Android status bar and navigation bar.
         */
        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );

        /*
         * Status bar
         */
        getWindow().setStatusBarColor(
                android.graphics.Color.parseColor("#4B160D")
        );

        /*
         * Navigation bar
         */
        getWindow().setNavigationBarColor(
                android.graphics.Color.parseColor("#4B160D")
        );

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        /*
         * IMPORTANT
         *
         * Instead of adding padding to WebView,
         * we change its actual top/bottom margins.
         *
         * This prevents the website's fixed header/footer
         * from going underneath Android system bars.
         */
        ViewCompat.setOnApplyWindowInsetsListener(
                webView,
                (view, windowInsets) -> {

                    Insets systemBars =
                            windowInsets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    FrameLayout.LayoutParams params =
                            (FrameLayout.LayoutParams)
                                    view.getLayoutParams();

                    params.topMargin = systemBars.top;
                    params.bottomMargin = systemBars.bottom;

                    view.setLayoutParams(params);

                    return windowInsets;
                }
        );

        ViewCompat.requestApplyInsets(webView);

        /*
         * WebView settings
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

                        /*
                         * Only handle main page errors.
                         */
                        if (request.isForMainFrame()) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Unable to load website",
                                    Toast.LENGTH_SHORT
                            ).show();
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
                                fileChooserParams.createIntent();

                        try {

                            startActivityForResult(
                                    intent,
                                    1001
                            );

                        } catch (
                                ActivityNotFoundException e
                        ) {

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
         * Open website
         */
        webView.loadUrl(HOME_URL);
    }

    /*
     * URL handling
     */
    private boolean handleUrl(Uri uri) {

        String scheme = uri.getScheme();

        if (scheme == null) {
            return false;
        }

        /*
         * Keep Kaveri Sweets website inside WebView
         */
        if (
                scheme.equals("http") ||
                scheme.equals("https")
        ) {

            String host = uri.getHost();

            if (
                    host != null &&
                    (
                            host.equals("kaverisweets.in") ||
                            host.endsWith(".kaverisweets.in")
                    )
            ) {

                return false;
            }

            /*
             * External website
             */
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

        /*
         * External applications
         */
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
     * File picker result
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
     * Destroy WebView
     */
    @Override
    protected void onDestroy() {

        if (webView != null) {

            webView.destroy();
        }

        super.onDestroy();
    }
}
