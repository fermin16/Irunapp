package com.example.myapplication.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ListaLugaresAdapter;
import com.example.myapplication.Modelos.Lugar;
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
        // listaLugares.add(LUGAR);

        ListaLugaresAdapter listaLugaresAdapter = new ListaLugaresAdapter(getActivity(), listaLugares, true);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.numero_columnas));

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(listaLugaresAdapter);

        return root;
    }
}