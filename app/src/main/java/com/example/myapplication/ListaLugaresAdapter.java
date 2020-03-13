package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ui.main.CrearRuta;

import java.util.List;

public class ListaLugaresAdapter extends RecyclerView.Adapter<ListaLugaresAdapter.myViewHolder> {

    Context mContext;
    List<Lugar> mData;
    boolean tarjetaGrande;

    public ListaLugaresAdapter(Context mContext, List<Lugar> mData, boolean tarjetaGrande) {
        this.mContext = mContext;
        this.mData = mData;
        this.tarjetaGrande = tarjetaGrande;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v;
        if (this.tarjetaGrande) {
            v = inflater.inflate(R.layout.tarjeta_lugar, parent, false);
        }
        else {
            v = inflater.inflate(R.layout.tarjeta_pequena, parent, false);
        }

        return new myViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final myViewHolder holder, int position) {
        holder.imagenPrincipal.setImageResource(mData.get(position).getImagenPrincipal());
        holder.nombreLugar.setText(mData.get(position).getNombreLugar());
        if (tarjetaGrande) {
            holder.horario.setText(mData.get(position).getHorario());
            holder.botonVerMas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO cargar los datos en activity_info
                    // TODO mostrar activity_info
                }
            });
            holder.botonComoLlegar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO cargar la ruta en el mapa para saber como llegar al lugar
                    // TODO cambiar a la pestaña de mapa
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {

        ImageView imagenPrincipal;
        TextView nombreLugar, horario;
        Button botonVerMas, botonComoLlegar;

        public myViewHolder(View itemView){
            super(itemView);

            imagenPrincipal = itemView.findViewById(R.id.imagen_principal);
            nombreLugar = itemView.findViewById(R.id.nombre_lugar);
            if (tarjetaGrande) {
                botonVerMas = itemView.findViewById(R.id.boton_vm);
                botonComoLlegar = itemView.findViewById(R.id.boton_cm);
                horario = itemView.findViewById(R.id.horario_lugar);
            }
        }
    }

}

