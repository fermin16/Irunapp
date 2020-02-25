package com.example.myapplication;

public class ItemLista {
    private int imagenPrincipal;
    private String nombreLugar;
    private String horario;

    public ItemLista(int imagenPrincipal, String nombreLugar, String horario) {
        this.imagenPrincipal = imagenPrincipal;
        this.nombreLugar = nombreLugar;
        this.horario = horario;
    }

    public int getImagenPrincipal() {
        return imagenPrincipal;
    }

    public String getNombreLugar() {
        return nombreLugar;
    }

    public String getHorario() {
        return horario;
    }

    public void setImagenPrincipal(int imagenPrincipal) {
        this.imagenPrincipal = imagenPrincipal;
    }

    public void setNombreLugar(String nombreLugar) {
        this.nombreLugar = nombreLugar;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }
}
