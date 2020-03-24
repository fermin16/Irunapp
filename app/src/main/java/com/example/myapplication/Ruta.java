package com.example.myapplication;

public class Ruta {
    private int imagenPrincipal;
    private String nombreRuta;
    private String descripcion;

    public Ruta(int imagenPrincipal, String nombreRuta, String descripcion) {
        this.imagenPrincipal = imagenPrincipal;
        this.nombreRuta = nombreRuta;
        this.descripcion = descripcion;
    }

    public int getImagenPrincipal() {
        return imagenPrincipal;
    }

    public String getNombreRuta() {
        return nombreRuta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setImagenPrincipal(int imagenPrincipal) {
        this.imagenPrincipal = imagenPrincipal;
    }

    public void setNombreRuta(String nombreRuta) {
        this.nombreRuta = nombreRuta;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
