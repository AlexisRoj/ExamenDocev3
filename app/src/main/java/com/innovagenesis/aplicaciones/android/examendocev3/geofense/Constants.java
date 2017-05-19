package com.innovagenesis.aplicaciones.android.examendocev3.geofense;

import com.google.android.gms.location.Geofence;

/**
 * Encargado de gestionar los elementos del Geofense.
 * Created by alexi on 18/05/2017.
 */

public class Constants {

    private Constants() {
    }

    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final long CONNECTION_TIME_OUT_MS = 100;
    public static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;
    public static final String ANDROID_ID = "1";
    public static final double ANDROID_LATITUDE = 9.90;
    public static final double ANDROID_LONGITUDE = -84.10;
    public static final float ANDROID_RADIUS_METERS = 1000F;
    public static final int ANDROID_LOITERING_DELAY = 100;

}
