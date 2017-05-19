package com.innovagenesis.aplicaciones.android.examendocev3.geofense;

import com.google.android.gms.location.Geofence;

public class SimpleGeofence {

    private final String mId;
    private final double mLatitude;
    private final double mLongitude;
    private final float mRadius;
    private long mExpirationDuration;
    private int mTransitionType;
    private int mLoitering;

    /** Constructor del Geofense*/
    public SimpleGeofence(String geofenceId,
                          double latitude,
                          double longitude,
                          float radius,
                          long expiration,
                          int transition,
                          int loitering){
        this.mId = geofenceId;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        this.mTransitionType = transition;
        this.mExpirationDuration = expiration;
        this.mLoitering = loitering;
    }

    public String getmId() {
        return mId;
    }

    public double getmLatitude() {
        return mLatitude;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public float getmRadius() {
        return mRadius;
    }

    public long getmExpirationDuration() {
        return mExpirationDuration;
    }

    public int getmTransitionType() {
        return mTransitionType;
    }

    public int getmLoitering(){ return mLoitering;}

    public Geofence toGeofence(){
        return new Geofence.Builder()
                .setRequestId(mId)
                .setTransitionTypes(mTransitionType)
                .setCircularRegion(mLatitude,mLongitude, mRadius)
                .setExpirationDuration(mExpirationDuration)
                .setLoiteringDelay(mLoitering)
                .build();
    }
}
