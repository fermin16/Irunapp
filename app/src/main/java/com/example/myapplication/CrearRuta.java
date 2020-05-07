package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.EmptyRecyclerView;
import com.example.myapplication.ListaLugaresAdapter;
import com.example.myapplication.LugarAutoCompleteAdapter;
import com.example.myapplication.Modelos.lugar;
import com.example.myapplication.R;
import com.example.myapplication.Ruta;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;

public class CrearRuta extends AppCompatActivity {

    private Bitmap imagenRuta;
    public ArrayList<lugar> ruta;
    private ListaLugaresAdapter listaLugaresAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_ruta);

        final AutoCompleteTextView buscadorLugares = findViewById(R.id.buscador_lugares);

        ArrayList<lugar> listaLugares = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("lugar");
        query.findInBackground((queryresult, e) -> {
            if (e == null) {
                if(queryresult!=null && !queryresult.isEmpty()){
                    for(ParseObject obj: queryresult){
                        listaLugares.add((lugar)obj);
                    }
                    LugarAutoCompleteAdapter adapter = new LugarAutoCompleteAdapter(this, listaLugares);
                    buscadorLugares.setAdapter(adapter);
                }
                else
                    ((Activity)getApplicationContext()).runOnUiThread(() ->Toast.makeText(getApplicationContext(), getString(R.string.error_query), Toast.LENGTH_LONG).show());
            }
            else
                ((Activity)getApplicationContext()).runOnUiThread(() ->Toast.makeText(getApplicationContext(), getString(R.string.error_query), Toast.LENGTH_LONG).show());
        });


        // Establecer el adapter para la lista de lugares de la ruta
        RecyclerView recyclerView = findViewById(R.id.lista_lugares);
        ruta = new ArrayList<>();
        listaLugaresAdapter = new ListaLugaresAdapter(this, ruta, false);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(listaLugaresAdapter);
        ((EmptyRecyclerView)recyclerView).setEmptyView(findViewById(R.id.lista_vacia));

        buscadorLugares.setOnItemClickListener((adapterView, view, i, l) -> {

            // Obtener el punto seleccionado
            lugar lugar = (com.example.myapplication.Modelos.lugar)adapterView.getItemAtPosition(i);

            // Añadir el punto a la ruta
            ruta.add(lugar);

            // Vaciar la barra de búsqueda y notificar al adaptador de que se ha insertado
            // un nuevo elemento en la lista
            buscadorLugares.setText("");
            listaLugaresAdapter.notifyDataSetChanged();
        });

        FloatingActionButton botonCrearRuta = findViewById(R.id.boton_crear_ruta);
        botonCrearRuta.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.activar_permisos_almacenamiento, Toast.LENGTH_LONG).show();
                }
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
            else {
                // Comprobar que todos los campos han sido rellenados y mostrar un mensaje en caso de que alguno de ellos no lo esté
                EditText editTextNombre = findViewById(R.id.nombre_ruta);
                EditText editTextDescripcion = findViewById(R.id.descripcion_ruta);
                String nombre = editTextNombre.getText().toString();
                String descripcion = editTextDescripcion.getText().toString();

                if (nombre.trim().equalsIgnoreCase("")) {
                    editTextNombre.setError("Debes rellenar este campo");
                    return;
                }

                if (descripcion.trim().equalsIgnoreCase("")) {
                    editTextDescripcion.setError("Debes rellenar este campo");
                    return;
                }

                if (ruta.size() == 0) {
                    Toast.makeText(this, "Debes añadir al menos un lugar a tu ruta", Toast.LENGTH_LONG).show();
                    return;
                }

                // Si todos los campos se han rellenado, crear la ruta
                Ruta r = Ruta.crearRuta(nombre, descripcion, imagenRuta, ruta);
                r.guardar();
                finish();    // Cerrar la actividad

            }
        });

        ImageView botonImagen = findViewById(R.id.imagen_ruta);
        botonImagen.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.activar_permisos_almacenamiento, Toast.LENGTH_LONG).show();
                }
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
            else {
                Intent i = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
            imagenRuta = BitmapFactory.decodeFile(picturePath);
            imageView.setImageBitmap(imagenRuta);
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
            final lugar deletedItem = ruta.get(viewHolder.getAdapterPosition());
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

    public void updateList(ArrayList<lugar> newList){
        if(listaLugaresAdapter!= null) {
            // update data in our adapter
            listaLugaresAdapter.getData().clear();
            listaLugaresAdapter.getData().addAll(newList);
            // fire the event
            listaLugaresAdapter.notifyDataSetChanged();
        }
    }
}
