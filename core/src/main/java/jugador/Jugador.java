package jugador;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import packets.PlayerDataPacket; 
import audio.ControladorDeAudio;
import configuracion.ConfiguracionJuego;
import configuracion.EstadoJugador;
import palmeras.PuntoAgarre;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

public class Jugador {
	
	// ===== CLASE INTERNA PARA ARMAS =====
    public static class Arma {
        public String nombre;
        public int daño;
        public int municionActual;
        public int municionMaxima;
        public int municionReserva;
        public int municionReservaMaxima;
        public float tiempoRecarga;
        
        public Arma(String nombre, int daño, int municionMaxima, int municionReservaMaxima, float tiempoRecarga) {
            this.nombre = nombre;
            this.daño = daño;
            this.municionActual = municionMaxima;
            this.municionMaxima = municionMaxima;
            this.municionReserva = municionReservaMaxima;
            this.municionReservaMaxima = municionReservaMaxima;
            this.tiempoRecarga = tiempoRecarga;
        }
    }
    
	// FISICAS
    private float x, y;
    private float velocidadHorizontal = 200;
    private float velocidadVertical = 0;
    private float velocidadSalto = 400;
    private float gravedad = -800;
    private final float ALTO = 64f, ANCHO = 64f;
    private boolean enSuelo = true;
    private boolean agachado = false;
    
    // ===== SISTEMA DE PALMERAS =====
    private boolean agarradoPalmera = false;
    private PuntoAgarre palmeraActual = null;
    
    // ===== SISTEMA DE SALUD Y VIDAS =====
    private int vidas = 3;
    private int saludActual = 300;
    private final int SALUD_MAXIMA = 300;
    
    // ===== SISTEMA DE SALUD MENTAL =====
    private float saludMental = 100f; // 0-100%
    private float tiempoDesdeUltimaDegradacion = 0f;
    private final float INTERVALO_DEGRADACION = 20f; // 20 segundos
    private final float DEGRADACION_AUTOMATICA = 1f; // 1% cada 20 segundos
    private final float PENALIZACION_ATAQUE_CANIBAL = 5f; // 5%
    private final float PENALIZACION_MATAR_CUCHILLO = 10f; // 10%
    private final float PENALIZACION_SIN_BALAS = 20f; // 20%
    
    // ===== SISTEMA DE PUNTOS =====
    private int puntos = 0;
    
    // ===== SISTEMA DE MÚLTIPLES ARMAS =====
    private Map<Integer, Arma> armasDisponibles = new HashMap<>(); // Slot -> Arma
    private int slotArmaActual = 1; // 1, 2, 3, 4
    
    // ATRIBUTOS DE CUCHILLO
    private int dañoCuchillo = 25;

    // SISTEMA DE RECARGA
    private boolean recargando = false;
    private float tiempoRecarga = 0f;
    private float tiempoUltimoDisparo = 0f;
    private final float CADENCIA_DISPARO = 0.2f; // 200ms entre disparos
    
    // ===== SISTEMA DE CUCHILLO =====
    private boolean atacandoConCuchillo = false;
    private float tiempoUltimoAtaqueCuchillo = 0f;
    private final float CADENCIA_CUCHILLO = 0.8f;
    private final float RANGO_CUCHILLO = 70f;
    private boolean yaAplicoPenalizacionSinBalas = false;
    
    // AUDIO
    private ControladorDeAudio controladorAudio;
    
    // Configuracion
    private ConfiguracionJuego config;
    
    // ===== POWER-UPS =====
    private boolean powerUpRecargaRapidaActivo = false;
    private float tiempoPowerUpRecargaRestante = 0f;
    private final float DURACION_POWER_UP_RECARGA = 60f;

    private boolean powerUpInstaKillActivo = false;
    private float tiempoPowerUpInstaKillRestante = 0f;
    private final float DURACION_POWER_UP_INSTAKILL = 15f;

    // TEXTURAS Y ANIMACIONES
    private TextureRegion frameQuieto;
    private TextureRegion frameAgachado;
    private boolean mirandoDerecha = true;
    private boolean seEstaMoviendo;
    
    private Texture[] framesCaminando;
    private int frameIndex = 0;
    private float tiempoFrame = 0f;
    private float duracionFrame = 0.1f;
    
    // Ocultarse en arbustos
    private boolean ocultoEnArbusto = false;

    public Jugador(float startX, float startY, ConfiguracionJuego config) {
    	this.x = startX;
        this.y = startY;
        this.config = config;
        
        this.vidas = this.config.getVidasJugador();
        
        // ===== ⭐ GLOCK CORREGIDA: Daño 20, Cargador 12, Reserva 60 =====
        Arma glock = new Arma("Glock", 20, 12, 60, 2.5f);
        armasDisponibles.put(1, glock);
        slotArmaActual = 1;
        
        System.out.println("✅ Jugador iniciado con Glock (Slot 1)");
        System.out.println("   → Daño: 20");
        System.out.println("   → Munición: 12/60 (cargador/reserva)");
        
        this.framesCaminando = new Texture[9];
        for (int i = 0; i < 9; i++) {
            this.framesCaminando[i] = new Texture("Jugador Imagenes/Caminando " + (i + 1) + ".png");
        }

		this.frameQuieto = new TextureRegion(new Texture("Jugador Imagenes/Quieto.png"));
		this.frameAgachado = new TextureRegion(new Texture("Jugador Imagenes/Agachado.png"));
    }
    
    // === MÉTODOS PARA INTEGRAR EL AUDIO ===
    public void setControladorAudio(ControladorDeAudio controlador) {
        this.controladorAudio = controlador;
    }
    
    // ===== SISTEMA DE MÚLTIPLES ARMAS =====
    
    /**
     * Obtiene el arma actual equipada
     */
    public Arma getArmaActual() {
        return armasDisponibles.get(slotArmaActual);
    }
    
    /**
     * Cambia a un slot de arma específico
     */
    public void cambiarArma(int slot) {
        if (armasDisponibles.containsKey(slot) && slot != slotArmaActual) {
            slotArmaActual = slot;
            Arma arma = armasDisponibles.get(slot);
            System.out.println("🔄 Cambiado a: " + arma.nombre + " (Slot " + slot + ")");
            
            // Resetear estado de recarga
            recargando = false;
            tiempoRecarga = 0f;
        }
    }
    
    /**
     * Agrega una nueva arma al inventario
     */
    public void agregarArma(int slot, String nombre, int daño, int municionMaxima, int municionReservaMaxima, float tiempoRecarga) {
        if (slot >= 1 && slot <= 4) {
            Arma nuevaArma = new Arma(nombre, daño, municionMaxima, municionReservaMaxima, tiempoRecarga);
            armasDisponibles.put(slot, nuevaArma);
            System.out.println("✅ Arma agregada: " + nombre + " (Slot " + slot + ")");
        }
    }
    
    /**
     * Verifica si el jugador tiene un arma en un slot específico
     */
    public boolean tieneArmaEnSlot(int slot) {
        return armasDisponibles.containsKey(slot);
    }
    
    /**
     * Obtiene el siguiente slot disponible para comprar armas
     */
    public int getSiguienteSlotDisponible() {
        for (int i = 2; i <= 4; i++) {
            if (!armasDisponibles.containsKey(i)) {
                return i;
            }
        }
        return -1; // No hay slots disponibles
    }
    
    /**
     * ⭐ NUEVO: Agrega munición de reserva a la Glock cuando matas nazis
     */
    public void agregarMunicionGlock(int cantidad) {
        Arma glock = armasDisponibles.get(1); // La Glock siempre está en slot 1
        if (glock != null) {
            int reservaAnterior = glock.municionReserva;
            glock.municionReserva += cantidad;
            System.out.println("📦 MUNICIÓN AGREGADA: +" + cantidad + " balas | Antes: " + reservaAnterior + " | Ahora: " + glock.municionReserva);
        } else {
            System.err.println("❌ ERROR: No se encontró la Glock en el slot 1");
        }
    }
    
    // ===== SISTEMA DE PUNTOS =====
    
    public int getPuntos() {
        return this.puntos;
    }
    
    public void sumarPuntos(int cantidad) {
        this.puntos += cantidad;
        System.out.println("✅ Puntos ganados: +" + cantidad + " | Total: " + this.puntos);
    }
    
    public void restarPuntos(int cantidad) {
        this.puntos -= cantidad;
        if (this.puntos < 0) {
            this.puntos = 0;
        }
        System.out.println("💰 Puntos gastados: -" + cantidad + " | Total: " + this.puntos);
    }
    
    // ===== SISTEMA DE PALMERAS =====
    
    public boolean intentarAgarrarsePalmera(PuntoAgarre palmera) {
        if (palmera != null && !agarradoPalmera) {
            this.agarradoPalmera = true;
            this.palmeraActual = palmera;
            this.palmeraActual.ocupar();
            
            this.x = palmera.getPosicionAgarreX() - ANCHO / 2f;
            this.y = palmera.getPosicionAgarreY() - ALTO;
            
            this.velocidadVertical = 0;
            this.enSuelo = false;
            
            System.out.println("🌴 ¡Agarrado a la palmera!");
            return true;
        }
        return false;
    }
    
    public void soltarPalmera() {
        if (agarradoPalmera && palmeraActual != null) {
            palmeraActual.liberar();
            palmeraActual = null;
            agarradoPalmera = false;
            System.out.println("🌴 Palmera liberada");
        }
    }
    
    public boolean estaAgarradoPalmera() {
        return agarradoPalmera;
    }
    
    private void actualizarPalmera(float delta) {
        if (agarradoPalmera) {
            if (palmeraActual != null) {
                this.x = palmeraActual.getPosicionAgarreX() - ANCHO / 2f;
                this.y = palmeraActual.getPosicionAgarreY() - ALTO;
                this.velocidadVertical = 0;
            }
        }
    }
    
    // ===== SISTEMA DE SALUD Y VIDAS =====
    
    public void recibirDaño(int cantidad) {
        this.saludActual -= cantidad;
        
        if (this.saludActual <= 0) {
            this.saludActual = 0;
            perderVida();
        }
    }
    
    private void perderVida() {
        this.vidas--;
        
        if (this.vidas > 0) {
            this.saludActual = this.SALUD_MAXIMA;
            System.out.println("¡Perdiste una vida! Vidas restantes: " + this.vidas);
        } else {
            this.vidas = 0;
            System.out.println("GAME OVER");
        }
    }
    
    public void recuperarSalud(int cantidad) {
        this.saludActual = Math.min(this.SALUD_MAXIMA, this.saludActual + cantidad);
    }
    
    public void recuperarVida() {
        if (this.vidas < 3) {
            this.vidas++;
            this.saludActual = this.SALUD_MAXIMA;
        }
    }
    
    // ===== SISTEMA DE SALUD MENTAL =====
    
    private void actualizarSaludMental(float delta) {
        tiempoDesdeUltimaDegradacion += delta;
        
        if (tiempoDesdeUltimaDegradacion >= INTERVALO_DEGRADACION) {
            reducirSaludMental(DEGRADACION_AUTOMATICA);
            tiempoDesdeUltimaDegradacion = 0f;
        }
    }
    
    public void reducirSaludMental(float porcentaje) {
        this.saludMental -= porcentaje;
        
        if (this.saludMental <= 0) {
            this.saludMental = 0;
            System.out.println("¡Salud mental agotada! Perdiste una vida.");
            perderVida();
            this.saludMental = 100f;
        }
    }
    
    public void penalizarAtaqueCanibal() {
        reducirSaludMental(PENALIZACION_ATAQUE_CANIBAL);
        System.out.println("¡Ataque de caníbal! Salud mental: " + (int)saludMental + "%");
    }
    
    public void penalizarMatarCanibalConCuchillo() {
        reducirSaludMental(PENALIZACION_MATAR_CUCHILLO);
        System.out.println("¡Mataste con cuchillo! Salud mental: " + (int)saludMental + "%");
    }
    
    private void penalizarSinBalas() {
        if (!yaAplicoPenalizacionSinBalas) {
            reducirSaludMental(PENALIZACION_SIN_BALAS);
            yaAplicoPenalizacionSinBalas = true;
            System.out.println("¡Sin munición! Salud mental: " + (int)saludMental + "%");
        }
    }
    
    public void recuperarSaludMental(float porcentaje) {
        this.saludMental = Math.min(100f, this.saludMental + porcentaje);
    }
    
    // === MÉTODOS DE DISPARO ===
    
    public boolean puedeDisparar(float tiempoActual) {
        Arma arma = getArmaActual();
        if (arma == null) return false;
        
        return !this.recargando && arma.municionActual > 0 && (tiempoActual - this.tiempoUltimoDisparo) >= this.CADENCIA_DISPARO;
    }
    
    public boolean disparar(float tiempoActual) {
        Arma arma = getArmaActual();
        if (arma == null) return false;
        
        if (puedeDisparar(tiempoActual)) {
            arma.municionActual--;
            this.tiempoUltimoDisparo = tiempoActual;
            
            if (this.controladorAudio != null) {
                this.controladorAudio.reproducirSonido("disparo");
            }
            
            if (arma.municionActual <= 0) {
                iniciarRecarga();
            }
            
            return true;
        }
        return false;
    }
    
    public void iniciarRecarga() {
        Arma arma = getArmaActual();
        if (arma == null) return;
        
        if (!this.recargando && arma.municionActual < arma.municionMaxima && arma.municionReserva > 0) {
            this.recargando = true;
            this.tiempoRecarga = 0f;
            
            if (this.controladorAudio != null) {
                this.controladorAudio.reproducirSonido("recarga");
            }
        }
    }
    
    private void actualizarRecarga(float delta) {
        if (this.recargando) {
            Arma arma = getArmaActual();
            if (arma == null) {
                this.recargando = false;
                return;
            }
            
            this.tiempoRecarga += delta;
            float tiempoRecargaActual = this.powerUpRecargaRapidaActivo ? 0.5f : arma.tiempoRecarga;
            
            if (this.tiempoRecarga >= tiempoRecargaActual) {
                // ⭐ CÁLCULO CORREGIDO:
                // 1. Calcular cuántas balas faltan para llenar el cargador
                int municionNecesaria = arma.municionMaxima - arma.municionActual;
                
                // 2. Verificar si hay munición en reserva
                if (arma.municionReserva > 0) {
                    // 3. Tomar lo que se pueda de la reserva (lo que necesites O lo que tengas)
                    int municionARecargar = Math.min(municionNecesaria, arma.municionReserva);
                    
                    // 4. Agregar al cargador y restar de la reserva
                    arma.municionActual += municionARecargar;
                    arma.municionReserva -= municionARecargar;
                    
                    System.out.println("🔄 Recarga completada: " + arma.municionActual + "/" + arma.municionMaxima + " | Reserva: " + arma.municionReserva);
                } else {
                    System.out.println("❌ Sin munición en reserva");
                }
                
                this.recargando = false;
                this.tiempoRecarga = 0f;
                yaAplicoPenalizacionSinBalas = false;
            }
        }
    }
    
    // ===== SISTEMA DE CUCHILLO =====
    
    public boolean puedeAtacarConCuchillo(float tiempoActual) {
        return (tiempoActual - this.tiempoUltimoAtaqueCuchillo) >= this.CADENCIA_CUCHILLO;
    }
    
    public void atacarConCuchillo(float tiempoActual) {
        if (puedeAtacarConCuchillo(tiempoActual)) {
            this.atacandoConCuchillo = true;
            this.tiempoUltimoAtaqueCuchillo = tiempoActual;
            System.out.println("¡Ataque con cuchillo!");
        }
    }
    
    public Rectangle getRangoCuchillo() {
        float rangoX = mirandoDerecha ? x + ANCHO : x - RANGO_CUCHILLO;
        return new Rectangle(rangoX, y, RANGO_CUCHILLO, ALTO);
    }
    
    public void resetearAtaqueCuchillo() {
        this.atacandoConCuchillo = false;
    }
    
    // ===== POWER-UPS =====
    
    public void activarPowerUpRecargaRapida() {
        this.powerUpRecargaRapidaActivo = true;
        this.tiempoPowerUpRecargaRestante = this.DURACION_POWER_UP_RECARGA;
        
        if (this.recargando) {
            Arma arma = getArmaActual();
            if (arma != null) {
                float progreso = this.tiempoRecarga / arma.tiempoRecarga;
                this.tiempoRecarga = progreso * 0.5f;
            }
        }
        
        System.out.println("⚡ Power-Up Recarga Rápida activado por 60 segundos");
    }
    
    public void activarPowerUpInstaKill() {
        this.powerUpInstaKillActivo = true;
        this.tiempoPowerUpInstaKillRestante = this.DURACION_POWER_UP_INSTAKILL;
        
        System.out.println("💀 Power-Up INSTAKILL activado por 15 segundos");
    }
    
    private void actualizarPowerUp(float delta) {
        if (this.powerUpRecargaRapidaActivo) {
            this.tiempoPowerUpRecargaRestante -= delta;
            if (this.tiempoPowerUpRecargaRestante <= 0) {
                this.powerUpRecargaRapidaActivo = false;
                this.tiempoPowerUpRecargaRestante = 0f;
                System.out.println("⏱️ Power-Up Recarga Rápida terminado");
            }
        }
        
        if (this.powerUpInstaKillActivo) {
            this.tiempoPowerUpInstaKillRestante -= delta;
            if (this.tiempoPowerUpInstaKillRestante <= 0) {
                this.powerUpInstaKillActivo = false;
                this.tiempoPowerUpInstaKillRestante = 0f;
                System.out.println("⏱️ Power-Up INSTAKILL terminado");
            }
        }
    }
    
    public boolean tienePowerUpRecargaRapida() {
        return this.powerUpRecargaRapidaActivo;
    }
    
    public boolean tieneInstaKill() {
        return this.powerUpInstaKillActivo;
    }
    
    public float getTiempoInstaKillRestante() {
        return this.tiempoPowerUpInstaKillRestante;
    }
    
    public float getTiempoRecargaRapidaRestante() {
        return this.tiempoPowerUpRecargaRestante;
    }
    
    // === GETTERS PARA EL HUD ===
    public int getMunicionActual() {
        Arma arma = getArmaActual();
        return arma != null ? arma.municionActual : 0;
    }
    
    public int getMunicionMaxima() {
        Arma arma = getArmaActual();
        return arma != null ? arma.municionMaxima : 0;
    }
    
    public int getMunicionReserva() {
        Arma arma = getArmaActual();
        return arma != null ? arma.municionReserva : 0;
    }
    
    public String getNombreArmaActual() {
        Arma arma = getArmaActual();
        return arma != null ? arma.nombre : "Sin arma";
    }
    
    public boolean estaRecargando() {
        return this.recargando;
    }
    
    public float getProgresoRecarga() {
        if (!this.recargando) return 0f;
        Arma arma = getArmaActual();
        if (arma == null) return 0f;
        
        float tiempoRecargaActual = this.powerUpRecargaRapidaActivo ? 0.5f : arma.tiempoRecarga;
        return this.tiempoRecarga / tiempoRecargaActual;
    }
    
    public int getSaludActual() {
        return this.saludActual;
    }
    
    public int getSaludMaxima() {
        return this.SALUD_MAXIMA;
    }
    
    public float getSaludMental() {
        return this.saludMental;
    }
    
    public float getAlto() {
        return this.ALTO;
    }
    
    public float getAncho() {
        return this.ANCHO;
    }
    
    public int getVidas() {
        return this.vidas;
    }
    
    public int getDaño() {
        Arma arma = getArmaActual();
        return arma != null ? arma.daño : 0;
    }
    
    public int getDañoCuchillo() {
        return this.dañoCuchillo;
    }
    
    public boolean estaAtacandoConCuchillo() {
        return this.atacandoConCuchillo;
    }
    
    public boolean estaMuerto() {
        return this.vidas <= 0;
    }
    
    // === COLISIONES ===
    
    private boolean hayColisionIzquierda(TiledMapTileLayer capa) {
        float tileWidth = capa.getTileWidth();
        float tileHeight = capa.getTileHeight();
        float margen = 2f;

        int xTile = (int)((this.x - 1) / tileWidth);
        int yTileInicio = (int)((this.y + margen) / tileHeight);
        int yTileFin = (int)((this.y + this.ALTO - margen) / tileHeight);

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
        float margen = 2f;

        int xTile = (int)((this.x + this.ANCHO + 1) / tileWidth);
        int yTileInicio = (int)((this.y + margen) / tileHeight);
        int yTileFin = (int)((this.y + this.ALTO - margen) / tileHeight);

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
                return true;
            }
        }

        return false;
    }
    
    // ocultarse en arbustos
    public boolean estaOcultoEnArbusto() {
        return this.ocultoEnArbusto;
    }

    public void setOcultoEnArbusto(boolean oculto) {
        this.ocultoEnArbusto = oculto;
    }

    public void actualizarMovimientoJugador(float dx, boolean saltar, boolean agacharse, 
                                            boolean intentarDisparar, boolean recargarManual, 
                                            boolean atacarCuchillo, boolean agarrarse, 
                                            PuntoAgarre palmeraCercana, float delta, 
                                            TiledMapTileLayer capa, float tiempoTotal) {
        actualizarRecarga(delta);
        actualizarPowerUp(delta);
        actualizarSaludMental(delta);
        actualizarPalmera(delta);
        
        if (agarrarse && !agarradoPalmera && palmeraCercana != null) {
            intentarAgarrarsePalmera(palmeraCercana);
        } else if (agarradoPalmera && (saltar || dx != 0)) {
            soltarPalmera();
            if (saltar) {
                velocidadVertical = velocidadSalto * 0.8f;
            }
        }
        
        if (agarradoPalmera) {
            seEstaMoviendo = false;
            
            if (intentarDisparar) {
                disparar(tiempoTotal);
            }
            
            if (recargarManual) {
                iniciarRecarga();
            }
            
            return;
        }
        
        seEstaMoviendo = dx != 0;
        
        if (intentarDisparar) {
            disparar(tiempoTotal);
        }
        
        if (recargarManual) {
            iniciarRecarga();
        }
        
        if (atacarCuchillo) {
            atacarConCuchillo(tiempoTotal);
        } else {
            resetearAtaqueCuchillo();
        }
        
        if (agacharse && this.enSuelo) {
            this.agachado = true;
        } else {
            this.agachado = false;
        }
        
        if (saltar && this.enSuelo) {
            this.velocidadVertical = this.velocidadSalto;
            this.enSuelo = false;
            this.controladorAudio.reproducirSonido("salto");
        }

        this.velocidadVertical += this.gravedad * delta;
        this.y += this.velocidadVertical * delta;
        
        if (dx < 0) { 
            if (!hayColisionIzquierda(capa)) {
                this.x += dx * this.velocidadHorizontal * delta;
            } else {
                float tileWidth = capa.getTileWidth();
                int xTile = (int)((this.x - 1) / tileWidth);
                this.x = (xTile + 1) * tileWidth + 0.1f;
            }
        } else if (dx > 0) { 
            if (!hayColisionDerecha(capa)) {
                this.x += dx * this.velocidadHorizontal * delta;
            } else {
                float tileWidth = capa.getTileWidth();
                int xTile = (int)((this.x + this.ANCHO) / tileWidth);
                this.x = xTile * tileWidth - this.ANCHO - 0.1f;
            }
        }
        
        if (hayColisionAbajo(capa)) {
            int yTile = (int)((y - 1) / capa.getTileHeight());
            this.y = (yTile + 1) * capa.getTileHeight();
            this.velocidadVertical = 0;
            this.enSuelo = true;
        } else {
            this.enSuelo = false;
        }
        
        if (dx != 0) {
            seEstaMoviendo = true;
            tiempoFrame += delta;

            if (tiempoFrame >= duracionFrame) {
                frameIndex++;
                if (frameIndex >= framesCaminando.length) frameIndex = 0;
                tiempoFrame = 0f;
            }
        } else {
            seEstaMoviendo = false;
            frameIndex = 0;
        }
    }

    public void render(SpriteBatch batch) {
        Texture frameActual;

        if (enSuelo && agachado) {
            frameActual = frameAgachado.getTexture();
        } else if (seEstaMoviendo) {
            frameActual = framesCaminando[frameIndex];
        } else {
            frameActual = frameQuieto.getTexture();
        }

        if (mirandoDerecha) {
            batch.draw(frameActual, x, y, ANCHO, ALTO);
        } else {
            batch.draw(frameActual, x + ANCHO, y, -ANCHO, ALTO);
        }
    }
    
    public void apuntarHacia(float mouseX, float mouseY) {
        float centroX = this.x + this.ANCHO / 2f;
        float centroY = this.y + this.ALTO / 2f;
        float dx = mouseX - centroX;
        float dy = mouseY - centroY;
        this.mirandoDerecha = dx >= 0;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }
    
    public void dispose() {
        for (Texture t : framesCaminando) {
            t.dispose();
        }
        
        frameQuieto.getTexture().dispose();
        frameAgachado.getTexture().dispose();
    }
    
    public boolean getMirandoDerecha() {
        return mirandoDerecha;
    }

    public boolean estaEnSuelo() {
        return enSuelo;
    }

    public boolean estaAgachado() {
        return agachado;
    }

 // =====================================================
 // En Jugador.java, reemplaza el método crearDataPacket()
 // (está al final del archivo)
 // =====================================================

 public PlayerDataPacket crearDataPacket(int idJugador) {
     return new PlayerDataPacket(
         idJugador,
         this.x,
         this.y,
         this.vidas,
         this.saludActual,
         this.SALUD_MAXIMA,
         this.saludMental,
         this.getMunicionActual(),
         this.getMunicionMaxima(),
         this.getMunicionReserva(),
         this.getNombreArmaActual(),
         this.estaRecargando(),
         this.getProgresoRecarga(),
         this.getMirandoDerecha(),
         this.estaAgachado(),
         this.estaEnSuelo(),
         this.estaAgarradoPalmera(),
         this.getPuntos(), // ⭐⭐⭐ PUNTOS AGREGADOS ⭐⭐⭐
         this.tienePowerUpRecargaRapida(),
         this.getTiempoRecargaRapidaRestante(),
         this.tieneInstaKill(),
         this.getTiempoInstaKillRestante(),
         this.ocultoEnArbusto
     );
 }
//===== PERSISTENCIA DE ESTADO ENTRE NIVELES =====

/**
* Guarda el estado actual del jugador en un EstadoJugador
*/
public EstadoJugador guardarEstado() {
  EstadoJugador estado = new EstadoJugador();
  estado.setPuntos(this.puntos);
  estado.setSlotArmaActual(this.slotArmaActual);
  
  // Guardar todas las armas
  for (Map.Entry<Integer, Arma> entry : armasDisponibles.entrySet()) {
      Arma arma = entry.getValue();
      estado.agregarArma(
          entry.getKey(),
          arma.nombre,
          arma.daño,
          arma.municionActual,
          arma.municionMaxima,
          arma.municionReserva,
          arma.municionReservaMaxima,
          arma.tiempoRecarga
      );
  }
  
  System.out.println("💾 Estado guardado: " + estado);
  return estado;
}

/**
* Restaura el estado del jugador desde un EstadoJugador
*/
public void cargarEstado(EstadoJugador estado) {
  if (estado == null || !estado.tieneArmas()) {
      System.out.println("⚠️ No hay estado previo, usando configuración por defecto");
      return;
  }
  
  System.out.println("📂 Cargando estado: " + estado);
  
  // Restaurar puntos
  this.puntos = estado.getPuntos();
  
  // Restaurar armas
  this.armasDisponibles.clear();
  for (Map.Entry<Integer, EstadoJugador.ArmaGuardada> entry : estado.getArmas().entrySet()) {
      EstadoJugador.ArmaGuardada armaGuardada = entry.getValue();
      Arma arma = new Arma(
          armaGuardada.nombre,
          armaGuardada.daño,
          armaGuardada.municionMaxima,
          armaGuardada.municionReservaMaxima,
          armaGuardada.tiempoRecarga
      );
      
      // Restaurar munición exacta
      arma.municionActual = armaGuardada.municionActual;
      arma.municionReserva = armaGuardada.municionReserva;
      
      this.armasDisponibles.put(entry.getKey(), arma);
      
      System.out.println("  ✅ Arma restaurada: " + arma.nombre + " (Slot " + entry.getKey() + 
                       ") - " + arma.municionActual + "/" + arma.municionReserva);
  }
  
  // Restaurar slot actual
  this.slotArmaActual = estado.getSlotArmaActual();
  
  System.out.println("✅ Estado cargado: " + this.puntos + " puntos, " + 
                   this.armasDisponibles.size() + " armas");
} 
 
}