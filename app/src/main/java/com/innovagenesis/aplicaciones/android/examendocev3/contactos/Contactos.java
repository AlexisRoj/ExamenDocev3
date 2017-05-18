package com.innovagenesis.aplicaciones.android.examendocev3.contactos;

/**
 * Clase encargada de extraer los elementos del dispositivo
 * Created by alexi on 18/05/2017.
 */

public class Contactos {

    private String email;
    private String nombre;
    private String numero;

    public Contactos(String email, String nombre, String numero) {
        this.email = email;
        this.nombre = nombre;
        this.numero = numero;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}
