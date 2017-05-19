package com.innovagenesis.aplicaciones.android.examendocev3;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.innovagenesis.aplicaciones.android.examendocev3.contactos.ContactosFragment;
import com.innovagenesis.aplicaciones.android.examendocev3.geofense.GeofenceTransitionsIntentService;
import com.innovagenesis.aplicaciones.android.examendocev3.geofense.SimpleGeofence;

import java.util.ArrayList;
import java.util.List;

import static com.innovagenesis.aplicaciones.android.examendocev3.geofense.Constants.ANDROID_ID;
import static com.innovagenesis.aplicaciones.android.examendocev3.geofense.Constants.ANDROID_LATITUDE;
import static com.innovagenesis.aplicaciones.android.examendocev3.geofense.Constants.ANDROID_LOITERING_DELAY;
import static com.innovagenesis.aplicaciones.android.examendocev3.geofense.Constants.ANDROID_LONGITUDE;
import static com.innovagenesis.aplicaciones.android.examendocev3.geofense.Constants.ANDROID_RADIUS_METERS;
import static com.innovagenesis.aplicaciones.android.examendocev3.geofense.Constants.GEOFENCE_EXPIRATION_TIME;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    public static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient mApiClient;
    public static final int SIGN_IN_GOOGLE_REQUEST_CODE = 1;
    private TextView textLogin;

    private SimpleGeofence mAndroidGeofence;
    private List<Geofence> mGeofence;
    private PendingIntent mGeofenceRequestIntent;

    private static final int REQUEST_CODE = 1;
    private static final String[] PERMISOS = {
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Pide los permisos
        int leer = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int gps = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if ((leer == PackageManager.PERMISSION_DENIED) || (gps == PackageManager.PERMISSION_DENIED)) {
            ActivityCompat.requestPermissions(this, PERMISOS, REQUEST_CODE);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textLogin = (TextView) findViewById(R.id.txtview_email); //Muestra correo

        /** Inicializacion de complementos para login de Google*/
        findViewById(R.id.sign_in_button_google).setOnClickListener(this);
        findViewById(R.id.sign_out_button_google).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder
                (GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        if (!disponiblesGooglePlayServices()){
            Toast.makeText(MainActivity.this,
                    "Servicios de Google Play no disponibles", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void crearGeofences(){
        mAndroidGeofence = new SimpleGeofence(
                ANDROID_ID,
                ANDROID_LATITUDE,
                ANDROID_LONGITUDE,
                ANDROID_RADIUS_METERS,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL,
                ANDROID_LOITERING_DELAY
        );
        mGeofence.add(mAndroidGeofence.toGeofence());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Listener selector de los botones
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.sign_in_button_google:
                mInicioSeccionGoogle();
                break;
            case R.id.sign_out_button_google:
                mCerrarSesionGoogle();
                break;
        }
    }

    /**
     * Método encargado de gestionar los elementos del cerrar
     * sesión de Google
     */
    private void mCerrarSesionGoogle() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        //Limpia el textview
                        textLogin.setText(null);
                        fab.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Método encargado de gestionar los elementos del inicio
     * de sesión de Google
     */
    private void mInicioSeccionGoogle() {
        Intent signIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signIntent, SIGN_IN_GOOGLE_REQUEST_CODE);
    }

    /**
     * Encargada de esperar los resultados de Google y Facebook
     * Login con la api
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Valida el login de google
        if (requestCode == SIGN_IN_GOOGLE_REQUEST_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /***
     * Si el sign in con google es exito ejecuta este método
     * */
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult: " + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            if (account != null) {

                mApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();

                mApiClient.connect();

                mGeofence = new ArrayList<>();
                crearGeofences();

                textLogin.setText(account.getEmail());

                fab = (FloatingActionButton) findViewById(R.id.fab);

                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        ContactosFragment fragment = new ContactosFragment();
                        FragmentManager fragmentManager = getSupportFragmentManager();

                        fragmentManager.beginTransaction()
                                .replace(R.id.contenedor, fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                });
            }
        }
    }

    /**
     * Valida error de conexion a Google
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG, "onConnectionFailed: Error conectando cuenta Google, " +
                connectionResult.getErrorMessage());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        int leer = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(leer == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,PERMISOS,1);
        }
        mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
        LocationServices.GeofencingApi.addGeofences(mApiClient,mGeofence,mGeofenceRequestIntent);
        Toast.makeText(MainActivity.this, "Iniciando servicio de Geofence",
                Toast.LENGTH_SHORT).show();
        //finish();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private boolean disponiblesGooglePlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            Toast.makeText(MainActivity.this, "Servicios de Google Play disponibles",
                    Toast.LENGTH_SHORT).show();
            return true;
        } else return false;
    }

    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
