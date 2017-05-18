package com.innovagenesis.aplicaciones.android.examendocev3;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Clase encargada de crear el splash screen
 * Created by alexi on 14/05/2017.
 */

public class SplashScreenActivity extends AppCompatActivity implements Runnable{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(this).start(); //Ejecuta el delay

    }

    /**
     * Hace el delay
     * */
    @Override
    public void run() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        irActividadPrincipal();
    }

    /**
     * Inicia la activity principal
     * */
    private void irActividadPrincipal() {
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }
}
