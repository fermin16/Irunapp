package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.api.directions.v5.DirectionsCriteria;
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
    private Button boton_menos;
    private Button boton_ruta;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Intent intent = getIntent();

        if(intent!=null){
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
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
                boton_menos = findViewById(R.id.boton_verMenos);
                boton_menos.setOnClickListener(v -> super.onBackPressed());

                boton_ruta = findViewById(R.id.boton_ruta);
                boton_ruta.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(getApplicationContext(), v);
                    popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                    popup.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.modoCoche) {
                            Map.rutaSeleccionada = DirectionsCriteria.PROFILE_DRIVING;
                        } else if (item.getItemId() == R.id.modoBicicleta) {
                            Map.rutaSeleccionada = DirectionsCriteria.PROFILE_CYCLING;
                        } else {
                            Map.rutaSeleccionada = DirectionsCriteria.PROFILE_WALKING;
                        }
                        super.onBackPressed();
                        return true;
                    });
                    MenuPopupHelper menuHelper = new MenuPopupHelper(getApplicationContext(), (MenuBuilder) popup.getMenu(), v);
                    menuHelper.setForceShowIcon(true);
                    menuHelper.show();
                });
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
