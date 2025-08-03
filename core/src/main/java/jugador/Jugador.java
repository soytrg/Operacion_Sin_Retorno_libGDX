package jugador;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import audio.ControladorDeAudio;

public class Jugador {
    private float x, y;
    private float velocidadHorizontal = 200;
    private float velocidadVertical = 0;
    private float velocidadSalto = 400;
    private float gravedad = -800;
    private final float ALTO = 32f, ANCHO = 128f;
    private boolean enSuelo = true;
    
    private int vidas = 3;

    // === SISTEMA DE DISPARO ===
    private int municionActual = 30;
    private final int MUNICION_MAXIMA = 30;
    private boolean recargando = false;
    private float tiempoRecarga = 0f;
    private final float TIEMPO_RECARGA_TOTAL = 2.5f; // 2.5 segundos exactos
    private float tiempoUltimoDisparo = 0f;
    private final float CADENCIA_DISPARO = 0.2f; // 200ms entre disparos (5 disparos por segundo)
    private ControladorDeAudio controladorAudio;
    
    // === SISTEMA DE POWER-UP RECARGA RÁPIDA ===
    private boolean powerUpRecargaRapidaActivo = false;
    private float tiempoPowerUpRestante = 0f;
    private final float DURACION_POWER_UP = 60f; // 60 segundos
    private final float TIEMPO_RECARGA_POWER_UP = 0.5f; // 0.5 segundos con power-up

    private Texture caminando;
    private Texture quieto;
    private Texture saltando;
    private Texture disparoIzquierdaArriba;
    private Texture disparoDerechaArriba;
    private Texture agachado;
    private Texture imagenActual;
    
    public Jugador(float startX, float startY) {
    	this.x = startX;
        this.y = startY;

        this.caminando = new Texture("Jugador Imagenes/player_walk.png");
        this.quieto = new Texture("Jugador Imagenes/player.png");
        this.saltando = new Texture("Jugador Imagenes/player_up.png");

        // Nuevas texturas para disparos diagonales
        try {
        	this.disparoIzquierdaArriba = new Texture("Jugador Imagenes/player_shoot_left_up.png");
        } catch (Exception e) {
        	this.disparoIzquierdaArriba = new Texture("Jugador Imagenes/player_walk.png"); // Temporal
        }

        try {
        	this.disparoDerechaArriba = new Texture("Jugador Imagenes/player_shoot_right_up.png");
        } catch (Exception e) {
        	this.disparoDerechaArriba = new Texture("Jugador Imagenes/player_walk.png"); // Temporal
        }

        // Nueva textura para agacharse
        try {
        	this.agachado = new Texture("Jugador Imagenes/player_down.png");
        } catch (Exception e) {
        	this.agachado = new Texture("Jugador Imagenes/player.png"); // Temporal
        }

        this.imagenActual = this.quieto;
    }
    
    // === MÉTODOS PARA INTEGRAR EL AUDIO ===
    public void setControladorAudio(ControladorDeAudio controlador) {
        this.controladorAudio = controlador;
    }
    
    // === MÉTODOS DE DISPARO ===
    public boolean puedeDisparar(float tiempoActual) {
        return !recargando && municionActual > 0 && (tiempoActual - tiempoUltimoDisparo) >= CADENCIA_DISPARO;
    }
    
    public boolean disparar(float tiempoActual) {
        if (puedeDisparar(tiempoActual)) {
            municionActual--;
            tiempoUltimoDisparo = tiempoActual;
            
            // Reproducir sonido de disparo
            if (controladorAudio != null) {
                controladorAudio.reproducirSonido("disparo");
            }
            
            // Si se quedó sin munición, iniciar recarg.0a automática
            if (municionActual <= 0) {
                iniciarRecarga();
            }
            
            return true; // Disparo exitoso
        }
        return false; // No pudo disparar
    }
    
    public void iniciarRecarga() {
        if (!recargando && municionActual < MUNICION_MAXIMA) {
            recargando = true;
            tiempoRecarga = 0f;
            
            // Reproducir sonido de recarga si lo tienes
            if (controladorAudio != null) {
                controladorAudio.reproducirSonido("recarga");
            }
        }
    }
    
    private void actualizarRecarga(float delta) {
        if (recargando) {
            tiempoRecarga += delta;
            // Usar tiempo de recarga según el power-up
            float tiempoRecargaActual = powerUpRecargaRapidaActivo ? TIEMPO_RECARGA_POWER_UP : TIEMPO_RECARGA_TOTAL;
            
            if (tiempoRecarga >= tiempoRecargaActual) {
                // Recarga completada
                municionActual = MUNICION_MAXIMA;
                recargando = false;
                tiempoRecarga = 0f;
            }
        }
    }
    
    // === MÉTODOS PARA POWER-UP ===
    public void activarPowerUpRecargaRapida() {
        powerUpRecargaRapidaActivo = true;
        tiempoPowerUpRestante = DURACION_POWER_UP;
        
        // Si está recargando actualmente, acelerar la recarga
        if (recargando) {
            // Calcular progreso actual
            float progreso = tiempoRecarga / TIEMPO_RECARGA_TOTAL;
            // Aplicar el mismo progreso al tiempo de power-up
            tiempoRecarga = progreso * TIEMPO_RECARGA_POWER_UP;
        }
    }
    
    private void actualizarPowerUp(float delta) {
        if (powerUpRecargaRapidaActivo) {
            tiempoPowerUpRestante -= delta;
            if (tiempoPowerUpRestante <= 0) {
                powerUpRecargaRapidaActivo = false;
                tiempoPowerUpRestante = 0f;
            }
        }
    }
    
    public boolean tienePowerUpRecargaRapida() {
        return powerUpRecargaRapidaActivo;
    }
    
    public float getTiempoPowerUpRestante() {
        return tiempoPowerUpRestante;
    }
    
    // === GETTERS PARA EL HUD ===
    public int getMunicionActual() {
        return municionActual;
    }
    
    public int getMunicionMaxima() {
        return MUNICION_MAXIMA;
    }
    
    public boolean estaRecargando() {
        return recargando;
    }
    
    public float getProgresoRecarga() {
        if (!recargando) return 0f;
        float tiempoRecargaActual = powerUpRecargaRapidaActivo ? TIEMPO_RECARGA_POWER_UP : TIEMPO_RECARGA_TOTAL;
        return tiempoRecarga / tiempoRecargaActual;
    }
    
    public float getAlto()
    {
    	return this.ALTO;
    }
    
    public float getAncho()
    {
    	return this.ANCHO;
    }
    
    private boolean hayColisionIzquierda(TiledMapTileLayer capa) {
        float tileWidth = capa.getTileWidth();
        float tileHeight = capa.getTileHeight();

        int xTile = (int)((this.x - 1) /	 tileWidth);
        int yTileInicio = (int)(this.y / tileHeight + 1);
        int yTileFin = (int)((this.y + this.ALTO - 1) / tileHeight);

        for (int i = yTileInicio; i <= yTileFin; i++) {
            if (capa.getCell(xTile, i) != null) {
                return true;
            }
        }
        return false;
    }

    private boolean hayColisionDerecha(TiledMapTileLayer capa) {
        float tileWidth = capa.getTileWidth();
        float tileHeight = capa.getTileHeight();

        int xTile = (int)((this.x + this.ANCHO) / tileWidth);
        int yTileInicio = (int)(this.y / tileHeight + 1);
        int yTileFin = (int)((this.y + this.ALTO - 1) / tileHeight);

        for (int i = yTileInicio; i <= yTileFin; i++) {
            if (capa.getCell(xTile, i) != null) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hayColisionAbajo(TiledMapTileLayer capa) {
        float tileWidth = capa.getTileWidth();
        float tileHeight = capa.getTileHeight();

        int xTileInicio = (int)(this.x / tileWidth);
        int xTileFin = (int)((this.x + this.ANCHO) / tileWidth);
        int yTile = (int)((this.y - 1) / tileHeight);

        for (int i = xTileInicio; i <= xTileFin; i++) {
            if (capa.getCell(i, yTile) != null) {
                return true; // hay colisión con cualquier tile debajo
            }
        }

        return false;
    }

    public void actualizarMovimientoJugador(float dx, boolean saltar, boolean disparoIzqArriba, boolean disparoDerrArriba, boolean agacharse, boolean intentarDisparar, boolean recargarManual, float delta, TiledMapTileLayer capa, float tiempoTotal) {
        // === ACTUALIZAR SISTEMA DE DISPARO ===
        actualizarRecarga(delta);
        actualizarPowerUp(delta); // Actualizar power-up
        
        // Manejar disparo
        if (intentarDisparar) {
            disparar(tiempoTotal);
        }
        
        // Manejar recarga manual
        if (recargarManual) {
            iniciarRecarga();
        }
        
        
        if (saltar && this.enSuelo) {
        	this.velocidadVertical = this.velocidadSalto;
            this.enSuelo = false;
        }

        // Aplicar gravedad
        this.velocidadVertical += this.gravedad * delta;
        this.y += this.velocidadVertical * delta;
        
        if (dx < 0) { // Mover izquierda
            if (!hayColisionIzquierda(capa)) {
            	this.x += dx * this.velocidadHorizontal * delta;
            } else {
                int xTile = (int)((this.x - 1) / capa.getTileWidth());
                this.x = (xTile + 1) * capa.getTileWidth();
            }
        } else if (dx > 0) { // Mover derecha
            if (!hayColisionDerecha(capa)) {
            	this.x += dx * this.velocidadHorizontal * delta;
            } else {
                int xTile = (int)((this.x + this.ANCHO) / capa.getTileWidth());
                this.x = xTile * capa.getTileWidth() - this.ANCHO;
            }
        }
        
        if (hayColisionAbajo(capa)) {
            int yTile = (int)((y - 1) / capa.getTileHeight());
            this.y = (yTile + 1) * capa.getTileHeight(); // ajusta justo sobre el bloque
            this.velocidadVertical = 0;
            this.enSuelo = true;
        } else {
        	this.enSuelo = false;
        }

        // Actualizar imagen según el estado (PRIORIDAD: agacharse > disparos diagonales > salto > caminar > quieto)
        if (agacharse && this.enSuelo) {
        	this.imagenActual = this.agachado;
        } 
        else if (disparoIzqArriba) {
        	this.imagenActual = this.disparoIzquierdaArriba;
        } 
        else if (disparoDerrArriba) {
        	this.imagenActual = this.disparoDerechaArriba;
        } 
        else if (!enSuelo) {
        	this.imagenActual = this.saltando;
        } 
        else if (dx != 0) {
        	this.imagenActual = this.caminando;
        } 
        else {
        	this.imagenActual = this.quieto;
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(this.imagenActual, x, y, this.ANCHO, this.ALTO);
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }
    
    public int getVidas()
    {
    	return this.vidas;
    }
    
    public void quitarVida()
    {
    	if(vidas > 0) this.vidas -= 1;
    	//aca iria un "else" en el cual se llame a otra funcion en la que muera el jugador (en modo coop), o pierda el juego (en modo 1 jugador) 
    }

    public void dispose() {
    	this.caminando.dispose();
    	this.quieto.dispose();
    	this.saltando.dispose();
    	this.disparoIzquierdaArriba.dispose();
    	this.disparoDerechaArriba.dispose();
    	this.agachado.dispose();
    }
}