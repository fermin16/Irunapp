package com.example.myapplication.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Ruta;
import com.example.myapplication.ListaRutasAdapter;
import com.example.myapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class PestanaRutas extends Fragment {

    private static PestanaRutas pestana = null;


    public static PestanaRutas getPestana() {
        if (pestana == null) {
            pestana = new PestanaRutas();
        }
        return pestana;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_lista_rutas, container, false);

        FloatingActionButton btn = (FloatingActionButton) root.findViewById(R.id.floatingActionButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (v.getContext(), CrearRuta.class);
                startActivityForResult(intent, 0);
            }
        });

        RecyclerView recyclerView = root.findViewById(R.id.lista_rutas);
        List<Ruta> listaRutas = new ArrayList<>();
        // Añadir los lugares a la lista de la siguiente manera:
        //listaLugares.add(new ItemLista(imagenPrincipal, nombreLugar, horarioLugar));
        listaRutas.add(new Ruta(0, "Lugar 0", "Abre a las 17:00"));
        listaRutas.add(new Ruta(0, "Lugar 1", "Cerrado"));
        listaRutas.add(new Ruta(0, "Lugar 2", "Abierto todo el día"));

        ListaRutasAdapter adapterListaRutasAdapter = new ListaRutasAdapter(getActivity(), listaRutas);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity()); //IMPORTANTE
        recyclerView.setLayoutManager(manager); //IMPORTANTE
        recyclerView.setAdapter(adapterListaRutasAdapter);

        return root;
    }
}