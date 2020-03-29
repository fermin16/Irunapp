package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.Modelos.Lugar;

import java.util.ArrayList;
import java.util.List;

public class LugarAutoCompleteAdapter extends ArrayAdapter<Lugar> {

    private List<Lugar> todosLugares;   // Lista de todos los lugares

    public LugarAutoCompleteAdapter(@NonNull Context context, @NonNull List<Lugar> lugares) {
        super(context, 0, lugares);
        todosLugares = new ArrayList<>(lugares);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filterLugares;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_sugerencia_buscar_rutas, parent, false
            );
        }

        TextView textViewLugar = convertView.findViewById(R.id.nombre_lugar);
        ImageView imageViewLugar = convertView.findViewById(R.id.imagen_principal);

        Lugar lugar = getItem(position);

        if (lugar != null) {
            textViewLugar.setText(lugar.getNombre());

            byte[] foto = lugar.getFoto();
            Bitmap fotoBmp = BitmapFactory.decodeByteArray(foto, 0, foto.length);

            imageViewLugar.setImageBitmap(Bitmap.createScaledBitmap(
                    fotoBmp,
                    imageViewLugar.getWidth(),
                    imageViewLugar.getHeight(),
                    false
            ));
        }

        return convertView;
    }

    private Filter filterLugares = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            // TODO modificar para que la búsqueda se realice en el servidor
            FilterResults resultados = new FilterResults();
            List<Lugar> sugerencias = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                sugerencias.addAll(todosLugares);
            }
            else {
                String filtro = charSequence.toString().toLowerCase().trim();

                for (Lugar l : todosLugares) {
                    if (l.getNombre().toLowerCase().contains(filtro)) {
                        sugerencias.add(l);
                    }
                }
            }
            resultados.values = sugerencias;
            resultados.count = sugerencias.size();

            return resultados;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            clear();
            addAll((List)filterResults.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Lugar)resultValue).getNombre();
        }
    };
}