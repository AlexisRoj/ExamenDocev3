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

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.innovagenesis.aplicaciones.android.examendocev3.contactos.ContactosFragment;
import com.innovagenesis.aplicaciones.android.examendocev3.geofense.GeofenceTransitionsIntentService;
import com.innovagenesis.aplicaciones.android.examendocev3.geofense.SimpleGeofence;

import org.json.JSONException;
import org.json.JSONObject;

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
        GoogleApiClient.ConnectionCallbacks, FacebookCallback<LoginResult> {

    public static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient mApiClient;
    public static final int SIGN_IN_GOOGLE_REQUEST_CODE = 1;
    private TextView textLogin, txtSeguro;

    AccessTokenTracker accessTokenTracker;
    private List<Geofence> mGeofence;

    LoginButton loginButton;
    CallbackManager callbackManager;


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
        int internet = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET);

        if ((leer == PackageManager.PERMISSION_DENIED)
                || (gps == PackageManager.PERMISSION_DENIED)
                || (internet == PackageManager.PERMISSION_DENIED)) {
            ActivityCompat.requestPermissions(this, PERMISOS, REQUEST_CODE);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button_facebook);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, this);
        /* Logut de Facebook*/
        accessTokenTracker = new AccessTokenTracker() {
            /** Finaliza sesión en facebook*/
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    //write your code here what to do when user logout
                    Toast.makeText(MainActivity.this, "Finalizo sesión", Toast.LENGTH_SHORT).show();
                    mDesloguearse();
                }
            }
        };

        textLogin = (TextView) findViewById(R.id.txtview_email); //Muestra correo
        txtSeguro = (TextView) findViewById(R.id.txtview_geofence); //Mensaje GeoFence

        textLogin.setText(null);
        txtSeguro.setText(null);

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

        if (!disponiblesGooglePlayServices()) {
            Toast.makeText(MainActivity.this,
                    R.string.googleNoDisponible, Toast.LENGTH_SHORT).show();
            finish();
        }


    }

    /**
     * Inicializa el GeoFence
     */
    public void crearGeofences() {
        SimpleGeofence mAndroidGeofence = new SimpleGeofence(
                ANDROID_ID,
                ANDROID_LATITUDE,
                ANDROID_LONGITUDE,
                ANDROID_RADIUS_METERS,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL,
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
        if (mGoogleApiClient != null) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            //Limpia el textview
                            mDesloguearse();
                        }
                    });
        }
    }

    /**
     * Limpia al desloguear la cuenta
     * */
    private void mDesloguearse() {
        textLogin.setText(null);
        txtSeguro.setText(null);
        fab.setVisibility(View.GONE);
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
        } else
            callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /***
     * Si el sign in con google es exito ejecuta este método
     * */
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult: " + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            if (account != null) {

                mIniciarGeofences();//Inicia el Geofences

                textLogin.setText(account.getEmail());
                txtSeguro.setText(getString(R.string.zonas_seguras));

                botonFlotante(); // Inica el boton Flotante
            }
        }
    }

    private void botonFlotante() {
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** Carga los contactos en el fragmeto*/
                ContactosFragment fragment = new ContactosFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();

                fragmentManager.beginTransaction()
                        .replace(R.id.contenedor, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    /**
     * Encargado de iniciar el Geofences
     */
    private void mIniciarGeofences() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();
        mGeofence = new ArrayList<>();
        crearGeofences();
    }

    /**
     * Valida error de conexion a Google
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG, "onConnectionFailed: Error conectando cuenta Google, " +
                connectionResult.getErrorMessage());
    }

    /**
     * Conecta con el GeoFence
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        int leer = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (leer == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, PERMISOS, 1);
        }
        PendingIntent mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
        LocationServices.GeofencingApi.addGeofences(mApiClient, mGeofence, mGeofenceRequestIntent);
        Toast.makeText(MainActivity.this, R.string.iniciandoGeofences,
                Toast.LENGTH_SHORT).show();
        //finish();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Coprueba si el servicio se encuentra disponible
     */
    private boolean disponiblesGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            Toast.makeText(MainActivity.this, R.string.servicioDisponible,
                    Toast.LENGTH_SHORT).show();
            return true;
        } else return false;
    }

    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Metodo confirmacion inicio de seccion facebook
     */
    @Override
    public void onSuccess(LoginResult loginResult) {

        Toast.makeText(this, R.string.successFacebook, Toast.LENGTH_SHORT).show();
        /** Pide el correo*/
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        mIniciarGeofences();//Inicia el Geofences
                        String email = null;
                        try {
                            email = object.getString("email");
                            textLogin.setText(email);
                            txtSeguro.setText(getString(R.string.zonas_seguras));
                            botonFlotante(); // Inica el boton Flotante

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        Bundle parameters = new Bundle();
        //parameters.putString("fields", "id,name,email,gender,birthday");
        parameters.putString("fields", "email");
        request.setParameters(parameters);
        request.executeAsync();

    }

    /**
     * Metodo procesos en la cancelacion inicio sesion facebook
     */
    @Override
    public void onCancel() {
        Toast.makeText(this, R.string.cancelFacebook, Toast.LENGTH_SHORT).show();
    }

    /**
     * Metodo a ejecutar cuando existe un error en el login de facebook
     */
    @Override
    public void onError(FacebookException error) {
        Toast.makeText(this, getString(R.string.errorFacebook)
                + error, Toast.LENGTH_SHORT).show();

    }


}
