package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;

public class activityInfo extends AppCompatActivity {

    //Componentes Visuales del activity
    private TextView nombreLugar;
    private TextView descripcion;
    private TextView direccion;
    private TextView horario;
    private TextView precio;
    private TextView contacto;
    private TextView web;
    private ImageView imagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Intent intent = getIntent();

        if(intent!=null){
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                nombreLugar = findViewById(R.id.nombre_lugar);
                nombreLugar.setText(bundle.getString(String.valueOf(R.string.bundle_titulo)));
                descripcion = findViewById(R.id.descripcion);
                descripcion.setText(bundle.getString(String.valueOf(R.string.bundle_descripcion)));
                direccion = findViewById(R.id.direccion);
                direccion.setText(bundle.getString(String.valueOf(R.string.bundle_direccion)));
                horario = findViewById(R.id.horario);
                horario.setText("PRUEBA HORARIO");
                precio = findViewById(R.id.precio);
                precio.setText("PRUEBA PRECIO");
                contacto = findViewById(R.id.contacto);
                contacto.setText("PRUEBA CONTACTO");
                web = findViewById(R.id.web);
                web.setText("PRUEBA WEB");
                imagen = findViewById(R.id.imagen_principal);
                byte[] imagen_bytes = bundle.getByteArray(String.valueOf(R.string.bundle_imagen));
                imagen.setImageBitmap(BitmapFactory.decodeByteArray(imagen_bytes, 0, imagen_bytes.length));
            }
            else {
                Toast.makeText(this,R.string.punto_no_recibido, Toast.LENGTH_LONG).show();
                finish();
            }
        }
        else{
            Toast.makeText(this,R.string.punto_no_recibido, Toast.LENGTH_LONG).show();
            finish();
        }

    }
}
