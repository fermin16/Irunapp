package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Modelos.lugar;

import java.util.List;

public class ListaLugaresAdapter extends RecyclerView.Adapter<ListaLugaresAdapter.MyViewHolder> {

    Context mContext;
    List<lugar> mData;
    boolean tarjetaGrande;

    public ListaLugaresAdapter(Context mContext, List<lugar> mData, boolean tarjetaGrande) {
        this.mContext = mContext;
        this.mData = mData;
        this.tarjetaGrande = tarjetaGrande;
    }

    public void restoreItem(lugar item, int position) {
        mData.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v;
        if (this.tarjetaGrande) {
            v = inflater.inflate(R.layout.tarjeta_lugar, parent, false);
        }
        else {
            v = inflater.inflate(R.layout.tarjeta_lugar_pequena, parent, false);
        }

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        byte[] foto = mData.get(position).getFoto();
        if(foto != null) {
            Bitmap fotoBmp = BitmapFactory.decodeByteArray(foto, 0, foto.length);

            holder.imagenPrincipal.setImageBitmap(Bitmap.createScaledBitmap(
                    fotoBmp,
                    holder.imagenPrincipal.getWidth(),
                    holder.imagenPrincipal.getHeight(),
                    false
            ));
        }
        holder.nombreLugar.setText(mData.get(position).getNombre());
        if (tarjetaGrande) {
            holder.horario.setText(mData.get(position).getHorario());
            holder.botonVerMas.setOnClickListener(view -> {

                // Ordinary Intent for launching a new activity
                Intent intent = new Intent(mContext, activityInfo.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //Animaciones para la transicion:
                // Obtener la vistas de los objetos para la animacion:
                View cardview = view.findViewById(R.id.cardLugarGrande);

                Pair<View, String> imagen = Pair.create(cardview.findViewById(R.id.imagen_principal), mContext.getString(R.string.id_transicion_imagen_lugar));
                Pair<View, String> nombre = Pair.create(cardview.findViewById(R.id.nombre_lugar_grande), mContext.getString(R.string.id_transicion_nombre_lugar));
                Pair<View, String> horario = Pair.create(cardview.findViewById(R.id.horario_lugar), mContext.getString(R.string.id_transicion_horario_lugar));
                Pair<View, String> boton_mas = Pair.create(cardview.findViewById(R.id.boton_vm), mContext.getString(R.string.id_transicion_botonMas_lugar));
                Pair<View, String> boton_ruta = Pair.create(cardview.findViewById(R.id.boton_cm), mContext.getString(R.string.id_transicion_botonRuta_lugar));

                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(mContext., imagen,nombre, horario, boton_mas,boton_ruta);

                //Información de la tarjeta seleccionada
                Bundle bundle = new Bundle(); //Crear bundle para enviar coordenadas
                bundle.putString(String.valueOf(R.string.bundle_direccion),((lugar)cardSelected).getDireccion()); //Guardar direccion
                bundle.putString(String.valueOf(R.string.bundle_titulo),((lugar)cardSelected).getNombre()); //Guardar nombre
                bundle.putString(String.valueOf(R.string.bundle_descripcion),((lugar)cardSelected).getDescripcion()); //Guardar descripcion
                bundle.putString(String.valueOf(R.string.bundle_horario),((lugar)cardSelected).getHorario()); //Guardar horario
                bundle.putString(String.valueOf(R.string.bundle_web),((lugar)cardSelected).getWeb()); //Guardar web
                // TODO bundle.putString(String.valueOf(R.string.bundle_precio),((Lugar)cardSelected).getPrecio()); //Guardar precio
                bundle.putString(String.valueOf(R.string.bundle_contacto),((lugar)cardSelected).getContacto()); //Guardar contacto
                bundle.putByteArray(String.valueOf(R.string.bundle_imagen),((lugar)cardSelected).getFoto());

                intent.putExtras(bundle);

                startActivity(intent, options.toBundle());
            });
            holder.botonComoLlegar.setOnClickListener(view -> {
                // TODO cargar la ruta en el mapa para saber como llegar al lugar
                // TODO cambiar a la pestaña de mapa
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public List<lugar> getData(){
        return mData;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imagenPrincipal;
        TextView nombreLugar, horario;
        Button botonVerMas, botonComoLlegar;
        public RelativeLayout viewFondo;
        public LinearLayout viewFrente;

        public MyViewHolder(View itemView){
            super(itemView);

            imagenPrincipal = itemView.findViewById(R.id.imagen_principal);
            nombreLugar = itemView.findViewById(R.id.nombre_lugar);
            if (tarjetaGrande) {
                botonVerMas = itemView.findViewById(R.id.boton_vm);
                botonComoLlegar = itemView.findViewById(R.id.boton_cm);
                horario = itemView.findViewById(R.id.horario_lugar);
            }
            else {
                viewFrente = itemView.findViewById(R.id.item);
                viewFondo = itemView.findViewById(R.id.fondo);
            }
        }
    }
}

