package com.MiEmpresa.OperacionSinRetorno;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import audio.ControladorDeAudio;
import configuracion.ConfiguracionJuego;
import configuracion.EstadoJuego;
import hud.HUD;
import jugador.ControladorEntrada;
import jugador.Jugador;
import menus.MenuDificultad;
import menus.MenuOpciones;
import menus.MenuPersonalizacion;
import menus.MenuPrincipal;
import powerups.PowerUp;

public class Principal extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture image;
    private BitmapFont font;
    
    //HUD:
    private HUD hud;
    
    // Configuración y estados
    private EstadoJuego estadoActual = EstadoJuego.MENU_PRINCIPAL;
    private ConfiguracionJuego config;
    private boolean juegoInicializado = false;

    // Menús
    private MenuPrincipal menuPrincipal;
    private MenuPersonalizacion menuPersonalizacion;
    private MenuDificultad menuDificultad;
    private MenuOpciones menuOpciones;

    // Cámara
    private OrthographicCamera camara;
    private Viewport viewport;
    private static final float ANCHO_PANTALLA = 800;
    private static final float ALTO_PANTALLA = 600;

    // Mapa y colisiones
    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private TiledMapTileLayer capaColisiones;

    // Audio
    private ControladorDeAudio controladorDeAudio;

    // Jugador
    private Jugador jugador;
    private ControladorEntrada controladorDeEntrada;
    private ShapeRenderer shapeRenderer;
    
    // POWER-UP:
    private PowerUp powerUpRecargaRapida;
    private boolean powerUpCreado = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        font = new BitmapFont();
        font.getData().setScale(2f);

        config = ConfiguracionJuego.getInstancia();

     // cargar sonidos:
        controladorDeAudio = new ControladorDeAudio();
        controladorDeAudio.cargarMusica();
        controladorDeAudio.iniciarMusicaMenu();
        controladorDeAudio.cargarSonidos("salto", "Efectos de sonido/Sonido de salto.mp3");
        controladorDeAudio.cargarSonidos("menu_select", "Efectos de sonido/menu_select.mp3");
        controladorDeAudio.cargarSonidos("menu_move", "Efectos de sonido/menu_move.mp3");
        
        controladorDeAudio.cargarSonidos("disparo", "Efectos de sonido/disparar.mp3");
        controladorDeAudio.cargarSonidos("recarga", "Efectos de sonido/recargar.mp3");
        controladorDeAudio.cargarSonidos("powerup", "Efectos de sonido/powerup.mp3");
        
        
     // Inicializar menús
        menuPrincipal = new MenuPrincipal(this, font, controladorDeAudio);
        menuPersonalizacion = new MenuPersonalizacion(this, font, controladorDeAudio);
        menuDificultad = new MenuDificultad(this, font, controladorDeAudio);
        menuOpciones = new MenuOpciones(this, font, controladorDeAudio);

        shapeRenderer = new ShapeRenderer();
        controladorDeEntrada = new ControladorEntrada();

        // Seteamos input al menú principal
        Gdx.input.setInputProcessor(menuPrincipal);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        switch (estadoActual) {
            case MENU_PRINCIPAL:
            	menuPrincipal.render(batch);
                break;
            case MENU_PERSONALIZACION:
                menuPersonalizacion.render(batch);
                break;
            case MENU_DIFICULTAD:
                menuDificultad.render(batch);
                break;
            case OPCIONES:
                menuOpciones.render(batch);
                
                break;
            case JUGANDO_SOLO:
            case JUGANDO_COOPERATIVO:
                if (!juegoInicializado) {
                    inicializarJuego();
                    juegoInicializado = true;
                }
                renderJuego();
                break;
            case PAUSADO:
                renderJuego(); // Dibuja, pero no actualiza
                renderMenuPausa();
                break;
            case GAME_OVER:
                renderGameOver();
                break;
        }
    }

    private void inicializarJuego() {
        tiledMap = new TmxMapLoader().load("Mapas_Niveles/Nivel 1.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        capaColisiones = (TiledMapTileLayer) tiledMap.getLayers().get("colisiones");

        camara = new OrthographicCamera(ANCHO_PANTALLA,ALTO_PANTALLA);
        viewport = new FillViewport(ANCHO_PANTALLA, ALTO_PANTALLA, camara);
      
        // Spawn del jugador desde el mapa
        MapObject spawnObj = tiledMap.getLayers().get("spawns").getObjects().get("spawn");
        float spawnX = (float) spawnObj.getProperties().get("x");
        float spawnY = (float) spawnObj.getProperties().get("y");
        jugador = new Jugador(spawnX, spawnY);
        
        
        jugador.setControladorAudio(controladorDeAudio);
        // CREAR POWER-UP CERCA DEL FINAL DEL MAPA 
        if (!powerUpCreado) {
            // Colocar power-up cerca del final del mapa
            float mapWidth = capaColisiones.getWidth() * capaColisiones.getTileWidth();
            float powerUpX = mapWidth - 200; // 200 píxeles antes del final
            float powerUpY = 380; // Altura adecuada
            
            powerUpRecargaRapida = new PowerUp(powerUpX, powerUpY);
            powerUpCreado = true;
        }
        
        
        hud = new HUD(batch);
        
        camara.update();
        
        Gdx.input.setInputProcessor(controladorDeEntrada);

        controladorDeAudio.iniciarMusicaNivel1();
    }

    private void renderJuego() {
        // Cámara sigue al jugador
        float mapWidth = capaColisiones.getWidth() * capaColisiones.getTileWidth();
        float mapHeight = capaColisiones.getHeight() * capaColisiones.getTileHeight();
        float halfWidth = camara.viewportWidth / 2f;
        float halfHeight = camara.viewportHeight / 2f;

        float camX = Math.max(halfWidth, Math.min(jugador.getX() + jugador.getAncho() / 2f, mapWidth - halfWidth));
        float camY = Math.max(halfHeight, Math.min(jugador.getY() + jugador.getAlto() / 2f, mapHeight - halfHeight));
        
        camara.position.lerp(new Vector3(camX, camY, 0), 0.1f);
        camara.update();

        tiledMapRenderer.setView(camara);
        tiledMapRenderer.render();

        batch.setProjectionMatrix(camara.combined);
        shapeRenderer.setProjectionMatrix(camara.combined);

        batch.begin();
        jugador.render(batch);
        batch.end();
        
        // RENDERIZAR POWER-UP 
        if (powerUpRecargaRapida != null && !powerUpRecargaRapida.estaRecolectado()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            powerUpRecargaRapida.render(shapeRenderer);
            shapeRenderer.end();
        }
        
        
        
        hud.render();
       
        // ACTUALIZAR HUD CON INFORMACIÓN DE MUNICIÓN Y POWER-UP
        hud.actualizarMunicion(jugador.getMunicionActual(), jugador.getMunicionMaxima(), jugador.estaRecargando(), jugador.getProgresoRecarga());
        hud.actualizarPowerUp(jugador.tienePowerUpRecargaRapida(), jugador.getTiempoPowerUpRestante());

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(jugador.getX(), jugador.getY(), jugador.getAncho(), jugador.getAlto());
        shapeRenderer.end();

        if (estadoActual == EstadoJuego.PAUSADO) return;
        
        // ACTUALIZAR POWER-UP
        if (powerUpRecargaRapida != null) {
            powerUpRecargaRapida.actualizar(Gdx.graphics.getDeltaTime());
            
            // Verificar colisión con power-up
            if (!powerUpRecargaRapida.estaRecolectado() && 
                powerUpRecargaRapida.verificarColision(jugador.getX(), jugador.getY(),jugador.getAncho(), jugador.getAlto())) {
                
                // Recolectar power-up
                powerUpRecargaRapida.recolectar();
                jugador.activarPowerUpRecargaRapida();
                controladorDeAudio.reproducirSonido("powerup");
                System.out.println("¡Power-up de recarga rápida activado!");
            }
        }
        
        // Movimiento
        float dx = 0;
        boolean saltar = false;
        boolean disparoIzquierdaArriba = false;
        boolean disparoDerechaArriba = false;
        boolean agacharse = false;
        boolean intentarDisparar = false;
        boolean recargarManual = false;

        boolean izquierda = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean derecha = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean arriba = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean abajo = Gdx.input.isKeyPressed(Input.Keys.S);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            estadoActual = EstadoJuego.PAUSADO;
            return;
        }
        
        // CONTROL DE DISPARO CON ALT
        if (Gdx.input.isKeyJustPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.ALT_RIGHT)) {
            intentarDisparar = true;
        }
        
        //CONTROL DE RECARGA MANUAL CON R
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            recargarManual = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            estadoActual = EstadoJuego.PAUSADO;
            return;
        }

        if (abajo) agacharse = true;

        if (!agacharse && izquierda && arriba) {
            disparoIzquierdaArriba = true;
            dx = -1;
        } else if (!agacharse && derecha && arriba) {
            disparoDerechaArriba = true;
            dx = 1;
        } else if (!agacharse) {
            if (izquierda) dx = -1;
            else if (derecha) dx = 1;

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                controladorDeAudio.reproducirSonido("salto");
                saltar = true;
            }
        }

        jugador.actualizarMovimientoJugador(dx, saltar, disparoIzquierdaArriba, disparoDerechaArriba, agacharse, intentarDisparar, recargarManual, Gdx.graphics.getDeltaTime(), capaColisiones, com.badlogic.gdx.utils.TimeUtils.nanoTime() / 1000000000.0f);
    }

    private void renderMenuPausa() {
    	float  x= jugador.getX(), y= jugador.getY();
    	batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "JUEGO PAUSADO", x, y);
        font.draw(batch, "ENTER para continuar", x, y-20);
        font.draw(batch, "M para volver al menu", x, y-40);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            estadoActual = config.isModoCooperativo() ? EstadoJuego.JUGANDO_COOPERATIVO : EstadoJuego.JUGANDO_SOLO;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            volverAlMenuPrincipal();
        }
    }

    private void renderGameOver() {
        batch.begin();
        font.setColor(Color.RED);
        font.draw(batch, "GAME OVER", 300, 350);
        font.setColor(Color.WHITE);
        font.draw(batch, "R para reiniciar", 300, 300);
        font.draw(batch, "M para volver al menu", 300, 250);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            juegoInicializado = false;
            estadoActual = config.isModoCooperativo() ? EstadoJuego.JUGANDO_COOPERATIVO : EstadoJuego.JUGANDO_SOLO;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            volverAlMenuPrincipal();
        }
    }

    @SuppressWarnings("incomplete-switch")
	public void cambiarEstado(EstadoJuego nuevoEstado) {
        estadoActual = nuevoEstado;
        switch (nuevoEstado) {
            case MENU_PRINCIPAL: 
            	Gdx.input.setInputProcessor(menuPrincipal);
            	break;
            
            case MENU_PERSONALIZACION: 
            	Gdx.input.setInputProcessor(menuPersonalizacion); 
            	break;
            
            case MENU_DIFICULTAD: 
            	Gdx.input.setInputProcessor(menuDificultad); 
            	break;
            
            case OPCIONES: 
            	Gdx.input.setInputProcessor(menuOpciones); 
            	break;
            
            case JUGANDO_SOLO:
            case JUGANDO_COOPERATIVO: 
            	Gdx.input.setInputProcessor(controladorDeEntrada); 
            	break;
        }
    }

    public void volverAlMenuPrincipal() {
        juegoInicializado = false;
        powerUpCreado = false;	
        estadoActual = EstadoJuego.MENU_PRINCIPAL;
        Gdx.input.setInputProcessor(menuPrincipal);
        camara.position.set(ANCHO_PANTALLA / 2f, ALTO_PANTALLA / 2f, 0);
        camara.update();
        batch.setProjectionMatrix(camara.combined);
        controladorDeAudio.volverAlMenu();
    }
    
    public void iniciarJuego() {
        if (config.isModoCooperativo()) {
            estadoActual = EstadoJuego.JUGANDO_COOPERATIVO;
        } else {
            estadoActual = EstadoJuego.JUGANDO_SOLO;
        }
        juegoInicializado = false;
        Gdx.input.setInputProcessor(controladorDeEntrada);
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height);
        if (hud != null) hud.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
        font.dispose();
        hud.dispose();
        controladorDeAudio.dispose();
        if (jugador != null) jugador.dispose();
        shapeRenderer.dispose();
        if (tiledMap != null) tiledMap.dispose();
    }
}
