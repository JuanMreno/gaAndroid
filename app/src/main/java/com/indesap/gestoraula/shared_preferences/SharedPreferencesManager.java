package com.indesap.gestoraula.shared_preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    static public String URL_HOST = "URL_HOST";

    SharedPreferences sharedPreferences;
    Context context;

    String type;


    public SharedPreferencesManager(Context context,String type)
    {
        sharedPreferences = context.getSharedPreferences(type,Context.MODE_PRIVATE);
        this.type = type;
    }

    public void setString(String s){
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.putString(this.type, s);
        editor.apply();
    }

    public String getString(){
        if(sharedPreferences.contains(this.type))
            return sharedPreferences.getString(this.type,"null");
        else
            return "null";
    }

}
