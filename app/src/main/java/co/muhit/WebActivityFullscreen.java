package co.muhit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * This Activity shows the website in a WebView.
 */
public class WebActivityFullscreen extends AppCompatActivity {

    /**
     * A resultcode to identify file-choice intents
     */
    private static int FILECHOOSER_RESULTCODE = 6321;
    /**
     * File upload callback for platform versions prior to Android 5.0
     */
    private ValueCallback<Uri> mFileUploadCallbackFirst;
    /**
     * File upload callback for Android 5.0+
     */
    private ValueCallback<Uri[]> mFileUploadCallbackSecond;
    /**
     * The WebView that displays the website
     */
    private WebView mWebView;
    /**
     * The WebView that displays any window popups.
     */
    private WebView mWebViewPop;

    /**
     * The button to press for retrying an internet connection
     */
    private Button mRetryButton;
    /**
     * The container layout that shows the WebView and any overlayed content
     */
    private RelativeLayout mContainer;
    /**
     * The view that blocks user input from reaching the WebView as long as the device is offline
     */
    private View mOfflineWarningView;
    /**
     * The view that entirely covers the WebView to make it faded
     */
    private View mOfflineWarningFadeLayover;
    /**
     * The URL to the website shown in the WebView
     */
    private String mAppUrl;
    /**
     * The domain of the website shown in the WebView
     */
    private String mAppWebDomain;

    private enum PageLoadingState {
        Initial,
        InProgress,
        Failed,
        Succeeded
    }
    private PageLoadingState mCurrentLoadingState = PageLoadingState.Initial;

    /**
     * A receiver that triggers an offline warning when internet is lost
     */
    private final NetworkReceiver mConnectivityReceiver = new NetworkReceiver(this) {
        @Override
        void onInternetAvailable() {
            // Do nothing, wait for manual retry request
        }

        @Override
        void onDisconnected() {
            WebActivityFullscreen.this.showOfflineWarning();
        }
    };

    /**
     * Method to update the view when the app is online again
     */
    private void continueOnline() {
        switch (mCurrentLoadingState) {
            case InProgress: {
                // Continue loading
                break;
            }
            case Initial:
            case Failed: {
                // If no successful load ever occurred yet, reload the webview now.
                startFirstPageLoad();
                break;
            }
            case Succeeded: {
                WebActivityFullscreen.this.hideOfflineWarning();
                break;
            }
        }
    }

    /**
     * The alpha of the offline warning. It's value will be taken from the initial layout value.
     */
    private float mInitialOfflineLayoverAlpha;

    public void onBackPressed() {
        // Forward BACK presses to the WebView as long as there is navigation history
        if (mWebView.isFocused() && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the website url and domain from resources
        mAppUrl = getString(R.string.web_url);
        mAppWebDomain = getString(R.string.web_prefix);

        // Load the layout and get outer container and get the views.
        // The container can be used to overlay content on the webview while offline
        setContentView(R.layout.activity_web);
        mContainer = (RelativeLayout) findViewById(R.id.activity_web);
        mWebView = (WebView) findViewById(R.id.web_view);
        mOfflineWarningView = findViewById(R.id.offline_warning_view);
        mOfflineWarningFadeLayover = findViewById(R.id.offline_warning_fade);
        mRetryButton = (Button) findViewById(R.id.retryButton);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mInitialOfflineLayoverAlpha = mOfflineWarningFadeLayover.getAlpha();
        }

        configureOpaque();

        mOfflineWarningView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // True to indicate that the event was handled by the view/listener
                return true;
            }
        });

        // Listen for changes in connectivity
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        getApplicationContext().registerReceiver(mConnectivityReceiver, intentFilter);

        // Configure the webview and cookie management
        mWebView.setWebViewClient(new DomainSpecificWebViewClient());
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setWebChromeClient(new DomainSpecificChromeClient());

        //Cookie manager for the webview
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        }

        CookieSyncManager.getInstance().sync();

        //Settings
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSavePassword(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);

        if (mConnectivityReceiver.isConnected()) {
            hideOfflineWarning();
            startFirstPageLoad();
        }

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnectivityReceiver.isConnected()) {
                    continueOnline();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.retry_still_offline, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void configureTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mOfflineWarningFadeLayover.setAlpha(mInitialOfflineLayoverAlpha);
        }

        mOfflineWarningFadeLayover.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.muhitOfflineTransparent));
    }

    private void configureOpaque() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mOfflineWarningFadeLayover.setAlpha(1.0f);
        }

        mOfflineWarningFadeLayover.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.muhitOfflineOpaque));
    }

    @Override
    protected void onResume() {
        super.onResume();

        CookieSyncManager.getInstance().startSync();
    }

    @Override
    protected void onPause() {
        super.onPause();

        CookieSyncManager.getInstance().stopSync();
    }

    /**
     * Start displaying the warning that the website cannot be reached
     */
    private void showOfflineWarning() {
        mOfflineWarningView.setVisibility(View.VISIBLE);
    }

    /**
     * Stop displaying the warning that the website cannot be reached and make the webview available again
     */
    private void hideOfflineWarning() {
        mOfflineWarningView.setVisibility(View.GONE);
    }

    private void startFirstPageLoad() {
        mCurrentLoadingState = PageLoadingState.InProgress;

        mWebView.loadUrl(mAppUrl);
    }

    /**
     * Handle loading of new URLs. When a target should be handled externally this function launches an Intent to do so
     *
     * @param host The host of which the request is to be handeled
     * @param uri The target URI to load
     * @return Whether a target should be loaded into this app's WebView (true) or externally (false)
     */
    private boolean handleRequest(String host, Uri uri) {
        if (host.equals(mAppWebDomain)) {
            // This is within the website, so do not override; let my WebView load
            // the page
            if (mWebViewPop != null) {
                mWebViewPop.setVisibility(View.GONE);
                mContainer.removeView(mWebViewPop);
                mWebViewPop = null;
            }
            return false;
        }

        // Handle facebook requests in the webview so that facebook-login can function
        if (host.equals("m.facebook.com") || host.equals("www.facebook.com")) {
            return false;
        }

        // Otherwise, the link is not within this website
        // Launch an intent to view the link externally
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    if (mFileUploadCallbackFirst != null) {
                        mFileUploadCallbackFirst.onReceiveValue(intent.getData());
                        mFileUploadCallbackFirst = null;
                    } else if (mFileUploadCallbackSecond != null) {
                        Uri[] dataUris;
                        try {
                            dataUris = new Uri[]{Uri.parse(intent.getDataString())};
                        } catch (Exception e) {
                            dataUris = null;
                        }

                        mFileUploadCallbackSecond.onReceiveValue(dataUris);
                        mFileUploadCallbackSecond = null;
                    }
                }
            } else {
                if (mFileUploadCallbackFirst != null) {
                    mFileUploadCallbackFirst.onReceiveValue(null);
                    mFileUploadCallbackFirst = null;
                } else if (mFileUploadCallbackSecond != null) {
                    mFileUploadCallbackSecond.onReceiveValue(null);
                    mFileUploadCallbackSecond = null;
                }
            }
        }
    }

    /**
     * Extension of WebViewClient that only allows the website's domain and facebook requests to be handled by the app's WebView.
     */
    private class DomainSpecificWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                String host = request.getUrl().getHost();
                return handleRequest(host, request.getUrl());
            } else {
                return super.shouldOverrideUrlLoading(view, request);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();

            return handleRequest(host, uri);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            // If an error was received before an onPageFinished, then we assume that Muhit.co cannot be reached
            if (mCurrentLoadingState != PageLoadingState.Succeeded) {
                mCurrentLoadingState = PageLoadingState.Failed;

                configureOpaque();
                showOfflineWarning();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (mCurrentLoadingState == PageLoadingState.InProgress && mConnectivityReceiver.isConnected()) {
                mCurrentLoadingState = PageLoadingState.Succeeded;

                // The overlay can now become transparent
                configureTransparent();
                hideOfflineWarning();
            }
        }
    }

    /**
     * See also: http://stackoverflow.com/questions/12648099/making-facebook-login-work-with-an-android-webview
     * See also: http://stackoverflow.com/questions/23790231/android-webview-and-facebook-login-not-working
     */
    private class DomainSpecificChromeClient extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            mWebViewPop = new WebView(getApplicationContext());
            mWebViewPop.setVerticalScrollBarEnabled(false);
            mWebViewPop.setHorizontalScrollBarEnabled(false);
            mWebViewPop.setWebViewClient(new DomainSpecificWebViewClient());
            mWebViewPop.getSettings().setJavaScriptEnabled(true);
            mWebViewPop.getSettings().setSavePassword(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().setAcceptThirdPartyCookies(mWebViewPop, true);
            }

            mWebViewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(mWebViewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebViewPop);
            resultMsg.sendToTarget();

            return true;
        }

        // File uploads as found in: https://github.com/delight-im/Android-AdvancedWebView

        // file upload callback (Android 2.2 (API level 8) -- Android 2.3 (API level 10)) (hidden method)
        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, null);
        }

        // file upload callback (Android 3.0 (API level 11) -- Android 4.0 (API level 15)) (hidden method)
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooser(uploadMsg, acceptType, null);
        }

        // file upload callback (Android 4.1 (API level 16) -- Android 4.3 (API level 18)) (hidden method)
        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileInput(uploadMsg, null);
        }

        // file upload callback (Android 5.0 (API level 21) -- current) (public method)
        @SuppressWarnings("all")
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            openFileInput(null, filePathCallback);
            return true;
        }

        @SuppressLint("NewApi")
        protected void openFileInput(final ValueCallback<Uri> fileUploadCallbackFirst, final ValueCallback<Uri[]> fileUploadCallbackSecond) {
            if (mFileUploadCallbackFirst != null) {
                mFileUploadCallbackFirst.onReceiveValue(null);
            }
            mFileUploadCallbackFirst = fileUploadCallbackFirst;

            if (mFileUploadCallbackSecond != null) {
                mFileUploadCallbackSecond.onReceiveValue(null);
            }
            mFileUploadCallbackSecond = fileUploadCallbackSecond;

            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");

            startActivityForResult(Intent.createChooser(i, getString(R.string.upload_images_msg)), FILECHOOSER_RESULTCODE);
        }
    }
}