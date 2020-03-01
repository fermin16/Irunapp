package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.Modelos.Alert;
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
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class Map extends AppCompatActivity {

    //Macros:
    private final int ACTIVAR_UBICACION = 0; //Codigo de la actividad que nos servira para saber si el usuario a activado o no la ubicacion
    private final double ZOOM_FIND = 15.0; //Zoom al pulsar el boton de localizacion
    public static final int MAX_DISTANCIA = 3; //Distancia maxima en kilometros para compobar los puntos cercanos
    private final int TIEMPO_REFRESCO = 15000; //Tiempo de refresco del hijo en milisegundos
    private final int MAX_PUNTOS = 50; //Numero maximo de puntos cercanos que se le muestran al usuario
    private final String DESTINATION_SOURCE = "destination-source-id"; //Vairiable que almacena la fuente para el layer que aparece al hacer click en el mapa
    private final String DESTINATION_LAYER = "destination-symbol-layer-id"; //Variable que almacena el nombre del layer que aparece al hacer click en el mapa.
    private final String CLUSTER_SOURCE = "cluster-source-id"; //Vairiable que almacena la fuente para el layer cluster.
    private final String CLUSTER_LAYER = "destination-cluster-layer-id"; //Variable que almacena el nombre del cluster layer.

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
    private Handler manejador; //Handler que maneja los mensajes del hilo hijo.

    //Componentes del hilo hijo:
    private boolean stop; //Variable que comprueba si el Activity está detenido.
    private boolean pause; //Variable que indica si el Activity esta pausado.
    private Thread hijo; //Variable que contiene a la intancia del hijo.
    private Semaphore semaforo_puntos; //Semaforo para controlar cuando el main esta printeando los puntos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.map_token));
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        //Inicializar los semaforod
        loading_style = new Semaphore(1);
        semaforo_puntos = new Semaphore(1);

        //Inicializar el handler del main thread:
         manejador = new Handler(getApplicationContext().getMainLooper()){
             @Override
             public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    ArrayList<ParseObject> listaPuntos = (ArrayList<ParseObject>) msg.obj;
                    mostrarPuntos(listaPuntos);
             }
         };

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
                        pause = true; //Poner el hijo en pausa
                        Toast.makeText(context,getString(R.string.serviciosDesactivados),Toast.LENGTH_LONG).show();
                    }
                    else{ //Si se han activado
                        findMe(); //Mostrar localizacion
                        if(hijo!=null) {//Si el hijo estaba dormido, reactivarlo:
                            if (pause)
                                despertar();
                        }
                        else {//Si el hijo no estaba creado aun:
                            //Comenzar a obtener los puntos del mapa
                            buscaPuntos();
                        }
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
                        addClusterSymbolLayer(style);
                        addDestinationIconSymbolLayer(style);
                        enableLocationComponent(style);
                    }
                };
                changeStyle(BasicStyle,loadedStyle);
            }
        });
    }

    /* Metodo que se encarga de añadir el layer a el mapa: */
    private void addLayer(String layer, String source, Style loadedMapStyle){
        SymbolLayer symbolLayer = new SymbolLayer(layer, source);
        symbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true)
        );
        loadedMapStyle.addLayer(symbolLayer);
    }

    /*Metodo que se encarga de añadir el layer del marker al mapa cuando se le añade un estilo al mismo. */
    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource(DESTINATION_SOURCE);
        if(loadedMapStyle.getSource(DESTINATION_SOURCE) == null) { //comprobar que no esta cargado el estilo por si acaso hay un cambio rapido de estilos
            loadedMapStyle.addSource(geoJsonSource);
            addLayer(DESTINATION_LAYER, DESTINATION_SOURCE, loadedMapStyle);
        }
    }

    private void addClusterSymbolLayer(@ NonNull Style loadedMapStyle){
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource(CLUSTER_SOURCE);
        if(loadedMapStyle.getSource(CLUSTER_SOURCE) == null) { //comprobar que no esta cargado el estilo por si acaso hay un cambio rapido de estilos
            loadedMapStyle.addSource(geoJsonSource);
            addLayer(CLUSTER_LAYER, CLUSTER_SOURCE, loadedMapStyle);
        }
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

                    //Comenzar a printear los puntos en el mapa
                    buscaPuntos();
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

    /** Este método permite cambiar de estilo de mapa evitando posibles riesgos a la hora de obtener la ubicación o de hacer un cambio rápido de estilos**/
    public void changeStyle(Style.Builder newStyle,Style.OnStyleLoaded loadStyle){
        try {
            loading_style.acquire();
            mapboxMap.setStyle(newStyle,loadStyle);
            loading_style.release();
        } catch (InterruptedException e) {
            Log.v("Cambio de estilo", "No se ha podido cambiar de estilo");
        }
    }

    /* Este metodo se encarga de crear un hilo que cada x segundos realiza una query al servidor Parse
     * para obtener la información de los puntos cercanos a la ubicación del usuario. */
    public void buscaPuntos(){
        Toast.makeText(getApplicationContext(),getString(R.string.obteniendoUbicacion),Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(),getString(R.string.obteniendoAlertas)+"("+MAX_DISTANCIA+" km)",Toast.LENGTH_LONG).show();
        Runnable hilo = new Runnable() {
            @SuppressLint("MissingPermission")
            public void run() {
                Location myLocation;
                do{
                    try{
                        //Esperar un tiempo y actualizar los puntos cercanos.
                        Thread.sleep(TIEMPO_REFRESCO);
                        //Si la actividad es pausada o pasa a segundo plano debemos pausar el hilo
                        if(pause){
                            synchronized (hijo){
                                hijo.wait();
                            }
                        }
                        //Comprobar que la componente de localizacion esta activada
                        if(locationComponent != null) {
                            //Obtener la ultima ubicacion del usuario
                            myLocation = locationComponent.getLastKnownLocation();
                            /*Si la ubicacion no es nula, hacer una query que obtenga todos los punto a una distancia menor o igual
                             * a MAX_DISTANCIA y mostrarlos en el mapa. Para ello obtener la localizacion actual del usuario */
                            if(myLocation != null) {
                                ParseGeoPoint userLocation = new ParseGeoPoint(myLocation.getLatitude(),myLocation.getLongitude());
                                ParseQuery<ParseObject> query = ParseQuery.getQuery("Alert");
                                query.whereWithinKilometers("localizacion", userLocation, MAX_DISTANCIA);
                                query.setLimit(MAX_PUNTOS);
                                query.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> queryresult, ParseException e) {
                                        if(e == null) {
                                            //Comprobar el resultado de la query:
                                            if (!queryresult.isEmpty()) {
                                                Message msg = new Message();
                                                msg.obj = queryresult;
                                                manejador.sendMessage(msg);
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    } catch (InterruptedException e) {
                        Log.v("Hijo interrumpido","El hijo ha sido interrumpido");
                    }
                }while (!stop);
            }
        };
        hijo = new Thread(hilo);
        hijo.start();
    }

    /* Metodo que pasa los puntos que recoge el hilo hijo. requiere de acceso al semaforo por si el padre esta mostrando los puntos.
    * Los puntos se envían a traves de un mensaje con el handler del main */
    private void guardarAlertas(List<ParseObject> queryresult){
        try {
            semaforo_puntos.acquire();
            manejador.sendMessage(new Message());
            semaforo_puntos.release();
        } catch (InterruptedException e) {
            Log.v("Hijo interrumpido","El hijo ha sido interrumpido cuando enviaba los puntos");
        }
    }

    /*Metodo que utilizará el hilo main para obetener los puntos que ha leido el hijo y mostrarlos en el mapa.
    * El padre es el que muestra puesto que invocar metodos de mapbox en un hilo worker, lleva a errores y cuelgues
    * en la aplicación.*/
    private void mostrarPuntos(ArrayList<ParseObject> listaPuntos){
        //Comprobar el acceso a la lista de puntos:
        if(semaforo_puntos.tryAcquire()) {
            List<Feature> lista = new ArrayList<>();
            if(loading_style.tryAcquire() && mapboxMap.getStyle() != null) {
                //Obtener la fuente GeoJSON
                GeoJsonSource source = mapboxMap.getStyle().getSourceAs(CLUSTER_SOURCE);

                //Crear la lista de Feature a partir de los objetos devueltos en la lista, despues de realizar la query
                for (ParseObject alerta : listaPuntos) {
                    ParseGeoPoint loc = ((Alert) alerta).getLocalizacion();
                    lista.add(Feature.fromGeometry(Point.fromLngLat(loc.getLongitude(), loc.getLatitude())));
                }

                //Apartir de lista de Feature construir una FeatureCollection y añadirla a la fuente para mostrarla
                FeatureCollection feature = FeatureCollection.fromFeatures(lista);
                if (source != null) {
                    source.setGeoJson(feature);
                }
                loading_style.release();
            }
            semaforo_puntos.release();
        }
    }

    /* Metodo que se encarga de despertar al hijo*/
    public void despertar(){
        pause = false;
        if(hijo != null) {
            synchronized (hijo) {
                hijo.notifyAll(); //Notificar al hijo que puede continuar su ejecucion
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStart() {
        try {
            super.onStart();
            mapView.onStart();
        }catch(SecurityException ex){
            Log.v("Última localización","No se pudo obtener la última localizacion del usuario");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        despertar();
        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)); //Registrar el broadcastReciever (tambien se pede hacer desde el manifest pero para ello deberiamos crear una clase que extienda a BroadcastReciver)
    }

    @Override
    protected  void onRestart() {
        super.onRestart();
        despertar();
    }

    @Override
    public void onPause() {
        pause = true; //Pausar el hilo hijo
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        pause = true; //Pausar el hilo hijo
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
        stop = true;
        if(hijo != null) {
            if(hijo.getState() == Thread.State.TIMED_WAITING) //Si el hijo está durmiendo:
                hijo.interrupt(); //Interrumpir el hilo
        }
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