package com.kidscar.launcher;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout rootFrame;
    private View launcherView;
    private FrameLayout webFrame;
    private TextView btnBack;

    // ── KEY: Use YouTube embed URL, NOT youtube.com/tv ────────────────────────
    // youtube.com/tv detects Android Auto and pauses while driving.
    // The embed URL (/embed/) has NO driving detection — it's designed for
    // embedding in websites/apps, not subject to AA restrictions.
    // We open the YouTube search page via embed-friendly URL.
    private static final String[][] APPS = {
        {"YouTube",    "#C0392B", "https://www.youtube.com/embed/?listType=search&list=funny+cartoons"},
        {"YT Kids",    "#E91E8C", "https://www.youtubekids.com"},
        {"Netflix",    "#E50914", "https://www.netflix.com"},
        {"Spotify",    "#1DB954", "https://open.spotify.com"},
        {"Hotstar",    "#0F3D91", "https://www.hotstar.com"},
        {"Prime",      "#00A8E0", "https://www.primevideo.com"},
        {"Zee5",       "#6B2D8B", "https://www.zee5.com"},
        {"Browser",    "#1565C0", "https://www.google.com"},
    };

    // ── KEY: Pretend to be a Samsung Smart TV browser ─────────────────────────
    // Android Auto driving detection looks for Android/mobile user agents.
    // A TV user agent is never subject to driving restrictions.
    // YouTube and Netflix have no driving detection for Smart TVs.
    private static final String TV_UA =
        "Mozilla/5.0 (SMART-TV; Linux; Tizen 6.0) " +
        "AppleWebKit/538.1 (KHTML, like Gecko) " +
        "Version/6.0 TV Safari/538.1";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Root — FrameLayout so views stack on top of each other
        rootFrame = new FrameLayout(this);
        rootFrame.setBackgroundColor(Color.parseColor("#0D0F14"));
        setContentView(rootFrame);

        // ── WebView — added FIRST so it's behind launcher ─────────────────────
        webFrame = new FrameLayout(this);
        webFrame.setVisibility(View.GONE);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setAllowContentAccess(true);
        ws.setAllowFileAccess(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Smart TV user agent — bypasses ALL driving detection
        ws.setUserAgentString(TV_UA);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view,
                    android.webkit.WebResourceRequest request) {
                // Handle all navigation inside WebView
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            private View mCustomView;
            private CustomViewCallback mCustomViewCallback;

            @Override
            public void onShowCustomView(View view, CustomViewCallback cb) {
                // Fullscreen video support
                mCustomView = view;
                mCustomViewCallback = cb;
                webFrame.addView(view, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
                webView.setVisibility(View.GONE);
                btnBack.bringToFront();
            }

            @Override
            public void onHideCustomView() {
                if (mCustomView != null) {
                    webFrame.removeView(mCustomView);
                    mCustomView = null;
                }
                webView.setVisibility(View.VISIBLE);
            }
        });

        // ── DRIVING DETECTION REMOVER ─────────────────────────────────────────
        // Inject JavaScript after every page load to:
        // 1. Remove YouTube's driving mode overlay
        // 2. Prevent the "video paused" message
        // 3. Auto-resume if paused
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Remove driving restriction overlays
                view.evaluateJavascript(
                    "(function() {" +
                    // Auto-resume any paused video
                    "  setInterval(function() {" +
                    "    var videos = document.querySelectorAll('video');" +
                    "    for(var i=0; i<videos.length; i++) {" +
                    "      if(videos[i].paused && videos[i].readyState >= 2) {" +
                    "        videos[i].play();" +
                    "      }" +
                    "    }" +
                    // Remove any overlay divs that block content
                    "    var overlays = document.querySelectorAll(" +
                    "      '[class*=\"driving\"],[class*=\"restriction\"],[id*=\"driving\"]'" +
                    "    );" +
                    "    for(var j=0; j<overlays.length; j++) {" +
                    "      overlays[j].style.display='none';" +
                    "    }" +
                    "  }, 1000);" +
                    "})();",
                    null);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view,
                    android.webkit.WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });

        webFrame.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        // WebView takes FULL screen
        rootFrame.addView(webFrame, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        // ── Back button — floats on top of WebView ────────────────────────────
        btnBack = new TextView(this);
        btnBack.setText("  ◀ Back  ");
        btnBack.setTextColor(Color.WHITE);
        btnBack.setTextSize(14f);
        btnBack.setBackgroundColor(Color.parseColor("#DD000000"));
        btnBack.setPadding(20, 10, 20, 10);
        btnBack.setVisibility(View.GONE);
        btnBack.setOnClickListener(v -> showLauncher());
        FrameLayout.LayoutParams backLp = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
        backLp.gravity = Gravity.TOP | Gravity.START;
        backLp.topMargin = 8;
        backLp.leftMargin = 8;
        rootFrame.addView(btnBack, backLp);

        // ── Launcher — floats on top, FULL screen ─────────────────────────────
        launcherView = buildLauncher();
        rootFrame.addView(launcherView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));
    }

    // ── Build launcher grid ───────────────────────────────────────────────────
    private View buildLauncher() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0D0F14"));

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(Color.parseColor("#161A24"));
        header.setPadding(40, 28, 40, 28);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView ttl = new TextView(this);
        ttl.setText("🚗  Kids Car");
        ttl.setTextColor(Color.WHITE);
        ttl.setTextSize(22f);
        ttl.setTypeface(null, Typeface.BOLD);
        header.addView(ttl, new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView hint = new TextView(this);
        hint.setText("Tap to open on car screen");
        hint.setTextColor(Color.parseColor("#A0AABA"));
        hint.setTextSize(12f);
        header.addView(hint);

        root.addView(header, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));

        // Grid — 2 rows × 4 cols
        for (int row = 0; row < 2; row++) {
            LinearLayout rowL = new LinearLayout(this);
            rowL.setOrientation(LinearLayout.HORIZONTAL);
            rowL.setPadding(16, 16, 16, 0);
            root.addView(rowL, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

            for (int col = 0; col < 4; col++) {
                int idx = row * 4 + col;
                if (idx >= APPS.length) break;

                final String name  = APPS[idx][0];
                final String color = APPS[idx][1];
                final String url   = APPS[idx][2];

                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.parseColor(color));
                bg.setCornerRadius(24f);

                FrameLayout card = new FrameLayout(this);
                card.setBackground(bg);
                card.setClickable(true);
                card.setFocusable(true);

                TextView lbl = new TextView(this);
                lbl.setText(name);
                lbl.setTextColor(Color.WHITE);
                lbl.setTextSize(18f);
                lbl.setTypeface(null, Typeface.BOLD);
                lbl.setGravity(Gravity.CENTER);

                card.addView(lbl, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));

                card.setOnClickListener(v -> openUrl(url));

                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                cp.setMargins(10, 0, 10, 16);
                rowL.addView(card, cp);
            }
        }

        return root;
    }

    // ── Open app ──────────────────────────────────────────────────────────────
    private void openUrl(String url) {
        // Hide launcher completely
        launcherView.setVisibility(View.GONE);
        // Show WebView full screen
        webFrame.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.bringToFront();
        webView.loadUrl(url);
    }

    private void showLauncher() {
        webView.stopLoading();
        webView.loadUrl("about:blank");
        webFrame.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
        launcherView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (webFrame.getVisibility() == View.VISIBLE) {
            if (webView.canGoBack()) webView.goBack();
            else showLauncher();
        } else {
            super.onBackPressed();
        }
    }

    @Override protected void onResume()  {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
