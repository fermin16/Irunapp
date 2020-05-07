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

import com.example.myapplication.Modelos.lugar;

import java.util.ArrayList;
import java.util.List;

public class LugarAutoCompleteAdapter extends ArrayAdapter<lugar> {

    private List<lugar> todosLugares;   // Lista de todos los lugares

    public LugarAutoCompleteAdapter(@NonNull Context context, @NonNull List<lugar> lugares) {
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

        lugar lugar = getItem(position);

        if (lugar != null) {
            textViewLugar.setText(lugar.getNombre());

            byte[] foto = lugar.getFoto();
            if(foto != null) {
               Bitmap fotoBmp = BitmapFactory.decodeByteArray(foto, 0, foto.length);

               imageViewLugar.setImageBitmap(Bitmap.createScaledBitmap(
                       fotoBmp,
                       imageViewLugar.getWidth(),
                       imageViewLugar.getHeight(),
                       false
               ));
           }
        }

        return convertView;
    }

    private Filter filterLugares = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults resultados = new FilterResults();
            List<lugar> sugerencias = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                sugerencias.addAll(todosLugares);
            }
            else {
                String filtro = charSequence.toString().toLowerCase().trim();

                for (lugar l : todosLugares) {
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
            return ((lugar)resultValue).getNombre();
        }
    };
}