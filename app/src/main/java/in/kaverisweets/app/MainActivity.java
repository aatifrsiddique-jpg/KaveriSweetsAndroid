package in.kaverisweets.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
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
         * EDGE-TO-EDGE
         *
         * Required for modern Android versions.
         * Splash screen can occupy the complete display.
         */
        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= 30) {

            window.setDecorFitsSystemWindows(false);

        } else {

            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }

        /*
         * Transparent system bars.
         *
         * Splash/background can reach the edges.
         */
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= 29) {

            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }

        /*
         * Layout
         */
        setContentView(R.layout.activity_main);

        /*
         * Views
         */
        webView = findViewById(R.id.webView);

        openingScreen = findViewById(
                R.id.openingScreen
        );

        /*
         * =================================================
         * IMPORTANT PART
         * =================================================
         *
         * Splash = FULL SCREEN
         *
         * WebView = SAFE AREA
         *
         * We do NOT put padding on the root layout.
         * We put padding ONLY on WebView.
         *
         * Therefore the splash remains edge-to-edge,
         * while the website stays away from the
         * status/navigation bars.
         */
        if (Build.VERSION.SDK_INT >= 23) {

            webView.setOnApplyWindowInsetsListener(
                    (view, insets) -> {

                        int topInset = 0;
                        int bottomInset = 0;

                        if (Build.VERSION.SDK_INT >= 30) {

                            WindowInsets windowInsets =
                                    insets;

                            android.graphics.Insets bars =
                                    windowInsets.getInsets(
                                            WindowInsets.Type.statusBars()
                                                    | WindowInsets.Type.navigationBars()
                                    );

                            topInset = bars.top;
                            bottomInset = bars.bottom;

                        } else {

                            topInset =
                                    insets.getSystemWindowInsetTop();

                            bottomInset =
                                    insets.getSystemWindowInsetBottom();
                        }

                        /*
                         * Website top:
                         * below status bar
                         *
                         * Website bottom:
                         * above Android navigation bar
                         */
                        view.setPadding(
                                0,
                                topInset,
                                0,
                                bottomInset
                        );

                        return insets;
                    }
            );

            webView.requestApplyInsets();
        }

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
         * 2.5 seconds
         */
        handler.postDelayed(
                this::hideOpeningScreen,
                2500
        );
    }

    /*
     * =================================================
     * HIDE OPENING SCREEN
     * =================================================
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
                .withEndAction(() -> {

                    openingScreen.setVisibility(
                            View.GONE
                    );

                    openingScreen.setAlpha(1f);

                })
                .start();
    }

    /*
     * =================================================
     * BACK BUTTON
     * =================================================
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
     * =================================================
     * CLEANUP
     * =================================================
     */
    @Override
    protected void onDestroy() {

        handler.removeCallbacksAndMessages(null);

        if (webView != null) {

            webView.stopLoading();

            webView.setWebViewClient(null);

            webView.destroy();

            webView = null;
        }

        super.onDestroy();
    }
}
