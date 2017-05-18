package com.innovagenesis.aplicaciones.android.examendocev3;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    public static final int SIGN_IN_GOOGLE_REQUEST_CODE = 1;
    private TextView textLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
     * */
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
     * */
    private void mCerrarSesionGoogle() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                //Limpia el textview
                textLogin.setText(null);
            }
        });
    }

    /**
     * Método encargado de gestionar los elementos del inicio
     * de sesión de Google
     * */
    private void mInicioSeccionGoogle() {
        Intent signIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signIntent, SIGN_IN_GOOGLE_REQUEST_CODE);
    }


    /**
     * Encargada de esperar los resultados de Google y Facebook
     * Login con la api
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Valida el login de google
        if (requestCode == SIGN_IN_GOOGLE_REQUEST_CODE){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

    }

    /***
     * Si el sign in con google es exito ejecuta este método
     * */
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG,"handleSignInResult: "+result.isSuccess());
        if (result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            if (account!= null){
                textLogin.setText(account.getEmail());
            }
        }
    }

    /**
     * Valida error de conexion a Google
     * */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG, "onConnectionFailed: Error conectando cuenta Google, " +
                connectionResult.getErrorMessage());
    }
}
