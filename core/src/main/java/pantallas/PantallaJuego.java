package pantallas;

import com.badlogic.gdx.Game; 
import com.MiEmpresa.OperacionSinRetorno.Principal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import bala.Bala;
import enemigos.Enemigo;
import audio.ControladorDeAudio;
import configuracion.ConfiguracionJuego;
import configuracion.EstadoJugador;
import enemigos.ControladorEnemigos;
import hud.HUD;
import jugador.ControladorEntrada;
import jugador.Jugador;
import jugador.JugadorRemoto;
import jugador.JugadorRemotoControlable;
import palmeras.GestorPalmeras;
import palmeras.PuntoAgarre;
import powerups.PowerUp;
import network.NetworkManager;
import packets.*;
import configuracion.EstadoJugador;

public class PantallaJuego implements Screen {
    private final Game game;
    private final Principal principal;
    private OrthographicCamera camara;
    private Viewport viewport;
    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private TiledMapTileLayer capaColisiones;
    private ShapeRenderer shapeRenderer;
    private TiledMapTileLayer capaArbustos;
    private ConfiguracionJuego config;
    private HUD hud;
    private Jugador jugador;
    private SpriteBatch batch;
    private ControladorDeAudio controladorDeAudio;
    private ControladorEntrada controladorDeEntradas;
    private ArrayList<Bala> balas = new ArrayList<>();
    private float tiempoScreenShake = 0f;
    private float intensidadShake = 5f;
    private float finNivelX, finNivelY, finNivelWidth, finNivelHeight;
    private boolean cambiandoNivel = false;
    private boolean nivelYaProcesado = false;
    
    // MODO SPECTADOR Y GAME OVER
    private boolean jugadorLocalMuerto = false;
    private boolean jugadorRemotoMuerto = false;
    private boolean modoSpectate = false;
    private PantallaSpectate pantallaSpectate;
    
    // === PAUSA ===
    private boolean juegoPausado = false;
    private BitmapFont fontPausa;
    private String[] opcionesPausa = {"Reanudar", "Menu Principal", "Salir"};
    private int opcionSeleccionada = 0;
    private float tiempoUltimoInput = 0f;
    private final float DELAY_INPUT = 0.6f;
    
    // === TIENDA ===
    private boolean tiendaActiva = false;
    private BitmapFont fontTienda;
    private float tiendaX, tiendaY;
    
    private static class ItemTienda {
        String nombre;
        String descripcion;
        int precio;
        String tipo;
        int daño;
        int municionMaxima;
        int municionReserva;
        float tiempoRecarga;
        
        ItemTienda(String nombre, String descripcion, int precio, String tipo) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.precio = precio;
            this.tipo = tipo;
        }
        
        ItemTienda(String nombre, int precio, int daño, int municionMaxima, int municionReserva, float tiempoRecarga) {
            this.nombre = nombre;
            this.precio = precio;
            this.tipo = "arma";
            this.daño = daño;
            this.municionMaxima = municionMaxima;
            this.municionReserva = municionReserva;
            this.tiempoRecarga = tiempoRecarga;
            this.descripcion = "Daño: " + daño + " | Munición: " + municionMaxima + "/" + municionReserva + " | Recarga: " + tiempoRecarga + "s";
        }
    }
    
    private ArrayList<ItemTienda> itemsDisponibles = new ArrayList<>();
    private int opcionTiendaSeleccionada = 0;
    private ControladorEnemigos controladorEnemigos;
    private GestorPalmeras gestorPalmeras;
    
    // ===== VARIABLES DE RED =====
    private NetworkManager networkManager;
    private JugadorRemoto jugadorRemoto;
    private boolean modoOnline;
    private float tiempoSincronizacion = 0f;
    private final float INTERVALO_SINCRONIZACION = 1f / 30f;
    private int contadorIdBalas = 0;

    public PantallaJuego(Game game, SpriteBatch batch, ControladorDeAudio controladorDeAudio, ControladorEntrada controladorDeEntradas, ConfiguracionJuego config) {
        this.game = game;
        this.principal = (Principal) game;
        this.batch = batch;
        this.controladorDeAudio = controladorDeAudio;
        this.controladorDeEntradas = controladorDeEntradas;
        this.config = config;
        this.modoOnline = config.getModoOnline();
    }

    @Override
    public void show() {
        this.controladorDeAudio.detenerMusicaMenu();
        this.controladorDeAudio.cargarSonidos("salto", "Efectos de sonido/Sonido de salto.mp3");    
        this.controladorDeAudio.cargarSonidos("disparo", "Efectos de sonido/disparar.mp3");
        this.controladorDeAudio.cargarSonidos("recarga", "Efectos de sonido/recargar.mp3");
        this.controladorDeAudio.cargarSonidos("powerup", "Efectos de sonido/powerup.mp3");
        this.controladorDeAudio.cargarSonidos("menu_move", "Efectos de sonido/menu_move.mp3");
        this.controladorDeAudio.cargarSonidos("nazi-shoot", "Efectos de sonido/nazi-shoot.mp3");
        this.controladorDeAudio.cargarSonidos("muerte", "Efectos de sonido/muerte.mp3");
        this.controladorDeAudio.cargarSonidos("instakill", "Efectos de sonido/INSTAKILL.mp3");
        this.controladorDeAudio.cargarSonidos("nuclear", "Efectos de sonido/nuclear.mp3");
        this.controladorDeAudio.cargarSonidos("nazi-dialogo1", "Efectos de sonido/nazi-dialogo1.mp3");
        this.controladorDeAudio.cargarSonidos("nazi-dialogo2", "Efectos de sonido/nazi-dialogo2.mp3");
        this.controladorDeAudio.cargarSonidos("nazi-dialogo3", "Efectos de sonido/nazi-dialogo3.mp3");
        this.controladorDeAudio.inicializarDialogosNazis();
        this.controladorDeAudio.cargarSonidos("canibal-te-ve", "Efectos de sonido/canibal-te-ve.mp3");
        this.controladorDeAudio.cargarRespiracionCanibal();

        int nivelActual = this.config.getNivel();
        
        switch(nivelActual) {
            case 1:
                this.controladorDeAudio.iniciarMusicaNivel1();
                tiledMap = new TmxMapLoader().load("Mapas_Niveles/Nivel 1/Nivel 1.tmx");
                break;
            case 2:
                tiledMap = new TmxMapLoader().load("Mapas_Niveles/Nivel 2/nivel 2.tmx");
                break;
            case 3:
                tiledMap = new TmxMapLoader().load("Mapas_Niveles/Nivel 3/Nivel 3.tmx");
                break;
            case 4:
                tiledMap = new TmxMapLoader().load("Mapas_Niveles/Nivel 4/Nivel 4.tmx");
                break;
            case 5:
                tiledMap = new TmxMapLoader().load("Mapas_Niveles/Nivel 5/Nivel 5.tmx");
                break;            
            case 6:
                this.config.setNivel(1);
                this.config.limpiarEstadosJugadores(); // ⭐ LIMPIAR ESTADOS AL TERMINAR EL JUEGO
                this.controladorDeAudio.detenerTodaMusica();
                this.controladorDeAudio.iniciarMusicaMenu();
                this.game.setScreen(new PantallaMenuPrincipal(game, new BitmapFont(), controladorDeAudio, batch, controladorDeEntradas, config));
                return;
        }

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        capaColisiones = (TiledMapTileLayer) tiledMap.getLayers().get("colisiones");
        
        if (tiledMap.getLayers().get("arbustos") != null) {
            capaArbustos = (TiledMapTileLayer) tiledMap.getLayers().get("arbustos");
        }

        if (tiledMap.getLayers().get("tienda_object") != null) {
            MapObjects objetosTienda = tiledMap.getLayers().get("tienda_object").getObjects();
            for (MapObject obj : objetosTienda) {
                if (obj.getName() != null && obj.getName().equalsIgnoreCase("tienda")) {
                    try {
                        tiendaX = (float) obj.getProperties().get("x");
                        tiendaY = (float) obj.getProperties().get("y");
                    } catch (Exception e) {
                        System.err.println("❌ Error al obtener coordenadas de la tienda: " + e.getMessage());
                    }
                    break;
                }
            }
        }
        
        fontTienda = new BitmapFont();

        MapObject finNivelObj = tiledMap.getLayers().get("fin del nivel").getObjects().get(0);
        finNivelX = (float) finNivelObj.getProperties().get("x");
        finNivelY = (float) finNivelObj.getProperties().get("y");
        finNivelWidth = (float) finNivelObj.getProperties().get("width");
        finNivelHeight = (float) finNivelObj.getProperties().get("height");

        shapeRenderer = new ShapeRenderer();

        String nombreSpawn = config.esHost() ? "spawn" : "spawn2";
        MapObject spawnObj = tiledMap.getLayers().get("spawn jugador").getObjects().get(nombreSpawn);

        if (spawnObj == null) {
            spawnObj = tiledMap.getLayers().get("spawn jugador").getObjects().get("spawn");
        }

        float spawnX = (float) spawnObj.getProperties().get("x");
        float spawnY = (float) spawnObj.getProperties().get("y");
        jugador = new Jugador(spawnX, spawnY + 250, this.config);

        camara = new OrthographicCamera(800, 600);
        viewport = new FitViewport(600, 350, camara);
      
        jugador.setControladorAudio(controladorDeAudio);
        
        controladorEnemigos = new ControladorEnemigos();
        controladorEnemigos.setControladorAudio(controladorDeAudio);
        controladorEnemigos.setConfiguracion(this.config);
        
        cargarEnemigosDelMapa();
        
        gestorPalmeras = new GestorPalmeras();
        

            if (tiledMap.getLayers().get("ramas de agarre") != null) {
                MapObjects objetosRamas = tiledMap.getLayers().get("ramas de agarre").getObjects();
                gestorPalmeras.cargarDesdeMapaTiled(objetosRamas);
            }

     
        hud = new HUD(this.batch, this.config);
        camara.update();
        
        if (modoOnline) {
            pantallaSpectate = new PantallaSpectate(batch);
        }

        fontPausa = new BitmapFont();
        inicializarTienda();

        if (modoOnline) {
            inicializarRed();
            
            // 🔥 DEBUG: Verificar que jugadorRemoto se creó
            if (jugadorRemoto != null) {
                System.out.println("✅ JugadorRemoto creado en: (" + jugadorRemoto.getX() + ", " + jugadorRemoto.getY() + ")");
                System.out.println("📊 Posición cámara: (" + camara.position.x + ", " + camara.position.y + ")");
            } else {
                System.out.println("❌ ERROR: JugadorRemoto es NULL!");
            }
        }

        // ⭐⭐⭐ CARGAR ESTADO GUARDADO (si existe) ⭐⭐⭐
        cargarEstadoJugadores();

        Gdx.input.setInputProcessor(controladorDeEntradas);
    }
    
    // ⭐⭐⭐ NUEVOS MÉTODOS PARA PERSISTENCIA DE ESTADO ⭐⭐⭐
    
    /**
     * Guarda el estado de ambos jugadores antes de cambiar de nivel
     */
    private void guardarEstadoJugadores() {
        System.out.println("💾 Guardando estado de los jugadores...");
        
        // Guardar estado del jugador local
        EstadoJugador estadoLocal = jugador.guardarEstado();
        config.getEstadoJugadorLocal().setPuntos(estadoLocal.getPuntos());
        config.getEstadoJugadorLocal().setArmas(estadoLocal.getArmas());
        config.getEstadoJugadorLocal().setSlotArmaActual(estadoLocal.getSlotArmaActual());
        
        System.out.println("  ✅ Jugador local guardado: " + estadoLocal.getPuntos() + " puntos");
        
        // Si es HOST y hay jugador remoto, guardar su estado también
        if (modoOnline && config.esHost() && jugadorRemoto instanceof JugadorRemotoControlable) {
            JugadorRemotoControlable jugadorControlable = (JugadorRemotoControlable) jugadorRemoto;
            EstadoJugador estadoRemoto = jugadorControlable.guardarEstado();
            config.getEstadoJugadorRemoto().setPuntos(estadoRemoto.getPuntos());
            config.getEstadoJugadorRemoto().setArmas(estadoRemoto.getArmas());
            config.getEstadoJugadorRemoto().setSlotArmaActual(estadoRemoto.getSlotArmaActual());
            
            System.out.println("  ✅ Jugador remoto guardado: " + estadoRemoto.getPuntos() + " puntos");
        }
        
        System.out.println("✅ Estados guardados exitosamente");
    }
    
    /**
     * Carga el estado guardado en los jugadores
     */
    private void cargarEstadoJugadores() {
        System.out.println("📂 Cargando estado de los jugadores...");
        
        // Cargar estado del jugador local
        EstadoJugador estadoLocal = config.getEstadoJugadorLocal();
        if (estadoLocal.tieneArmas()) {
            jugador.cargarEstado(estadoLocal);
            System.out.println("  ✅ Jugador local cargado: " + estadoLocal.getPuntos() + " puntos");
        }
        
        // Si es HOST y hay jugador remoto, cargar su estado también
        if (modoOnline && config.esHost() && jugadorRemoto instanceof JugadorRemotoControlable) {
            JugadorRemotoControlable jugadorControlable = (JugadorRemotoControlable) jugadorRemoto;
            EstadoJugador estadoRemoto = config.getEstadoJugadorRemoto();
            if (estadoRemoto.tieneArmas()) {
                jugadorControlable.cargarEstado(estadoRemoto);
                System.out.println("  ✅ Jugador remoto cargado: " + estadoRemoto.getPuntos() + " puntos");
            }
        }
        
        System.out.println("✅ Estados cargados exitosamente");
    }
    
    private void aplicarCompraRemota(ShopPurchasePacket compra) {
        int idComprador = compra.getIdJugador();
        
        if (!config.esHost()) {
            return;
        }
        
        if (idComprador == 0) {
            return;
        }
        
        if (jugadorRemoto instanceof JugadorRemotoControlable) {
            JugadorRemotoControlable jugadorControlable = (JugadorRemotoControlable) jugadorRemoto;
            
            if (jugadorControlable.getPuntos() < compra.getPrecio()) {
                System.out.println("❌ Cliente no tiene suficientes puntos para comprar");
                return;
            }
            
            jugadorControlable.restarPuntos(compra.getPrecio());
            
            if (compra.getTipoCompra().equals("medicina")) {
                jugadorControlable.recuperarSalud(jugadorControlable.getSaludMaxima());
                controladorDeAudio.reproducirSonido("powerup");
                System.out.println("💊 Cliente compró medicina - Salud restaurada");
                
            } else if (compra.getTipoCompra().equals("arma")) {
                jugadorControlable.agregarArma(
                    compra.getSlot(),
                    compra.getNombreItem(),
                    compra.getDaño(),
                    compra.getMunicionMaxima(),
                    compra.getMunicionReserva(),
                    compra.getTiempoRecarga()
                );
                controladorDeAudio.reproducirSonido("powerup");
                System.out.println("🔫 Cliente compró: " + compra.getNombreItem() + " (Slot " + compra.getSlot() + ")");
            }
            
            System.out.println("✅ Compra del cliente aplicada - Puntos restantes: " + jugadorControlable.getPuntos());
        }
    }
    
    private void inicializarRed() {
        networkManager = new NetworkManager(config, new NetworkManager.NetworkCallback() {
            @Override
            public void onClienteConectado(String nombreCliente) {
                System.out.println("✅ Cliente conectado en juego: " + nombreCliente);
            }
            
            @Override
            public void onClienteDesconectado(String razon) {
                System.out.println("⚠️ Cliente desconectado: " + razon);
                modoOnline = false;
            }
            
            @Override
            public void onConexionExitosa() {
                System.out.println("✅ Conexión exitosa en juego");
            }
            
            @Override
            public void onConexionFallida(String razon) {
                System.err.println("❌ Conexión fallida: " + razon);
                modoOnline = false;
            }
            
            @Override
            public void onCambioNivel(LevelChangePacket cambioNivel) {
                if (!nivelYaProcesado) {
                    config.setNivel(cambioNivel.getSiguienteNivel());
                    cambiandoNivel = true;
                    nivelYaProcesado = true;
                }
            }
            
            @Override
            public void onMuerteJugador(PlayerDeathPacket muerte) {
                manejarMuerteJugador(muerte);
            }
            
            @Override
            public void onCompraEnTienda(ShopPurchasePacket compra) {
                System.out.println("🛒 Callback de compra recibido: " + compra);
                aplicarCompraRemota(compra);
            }
        });
        
        networkManager.iniciar();
        
        if (principal != null) {
            principal.registrarNetworkManager(networkManager);
            System.out.println("✅ NetworkManager registrado en Principal desde PantallaJuego");
        }
        
        if (config.esHost()) {
            MapObject spawn2Obj = tiledMap.getLayers().get("spawn jugador").getObjects().get("spawn2");
            
            if (spawn2Obj != null) {
                float spawn2X = (float) spawn2Obj.getProperties().get("x");
                float spawn2Y = (float) spawn2Obj.getProperties().get("y");
                jugadorRemoto = new JugadorRemotoControlable(spawn2X, spawn2Y + 250, config);
            } else {
                MapObject spawnObj = tiledMap.getLayers().get("spawn jugador").getObjects().get("spawn");
                float spawnX = (float) spawnObj.getProperties().get("x");
                float spawnY = (float) spawnObj.getProperties().get("y");
                jugadorRemoto = new JugadorRemotoControlable(spawnX + 100, spawnY + 250, config);
            }
        } else {
            jugadorRemoto = new JugadorRemoto();
        }
    }
    
    private void manejarMuerteJugador(PlayerDeathPacket muerte) {
        int idMuerto = muerte.getIdJugadorMuerto();
        
        if (config.esHost()) {
            if (idMuerto == 1) {
                jugadorRemotoMuerto = true;
            }
        } else {
            if (idMuerto == 0) {
                jugadorRemotoMuerto = true;
            } else if (idMuerto == 1) {
                jugadorLocalMuerto = true;
                modoSpectate = true;
            }
        }
        
        if (muerte.isAmbosJugadoresMuertos()) {
            volverAlMenuConectado();
        }
    }
    
    private void volverAlMenuConectado() {
        if (controladorDeAudio != null) {
            controladorDeAudio.detenerTodaMusica();
            controladorDeAudio.iniciarMusicaMenu();
        }
        
        if (principal != null && networkManager != null) {
            principal.desregistrarNetworkManager();
        }
        
        if (controladorEnemigos != null) {
            controladorEnemigos.limpiar();
        }
        
        config.setNivel(1);
        config.limpiarEstadosJugadores(); // ⭐ LIMPIAR ESTADOS AL VOLVER AL MENÚ
        config.setModoOnline(false);
        config.setModoCooperativo(false);
        
        game.setScreen(new PantallaMenuPrincipal(game, new BitmapFont(), controladorDeAudio, batch, controladorDeEntradas, config));
    }
    
    private void inicializarTienda() {
        itemsDisponibles.clear();
        
        if (!jugador.tieneArmaEnSlot(2)) {
            itemsDisponibles.add(new ItemTienda("Deagle/Revolver", 240, 90, 6, 36, 2f));
        }
        if (!jugador.tieneArmaEnSlot(3)) {
            itemsDisponibles.add(new ItemTienda("Uzi", 260, 15, 30, 120, 1.5f));
        }
        if (!jugador.tieneArmaEnSlot(4)) {
            itemsDisponibles.add(new ItemTienda("AK-47", 300, 45, 30, 120, 2f));
        }
        
        itemsDisponibles.add(new ItemTienda("Medicina", "Restaura toda tu salud", 150, "medicina"));
        itemsDisponibles.add(new ItemTienda("Salir", "", 0, "salir"));
        
        opcionTiendaSeleccionada = 0;
    }
    
    private void cargarEnemigosDelMapa() {
        try {
            String[] capasSpawn = { "spawn soldados", "spawn canibales" };
            boolean seCargoAlMenosUno = false;

            for (String nombreCapa : capasSpawn) {
                if (tiledMap.getLayers().get(nombreCapa) != null) {
                    MapObjects objetos = tiledMap.getLayers().get(nombreCapa).getObjects();
                    procesarObjetosEnemigos(objetos, nombreCapa);
                    seCargoAlMenosUno = true;
                }
            }

            if (!seCargoAlMenosUno) {
                controladorEnemigos.agregarCanibal(800, 300);
                controladorEnemigos.agregarSoldadoNazi(1200, 300);
            }

        } catch (Exception e) {
            controladorEnemigos.agregarCanibal(800, 300);
            controladorEnemigos.agregarSoldadoNazi(1200, 300);
        }
    }

    private void procesarObjetosEnemigos(MapObjects objetos, String nombreCapa) {
        if (objetos == null || objetos.getCount() == 0) return;

        for (MapObject objeto : objetos) {
            if (objeto instanceof RectangleMapObject) {
                try {
                    float x = (float) objeto.getProperties().get("x");
                    float y = (float) objeto.getProperties().get("y");

                    if (nombreCapa.equalsIgnoreCase("spawn canibales")) {
                        controladorEnemigos.agregarCanibal(x, y + 350);
                    } else if (nombreCapa.equalsIgnoreCase("spawn soldados")) {
                        controladorEnemigos.agregarSoldadoNazi(x, y + 350);
                    }
                } catch (Exception e) {
                    System.err.println("Error al procesar enemigo: " + e.getMessage());
                }
            }
        }
    }
    
    @Override
    public void render(float delta) {
        if (cambiandoNivel) {
            controladorEnemigos.limpiar();
            if (gestorPalmeras != null) gestorPalmeras.dispose();
            this.game.setScreen(new PantallaJuego(this.game, batch, controladorDeAudio, controladorDeEntradas, config));
            return;
        }
        
        if (modoOnline && config.esHost() && networkManager != null) {
            ShopPurchasePacket compraPendiente = networkManager.obtenerCompraPendiente();
            if (compraPendiente != null) {
                aplicarCompraRemota(compraPendiente);
            }
        }
        
        if (!jugadorLocalMuerto && jugador.estaMuerto()) {
            jugadorLocalMuerto = true;
            
            if (modoOnline) {
                boolean otroJugadorMuerto = jugadorRemotoMuerto;
                
                if (config.esHost()) {
                    if (otroJugadorMuerto) {
                        networkManager.enviarMuerteJugador(0, true);
                        volverAlMenuConectado();
                        return;
                    } else {
                        modoSpectate = true;
                        networkManager.enviarMuerteJugador(0, false);
                    }
                } else {
                    modoSpectate = true;
                }
            } else {
                this.game.setScreen(new PantallaGameOver(this.game, this.batch, this.controladorDeAudio, this.controladorDeEntradas, this.config));
                return;
            }
        }
        
        if (modoOnline && !jugadorRemotoMuerto && jugadorRemoto != null) {
            if (config.esHost() && jugadorRemoto instanceof JugadorRemotoControlable) {
                JugadorRemotoControlable jugadorControlable = (JugadorRemotoControlable) jugadorRemoto;
                
                if (jugadorControlable.getVidas() <= 0 && !jugadorRemotoMuerto) {
                    jugadorRemotoMuerto = true;
                    
                    if (jugadorLocalMuerto) {
                        networkManager.enviarMuerteJugador(1, true);
                        volverAlMenuConectado();
                        return;
                    } else {
                        networkManager.enviarMuerteJugador(1, false);
                    }
                }
            }
        }
        
        if (modoSpectate) {
            renderModoSpectate(delta);
            return;
        }
        
        tiempoUltimoInput += delta;
        if (controladorDeEntradas.isEscape() && tiempoUltimoInput >= DELAY_INPUT) {
            juegoPausado = !juegoPausado;
            tiempoUltimoInput = 0;
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 0, 0, 0.5f);
        
        if (juegoPausado) {
            renderJuegoCongelado();
            renderMenuPausa(delta);
            return;
        }

        if (!tiendaActiva) {
            if (controladorDeEntradas.is1()) {
                jugador.cambiarArma(1);
            } else if (controladorDeEntradas.is2() && jugador.tieneArmaEnSlot(2)) {
                jugador.cambiarArma(2);
            } else if (controladorDeEntradas.is3() && jugador.tieneArmaEnSlot(3)) {
                jugador.cambiarArma(3);
            } else if (controladorDeEntradas.is4() && jugador.tieneArmaEnSlot(4)) {
                jugador.cambiarArma(4);
            }
        }

        boolean salto = this.controladorDeEntradas.isSpace(); 
        boolean agacharse = this.controladorDeEntradas.isAbajo();
        boolean disparo = this.controladorDeEntradas.isDisparar();
        boolean recargar = this.controladorDeEntradas.isRecargar();
        boolean atacarCuchillo = this.controladorDeEntradas.isV();
        boolean izquierda = this.controladorDeEntradas.isIzquierda();
        boolean derecha = this.controladorDeEntradas.isDerecha();
        boolean agarrarse = this.controladorDeEntradas.isShift();
        
        // Mostrar indicador de tienda
        if (!juegoPausado && !tiendaActiva) {
            float distanciaTienda = Math.abs(jugador.getX() - tiendaX);
            if (distanciaTienda < 150) {
                batch.setProjectionMatrix(camara.combined);
                batch.begin();
                fontTienda.setColor(Color.WHITE);
                fontTienda.getData().setScale(1.5f);
                fontTienda.draw(batch, "Presiona E para abrir tienda", tiendaX - 80, tiendaY + 200);
                batch.end();

                if (this.controladorDeEntradas.isE()) {
                    tiendaActiva = true;
                    inicializarTienda();
                }
            }
        }

        if (disparo) {
            float tiempoActual = com.badlogic.gdx.utils.TimeUtils.nanoTime() / 1_000_000_000f;
            if (jugador.puedeDisparar(tiempoActual)) {
                jugador.disparar(tiempoActual);

                Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                camara.unproject(mouse);
                
                Bala bala = new Bala(
                    jugador.getX() + jugador.getAncho() / 2f, 
                    jugador.getY() + jugador.getAlto() / 2f, 
                    mouse.x, 
                    mouse.y,
                    config.getIdJugador()
                );
                balas.add(bala);
                
                System.out.println("🔫 Jugador " + config.getIdJugador() + " disparó (esHost: " + config.esHost() + ")");
            }
        }
        
        PuntoAgarre palmeraCercana = null;
        int nivelActual = this.config.getNivel();
        if ((nivelActual == 1 || nivelActual == 2) && gestorPalmeras != null) {
            palmeraCercana = gestorPalmeras.buscarPalmeraCercana(jugador.getX(), jugador.getY(), jugador.getAncho(), jugador.getAlto());
        }
       
        float targetX = jugador.getX() + jugador.getAncho() / 2f;
        float targetY = jugador.getY() + jugador.getAlto() / 2f;
        camara.position.set(targetX, targetY, 0);

        if (tiempoScreenShake > 0) {
            tiempoScreenShake -= delta;
            float offsetX = (float)(Math.random() * intensidadShake * 2 - intensidadShake);
            float offsetY = (float)(Math.random() * intensidadShake * 2 - intensidadShake);
            camara.position.x += offsetX;
            camara.position.y += offsetY;
        }

        float mapWidth = capaColisiones.getWidth() * capaColisiones.getTileWidth();
        float mapHeight = capaColisiones.getHeight() * capaColisiones.getTileHeight();
        float halfViewportWidth = camara.viewportWidth * camara.zoom / 2f;
        float halfViewportHeight = camara.viewportHeight * camara.zoom / 2f;
        camara.position.x = Math.max(halfViewportWidth, Math.min(camara.position.x, mapWidth - halfViewportWidth));
        camara.position.y = Math.max(halfViewportHeight, Math.min(camara.position.y, mapHeight - halfViewportHeight));
        camara.update();

        tiledMapRenderer.setView(camara);
        tiledMapRenderer.render();

        batch.setProjectionMatrix(camara.combined);
        batch.begin();
        controladorEnemigos.renderTexturas(batch);
        
        if (jugador.estaOcultoEnArbusto()) {
            batch.setColor(1, 1, 1, 0.3f);
        }
        jugador.render(batch);
        batch.setColor(1, 1, 1, 1);
        
        if (modoOnline && jugadorRemoto != null) {
//            jugadorRemoto.actualizar(delta);
            
            if (jugadorRemoto.estaOcultoEnArbusto()) {
                batch.setColor(1, 1, 1, 0.3f);
            }
            
            jugadorRemoto.render(batch);
            batch.setColor(1, 1, 1, 1);
        }
        
//     // 🔍 DEBUG: Verificar estado del jugador remoto
//        if (modoOnline && jugadorRemoto != null && config.esHost()) {
//            JugadorRemotoControlable jrc = (JugadorRemotoControlable) jugadorRemoto;
//            
//            // Imprimir cada 60 frames (aproximadamente cada segundo)
//            if (Gdx.graphics.getFrameId() % 60 == 0) {
//                System.out.println("═══════════════════════════════════════");
//                System.out.println("🔍 DEBUG JUGADOR REMOTO (Cliente):");
//                System.out.println("  Posición: (" + jrc.getX() + ", " + jrc.getY() + ")");
//                System.out.println("  targetX/Y: (" + jrc.targetX + ", " + jrc.targetY + ")");
//                System.out.println("  Agachado: " + jrc.estaAgachado());
//                System.out.println("  En suelo: " + jrc.estaEnSuelo());
//                System.out.println("  Palmera: " + jrc.estaAgarradoPalmera());
//                System.out.println("  Mirando derecha: " + jrc.getMirandoDerecha());
//                
//                // Acceder al estado interno (necesitarás hacer estos campos protected o crear getters)
//                System.out.println("  seEstaMoviendo: " + jrc.seEstaMoviendo);
//                System.out.println("  frameIndex: " + jrc.frameIndex);
//                System.out.println("  primerPacketRecibido: " + jrc.primerPacketRecibido);
//                System.out.println("═══════════════════════════════════════");
//            }
//        }
//        
        batch.end();
        

        shapeRenderer.setProjectionMatrix(camara.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Bala bala : balas) {
            bala.render(shapeRenderer);
        }
        shapeRenderer.end();
        
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camara.unproject(mouse);
        jugador.apuntarHacia(mouse.x, mouse.y);
        
        verificarOcultacionEnArbustos();
        
        float tiempoTotal = com.badlogic.gdx.utils.TimeUtils.nanoTime() / 1_000_000_000f;
        
        if(config.getModoOnline()) {
            controladorEnemigos.actualizar(delta, jugador, jugadorRemoto, capaColisiones, tiempoTotal);
        } else {
            controladorEnemigos.actualizar(delta, jugador, capaColisiones, tiempoTotal);
        }

        controladorEnemigos.render(shapeRenderer);
        
        // Indicador de palmera cercana
        if (palmeraCercana != null && !jugador.estaAgarradoPalmera()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            float tiempoAnimacion = tiempoTotal;
            float pulsacion = (float)(Math.sin(tiempoAnimacion * 6) * 0.3f + 1.0f);
            float rebote = (float)(Math.sin(tiempoAnimacion * 4) * 10f);
            float indicadorX = palmeraCercana.getX() + palmeraCercana.getAncho() / 2f;
            float indicadorY = palmeraCercana.getY() + palmeraCercana.getAlto() + 40f + rebote;
            float radio = 15f * pulsacion;
            shapeRenderer.setColor(0, 1, 0, 0.3f);
            shapeRenderer.circle(indicadorX, indicadorY, radio + 10f);
            shapeRenderer.setColor(0, 1, 0, 1);
            shapeRenderer.circle(indicadorX, indicadorY, radio);
            shapeRenderer.end();
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            Gdx.gl.glLineWidth(3);
            float flechaX = indicadorX;
            float flechaY = indicadorY;
            shapeRenderer.line(flechaX, flechaY, flechaX, flechaY + 20f);
            shapeRenderer.line(flechaX, flechaY + 20f, flechaX - 5f, flechaY + 15f);
            shapeRenderer.line(flechaX, flechaY + 20f, flechaX + 5f, flechaY + 15f);
            Gdx.gl.glLineWidth(1);
            shapeRenderer.end();
        }
        
        // Actualizar balas
        for (int i = 0; i < balas.size(); i++) {
            Bala bala = balas.get(i);
            bala.actualizar(Gdx.graphics.getDeltaTime());
            if (!bala.estaActiva()) {
                balas.remove(i);
                i--;
            }
        }
        
        ControladorEnemigos.ResultadoColisionBalas resultado = controladorEnemigos.verificarColisionesBalas(balas, jugador, jugadorRemoto);
        
        if (resultado.sanidadRecuperada > 0) {
            int saludRecuperada = (int)((resultado.sanidadRecuperada / 100f) * 300f);
            jugador.recuperarSalud(saludRecuperada);
        }
        
        if (resultado.saludMentalRecuperada > 0) {
            jugador.recuperarSaludMental(resultado.saludMentalRecuperada);
        }
        
        controladorEnemigos.verificarColisionesCuchillo(jugador, jugadorRemoto);
        controladorEnemigos.verificarColisionesJugador(jugador, jugadorRemoto);
        
        hud.actualizarVidas(jugador.getVidas());
        hud.actualizarSalud(jugador.getSaludActual(), jugador.getSaludMaxima());
        hud.actualizarSaludMental(jugador.getSaludMental());
        hud.actualizarMunicion(jugador.getMunicionActual(), jugador.getMunicionMaxima(), jugador.getNombreArmaActual(), jugador.estaRecargando(), jugador.getProgresoRecarga());
        hud.actualizarPowerUpRecarga(jugador.tienePowerUpRecargaRapida(), jugador.getTiempoRecargaRapidaRestante());
        hud.actualizarPowerUpInstaKill(jugador.tieneInstaKill(), jugador.getTiempoInstaKillRestante());
        hud.actualizarEstadoPalmera(jugador.estaAgarradoPalmera());
        hud.actualizarPuntos(jugador);
        hud.render();
        
        batch.setProjectionMatrix(camara.combined);
        batch.begin();
        controladorEnemigos.renderPowerUps(batch);
        batch.end();

        // Recolectar power-ups
        ArrayList<PowerUp> powerUps = controladorEnemigos.getPowerUpsDropeados();
        for (PowerUp powerUp : powerUps) {
            if (!powerUp.estaRecolectado() && powerUp.verificarColision(jugador.getX(), jugador.getY(), jugador.getAncho(), jugador.getAlto())) {
                powerUp.recolectar();
                switch(powerUp.getTipo()) {
                    case RECARGA_RAPIDA:
                        jugador.activarPowerUpRecargaRapida();
                        controladorDeAudio.reproducirSonido("powerup");
                        break;
                    case INSTAKILL:
                        jugador.activarPowerUpInstaKill();
                        controladorDeAudio.reproducirSonido("instakill");
                        break;
                    case NUCLEAR:
                        controladorEnemigos.activarNuclear();
                        controladorDeAudio.reproducirSonido("nuclear");
                        tiempoScreenShake = 7f;
                        break;
                }
            }
        }
        
        if (tiendaActiva) {
            renderTienda();
            return;
        }
        
        // ⭐⭐⭐ VERIFICAR FIN DE NIVEL CON GUARDADO DE ESTADO ⭐⭐⭐
        boolean jugador1EnFinNivel = jugador.getX() < finNivelX + finNivelWidth &&
            jugador.getX() + jugador.getAncho() > finNivelX &&
            jugador.getY() < finNivelY + finNivelHeight &&
            jugador.getY() + jugador.getAlto() > finNivelY;

        boolean jugador2EnFinNivel = false;
        if (modoOnline && jugadorRemoto != null) {
            jugador2EnFinNivel = jugadorRemoto.getX() < finNivelX + finNivelWidth &&
                jugadorRemoto.getX() + jugadorRemoto.getAncho() > finNivelX &&
                jugadorRemoto.getY() < finNivelY + finNivelHeight &&
                jugadorRemoto.getY() + jugadorRemoto.getAlto() > finNivelY;
        }

        if (modoOnline) {
            if (config.esHost()) {
                if ((jugador1EnFinNivel || jugador2EnFinNivel) && !cambiandoNivel && !nivelYaProcesado) {
                    // ⭐⭐⭐ GUARDAR ESTADO ANTES DE CAMBIAR NIVEL ⭐⭐⭐
                    guardarEstadoJugadores();
                    
                    int jugadorCompleto = jugador1EnFinNivel ? 0 : 1;
                    int nivel = config.getNivel();
                    int siguienteNivel = nivelActual + 1;
                    networkManager.enviarCambioNivel(nivel, siguienteNivel, jugadorCompleto);
                    config.setNivel(siguienteNivel);
                    cambiandoNivel = true;
                    nivelYaProcesado = true;
                }
            }
        } else {
            if (jugador1EnFinNivel) {
                // ⭐⭐⭐ GUARDAR ESTADO ANTES DE CAMBIAR NIVEL ⭐⭐⭐
                guardarEstadoJugadores();
                avanzarDeNivel();
            }
        }
        
        float movimiento = 0;
        if (!agacharse) {
            if (izquierda) movimiento = -1;
            else if (derecha) movimiento = 1;
        }
        
        jugador.actualizarMovimientoJugador(movimiento, salto, agacharse, disparo, recargar, atacarCuchillo, agarrarse, palmeraCercana, Gdx.graphics.getDeltaTime(), capaColisiones, tiempoTotal);
        
        if (modoOnline && networkManager != null) {
            actualizarRed(delta);
        }
    }
    
    private void renderModoSpectate(float delta) {
        if (modoOnline && jugadorRemoto != null && !jugadorRemotoMuerto) {
            float targetX = jugadorRemoto.getX() + jugadorRemoto.getAncho() / 2f;
            float targetY = jugadorRemoto.getY() + jugadorRemoto.getAlto() / 2f;
            camara.position.set(targetX, targetY, 0);
            
            float mapWidth = capaColisiones.getWidth() * capaColisiones.getTileWidth();
            float mapHeight = capaColisiones.getHeight() * capaColisiones.getTileHeight();
            float halfViewportWidth = camara.viewportWidth * camara.zoom / 2f;
            float halfViewportHeight = camara.viewportHeight * camara.zoom / 2f;
            camara.position.x = Math.max(halfViewportWidth, Math.min(camara.position.x, mapWidth - halfViewportWidth));
            camara.position.y = Math.max(halfViewportHeight, Math.min(camara.position.y, mapHeight - halfViewportHeight));
            camara.update();
            
            tiledMapRenderer.setView(camara);
            tiledMapRenderer.render();
            
            batch.setProjectionMatrix(camara.combined);
            batch.begin();
            controladorEnemigos.renderTexturas(batch);
            if (jugadorRemoto != null) {
                jugadorRemoto.actualizar(delta);
                jugadorRemoto.render(batch);
            }
            batch.end();
            
            shapeRenderer.setProjectionMatrix(camara.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (Bala bala : balas) {
                bala.render(shapeRenderer);
            }
            shapeRenderer.end();
            
            float tiempoTotal = com.badlogic.gdx.utils.TimeUtils.nanoTime() / 1_000_000_000f;
            controladorEnemigos.actualizar(delta, jugador, jugadorRemoto, capaColisiones, tiempoTotal);
            controladorEnemigos.render(shapeRenderer);
            
            for (int i = 0; i < balas.size(); i++) {
                Bala bala = balas.get(i);
                bala.actualizar(delta);
                if (!bala.estaActiva()) {
                    balas.remove(i);
                    i--;
                }
            }
            
            if (networkManager != null) {
                actualizarRed(delta);
            }
            
            hud.render();
            
            if (pantallaSpectate != null) {
                pantallaSpectate.render(camara);
            }
        }
    }
    
    private void verificarOcultacionEnArbustos() {
        if (capaArbustos == null) {
            jugador.setOcultoEnArbusto(false);
            if (modoOnline && jugadorRemoto != null && jugadorRemoto instanceof JugadorRemotoControlable) {
                ((JugadorRemotoControlable) jugadorRemoto).setOcultoEnArbusto(false);
            }
            return;
        }
        
        float tileWidth = capaArbustos.getTileWidth();
        float tileHeight = capaArbustos.getTileHeight();
        
        int xTileInicio = (int)(jugador.getX() / tileWidth);
        int xTileFin = (int)((jugador.getX() + jugador.getAncho()) / tileWidth);
        int yTileInicio = (int)(jugador.getY() / tileHeight);
        int yTileFin = (int)((jugador.getY() + jugador.getAlto()) / tileHeight);
        
        boolean jugadorLocalEnArbusto = false;
        for (int x = xTileInicio; x <= xTileFin; x++) {
            for (int y = yTileInicio; y <= yTileFin; y++) {
                if (capaArbustos.getCell(x, y) != null) {
                    jugadorLocalEnArbusto = true;
                    break;
                }
            }
            if (jugadorLocalEnArbusto) break;
        }
        
        jugador.setOcultoEnArbusto(jugadorLocalEnArbusto);
        
        if (modoOnline && jugadorRemoto != null && jugadorRemoto instanceof JugadorRemotoControlable) {
            JugadorRemotoControlable jugadorControlable = (JugadorRemotoControlable) jugadorRemoto;
            
            int xTileInicio2 = (int)(jugadorControlable.getX() / tileWidth);
            int xTileFin2 = (int)((jugadorControlable.getX() + jugadorControlable.getAncho()) / tileWidth);
            int yTileInicio2 = (int)(jugadorControlable.getY() / tileHeight);
            int yTileFin2 = (int)((jugadorControlable.getY() + jugadorControlable.getAlto()) / tileHeight);
            
            boolean jugadorRemotoEnArbusto = false;
            for (int x = xTileInicio2; x <= xTileFin2; x++) {
                for (int y = yTileInicio2; y <= yTileFin2; y++) {
                    if (capaArbustos.getCell(x, y) != null) {
                        jugadorRemotoEnArbusto = true;
                        break;
                    }
                }
                if (jugadorRemotoEnArbusto) break;
            }
            
            jugadorControlable.setOcultoEnArbusto(jugadorRemotoEnArbusto);
        }
    }
    
    private void actualizarRed(float delta) {
        tiempoSincronizacion += delta;
        
        if (config.esHost()) {
            PlayerInputPacket inputCliente = networkManager.obtenerInputCliente();
            
            if (inputCliente != null && jugadorRemoto != null && jugadorRemoto instanceof JugadorRemotoControlable) {
                JugadorRemotoControlable jugadorControlable = (JugadorRemotoControlable) jugadorRemoto;
                
                float movimientoCliente = 0;
                if (!inputCliente.isAgacharse()) {
                    if (inputCliente.isIzquierda()) movimientoCliente = -1;
                    else if (inputCliente.isDerecha()) movimientoCliente = 1;
                    else movimientoCliente = 0;
                } else {
                	movimientoCliente = 0;
                }
                
                PuntoAgarre palmeraCliente = null;
                int nivelActual = config.getNivel();
                if ((nivelActual == 1 || nivelActual == 2) && gestorPalmeras != null) {
                    try {
                        palmeraCliente = gestorPalmeras.buscarPalmeraCercana(jugadorControlable.getX(), jugadorControlable.getY(), jugadorControlable.getAncho(), jugadorControlable.getAlto());
                    } catch (Exception e) {
                        // Ignorar
                    }
                }
                
                float tiempoActual = com.badlogic.gdx.utils.TimeUtils.nanoTime() / 1_000_000_000f;
                
                if (capaColisiones == null) return;
                
                float mouseClienteX = inputCliente.getMouseX();
                float mouseClienteY = inputCliente.getMouseY();
                
                boolean clienteDisparo = false;
                if (inputCliente.isDisparar() && jugadorControlable.puedeDisparar(tiempoActual)) {
                    jugadorControlable.disparar(tiempoActual);
                    clienteDisparo = true;
                    
                    float balaX = jugadorControlable.getX() + jugadorControlable.getAncho() / 2f;
                    float balaY = jugadorControlable.getY() + jugadorControlable.getAlto() / 2f;
                    Bala balaCliente = new Bala(
                        balaX, 
                        balaY, 
                        mouseClienteX, 
                        mouseClienteY,
                        1
                    );
                    balas.add(balaCliente);
                    System.out.println("🔫 Jugador REMOTO (Cliente) disparó");
                }
                
                jugadorControlable.apuntarHacia(mouseClienteX, mouseClienteY);
                
                // 🔥 PROCESAR INPUT (actualiza posición y estado)
                jugadorControlable.procesarInput(
                    movimientoCliente, 
                    inputCliente.isSaltar(), 
                    inputCliente.isAgacharse(), 
                    clienteDisparo, 
                    inputCliente.isRecargar(), 
                    inputCliente.isAtacarCuchillo(), 
                    inputCliente.isAgarrarse(), 
                    palmeraCliente, 
                    delta, 
                    capaColisiones, 
                    tiempoActual, 
                    this.controladorDeAudio
                );
                
                // 🔥🔥🔥 CRÍTICO: LLAMAR actualizar() UNA SOLA VEZ DESPUÉS DE procesarInput() 🔥🔥🔥
                jugadorControlable.actualizar(delta);
            }
            
            if (tiempoSincronizacion >= INTERVALO_SINCRONIZACION) {
                enviarEstadoJuego();
                tiempoSincronizacion = 0f;
            }
            
        } else {
            // CLIENTE
            enviarInputJugador();
            
            GameStatePacket estado = networkManager.obtenerEstadoJuego();
            if (estado != null) {
                aplicarEstadoJuego(estado);
            }
            
            // 🔥 ACTUALIZAR ANIMACIONES DEL JUGADOR REMOTO (HOST visto desde CLIENTE)
            if (jugadorRemoto != null) {
                jugadorRemoto.actualizar(delta);
            }
        }
    }
    
    private void enviarEstadoJuego() {
        PlayerDataPacket datosJugador1 = jugador.crearDataPacket(0);
        
        PlayerDataPacket datosJugador2 = null;
        if (jugadorRemoto != null && jugadorRemoto instanceof JugadorRemotoControlable) {
            datosJugador2 = ((JugadorRemotoControlable) jugadorRemoto).crearDataPacket(1);
        }
        
        ArrayList<EnemyDataPacket> enemigosData = new ArrayList<>();
        for (int i = 0; i < controladorEnemigos.getEnemigos().size(); i++) {
            Enemigo enemigo = controladorEnemigos.getEnemigos().get(i);
            String tipo = enemigo.getClass().getSimpleName().toLowerCase();
            EnemyDataPacket enemigoData = new EnemyDataPacket(
                i, tipo, enemigo.getX(), enemigo.getY(), 
                enemigo.getVidaActual(), enemigo.getVidaMaxima(), 
                !enemigo.estaMuerto(), enemigo.getMirandoDerecha(), false
            );
            enemigosData.add(enemigoData);
        }
        
        // 🔥 CORREGIDO: Ahora incluye el ID del dueño
        ArrayList<BulletDataPacket> balasData = new ArrayList<>();
        for (int i = 0; i < balas.size(); i++) {
            Bala bala = balas.get(i);
            if (bala.estaActiva()) {
                BulletDataPacket balaData = new BulletDataPacket(
                    contadorIdBalas++, 
                    bala.getX(), 
                    bala.getY(), 
                    bala.getVelocidadX(), 
                    bala.getVelocidadY(), 
                    true,
                    bala.getIdDueño() // ⭐ INCLUIR ID DEL DUEÑO
                );
                balasData.add(balaData);
            }
        }
        
        ArrayList<PowerUpDataPacket> powerUpsData = new ArrayList<>();
        
        GameStatePacket estadoJuego = new GameStatePacket(
            datosJugador1, datosJugador2, enemigosData, balasData, 
            powerUpsData, config.getNivel(), false
        );
        networkManager.enviarEstadoJuego(estadoJuego);
    }

    
    private void enviarInputJugador() {
        boolean disparando = controladorDeEntradas.isDisparar();
        
        Vector3 mouseMundo = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camara.unproject(mouseMundo);
        
        PlayerInputPacket input = new PlayerInputPacket(config.getIdJugador(), controladorDeEntradas.isDerecha(), controladorDeEntradas.isIzquierda(), controladorDeEntradas.isSpace(), controladorDeEntradas.isAbajo(), disparando, controladorDeEntradas.isRecargar(), controladorDeEntradas.isV(), controladorDeEntradas.isShift(), mouseMundo.x, mouseMundo.y);
        networkManager.enviarInput(input);
    }
    
    private void aplicarEstadoJuego(GameStatePacket estado) {
        if (estado == null) return;
        
        if (estado.getJugador1() != null && jugadorRemoto != null) {
            jugadorRemoto.actualizarDesdePacket(estado.getJugador1());
        }
        
        if (estado.getJugador2() != null) {
            PlayerDataPacket datosCliente = estado.getJugador2();
            
            if (datosCliente.getPuntos() > jugador.getPuntos()) {
                int puntosNuevos = datosCliente.getPuntos() - jugador.getPuntos();
                jugador.sumarPuntos(puntosNuevos);
                System.out.println("💰 Cliente sincronizó puntos del servidor: +" + puntosNuevos + 
                                 " (Total: " + jugador.getPuntos() + ")");
            }
            
            if (datosCliente.getMunicionReserva() > jugador.getMunicionReserva()) {
                int municionNueva = datosCliente.getMunicionReserva() - jugador.getMunicionReserva();
                jugador.agregarMunicionGlock(municionNueva);
                System.out.println("📦 Cliente sincronizó munición del servidor: +" + municionNueva);
            }
        }
        
        if (estado.getEnemigos() != null) {
            for (EnemyDataPacket enemigoData : estado.getEnemigos()) {
                if (enemigoData.getIdEnemigo() < controladorEnemigos.getEnemigos().size()) {
                    Enemigo enemigo = controladorEnemigos.getEnemigos().get(enemigoData.getIdEnemigo());
                    enemigo.setX(enemigoData.getX());
                    enemigo.setY(enemigoData.getY());
                    enemigo.setVidaActual(enemigoData.getVidaActual());
                }
            }
        }
        
        sincronizarBalas(estado.getBalas());
    }
    
    private void sincronizarBalas(ArrayList<BulletDataPacket> balasRecibidas) {
        if (balasRecibidas == null || config.esHost()) return;
        
        for (BulletDataPacket balaData : balasRecibidas) {
            boolean encontrada = false;
            
            // Buscar si la bala ya existe localmente
            for (Bala balaLocal : balas) {
                float distancia = (float) Math.sqrt(
                    Math.pow(balaLocal.getX() - balaData.getX(), 2) + 
                    Math.pow(balaLocal.getY() - balaData.getY(), 2)
                );
                
                // ⭐ VERIFICAR TAMBIÉN EL ID DEL DUEÑO
                if (distancia < 50f && balaLocal.getIdDueño() == balaData.getIdDueño()) {
                    encontrada = true;
                    break;
                }
            }
            
            // Solo crear balas que NO sean del cliente local
            if (!encontrada && balaData.isActiva()) {
                // ⭐ EVITAR DUPLICAR BALAS DEL CLIENTE
                if (balaData.getIdDueño() == config.getIdJugador()) {
                    // Esta bala es del cliente local, ya debería existir
                    continue;
                }
                
                float destinoX = balaData.getX() + balaData.getVelocidadX() * 100;
                float destinoY = balaData.getY() + balaData.getVelocidadY() * 100;
                
                // ⭐ CREAR BALA CON EL ID DEL DUEÑO CORRECTO
                Bala nuevaBala = new Bala(
                    balaData.getX(), 
                    balaData.getY(), 
                    destinoX, 
                    destinoY,
                    balaData.getIdDueño() // ⭐ MANTENER EL ID ORIGINAL
                );
                balas.add(nuevaBala);
                
                System.out.println("🌐 Sincronizada bala del jugador " + balaData.getIdDueño());
            }
        }
    }
    
    private void renderJuegoCongelado() {
        tiledMapRenderer.setView(camara);
        tiledMapRenderer.render();

        batch.setProjectionMatrix(camara.combined);
        batch.begin();
        controladorEnemigos.renderTexturas(batch);
        jugador.render(batch);
        batch.end();

        hud.render();
    }

    private void renderMenuPausa(float delta) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        ShapeRenderer shape = new ShapeRenderer();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0, 0, 0, 0.55f);
        shape.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        shape.dispose();
        
        batch.begin();
        fontPausa.getData().setScale(3f);
        fontPausa.setColor(Color.CYAN);
        GlyphLayout layoutTitulo = new GlyphLayout(fontPausa, "PAUSA");
        fontPausa.draw(batch, layoutTitulo, Gdx.graphics.getWidth() / 2f - layoutTitulo.width / 2f, Gdx.graphics.getHeight() * 0.8f);

        fontPausa.getData().setScale(2f);
        float startY = Gdx.graphics.getHeight() * 0.55f;
        GlyphLayout layoutOpciones = new GlyphLayout();

        for (int i = 0; i < opcionesPausa.length; i++) {
            fontPausa.setColor(i == opcionSeleccionada ? Color.YELLOW : Color.WHITE);
            String texto = (i == opcionSeleccionada ? "> " + opcionesPausa[i] + " <" : opcionesPausa[i]);
            layoutOpciones.setText(fontPausa, texto);
            float opcionX = Gdx.graphics.getWidth() / 2f - layoutOpciones.width / 2f;
            float opcionY = startY - i * 80;
            fontPausa.draw(batch, layoutOpciones, opcionX, opcionY);
        }
        batch.end();

        if (tiempoUltimoInput >= DELAY_INPUT) {
            if (controladorDeEntradas.isAbajo()) {
                controladorDeAudio.reproducirSonido("menu_move");
                opcionSeleccionada = (opcionSeleccionada + 1) % opcionesPausa.length;
                tiempoUltimoInput = 0;
            } else if (controladorDeEntradas.isArriba()) {
                controladorDeAudio.reproducirSonido("menu_move");
                opcionSeleccionada = (opcionSeleccionada - 1 + opcionesPausa.length) % opcionesPausa.length;
                tiempoUltimoInput = 0;
            } else if (controladorDeEntradas.isEnter()) {
                seleccionarOpcionPausa();
                tiempoUltimoInput = 0;
            }
        }
        tiempoUltimoInput += delta;
    }

    private void seleccionarOpcionPausa() {
        controladorDeAudio.reproducirSonido("menu_move");
        switch (opcionSeleccionada) {
            case 0:
                juegoPausado = false;
                break;
            case 1:
                controladorDeAudio.detenerTodaMusica();
                controladorDeAudio.iniciarMusicaMenu();
                game.setScreen(new PantallaMenuPrincipal(game, new BitmapFont(), controladorDeAudio, batch, controladorDeEntradas, config));
                break;
            case 2:
                Gdx.app.exit();
                break;
        }
    }
    
    private void renderTienda() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        ShapeRenderer shape = new ShapeRenderer();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0, 0, 0, 0.75f);
        shape.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        shape.dispose();

        batch.setProjectionMatrix(hud.getStage().getCamera().combined);
        batch.begin();
        
        fontTienda.getData().setScale(3.5f);
        fontTienda.setColor(Color.GOLD);
        GlyphLayout layoutTitulo = new GlyphLayout(fontTienda, "TIENDA");
        fontTienda.draw(batch, layoutTitulo, Gdx.graphics.getWidth() / 2f - layoutTitulo.width / 2f, Gdx.graphics.getHeight() * 0.9f);
        
        fontTienda.getData().setScale(2f);
        fontTienda.setColor(Color.GREEN);
        String textoPuntos = "Tus puntos: " + jugador.getPuntos();
        GlyphLayout layoutPuntos = new GlyphLayout(fontTienda, textoPuntos);
        fontTienda.draw(batch, layoutPuntos, Gdx.graphics.getWidth() / 2f - layoutPuntos.width / 2f, Gdx.graphics.getHeight() * 0.82f);

        float centerX = Gdx.graphics.getWidth() / 2f;
        float startY = Gdx.graphics.getHeight() * 0.72f;
        float espaciadoItem = 90f;

        for (int i = 0; i < itemsDisponibles.size(); i++) {
            ItemTienda item = itemsDisponibles.get(i);
            float itemY = startY - (i * espaciadoItem);
            boolean seleccionado = (i == opcionTiendaSeleccionada);
            fontTienda.setColor(seleccionado ? Color.YELLOW : Color.WHITE);
            
            if (item.tipo.equals("salir")) {
                fontTienda.getData().setScale(2.5f);
                String textoSalir = seleccionado ? "> SALIR <" : "SALIR";
                GlyphLayout layoutSalir = new GlyphLayout(fontTienda, textoSalir);
                fontTienda.draw(batch, layoutSalir, centerX - layoutSalir.width / 2f, itemY);
                
            } else if (item.tipo.equals("medicina")) {
                fontTienda.getData().setScale(2.2f);
                String textoMedicina = (seleccionado ? "> " : "") + item.nombre + " - " + item.precio + " PTS" + (seleccionado ? " <" : "");
                GlyphLayout layoutMedicina = new GlyphLayout(fontTienda, textoMedicina);
                fontTienda.draw(batch, layoutMedicina, centerX - layoutMedicina.width / 2f, itemY);
                
                fontTienda.getData().setScale(1.5f);
                fontTienda.setColor(Color.CYAN);
                GlyphLayout layoutDesc = new GlyphLayout(fontTienda, item.descripcion);
                fontTienda.draw(batch, layoutDesc, centerX - layoutDesc.width / 2f, itemY - 25f);
                
            } else if (item.tipo.equals("arma")) {
                fontTienda.getData().setScale(2.2f);
                String textoArma = (seleccionado ? "> " : "") + item.nombre + " - " + item.precio + " PTS" + (seleccionado ? " <" : "");
                GlyphLayout layoutArma = new GlyphLayout(fontTienda, textoArma);
                fontTienda.draw(batch, layoutArma, centerX - layoutArma.width / 2f, itemY);
                
                fontTienda.getData().setScale(1.4f);
                fontTienda.setColor(Color.CYAN);
                String stats = "Daño: " + item.daño + " | Munición: " + item.municionMaxima + "/" + item.municionReserva + " | Recarga: " + item.tiempoRecarga + "s";
                GlyphLayout layoutStats = new GlyphLayout(fontTienda, stats);
                fontTienda.draw(batch, layoutStats, centerX - layoutStats.width / 2f, itemY - 25f);
            }
        }
        
        fontTienda.getData().setScale(1.3f);
        fontTienda.setColor(Color.LIGHT_GRAY);
        String instrucciones = "Arriba/Abajo: Navegar | Enter: Comprar | ESC: Salir";
        GlyphLayout layoutInst = new GlyphLayout(fontTienda, instrucciones);
        fontTienda.draw(batch, layoutInst, centerX - layoutInst.width / 2f, Gdx.graphics.getHeight() * 0.08f);
        
        batch.end();
        
        if (tiempoUltimoInput >= DELAY_INPUT) {
            if (controladorDeEntradas.isAbajo()) {
                controladorDeAudio.reproducirSonido("menu_move");
                opcionTiendaSeleccionada = (opcionTiendaSeleccionada + 1) % itemsDisponibles.size();
                tiempoUltimoInput = 0;
            } else if (controladorDeEntradas.isArriba()) {
                controladorDeAudio.reproducirSonido("menu_move");
                opcionTiendaSeleccionada = (opcionTiendaSeleccionada - 1 + itemsDisponibles.size()) % itemsDisponibles.size();
                tiempoUltimoInput = 0;
            } else if (controladorDeEntradas.isEnter()) {
                procesarCompraTienda();
                tiempoUltimoInput = 0;
            } else if (controladorDeEntradas.isEscape()) {
                tiendaActiva = false;
                tiempoUltimoInput = 0;
            }
        }
    }
    
    private void procesarCompraTienda() {
        if (opcionTiendaSeleccionada >= itemsDisponibles.size()) return;
        
        ItemTienda itemSeleccionado = itemsDisponibles.get(opcionTiendaSeleccionada);
        
        if (itemSeleccionado.tipo.equals("salir")) {
            tiendaActiva = false;
            return;
        }
        
        if (jugador.getPuntos() < itemSeleccionado.precio) {
            System.out.println("❌ No tienes suficientes puntos (necesitas " + itemSeleccionado.precio + ", tienes " + jugador.getPuntos() + ")");
            return;
        }
        
        if (itemSeleccionado.tipo.equals("medicina")) {
            jugador.restarPuntos(itemSeleccionado.precio);
            jugador.recuperarSalud(jugador.getSaludMaxima());
            controladorDeAudio.reproducirSonido("powerup");
            System.out.println("💊 ¡Medicina comprada! Salud restaurada completamente.");
            
            if (modoOnline && networkManager != null) {
                ShopPurchasePacket compra = new ShopPurchasePacket(config.getIdJugador(), itemSeleccionado.precio);
                networkManager.enviarCompraTienda(compra);
            }
            return;
        }
        
        if (itemSeleccionado.tipo.equals("arma")) {
            int slotDisponible = jugador.getSiguienteSlotDisponible();
            
            if (slotDisponible == -1) {
                System.out.println("❌ Ya tienes todas las armas disponibles");
                return;
            }
            
            jugador.restarPuntos(itemSeleccionado.precio);
            jugador.agregarArma(slotDisponible, itemSeleccionado.nombre, itemSeleccionado.daño, itemSeleccionado.municionMaxima, itemSeleccionado.municionReserva, itemSeleccionado.tiempoRecarga);
            controladorDeAudio.reproducirSonido("powerup");
            System.out.println("🔫 ¡" + itemSeleccionado.nombre + " comprada y agregada al Slot " + slotDisponible + "!");
            
            if (modoOnline && networkManager != null) {
                ShopPurchasePacket compra = new ShopPurchasePacket(config.getIdJugador(), itemSeleccionado.nombre, slotDisponible, itemSeleccionado.precio, itemSeleccionado.daño, itemSeleccionado.municionMaxima, itemSeleccionado.municionReserva, itemSeleccionado.tiempoRecarga);
                networkManager.enviarCompraTienda(compra);
            }
            
            inicializarTienda();
        }
    }
    
    private void avanzarDeNivel() {
        int siguienteNivel = config.getNivel() + 1;
        config.setNivel(siguienteNivel);
        controladorEnemigos.limpiar();
        if (gestorPalmeras != null) gestorPalmeras.dispose();
        this.game.setScreen(new PantallaJuego(this.game, batch, controladorDeAudio, controladorDeEntradas, config));
    }

    @Override 
    public void resize(int width, int height) { 
        viewport.update(width, height); 
    }
    
    @Override 
    public void hide() { 
        dispose(); 
    }
    
    @Override
    public void dispose() {
        System.out.println("🛑 PantallaJuego.dispose() llamado");
        
        if (principal != null && networkManager != null) {
            System.out.println("🔌 Desregistrando NetworkManager desde PantallaJuego...");
            principal.desregistrarNetworkManager();
        }
        
        if (tiledMap != null) tiledMap.dispose();
        if (hud != null) hud.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (fontPausa != null) fontPausa.dispose();
        if (fontTienda != null) fontTienda.dispose();
        if (controladorEnemigos != null) controladorEnemigos.limpiar();
        if (gestorPalmeras != null) gestorPalmeras.dispose();
        if (jugadorRemoto != null) jugadorRemoto.dispose();
        if (pantallaSpectate != null) pantallaSpectate.dispose();
        
        System.out.println("✅ PantallaJuego recursos liberados");
    }
    
    @Override
    public void pause() {}

    @Override
    public void resume() {}
}