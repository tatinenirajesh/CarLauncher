package com.kidscar.launcher;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private LinearLayout launcher;
    private LinearLayout webViewLayout;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Outermost container
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);

        // ── LAUNCHER ──────────────────────────────────────────────────────────
        launcher = new LinearLayout(this);
        launcher.setOrientation(LinearLayout.VERTICAL);
        launcher.setBackgroundColor(Color.parseColor("#0D0F14"));

        TextView title = new TextView(this);
        title.setText("Kids Car Launcher");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24f);
        title.setPadding(40, 40, 40, 20);
        launcher.addView(title);

        // App buttons
        String[][] apps = {
            {"YouTube TV",    "https://www.youtube.com/tv"},
            {"YouTube Kids",  "https://www.youtubekids.com"},
            {"Netflix",       "https://www.netflix.com"},
            {"Spotify",       "https://open.spotify.com"},
            {"Hotstar",       "https://www.hotstar.com"},
            {"Prime Video",   "https://www.primevideo.com"},
            {"Zee5",          "https://www.zee5.com"},
            {"Browser",       "https://www.google.com"},
        };

        for (String[] app : apps) {
            String name = app[0];
            String url  = app[1];

            Button btn = new Button(this);
            btn.setText(name);
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(18f);
            btn.setBackgroundColor(Color.parseColor("#1A2030"));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 120);
            lp.setMargins(24, 12, 24, 0);
            btn.setOnClickListener(v -> openUrl(url));
            launcher.addView(btn, lp);
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(launcher);
        root.addView(scrollView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        // ── WEBVIEW LAYOUT ────────────────────────────────────────────────────
        webViewLayout = new LinearLayout(this);
        webViewLayout.setOrientation(LinearLayout.VERTICAL);
        webViewLayout.setVisibility(View.GONE);

        Button backBtn = new Button(this);
        backBtn.setText("◀ Back to Launcher");
        backBtn.setTextColor(Color.WHITE);
        backBtn.setBackgroundColor(Color.parseColor("#333333"));
        backBtn.setOnClickListener(v -> showLauncher());
        webViewLayout.addView(backBtn, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 80));

        webView = new WebView(this);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setUserAgentString(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Safari/537.36");
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.setWebViewClient(new WebViewClient());

        webViewLayout.addView(webView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        root.addView(webViewLayout, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
    }

    private void openUrl(String url) {
        launcher.setVisibility(View.GONE);
        webViewLayout.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
    }

    private void showLauncher() {
        webView.loadUrl("about:blank");
        webViewLayout.setVisibility(View.GONE);
        launcher.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (webViewLayout.getVisibility() == View.VISIBLE) {
            if (webView.canGoBack()) webView.goBack();
            else showLauncher();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
