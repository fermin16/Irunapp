package com.example.myapplication.ui.main;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.d.lib.tabview.TabView;
import com.example.myapplication.ActivityInicio;
import com.example.myapplication.Modelos.Preferencias;
import com.example.myapplication.Modelos.lugar;
import com.example.myapplication.R;
import com.example.myapplication.activityInfo;
import com.example.myapplication.mensajesHandler;
import com.example.myapplication.tipoLugar;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
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
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.LOCATION_SERVICE;
import static androidx.navigation.fragment.NavHostFragment.findNavController;


public class PestanaMapa extends Fragment implements OnMapReadyCallback, mensajesHandler, tipoLugar {

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
    private static final String SIMBOLO_NATURAL = "LUGAR NATURAL";
    private static final String SIMBOLO_SIN_CLSIFICAR = "SIN CLASIFICAR";
    private static final String SIMBOLO_MONUMENTO = "MONUMENTO";
    private static final String SIMBOLO_MUSEO = "MUSEO";
    private static final String SIMBOLO_EDIFICIO_HISTORICO = "EDIFICIO HISTORICO";
    private static final String SIMBOLO_GALERIA = "GALERIA DE ARTE";
    private static final String SIMBOLO_CASAS = "CASAS REGIONALES";

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

    //Macros para Ruta:
    private static final int DURACION_VIBRACION = 100;

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


    //Componentes para funcionalidades del mapa:
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent; //Variable para obtener la localizacion actual
    private AlertDialog alertDialog; //Guardar una variable para el alertDialog que permitira cerrarlo cuando deba crearse uno nuevo
    private  Snackbar snackbar; //Snackbar para mostrar información
    private BroadcastReceiver mGpsSwitchStateReceiver; //BoradcastReciver para saber cuando un usuario activa o desactiva gps
    private Context contextoBroadcast;
    public static Handler manejador; //Handler que maneja los mensajes del hilo hijo.

    //Componentes marcadores:
    private Symbol markerSelected; //Variable que indica si un marcador esta o no seleccionado.
    private int simboloActivado; //Valor entero que indique que un marker acaba de ser seleccionado por el listener de symbolmanager y no debe ser deseleccionado
    private ValueAnimator markerAnimator; //Animador para cuando se selecciona un marker del mapa
    private boolean noMarker; //Variable booleana que indica si el marker seleccionado estaba o no en la ultima query

    //Componentes cardView:
    private ArrayList<ParseObject> prevQuery; //Array list que almacena la query anterior
    private ParseObject cardSelected; //Guarda la card seleccionada en el mapa.
    private FloatingActionButton BotonTarjetas;

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

    //Elementos para navegacion:
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;
    private ProgressBar routeLoading;
    private FloatingActionButton BotonNavegacion;
    private static String modoRuta;
    private static TextView textodistancia;
    private static TextView textoDuracion;
    private static ImageView imagendistancia;
    private static ImageView imagenduracion;
    public static String rutaSeleccionada; //Ruta para seleccionar desde activity Info.
    private boolean isVisibleRoute;
    private boolean rutaMultipunto; //Variable que indica si la ruta es multipunto

    //Componentes comunicación con la lista:
    private PestanaLugares pestanaLugares;
    private static PestanaMapa pestana = null;
    private View root;


    public static PestanaMapa getPestana() {
        if (pestana == null) {
            pestana = new PestanaMapa();
        }
        return pestana;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(snackbar != null && snackbar.isShown()) {
                    updateNavigationRoute();
                    snackbar.dismiss();
                }
                else {
                    setEnabled(false); //this is important line
                    requireActivity().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getActivity(), getString(R.string.map_token));

        root = inflater.inflate(R.layout.activity_map, container, false);

        mapView = root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        routeLoading = root.findViewById(R.id.routeLoadingProgressBar);

        //Inicializar la pestaña de lugares:
        pestanaLugares = PestanaLugares.getPestana();

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
        manejador = new Handler(getActivity().getMainLooper()){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == MSG_QUERY) {
                    ArrayList<ParseObject> lista_puntos = (ArrayList<ParseObject>) msg.obj;
                    mostrarPuntos(lista_puntos);
                }
                else if(msg.what == MSG_RUTA_MULTIPLE){
                    ArrayList<ParseGeoPoint> puntosRuta = (ArrayList<ParseGeoPoint>) msg.obj;
                    ArrayList<Point> ruta = new ArrayList<>();
                    for(ParseGeoPoint geoPoint: puntosRuta){
                        ruta.add(Point.fromLngLat(geoPoint.getLongitude(),geoPoint.getLatitude()));
                    }
                    modoRuta = rutaSeleccionada;
                    getroute(ruta);
                }
                else{
                    if(BotonFlotante.isOpened())
                        BotonFlotante.close(true);
                    LatLng cardclick = (LatLng) msg.obj;
                    int i = 0;
                    boolean salir = false;
                    while (i < lista_symbol.size() && !salir){
                        Symbol simbolo = lista_symbol.get(i);
                        if(simbolo.getLatLng().getLatitude() == cardclick.getLatitude() && simbolo.getLatLng().getLongitude() == cardclick.getLongitude()) {
                            salir = true;
                            selectMarker(simbolo,true);
                        }
                        i++;
                    }
                    //Si era un click sobre el boton mostrar mas
                    if(msg.what == MSG_AMPLIA_CARD) {
                        selectCard(true);
                        // Ordinary Intent for launching a new activity
                        Intent intent = new Intent(getActivity(), activityInfo.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        //Animaciones para la transicion:
                        // Obtener la vistas de los objetos para la animacion:
                        View cardview = root.findViewById(R.id.cardviewLugar);

                        Pair<View, String> imagen = Pair.create(cardview.findViewById(R.id.imagen), getString(R.string.id_transicion_imagen));
                        Pair<View, String> nombre = Pair.create(cardview.findViewById(R.id.lugar_textview), getString(R.string.id_transicion_nombre));
                        Pair<View, String> boton_mas = Pair.create(cardview.findViewById(R.id.boton_verMas), getString(R.string.id_transicion_boton_mas));
                        Pair<View, String> boton_ruta = Pair.create(cardview.findViewById(R.id.boton_IR), getString(R.string.id_transicion_boton_ruta));

                        ActivityOptionsCompat options =
                                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), imagen,nombre,boton_mas,boton_ruta);

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
                    }
                    else if(msg.what == MSG_RUTA_TAB_LUGARES || msg.what == MSG_AMPLIA_CARD_LUGARES){
                        selectCard(false);
                        LocationRecyclerViewAdapter.MyViewHolder.iniciaComponentesRuta(recyclerLayoutManager.findViewByPosition(recyclerLayoutManager.findFirstVisibleItemPosition()));
                        modoRuta = rutaSeleccionada;
                        check_modo_ruta();
                    }
                    if(msg.what == MSG_RUTA || msg.what == MSG_RUTA_TAB_LUGARES){
                        selectCard(true);
                        ArrayList<Point> puntos = new ArrayList<>();
                        puntos.add(Point.fromLngLat(cardclick.getLongitude(),cardclick.getLatitude()));
                        getroute(puntos);
                    }
                }
            }
        };

        //Instanciamos el Boton flotante con el mnú y los botones de localización y cambio de estilo:
        BotonFlotante = root.findViewById(R.id.BotonFlotante);
        BotonFlotante.setClosedOnTouchOutside(true);
        BotonFlotante.getMenuIconView().setImageResource(R.drawable.botonmapaflotante);
        BotonFlotante.bringToFront();

        BotonMapa =  root.findViewById(R.id.BotonCambioMapa);
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

        BotonLocalizacion = root.findViewById(R.id.FindMe);
        BotonLocalizacion.setOnClickListener(v -> findMe());

        BotonBuscaPuntos = root.findViewById(R.id.BuscaPuntos);
        BotonBuscaPuntos.setEnabled(false);
        BotonBuscaPuntos.setOnClickListener(v -> {
            despertar(true);
            BotonBuscaPuntos.setEnabled(false);
        });

        BotonTarjetas = root.findViewById(R.id.BotonTarjetas);
        BotonTarjetas.setEnabled(false);
        BotonTarjetas.setOnClickListener(v -> {
            if(BotonTarjetas.getColorNormal() == ContextCompat.getColor(getActivity(), R.color.botonTarjetasNormal)) {
                getActivity().runOnUiThread(() ->BotonTarjetas.setColorNormal(ContextCompat.getColor(getActivity(), R.color.botonTarjetasDesactivado)));
                getActivity().runOnUiThread(() ->BotonTarjetas.setEnabled(false));
                ocultarTarjetas();
            }else{
                getActivity().runOnUiThread(() ->BotonTarjetas.setColorNormal(ContextCompat.getColor(getActivity(), R.color.botonTarjetasNormal)));
                mostrarTarjetas();
            }
        });

        BotonNavegacion = root.findViewById(R.id.BotonNavegacion);
        BotonNavegacion.setEnabled(false);
        BotonNavegacion.setOnClickListener(v ->{
            startNavigation(mapView);
        });

        gif_ajustes = root.findViewById(R.id.swipe_gif);
        panelDeslizante = root.findViewById(R.id.PanelDeslizante);
        panelDeslizante.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                BotonFlotante.close(true);
                BotonFlotante.animate().translationY((float) (panelDeslizante.getCurrentParallaxOffset()*OFFSET_FAB)).setDuration(0);
                BotonTarjetas.animate().translationY((float) (panelDeslizante.getCurrentParallaxOffset()*OFFSET_FAB)).setDuration(0);
                BotonNavegacion.animate().translationY((float) (panelDeslizante.getCurrentParallaxOffset()*OFFSET_FAB)).setDuration(0);
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
        max_puntos = Preferencias.getPuntos(getActivity());
        int selected_tab_distancia = Preferencias.getKilometros(getActivity());
        tiempo_refresco = Preferencias.getFrecuencia(getActivity());

        semaforo_ajustes = new Semaphore(1);
        tab_distancia = root.findViewById(R.id.kilometros);
        tab_distancia.setOnTabSelectedListener(index -> {
            setAjustesMapa(index, tab_distancia.getId(),true);
        });

        tab_frecuencia = root.findViewById(R.id.frecuencia);
        tab_frecuencia.setOnTabSelectedListener(index -> {
            setAjustesMapa(index, tab_frecuencia.getId(),true);
        });

        tab_puntos = root.findViewById(R.id.numpuntos);
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
                        dormir(); //Poner el hijo en pausa
                        Toast.makeText(context,getString(R.string.serviciosDesactivados),Toast.LENGTH_LONG).show();
                    }
                    else{ //Si se han activado
                        findMe(); //Mostrar localizacion
                        if (pause)
                            despertar(true);

                    }
                }
            }
        };
        contextoBroadcast = getActivity();
        //Registrar el broadcastReciever (tambien se pede hacer desde el manifest pero para ello deberiamos crear una clase que extienda a BroadcastReciver)
        contextoBroadcast.registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        set_style(BasicStyle);
        initRecyclerView(null);

        return root;
    }

    /* Metodo para chequear los resultados de las subactiviades:*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTIVAR_UBICACION: //Comprobar si el usuario ha activado o no los activity_ajustes de ubicacion.
                findMe();
                break;
        }
    }

    /* Metodo que se encarga de activar el elemento de localizacion, para ello pide permisos al usuario
     * (en caso de no haberlos pedido antes) y si el usuario acepta podra utilizar el mapa en caso
     * contrario se terminara el activity.*/
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        permissionsManager = new PermissionsManager(new PermissionsListener() {
            @Override
            public void onExplanationNeeded(List<String> permissionsToExplain) {
                Toast.makeText(getActivity(), getString(R.string.activarPermisosAjustes),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPermissionResult(boolean granted) {
                if (granted) {
                    enableLocationComponent(loadedMapStyle);
                } else {
                    if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                        Toast.makeText(getActivity(), getString(R.string.activarPermisos),Toast.LENGTH_LONG).show();
                    else
                        onExplanationNeeded(null);
                }
            }
        });
        //Comprobar si se han concedido los permisos de ubicacion:
        if (permissionsManager.areLocationPermissionsGranted(getActivity())) {
            if(checkLocationServices()) {//Comprobar si estan activados los servicios de red y ubicacion.
                //Obtener una instancia del componente de localizacion
                locationComponent = mapboxMap.getLocationComponent();

                // Activate with a built LocationComponentActivationOptions object
                locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(getActivity(), mapboxMap.getStyle()).build());
                if(checkLocationServices()) {//Comprobar si estan activados los servicios de red y ubicacion.
                    //Hacer el indicador visible
                    locationComponent.setLocationComponentEnabled(true);

                    Location miubicacion = locationComponent.getLastKnownLocation();
                    if(compruebaRangoUbicacion(miubicacion)) {
                        //Establecer el compas (brujula)
                        locationComponent.setRenderMode(RenderMode.COMPASS);

                        //Colocar el modo de la camara en Tracking
                        locationComponent.setCameraMode(CameraMode.TRACKING);

                        //Hacer zoom a la ubicacion del usuario
                        locationComponent.zoomWhileTracking(ZOOM_FIND);
                        //Comenzar a printear los puntos en el mapa
                        if(hijo == null || !hijo.isAlive())
                            buscaPuntos();
                    }
                }
            }
        }
        else{ //Si no se han concedido permisos y el usuario no ha marcado la opcion no volver a preguntar, solicitarlos
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    /* Metodo que permite chequear los servicios de acceso para ver si estan activados o no. Devuelve
     *  falso si estan desactivados y verdadero en caso contrario.*/
    public boolean checkLocationServices(){
        LocationManager lm = (LocationManager)getActivity().getSystemService(LOCATION_SERVICE);
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
            alertDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.gps_desactivado)
                    .setPositiveButton(R.string.activar_gps, (paramDialogInterface, paramInt) -> startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),ACTIVAR_UBICACION)).setNegativeButton(R.string.cancelar_gps, (dialog, which) -> Toast.makeText(getActivity(),getString(R.string.serviciosDesactivados),Toast.LENGTH_LONG).show())
                    .show();
            return false;
        }
        else if(!network){
            //Notificar al usuario que tiene desactivados los servicios de red.
            alertDialog = new AlertDialog.Builder(getActivity())
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

                        Location miubicacion = locationComponent.getLastKnownLocation();
                        if(compruebaRangoUbicacion(miubicacion)) {
                            //Colocar el modo de la camara en Tracking
                            locationComponent.setCameraMode(CameraMode.TRACKING);

                            //Establecer el compas (brujula)
                            locationComponent.setRenderMode(RenderMode.COMPASS);

                            //Colocar el modo de la camara en Tracking
                            locationComponent.setCameraMode(CameraMode.TRACKING);

                            //Hacer zoom a la ubicacion del usuario
                            locationComponent.zoomWhileTracking(ZOOM_FIND);
                        }
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
                Toast.makeText(getActivity(), getString(R.string.cargando_mapa),Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Función que comprueba so una ubicación está dentro de rango.
     * @param miubicacion
     * @return
     */
    private boolean compruebaRangoUbicacion(Location miubicacion){
        if(miubicacion!=null) {
            LatLng coordenadas = new LatLng(miubicacion.getLatitude(),miubicacion.getLongitude());
            if (!RESTRICTED_BOUNDS_AREA.contains(coordenadas)) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.ubicacionErronea), Toast.LENGTH_LONG).show());
                dormir();
                return false;
            }
            else {
                if(hijo == null || !hijo.isAlive())
                    buscaPuntos();
                else
                    despertar(false);
                return true;
            }
        }
        else {
            snackbar = Snackbar.make(recyclerView, getResources().getString(R.string.ubicacionNoEncontrada), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(getResources().getString(R.string.reintentar), view -> {
                TabLayout tabhost = (TabLayout) ((Activity)this.getContext()).findViewById(R.id.tabs);
                tabhost.getTabAt(1).select();
                findMe();
            });
            snackbar.setActionTextColor(Color.RED);

            getActivity().runOnUiThread(() ->snackbar.show());
        }
        return false;
    }

    /** Este método permite cambiar de estilo de mapa evitando posibles riesgos a la hora de obtener la ubicación o de hacer un cambio rápido de estilos**/
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void set_style(final Style.Builder newStyle){
        try {
            loading_style.acquire();
            mapView.getMapAsync(mapboxMap -> {
                PestanaMapa.this.mapboxMap = mapboxMap;
                //Iinicializar la variable que almacenara los añadidos del mapa
                loadedStyle = style -> {
                    if(symbolManager!=null)
                        symbolManager.deleteAll();
                    // Set up a SymbolManager instance
                    symbolManager = new SymbolManager(mapView, mapboxMap, style);
                    // Add click listener and change the symbol to a cafe icon on click
                    symbolManager.addClickListener(symbol -> {
                        simboloActivado = 1;
                        selectMarker(symbol,true);
                        selectCard(true);
                    });
                    setSymbols(style);
                    if(mapListener !=null)
                        mapboxMap.removeOnMapClickListener(mapListener);
                    //Añadir Listener al mapa:
                    mapboxMap.addOnMapClickListener(mapListener = point -> {
                        //Decrementar el valor de simboloActivado si es igual a 0 acaba de ocurrir un evento en symbolManager y no debemos deseleccionar el marcador
                        simboloActivado--;
                        if((markerSelected != null) && simboloActivado!=0){
                            if(BotonFlotante.isOpened())
                                BotonFlotante.close(true);
                            deselectMarker();
                        }
                        updateNavigationRoute();
                        return false;
                    });
                    symbolManager.setIconAllowOverlap(true);
                    symbolManager.setTextAllowOverlap(true);
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
        Toast.makeText(getActivity(),getString(R.string.obteniendoPuntos)+" "+aux+" "+units+" "+getString(R.string.obteniendoPuntos2),Toast.LENGTH_LONG).show();
        pause = false;
        Runnable hilo = () -> {
            Location myLocation;
            do{
                try{
                    //Si la actividad es pausada o pasa a segundo plano debemos pausar el hilo
                    if(pause){
                        synchronized (hijo){
                            hijo.wait();
                        }

                        if(BotonBuscaPuntos.isEnabled())
                            getActivity().runOnUiThread(() -> BotonBuscaPuntos.setEnabled(false));
                    }
                    //Comprobar que la componente de localizacion esta activada
                    if (locationComponent != null) {
                        //Obtener la ultima ubicacion del usuario
                        myLocation = locationComponent.getLastKnownLocation();
                        /*Si la ubicacion no es nula, hacer una query que obtenga todos los punto a una distancia menor o igual
                         * a MAX_DISTANCIA y mostrarlos en el mapa. Para ello obtener la localizacion actual del usuario */
                        if (compruebaRangoUbicacion(myLocation)) {
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

    public void setSymbols(Style style){
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.symbol_tree);
        style.addImage(SIMBOLO_NATURAL,bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.symbol_house);
        style.addImage(SIMBOLO_CASAS,bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.symbol_historic_building);
        style.addImage(SIMBOLO_EDIFICIO_HISTORICO,bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.symbol_art_gallery);
        style.addImage(SIMBOLO_GALERIA,bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.symbol_monument);
        style.addImage(SIMBOLO_MONUMENTO,bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.symbol_museum);
        style.addImage(SIMBOLO_MUSEO,bm);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.symbol_sin_clasificar);
        style.addImage(SIMBOLO_SIN_CLSIFICAR,bm);
    }
    /** Método que realiza la query a la BBDD para obtener los puntos cercanos **/
    private void queryPuntos(Location myLocation ){
        try {

            ParseGeoPoint userLocation = new ParseGeoPoint(myLocation.getLatitude(), myLocation.getLongitude());
            ParseQuery<ParseObject> query = ParseQuery.getQuery("lugar");
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
        msg.what = MSG_QUERY;
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
                if(markerSelected == null)
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
                for (ParseObject punto : listaPuntos) {
                    ParseGeoPoint loc = ((lugar) punto).getLocalizacion();
                    int tipoPunto = ((lugar)punto).getTipo();
                    //Si el marker seleccionado está en la lista poner el tamaño de su icono ampliado y poner noMarker a false (El marker se ha recuperado en la ultima query)
                    if (markerSelected != null && (loc.getLatitude() == markerSelected.getLatLng().getLatitude() && loc.getLongitude() == markerSelected.getLatLng().getLongitude())) {
                        icon_size = TAMANO_MAX_ICONO;
                        noMarker=false;
                    }
                    else
                        icon_size = TAMANO_MIN_ICONO;
                    String iconoPunto = SIMBOLO_SIN_CLSIFICAR;
                    if(tipoPunto == LUGAR_NATURAL)
                        iconoPunto = SIMBOLO_NATURAL ;
                    else if(tipoPunto == MONUMENTO)
                        iconoPunto = SIMBOLO_MONUMENTO ;
                    else if(tipoPunto == MUSEO)
                        iconoPunto = SIMBOLO_MUSEO;
                    else if(tipoPunto == EDIFICIO_HISTORICO)
                        iconoPunto = SIMBOLO_EDIFICIO_HISTORICO ;
                    else if(tipoPunto == GALERIA_ARTE)
                        iconoPunto = SIMBOLO_GALERIA ;
                    else if(tipoPunto == CASAS_REGIONALES)
                        iconoPunto = SIMBOLO_CASAS ;
                    // Add symbol at specified lat/lon
                    Symbol symbol = symbolManager.create(new SymbolOptions()
                            .withLatLng(new LatLng(loc.getLatitude(), loc.getLongitude()))
                            .withIconImage(iconoPunto)
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
            //Comprobar si habia marker seleccionado y no se ha recuperado en la query
            if(markerSelected != null && !lista_symbol.contains(markerSelected)){
                markerSelected = symbolManager.create(new SymbolOptions()
                        .withLatLng(markerSelected.getLatLng())
                        .withIconImage(markerSelected.getIconImage())
                        .withIconSize(TAMANO_MAX_ICONO)
                        .withDraggable(markerSelected.isDraggable()));
                lista_symbol.add(0,markerSelected);
            }
            // Actualizar el adapter del recyclerView:
            updateAdapter(listaPuntos);
            pestanaLugares.updateList(listaPuntos);
            loading_style.release();
        }
    }

    private  void sinPuntos() {
        dormir();
        BotonBuscaPuntos.setEnabled(true);
        noMarker = true;
        if (alertDialog != null)
            alertDialog.dismiss();
        alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.puntos_no_localizados)
                .setPositiveButton(R.string.volver_buscar, (paramDialogInterface, paramInt) -> {
                    if (hijo != null && hijo.isAlive()) {
                        despertar(true);
                    }
                }).setNegativeButton(R.string.cancelar_busqueda, (dialog, which) -> {
                    Toast.makeText(getActivity(), R.string.busqueda_detenida, Toast.LENGTH_LONG).show();
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
    private void selectCard(boolean Smooth){
        if(markerSelected != null) {
            int index = lista_symbol.indexOf(markerSelected);
            cardSelected = prevQuery.get(index);
            if(!Smooth)
                recyclerView.scrollToPosition(index);
            else
                recyclerView.smoothScrollToPosition(index);
        }
    }

    /* Metodo que permite seleccionar un marcador del mapa haciendolo más grande mediante una animación.
     * Primero comprobar que el marcador seleccionado no es el mismo que el que se acaba de seleccionar.*/
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void selectMarker(final Symbol symbol, boolean anim) {
        if(anim)
            animateCamera(symbol);
        if((markerSelected == null) || (!symbol.equals(markerSelected))) {
            deselectMarker();
            updateNavigationRoute();
            noMarker = false; //Lo ponemos a false evitando asi que si es la primera query y no teniamos un punto seleccionado al deseleccionar no se borre el punto.s
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
            if(lista_symbol.remove(markerSelected)) {
                if (noMarker) {
                    //Solo estaba disponible el punto seleccionado:
                    if (lista_symbol.isEmpty()) {
                        cardSelected = null;
                        BotonTarjetas.setEnabled(false);
                        BotonTarjetas.setColorNormal(ContextCompat.getColor(getActivity(), R.color.botonTarjetasDesactivado));
                        recyclerView.animate()
                                .alpha(0f)
                                .setDuration(ANIMACION_TARJETAS)
                                .setListener(null);

                        new Handler().postDelayed(() -> {
                            recyclerView.setVisibility(View.GONE);
                            ArrayList voidList = new ArrayList<>();
                            updateAdapter(voidList);
                            pestanaLugares.updateList(voidList);
                        },ANIMACION_TARJETAS);
                    }
                    //Habia más puntos ademas del seleccionado pero en la ultima query no se recupero (ya no esta en alcance)
                    else {
                        ArrayList<ParseObject> query_elimnaTarjeta = new ArrayList<>(prevQuery);
                        query_elimnaTarjeta.removeIf(obj -> obj.hasSameId(cardSelected));
                        cardSelected = null;
                        updateAdapter(query_elimnaTarjeta);
                    }
                } else {
                    //Punto seleccionado sigue al alcance
                    cardSelected = null;
                    markerSelected.setIconSize(TAMANO_MIN_ICONO);
                    lista_symbol.add(index, markerSelected);
                }
                markerSelected = null;
                symbolManager.deleteAll();
                symbolManager.update(lista_symbol);
            }
        }
    }

    /** Metodo para configurar los ajustes del mapa mediante los tabView del menu desplegable,
     * se recie el id del TaView y el index del mismo.
     **/
    private void setAjustesMapa(int index, int tabViewId, boolean muestraMensaje){
        try {
            //Si el hijo no está muerto:
            if(hijo != null && hijo.isAlive()){
                dormir();
            }
            semaforo_ajustes.acquire();
            if(tabViewId == R.id.kilometros) {
                if (index == 0) {
                    max_distancia = TAB_1_DISTANCIA;
                } else if (index == 1) {
                    max_distancia = TAB_2_DISTANCIA;
                } else
                    max_distancia = TAB_3_DISTANCIA;
                Preferencias.guardaKilometros(getActivity(), index);
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
                Preferencias.guardaFrecuencia(getActivity(),index);
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
                Preferencias.guardaPuntos(getActivity(),index);
            }
            semaforo_ajustes.release();
                despertar(false);
            if(muestraMensaje) {
                String units;
                Double aux;
                if (max_distancia < 1) {
                    units = "m";
                    aux = max_distancia * 1000;
                } else {
                    units = "km";
                    aux = max_distancia;
                }
                Toast.makeText(getActivity(), getString(R.string.obteniendoPuntos) + " " + aux + " " + units + " " + getString(R.string.obteniendoPuntos2), Toast.LENGTH_LONG).show();
            }
            } catch (InterruptedException e) {
            Toast.makeText(getActivity(), R.string.error_ajustes_mapa, Toast.LENGTH_SHORT).show();
        }
    }

    /* Metodo que se encarga de despertar al hijo*/
    public void despertar(boolean mensajePausa){
        pause = false;
        if(mensajePausa)
            getActivity().runOnUiThread(() ->Toast.makeText(getActivity(), getString(R.string.busqueda_reanudada), Toast.LENGTH_LONG).show());
        if(hijo != null && hijo.isAlive()) {
            synchronized (hijo) {
                hijo.notifyAll(); //Notificar al hijo que puede continuar su ejecucion
            }
        }
        else if(locationComponent!=null && locationComponent.isLocationComponentActivated())
            buscaPuntos();
    }

    /** Metodo para dormir o pausar al hijo **/
    public void dormir(){
        if(hijo != null && hijo.isAlive()) {
            if(!pause) {
                pause = true;
                hijo.interrupt();
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
                .setListener(null);
        new Handler().postDelayed(() -> {
            recyclerView.setVisibility(View.GONE);
            if(!BotonTarjetas.isEnabled() && !rutaMultipunto)
                BotonTarjetas.setEnabled(true);
        },ANIMACION_TARJETAS);
    }

    public void check_modo_ruta() {
        if (modoRuta != null) {
            if (modoRuta == DirectionsCriteria.PROFILE_DRIVING)
                imagendistancia.setImageResource(R.drawable.modo_coche);
            else if (modoRuta == DirectionsCriteria.PROFILE_CYCLING)
                imagendistancia.setImageResource(R.drawable.modo_bici);
            else
                imagendistancia.setImageResource(R.drawable.modo_andar);
            rutaSeleccionada = null;
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
            if(mGpsSwitchStateReceiver != null)
                contextoBroadcast.registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)); //Registrar el broadcastReciever (tambien se pede hacer desde el manifest pero para ello deberiamos crear una clase que extienda a BroadcastReciver)
            if(navigationMapRoute != null)
                navigationMapRoute.onStart();
            if(pause)
                despertar(true);
        }catch(SecurityException ex){
            Log.v("Última localización","No se pudo obtener la última localizacion del usuario");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if(pause)
            despertar(true);
        if(rutaSeleccionada != null){
            if(cardSelected != null) {
                modoRuta = rutaSeleccionada;
                ParseGeoPoint puntoSeleccionado = ((lugar) cardSelected).getLocalizacion();
                check_modo_ruta();
                ArrayList<Point> puntos = new ArrayList<>();
                puntos.add(Point.fromLngLat(puntoSeleccionado.getLongitude(), puntoSeleccionado.getLatitude()));
                getroute(puntos);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onPause() {
        dormir(); //Pausar el hilo hijo
//        getActivity().enterPictureInPictureMode();
        super.onPause();
        mapView.onPause();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onStop() {
        dormir(); //Pausar el hilo hijo
        try {
            if(mGpsSwitchStateReceiver != null) {
                contextoBroadcast.unregisterReceiver(mGpsSwitchStateReceiver); //Eliminar el broadcastReciever
            }
        } catch(IllegalArgumentException e) {
            Log.v("Problema BroadcastReceiver","El BroadcastReciever ya se había eliminado");
        }
        finally {
            super.onStop();
            if(navigationMapRoute != null)
                navigationMapRoute.onStop();
            mapView.onStop();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onDestroy() {
        stop = true;
        if(hijo != null) {
            if(hijo.getState() == Thread.State.TIMED_WAITING) //Si el hijo está durmiendo:
                hijo.interrupt(); //Interrumpir el hilo
        }
        try {
            if(mGpsSwitchStateReceiver != null) {
                contextoBroadcast.unregisterReceiver(mGpsSwitchStateReceiver); //Eliminar el broadcastReciever
            }
        } catch(IllegalArgumentException e) {
            Log.v("Problema BroadcastReceiver","El BroadcastReciever ya se había eliminado");
        }
        finally{
            //Eliminar el animador.
            if (markerAnimator != null) {
                markerAnimator.cancel();
            }
            super.onDestroy();
            mapView.onDestroy();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /****
     **********************************************************
     **********************************************************
     **********************************************************
     **********************************************************
     **********************************************************
     **********************************************************
     * Parte para la navegacion del mapa **/
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {

    }

    @SuppressLint("MissingPermission")
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(DURACION_VIBRACION, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(DURACION_VIBRACION);
        }
    }

    private void startNavigation(View v){
        boolean simularRuta = true;
        if(currentRoute !=null) {
            NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                    .directionsRoute(currentRoute)
                    .shouldSimulateRoute(simularRuta)
                    .build();
            vibrate();
//            onNavigationMode = true;
//            getActivity().enterPictureInPictureMode();
            NavigationLauncher.startNavigation(getActivity(), options);
        }
    }
//
//    @Override
//    public void onPictureInPictureModeChanged(boolean isInPIPMode) {
//        if (isInPIPMode) {
//            getActivity().findViewById(R.id.tabs).setVisibility(View.INVISIBLE);
//        } else {
//            getActivity().findViewById(R.id.tabs).setVisibility(View.VISIBLE);
//        }
//        super.onPictureInPictureModeChanged(isInPIPMode);
//    }

    private void getroute(List<Point> puntosDestino) {
        if (locationComponent !=null) {
            Location localizacionActual = locationComponent.getLastKnownLocation();
            if (compruebaRangoUbicacion(localizacionActual)) {
                int tamanoLista = puntosDestino.size();
                routeLoading.setVisibility(View.VISIBLE);
                Point PuntoOrigen = Point.fromLngLat(localizacionActual.getLongitude(), localizacionActual.getLatitude());
                NavigationRoute.Builder builder = NavigationRoute.builder(getActivity())
                        .accessToken(getString(R.string.map_token))
                        .origin(PuntoOrigen);
                if(tamanoLista == 1)
                    builder.destination(puntosDestino.get(0));
                else {
                    for (int i = 0; i < tamanoLista; i++) {
                        Point waypoint = puntosDestino.get(i);
                        if(i == tamanoLista-1)
                            builder.destination(waypoint);
                        else
                            builder.addWaypoint(waypoint);
                    }
                }
                        builder.profile(modoRuta)
                        .alternatives(true)
                        .build()
                        .getRoute(new Callback<DirectionsResponse>() {
                            @Override
                            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                                if(response.body() != null && response.body().routes().size() >= 1){
                                    currentRoute = response.body().routes().get(0);

                                    //Printear y dibujar las distancias y sus simbolos
                                    if(tamanoLista == 1) {
                                        updateNavigationRoute();
                                        rutaMultipunto = false;
                                        String distancia = String.format("%.2f",currentRoute.distance()/1000);
                                        textodistancia.setText("  "+distancia+" KM   ");
                                        try {
                                            String duracion = String.format("%06d", currentRoute.duration().intValue());
                                            DateFormat format = new SimpleDateFormat("HHmmss", Locale.US);
                                            Date date = format.parse(duracion);
                                            textoDuracion.setText(date.getHours()+":"+date.getMinutes()+":"+date.getSeconds());
                                        } catch (ParseException e) {
                                            Toast.makeText(getActivity(), R.string.error_calculo_tiempo, Toast.LENGTH_SHORT).show();
                                        }
                                        imagendistancia.setVisibility(View.VISIBLE);
                                        imagenduracion.setVisibility(View.VISIBLE);
                                    }else {
                                        updateNavigationRoute();
                                        rutaMultipunto = true;
                                        String distancia = String.format("%.2f",currentRoute.distance()/1000);
                                        try {
                                            String duracion = String.format("%06d", currentRoute.duration().intValue());
                                            DateFormat format = new SimpleDateFormat("HHmmss", Locale.US);
                                            Date date = format.parse(duracion);
                                            snackbar = Snackbar.make(recyclerView, getResources().getString(R.string.info_ruta)+"\n"+getResources().getString(R.string.DistanciaRuta)+" "+distancia+"KM "+getResources().getString(R.string.DistanciaRuta)+" "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds(), Snackbar.LENGTH_INDEFINITE);
                                            snackbar.setAction(getResources().getString(R.string.cerrar_navegacion), view -> {
                                                updateNavigationRoute();
                                            });
                                            snackbar.setActionTextColor(Color.CYAN);
                                            getActivity().runOnUiThread(() ->snackbar.show());
                                            if(BotonTarjetas.getColorNormal() == ContextCompat.getColor(getActivity(), R.color.botonTarjetasNormal))
                                                BotonTarjetas.performClick();
                                            else
                                                BotonTarjetas.setEnabled(false);
                                            dormir();
                                        } catch (ParseException e) {
                                            Toast.makeText(getActivity(), R.string.error_calculo_tiempo, Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                    navigationMapRoute.addRoute(currentRoute);
                                    routeLoading.setVisibility(View.INVISIBLE);
                                    if(alertDialog != null)
                                        alertDialog.dismiss();
                                    alertDialog = new AlertDialog.Builder(getActivity())
                                            .setMessage(R.string.iniciar_ruta )
                                            .setPositiveButton(R.string.iniciar, (paramDialogInterface, paramInt) -> startNavigation(mapView)).setNegativeButton(R.string.cancelar_navegavecion,null).show();
                                    BotonNavegacion.setEnabled(true);
                                    isVisibleRoute = true;
                                }
                            }
                            @Override
                            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                                Snackbar.make(mapView, R.string.error_ruta, Snackbar.LENGTH_SHORT).show();
                            }
                        });
                return;
            }
        }
        Snackbar.make(mapView, R.string.error_ruta, Snackbar.LENGTH_SHORT).show();
    }

    private void cerrarRutaMultiPunto(){
        updateNavigationRoute();
        BotonTarjetas.setEnabled(true);
        BotonTarjetas.performClick();
        despertar(true);
        rutaMultipunto = false;
    }

    private void updateNavigationRoute() {
        if(navigationMapRoute != null){
            if(isVisibleRoute) {
                navigationMapRoute.updateRouteArrowVisibilityTo(false);
                navigationMapRoute.updateRouteVisibilityTo(false);
                if (BotonNavegacion.isEnabled())
                    BotonNavegacion.setEnabled(false);
                if (!rutaMultipunto) {
                    textodistancia.setText("");
                    textoDuracion.setText("");
                    imagendistancia.setVisibility(View.INVISIBLE);
                    imagenduracion.setVisibility(View.INVISIBLE);
                } else {
                    snackbar.dismiss();
                    BotonTarjetas.setEnabled(true);
                    BotonTarjetas.performClick();
                    despertar(true);
                    rutaMultipunto = false;
                }
                isVisibleRoute = false;
            }
        }
        else{
            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
        }
    }


        /****
         **********************************************************
         **********************************************************
         **********************************************************
         **********************************************************
         **********************************************************
         **********************************************************
         * Parte para las infoWindows que se mostraran en el mapa **/

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initRecyclerView(List<ParseObject> listaParse) {
        recyclerView = root.findViewById(R.id.rv_on_top_of_map);
        PestanaMapa.LocationRecyclerViewAdapter locationAdapter =
                new PestanaMapa.LocationRecyclerViewAdapter(createRecyclerViewLocations(listaParse), mapboxMap, getActivity(),true);
        recyclerLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, true);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(locationAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        recyclerView.setOnFlingListener(null);

        //Seleccionar card en scroll (Desactivado por el momento):
        RecyclerView.OnScrollListener listenerScroll = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int elementoVisible = recyclerLayoutManager.findFirstVisibleItemPosition();

                    if (elementoVisible != elementoActualRecyclerView) {
                        selectMarker(lista_symbol.get(elementoVisible),false);
                        selectCard(true);
                        elementoActualRecyclerView = elementoVisible;
                    }
                }
            }
        };
        recyclerView.addOnScrollListener(listenerScroll);
        snapHelper.attachToRecyclerView(recyclerView);
    }

    private List<PestanaMapa.SingleRecyclerViewLocation> createRecyclerViewLocations(List<ParseObject> listaParse) {
        ArrayList<PestanaMapa.SingleRecyclerViewLocation> locationList = new ArrayList<>();
        if(listaParse != null && !listaParse.isEmpty()) {
            for (ParseObject object : listaParse) {
                PestanaMapa.SingleRecyclerViewLocation singleLocation = new PestanaMapa.SingleRecyclerViewLocation();
                singleLocation.setNombreLugar(((lugar) object).getNombre());
                singleLocation.setLocationCoordinates(new LatLng(((lugar) object).getLocalizacion().getLatitude(), ((lugar) object).getLocalizacion().getLongitude()));
                singleLocation.setImagen(((lugar) object).getFoto());
                locationList.add(singleLocation);
            }
        }
        return locationList;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateAdapter(ArrayList<ParseObject> listaParse){
        if (!listas_iguales(listaParse)) {
            boolean update_info = true; //Valor booleano para saber ssi una ruta esta activa y no borrar la distancia y tiempo
            //Comprobar que si hay una tarjeta (y por tanto marker seleccionado)
            if (cardSelected != null) {
                lista_symbol.remove(markerSelected); //En este caso previamente se ha guardado el marcador asi que podemos usar un remove normal
                lista_symbol.add(0, markerSelected);
                listaParse.removeIf(obj -> (obj.hasSameId(cardSelected))); //Este remove es diferente pues el id de card selected entre iteraciones habrá cambiado
                listaParse.add(0, cardSelected);
                elementoActualRecyclerView = 0; //Actualizar la posicion para el scrollview evitando asi conflictos.
                if (isVisibleRoute) {
                    update_info = false;
                }
            }
            PestanaMapa.LocationRecyclerViewAdapter locationAdapter =
                    new PestanaMapa.LocationRecyclerViewAdapter(createRecyclerViewLocations(listaParse), mapboxMap, getActivity(), update_info);
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
    public static class LocationRecyclerViewAdapter extends
            RecyclerView.Adapter<PestanaMapa.LocationRecyclerViewAdapter.MyViewHolder> {

        private List<PestanaMapa.SingleRecyclerViewLocation> locationList;
        private MapboxMap map;
        private Context contextoApp;
        private boolean update_info;
        private ArrayList<View> list_view;

        public LocationRecyclerViewAdapter(List<PestanaMapa.SingleRecyclerViewLocation> locationList, MapboxMap mapBoxMap, Context contextoApp, boolean update_info) {
            super();
            this.locationList = locationList;
            this.map = mapBoxMap;
            this.contextoApp = contextoApp;
            this.update_info = update_info;
            list_view = new ArrayList<>();
        }

        @Override
        public PestanaMapa.LocationRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cardview_puntos, parent, false);
            list_view.add(itemView);
            return new PestanaMapa.LocationRecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(PestanaMapa.LocationRecyclerViewAdapter.MyViewHolder holder, int position) {
            //Si se trata de la tarjeta seleccionada (que ocupará la posición 0) y no hay que updatear la info:
            if(!update_info && position == 0){
                holder.restauraComponentes(holder.itemView);
            }
            PestanaMapa.SingleRecyclerViewLocation singleRecyclerViewLocation = locationList.get(position);
            holder.lugar_textview.setText(singleRecyclerViewLocation.getNombreLugar());
            byte[] imagen = singleRecyclerViewLocation.getImagen();
            if(imagen!=null)
                holder.imagen.setImageBitmap(BitmapFactory.decodeByteArray(imagen, 0, imagen.length));
            holder.setClickListener((view, position1) -> {
                LatLng selectedLocationLatLng = locationList.get(position1).getLocationCoordinates();
                Message msg = new Message();
                msg.obj = selectedLocationLatLng;
                msg.what = MSG_CLICK_CARD;
                manejador.sendMessage(msg);
            });
            holder.coordenadas = singleRecyclerViewLocation.getLocationCoordinates();
            holder.contextoApp = contextoApp;
        }

        @Override
        public int getItemCount() {
            return locationList.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView lugar_textview;
            CardView cardview;
            ImageView imagen;
            Button botonVermas;
            Button botonIr;
            LatLng coordenadas;
            PestanaMapa.ItemClickListener clickListener;
            Context contextoApp;

            @SuppressLint("RestrictedApi")
            MyViewHolder(View view) {
                super(view);
                lugar_textview = view.findViewById(R.id.lugar_textview);
                imagen = view.findViewById(R.id.imagen);

                botonVermas = view.findViewById(R.id.boton_verMas);
                botonVermas.setOnClickListener(v -> {
                    Message msg = new Message();
                    msg.obj = coordenadas;
                    msg.what = MSG_AMPLIA_CARD;
                    manejador.sendMessage(msg);
                    iniciaComponentesRuta(view);
                });
                botonIr = view.findViewById(R.id.boton_IR);
                botonIr.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(contextoApp, v);
                    popup.getMenuInflater().inflate(R.menu.popup_menu,popup.getMenu());

                    popup.setOnMenuItemClickListener(item -> {
                        iniciaComponentesRuta(view);
                        if(item.getItemId() == R.id.modoCoche) {
                            modoRuta = DirectionsCriteria.PROFILE_DRIVING;
                            imagendistancia.setImageResource(R.drawable.modo_coche);
                        }
                        else if(item.getItemId() == R.id.modoBicicleta) {
                            modoRuta = DirectionsCriteria.PROFILE_CYCLING;
                            imagendistancia.setImageResource(R.drawable.modo_bici);
                        }
                        else {
                            modoRuta = DirectionsCriteria.PROFILE_WALKING;
                            imagendistancia.setImageResource(R.drawable.modo_andar);
                        }

                        Message msg = new Message();
                        msg.obj = coordenadas;
                        msg.what = MSG_RUTA;
                        manejador.sendMessage(msg);
                        return true;
                    });
                    MenuPopupHelper menuHelper = new MenuPopupHelper(contextoApp, (MenuBuilder) popup.getMenu(), v);
                    menuHelper.setForceShowIcon(true);
                    menuHelper.show();
                });
                cardview = view.findViewById(R.id.cardviewLugar);
                cardview.setOnClickListener(this);
            }

            public static void iniciaComponentesRuta(View view){
                boolean vistaResfrescada = false;
                if(imagenduracion != null) {
                    if(!imagenduracion.equals(view.findViewById(R.id.imagen_duracion_cardview))){
                        vistaResfrescada = true;
                        textodistancia.setText("");
                        textoDuracion.setText("");
                        imagendistancia.setVisibility(View.INVISIBLE);
                        imagenduracion.setVisibility(View.INVISIBLE);
                    }
                }
                if(imagenduracion == null || vistaResfrescada){
                    imagendistancia = view.findViewById(R.id.imagen_distancia_cardview);
                    textodistancia = view.findViewById(R.id.texto_distancia_cardview);
                    textoDuracion = view.findViewById(R.id.texto_duracion_cardview);
                    imagenduracion = view.findViewById(R.id.imagen_duracion_cardview);
                    imagenduracion.setVisibility(view.INVISIBLE);
                    imagenduracion.setImageResource(R.drawable.icono_duracion_ruta);
                    imagendistancia.setVisibility(view.INVISIBLE);
                }
            }

            public void restauraComponentes(View view){
                Drawable imagen = imagendistancia.getDrawable();
                CharSequence textoDistancia = textodistancia.getText();
                CharSequence textoduracion = textoDuracion.getText();
                iniciaComponentesRuta(view);
                imagendistancia.setImageDrawable(imagen);
                textodistancia.setText(textoDistancia);
                textoDuracion.setText(textoduracion);
                imagenduracion.setVisibility(View.VISIBLE);
                imagendistancia.setVisibility(view.VISIBLE);
            }

            public void setClickListener(PestanaMapa.ItemClickListener itemClickListener) {
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