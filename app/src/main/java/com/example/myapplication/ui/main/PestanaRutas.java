package com.example.myapplication.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.navigation.fragment.NavHostFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ActivityInfo;
import com.example.myapplication.ItemRutas;
import com.example.myapplication.ListaRutas;
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
                Intent intent = new Intent (v.getContext(), ActivityInfo.class);
                startActivityForResult(intent, 0);
            }
        });

        RecyclerView recyclerView = root.findViewById(R.id.lista_rutas);
        List<ItemRutas> listaRutas = new ArrayList<>();
        // Añadir los lugares a la lista de la siguiente manera:
        //listaLugares.add(new ItemLista(imagenPrincipal, nombreLugar, horarioLugar));
        listaRutas.add(new ItemRutas(0, "Lugar 0", "Abre a las 17:00"));
        listaRutas.add(new ItemRutas(0, "Lugar 1", "Cerrado"));
        listaRutas.add(new ItemRutas(0, "Lugar 2", "Abierto todo el día"));

        ListaRutas adapterListaRutas = new ListaRutas(getActivity(), listaRutas);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity()); //IMPORTANTE
        recyclerView.setLayoutManager(manager); //IMPORTANTE
        recyclerView.setAdapter(adapterListaRutas);

        return root;
    }
}