package com.example.myapplication;


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.Modelos.Alert;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
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


import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;


public class Map extends AppCompatActivity {

    //Macros:
    private final int ACTIVAR_UBICACION = 0; //Codigo de la actividad que nos servira para saber si el usuario a activado o no la ubicacion
    private final double ZOOM_FIND = 15.0; //Zoom al pulsar el boton de localizacion
    public static final int MAX_DISTANCIA = 3; //Distancia maxima en kilometros para compobar los puntos cercanos
    private final int TIEMPO_REFRESCO = 15000; //Tiempo de refresco del hijo en milisegundos
    private final int MAX_PUNTOS = 50; //Numero maximo de puntos cercanos que se le muestran al usuario
    private final int CAMERA_ANIMATION = 3000; //Duración de la animación de la camara (milisegundos)
    private static final String MAKI_ICON_CAFE = "cafe-15";
    private final float TAMANO_MIN_ICONO = 2.0f; //Tamaño minimo del icono para la animación
    private final float TAMANO_MAX_ICONO = 4.0f; //Tamaño maximo del icono para la animación
    private final int ANIMACION_ICONO = 300; //Animación del tamaño del icono en milisegundos

    //Mapa y estilos del mapa:
    private MapView mapView;
    private Style.Builder BasicStyle;
    private Style.Builder SatelliteStyle;
    private Style.OnStyleLoaded loadedStyle; //Variable que almacenara el estilo cargado en el mapa con sus añadidos
    private boolean style; //Varible que indica si el style es el basico (false) o satelite (true)
    private Semaphore loading_style; //Variable que determina si el style del mapa está cargando.
    private MapboxMap mapboxMap; //Instancia del mapa generado
    private SymbolManager symbolManager; //Symbol manager para manejar los eventos y la creación de marcadores

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
    private Symbol markerSelected; //Variable que indica si un marcador esta o no seleccionado.
    private ValueAnimator markerAnimator; //Animador para cuando se selecciona un marker del mapa

    //Componentes del hilo hijo:
    private boolean stop; //Variable que comprueba si el Activity está detenido.
    private boolean pause; //Variable que indica si el Activity esta pausado.
    private Thread hijo; //Variable que contiene a la intancia del hijo.
    private List<Symbol> lista_symbol; //Lista que almacenará los Simbolos añadidos al mapa
    private List<Symbol>  query; //Lista que almacenará los Simbolos añadidos al mapa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.map_token));
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        ListView listView = findViewById(R.id.AjustesMapa);
        listView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, new String[] {"Copy","Paste","Delete","Cut","Convert","Open"}));


        //Inicializar los semaforos
        loading_style = new Semaphore(1);

        //Inicializar marcador seleccionado a null:
        markerSelected = null;

        //Inicizalizar la lista de puntos:
        lista_symbol = new ArrayList<>();

        //Inicializar el handler del main thread:
         manejador = new Handler(getApplicationContext().getMainLooper()){
             @Override
             public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    ArrayList<ParseObject> lista_puntos = (ArrayList<ParseObject>) msg.obj;
                    mostrarPuntos(lista_puntos);
             }
         };

        //Instanciamos el Boton flotante con el mnú y los botones de localización y cambio de estilo:
        BotonFlotante = (FloatingActionMenu) findViewById(R.id.BotonFlotante);
        BotonFlotante.setClosedOnTouchOutside(true);
        BotonFlotante.getMenuIconView().setImageResource(R.drawable.botonmapaflotante);
        BotonFlotante.bringToFront();

        BotonMapa = (FloatingActionButton) findViewById(R.id.BotonCambioMapa);
        BotonMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!style){
                    set_style(SatelliteStyle);
                    style = true;
                }
                else {
                    set_style(BasicStyle);
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
                        //Comprobamos si es la primera vez que se activan
                        if(locationComponent == null)
                            enableLocationComponent(mapboxMap.getStyle());
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
        set_style(BasicStyle);
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
                    }).setNegativeButton(R.string.cancelar_gps, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(),getString(R.string.serviciosDesactivados),Toast.LENGTH_LONG).show();
                        }
                    })
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
    public void set_style(final Style.Builder newStyle){
        try {
            loading_style.acquire();
            mapView.getMapAsync(mapboxMap -> {
                Map.this.mapboxMap = mapboxMap;

                //Iinicializar la variable que almacenara los añadidos del mapa
                loadedStyle = new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        if(symbolManager!=null)
                            symbolManager.deleteAll();
                        // Set up a SymbolManager instance
                        symbolManager = new SymbolManager(mapView, mapboxMap, style);
                        // Add click listener and change the symbol to a cafe icon on click
                        symbolManager.addClickListener(new OnSymbolClickListener() {
                            @Override
                            public void onAnnotationClick(final Symbol symbol) {
                                Toast.makeText(Map.this,
                                        "HOLA MUNDO", Toast.LENGTH_SHORT).show();
                                selectMarker(symbol);
                            }
                        });
                        symbolManager.setIconAllowOverlap(false);
                        symbolManager.setTextAllowOverlap(true);
                        markerSelected = null;
                        enableLocationComponent(style);
                    }
                };
                mapboxMap.setStyle(newStyle,loadedStyle);
            });
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
                                            guardarAlertas(queryresult);
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
            Message msg = new Message();
            msg.obj = queryresult;
            manejador.sendMessage(msg);
    }

    /*Metodo que utilizará el hilo main para obetener los puntos que ha leido el hijo y mostrarlos en el mapa.
    * El padre es el que muestra puesto que invocar metodos de mapbox en un hilo worker, lleva a errores y cuelgues
    * en la aplicación.*/
    private void mostrarPuntos(ArrayList<ParseObject> listaPuntos){
            if(loading_style.tryAcquire() && mapboxMap.getStyle() != null) {
                //Caso en el que la query es empty y habia puntos en el mapa. Eliminarlos todos.
                if(listaPuntos.isEmpty() && !lista_symbol.isEmpty()){
                    symbolManager.delete(lista_symbol);
                }
                //Caso en el que ninguna lista es vacia y hay que añadir elementos
                else if((!listaPuntos.isEmpty() && !lista_symbol.isEmpty()) || (!listaPuntos.isEmpty() && lista_symbol.isEmpty())){
                    //Eliminar los puntos actuales
                    symbolManager.delete(lista_symbol);
                    //Limpiar la lista de puntos y volver a añadirlos:
                    lista_symbol.clear();
                    float icon_size;
                    //Añadir nuevos puntos
                    for (ParseObject alerta : listaPuntos) {
                        ParseGeoPoint loc = ((Alert) alerta).getLocalizacion();
                        if (markerSelected != null && (loc.getLatitude() == markerSelected.getLatLng().getLatitude() && loc.getLongitude() == markerSelected.getLatLng().getLongitude())) {
                            icon_size = TAMANO_MAX_ICONO;
                        }
                        else
                            icon_size = TAMANO_MIN_ICONO;
                        // Add symbol at specified lat/lon
                            Symbol symbol = symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(loc.getLatitude(), loc.getLongitude()))
                                    .withIconImage(MAKI_ICON_CAFE)
                                    .withIconSize(icon_size)
                                    .withDraggable(false)); //No permitir el movimiento del icono
                            lista_symbol.add(symbol);
                    }
                }
                if(markerSelected != null){
                    lista_symbol.add(markerSelected);
                }
                loading_style.release();
            }
    }

    /** Metodo que anima la camara y la lleva hasta el lugar indicado mediante las coordenadas de un objeto Symbol **/
    private void animateCamera(Symbol symbol){
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(symbol.getLatLng())) // Sets the new camera position
                .zoom(ZOOM_FIND) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), CAMERA_ANIMATION);
    }

    /* Metodo que permite seleccionar un marcador del mapa haciendolo más grande mediante una animación.
    * Primero comprobar que el marcador seleccionado no es el mismo que el que se acaba de seleccionar.*/
    private void selectMarker(final Symbol symbol) {
        if((markerSelected == null) || (!symbol.equals(markerSelected))) {
            deselectMarker(markerSelected);
            animateCamera(symbol);
            markerAnimator = new ValueAnimator();
            markerAnimator.setObjectValues(TAMANO_MAX_ICONO, TAMANO_MIN_ICONO);
            markerAnimator.setDuration(ANIMACION_ICONO);
            markerAnimator.addUpdateListener(animator -> symbol.setIconSize((float) markerAnimator.getAnimatedValue()));
            markerSelected = symbol;
            markerAnimator.start();
            symbolManager.update(symbol);
        }
    }

    /* Metodo que permite deseleccionar un marcador del mapa haciendolo más pequeño mediante una animación.
    * Comprobar que hay un marcador seleccionado.*/
    private void deselectMarker(final Symbol symbol) {
        if (markerSelected != null){
            markerSelected = null;
            symbolManager.deleteAll();
            symbolManager.update(lista_symbol);
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
        //Eliminar el animador.
        if (markerAnimator != null) {
            markerAnimator.cancel();
        }
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}