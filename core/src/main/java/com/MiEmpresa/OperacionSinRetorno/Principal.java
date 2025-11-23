package com.MiEmpresa.OperacionSinRetorno;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import audio.ControladorDeAudio;
import configuracion.ConfiguracionJuego;
import jugador.ControladorEntrada;
import pantallas.*;
import network.NetworkManager; // 🔥 VERIFICAR QUE ESTÉ ESTE IMPORT

public class Principal extends Game {
    private SpriteBatch batch;
    private Texture image;
    private BitmapFont font;

    // Configuración
    private ConfiguracionJuego config;
    // Audio
    private ControladorDeAudio controladorDeAudio;
    private ControladorEntrada controladorDeEntrada;

    // 🔥🔥🔥 VERIFICAR QUE ESTÉ ESTA LÍNEA 🔥🔥🔥
    private NetworkManager networkManager;

    // 🔥 Flag para evitar dispose múltiple
    private boolean recursosLiberados = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        font = new BitmapFont();
        font.getData().setScale(2f);
        controladorDeAudio = new ControladorDeAudio();
        controladorDeEntrada = new ControladorEntrada();
        config = new ConfiguracionJuego();

        // 🔥🔥🔥 SHUTDOWN HOOK MEJORADO 🔥🔥🔥
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🛑 ShutdownHook activado - Limpiando recursos...");
            System.out.println("🔍 DIAGNÓSTICO: NetworkManager es null? " + (networkManager == null)); // 🔥 AGREGAR
            
            // 🔥 PRIMERO CERRAR RED (MUY IMPORTANTE)
            if (networkManager != null) {
                try {
                    System.out.println("🌐 Cerrando conexiones de red...");
                    networkManager.detener();
                    System.out.println("✅ Conexiones de red cerradas");
                } catch (Exception e) {
                    System.err.println("⚠️ Error al cerrar red: " + e.getMessage());
                    e.printStackTrace(); // 🔥 AGREGAR PARA VER EL ERROR
                }
            } else {
                System.out.println("⚠️ DIAGNÓSTICO: NetworkManager es NULL, no se registró"); // 🔥 AGREGAR
            }
            
            liberarRecursos();
        }, "ShutdownHook-Thread"));

        System.out.println("✅ ShutdownHook registrado");

        this.setScreen(new PantallaMenuPrincipal(this, font, controladorDeAudio, batch, controladorDeEntrada, config));
    }

    /**
     * 🔥 MÉTODO CENTRALIZADO PARA LIBERAR RECURSOS
     */
    private void liberarRecursos() {
        if (recursosLiberados) {
            System.out.println("⚠️ Recursos ya liberados, saltando...");
            return;
        }

        System.out.println("🛑 Liberando recursos...");

        try {
            // Limpiar audio
            if (controladorDeAudio != null) {
                controladorDeAudio.dispose();
                System.out.println("✅ Audio liberado");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error al liberar audio: " + e.getMessage());
        }

        try {
            // Limpiar batch
            if (batch != null) {
                batch.dispose();
                System.out.println("✅ SpriteBatch liberado");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error al liberar batch: " + e.getMessage());
        }

        try {
            // Limpiar texturas
            if (image != null) {
                image.dispose();
                System.out.println("✅ Texturas liberadas");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error al liberar texturas: " + e.getMessage());
        }

        try {
            // Limpiar fuentes
            if (font != null) {
                font.dispose();
                System.out.println("✅ Fuentes liberadas");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error al liberar fuentes: " + e.getMessage());
        }

        recursosLiberados = true;
        System.out.println("✅ Todos los recursos liberados correctamente");
    }

    // 🔥🔥🔥 VERIFICAR QUE ESTÉN ESTOS DOS MÉTODOS 🔥🔥🔥
    
    /**
     * Llama a este método desde PantallaJuego cuando crees el NetworkManager
     */
    public void registrarNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
        System.out.println("✅ NetworkManager registrado en Principal");
    }

    /**
     * Desregistra y detiene el NetworkManager
     */
    public void desregistrarNetworkManager() {
        if (this.networkManager != null) {
            System.out.println("🛑 Desregistrando NetworkManager...");
            this.networkManager.detener();
            this.networkManager = null;
        }
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        System.out.println("🛑 Principal.dispose() llamado");
        
        // 🔥 CERRAR RED PRIMERO
        if (networkManager != null) {
            System.out.println("🌐 Cerrando NetworkManager desde dispose()...");
            networkManager.detener();
            networkManager = null;
        }
        
        liberarRecursos();
    }
    
    
}