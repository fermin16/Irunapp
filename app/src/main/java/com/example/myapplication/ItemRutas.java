package com.example.myapplication;

public class ItemRutas {
    private int imagenPrincipal;
    private String nombreLugar;
    private String descripcion;

    public ItemRutas(int imagenPrincipal, String nombreLugar, String descripcion) {
        this.imagenPrincipal = imagenPrincipal;
        this.nombreLugar = nombreLugar;
        this.descripcion = descripcion;
    }

    public int getImagenPrincipal() {
        return imagenPrincipal;
    }

    public String getNombreLugar() {
        return nombreLugar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setImagenPrincipal(int imagenPrincipal) {
        this.imagenPrincipal = imagenPrincipal;
    }

    public void setNombreLugar(String nombreLugar) {
        this.nombreLugar = nombreLugar;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
