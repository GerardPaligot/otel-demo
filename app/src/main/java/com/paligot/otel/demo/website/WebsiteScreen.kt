package com.paligot.otel.demo.website

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.paligot.otel.demo.BuildConfig
import com.paligot.otel.demo.schedules.AppBottomBar
import com.paligot.otel.demo.schedules.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WebsiteViewModel = hiltViewModel(),
) {
    var webView by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        val currentWebView = webView
        if (currentWebView?.canGoBack() == true) {
            currentWebView.goBack()
        } else {
            val popped = navController.popBackStack(BottomNavItem.SCHEDULE.route, false)
            if (!popped) {
                navController.navigate(BottomNavItem.SCHEDULE.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Website",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                },
            )
        },
        bottomBar = {
            AppBottomBar(navController = navController)
        },
    ) { innerPadding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webView = this
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            viewModel.onPageStarted(url)
                        }

                        override fun onPageFinished(view: WebView, url: String) {
                            super.onPageFinished(view, url)
                            viewModel.onPageFinished(url)
                        }
                    }
                    settings.javaScriptEnabled = true
                    loadUrl("https://${BuildConfig.EVENT_URL}")
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}
