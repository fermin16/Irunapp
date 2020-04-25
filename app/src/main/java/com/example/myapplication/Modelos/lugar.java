package com.example.myapplication.Modelos;


import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

@ParseClassName("lugar")
public class lugar extends ParseObject {
    public lugar() {
    }

    //Metodos setter:
    public void setNombre(String nombre) {
        put("nombre", nombre);
    }

    public void setDescripcion(String descripcion) {
        put("descripcion", descripcion);
    }

    public void setContacto(String contacto) {
        put("contacto", contacto);
    }

    public void setWeb(String web) {
        put("web", web);
    }

    public void setDireccion(String direccion) {
        put("direccion", direccion);
    }

    public void setLocalizacion(ParseGeoPoint localizacion) {
        put("localizacion", localizacion);
    }

    public void setFoto(byte[] foto) {
        put("foto", foto);
    }

    public void setMas(String mas) {
        put("mas", mas);
    }

    //Metodos getter:

    public String getDireccion() {
        return getString("direccion");
    }

    public String getDescripcion() {
        return getString("descripcion");
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

    public String getMas() {
        return getString("mas");
    }


}




