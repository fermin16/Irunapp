package com.example.myapplication;

/**
 * Clase User:
 * Esta clase contiene la definicion de objetos de tipo User que extienden del servidor
 * y que seran utilizado para realizar consultas en la base de datos y poder registrar
 * a los usuarios.
 */

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("USUARIO")
public class User extends ParseObject {

    public static final String NOMBRE = "nombre";
    public static final String AP1 = "apellido1";
    public static final String AP2 = "apellido2";
    public static final String DNI = "DNI";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String PUNT = "puntuacion";
    public static final String VOTOS = "votos";

    public User(){
    }

    /* Metodos SET: */
    public void setNombre(String nombre){
        put(NOMBRE,nombre);
    }

    public void setAp1(String ap1){
        put(AP1,ap1);
    }

    public void setAp2(String ap2){
        put(AP2,ap2);
    }

    public void setDni(String dni){ put(DNI,dni); }

    public void setEmail(String email){
        put(EMAIL,email);
    }

    public void setPassword(String password){
        put(PASSWORD,password);
    }

    public void setPuntuacion(int puntuacion){ put(PUNT,puntuacion); }

    public void setVotos(int votos){ put(VOTOS,votos); }

    /* Metodos GET: */
    public String getNombre(){
        return getString(NOMBRE);
    }

    public String getAp1(){
        return getString(AP1);
    }

    public String getAp2(){
        return getString(AP2);
    }

    public String getDni(){
        return getString(DNI);
    }

    public String getEmail(){
        return getString(EMAIL);
    }

    public String getPassword(){
        return getString(PASSWORD);
    }

    public int getPuntuacion(){ return getInt(PUNT); }

    public int getVotos(){
        return getInt(VOTOS);
    }
}
