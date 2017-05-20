package com.innovagenesis.aplicaciones.android.examendocev3;

import android.app.Application;

import com.facebook.appevents.AppEventsLogger;

/**
 * Instancia clase de Facebook
 * Created by alexi on 19/05/2017.
 */

public class ExamenDoceAplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppEventsLogger.activateApp(this);
    }
}
