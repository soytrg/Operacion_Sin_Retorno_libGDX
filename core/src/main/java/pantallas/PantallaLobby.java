package pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import audio.ControladorDeAudio;
import configuracion.ConfiguracionJuego;
import jugador.ControladorEntrada;
import network.NetworkManager;
import packets.LevelChangePacket;
import packets.PlayerDeathPacket;
import packets.ShopPurchasePacket;

public class PantallaLobby implements Screen {
    
    private final Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private ConfiguracionJuego config;
    private ControladorDeAudio audio;
    private ControladorEntrada controladorEntrada;
    
    private NetworkManager networkManager;
    private boolean esperandoConexion = false;
    private String mensajeEstado = "";
    
    private String[] opciones = {"Crear Partida (Host)", "Unirse a Partida", "Volver"};
    private int opcionSeleccionada = 0;
    
    private boolean enMenuIP = false;
    private String ipIngresada = "";
    private float tiempoUltimoInput = 0f;
    private final float DELAY_INPUT = 0.2f;
    
    public PantallaLobby(Game game, SpriteBatch batch, ControladorDeAudio audio, 
                        ControladorEntrada controladorEntrada, ConfiguracionJuego config) {
        this.game = game;
        this.batch = batch;
        this.audio = audio;
        this.controladorEntrada = controladorEntrada;
        this.config = config;
    }
    
    @Override
    public void show() {
        font = new BitmapFont();
        font.getData().setScale(2f);
        config.setModoOnline(true);
        
        Gdx.input.setInputProcessor(this.controladorEntrada); 

    }
    
    @Override
    public void render(float delta) {
        tiempoUltimoInput += delta;
        
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        
        if (enMenuIP) {
            renderMenuIP(delta);
        } else if (esperandoConexion) {
            renderEsperando();
        } else {
            renderMenu();
        }
    }
    
    private void renderMenu() {
    	  boolean arriba = this.controladorEntrada.isArriba();
          boolean abajo = this.controladorEntrada.isAbajo();
          boolean enter = this.controladorEntrada.isEnter();      
    
    	
        batch.begin();
        
        font.setColor(Color.CYAN);
        font.getData().setScale(3f);
        GlyphLayout titulo = new GlyphLayout(font, "MODO ONLINE");
        font.draw(batch, titulo, 
            Gdx.graphics.getWidth() / 2f - titulo.width / 2f, 
            Gdx.graphics.getHeight() * 0.8f);
        
        font.getData().setScale(2f);
        float startY = Gdx.graphics.getHeight() * 0.5f;
        
        for (int i = 0; i < opciones.length; i++) {
            font.setColor(i == opcionSeleccionada ? Color.YELLOW : Color.WHITE);
            String texto = (i == opcionSeleccionada ? "> " : "") + opciones[i] + (i == opcionSeleccionada ? " <" : "");
            GlyphLayout layout = new GlyphLayout(font, texto);
            font.draw(batch, layout,
                Gdx.graphics.getWidth() / 2f - layout.width / 2f,
                startY - i * 80);
        }
        
        batch.end();
        
        if (tiempoUltimoInput >= DELAY_INPUT) {
            if (abajo) {
                audio.reproducirSonido("menu_move");
                opcionSeleccionada = (opcionSeleccionada + 1) % opciones.length;
                tiempoUltimoInput = 0f;
            } else if (arriba) {
                audio.reproducirSonido("menu_move");
                opcionSeleccionada = (opcionSeleccionada - 1 + opciones.length) % opciones.length;
                tiempoUltimoInput = 0f;
            } else if (enter) {
                audio.reproducirSonido("menu_move");
                seleccionarOpcion();
                tiempoUltimoInput = 0f;
            }
        }
    }
    
    private void renderMenuIP(float delta) {
        batch.begin();
        
        font.setColor(Color.CYAN);
        font.getData().setScale(2.5f);
        GlyphLayout titulo = new GlyphLayout(font, "Ingresa la IP del Host:");
        font.draw(batch, titulo,
            Gdx.graphics.getWidth() / 2f - titulo.width / 2f,
            Gdx.graphics.getHeight() * 0.7f);
        
        font.setColor(Color.YELLOW);
        font.getData().setScale(3f);
        String ipMostrada = ipIngresada + "_";
        GlyphLayout layoutIP = new GlyphLayout(font, ipMostrada);
        font.draw(batch, layoutIP,
            Gdx.graphics.getWidth() / 2f - layoutIP.width / 2f,
            Gdx.graphics.getHeight() * 0.5f);
        
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        GlyphLayout instrucciones = new GlyphLayout(font, "Enter: Conectar | ESC: Cancelar");
        font.draw(batch, instrucciones,
            Gdx.graphics.getWidth() / 2f - instrucciones.width / 2f,
            Gdx.graphics.getHeight() * 0.3f);
        
        batch.end();
        
        if (tiempoUltimoInput >= DELAY_INPUT) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && ipIngresada.length() > 0) {
                ipIngresada = ipIngresada.substring(0, ipIngresada.length() - 1);
                tiempoUltimoInput = 0f;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                conectarseAHost();
                tiempoUltimoInput = 0f;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                enMenuIP = false;
                ipIngresada = "";
                tiempoUltimoInput = 0f;
            }
            
            // Capturar números del teclado principal
            for (int i = 0; i <= 9; i++) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0 + i)) {
                    if (ipIngresada.length() < 15) {
                        ipIngresada += i;
                        tiempoUltimoInput = 0f;
                    }
                }
            }
            
            // Capturar punto
            if (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD)) {
                if (ipIngresada.length() < 15) {
                    ipIngresada += ".";
                    tiempoUltimoInput = 0f;
                }
            }
        }
    }
    
    private void renderEsperando() {
        batch.begin();
        
        font.setColor(Color.YELLOW);
        font.getData().setScale(2.5f);
        GlyphLayout layout = new GlyphLayout(font, mensajeEstado);
        font.draw(batch, layout,
            Gdx.graphics.getWidth() / 2f - layout.width / 2f,
            Gdx.graphics.getHeight() * 0.6f);
        
        if (config.esHost()) {
            font.setColor(Color.WHITE);
            font.getData().setScale(2f);
            String ip = "Tu IP: " + obtenerIPLocal();
            GlyphLayout layoutIP = new GlyphLayout(font, ip);
            font.draw(batch, layoutIP,
                Gdx.graphics.getWidth() / 2f - layoutIP.width / 2f,
                Gdx.graphics.getHeight() * 0.4f);
            
            font.getData().setScale(1.5f);
            font.setColor(Color.LIGHT_GRAY);
            GlyphLayout info = new GlyphLayout(font, "El otro jugador debe usar esta IP para conectarse");
            font.draw(batch, info,
                Gdx.graphics.getWidth() / 2f - info.width / 2f,
                Gdx.graphics.getHeight() * 0.3f);
        }
        
        font.setColor(Color.RED);
        font.getData().setScale(1.5f);
        GlyphLayout cancelar = new GlyphLayout(font, "ESC para cancelar");
        font.draw(batch, cancelar,
            Gdx.graphics.getWidth() / 2f - cancelar.width / 2f,
            Gdx.graphics.getHeight() * 0.15f);
        
        batch.end();
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (networkManager != null) {
                networkManager.detener();
            }
            esperandoConexion = false;
        }
    }
    
    private void seleccionarOpcion() {
        switch (opcionSeleccionada) {
            case 0: // Crear Partida
                crearPartida();
                break;
            case 1: // Unirse
                enMenuIP = true;
                ipIngresada = "";
                break;
            case 2: // Volver
                game.setScreen(new PantallaMenuPrincipal(game, font, audio, batch, controladorEntrada, config));
                break;
        }
    }
    
    private void crearPartida() {
        config.setEsHost(true);
        config.setIdJugador(0);
        esperandoConexion = true;
        mensajeEstado = "Esperando jugador...";
        
        networkManager = new NetworkManager(config, new NetworkManager.NetworkCallback() {
            @Override
            public void onClienteConectado(String nombreCliente) {
                Gdx.app.postRunnable(() -> {
                    System.out.println("✅ Cliente conectado: " + nombreCliente);
                    game.setScreen(new PantallaJuego(game, batch, audio, controladorEntrada, config));
                });
            }
            
            @Override
            public void onClienteDesconectado(String razon) {
                System.out.println("⚠️ Cliente desconectado: " + razon);
            }
            
            @Override
            public void onConexionExitosa() {}
            
            @Override
            public void onConexionFallida(String razon) {}
            
            @Override
            public void onCambioNivel(LevelChangePacket cambioNivel) {}

            @Override
            public void onMuerteJugador(PlayerDeathPacket muerte) {}
            
            @Override
            public void onCompraEnTienda(ShopPurchasePacket compra) {}
        });
        
        networkManager.iniciar();
    }
    
    private void conectarseAHost() {
        if (ipIngresada.isEmpty()) {
            ipIngresada = "localhost";
        }
        
        config.setEsHost(false);
        config.setIdJugador(1);
        config.setIpServidor(ipIngresada);
        esperandoConexion = true;
        mensajeEstado = "Conectando a " + ipIngresada + "...";
        enMenuIP = false;
        
        networkManager = new NetworkManager(config, new NetworkManager.NetworkCallback() {
            @Override
            public void onClienteConectado(String nombreCliente) {}
            
            @Override
            public void onClienteDesconectado(String razon) {
                Gdx.app.postRunnable(() -> {
                    esperandoConexion = false;
                    mensajeEstado = "Desconectado: " + razon;
                });
            }
            
            @Override
            public void onConexionExitosa() {
                Gdx.app.postRunnable(() -> {
                    System.out.println("✅ Conexión exitosa");
                    game.setScreen(new PantallaJuego(game, batch, audio, controladorEntrada, config));
                });
            }
            
            @Override
            public void onConexionFallida(String razon) {
                Gdx.app.postRunnable(() -> {
                    esperandoConexion = false;
                    mensajeEstado = "Error: " + razon;
                });
            }
            
            @Override
            public void onCambioNivel(LevelChangePacket cambioNivel) {}

            @Override
            public void onMuerteJugador(PlayerDeathPacket muerte) {}
            
            @Override
            public void onCompraEnTienda(ShopPurchasePacket compra) {}
        });
        
        networkManager.iniciar();
    }
    
    private String obtenerIPLocal() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "No disponible";
        }
    }
    
    @Override
    public void resize(int width, int height) {}
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void hide() {
        if (networkManager != null) {
            networkManager.detener();
        }
    }
    
    @Override
    public void dispose() {
        if (font != null) font.dispose();
        if (networkManager != null) {
            networkManager.detener();
        }
    }
}