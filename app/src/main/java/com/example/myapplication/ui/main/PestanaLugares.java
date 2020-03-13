package com.example.myapplication.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Lugar;
import com.example.myapplication.ListaLugaresAdapter;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;


public class PestanaLugares extends Fragment {

    private static PestanaLugares pestana = null;

    public static PestanaLugares getPestana() {
        if (pestana == null) {
            pestana = new PestanaLugares();
        }
        return pestana;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_lista_lugares, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.lista_lugares);
        List<Lugar> listaLugares = new ArrayList<>();

        // TODO Añadir los lugares a la lista de la siguiente manera:
        //listaLugares.add(new ItemLista(imagenPrincipal, nombreLugar, horarioLugar));
        listaLugares.add(new Lugar(0, "Lugar 0", "Abre a las 17:00"));
        listaLugares.add(new Lugar(0, "Lugar 1", "Cerrado"));
        listaLugares.add(new Lugar(0, "Lugar 2", "Abierto todo el día"));

        ListaLugaresAdapter listaLugaresAdapter = new ListaLugaresAdapter(getActivity(), listaLugares, true);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(listaLugaresAdapter);

        return root;
    }
}