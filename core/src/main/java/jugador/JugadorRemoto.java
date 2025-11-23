package jugador;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import packets.PlayerDataPacket;

/**
 * ⭐ JUGADOR REMOTO - ANIMACIONES 100% FUNCIONALES ⭐
 * 
 * CORRECCIÓN CRÍTICA:
 * - Detección de movimiento basada en cambio real de posición
 * - Compara posición actual con anterior frame
 */
public class JugadorRemoto {
    
    protected float x, y;
    protected final float ALTO = 64f, ANCHO = 64f;
    
    protected int vidas;
    protected int saludActual;
    protected int saludMaxima;
    protected float saludMental;
    protected int municionActual;
    protected int municionMaxima;
    protected int municionReserva;
    protected String nombreArma;
    protected boolean recargando;
    protected float progresoRecarga;
    
    protected int puntos = 0;
    
    protected boolean mirandoDerecha;
    protected boolean agachado;
    protected boolean enSuelo;
    protected boolean agarradoPalmera;
    
    protected boolean powerUpRecargaActivo;
    protected float tiempoRecargaRestante;
    protected boolean powerUpInstaKillActivo;
    protected float tiempoInstaKillRestante;
    
    protected boolean ocultoEnArbusto = false;
    
    private Texture[] framesCaminando;
    private TextureRegion frameQuieto;
    private TextureRegion frameAgachado;
    
    // 🔥 ANIMACIÓN MEJORADA
    protected int frameIndex = 0;
    protected float tiempoFrame = 0f;
    private final float DURACION_FRAME = 0.1f;
    
    // 🔥 INTERPOLACIÓN CON TELEPORT
    protected float targetX, targetY;
    private final float VELOCIDAD_INTERPOLACION = 15f;
    private final float DISTANCIA_TELEPORT = 200f;
    protected boolean primerPacketRecibido = false;
    
    // 🔥🔥🔥 DETECCIÓN DE MOVIMIENTO CORREGIDA 🔥🔥🔥
    private float posicionAnteriorX = 0f;
    private float posicionAnteriorY = 0f;
    protected boolean seEstaMoviendo = false;
    private final float UMBRAL_MOVIMIENTO = 0.5f; // Umbral en píxeles
    
    public JugadorRemoto() {
        cargarTexturas();
        this.x = -999999;
        this.y = -999999;
        this.targetX = -999999;
        this.targetY = -999999;
        this.posicionAnteriorX = this.x;
        this.posicionAnteriorY = this.y;
    }
    
    private void cargarTexturas() {
        this.framesCaminando = new Texture[9];
        for (int i = 0; i < 9; i++) {
            this.framesCaminando[i] = new Texture("Jugador Imagenes/Caminando " + (i + 1) + ".png");
        }
        
        this.frameQuieto = new TextureRegion(new Texture("Jugador Imagenes/Quieto.png"));
        this.frameAgachado = new TextureRegion(new Texture("Jugador Imagenes/Agachado.png"));
    }
    
    public void actualizarDesdePacket(PlayerDataPacket packet) {
        if (packet == null) return;
        
        if (!primerPacketRecibido) {
            this.x = packet.getX();
            this.y = packet.getY();
            this.targetX = packet.getX();
            this.targetY = packet.getY();
            this.posicionAnteriorX = this.x;
            this.posicionAnteriorY = this.y;
            primerPacketRecibido = true;
            System.out.println("🌐 Jugador remoto inicializado en (" + x + ", " + y + ")");
        } else {
            this.targetX = packet.getX();
            this.targetY = packet.getY();
            
            float distancia = (float) Math.sqrt(
                Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2)
            );
            
            if (distancia > DISTANCIA_TELEPORT) {
                System.out.println("⚡ Teleport remoto: distancia=" + (int)distancia);
                this.x = this.targetX;
                this.y = this.targetY;
                this.posicionAnteriorX = this.x;
                this.posicionAnteriorY = this.y;
            }
        }
        
        // Sincronizar todos los estados
        this.vidas = packet.getVidas();
        this.saludActual = packet.getSaludActual();
        this.saludMaxima = packet.getSaludMaxima();
        this.saludMental = packet.getSaludMental();
        this.municionActual = packet.getMunicionActual();
        this.municionMaxima = packet.getMunicionMaxima();
        this.municionReserva = packet.getMunicionReserva();
        this.nombreArma = packet.getNombreArma();
        this.recargando = packet.isRecargando();
        this.progresoRecarga = packet.getProgresoRecarga();
        this.mirandoDerecha = packet.isMirandoDerecha();
        this.agachado = packet.isAgachado();
        this.enSuelo = packet.isEnSuelo();
        this.agarradoPalmera = packet.isAgarradoPalmera();
        this.puntos = packet.getPuntos();
        this.powerUpRecargaActivo = packet.isPowerUpRecargaActivo();
        this.tiempoRecargaRestante = packet.getTiempoRecargaRestante();
        this.powerUpInstaKillActivo = packet.isPowerUpInstaKillActivo();
        this.tiempoInstaKillRestante = packet.getTiempoInstaKillRestante();
        this.ocultoEnArbusto = packet.isOcultoEnArbusto();
    }
    
    /**
     * 🔥🔥🔥 ACTUALIZACIÓN CON DETECCIÓN DE MOVIMIENTO CORREGIDA 🔥🔥🔥
     */
    public void actualizar(float delta) {
        if (!primerPacketRecibido) {
            return;
        }
        
        // 🔥 DETECTAR MOVIMIENTO: Comparar posición actual con la del frame anterior
        float distanciaMovidaX = Math.abs(x - posicionAnteriorX);
        float distanciaMovidaY = Math.abs(y - posicionAnteriorY);
        
        // También verificar si hay diferencia con el target (indica movimiento futuro)
        float distanciaTargetX = Math.abs(targetX - x);
        
        // 🔥🔥🔥 MEJORADO: El jugador se está moviendo si:
        // 1. Se movió desde el frame anterior (distanciaMovidaX > umbral muy pequeño)
        // 2. Tiene un target diferente a su posición actual (distanciaTargetX > umbral)
        // Usamos un umbral MUY PEQUEÑO para capturar incluso movimientos sutiles
        seEstaMoviendo = (distanciaMovidaX > 0.01f) || 
                         (distanciaTargetX > UMBRAL_MOVIMIENTO);
        
        // 🔥 INTERPOLAR DESPUÉS DE DETECTAR MOVIMIENTO
        float dx = targetX - x;
        float dy = targetY - y;
        float distancia = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distancia > 0.1f) {
            float velocidad = Math.min(VELOCIDAD_INTERPOLACION * delta, 1.0f);
            x += dx * velocidad;
            y += dy * velocidad;
        } else {
            x = targetX;
            y = targetY;
        }
        
        // Guardar posición actual para el siguiente frame
        posicionAnteriorX = x;
        posicionAnteriorY = y;
        
        // 🔥 ANIMACIÓN: Solo avanzar frames si se está moviendo
        // NOTA: Removemos la condición enSuelo temporalmente para debugging
        if (seEstaMoviendo && !agarradoPalmera && !agachado) {
            tiempoFrame += delta;
            if (tiempoFrame >= DURACION_FRAME) {
                frameIndex++;
                if (frameIndex >= framesCaminando.length) {
                    frameIndex = 0;
                }
                tiempoFrame = 0f;
            }
        } else {
            // Solo resetear si realmente se detuvo
            if (!seEstaMoviendo) {
                frameIndex = 0;
                tiempoFrame = 0f;
            }
        }

    }
    
    public void render(SpriteBatch batch) {
        if (!primerPacketRecibido) {
            return;
        }
        
        Texture frameActual;
        
        if (enSuelo && agachado) {
            frameActual = frameAgachado.getTexture();
        } else if (seEstaMoviendo && !agarradoPalmera) {
            frameActual = framesCaminando[frameIndex];
        } else {
            frameActual = frameQuieto.getTexture();
        }
        
        // Aplicar efectos visuales
        if (ocultoEnArbusto) {
            batch.setColor(1, 1, 1, 0.3f);
        } else if (powerUpInstaKillActivo) {
            batch.setColor(1f, 0f, 0f, 1f);
        } else if (powerUpRecargaActivo) {
            batch.setColor(0f, 1f, 1f, 1f);
        }
        
        // Renderizar con dirección correcta
        if (mirandoDerecha) {
            batch.draw(frameActual, x, y, ANCHO, ALTO);
        } else {
            batch.draw(frameActual, x + ANCHO, y, -ANCHO, ALTO);
        }
        
        batch.setColor(Color.WHITE);
    }
    
    // ===== GETTERS =====
    
    public float getX() { return x; }
    public float getY() { return y; }
    public float getAncho() { return ANCHO; }
    public float getAlto() { return ALTO; }
    public int getVidas() { return vidas; }
    public int getSaludActual() { return saludActual; }
    public int getSaludMaxima() { return saludMaxima; }
    public float getSaludMental() { return saludMental; }
    public int getMunicionActual() { return municionActual; }
    public int getMunicionMaxima() { return municionMaxima; }
    public int getMunicionReserva() { return municionReserva; }
    public String getNombreArma() { return nombreArma; }
    public boolean estaRecargando() { return recargando; }
    public float getProgresoRecarga() { return progresoRecarga; }
    public boolean getMirandoDerecha() { return mirandoDerecha; }
    public boolean estaAgachado() { return agachado; }
    public boolean estaEnSuelo() { return enSuelo; }
    public boolean estaAgarradoPalmera() { return agarradoPalmera; }
    
    public int getPuntos() { return puntos; }
    
    public boolean tienePowerUpRecargaRapida() { return powerUpRecargaActivo; }
    public float getTiempoRecargaRestante() { return tiempoRecargaRestante; }
    public boolean tieneInstaKill() { return powerUpInstaKillActivo; }
    public float getTiempoInstaKillRestante() { return tiempoInstaKillRestante; }
    public boolean estaOcultoEnArbusto() { return ocultoEnArbusto; }
    
    public boolean estaInicializado() {
        return primerPacketRecibido;
    }
    
    public void dispose() {
        for (Texture t : framesCaminando) {
            if (t != null) t.dispose();
        }
        if (frameQuieto != null) frameQuieto.getTexture().dispose();
        if (frameAgachado != null) frameAgachado.getTexture().dispose();
    }
}