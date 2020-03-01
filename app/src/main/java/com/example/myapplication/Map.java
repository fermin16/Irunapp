package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Map extends AppCompatActivity {

    //Macros:
    private final int ACTIVAR_UBICACION = 0; //Codigo de la actividad que nos servira para saber si el usuario a activado o no la ubicacion
    private final double ZOOM_FIND = 15.0; //Zoom al pulsar el boton de localizacion


    //Mapa y estilos del mapa:
    private MapView mapView;
    private Style.Builder BasicStyle;
    private Style.Builder SatelliteStyle;
    private Style.OnStyleLoaded loadedStyle; //Variable que almacenara el estilo cargado en el mapa con sus añadidos
    private boolean style; //Varible que indica si el style es el basico (false) o satelite (true)
    private Semaphore loading_style; //Variable que determina si el style del mapa está cargando.
    private MapboxMap mapboxMap;

    //Menu de boton flotante y sus botones:
    private FloatingActionMenu BotonFlotante;
    private FloatingActionButton BotonMapa;
    private FloatingActionButton BotonLocalizacion;

    //Componentes para funcionalidades del mapa:
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent; //Variable para obtener la localizacion actual
    private AlertDialog alertDialog; //Guardar una variable para el alertDialog que permitira cerrarlo cuando deba crearse uno nuevo
    private BroadcastReceiver mGpsSwitchStateReceiver; //BoradcastReciver para saber cuando un usuario activa o desactiva gps

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.map_token));
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        loading_style = new Semaphore(1);

        //Instanciamos el Boton flotante con el mnú y los botones de localización y cambio de estilo:
        BotonFlotante = (FloatingActionMenu) findViewById(R.id.BotonFlotante);
        BotonFlotante.setClosedOnTouchOutside(true);
        BotonFlotante.bringToFront();

        BotonMapa = (FloatingActionButton) findViewById(R.id.BotonCambioMapa);
        BotonMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!style){
                    changeStyle(SatelliteStyle,loadedStyle);
                    style = true;
                }
                else {
                    changeStyle(BasicStyle,loadedStyle);
                    style = false;
                }
            }
        });

        BotonLocalizacion = (FloatingActionButton) findViewById(R.id.FindMe);
        BotonLocalizacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findMe();
            }
        });

        //Inicializar los dos estilos de mapa:
        BasicStyle = new Style.Builder().fromUri(getString(R.string.map_style_basic));
        SatelliteStyle = new Style.Builder().fromUri(getString(R.string.map_style_road));

        //Registrar el broadcastReciever (tambien se pede hacer desde el manifest pero para ello deberiamos crear una clase que extienda a BroadcastReciver)
        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        //Crear un BroadCastReciever para saber cuando un usuario activa o desactiva los servicios de ubicacion:
        mGpsSwitchStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")){ //Si han cambiado los proveedores de servicio o de locaizacion
                    //Comprobar los servicios de red y ubicacion:
                    if(!checkLocationServices()) {
                        Toast.makeText(context,getString(R.string.serviciosDesactivados),Toast.LENGTH_LONG).show();
                    }
                    else{ //Si se han activado
                        findMe(); //Mostrar localizacion
                    }
                }
            }
        };

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                Map.this.mapboxMap = mapboxMap;

                //Iinicializar la variable que almacenara los añadidos del mapa
                loadedStyle = new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                    }
                };
                List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
                symbolLayerIconFeatureList.add(Feature.fromGeometry(
                        Point.fromLngLat(-57.225365, -33.213144)));
                symbolLayerIconFeatureList.add(Feature.fromGeometry(
                        Point.fromLngLat(-54.14164, -33.981818)));
                symbolLayerIconFeatureList.add(Feature.fromGeometry(
                        Point.fromLngLat(-56.990533, -30.583266)));
                changeStyle(BasicStyle,loadedStyle);
            }
        });
    }

    /* Metodo que se encarga de activar el elemento de localizacion, para ello pide permisos al usuario
     * (en caso de no haberlos pedido antes) y si el usuario acepta podra utilizar el mapa en caso
     * contrario se terminara el activity.*/
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        //Comprobar si se han concedido los permisos de ubicacion:
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            if(checkLocationServices()) {//Comprobar si estan activados los servicios de red y ubicacion.
                //Obtener una instancia del componente de localizacion
                 locationComponent = mapboxMap.getLocationComponent();

                // Activate with a built LocationComponentActivationOptions object
                locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, mapboxMap.getStyle()).build());
                if(checkLocationServices()) {//Comprobar si estan activados los servicios de red y ubicacion.
                    //Hacer el indicador visible
                    locationComponent.setLocationComponentEnabled(true);

                    //Establecer el compas (brujula)
                    locationComponent.setRenderMode(RenderMode.COMPASS);
                }
            }
        }
        else if(shouldShowRequestPermissionRationale(LOCATION_SERVICE)){ //Si no se han concedido permisos y el usuario no ha marcado la opcion no volver a preguntar, solicitarlos
            permissionsManager = new PermissionsManager((PermissionsListener) this);
            permissionsManager.requestLocationPermissions(this);
        }
        else{ //Si el usuario marcó la opción no volver a solicitar permisos: (Requiere API 23 o mayor):
            finish();
            Toast.makeText(this,getString(R.string.activarPermisos),Toast.LENGTH_LONG).show();
        }
    }

    /* Metodo que permite chequear los servicios de acceso para ver si estan activados o no. Devuelve
     *  falso si estan desactivados y verdadero en caso contrario.*/
    public boolean checkLocationServices(){
        LocationManager lm = (LocationManager)this.getSystemService(LOCATION_SERVICE);
        boolean gps = false;
        boolean network = false;

        try {
            gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}
        if(alertDialog != null)
            alertDialog.dismiss();
        if(!gps){
            //Notificar al usuario que tiene desactivados los servicios de ubicacion.
            alertDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.gps_desactivado)
                    .setPositiveButton(R.string.activar_gps, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),ACTIVAR_UBICACION);
                        }
                    }).setNegativeButton(R.string.cancelar_gps, null)
                    .show();
            return false;
        }
        else if(!network){
            //Notificar al usuario que tiene desactivados los servicios de red.
            alertDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.net_desactivada)
                    .setPositiveButton(R.string.activar_gps, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),ACTIVAR_UBICACION);
                        }
                    }).setNegativeButton(R.string.cancelar_gps, null)
                    .show();
            return false;
        }
        return true;
    }

    /*Metodo asociado a el boton de localizar, permite posicionar el mapa en la ubicacion actual del usuario*/
    public void findMe(){
        //Comprobar que los servicios de ubicacion estan activados:
        if(checkLocationServices()) {
            if(loading_style.tryAcquire()) {
                try {
                    //Hacer el indicador visible
                    locationComponent.setLocationComponentEnabled(true);

                    //Colocar el modo de la camara en Tracking
                    locationComponent.setCameraMode(CameraMode.TRACKING);

                    //Hacer zoom a la ubicacion del usuario
                    locationComponent.zoomWhileTracking(ZOOM_FIND);

                    //Devolver el permiso del semaforo
                    loading_style.release();
                } catch (SecurityException e) {
                    Log.v("Última localización", "No se pudo obtener la última localizacion del usuario");
                }
            }
            else{
                Toast.makeText(this,getString(R.string.cargando_mapa),Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void changeStyle(Style.Builder newStyle,Style.OnStyleLoaded loadStyle){
        try {
            loading_style.acquire();
            mapboxMap.setStyle(newStyle,loadStyle);
            loading_style.release();
        } catch (InterruptedException e) {
            Log.v("Cambio de estilo", "No se ha podido cambiar de estilo");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)); //Registrar el broadcastReciever (tambien se pede hacer desde el manifest pero para ello deberiamos crear una clase que extienda a BroadcastReciver)
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mGpsSwitchStateReceiver); //Eliminar el broadcastReciever
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}