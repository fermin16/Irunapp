package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Modelos.lugar;
import com.example.myapplication.ui.main.PestanaMapa;
import com.google.android.material.tabs.TabLayout;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.parse.ParseGeoPoint;

import java.util.ArrayList;
import java.util.List;

public class ListaLugaresAdapter extends RecyclerView.Adapter<ListaLugaresAdapter.MyViewHolder> implements mensajesHandler{

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

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        lugar place = mData.get(position);
        if(place != null) {
            byte[] foto = place.getFoto();
            if (foto != null) {
                Bitmap fotoBmp = BitmapFactory.decodeByteArray(foto, 0, foto.length);

                holder.imagenPrincipal.setImageBitmap(Bitmap.createScaledBitmap(
                        fotoBmp,
                        holder.imagenPrincipal.getWidth(),
                        holder.imagenPrincipal.getHeight(),
                        false
                ));
            }
            String nombre_lugar = place.getNombre();
            if(nombre_lugar !=null) {
                holder.nombreLugar.setText(nombre_lugar);
                if (tarjetaGrande) {
                    String horario_lugar = place.getHorario();
                    if(horario_lugar !=null) {
                        holder.horario.setText(horario_lugar);
                        holder.botonVerMas.setOnClickListener(view -> {

                            // Ordinary Intent for launching a new activity
                            Intent intent = new Intent(mContext, activityInfo.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //Animaciones para la transicion:
                            // Obtener la vistas de los objetos para la animacion:
                            View itemview = holder.getItemView();

                            Pair<View, String> imagen = Pair.create(itemview.findViewById(R.id.imagen_principal), mContext.getString(R.string.id_transicion_imagen));
                            Pair<View, String> nombre = Pair.create(itemview.findViewById(R.id.nombre_lugar_grande), mContext.getString(R.string.id_transicion_nombre));
                            Pair<View, String> horario = Pair.create(itemview.findViewById(R.id.horario_lugar), mContext.getString(R.string.id_transicion_horario));
                            Pair<View, String> boton_mas = Pair.create(itemview.findViewById(R.id.boton_vm), mContext.getString(R.string.id_transicion_boton_mas));
                            Pair<View, String> boton_ruta = Pair.create(itemview.findViewById(R.id.boton_cm), mContext.getString(R.string.id_transicion_boton_ruta));

                            ActivityOptionsCompat options =
                                    ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, imagen, nombre, horario, boton_mas, boton_ruta);

                            //Información de la tarjeta seleccionada
                            Bundle bundle = new Bundle(); //Crear bundle para enviar coordenadas
                            bundle.putString(String.valueOf(R.string.bundle_direccion), place.getDireccion()); //Guardar direccion
                            bundle.putString(String.valueOf(R.string.bundle_titulo), place.getNombre()); //Guardar nombre
                            bundle.putString(String.valueOf(R.string.bundle_descripcion), place.getDescripcion()); //Guardar descripcion
                            bundle.putString(String.valueOf(R.string.bundle_horario), place.getHorario()); //Gua
                            bundle.putString(String.valueOf(R.string.bundle_web), place.getWeb()); //Guardar webrdar horario
                            // TODO bundle.putString(String.valueOf(R.string.bundle_precio),((Lugar)cardSelected).getPrecio()); //Guardar precio
                            bundle.putString(String.valueOf(R.string.bundle_contacto), place.getContacto()); //Guardar contacto
                            bundle.putByteArray(String.valueOf(R.string.bundle_imagen), place.getFoto());

                            intent.putExtras(bundle);

                            Message msg = new Message();
                            ParseGeoPoint punto = place.getLocalizacion();
                            msg.obj = new LatLng(punto.getLatitude(),punto.getLongitude());
                            msg.what = MSG_AMPLIA_CARD_LUGARES;
                            PestanaMapa.manejador.sendMessage(msg);

                            mContext.startActivity(intent, options.toBundle());
                        });
                        holder.botonComoLlegar.setOnClickListener(view -> {

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
                                    ParseGeoPoint punto = place.getLocalizacion();
                                    msg.obj = new LatLng(punto.getLatitude(),punto.getLongitude());
                                    msg.what = MSG_RUTA_TAB_LUGARES;
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
                }
            }
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
        private View itemView;

        public View getItemView(){
            return itemView;
        }

        public MyViewHolder(View itemView){
            super(itemView);

            this.itemView = itemView;

            imagenPrincipal = itemView.findViewById(R.id.imagen_principal);

            if (tarjetaGrande) {
                nombreLugar = itemView.findViewById(R.id.nombre_lugar_grande);
                botonVerMas = itemView.findViewById(R.id.boton_vm);
                botonComoLlegar = itemView.findViewById(R.id.boton_cm);
                horario = itemView.findViewById(R.id.horario_lugar);
            }
            else {
                nombreLugar = itemView.findViewById(R.id.nombre_lugar);
                viewFrente = itemView.findViewById(R.id.item);
                viewFondo = itemView.findViewById(R.id.fondo);
            }
        }
    }
}

