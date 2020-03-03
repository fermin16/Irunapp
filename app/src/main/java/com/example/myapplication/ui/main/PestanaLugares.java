package com.example.myapplication.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ItemLista;
import com.example.myapplication.ListaLugares;
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
        List<ItemLista> listaLugares = new ArrayList<>();
        // Añadir los lugares a la lista de la siguiente manera:
        //listaLugares.add(new ItemLista(imagenPrincipal, nombreLugar, horarioLugar));
        listaLugares.add(new ItemLista(0, "Lugar 0", "Abre a las 17:00"));
        listaLugares.add(new ItemLista(0, "Lugar 1", "Cerrado"));
        listaLugares.add(new ItemLista(0, "Lugar 2", "Abierto todo el día"));

        ListaLugares adapterListaLugares = new ListaLugares(getActivity(), listaLugares);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity()); //IMPORTANTE
        recyclerView.setLayoutManager(manager); //IMPORTANTE
        recyclerView.setAdapter(adapterListaLugares);

        return root;
    }
}