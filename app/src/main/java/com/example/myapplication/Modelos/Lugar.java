package com.example.myapplication.Modelos;


import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;


@ParseClassName("Alert")
public class Alert extends ParseObject {
    public Alert() {
    }


    //Metodos setter:
    public void setTitulo(String titulo) {
        put("titulo", titulo);
    }

    public void setDescripcion(String descripcion) {
        put("descripcion", descripcion);
    }

    public void setUser(String user) {
        put("user", user);
    }

    public void setRate(int rate) {
        put("rate", rate);
    }

    public void setNumRate(int numRate) {
        put("numRate", numRate);
    }

    public void setDireccion(String direccion) {
        put("direccion", direccion);
    }

    public void setlocalizacion(ParseGeoPoint localizacion) {
        put("localizacion", localizacion);
    }

    public void setFoto(byte[] foto) {
        put("foto", foto);
    }

    //Metodos getter:

    public String getDireccion() {
        return getString("direccion");
    }

    public String getDescripcion() {
        return getString("descripcion");
    }

    public String getTitulo() {
        return getString("titulo");
    }

    public String getDni() {
        return getString("dni");
    }

    public String getUser() {
        return getString("user");
    }

    public int getRate() {
        return getInt("rate");
    }

    public int getNumRate() {
        return getInt("numRate");
    }

    public List<String> getSoluciones() {
        return getList("solucion");
    }


    public ParseGeoPoint getLocalizacion() {
        return getParseGeoPoint("localizacion");
    }

    public byte[] getFoto() {
        return (byte[]) get("foto");
    }


}




