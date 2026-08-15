package in.kaverisweets.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
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
         * Android 15/16 edge-to-edge handling.
         *
         * We allow the window to receive system-bar
         * insets, then keep ONLY the website WebView
         * inside the safe area.
         *
         * Opening screen remains full screen.
         */
        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );

        setContentView(R.layout.activity_main);

        /*
         * Find views
         */
        webView = findViewById(R.id.webView);
        openingScreen = findViewById(R.id.openingScreen);

        /*
         * -------------------------------------------------
         * SYSTEM BAR / SAFE AREA FIX
         * -------------------------------------------------
         *
         * The website is moved below the Android status bar
         * and above the Android navigation bar.
         *
         * The opening screen is NOT affected.
         */
        ViewCompat.setOnApplyWindowInsetsListener(
                webView,
                (view, windowInsets) -> {

                    Insets systemBars =
                            windowInsets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    ViewGroup.LayoutParams params =
                            view.getLayoutParams();

                    if (params instanceof ViewGroup.MarginLayoutParams) {

                        ViewGroup.MarginLayoutParams margins =
                                (ViewGroup.MarginLayoutParams) params;

                        margins.topMargin =
                                systemBars.top;

                        margins.bottomMargin =
                                systemBars.bottom;

                        margins.leftMargin = 0;
                        margins.rightMargin = 0;

                        view.setLayoutParams(margins);
                    }

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
