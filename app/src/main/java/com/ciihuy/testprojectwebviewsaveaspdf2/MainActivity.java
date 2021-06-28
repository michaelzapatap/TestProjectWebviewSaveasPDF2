package com.ciihuy.testprojectwebviewsaveaspdf2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.webviewtopdf.PdfView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    WebView browser;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        browser = findViewById(R.id.webView);
        browser.setWebChromeClient(new WebChromeClient());
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setDomStorageEnabled(true);
        browser.getSettings().setAllowFileAccessFromFileURLs(true);
        browser.getSettings().setAllowUniversalAccessFromFileURLs(true);
        browser.addJavascriptInterface(new WebAppInterface(this), "Android");
        browser.loadUrl("file:///android_asset/index.html");
        browser.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView viewx, String urlx) {
                if(!urlx.contains("http")){
                    viewx.loadUrl(urlx);
                    return false;
                }else{
                    if(!urlx.contains("https://ciihuy.com/")){
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlx));
                        startActivity(intent);
                        return true;
                    }else{
                        viewx.loadUrl(urlx);
                        return false;
                    }
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                browser.loadUrl("file:///android_asset/error.html");
            }

            @Override
            public void onPageFinished(WebView view, String url){
                progress.dismiss();

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                progress = ProgressDialog.show(MainActivity.this, null, "Please wait...", true);
                progress.setCanceledOnTouchOutside(false);
            }
        });
    }

    public class WebAppInterface {

        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showAd(){

        }

        @JavascriptInterface
        public void showBanner() {

        }

        @JavascriptInterface
        public void hideBanner() {

        }

        @JavascriptInterface
        public void removeAds() {

        }


        @JavascriptInterface
        public void portrait() {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }


        @JavascriptInterface
        public void landscape() {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        @JavascriptInterface
        public void printMe(){

            printAsPDF();

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        WebView browser = findViewById(R.id.webView);
        if ((keyCode == KeyEvent.KEYCODE_BACK) && browser.canGoBack()) {
            //if Back key pressed and webview can navigate to previous page
            browser.goBack();
            // go back to previous page
            return true;
        }
        else
        {
            //finish();
            // finish the activity
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    printAsPDF();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public boolean isFilePermissionAllowed(){

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }else{
            return true;
        }

    }

    public void requestFilePermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }


    public void printAsPDF(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            Toast.makeText(getApplicationContext(), "This feature is not available for your device's Android version.", Toast.LENGTH_LONG).show();
        }else{
            if(isFilePermissionAllowed()){
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/PDFTest/");
                final String fileName="Test.pdf";

                final ProgressDialog progressDialog=new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Please wait");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                PdfView.createWebPrintJob(MainActivity.this, browser, directory, fileName, new PdfView.Callback() {

                    @Override
                    public void success(String path) {
                        progressDialog.dismiss();
                        PdfView.openPdfFile(MainActivity.this,getString(R.string.app_name),"Do you want to open the pdf file? " + fileName, path);
                    }

                    @Override
                    public void failure() {
                        progressDialog.dismiss();

                    }
                });
            }else{
                requestFilePermission();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.savethispage:
                printAsPDF();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
