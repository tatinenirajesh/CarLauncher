package com.kidscar.launcher;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout rootLayout;
    private View launcherView;
    private FrameLayout webViewContainer;
    private TextView btnBack;

    private static final String[][] APPS = {
        {"▶  YouTube",  "#C0392B", "https://www.youtube.com/tv"},
        {"🧒  YT Kids", "#E91E8C", "https://www.youtubekids.com"},
        {"🎬  Netflix",  "#E50914", "https://www.netflix.com"},
        {"🎵  Spotify",  "#1DB954", "https://open.spotify.com"},
        {"⭐  Hotstar",  "#0F3D91", "https://www.hotstar.com"},
        {"📦  Prime",    "#00A8E0", "https://www.primevideo.com"},
        {"5️⃣  Zee5",    "#6B2D8B", "https://www.zee5.com"},
        {"🌐  Browser",  "#1565C0", "https://www.google.com"},
    };

    private static final String DESKTOP_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.0.0 Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUI();

        // Root
        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.parseColor("#0D0F14"));
        setContentView(rootLayout);

        // Build launcher
        launcherView = buildLauncher();
        rootLayout.addView(launcherView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // WebView container
        webViewContainer = new FrameLayout(this);
        webViewContainer.setVisibility(View.GONE);
        rootLayout.addView(webViewContainer, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Back button
        btnBack = new TextView(this);
        btnBack.setText("  ◀ Back  ");
        btnBack.setTextColor(Color.WHITE);
        btnBack.setTextSize(16f);
        btnBack.setBackgroundColor(Color.parseColor("#CC000000"));
        btnBack.setPadding(24, 12, 24, 12);
        btnBack.setVisibility(View.GONE);
        btnBack.setOnClickListener(v -> showLauncher());
        FrameLayout.LayoutParams backLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        backLp.gravity = Gravity.TOP | Gravity.START;
        backLp.topMargin = 16;
        backLp.leftMargin = 16;
        rootLayout.addView(btnBack, backLp);

        // Setup WebView
        setupWebView();
    }

    private View buildLauncher() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0D0F14"));

        // Top bar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(Color.parseColor("#161A24"));
        topBar.setPadding(48, 24, 48, 24);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView appTitle = new TextView(this);
        appTitle.setText("🚗  Kids Car Launcher");
        appTitle.setTextColor(Color.WHITE);
        appTitle.setTextSize(22f);
        appTitle.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        topBar.addView(appTitle, titleLp);

        TextView subtitle = new TextView(this);
        subtitle.setText("Tap an app to watch on car screen");
        subtitle.setTextColor(Color.parseColor("#A0AABA"));
        subtitle.setTextSize(13f);
        topBar.addView(subtitle);

        root.addView(topBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Grid rows
        for (int row = 0; row < 2; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setPadding(20, 20, 20, 0);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
            root.addView(rowLayout, rowLp);

            for (int col = 0; col < 4; col++) {
                int idx = row * 4 + col;
                if (idx >= APPS.length) break;

                final String name = APPS[idx][0];
                final String color = APPS[idx][1];
                final String url = APPS[idx][2];

                // Card background
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.parseColor(color));
                bg.setCornerRadius(28f);

                FrameLayout card = new FrameLayout(this);
                card.setBackground(bg);
                card.setClickable(true);
                card.setFocusable(true);

                // Label
                TextView label = new TextView(this);
                label.setText(name);
                label.setTextColor(Color.WHITE);
                label.setTextSize(17f);
                label.setTypeface(null, Typeface.BOLD);
                label.setGravity(Gravity.CENTER);
                label.setPadding(16, 16, 16, 16);

                FrameLayout.LayoutParams labelLp = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
                labelLp.gravity = Gravity.CENTER;
                card.addView(label, labelLp);

                card.setOnClickListener(v -> openUrl(url, name));

                LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                cardLp.setMargins(12, 0, 12, 20);
                rowLayout.addView(card, cardLp);
            }
        }

        return root;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowContentAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setUserAgentString(DESKTOP_UA);
        s.setDatabaseEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view,
                    android.webkit.WebResourceRequest request) {
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            private View fullscreenView;

            @Override
            public void onShowCustomView(View view, CustomViewCallback cb) {
                fullscreenView = view;
                webViewContainer.addView(view, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
                webView.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() {
                if (fullscreenView != null) {
                    webViewContainer.removeView(fullscreenView);
                    fullscreenView = null;
                }
                webView.setVisibility(View.VISIBLE);
            }
        });

        webViewContainer.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void openUrl(String url, String name) {
        launcherView.setVisibility(View.GONE);
        webViewContainer.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
    }

    private void showLauncher() {
        webView.stopLoading();
        webView.loadUrl("about:blank");
        webViewContainer.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
        launcherView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (webViewContainer.getVisibility() == View.VISIBLE) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                showLauncher();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webViewContainer.removeView(webView);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.statusBars() |
                       WindowInsets.Type.navigationBars());
                c.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
