package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.example.myapplication.Modelos.lugar;
import com.parse.Parse;
import com.parse.ParseGeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ruta {
    private Bitmap imagenPrincipal;
    private String nombreRuta;
    private String descripcion;
    private List<ParseGeoPoint> lugares;

    private static ArrayList<Ruta> rutas = null;

    public static Ruta crearRuta(String nombreRuta, String descripcion, Bitmap imagen, List<lugar> lugares) {
        List<ParseGeoPoint> puntos = new ArrayList<>();
        for (lugar l: lugares) {
            puntos.add(l.getLocalizacion());
        }
        return new Ruta(nombreRuta, descripcion, imagen, puntos);
    }

    private Ruta(String nombreRuta, String descripcion, Bitmap imagen, List<ParseGeoPoint> lugares) {
        this.nombreRuta = nombreRuta;
        this.descripcion = descripcion;
        this.imagenPrincipal = imagen;
        this.lugares = lugares;
    }

    public Bitmap getImagenPrincipal() {
        return imagenPrincipal;
    }

    public String getNombreRuta() {
        return nombreRuta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public List<ParseGeoPoint> getLugares() {
        return lugares;
    }

    public void guardar() {
        if (rutas == null) {
            cargarRutas();
        }
        rutas.add(this);
        try {
            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath()+"/rutas.dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeInt(rutas.size());

            // Escribir las rutas una por una
            for (Ruta r : rutas) {
                oos.writeObject(r.nombreRuta);
                oos.writeObject(r.descripcion);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                r.imagenPrincipal.compress(Bitmap.CompressFormat.PNG, 100, stream);
                oos.writeInt(stream.toByteArray().length);
                oos.write(stream.toByteArray());
                oos.writeInt(r.lugares.size());
                for (ParseGeoPoint p : r.lugares) {
                    oos.writeDouble(p.getLatitude());
                    oos.writeDouble(p.getLongitude());
                }
            }

            oos.close();
        } catch (IOException ex) {
            // TODO gestionar excepción
        }

    }

    public static List<Ruta> cargarRutas() {
        if (rutas == null) {
            try {
                FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath()+"/rutas.dat");
                ObjectInputStream ois = new ObjectInputStream(fis);
                rutas = new ArrayList<>();
                int numRutas = ois.readInt();

                for (int i = 0; i < numRutas; i++) {
                    String nombre = (String)ois.readObject();
                    String descr = (String)ois.readObject();
                    int imSize = ois.readInt();
                    byte[] imBytes = new byte[imSize];
                    ois.readFully(imBytes, 0, imSize);
                    Bitmap imagen = BitmapFactory.decodeByteArray(imBytes, 0, imBytes.length);
                    int numLugares = ois.readInt();
                    ArrayList<ParseGeoPoint> lugares = new ArrayList<>();
                    for (int j = 0; j < numLugares; j++) {
                        double lat = ois.readDouble();
                        double lng = ois.readDouble();
                        lugares.add(new ParseGeoPoint(lat, lng));
                    }
                    rutas.add(new Ruta(nombre, descr, imagen, lugares));
                }

                ois.close();
            }
            catch (IOException | ClassNotFoundException ex) {
                // No existen rutas previas o ha habido algún error al cargarlas. Devolver una lista vacía
                rutas = new ArrayList<>();
            }
        }
        return rutas;
    }
}
