package in.kaverisweets.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

    private WebView webView;
    private FrameLayout openingScreen;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /*
         * IMPORTANT:
         * Keep normal Android system bars.
         *
         * This is the configuration that was
         * already working correctly.
         */
        getWindow()
                .getDecorView()
                .setSystemUiVisibility(0);

        setContentView(R.layout.activity_main);

        /*
         * Views
         */
        webView = findViewById(R.id.webView);

        openingScreen = findViewById(
                R.id.openingScreen
        );

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
         * Website
         */
        webView.setWebViewClient(
                new WebViewClient()
        );

        webView.loadUrl(
                "https://kaverisweets.in/"
        );

        /*
         * Opening screen
         *
         * Show for 2.5 seconds.
         */
        handler.postDelayed(
                new Runnable() {

                    @Override
                    public void run() {

                        hideOpeningScreen();
                    }

                },
                2500
        );
    }

    /*
     * Hide opening screen
     */
    private void hideOpeningScreen() {

        if (openingScreen == null) {
            return;
        }

        if (openingScreen.getVisibility()
                != View.VISIBLE) {

            return;
        }

        openingScreen.animate()
                .alpha(0f)
                .setDuration(400)
                .withEndAction(
                        new Runnable() {

                            @Override
                            public void run() {

                                openingScreen.setVisibility(
                                        View.GONE
                                );

                                openingScreen.setAlpha(1f);
                            }
                        }
                )
                .start();
    }

    /*
     * Back button
     */
    @Override
    public void onBackPressed() {

        if (
                webView != null &&
                webView.canGoBack()
        ) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }

    /*
     * Cleanup
     */
    @Override
    protected void onDestroy() {

        handler.removeCallbacksAndMessages(null);

        if (webView != null) {

            webView.stopLoading();

            webView.destroy();

            webView = null;
        }

        super.onDestroy();
    }
}
