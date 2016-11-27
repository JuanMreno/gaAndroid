package com.indesap.gestoraula;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    //String host = "http://10.253.102.245:3000/";
    String host = "http://192.168.0.8:3000/";
    private ValueCallback<Uri[]> mFilePathCallback;
    private int INPUT_FILE_REQUEST_CODE = 1;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        webView = (WebView) findViewById(R.id.mWebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setSaveFormData(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowContentAccess(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }

        if (Build.VERSION.SDK_INT >= 19)
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        else if(Build.VERSION.SDK_INT >=11 && Build.VERSION.SDK_INT < 19)
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUserAgentString(
                webView.getSettings().getUserAgentString() +
                " " +
                getString(R.string.user_agent_suffix)
        );

        //webView.loadUrl("http://192.168.0.5:3000");
        webView.loadUrl(host);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("WebView",url);
            if (url.equals(host)) {
                // This is my web site, so do not override; let my WebView load the page
                webView.loadUrl(host);
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            result.cancel();
            return true;
            //return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            Log.d("WebView","onShowFileChooser");
            //return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.setType("file/*");

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);

            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"onActivityResult");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG,">= LOLLIPOP");

            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri[] results = null;

            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    results = new Uri[]{};
                } else {
                    if (data.getDataString() != null) {
                        Uri uri = Uri.parse(data.getDataString());

                        Log.d(TAG,"uri: " + uri.toString());
                        try {
                            String fileExt = uri.toString().substring( uri.toString().lastIndexOf("."));
                            Log.d(TAG,"fileExt: " + fileExt);

                            if(fileExt.equals(".pdf") || fileExt.equals(".PDF")){
                                //String base64File = getStringFile(new File(uri.getPath()));
                                //Log.d(TAG,base64File);
                                //evaluateJS("javascript: {document.getElementById(\"inpCodFile\").value ='" + base64File + "';};");
                                results = new Uri[]{uri};
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"Únicamente se permiten adjuntar archivos tipo PDF.",Toast.LENGTH_LONG).show();
                                results = new Uri[]{};
                            }
                        }
                        catch(Exception e){
                            Toast.makeText(getApplicationContext(),"Únicamente se permiten adjuntar archivos tipo PDF.",Toast.LENGTH_LONG).show();
                            results = new Uri[]{};
                        }
                    }
                    else{
                        results = new Uri[]{};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;

        }/* else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }

                if (requestCode == FILECHOOSER_RESULTCODE) {

                    if (null == this.mUploadMessage) {
                        return;

                    }

                    Uri result = null;

                    try {
                        if (resultCode != RESULT_OK) {

                            result = null;

                        } else {

                            // retrieve from the private variable if the intent is null
                            result = data == null ? mCapturedImageURI : data.getData();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "activity :" + e,
                                Toast.LENGTH_LONG).show();
                    }

                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;

                }
            }*/

        return;
    }

    private void evaluateJS(String code) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(code, null);
        } else {
            webView.loadUrl(code);
        }
    }

    public String getStringFile(File f) {
        InputStream inputStream = null;
        String encodedFile= "", lastVal;
        try {
            inputStream = new FileInputStream(f.getAbsolutePath());

            byte[] buffer = new byte[10240];//specify the size to allow
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Base64OutputStream output64 = new Base64OutputStream(output, Base64.DEFAULT);

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output64.write(buffer, 0, bytesRead);
            }
            output64.close();
            encodedFile =  output.toString();
        }
        catch (FileNotFoundException e1 ) {
            e1.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        lastVal = encodedFile;
        return lastVal;
    }
}
