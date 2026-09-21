package com.rpsctest.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class TestViewerActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var errorLayout: LinearLayout
    private var currentUrl: String = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_viewer)

        webView = findViewById(R.id.webView)
        errorLayout = findViewById(R.id.errorLayout)
        val btnRetry = findViewById<Button>(R.id.btnRetry)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val indexPath = intent.getStringExtra("INDEX_PATH") ?: ""
        currentUrl = "file://$indexPath"

        setupWebView()

        btnRetry.setOnClickListener {
            errorLayout.visibility = View.GONE
            webView.visibility = View.VISIBLE
            webView.loadUrl(currentUrl)
        }

        btnBack.setOnClickListener {
            finish()
        }

        webView.loadUrl(currentUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    webView.visibility = View.GONE
                    errorLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Exit Test?")
            .setMessage("Are you sure you want to exit the current test?")
            .setPositiveButton("Exit") { _, _ -> super.onBackPressed() }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
