package in.kaverisweets.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
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
         * Splash screen can use the complete
         * device display, including system-bar areas.
         */
        Window window = getWindow();

        if (android.os.Build.VERSION.SDK_INT >= 30) {

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
         * This allows the splash image/background
         * to reach the complete screen.
         */
        window.setStatusBarColor(
                android.graphics.Color.TRANSPARENT
        );

        window.setNavigationBarColor(
                android.graphics.Color.TRANSPARENT
        );

        if (android.os.Build.VERSION.SDK_INT >= 29) {

            window.setNavigationBarContrastEnforced(false);

            window.setStatusBarContrastEnforced(false);
        }

        /*
         * Keep system icons visible.
         */
        if (android.os.Build.VERSION.SDK_INT >= 30) {

            WindowInsetsController controller =
                    window.getInsetsController();

            if (controller != null) {

                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_DEFAULT
                );
            }

        } else {

            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
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
         * IMPORTANT:
         *
         * Splash stays EDGE-TO-EDGE.
         *
         * Only WebView receives system-bar
         * padding.
         *
         * Therefore:
         *
         * Splash = full screen
         * Website = safe area
         */
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            webView.setOnApplyWindowInsetsListener(
                    (view, insets) -> {

                        int top = 0;
                        int bottom = 0;

                        if (android.os.Build.VERSION.SDK_INT >= 30) {

                            android.graphics.Insets systemBars =
                                    insets.getInsets(
                                            WindowInsets.Type.systemBars()
                                    );

                            top = systemBars.top;
                            bottom = systemBars.bottom;

                        } else {

                            top = insets.getSystemWindowInsetTop();
                            bottom = insets.getSystemWindowInsetBottom();
                        }

                        /*
                         * Website gets safe space.
                         *
                         * Top:
                         * Status bar ke neeche.
                         *
                         * Bottom:
                         * Android navigation buttons ke upar.
                         */
                        view.setPadding(
                                0,
                                top,
                                0,
                                bottom
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
     * Hide opening screen
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

                    openingScreen.setAlpha(1f);

                })
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

            webView.setWebViewClient(null);

            webView.destroy();

            webView = null;
        }

        super.onDestroy();
    }
}
