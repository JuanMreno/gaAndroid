package com.indesap.gestoraula;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.indesap.gestoraula.shared_preferences.SharedPreferencesManager;
import com.indesap.gestoraula.web_services.ValidateIpWebServices;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    //String host = "http://10.253.102.245:3000/";
    String host = "http://192.168.0.101:3000/";
    private ValueCallback<Uri[]> mFilePathCallback;
    private int INPUT_FILE_REQUEST_CODE = 1;
    private String TAG = "MainActivity";
    private static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");


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

        checkUrlHost();

        /*
        StringBuffer echo = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line = "";
            while((line = br.readLine()) != null) echo.append(line + "\n");
            br.close();

            Log.d(TAG,echo.toString());

            ArrayList<String> ips = new ArrayList<>();

            String res = echo.toString();
            Matcher matcher = IP_ADDRESS.matcher(res);
            while (matcher.find()){
                Log.d(TAG,"ip: " + matcher.group(1));

                ips.add(matcher.group(1));
                res = res.replace(matcher.group(1),"");
                matcher = IP_ADDRESS.matcher(res);
            }

            if(ips.size() > 0){
                Log.d(TAG,"IPS found");
                for(int i=0 ; i < ips.size() ; i++)
                    Log.w(TAG,ips.get(i));
            }
            else{
                Log.d(TAG,"Servidor no encontrado. Ninguna ip hallada.");
                Toast.makeText(this,"Servidor no encontrado.",Toast.LENGTH_LONG);
            }
        }
        catch(Exception e) { Log.e(TAG, e.toString()); }
        */
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

                            if(fileExt.equals(".pdf") || fileExt.equals(".PDF") || fileExt.equals(".vplc") || fileExt.equals(".VPLC")){
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

        } /*else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
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

    public void ipAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.ip_dialog_content, null);

        final EditText editText = (EditText)view.findViewById(R.id.editText);
        builder
            .setView(view)
            .setTitle(R.string.ip_dialog_title)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if(validate(editText.getText().toString())){
                        String ipAddress = editText.getText().toString();
                        ValidateIpWebServices validateIpWebServices = new ValidateIpWebServices();
                        validateIpWebServices.validateIpAddress(MainActivity.this,ipAddress);
                    }
                    else{
                        Toast.makeText(MainActivity.this, R.string.ip_not_val_toast,Toast.LENGTH_LONG).show();
                    }
                }
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    checkUrlHost();
                }
            })
            .show();
    }

    public void checkUrlHost() {
        SharedPreferencesManager sp =
                new SharedPreferencesManager(
                        this,
                        SharedPreferencesManager.URL_HOST
                );

        if(!sp.getString().equals("null")){
            ValidateIpWebServices validateIpWebServices = new ValidateIpWebServices();
            validateIpWebServices.validateHost(MainActivity.this,sp.getString());
        }
        else{
            ipAlertDialog();
        }
    }

    public static boolean validate(final String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }

    public void loadHost(){
        SharedPreferencesManager sp =
                new SharedPreferencesManager(
                        this,
                        SharedPreferencesManager.URL_HOST
                );

        if(!sp.getString().equals("null")){
            webView.loadUrl(sp.getString());
        }
    }

    public void alertDialogStandard(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .show();
    }
}
