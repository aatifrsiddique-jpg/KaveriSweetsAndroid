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
    private FrameLayout webContainer;
    private FrameLayout openingScreen;

    private final Handler handler =
            new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        // =================================================
        // EDGE TO EDGE
        // =================================================

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

        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= 29) {

            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }


        // =================================================
        // LAYOUT
        // =================================================

        setContentView(R.layout.activity_main);


        // =================================================
        // FIND VIEWS
        // =================================================

        webContainer = findViewById(
                R.id.webContainer
        );

        webView = findViewById(
                R.id.webView
        );

        openingScreen = findViewById(
                R.id.openingScreen
        );


        // =================================================
        // SYSTEM INSETS
        // =================================================
        //
        // IMPORTANT:
        //
        // We are NOT adding padding to WebView.
        //
        // We are changing the actual size/position
        // of webContainer.
        //
        // Therefore a website element using:
        //
        // position: fixed;
        // bottom: 0;
        //
        // will stop ABOVE Android's navigation bar.
        //
        // =================================================

        View rootLayout =
                findViewById(R.id.rootLayout);

        rootLayout.setOnApplyWindowInsetsListener(
                (view, insets) -> {

                    int top = 0;
                    int bottom = 0;

                    if (Build.VERSION.SDK_INT >= 30) {

                        android.graphics.Insets systemBars =
                                insets.getInsets(
                                        WindowInsets.Type.systemBars()
                                );

                        top = systemBars.top;
                        bottom = systemBars.bottom;

                    } else {

                        top =
                                insets.getSystemWindowInsetTop();

                        bottom =
                                insets.getSystemWindowInsetBottom();
                    }


                    // -----------------------------------------
                    // Resize WEBSITE container
                    // -----------------------------------------

                    FrameLayout.LayoutParams params =
                            (FrameLayout.LayoutParams)
                                    webContainer.getLayoutParams();

                    params.leftMargin = 0;
                    params.topMargin = top;
                    params.rightMargin = 0;
                    params.bottomMargin = bottom;

                    webContainer.setLayoutParams(params);


                    return insets;
                }
        );


        rootLayout.requestApplyInsets();


        // =================================================
        // WEBVIEW SETTINGS
        // =================================================

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


        // =================================================
        // COOKIES
        // =================================================

        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        cookieManager.setAcceptThirdPartyCookies(
                webView,
                true
        );


        // =================================================
        // WEBSITE
        // =================================================

        webView.setWebViewClient(
                new WebViewClient()
        );

        webView.loadUrl(
                "https://kaverisweets.in/"
        );


        // =================================================
        // OPENING SCREEN
        // =================================================

        handler.postDelayed(
                this::hideOpeningScreen,
                2500
        );
    }


    // =====================================================
    // HIDE OPENING SCREEN
    // =====================================================

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


    // =====================================================
    // BACK BUTTON
    // =====================================================

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


    // =====================================================
    // CLEANUP
    // =====================================================

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
