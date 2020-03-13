package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListaRutasAdapter extends RecyclerView.Adapter<ListaRutasAdapter.myViewHolder> {

    Context mContext;
    List<Ruta> mData;

    public ListaRutasAdapter(Context mContext, List<Ruta> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.tarjeta_ruta, parent, false);
        return new myViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        holder.imagenPrincipal.setImageResource(mData.get(position).getImagenPrincipal());
        holder.nombreRuta.setText(mData.get(position).getNombreRuta());
        holder.descripcion.setText(mData.get(position).getDescripcion());
        holder.botonVerMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO cargar la ruta
                // mostrar la ruta en el mapa
                // TODO cambiar a la pestaña de mapa
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {

        ImageView imagenPrincipal;
        TextView nombreRuta, descripcion;
        Button botonVerMapa;

        public myViewHolder(View itemView){
            super(itemView);
            imagenPrincipal = itemView.findViewById(R.id.imagen_principal);
            nombreRuta = itemView.findViewById(R.id.nombre_ruta);
            descripcion = itemView.findViewById(R.id.descripcion);
            botonVerMapa = itemView.findViewById(R.id.boton_cm);
        }
    }

}

