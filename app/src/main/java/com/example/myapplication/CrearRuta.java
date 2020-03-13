package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;

public class CrearRuta extends AppCompatActivity {

    //private static final String[] LUGARES = new String[] {"Lugar 1", "Otro lugar", "Y otro lugar más"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_ruta);

        final AutoCompleteTextView buscadorLugares = findViewById(R.id.buscador_lugares);
        // Adapter por defecto
        // ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, LUGARES);
        // TODO una vez buscados los lugares en el servidor se puede eliminar esta lista
        ArrayList<Lugar> listaLugares = new ArrayList<>();
        listaLugares.add(new Lugar(0, "Lugar 1", "Abre a las 17:00"));
        listaLugares.add(new Lugar(0, "Otro lugar", "Abierto todo el día"));
        listaLugares.add(new Lugar(0, "Y otro lugar más", "Cierra a las 20:00"));
        LugarAutoCompleteAdapter adapter = new LugarAutoCompleteAdapter(this, listaLugares);
        buscadorLugares.setAdapter(adapter);

        // Establecer el adapter para la lista de lugares de la ruta
        RecyclerView recyclerView = findViewById(R.id.lista_lugares);
        final ArrayList<Lugar> ruta = new ArrayList<>();
        final ListaLugaresAdapter listaLugaresAdapter = new ListaLugaresAdapter(this, ruta, false);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(listaLugaresAdapter);

        buscadorLugares.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RecyclerView listaLugares = view.findViewById(R.id.lugares_ruta);
                Lugar lugar = (Lugar)adapterView.getItemAtPosition(i);
                // Añadir el punto a la ruta
                ruta.add(lugar);

                // Vaciar la barra de búsqueda y notificar al adaptador de que se ha insertado
                // un nuevo elemento en la lista
                buscadorLugares.setText("");
                listaLugaresAdapter.notifyDataSetChanged();
            }
        });
    }
}
