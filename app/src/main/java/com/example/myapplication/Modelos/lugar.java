package com.example.myapplication.Modelos;


import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;


@ParseClassName("lugar")
public class lugar extends ParseObject {
    public lugar() {
    }

    //Metodos getter:

    public String getDireccion() {
        return getString("direccion");
    }

    public String getDescripcion() {
        return getString("mas");
    }

    public String getNombre() {
        return getString("nombre");
    }

    public String getHorario() {
        return getString("horario");
    }

    public String getContacto() {
        return getString("contacto");
    }

    public String getWeb() {
        return getString("web");
    }

    public ParseGeoPoint getLocalizacion() {
        return getParseGeoPoint("localizacion");
    }

    public byte[] getFoto() {
        return (byte[]) get("foto");
    }

    public String getPrecio() {
        return getString("precio");
    }


}




