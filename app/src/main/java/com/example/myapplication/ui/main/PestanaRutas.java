package com.example.myapplication.ui.main;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.CrearRuta;
import com.example.myapplication.EmptyRecyclerView;
import com.example.myapplication.Ruta;
import com.example.myapplication.ListaRutasAdapter;
import com.example.myapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;


public class PestanaRutas extends Fragment {


    private RecyclerView recyclerView;
    private FloatingActionButton btn;
    ListaRutasAdapter listaRutasAdapter;
    private View root;

    private static PestanaRutas pestana = null;


    public static PestanaRutas getPestana() {
        if (pestana == null) {
            pestana = new PestanaRutas();
        }
        return pestana;
    }

    public static void actualizarRutas() {
        getPestana().listaRutasAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_lista_rutas, container, false);

        btn = root.findViewById(R.id.floatingActionButton);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent (v.getContext(), CrearRuta.class);
            startActivityForResult(intent, 0);
        });

        recyclerView = root.findViewById(R.id.lista_rutas);

        List<Ruta> listaRutas = Ruta.cargarRutas();
        // TODO Añadir los lugares a la lista de la siguiente manera:
        // listaRutas.add(new Ruta(0, "Lugar 0", "Abre a las 17:00"));

        listaRutasAdapter = new ListaRutasAdapter(getContext(), listaRutas);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.numero_columnas));

        recyclerView.setLayoutManager(manager); //IMPORTANTE
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(listaRutasAdapter);
        ((EmptyRecyclerView)recyclerView).setEmptyView(root.findViewById(R.id.lista_vacia));

        return root;
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {

            // Obtener el ítem eliminado y eliminarlo de la lista
            final int deletedIndex = viewHolder.getAdapterPosition();
            Ruta eliminada = Ruta.eliminarRuta(deletedIndex);

            // Notificar la eliminación
            listaRutasAdapter.notifyDataSetChanged();

            // Mostrar el mensaje de deshacer en caso de que se haya eliminado accidentalmente
            Snackbar snackbar = Snackbar.make(recyclerView, getResources().getString(R.string.ruta_eliminada), Snackbar.LENGTH_LONG);
            snackbar.setAction(getResources().getString(R.string.deshacer), new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Volver a añadir el item a la lista
                    listaRutasAdapter.restoreItem(eliminada, deletedIndex);
                    Ruta.guardarRutas();
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();

            // Guardar las rutas
            Ruta.guardarRutas();
        }

        @Override
        public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
            final View foregroundView = ((ListaRutasAdapter.MyViewHolder)viewHolder).viewFrente;
            getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            final View foregroundView = ((ListaRutasAdapter.MyViewHolder)viewHolder).viewFrente;
            getDefaultUIUtil().clearView(foregroundView);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            final View foregroundView = ((ListaRutasAdapter.MyViewHolder)viewHolder).viewFrente;

            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive);
        }
    };
}