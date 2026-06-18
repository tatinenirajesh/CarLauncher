package com.kidscar.launcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
    private View launcherView;
    private FrameLayout webFrame;
    private TextView btnBack;
    private FrameLayout rootFrame;

    private static final String[][] APPS = {
        {"YouTube",   "#C0392B", "https://www.youtube.com/embed/?listType=search&list=cartoons+kids"},
        {"YT Kids",   "#E91E8C", "https://www.youtubekids.com"},
        {"Netflix",   "#E50914", "https://www.netflix.com"},
        {"Spotify",   "#1DB954", "https://open.spotify.com"},
        {"Hotstar",   "#0F3D91", "https://www.hotstar.com"},
        {"Prime",     "#00A8E0", "https://www.primevideo.com"},
        {"Zee5",      "#6B2D8B", "https://www.zee5.com"},
        {"Browser",   "#1565C0", "https://www.google.com"},
    };

    private static final String TV_UA =
        "Mozilla/5.0 (SMART-TV; Linux; Tizen 6.0) " +
        "AppleWebKit/538.1 (KHTML, like Gecko) " +
        "Version/6.0 TV Safari/538.1";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force fullscreen BEFORE setContentView
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        super.onCreate(savedInstanceState);

        // Get real screen dimensions
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        int screenW = dm.widthPixels;
        int screenH = dm.heightPixels;

        // Root covers entire physical screen
        rootFrame = new FrameLayout(this);
        rootFrame.setBackgroundColor(Color.parseColor("#0D0F14"));
        setContentView(rootFrame);

        // Force root to full screen size
        rootFrame.post(() -> {
            ViewGroup.LayoutParams lp = rootFrame.getLayoutParams();
            lp.width  = screenW;
            lp.height = screenH;
            rootFrame.setLayoutParams(lp);
        });

        // WebView — full screen
        webFrame = new FrameLayout(this);
        webFrame.setBackgroundColor(Color.BLACK);
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
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        ws.setUserAgentString(TV_UA);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Auto-resume paused videos every second
                view.evaluateJavascript(
                    "(function(){" +
                    "setInterval(function(){" +
                    "  var v=document.querySelectorAll('video');" +
                    "  for(var i=0;i<v.length;i++){" +
                    "    if(v[i].paused&&v[i].readyState>=2)v[i].play();" +
                    "  }" +
                    "  var o=document.querySelectorAll('[class*=driving],[class*=restrict]');" +
                    "  for(var j=0;j<o.length;j++)o[j].style.display='none';" +
                    "},800);" +
                    "})();", null);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view,
                    android.webkit.WebResourceRequest req) {
                view.loadUrl(req.getUrl().toString());
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            private View customView;
            @Override
            public void onShowCustomView(View view, CustomViewCallback cb) {
                customView = view;
                webFrame.addView(view, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
                webView.setVisibility(View.GONE);
            }
            @Override
            public void onHideCustomView() {
                if (customView != null) {
                    webFrame.removeView(customView);
                    customView = null;
                }
                webView.setVisibility(View.VISIBLE);
            }
        });

        webFrame.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        rootFrame.addView(webFrame, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        // Back button — top-left corner floating
        btnBack = new TextView(this);
        btnBack.setText(" ◀ Back ");
        btnBack.setTextColor(Color.WHITE);
        btnBack.setTextSize(13f);
        btnBack.setBackgroundColor(Color.parseColor("#CC000000"));
        btnBack.setPadding(16, 8, 16, 8);
        btnBack.setVisibility(View.GONE);
        btnBack.setOnClickListener(v -> showLauncher());
        FrameLayout.LayoutParams backLp = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
        backLp.gravity = Gravity.TOP | Gravity.START;
        backLp.topMargin  = 8;
        backLp.leftMargin = 8;
        rootFrame.addView(btnBack, backLp);

        // Launcher — full screen overlay on top
        launcherView = buildLauncher(screenW, screenH);
        rootFrame.addView(launcherView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private View buildLauncher(int screenW, int screenH) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0D0F14"));

        // Header bar
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(Color.parseColor("#161A24"));
        header.setPadding(48, 30, 48, 30);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView ttl = new TextView(this);
        ttl.setText("🚗  Kids Car");
        ttl.setTextColor(Color.WHITE);
        ttl.setTextSize(24f);
        ttl.setTypeface(null, Typeface.BOLD);
        header.addView(ttl, new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView sub = new TextView(this);
        sub.setText("Tap to open on car screen");
        sub.setTextColor(Color.parseColor("#A0AABA"));
        sub.setTextSize(13f);
        header.addView(sub);

        root.addView(header, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));

        // Card grid — 2 rows × 4 cols
        for (int row = 0; row < 2; row++) {
            LinearLayout rowL = new LinearLayout(this);
            rowL.setOrientation(LinearLayout.HORIZONTAL);
            rowL.setPadding(20, 20, 20, 0);

            for (int col = 0; col < 4; col++) {
                int idx = row * 4 + col;
                if (idx >= APPS.length) break;

                final String appName  = APPS[idx][0];
                final String appColor = APPS[idx][1];
                final String appUrl   = APPS[idx][2];

                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.parseColor(appColor));
                bg.setCornerRadius(28f);

                FrameLayout card = new FrameLayout(this);
                card.setBackground(bg);
                card.setClickable(true);
                card.setFocusable(true);

                TextView lbl = new TextView(this);
                lbl.setText(appName);
                lbl.setTextColor(Color.WHITE);
                lbl.setTextSize(20f);
                lbl.setTypeface(null, Typeface.BOLD);
                lbl.setGravity(Gravity.CENTER);

                card.addView(lbl, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));

                card.setOnClickListener(v -> openUrl(appUrl));

                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                cp.setMargins(12, 0, 12, 20);
                rowL.addView(card, cp);
            }

            root.addView(rowL, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        }

        return root;
    }

    private void openUrl(String url) {
        launcherView.setVisibility(View.GONE);
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

    @Override
    protected void onResume() {
        super.onResume();
        // Keep enforcing fullscreen on resume
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
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
