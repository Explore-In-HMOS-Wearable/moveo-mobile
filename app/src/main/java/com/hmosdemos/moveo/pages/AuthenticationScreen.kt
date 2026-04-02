package com.hmosdemos.moveo.pages

import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hmosdemos.moveo.common.Constants
import com.hmosdemos.moveo.viewmodel.AuthenticationViewModel

@Composable
fun AuthenticationScreen(
    onNavigateToMain: () -> Unit,
    viewModel: AuthenticationViewModel = viewModel()
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true

                webViewClient = object : WebViewClient() {

                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: SslError?
                    ) {
                        handler?.proceed()
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url.toString()
                        val code = viewModel.getQueryParameter(url, "code")

                        if (code != null) {
                            viewModel.postOAuthToken(
                                code = code,
                                context = context,
                                onSuccess = { onNavigateToMain() }
                            )
                            return true
                        }
                        return false
                    }
                }

                loadUrl(Constants.AUTH_URL)
            }
        }
    )
}