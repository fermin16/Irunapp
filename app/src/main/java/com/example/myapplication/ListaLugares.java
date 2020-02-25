package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListaLugares extends RecyclerView.Adapter<ListaLugares.myViewHolder> {

    Context mContext;
    List<ItemLista> mData;

    public ListaLugares(Context mContext, List<ItemLista> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.activity_tarjeta, parent, false);
        return new myViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        holder.imagenPrincipal.setImageResource(mData.get(position).getImagenPrincipal());
        holder.nombreLugar.setText(mData.get(position).getNombreLugar());
        holder.horario.setText(mData.get(position).getHorario());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {

        ImageView imagenPrincipal;
        TextView nombreLugar, horario;


        public myViewHolder(View itemView){
            super(itemView);
            imagenPrincipal = itemView.findViewById(R.id.imagen_principal);
            nombreLugar = itemView.findViewById(R.id.nombre_lugar);
            horario = itemView.findViewById(R.id.horario_lugar);

        }
    }

}

