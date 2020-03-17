package com.example.myapplication.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.myapplication.EmptyRecyclerView;
import com.example.myapplication.ListaLugaresAdapter;
import com.example.myapplication.Lugar;
import com.example.myapplication.LugarAutoCompleteAdapter;
import com.example.myapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class CrearRuta extends AppCompatActivity {

    public ArrayList<Lugar> ruta;
    private ListaLugaresAdapter listaLugaresAdapter;

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
        ruta = new ArrayList<>();
        listaLugaresAdapter = new ListaLugaresAdapter(this, ruta, false);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(listaLugaresAdapter);
        ((EmptyRecyclerView)recyclerView).setEmptyView(findViewById(R.id.lista_vacia));

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

        FloatingActionButton botonCrearRuta = findViewById(R.id.boton_crear_ruta);
        botonCrearRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO crear la ruta
                finish();    // Cerrar la actividad
            }
        });

        ImageView botonImagen = findViewById(R.id.imagen_ruta);
        botonImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = findViewById(R.id.imagen_ruta);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
            // Eliminar el item seleccionado
            final RecyclerView recyclerView = findViewById(R.id.lista_lugares);

            // Obtener el ítem eliminado
            final Lugar deletedItem = ruta.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // Eliminarlo de la lista
            ruta.remove(viewHolder.getAdapterPosition());
            listaLugaresAdapter.notifyDataSetChanged();

            // Mostrar el mensaje de deshacer en caso de que se haya eliminado accidentalmente
            Snackbar snackbar = Snackbar.make(recyclerView, getResources().getString(R.string.lugar_eliminado), Snackbar.LENGTH_LONG);
            snackbar.setAction(getResources().getString(R.string.deshacer), new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Volver a añadir el item a la lista
                    listaLugaresAdapter.restoreItem(deletedItem, deletedIndex);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();

        }

        @Override
        public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
            final View foregroundView = ((ListaLugaresAdapter.MyViewHolder)viewHolder).viewFrente;
            getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            final View foregroundView = ((ListaLugaresAdapter.MyViewHolder)viewHolder).viewFrente;
            getDefaultUIUtil().clearView(foregroundView);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            final View foregroundView = ((ListaLugaresAdapter.MyViewHolder)viewHolder).viewFrente;

            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive);
        }
    };
}
