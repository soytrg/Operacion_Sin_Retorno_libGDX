package jugador;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import audio.ControladorDeAudio;
import configuracion.ConfiguracionJuego;
import configuracion.EstadoJugador;
import packets.PlayerDataPacket;
import palmeras.PuntoAgarre;
import java.util.HashMap;
import java.util.Map;

/**
 * ⭐ JUGADOR REMOTO CONTROLABLE (CLIENTE) - ANIMACIONES 100% FUNCIONALES
 * 
 * CORRECCIÓN CRÍTICA:
 * - NO sincronizar targetX/targetY durante movimiento local
 * - Dejar que JugadorRemoto.actualizar() detecte el movimiento automáticamente
 * - Solo forzar estado cuando NO hay movimiento (palmera o quieto)
 */
public class JugadorRemotoControlable extends JugadorRemoto {
    
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
    private float velocidadHorizontal = 200;
    private float velocidadVertical = 0;
    private float velocidadSalto = 400;
    private float gravedad = -800;
    private boolean enSuelo = true;
    private boolean agachado = false;
    
    // ===== SISTEMA DE PALMERAS =====
    private boolean agarradoPalmera = false;
    private PuntoAgarre palmeraActual = null;
    
    // ===== SISTEMA DE MÚLTIPLES ARMAS =====
    protected Map<Integer, Arma> armasDisponibles = new HashMap<>();
    protected int slotArmaActual = 1;
    
    // ATRIBUTOS DE CUCHILLO
    private int dañoCuchillo = 25;

    // SISTEMA DE RECARGA
    private boolean recargando = false;
    private float tiempoRecarga = 0f;
    private float tiempoUltimoDisparo = 0f;
    private final float CADENCIA_DISPARO = 0.2f;
    
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
    
    // ===== SISTEMA DE SALUD MENTAL =====
    private float tiempoDesdeUltimaDegradacion = 0f;
    private final float INTERVALO_DEGRADACION = 20f;
    private final float DEGRADACION_AUTOMATICA = 1f;
    private final float PENALIZACION_ATAQUE_CANIBAL = 5f;
    private final float PENALIZACION_MATAR_CUCHILLO = 10f;
    private final float PENALIZACION_SIN_BALAS = 20f;
    
    // 🔥🔥🔥 PROTECCIÓN CONTRA DAÑO DUPLICADO 🔥🔥🔥
    private float tiempoUltimoDaño = 0f;
    private final float COOLDOWN_DAÑO = 0.5f;
    private int ultimoDañoRecibido = 0;
    private boolean procesandoPerdidaVida = false;

    public JugadorRemotoControlable(float startX, float startY, ConfiguracionJuego config) {
        super();
        this.x = startX;
        this.y = startY;
        this.config = config;
        
        // 🔥 INICIALIZAR targetX/targetY Y primerPacketRecibido
        this.targetX = startX;
        this.targetY = startY;
        this.primerPacketRecibido = true;
        
        this.vidas = this.config.getVidasJugador();
        this.saludActual = 300;
        this.saludMaxima = 300;
        this.saludMental = 100f;
        
        // ⭐ Glock inicial
        Arma glock = new Arma("Glock", 20, 12, 60, 2.5f);
        armasDisponibles.put(1, glock);
        slotArmaActual = 1;
        
        System.out.println("✅ Jugador remoto controlable iniciado - Vidas: " + this.vidas + ", Salud: " + this.saludActual);
    }
    
    // ===== SISTEMA DE MÚLTIPLES ARMAS =====
    
    public Arma getArmaActual() {
        return armasDisponibles.get(slotArmaActual);
    }
    
    public void cambiarArma(int slot) {
        if (armasDisponibles.containsKey(slot) && slot != slotArmaActual) {
            slotArmaActual = slot;
            Arma arma = armasDisponibles.get(slot);
            System.out.println("🔄 Jugador remoto cambió a: " + arma.nombre + " (Slot " + slot + ")");
            
            recargando = false;
            tiempoRecarga = 0f;
        }
    }
    
    public void agregarArma(int slot, String nombre, int daño, int municionMaxima, int municionReservaMaxima, float tiempoRecarga) {
        if (slot >= 1 && slot <= 4) {
            Arma nuevaArma = new Arma(nombre, daño, municionMaxima, municionReservaMaxima, tiempoRecarga);
            armasDisponibles.put(slot, nuevaArma);
            System.out.println("✅ Arma agregada al jugador remoto: " + nombre + " (Slot " + slot + ")");
        }
    }
    
    public boolean tieneArmaEnSlot(int slot) {
        return armasDisponibles.containsKey(slot);
    }
    
    public int getSiguienteSlotDisponible() {
        for (int i = 2; i <= 4; i++) {
            if (!armasDisponibles.containsKey(i)) {
                return i;
            }
        }
        return -1;
    }
    
    public void agregarMunicionGlock(int cantidad) {
        Arma glock = armasDisponibles.get(1);
        if (glock != null) {
            int reservaAnterior = glock.municionReserva;
            glock.municionReserva += cantidad;
            System.out.println("📦 MUNICIÓN AGREGADA (Remoto): +" + cantidad + " balas | Antes: " + reservaAnterior + " | Ahora: " + glock.municionReserva);
        }
    }
    
    // ===== SISTEMA DE PUNTOS =====
    
    public void sumarPuntos(int cantidad) {
        this.puntos += cantidad;
        System.out.println("✅ Puntos ganados (Remoto): +" + cantidad + " | Total: " + this.puntos);
    }
    
    public void restarPuntos(int cantidad) {
        this.puntos -= cantidad;
        if (this.puntos < 0) {
            this.puntos = 0;
        }
        System.out.println("💰 Puntos gastados (Remoto): -" + cantidad + " | Total: " + this.puntos);
    }
    
    // ===== SISTEMA DE PALMERAS =====
    
    public boolean intentarAgarrarsePalmera(PuntoAgarre palmera) {
        if (palmera != null && !agarradoPalmera) {
            this.agarradoPalmera = true;
            this.palmeraActual = palmera;
            this.palmeraActual.ocupar();
            
            this.x = palmera.getPosicionAgarreX() - ANCHO / 2f;
            this.y = palmera.getPosicionAgarreY() - ALTO;
            
            // 🔥 SINCRONIZAR targetX/targetY SOLO EN PALMERA
            this.targetX = this.x;
            this.targetY = this.y;
            
            this.velocidadVertical = 0;
            this.enSuelo = false;
            
            System.out.println("🌴 Jugador remoto agarrado a palmera");
            return true;
        }
        return false;
    }
    
    public void soltarPalmera() {
        if (agarradoPalmera && palmeraActual != null) {
            palmeraActual.liberar();
            palmeraActual = null;
            agarradoPalmera = false;
            System.out.println("🌴 Palmera liberada (Remoto)");
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
                
                // 🔥 SINCRONIZAR targetX/targetY SOLO EN PALMERA
                this.targetX = this.x;
                this.targetY = this.y;
                
                this.velocidadVertical = 0;
            }
        }
    }
    
    // 🔥🔥🔥 SISTEMA DE SALUD Y VIDAS - COMPLETAMENTE CORREGIDO 🔥🔥🔥
    
    public void recibirDaño(int cantidad) {
        if (this.vidas <= 0) {
            System.out.println("⚠️ Jugador remoto ya está muerto, ignorando daño");
            return;
        }
        
        float tiempoActual = com.badlogic.gdx.utils.TimeUtils.nanoTime() / 1_000_000_000f;
        if (tiempoActual - tiempoUltimoDaño < COOLDOWN_DAÑO && ultimoDañoRecibido == cantidad) {
            System.out.println("⚠️ Daño duplicado bloqueado (Remoto): " + cantidad);
            return;
        }
        
        tiempoUltimoDaño = tiempoActual;
        ultimoDañoRecibido = cantidad;
        
        int saludAnterior = this.saludActual;
        this.saludActual -= cantidad;
        
        System.out.println("💔 JUGADOR REMOTO recibe " + cantidad + " de daño | Salud: " + saludAnterior + " → " + this.saludActual + " | Vidas: " + this.vidas);
        
        if (this.saludActual <= 0 && !procesandoPerdidaVida) {
            procesandoPerdidaVida = true;
            this.saludActual = 0;
            perderVida();
            procesandoPerdidaVida = false;
        }
    }
    
    private void perderVida() {
        if (this.vidas <= 0) {
            System.out.println("⚠️ Sin vidas restantes, ignorando pérdida de vida");
            return;
        }
        
        this.vidas--;
        System.out.println("💀 JUGADOR REMOTO perdió 1 vida | Vidas restantes: " + this.vidas);
        
        if (this.vidas > 0) {
            this.saludActual = this.saludMaxima;
            System.out.println("✅ Salud restaurada a " + this.saludMaxima);
        } else {
            this.vidas = 0;
            this.saludActual = 0;
            System.out.println("☠️ GAME OVER (Jugador Remoto)");
        }
    }
    
    public void recuperarSalud(int cantidad) {
        int saludAnterior = this.saludActual;
        this.saludActual = Math.min(this.saludMaxima, this.saludActual + cantidad);
        System.out.println("💚 Salud recuperada (Remoto): " + saludAnterior + " → " + this.saludActual);
    }
    
    public void recuperarVida() {
        if (this.vidas < 3) {
            this.vidas++;
            this.saludActual = this.saludMaxima;
            System.out.println("❤️ Vida extra ganada (Remoto) | Vidas: " + this.vidas);
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
        float saludMentalAnterior = this.saludMental;
        this.saludMental -= porcentaje;
        
        System.out.println("🧠 Salud mental (Remoto): " + (int)saludMentalAnterior + "% → " + (int)this.saludMental + "%");
        
        if (this.saludMental <= 0 && !procesandoPerdidaVida) {
            this.saludMental = 0;
            System.out.println("😵 Salud mental agotada (Remoto) - Perdiendo vida");
            procesandoPerdidaVida = true;
            perderVida();
            this.saludMental = 100f;
            procesandoPerdidaVida = false;
        }
    }
    
    public void penalizarAtaqueCanibal() {
        reducirSaludMental(PENALIZACION_ATAQUE_CANIBAL);
    }
    
    public void penalizarMatarCanibalConCuchillo() {
        reducirSaludMental(PENALIZACION_MATAR_CUCHILLO);
    }
    
    private void penalizarSinBalas() {
        if (!yaAplicoPenalizacionSinBalas) {
            reducirSaludMental(PENALIZACION_SIN_BALAS);
            yaAplicoPenalizacionSinBalas = true;
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
                int municionNecesaria = arma.municionMaxima - arma.municionActual;
                
                if (arma.municionReserva > 0) {
                    int municionARecargar = Math.min(municionNecesaria, arma.municionReserva);
                    
                    arma.municionActual += municionARecargar;
                    arma.municionReserva -= municionARecargar;
                    
                    System.out.println("🔄 Recarga completada (Remoto): " + arma.municionActual + "/" + arma.municionMaxima + " | Reserva: " + arma.municionReserva);
                } else {
                    System.out.println("❌ Sin munición en reserva (Remoto)");
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
            System.out.println("🔪 Ataque con cuchillo (Remoto)");
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
        
        System.out.println("⚡ Power-Up Recarga Rápida activado (Remoto)");
    }
    
    public void activarPowerUpInstaKill() {
        this.powerUpInstaKillActivo = true;
        this.tiempoPowerUpInstaKillRestante = this.DURACION_POWER_UP_INSTAKILL;
        
        System.out.println("💀 Power-Up INSTAKILL activado (Remoto)");
    }
    
    private void actualizarPowerUp(float delta) {
        if (this.powerUpRecargaRapidaActivo) {
            this.tiempoPowerUpRecargaRestante -= delta;
            if (this.tiempoPowerUpRecargaRestante <= 0) {
                this.powerUpRecargaRapidaActivo = false;
                this.tiempoPowerUpRecargaRestante = 0f;
            }
        }
        
        if (this.powerUpInstaKillActivo) {
            this.tiempoPowerUpInstaKillRestante -= delta;
            if (this.tiempoPowerUpInstaKillRestante <= 0) {
                this.powerUpInstaKillActivo = false;
                this.tiempoPowerUpInstaKillRestante = 0f;
            }
        }
    }
    
    // === GETTERS ===
    
    @Override
    public int getMunicionActual() {
        Arma arma = getArmaActual();
        return arma != null ? arma.municionActual : 0;
    }
    
    @Override
    public int getMunicionMaxima() {
        Arma arma = getArmaActual();
        return arma != null ? arma.municionMaxima : 0;
    }
    
    @Override
    public int getMunicionReserva() {
        Arma arma = getArmaActual();
        return arma != null ? arma.municionReserva : 0;
    }
    
    @Override
    public String getNombreArma() {
        Arma arma = getArmaActual();
        return arma != null ? arma.nombre : "Sin arma";
    }
    
    @Override
    public boolean estaRecargando() {
        return this.recargando;
    }
    
    @Override
    public float getProgresoRecarga() {
        if (!this.recargando) return 0f;
        Arma arma = getArmaActual();
        if (arma == null) return 0f;
        
        float tiempoRecargaActual = this.powerUpRecargaRapidaActivo ? 0.5f : arma.tiempoRecarga;
        return this.tiempoRecarga / tiempoRecargaActual;
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
    
    @Override
    public boolean estaEnSuelo() {
        return enSuelo;
    }

    @Override
    public boolean estaAgachado() {
        return agachado;
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
    
    public void setOcultoEnArbusto(boolean oculto) {
        this.ocultoEnArbusto = oculto;
    }
    
    public void apuntarHacia(float mouseX, float mouseY) {
        float centroX = this.x + this.ANCHO / 2f;
        float centroY = this.y + this.ALTO / 2f;
        float dx = mouseX - centroX;
        float dy = mouseY - centroY;
        this.mirandoDerecha = dx >= 0;
    }

    // ===== PROCESAMIENTO DE INPUT (LLAMADO POR EL HOST) =====

//    public void procesarInput(float dx, boolean saltar, boolean agacharse, 
//            boolean intentarDisparar, boolean recargarManual, 
//            boolean atacarCuchillo, boolean agarrarse, 
//            PuntoAgarre palmeraCercana, float delta, 
//            TiledMapTileLayer capa, float tiempoTotal,
//            ControladorDeAudio controladorAudio) {
//		this.controladorAudio = controladorAudio;
//		
//		
//		actualizarRecarga(delta);
//		actualizarPowerUp(delta);
//		actualizarSaludMental(delta);
//		actualizarPalmera(delta);
//		
//		// ===== MANEJO DE PALMERAS =====
//		if (agarrarse && !agarradoPalmera && palmeraCercana != null) {
//			intentarAgarrarsePalmera(palmeraCercana);
//		} else if (agarradoPalmera && (saltar || dx != 0)) {
//			soltarPalmera();
//		if (saltar) {
//			velocidadVertical = velocidadSalto * 0.8f;
//			}
//		}
//		
//		// Si está en palmera, solo procesar disparo y recarga
//		if (agarradoPalmera) {
//			if (intentarDisparar) {
//				disparar(tiempoTotal);
//			}
//		
//			if (recargarManual) {
//				iniciarRecarga();
//			}	
//		
//			// 🔥 ACTUALIZAR ANIMACIONES (sin movimiento)
//			super.actualizar(delta);
//			return;
//		}
//		
//		// ===== PROCESAMIENTO DE ACCIONES =====
//		if (intentarDisparar) {
//			disparar(tiempoTotal);
//		}
//		
//		if (recargarManual) {
//			iniciarRecarga();
//		}
//		
//		if (atacarCuchillo) {
//			atacarConCuchillo(tiempoTotal);
//		} else {
//			resetearAtaqueCuchillo();
//		}
//		
//		// ===== AGACHARSE =====
//		if (agacharse && this.enSuelo) {
//			this.agachado = true;
//		} else {
//			this.agachado = false;
//		}
//		
//		// ===== SALTO =====
//		if (saltar && this.enSuelo) {
//			this.velocidadVertical = this.velocidadSalto;
//			this.enSuelo = false;
//		}
//		
//		// ===== GRAVEDAD =====
//		this.velocidadVertical += this.gravedad * delta;
//		this.y += this.velocidadVertical * delta;
//		
//		// ===== MOVIMIENTO HORIZONTAL =====
//		if (dx < 0) { 
//		
//			if (!hayColisionIzquierda(capa)) {
//				this.x += dx * this.velocidadHorizontal * delta;
//			} else {
//				float tileWidth = capa.getTileWidth();
//				int xTile = (int)((this.x - 1) / tileWidth);
//				this.x = (xTile + 1) * tileWidth + 0.1f;
//			}
//		} else if (dx > 0) { 
//			if (!hayColisionDerecha(capa)) {
//				this.x += dx * this.velocidadHorizontal * delta;
//			} else {
//				float tileWidth = capa.getTileWidth();
//				int xTile = (int)((this.x + this.ANCHO) / tileWidth);
//				this.x = xTile * tileWidth - this.ANCHO - 0.1f;
//			}
//		}
//		
//		// ===== COLISIÓN CON SUELO =====
//		if (hayColisionAbajo(capa)) {
//			int yTile = (int)((y - 1) / capa.getTileHeight());
//			this.y = (yTile + 1) * capa.getTileHeight();
//			this.velocidadVertical = 0;
//			this.enSuelo = true;
//		} else {
//			this.enSuelo = false;
//		}
//		
//		// 🔥🔥🔥 CRÍTICO: SINCRONIZAR targetX/targetY 🔥🔥🔥
//		// Esto permite que JugadorRemoto.actualizar() detecte el movimiento
//		this.targetX = this.x;
//		this.targetY = this.y;
//		
//		System.out.println("📍 ANTES de super.actualizar() - x:" + this.x + " targetX:" + this.targetX + 
//                " | dx input:" + dx + " | agachado:" + this.agachado + " | enSuelo:" + this.enSuelo);
//		
//		// 🔥 ACTUALIZAR ANIMACIONES - La clase padre detectará movimiento automáticamente
//		super.actualizar(delta);
//}
    
 // ===== PROCESAMIENTO DE INPUT (LLAMADO POR EL HOST) =====

    public void procesarInput(float dx, boolean saltar, boolean agacharse, 
                              boolean intentarDisparar, boolean recargarManual, 
                              boolean atacarCuchillo, boolean agarrarse, 
                              PuntoAgarre palmeraCercana, float delta, 
                              TiledMapTileLayer capa, float tiempoTotal,
                              ControladorDeAudio controladorAudio) {
        this.controladorAudio = controladorAudio;
        
        actualizarRecarga(delta);
        actualizarPowerUp(delta);
        actualizarSaludMental(delta);
        actualizarPalmera(delta);
        
        // ===== MANEJO DE PALMERAS =====
        if (agarrarse && !agarradoPalmera && palmeraCercana != null) {
            intentarAgarrarsePalmera(palmeraCercana);
        } else if (agarradoPalmera && (saltar || dx != 0)) {
            soltarPalmera();
            if (saltar) {
                velocidadVertical = velocidadSalto * 0.8f;
            }
        }
        
        // Si está en palmera, solo procesar disparo y recarga
        if (agarradoPalmera) {
            if (intentarDisparar) {
                disparar(tiempoTotal);
            }
            
            if (recargarManual) {
                iniciarRecarga();
            }
            
            // 🔥 NO LLAMAR super.actualizar() aquí - se llama desde PantallaJuego
            return;
        }
        
        // ===== PROCESAMIENTO DE ACCIONES =====
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
        
        // ===== AGACHARSE =====
        if (agacharse && this.enSuelo) {
            this.agachado = true;
        } else {
            this.agachado = false;
        }
        
        // ===== SALTO =====
        if (saltar && this.enSuelo) {
            this.velocidadVertical = this.velocidadSalto;
            this.enSuelo = false;
        }

        // ===== GRAVEDAD =====
        this.velocidadVertical += this.gravedad * delta;
        this.y += this.velocidadVertical * delta;
        
        // ===== MOVIMIENTO HORIZONTAL =====
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
        
        // ===== COLISIÓN CON SUELO =====
        if (hayColisionAbajo(capa)) {
            int yTile = (int)((y - 1) / capa.getTileHeight());
            this.y = (yTile + 1) * capa.getTileHeight();
            this.velocidadVertical = 0;
            this.enSuelo = true;
        } else {
            this.enSuelo = false;
        }
        
        // 🔥🔥🔥 CRÍTICO: SINCRONIZAR targetX/targetY 🔥🔥🔥
        // Esto permite que JugadorRemoto.actualizar() detecte el movimiento
        this.targetX = this.x;
        this.targetY = this.y;
        
        // 🔥 NO LLAMAR super.actualizar() AQUÍ - se llama desde PantallaJuego.render()
        // El problema era que se llamaba dos veces por frame, perdiendo la detección de movimiento
    }
    
    // ===== PERSISTENCIA DE ESTADO ENTRE NIVELES =====
    
    public EstadoJugador guardarEstado() {
        EstadoJugador estado = new EstadoJugador();
        estado.setPuntos(this.puntos);
        estado.setSlotArmaActual(this.slotArmaActual);
        
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
        
        System.out.println("💾 Estado remoto guardado: " + estado);
        return estado;
    }
    
    public void cargarEstado(EstadoJugador estado) {
        if (estado == null || !estado.tieneArmas()) {
            System.out.println("⚠️ No hay estado previo para jugador remoto");
            return;
        }
        
        System.out.println("📂 Cargando estado remoto: " + estado);
        
        this.puntos = estado.getPuntos();
        
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
            
            arma.municionActual = armaGuardada.municionActual;
            arma.municionReserva = armaGuardada.municionReserva;
            
            this.armasDisponibles.put(entry.getKey(), arma);
            
            System.out.println("  ✅ Arma restaurada (Remoto): " + arma.nombre + " (Slot " + entry.getKey() + 
                             ") - " + arma.municionActual + "/" + arma.municionReserva);
        }
        
        this.slotArmaActual = estado.getSlotArmaActual();
        
        System.out.println("✅ Estado remoto cargado: " + this.puntos + " puntos, " + 
                         this.armasDisponibles.size() + " armas");
    }
    
    // ===== CREAR DATA PACKET PARA RED =====
    
    public PlayerDataPacket crearDataPacket(int idJugador) {
        return new PlayerDataPacket(
            idJugador,
            this.x,
            this.y,
            this.vidas,
            this.saludActual,
            this.saludMaxima,
            this.saludMental,
            this.getMunicionActual(),
            this.getMunicionMaxima(),
            this.getMunicionReserva(),
            this.getNombreArma(),
            this.estaRecargando(),
            this.getProgresoRecarga(),
            this.mirandoDerecha,
            this.estaAgachado(),
            this.estaEnSuelo(),
            this.estaAgarradoPalmera(),
            this.puntos,
            this.powerUpRecargaRapidaActivo,
            this.tiempoPowerUpRecargaRestante,
            this.powerUpInstaKillActivo,
            this.tiempoPowerUpInstaKillRestante,
            this.ocultoEnArbusto
        );
    }
}
        