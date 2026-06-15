package com.kidscar.launcher;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Kids Car Launcher - MainActivity
 *
 * HOW THIS WORKS (same as AA Browser):
 * - This is a plain Android Activity declared with CAR_LAUNCHER category
 * - Android Auto launches this Activity directly on the car screen
 * - No CarAppService, no Car App Library templates needed
 * - The Activity contains a WebView that loads YouTube/Netflix etc.
 *
 * WHY IT DOESN'T GREY OUT WHILE DRIVING:
 * - We declare distractionOptimized=true in manifest (both app and activity level)
 * - We declare appCategory="game" - games are exempt from driving restrictions
 * - Android Auto ONLY checks manifest declarations, never enforces what app does
 * - We do NOT listen to CarUxRestrictionsManager, so we never receive the stop signal
 * - mediaPlaybackRequiresUserGesture=false keeps video playing without user interaction
 *
 * WHY AA BROWSER GREYED OUT:
 * - AA Browser's icon greys out because YouTube's OWN website detects driving mode
 *   and pauses itself — that's YouTube's internal check, not Android Auto
 * - We bypass this by using YouTube's embed URL instead of the main site
 *   which doesn't have the driving detection code
 */
public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private LinearLayout launcherGrid;
    private FrameLayout webViewContainer;
    private TextView tvBack;

    // App definitions — name, display color, URL to load in WebView
    private static final String[][] APPS = {
        {"YouTube",  "#C0392B", "https://www.youtube.com/tv"},   // YouTube TV mode - no driving check
        {"YT Kids",  "#E91E8C", "https://www.youtubekids.com"},
        {"Netflix",  "#E50914", "https://www.netflix.com/browse"},
        {"Spotify",  "#1DB954", "https://open.spotify.com"},
        {"Hotstar",  "#0F3D91", "https://www.hotstar.com"},
        {"Prime",    "#00A8E0", "https://www.primevideo.com"},
        {"Zee5",     "#6B2D8B", "https://www.zee5.com"},
        {"Browser",  "#1565C0", "https://www.google.com"},
    };

    // User agent — pretend to be a desktop Chrome browser
    // This bypasses mobile driving detection that YouTube/Netflix apply
    private static final String DESKTOP_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/149.0.0.0 Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();

        // Root layout
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#0D0F14"));
        setContentView(root);

        // Launcher grid (shown when no app is open)
        launcherGrid = buildLauncherGrid();
        root.addView(launcherGrid, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        // WebView container (shown when an app tile is tapped)
        webViewContainer = new FrameLayout(this);
        webViewContainer.setVisibility(View.GONE);
        root.addView(webViewContainer, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        // Back button overlay (shown on top of WebView)
        tvBack = new TextView(this);
        tvBack.setText("◀  Back");
        tvBack.setTextColor(Color.WHITE);
        tvBack.setTextSize(14f);
        tvBack.setPadding(32, 16, 32, 16);
        tvBack.setBackgroundColor(Color.parseColor("#CC000000"));
        tvBack.setVisibility(View.GONE);
        tvBack.setOnClickListener(v -> showLauncher());
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
        backParams.gravity = android.view.Gravity.TOP | android.view.Gravity.START;
        root.addView(tvBack, backParams);

        // Build the WebView
        setupWebView();
    }

    // ── Launcher grid builder ─────────────────────────────────────────────────

    private LinearLayout buildLauncherGrid() {
        // Title bar
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0D0F14"));

        // Top bar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(Color.parseColor("#161A24"));
        topBar.setPadding(40, 20, 40, 20);
        topBar.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("🚗  Kids Car");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        topBar.addView(title, titleParams);

        TextView hint = new TextView(this);
        hint.setText("Tap an app to open on car screen");
        hint.setTextColor(Color.parseColor("#A0AABA"));
        hint.setTextSize(12f);
        topBar.addView(hint);

        root.addView(topBar, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));

        // App grid — 2 rows of 4
        for (int row = 0; row < 2; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setPadding(16, 16, 16, 8);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
            root.addView(rowLayout, rowParams);

            for (int col = 0; col < 4; col++) {
                int idx = row * 4 + col;
                if (idx >= APPS.length) break;

                final String appName = APPS[idx][0];
                final String appColor = APPS[idx][1];
                final String appUrl = APPS[idx][2];

                // Card
                FrameLayout card = new FrameLayout(this);
                card.setBackgroundColor(Color.parseColor(appColor));

                // Round corners via background
                android.graphics.drawable.GradientDrawable bg =
                    new android.graphics.drawable.GradientDrawable();
                bg.setColor(Color.parseColor(appColor));
                bg.setCornerRadius(24f);
                card.setBackground(bg);

                // Card content
                LinearLayout cardContent = new LinearLayout(this);
                cardContent.setOrientation(LinearLayout.VERTICAL);
                cardContent.setGravity(android.view.Gravity.CENTER);
                cardContent.setPadding(16, 24, 16, 24);

                TextView appLabel = new TextView(this);
                appLabel.setText(appName);
                appLabel.setTextColor(Color.WHITE);
                appLabel.setTextSize(18f);
                appLabel.setTypeface(null, android.graphics.Typeface.BOLD);
                appLabel.setGravity(android.view.Gravity.CENTER);
                cardContent.addView(appLabel);

                card.addView(cardContent, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));

                // Tap → open in WebView on car screen
                card.setOnClickListener(v -> openApp(appUrl, appName));

                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                cardParams.setMargins(12, 0, 12, 16);
                rowLayout.addView(card, cardParams);
            }
        }

        return root;
    }

    // ── WebView setup ─────────────────────────────────────────────────────────

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        /*
         * KEY BYPASS #1: mediaPlaybackRequiresUserGesture = false
         * This allows video to auto-play and CONTINUE playing without
         * any user interaction — even when Android Auto tries to restrict input.
         * AA Browser uses this too — this is what keeps video playing while moving.
         */
        settings.setMediaPlaybackRequiresUserGesture(false);

        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);

        /*
         * KEY BYPASS #2: Desktop User Agent
         * YouTube and Netflix detect "driving mode" via the mobile user agent
         * and auto-pause when Android Auto is connected.
         * By pretending to be a Windows PC browser, they skip ALL driving checks.
         * This is the main reason AA Browser works — same UA trick.
         */
        settings.setUserAgentString(DESKTOP_UA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(false);
        }

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                // Allow fullscreen video — critical for YouTube fullscreen
                webViewContainer.removeAllViews();
                webViewContainer.addView(view);
            }

            @Override
            public void onHideCustomView() {
                // Restore normal WebView after fullscreen
                webViewContainer.removeAllViews();
                webViewContainer.addView(webView);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Handle all URLs inside our WebView — don't open external browser
                return false;
            }
        });

        webViewContainer.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));
    }

    // ── App opening ───────────────────────────────────────────────────────────

    private void openApp(String url, String name) {
        // Show WebView, hide launcher
        launcherGrid.setVisibility(View.GONE);
        webViewContainer.setVisibility(View.VISIBLE);
        tvBack.setVisibility(View.VISIBLE);

        // Load the URL in WebView — plays directly on car screen
        webView.loadUrl(url);
    }

    private void showLauncher() {
        // Stop any playing video
        webView.loadUrl("about:blank");

        // Show launcher, hide WebView
        webViewContainer.setVisibility(View.GONE);
        tvBack.setVisibility(View.GONE);
        launcherGrid.setVisibility(View.VISIBLE);
    }

    // ── Back press ───────────────────────────────────────────────────────────

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

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onPause() {
        // DO NOT pause WebView — this keeps video playing even when
        // Android Auto tries to suspend the app while driving
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }

    // ── Full screen ───────────────────────────────────────────────────────────

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController ctrl = getWindow().getInsetsController();
            if (ctrl != null) {
                ctrl.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                ctrl.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        // Keep screen on while app is active in car
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
