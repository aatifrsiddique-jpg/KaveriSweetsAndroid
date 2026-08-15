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

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends Activity {

    private WebView webView;
    private View openingScreen;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /*
         * Keep Android system bars normal.
         */
        getWindow()
                .getDecorView()
                .setSystemUiVisibility(0);

        setContentView(R.layout.activity_main);

        /*
         * -------------------------------------------------
         * FIND VIEWS
         * -------------------------------------------------
         */

        webView = findViewById(R.id.webView);

        openingScreen = findViewById(R.id.openingScreen);


        /*
         * -------------------------------------------------
         * SYSTEM BAR FIX
         * -------------------------------------------------
         *
         * Website content stays below the Android
         * status bar and above the navigation bar.
         *
         * Opening screen is NOT affected.
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
         * -------------------------------------------------
         * WEBVIEW SETTINGS
         * -------------------------------------------------
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


        /*
         * -------------------------------------------------
         * COOKIES
         * -------------------------------------------------
         */

        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        cookieManager.setAcceptThirdPartyCookies(
                webView,
                true
        );


        /*
         * -------------------------------------------------
         * WEBSITE
         * -------------------------------------------------
         */

        webView.setWebViewClient(
                new WebViewClient()
        );

        webView.loadUrl(
                "https://kaverisweets.in/"
        );


        /*
         * -------------------------------------------------
         * OPENING SCREEN
         * -------------------------------------------------
         *
         * Shows for 2.5 seconds.
         */

        handler.postDelayed(
                () -> hideOpeningScreen(),
                2500
        );
    }


    /*
     * -----------------------------------------------------
     * HIDE OPENING SCREEN
     * -----------------------------------------------------
     */

    private void hideOpeningScreen() {

        if (openingScreen == null) {
            return;
        }

        openingScreen.animate()
                .alpha(0f)
                .setDuration(400)
                .withEndAction(() -> {

                    openingScreen.setVisibility(
                            View.GONE
                    );

                })
                .start();
    }


    /*
     * -----------------------------------------------------
     * BACK BUTTON
     * -----------------------------------------------------
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
     * -----------------------------------------------------
     * CLEANUP
     * -----------------------------------------------------
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
