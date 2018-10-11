package np.mks.ioesyllabus;

        import android.annotation.SuppressLint;
        import android.net.Uri;
        import android.content.Context;
        import android.content.Intent;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.KeyEvent;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.WindowManager;
        import android.webkit.DownloadListener;
        import android.webkit.ValueCallback;
        import android.webkit.WebChromeClient;
        import android.webkit.WebView;
        import android.webkit.WebViewClient;


        import java.io.File;
        import java.io.IOException;
        import java.text.SimpleDateFormat;
        import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int FCR = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private WebView webView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(FCR);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_main);
        this.webView = (WebView) findViewById(R.id.webView1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);





        startWebView("file:///android_asset/index.html");
        this.webView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Intent i = new Intent("android.intent.action.VIEW");
                i.setData(Uri.parse(url));
                MainActivity.this.startActivity(i);
            }
        });
        this.webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String fallingUrl) {
                MainActivity.this.webView.loadUrl("file:///android_asset/error.html");
            }
        });
        return;


    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if (netinfo == null || !netinfo.isConnectedOrConnecting()) {
            return false;
        }
        NetworkInfo wifi = cm.getNetworkInfo(FCR);
        NetworkInfo mobile = cm.getNetworkInfo(0);
        if (mobile != null && mobile.isConnectedOrConnecting()) {
            return true;
        }
        if (wifi == null || !wifi.isConnectedOrConnecting()) {
            return false;
        }
        return true;
    }

   /* public Builder buildDialog(Context c) {
        Builder builder = new Builder(c);
        builder.setTitle("No Internet Connection");
        builder.setMessage("You need to have Mobile Data or wifi to access this. Press ok to Exit");
        builder.setPositiveButton("Ok", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        return builder;
    } */



    private void startWebView(String url) {
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setLoadsImagesAutomatically(true);
        this.webView.setWebViewClient(new WebViewClient());
        this.webView.setWebChromeClient(new WebChromeClient());

        this.webView.setScrollBarStyle(33554432);
        this.webView.setScrollbarFadingEnabled(false);
        this.webView.loadUrl(url);
        this.webView.setWebChromeClient(new WebChromeClient() {
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                Intent[] intentArray;
                if (MainActivity.this.mUMA != null) {
                    MainActivity.this.mUMA.onReceiveValue(null);
                }
                MainActivity.this.mUMA = filePathCallback;
                Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
                if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                    File file = null;
                    try {
                        file = MainActivity.this.createImageFile();
                        takePictureIntent.putExtra("PhotoPath", MainActivity.this.mCM);
                    } catch (IOException ex) {
                        Log.e(MainActivity.TAG, "Image file creation failed", ex);
                    }
                    if (file != null) {
                        MainActivity.this.mCM = "file:" + file.getAbsolutePath();
                        takePictureIntent.putExtra("output", Uri.fromFile(file));
                    } else {
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent("android.intent.action.GET_CONTENT");
                contentSelectionIntent.addCategory("android.intent.category.OPENABLE");
                contentSelectionIntent.setType("*/*");
                if (takePictureIntent != null) {
                    intentArray = new Intent[MainActivity.FCR];
                    intentArray[0] = takePictureIntent;
                } else {
                    intentArray = new Intent[0];
                }
                Intent chooserIntent = new Intent("android.intent.action.CHOOSER");
                chooserIntent.putExtra("android.intent.extra.INTENT", contentSelectionIntent);
                chooserIntent.putExtra("android.intent.extra.TITLE", "Select your PDF file");
                chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", intentArray);
                MainActivity.this.startActivityForResult(chooserIntent, MainActivity.FCR);
                return true;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= 21) {
            Uri[] results = null;
            if (resultCode == -1 && requestCode == FCR) {
                if (this.mUMA != null) {
                    if (intent != null) {
                        String dataString = intent.getDataString();
                        if (dataString != null) {
                            results = new Uri[FCR];
                            results[0] = Uri.parse(dataString);
                        }
                    } else if (this.mCM != null) {
                        results = new Uri[FCR];
                        results[0] = Uri.parse(this.mCM);
                    }
                } else {
                    return;
                }
            }
            this.mUMA.onReceiveValue(results);
            this.mUMA = null;
        } else if (requestCode == FCR && this.mUM != null) {
            Uri result = (intent == null || resultCode != -1) ? null : intent.getData();
            this.mUM.onReceiveValue(result);
            this.mUM = null;
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    private File createImageFile() throws IOException {
        return File.createTempFile("img_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_", ".jpg", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
    }

    public void onBackPressed() {
        if (this.webView.canGoBack()) {
            this.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
}
