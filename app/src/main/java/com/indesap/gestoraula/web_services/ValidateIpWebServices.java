package com.indesap.gestoraula.web_services;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.indesap.gestoraula.MainActivity;
import com.indesap.gestoraula.R;
import com.indesap.gestoraula.shared_preferences.SharedPreferencesManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by JuanMoreno on 17/12/2016.
 */
public class ValidateIpWebServices {
    Context context;
    SharedPreferencesManager sharedPreferences;

    private String TAG = "LoginWebServices";

    private String dbName = "";
    private Integer dbVersion;
    private String urlServidor;
    private String urlHost;

    public ValidateIpWebServices() {
    }

    public void validateIpAddress(Context context, String ipAddress){
        this.context = context;
        String url = "http://" + ipAddress + ":3000/externals/validate";
        urlHost = "http://" + ipAddress + ":3000/";

        urlServidor = url;
        ConnectivityManager connMgr = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new ConsumirValidateIpAddress().execute("");
        } else {
            ((MainActivity)context).alertDialogStandard(
                    context.getResources().getString(R.string.txt_tit_error_net),
                    context.getResources().getString(R.string.txt_mns_error_net)
            );
        }
    }

    public void validateHost(Context context, String host){
        this.context = context;
        urlServidor = host + "externals/validate";
        ConnectivityManager connMgr = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new ConsumirValidateHost().execute("");
        } else {
            ((MainActivity)context).alertDialogStandard(
                    context.getResources().getString(R.string.txt_tit_error_net),
                    context.getResources().getString(R.string.txt_mns_error_net)
            );
        }
    }

    public class ConsumirValidateIpAddress extends AsyncTask<String, Void, String> {
        ProgressDialog dialog = new ProgressDialog(context);
        boolean exception = false;

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                URL url = new URL(urlServidor);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                // Starts the query

                //conn.setRequestProperty("data",params[0]);

                Log.w(TAG, "Consulta: " + urlServidor);
                conn.connect();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.w(TAG, "Error Consulta, Code: " + conn.getResponseCode() + " message: " + conn.getResponseMessage());
                    exception = true;
                    return null;
                }

                String res = inputStreamToString(conn.getInputStream()).toString();
                Log.w(TAG, "Respuesta: " + res);

                JSONObject resultJson = (JSONObject) new JSONObject(res);

                if(!resultJson.getString("status").equals("true"))
                    exception = true;

                return null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.w(TAG, "Exception" + e.toString());
                exception = true;
            }catch (Exception e) {
                // TODO Auto-generated catch block
                Log.w(TAG, "Exception" + e.toString());
                exception = true;
            }
            return null;
        }

        @Override
        protected void onPreExecute(){
            dialog.setMessage(context.getString(R.string.looking_for_server_dialog));
            dialog.show();
            dialog.setCancelable(false);
        }

        @Override
        protected void onPostExecute(String result){
            dialog.dismiss();

            Log.w("onPostExecute",Boolean.toString(exception));
            if(exception){
                ((MainActivity)context).ipAlertDialog();
            }
            else{
                SharedPreferencesManager sp =
                        new SharedPreferencesManager(
                                context,
                                SharedPreferencesManager.URL_HOST
                        );
                sp.setString(urlHost);
                ((MainActivity)context).loadHost();
            }
        }
    }

    public class ConsumirValidateHost extends AsyncTask<String, Void, String> {
        ProgressDialog dialog = new ProgressDialog(context);
        boolean exception = false;

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                URL url = new URL(urlServidor);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                // Starts the query

                //conn.setRequestProperty("data",params[0]);

                Log.w(TAG, "Consulta: " + urlServidor);
                conn.connect();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.w(TAG, "Error Consulta, Code: " + conn.getResponseCode() + " message: " + conn.getResponseMessage());
                    exception = true;
                    return null;
                }

                String res = inputStreamToString(conn.getInputStream()).toString();
                Log.w(TAG, "Respuesta: " + res);

                JSONObject resultJson = (JSONObject) new JSONObject(res);

                if(!resultJson.getString("status").equals("true"))
                    exception = true;

                return null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.w(TAG, "Exception" + e.toString());
                exception = true;
            }catch (Exception e) {
                // TODO Auto-generated catch block
                Log.w(TAG, "Exception" + e.toString());
                exception = true;
            }
            return null;
        }

        @Override
        protected void onPreExecute(){
            dialog.setMessage(context.getString(R.string.looking_for_server_dialog));
            dialog.show();
            dialog.setCancelable(false);
        }

        @Override
        protected void onPostExecute(String result){
            dialog.dismiss();

            Log.w("onPostExecute",Boolean.toString(exception));
            if(exception){
                ((MainActivity)context).ipAlertDialog();
            }
            else{
                ((MainActivity)context).loadHost();
            }
        }
    }

    private StringBuilder inputStreamToString(InputStream is) {
        String rLine = "";
        StringBuilder answer = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        try{
            while ((rLine = rd.readLine()) != null) {
                answer.append(rLine);
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }
        return answer;
    }
}
