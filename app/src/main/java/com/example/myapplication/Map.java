package com.example.myapplication;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.d.lib.tabview.TabView;
import com.example.myapplication.Modelos.Alert;
import com.example.myapplication.Modelos.Preferencias;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import pl.droidsonroids.gif.GifImageView;


public class Map extends AppCompatActivity {

    //Macros:
    //Macros relacionadas con el mapa y la camara:
    private final int ACTIVAR_UBICACION = 0; //Codigo de la actividad que nos servira para saber si el usuario a activado o no la ubicacion
    private static final double ZOOM_FIND = 15.0; //Zoom al pulsar el boton de localizacion
    private final double ZOOM_MIN = 12.0;
    private static final int CAMERA_ANIMATION = 3000; //Duración de la animación de la camara (milisegundos)
    private static final LatLng BOUND_CORNER_NW = new LatLng(42.830833, -1.608611);
    private static final LatLng BOUND_CORNER_NE = new LatLng(42.8525, -1.6925);
    private static final LatLng BOUND_CORNER_SE = new LatLng(42.789167, -1.536111);
    private static final LatLng BOUND_CORNER_SO = new LatLng(42.786667, -1.690833);
    private static final LatLngBounds RESTRICTED_BOUNDS_AREA = new LatLngBounds.Builder()
            .include(BOUND_CORNER_NW)
            .include(BOUND_CORNER_NE)
            .include(BOUND_CORNER_SE)
            .include(BOUND_CORNER_SO)
            .build();

    //Macros para iconos (marker del mapa):
    private static final String MAKI_ICON_CAFE = "cafe-15";
    private final float TAMANO_MIN_ICONO = 2.0f; //Tamaño minimo del icono para la animación
    private final float TAMANO_MAX_ICONO = 4.0f; //Tamaño maximo del icono para la animación
    private final int ANIMACION_ICONO = 300; //Animación del tamaño del icono en milisegundos

    //Macros de panel deslizante:
    private final double OFFSET_FAB = 1.3; //OFFSET base de la animacion de subir y bajar el FAB
    private final double OFFSET_RECYCLEVIEW = 1.5; //OFFSET base de la animacion de subit y bajar la recycleview
    private final double TAB_1_DISTANCIA = 0.5;
    private final double TAB_2_DISTANCIA = 1.0;
    private final double TAB_3_DISTANCIA = 2.0;
    private final int TAB_1_FRECUENCIA = 15000;
    private final int TAB_2_FRECUENCIA = 30000;
    private final int TAB_3_FRECUENCIA = 60000;
    private final int TAB_1_PUNTOS = 10;
    private final int TAB_2_PUNTOS = 30;
    private final int TAB_3_PUNTOS = 50;
    private final int ANIMACION_TARJETAS = 500; //Tiempo animación tarjetas en milisegundos

    //Mapa y estilos del mapa:
    private MapView mapView;
    private Style.Builder BasicStyle;
    private Style.Builder SatelliteStyle;
    private Style.OnStyleLoaded loadedStyle; //Variable que almacenara el estilo cargado en el mapa con sus añadidos
    private boolean style; //Varible que indica si el style es el basico (false) o satelite (true)
    private Semaphore loading_style; //Variable que determina si el style del mapa está cargando.
    private MapboxMap mapboxMap; //Instancia del mapa generado
    private SymbolManager symbolManager; //Symbol manager para manejar los eventos y la creación de marcadores
    MapboxMap.OnMapClickListener mapListener; //Listener del mapa

    //Menu de boton flotante y sus botones:
    private FloatingActionMenu BotonFlotante;
    private FloatingActionButton BotonMapa;
    private FloatingActionButton BotonLocalizacion;
    private FloatingActionButton BotonBuscaPuntos;
    private FloatingActionButton BotonTarjetas;

    //Componentes para funcionalidades del mapa:
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent; //Variable para obtener la localizacion actual
    private AlertDialog alertDialog; //Guardar una variable para el alertDialog que permitira cerrarlo cuando deba crearse uno nuevo
    private BroadcastReceiver mGpsSwitchStateReceiver; //BoradcastReciver para saber cuando un usuario activa o desactiva gps
    private Handler manejador; //Handler que maneja los mensajes del hilo hijo.

    //Componentes marcadores:
    private Symbol markerSelected; //Variable que indica si un marcador esta o no seleccionado.
    private int simboloActivado; //Valor entero que indique que un marker acaba de ser seleccionado por el listener de symbolmanager y no debe ser deseleccionado
    private ValueAnimator markerAnimator; //Animador para cuando se selecciona un marker del mapa
    private boolean noMarker; //Variable booleana que indica si el marker seleccionado estaba o no en la ultima query

    //Componentes cardView:
    private ArrayList<ParseObject> prevQuery; //Array list que almacena la query anterior
    private ParseObject cardSelected; //Guarda la card seleccionada en el mapa.

    //Componentes del hilo hijo:
    private boolean stop; //Variable que comprueba si el Activity está detenido.
    private boolean pause; //Variable que indica si el Activity esta pausado.
    private Thread hijo; //Variable que contiene a la intancia del hijo.
    private List<Symbol> lista_symbol; //Lista que almacenará los Simbolos añadidos al mapa

    //Slide up menu y ajustes:
    private SlidingUpPanelLayout panelDeslizante;
    private double max_distancia; //Distancia maxima en kilometros para compobar los puntos cercanos
    private int tiempo_refresco; //Tiempo de refresco del hijo en milisegundos
    private int max_puntos; //Numero maximo de puntos cercanos que se le muestran al usuario
    private Semaphore semaforo_ajustes; //Semaforo para justes del mapa
    private TabView tab_distancia;
    private TabView tab_frecuencia;
    private TabView tab_puntos;
    private GifImageView gif_ajustes;

    //RecycleView y componentes:
    private RecyclerView recyclerView;
    private LinearLayoutManager recyclerLayoutManager;
    private int elementoActualRecyclerView;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.map_token));
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);


        //Inicializar los semaforos
        loading_style = new Semaphore(1);

        //Inicializar marcador seleccionado a null:
        markerSelected = null;

        //Iniciar lista de ParseObject y el card seleccionado:
        prevQuery = new ArrayList<>();

        //Inicizalizar la lista de puntos y variable:
        lista_symbol = new ArrayList<>();
        noMarker = false;

        //Inicializar el handler del main thread:
         manejador = new Handler(getApplicationContext().getMainLooper()){
             @RequiresApi(api = Build.VERSION_CODES.N)
             @Override
             public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    ArrayList<ParseObject> lista_puntos = (ArrayList<ParseObject>) msg.obj;
                    mostrarPuntos(lista_puntos);
             }
         };

        //Instanciamos el Boton flotante con el mnú y los botones de localización y cambio de estilo:
        BotonFlotante = findViewById(R.id.BotonFlotante);
        BotonFlotante.setClosedOnTouchOutside(true);
        BotonFlotante.getMenuIconView().setImageResource(R.drawable.botonmapaflotante);
        BotonFlotante.bringToFront();

        BotonMapa =  findViewById(R.id.BotonCambioMapa);
        BotonMapa.setOnClickListener(v -> {
            if(!style){
                set_style(SatelliteStyle);
                style = true;
            }
            else {
                set_style(BasicStyle);
                style = false;
            }
        });

        BotonLocalizacion =  findViewById(R.id.FindMe);
        BotonLocalizacion.setOnClickListener(v -> findMe());

        BotonBuscaPuntos = findViewById(R.id.BuscaPuntos);
        BotonBuscaPuntos.setEnabled(false);
        BotonBuscaPuntos.setOnClickListener(v -> {
            despertar();
            BotonBuscaPuntos.setEnabled(false);
        });

        BotonTarjetas = findViewById(R.id.BotonTarjetas);
        BotonTarjetas.setEnabled(false);
        BotonTarjetas.setOnClickListener(v -> {
            if(BotonTarjetas.getColorNormal() == getColor(R.color.botonTarjetasNormal)) {
                BotonTarjetas.setColorNormal(getColor(R.color.botonTarjetasDesactivado));
                BotonTarjetas.setEnabled(false);
                ocultarTarjetas();
            }else{
                BotonTarjetas.setColorNormal(getColor(R.color.botonTarjetasNormal));
                mostrarTarjetas();
            }
        });

        gif_ajustes = findViewById(R.id.swipe_gif);
        panelDeslizante = findViewById(R.id.PanelDeslizante);
        panelDeslizante.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                BotonFlotante.close(true);
                BotonFlotante.animate().translationY((float) (panelDeslizante.getCurrentParallaxOffset()*OFFSET_FAB)).setDuration(0);
                BotonTarjetas.animate().translationY((float) (panelDeslizante.getCurrentParallaxOffset()*OFFSET_FAB)).setDuration(0);
                if(recyclerView != null){
                    recyclerView.animate().translationY((float) (panelDeslizante.getCurrentParallaxOffset()*(OFFSET_RECYCLEVIEW))).setDuration(0);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED){
                    gif_ajustes.setImageResource(R.drawable.giphy);
                    gif_ajustes.setPadding(0,0,20,0);
                }
                else if(newState == SlidingUpPanelLayout.PanelState.COLLAPSED){
                    gif_ajustes.setImageResource(R.drawable.tenor);
                    gif_ajustes.setPadding(0,0,100,95);
                }
            }
        });

        //Inicializar variables para busqueda de puntos y ajustes:
        //Obtener valores de ajustes almacenados
        max_puntos = Preferencias.getPuntos(this);
        System.out.println(max_puntos);
        int selected_tab_distancia = Preferencias.getKilometros(this);
        tiempo_refresco = Preferencias.getFrecuencia(this);

        semaforo_ajustes = new Semaphore(1);
        tab_distancia = findViewById(R.id.kilometros);
        tab_distancia.setOnTabSelectedListener(index -> {
            setAjustesMapa(index, tab_distancia.getId(),true);
        });

        tab_frecuencia = findViewById(R.id.frecuencia);
        tab_frecuencia.setOnTabSelectedListener(index -> {
            setAjustesMapa(index, tab_frecuencia.getId(),true);
        });

        tab_puntos = findViewById(R.id.numpuntos);
        tab_puntos.setOnTabSelectedListener(index -> {
            setAjustesMapa(index, tab_puntos.getId(),true);
        });
        tab_distancia.selectTab(selected_tab_distancia,false);
        setAjustesMapa(selected_tab_distancia,tab_distancia.getId(),false);
        tab_frecuencia.selectTab(tiempo_refresco,false);
        setAjustesMapa(tiempo_refresco,tab_frecuencia.getId(),false);
        tab_puntos.selectTab(max_puntos,false);
        setAjustesMapa(max_puntos, tab_puntos.getId(),false);

        //Inicializar los dos estilos de mapa:
        BasicStyle = new Style.Builder().fromUri(getString(R.string.map_style_basic));
        SatelliteStyle = new Style.Builder().fromUri(getString(R.string.map_style_road));

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
        //Registrar el broadcastReciever (tambien se pede hacer desde el manifest pero para ello deberiamos crear una clase que extienda a BroadcastReciver)
        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        set_style(BasicStyle);
        initRecyclerView(null);
    }

    /* Metodo que se encarga de activar el elemento de localizacion, para ello pide permisos al usuario
     * (en caso de no haberlos pedido antes) y si el usuario acepta podra utilizar el mapa en caso
     * contrario se terminara el activity.*/
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
         permissionsManager = new PermissionsManager(new PermissionsListener() {
            @Override
            public void onExplanationNeeded(List<String> permissionsToExplain) {
                Toast.makeText(getApplicationContext(),getString(R.string.activarPermisosAjustes),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPermissionResult(boolean granted) {
                if (granted) {
                    enableLocationComponent(loadedMapStyle);
                } else {
                    if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                        Toast.makeText(getApplicationContext(),getString(R.string.activarPermisos),Toast.LENGTH_LONG).show();
                    else
                        onExplanationNeeded(null);
                }
            }
        });
        //Comprobar si se han concedido los permisos de ubicacion:
        if (permissionsManager.areLocationPermissionsGranted(this)) {
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
        else{ //Si no se han concedido permisos y el usuario no ha marcado la opcion no volver a preguntar, solicitarlos
            permissionsManager.requestLocationPermissions(this);
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
                    if(locationComponent !=null) {
                        //Hacer el indicador visible
                        locationComponent.setLocationComponentEnabled(true);

                        //Colocar el modo de la camara en Tracking
                        locationComponent.setCameraMode(CameraMode.TRACKING);

                        //Hacer zoom a la ubicacion del usuario
                        locationComponent.zoomWhileTracking(ZOOM_FIND);
                    }
                    else{
                        enableLocationComponent(mapboxMap.getStyle());
                    }
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
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void set_style(final Style.Builder newStyle){
        try {
            loading_style.acquire();
            mapView.getMapAsync(mapboxMap -> {
                Map.this.mapboxMap = mapboxMap;
                //Iinicializar la variable que almacenara los añadidos del mapa
                loadedStyle = style -> {
                    if(symbolManager!=null)
                        symbolManager.deleteAll();
                    // Set up a SymbolManager instance
                    symbolManager = new SymbolManager(mapView, mapboxMap, style);
                    // Add click listener and change the symbol to a cafe icon on click
                    symbolManager.addClickListener(symbol -> {
                        simboloActivado = 1;
                        selectMarker(symbol);
                        selectCard();
                    });
                    if(mapListener !=null)
                        mapboxMap.removeOnMapClickListener(mapListener);
                    //Añadir Listener al mapa:
                    mapboxMap.addOnMapClickListener(mapListener = point -> {
                        //Decrementar el valor de simboloActivado si es igual a 0 acaba de ocurrir un evento en symbolManager y no debemos deseleccionar el marcador
                        simboloActivado--;
                        if((markerSelected != null) && simboloActivado!=0){
                            deselectMarker();
                        }
                        return false;
                    });
                    symbolManager.setIconAllowOverlap(false);
                    symbolManager.setTextAllowOverlap(true);
                    markerSelected = null;
                    enableLocationComponent(style);
                };
                mapboxMap.setStyle(newStyle,loadedStyle);
                //Limitar el mapa y la vista:
                mapboxMap.setLatLngBoundsForCameraTarget(RESTRICTED_BOUNDS_AREA);
                mapboxMap.setMinZoomPreference(ZOOM_MIN);
            });
            loading_style.release();
        } catch (InterruptedException e) {
            Log.v("Cambio de estilo", "No se ha podido cambiar de estilo");
        }
    }

    /* Este metodo se encarga de crear un hilo que cada x segundos realiza una query al servidor Parse
     * para obtener la información de los puntos cercanos a la ubicación del usuario. */
    public void buscaPuntos(){
        String units;
        Double aux;
        if(max_distancia < 1) {
            units = "m";
            aux = max_distancia * 1000;
        }else {
            units = "km";
            aux = max_distancia;
        }
        Toast.makeText(getApplicationContext(),getString(R.string.obteniendoPuntos)+" "+aux+" "+units+" "+getString(R.string.obteniendoPuntos2),Toast.LENGTH_LONG).show();
        Runnable hilo = () -> {
            Location myLocation;
            do{
                try{
                    //Si la actividad es pausada o pasa a segundo plano debemos pausar el hilo
                    if(pause){
                        synchronized (hijo){
                            hijo.wait();
                        }
                        String units2;
                        Double aux2;
                        if(max_distancia < 1) {
                            units2 = "m";
                            aux2 = max_distancia*1000;
                        }
                        else{
                                units2 = "km";
                                aux2 = max_distancia;
                            }
                        manejador.postDelayed(new Runnable() {
                            public void run() {
                                Toast.makeText(Map.this, getString(R.string.obteniendoPuntos)+" "+aux2+" "+units2+" "+getString(R.string.obteniendoPuntos2), Toast.LENGTH_LONG).show();

                            }
                        }, 2000);
                        if(BotonBuscaPuntos.isEnabled())
                            runOnUiThread(() -> BotonBuscaPuntos.setEnabled(false));
                    }
                    //Comprobar que la componente de localizacion esta activada
                    if (locationComponent != null) {
                        //Obtener la ultima ubicacion del usuario
                        myLocation = locationComponent.getLastKnownLocation();
                        /*Si la ubicacion no es nula, hacer una query que obtenga todos los punto a una distancia menor o igual
                         * a MAX_DISTANCIA y mostrarlos en el mapa. Para ello obtener la localizacion actual del usuario */
                        if (myLocation != null) {
                            queryPuntos(myLocation);
                            //Esperar un tiempo y actualizar los puntos cercanos.
                            Thread.sleep(tiempo_refresco);
                        }
                    }
                } catch (InterruptedException e) {
                    Log.v("Hijo interrumpido","El hijo ha sido interrumpido");
                }
            }while (!stop);
        };
        hijo = new Thread(hilo);
        hijo.start();
    }

    /** Método que realiza la query a la BBDD para obtener los puntos cercanos **/
    private void queryPuntos(Location myLocation ){
        try {

                    ParseGeoPoint userLocation = new ParseGeoPoint(myLocation.getLatitude(), myLocation.getLongitude());
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Alert");
                    semaforo_ajustes.acquire();
                    query.whereWithinKilometers("localizacion", userLocation, max_distancia);
                    query.setLimit(max_puntos);
                    semaforo_ajustes.release();
                    query.findInBackground((queryresult, e) -> {
                        if (e == null) {
                            guardarAlertas(queryresult);
                        }
                    });
        }catch (InterruptedException e){
            Log.v("Hijo interrumpido", "No se ha podido completar la query, hijo interrumpido");
        }
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
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void mostrarPuntos(ArrayList<ParseObject> listaPuntos){
            symbolManager.delete(lista_symbol);
            if(loading_style.tryAcquire() && mapboxMap.getStyle() != null) {
                //Caso en el que la query es empty y habia puntos en el mapa. Eliminarlos todos.
                if(listaPuntos.isEmpty() && !lista_symbol.isEmpty()){
                    if(markerSelected != null){
                        markerSelected = symbolManager.create(new SymbolOptions()
                                .withLatLng(markerSelected.getLatLng())
                                .withIconImage(markerSelected.getIconImage())
                                .withIconSize(TAMANO_MAX_ICONO)
                                .withDraggable(markerSelected.isDraggable())); //No permitir el movimiento del icono
                         lista_symbol.clear();
                         lista_symbol.add(0,markerSelected);
                    }
                    else
                        BotonTarjetas.setEnabled(false);
                    lista_symbol.clear();
                    sinPuntos();
                }
                //Caso en el que ninguna lista es vacia y hay que añadir elementos
                else if((!listaPuntos.isEmpty() && !lista_symbol.isEmpty()) || (!listaPuntos.isEmpty() && lista_symbol.isEmpty())){
                    //Considerar que el marker seleccionado no esta en la lista de la query
                    noMarker=true;
                    BotonTarjetas.setEnabled(true);
                    //Limpiar la lista de puntos y volver a añadirlos:
                    lista_symbol.clear();
                    float icon_size;
                    //Añadir nuevos puntos
                    for (ParseObject alerta : listaPuntos) {
                        ParseGeoPoint loc = ((Alert) alerta).getLocalizacion();
                        //Si el marker seleccionado está en la lista poner el tamaño de su icono ampliado y poner noMarker a false (El marker se ha recuperado en la ultima query)
                        if (markerSelected != null && (loc.getLatitude() == markerSelected.getLatLng().getLatitude() && loc.getLongitude() == markerSelected.getLatLng().getLongitude())) {
                            icon_size = TAMANO_MAX_ICONO;
                            noMarker=false;
                        }
                        else
                            icon_size = TAMANO_MIN_ICONO;
                        // Add symbol at specified lat/lon
                        Symbol symbol = symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(loc.getLatitude(), loc.getLongitude()))
                                    .withIconImage(MAKI_ICON_CAFE)
                                    .withIconSize(icon_size)
                                    .withDraggable(false)); //No permitir el movimiento del icono
                        if(icon_size == TAMANO_MAX_ICONO)
                            markerSelected = symbol;
                        lista_symbol.add(symbol);
                    }
                }
                //Si no se ha encontrado ningún punto cercano a la ubicación
                else{
                    sinPuntos();
                }
                // Actualizar el adapter del recyclerView:
                updateAdapter(listaPuntos);
                loading_style.release();
            }
    }

    private  void sinPuntos() {
        pause = true;
        hijo.interrupt();
        BotonBuscaPuntos.setEnabled(true);
        noMarker = true;
        if (alertDialog != null)
            alertDialog.dismiss();
        alertDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.puntos_no_localizados)
                    .setPositiveButton(R.string.volver_buscar, (paramDialogInterface, paramInt) -> {
                        if (hijo != null) {
                            despertar();
                        }
                    }).setNegativeButton(R.string.cancelar_busqueda, (dialog, which) -> {
                Toast.makeText(getApplicationContext(), R.string.busqueda_detenida, Toast.LENGTH_LONG).show();
            }).show();
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

    /**Metodo que permite seleccionar un cardview del mapa de acuerdo con el simbolo seleccionado
     * en definitiva guarda el card asociado al simbolo seleccionado y hace el efecto de scroll. **/
    private void selectCard(){
        int index = lista_symbol.indexOf(markerSelected);
        cardSelected = prevQuery.get(index);
        recyclerView.smoothScrollToPosition(index);
    }

    /* Metodo que permite seleccionar un marcador del mapa haciendolo más grande mediante una animación.
    * Primero comprobar que el marcador seleccionado no es el mismo que el que se acaba de seleccionar.*/
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void selectMarker(final Symbol symbol) {
        if((markerSelected == null) || (!symbol.equals(markerSelected))) {
            deselectMarker();
            noMarker = false; //Lo ponemos a false evitando asi que si es la primera query y no teniamos un punto seleccionado al deseleccionar no se borre el punto.s
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
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void deselectMarker() {
        if (markerSelected != null){
            int index = lista_symbol.indexOf(markerSelected);
            lista_symbol.remove(markerSelected);
            cardSelected = null;
            if(noMarker){
                if(lista_symbol.isEmpty()) {
                    updateAdapter(new ArrayList<>());
                    BotonTarjetas.setEnabled(false);
                }
            }
            else{
                markerSelected.setIconSize(TAMANO_MIN_ICONO);
                lista_symbol.add(index,markerSelected);
            }
            markerSelected = null;
            symbolManager.deleteAll();
            symbolManager.update(lista_symbol);
        }
    }

    /** Metodo para configurar los ajustes del mapa mediante los tabView del menu desplegable,
     * se recie el id del TaView y el index del mismo.
      **/
    private void setAjustesMapa(int index, int tabViewId, boolean show_toast){
        try {
            //Si el hijo no está muerto:
            if(hijo != null && hijo.isAlive()){
                pause = true;
                hijo.interrupt();
            }
            semaforo_ajustes.acquire();
            if(tabViewId == R.id.kilometros){
                if(index == 0){
                    max_distancia = TAB_1_DISTANCIA;
                }
                else if(index == 1){
                    max_distancia = TAB_2_DISTANCIA;
                }
                else
                    max_distancia = TAB_3_DISTANCIA;
                Preferencias.guardaKilometros(getApplicationContext(),index);
                if(show_toast)
                    Toast.makeText(getApplicationContext(),getString(R.string.distancia_actualizada)+" "+Double.toString(max_distancia)+" km",Toast.LENGTH_LONG).show();
            }
            else if(tabViewId == R.id.frecuencia){
                if(index == 0){
                    tiempo_refresco = TAB_1_FRECUENCIA;
                }
                else if(index == 1){
                    tiempo_refresco = TAB_2_FRECUENCIA;
                }
                else
                    tiempo_refresco = TAB_3_FRECUENCIA;
                Preferencias.guardaFrecuencia(getApplicationContext(),index);
                if(show_toast)
                    Toast.makeText(this,getString(R.string.frecuencia_actualizada)+" "+tiempo_refresco/1000+" segundos",Toast.LENGTH_SHORT).show();
            }
            else{
                if(index == 0){
                    max_puntos = TAB_1_PUNTOS;
                }
                else if(index == 1){
                    max_puntos = TAB_2_PUNTOS;
                }
                else
                    max_puntos = TAB_3_PUNTOS;
                Preferencias.guardaPuntos(getApplicationContext(),index);
                if(show_toast)
                    Toast.makeText(this,getString(R.string.puntos_actualizados)+" "+max_puntos+" puntos",Toast.LENGTH_SHORT).show();
            }
            semaforo_ajustes.release();
            if(pause)
                despertar();
        } catch (InterruptedException e) {
            Log.v("Error en ajustes","No se pudieron modificar los ajustes del mapa");
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

    public void mostrarTarjetas(){
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        recyclerView.setAlpha(0f);
        recyclerView.setVisibility(View.VISIBLE);

        float transicion = 0f;
        if(panelDeslizante.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            transicion = (float) (panelDeslizante.getCurrentParallaxOffset()*(OFFSET_RECYCLEVIEW));
        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        recyclerView.animate()
                .alpha(1f)
                .translationY(transicion)
                .setDuration(ANIMACION_TARJETAS)
                .setListener(null);
    }

    private void ocultarTarjetas(){
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        recyclerView.animate()
                .alpha(0f)
                .translationY(recyclerView.getWidth())
                .setDuration(ANIMACION_TARJETAS)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        recyclerView.setVisibility(View.GONE);
                        BotonTarjetas.setEnabled(true);
                    }
                });
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
    }

    @Override
    protected  void onRestart() {
        super.onRestart();
        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)); //Registrar el broadcastReciever (tambien se pede hacer desde el manifest pero para ello deberiamos crear una clase que extienda a BroadcastReciver)
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
        if(mGpsSwitchStateReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mGpsSwitchStateReceiver); //Eliminar el broadcastReciever
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
        if(mGpsSwitchStateReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mGpsSwitchStateReceiver); //Eliminar el broadcastReciever
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


    /*** Parte para las infoWindows que se mostraran en el mapa **/

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initRecyclerView(List<ParseObject> listaParse) {
        recyclerView = findViewById(R.id.rv_on_top_of_map);
        LocationRecyclerViewAdapter locationAdapter =
                new LocationRecyclerViewAdapter(createRecyclerViewLocations(listaParse), mapboxMap);
        recyclerLayoutManager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, true);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(locationAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        recyclerView.setOnFlingListener(null);
        RecyclerView.OnScrollListener listenerScroll = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int elementoVisible = recyclerLayoutManager.findFirstVisibleItemPosition();

                    if (elementoVisible != elementoActualRecyclerView) {
                        selectMarker(lista_symbol.get(elementoVisible));
                        elementoActualRecyclerView = elementoVisible;
                    }
                }
            }
        };
        recyclerView.addOnScrollListener(listenerScroll);
        snapHelper.attachToRecyclerView(recyclerView);
    }

    private List<SingleRecyclerViewLocation> createRecyclerViewLocations(List<ParseObject> listaParse) {
        ArrayList<SingleRecyclerViewLocation> locationList = new ArrayList<>();
        if(listaParse != null && !listaParse.isEmpty()) {
            for (ParseObject object : listaParse) {
                SingleRecyclerViewLocation singleLocation = new SingleRecyclerViewLocation();
                singleLocation.setNombreLugar(((Alert) object).getTitulo());
                singleLocation.setLocationCoordinates(new LatLng(((Alert) object).getLocalizacion().getLatitude(), ((Alert) object).getLocalizacion().getLongitude()));
                singleLocation.setImagen(((Alert) object).getFoto());
                locationList.add(singleLocation);
            }
        }
        return locationList;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateAdapter(ArrayList<ParseObject> listaParse){
        if(!listas_iguales(listaParse)) {
            System.out.println("HOLA77777777");
            //Comprobar que si hay una tarjeta (y por tanto marker seleccionado)
            if(cardSelected != null){
                System.out.println("HOLA8888888");
                lista_symbol.remove(markerSelected); //En este caso previamente se ha guardado el marcador asi que podemos usar un remove normal
                lista_symbol.add(0,markerSelected);
                listaParse.removeIf(obj -> (obj.hasSameId(cardSelected))); //Este remove es diferente pues el id de card selected entre iteraciones habrá cambiado
                listaParse.add(0,cardSelected);
                elementoActualRecyclerView = 0; //Actualizar la posicion para el scrollview evitando asi conflictos.
            }
            LocationRecyclerViewAdapter locationAdapter =
                    new LocationRecyclerViewAdapter(createRecyclerViewLocations(listaParse), mapboxMap);
            recyclerView.setAdapter(locationAdapter);
        }
        prevQuery = listaParse;
    }

    /** Metodo que chequea que la query actual y la anterior sean iguales, comprueba tamaño y objeto a objeto.
     * No se utiliza equals ya que entre una query y otra el id de los objetos devueltos por la query es distinto.
     * @param listaParse
     * @return
     */
    private boolean listas_iguales(ArrayList<ParseObject> listaParse){
        boolean res = true;

        if(listaParse.size() == prevQuery.size()){
            int i = 0;
            while(res && i < listaParse.size()){
                if(!listaParse.get(i).hasSameId(prevQuery.get(i)))
                    res = false;
                i++;
            }
        }
        else
            res = false;
        return  res;
    }

    class SingleRecyclerViewLocation {

            private String lugar;
            private byte[] imagen;
            private LatLng locationCoordinates;

            public String getNombreLugar() {
                return lugar;
            }

            public void setNombreLugar(String name) {
                this.lugar = name;
            }

            public byte[] getImagen(){
                return imagen;
            }
            public void setImagen(byte[] imagenBitmap){
                imagen = imagenBitmap;
            }

            public LatLng getLocationCoordinates() {
                return locationCoordinates;
            }

            public void setLocationCoordinates(LatLng locationCoordinates) {
                this.locationCoordinates = locationCoordinates;
            }
        }

        /*** Clase que se encarga de actualizar la vista es decir el contenido de las tarjetas **/
        static class LocationRecyclerViewAdapter extends
                RecyclerView.Adapter<LocationRecyclerViewAdapter.MyViewHolder> {

            private List<SingleRecyclerViewLocation> locationList;
            private MapboxMap map;

            public LocationRecyclerViewAdapter(List<SingleRecyclerViewLocation> locationList, MapboxMap mapBoxMap) {
                super();
                this.locationList = locationList;
                this.map = mapBoxMap;
            }

            @Override
            public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cardview_puntos, parent, false);
                return new MyViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(MyViewHolder holder, int position) {
                SingleRecyclerViewLocation singleRecyclerViewLocation = locationList.get(position);
                holder.lugar_textview.setText(singleRecyclerViewLocation.getNombreLugar());
                byte[] imagen = singleRecyclerViewLocation.getImagen();
                holder.imagen.setImageBitmap(BitmapFactory.decodeByteArray(imagen, 0, imagen.length));
                holder.setClickListener((view, position1) -> {
                    LatLng selectedLocationLatLng = locationList.get(position1).getLocationCoordinates();
                    CameraPosition newCameraPosition = new CameraPosition.Builder()
                            .target(selectedLocationLatLng)
                            .zoom(ZOOM_FIND)
                            .build();
                    map.animateCamera(CameraUpdateFactory
                            .newCameraPosition(newCameraPosition), CAMERA_ANIMATION);

                });
            }

            @Override
            public int getItemCount() {
                return locationList.size();
            }

            static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
                TextView lugar_textview;
                CardView cardview;
                ImageView imagen;
                Button botonVermas;
                Button botonIr;
                ItemClickListener clickListener;

                MyViewHolder(View view) {
                    super(view);
                    lugar_textview = view.findViewById(R.id.lugar_textview);
                    imagen = view.findViewById(R.id.imagen);
                    botonVermas = view.findViewById(R.id.boton_verMas);
                    botonVermas.setOnClickListener(v -> {

                    });
                    botonIr = view.findViewById(R.id.boton_IR);
                    botonIr.setOnClickListener(v -> {

                    });

                    cardview = view.findViewById(R.id.cardviewLugar);
                    cardview.setOnClickListener(this);
                }

                public void setClickListener(ItemClickListener itemClickListener) {
                    this.clickListener = itemClickListener;
                }

                @Override
                public void onClick(View view) {
                    clickListener.onClick(view, getLayoutPosition());
                }
            }
        }

        public interface ItemClickListener {
            void onClick(View view, int position);
        }
    }