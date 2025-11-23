package packets;

import java.io.Serializable;

public class PlayerDataPacket implements Serializable {
    private static final long serialVersionUID = 4L; // ⭐ INCREMENTADO de 3 a 4

    private int idJugador;
    private float x, y;
    private int vidas;
    private int saludActual;
    private int saludMaxima;
    private float saludMental;
    private int municionActual;
    private int municionMaxima;
    private int municionReserva;
    private String nombreArma;
    private boolean recargando;
    private float progresoRecarga;
    private boolean mirandoDerecha;
    private boolean agachado;
    private boolean enSuelo;
    private boolean agarradoPalmera;
    
    // ⭐⭐⭐ PUNTOS RE-HABILITADOS ⭐⭐⭐
    private int puntos;

    // Power-ups
    private boolean powerUpRecargaActivo;
    private float tiempoRecargaRestante;
    private boolean powerUpInstaKillActivo;
    private float tiempoInstaKillRestante;
    private boolean ocultoEnArbusto;

    public PlayerDataPacket(int idJugador, float x, float y, int vidas, int saludActual,
                           int saludMaxima, float saludMental, int municionActual,
                           int municionMaxima, int municionReserva, String nombreArma,
                           boolean recargando, float progresoRecarga, boolean mirandoDerecha,
                           boolean agachado, boolean enSuelo, boolean agarradoPalmera,
                           int puntos, // ⭐ PUNTOS AGREGADOS
                           boolean powerUpRecargaActivo, float tiempoRecargaRestante,
                           boolean powerUpInstaKillActivo, float tiempoInstaKillRestante,
                           boolean ocultoEnArbusto) {
        this.idJugador = idJugador;
        this.x = x;
        this.y = y;
        this.vidas = vidas;
        this.saludActual = saludActual;
        this.saludMaxima = saludMaxima;
        this.saludMental = saludMental;
        this.municionActual = municionActual;
        this.municionMaxima = municionMaxima;
        this.municionReserva = municionReserva;
        this.nombreArma = nombreArma;
        this.recargando = recargando;
        this.progresoRecarga = progresoRecarga;
        this.mirandoDerecha = mirandoDerecha;
        this.agachado = agachado;
        this.enSuelo = enSuelo;
        this.agarradoPalmera = agarradoPalmera;
        this.puntos = puntos; // ⭐ PUNTOS ASIGNADOS
        this.powerUpRecargaActivo = powerUpRecargaActivo;
        this.tiempoRecargaRestante = tiempoRecargaRestante;
        this.powerUpInstaKillActivo = powerUpInstaKillActivo;
        this.tiempoInstaKillRestante = tiempoInstaKillRestante;
        this.ocultoEnArbusto = ocultoEnArbusto;
    }

    // Getters
    public int getIdJugador() { return idJugador; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getVidas() { return vidas; }
    public int getSaludActual() { return saludActual; }
    public int getSaludMaxima() { return saludMaxima; }
    public float getSaludMental() { return saludMental; }
    public int getMunicionActual() { return municionActual; }
    public int getMunicionMaxima() { return municionMaxima; }
    public int getMunicionReserva() { return municionReserva; }
    public String getNombreArma() { return nombreArma; }
    public boolean isRecargando() { return recargando; }
    public float getProgresoRecarga() { return progresoRecarga; }
    public boolean isMirandoDerecha() { return mirandoDerecha; }
    public boolean isAgachado() { return agachado; }
    public boolean isEnSuelo() { return enSuelo; }
    public boolean isAgarradoPalmera() { return agarradoPalmera; }
    public int getPuntos() { return puntos; } // ⭐ GETTER AGREGADO
    public boolean isPowerUpRecargaActivo() { return powerUpRecargaActivo; }
    public float getTiempoRecargaRestante() { return tiempoRecargaRestante; }
    public boolean isPowerUpInstaKillActivo() { return powerUpInstaKillActivo; }
    public float getTiempoInstaKillRestante() { return tiempoInstaKillRestante; }
    public boolean isOcultoEnArbusto() { return ocultoEnArbusto; }
}