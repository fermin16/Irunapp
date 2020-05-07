package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ListaRutasAdapter extends RecyclerView.Adapter<ListaRutasAdapter.MyViewHolder> {

    Context mContext;
    List<Ruta> mData;

    public ListaRutasAdapter(Context mContext, List<Ruta> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    public void restoreItem(Ruta item, int position) {
        mData.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.tarjeta_ruta, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Bitmap imagenRuta = mData.get(position).getImagenPrincipal();

        // Si no hay imagen, establecer la imagen por defecto para las rutas
        if (imagenRuta == null) {
            // TODO establecer la imagen por defecto para la ruta
        }
        else {
            holder.imagenPrincipal.setImageBitmap(imagenRuta);
        }
        holder.nombreRuta.setText(mData.get(position).getNombreRuta());
        holder.descripcion.setText(mData.get(position).getDescripcion());
        holder.botonVerMapa.setOnClickListener(view -> {
            // TODO cargar la ruta
            // mostrar la ruta en el mapa
            // TODO cambiar a la pestaña de mapa
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout viewFondo;
        public RelativeLayout viewFrente;

        ImageView imagenPrincipal;
        TextView nombreRuta, descripcion;
        Button botonVerMapa;

        public MyViewHolder(View itemView){
            super(itemView);
            imagenPrincipal = itemView.findViewById(R.id.imagen_principal);
            nombreRuta = itemView.findViewById(R.id.nombre_ruta);
            descripcion = itemView.findViewById(R.id.descripcion);
            botonVerMapa = itemView.findViewById(R.id.boton_cm);
            viewFrente = itemView.findViewById(R.id.item);
            viewFondo = itemView.findViewById(R.id.fondo);
        }
    }

}

