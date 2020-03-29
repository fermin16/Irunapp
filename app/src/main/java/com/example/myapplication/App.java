package com.example.myapplication;

import android.app.Application;

import com.example.myapplication.Modelos.Lugar;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Lugar.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                // if defined
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build()
        );

        // Save the current Installation to Back4App
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
