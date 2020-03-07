package com.example.myapplication.Modelos;

import android.content.Context;
import android.content.SharedPreferences;


import static android.content.Context.MODE_PRIVATE;

/*Clase preferencias que contiene los metodos para almacenmar y recuperar datos de una base de datos local
* de la aplicación.*/
public class Preferencias {
    //Constantes para almacenar los datos de la aplicacion.
    private static final String ID_PREFERENCIAS = "idPreferenciasIrunapp";
    private static final String KILOMETROS = "kilometros_mapa";
    private static final String FRECUENCIA = "frecuencia_mapa";
    private static final String NUM_PUNTOS = "num_puntos_mapa";


    //Metodos para guardar datos:
    /**Necesitamos un contexto con el que almacenar los datos**/
    public static void guardaKilometros(Context contexto, int kilometros){
        SharedPreferences preferencias = contexto.getSharedPreferences(ID_PREFERENCIAS,MODE_PRIVATE);//Generar un fichero interno para almacenar preferencias
        preferencias.edit().putInt(KILOMETROS,kilometros).apply();//Guardar el estado del boton.
    }
    public static void guardaFrecuencia(Context contexto, int frecuencia){
        SharedPreferences preferencias = contexto.getSharedPreferences(ID_PREFERENCIAS,MODE_PRIVATE);//Generar un fichero interno para almacenar preferencias
        preferencias.edit().putInt(FRECUENCIA,frecuencia).apply();//Guardar el estado del boton.
    }
    public static void guardaPuntos(Context contexto, int num_puntos){
        SharedPreferences preferencias = contexto.getSharedPreferences(ID_PREFERENCIAS,MODE_PRIVATE);//Generar un fichero interno para almacenar preferencias
        preferencias.edit().putInt(NUM_PUNTOS,num_puntos).apply();//Guardar el estado del boton.
    }


    //Metodos para recuperar datos:
    public static int getKilometros(Context contexto){
        SharedPreferences preferencias = contexto.getSharedPreferences(ID_PREFERENCIAS,MODE_PRIVATE);
        return preferencias.getInt(KILOMETROS, 1); //Devolver por defecto la opción 1
    }
    public static int getFrecuencia(Context contexto){
        SharedPreferences preferencias = contexto.getSharedPreferences(ID_PREFERENCIAS,MODE_PRIVATE);
        return preferencias.getInt(FRECUENCIA, 1); //Devolver por defecto la opción 1
    }
    public static int getPuntos(Context contexto){
        SharedPreferences preferencias = contexto.getSharedPreferences(ID_PREFERENCIAS,MODE_PRIVATE);
        return preferencias.getInt(NUM_PUNTOS, 1); //Devolver por defecto la opción 1
    }

}
