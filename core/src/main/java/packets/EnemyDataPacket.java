package packets;

import java.io.Serializable;

public class EnemyDataPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int idEnemigo;
    private String tipo; // "canibal" o "soldadonazi"
    private float x, y;
    private int vidaActual;
    private int vidaMaxima;
    private boolean vivo;
    private boolean mirandoDerecha;
    private boolean atacando;
    
    public EnemyDataPacket(int idEnemigo, String tipo, float x, float y, 
                          int vidaActual, int vidaMaxima, boolean vivo, 
                          boolean mirandoDerecha, boolean atacando) {
        this.idEnemigo = idEnemigo;
        this.tipo = tipo;
        this.x = x;
        this.y = y;
        this.vidaActual = vidaActual;
        this.vidaMaxima = vidaMaxima;
        this.vivo = vivo;
        this.mirandoDerecha = mirandoDerecha;
        this.atacando = atacando;
    }
    
    // Getters
    public int getIdEnemigo() { return idEnemigo; }
    public String getTipo() { return tipo; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getVidaActual() { return vidaActual; }
    public int getVidaMaxima() { return vidaMaxima; }
    public boolean isVivo() { return vivo; }
    public boolean isMirandoDerecha() { return mirandoDerecha; }
    public boolean isAtacando() { return atacando; }
}