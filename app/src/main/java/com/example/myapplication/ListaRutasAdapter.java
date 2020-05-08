package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ui.main.PestanaMapa;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.parse.ParseGeoPoint;

import java.util.List;

public class ListaRutasAdapter extends RecyclerView.Adapter<ListaRutasAdapter.MyViewHolder> implements mensajesHandler{

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

    @SuppressLint("RestrictedApi")
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
            PopupMenu popup = new PopupMenu(mContext, view);
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.modoCoche) {
                    PestanaMapa.rutaSeleccionada = DirectionsCriteria.PROFILE_DRIVING;
                } else if (item.getItemId() == R.id.modoBicicleta) {
                    PestanaMapa.rutaSeleccionada = DirectionsCriteria.PROFILE_CYCLING;
                } else {
                    PestanaMapa.rutaSeleccionada = DirectionsCriteria.PROFILE_WALKING;
                }
                Message msg = new Message();
                msg.obj = mData.get(position).getLugares();
                msg.what = MSG_RUTA_MULTIPLE;
                PestanaMapa.manejador.sendMessage(msg);

                //Movernos al tab del mapa:
                TabLayout tabhost = (TabLayout) ((Activity)mContext).findViewById(R.id.tabs);
                tabhost.getTabAt(1).select();
                return true;
            });
            MenuPopupHelper menuHelper = new MenuPopupHelper(mContext, (MenuBuilder) popup.getMenu(), view);
            menuHelper.setForceShowIcon(true);
            menuHelper.show();
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

