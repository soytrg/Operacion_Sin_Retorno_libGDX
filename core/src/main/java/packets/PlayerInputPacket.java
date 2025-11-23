package packets;

import java.io.Serializable;

/**
 * ⭐ PACKET CON EL INPUT DEL JUGADOR
 * El CLIENTE envía esto al HOST cada frame
 */
public class PlayerInputPacket implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idJugador;
    
    // Inputs de movimiento
    private boolean derecha;
    private boolean izquierda;
    private boolean saltar;
    private boolean agacharse;
    
    // Inputs de combate
    private boolean disparar;
    private boolean recargar;
    private boolean atacarCuchillo;
    private boolean agarrarse;
    
    // Posición del mouse (para apuntar)
    private float mouseX;
    private float mouseY;
    
    // Timestamp
    private long timestamp;
    
    public PlayerInputPacket(int idJugador, boolean derecha, boolean izquierda,
                            boolean saltar, boolean agacharse, boolean disparar,
                            boolean recargar, boolean atacarCuchillo, boolean agarrarse,
                            float mouseX, float mouseY) {
        this.idJugador = idJugador;
        this.derecha = derecha;
        this.izquierda = izquierda;
        this.saltar = saltar;
        this.agacharse = agacharse;
        this.disparar = disparar;
        this.recargar = recargar;
        this.atacarCuchillo = atacarCuchillo;
        this.agarrarse = agarrarse;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.timestamp = System.currentTimeMillis();
    }
    
    // ===== GETTERS =====
    
    public int getIdJugador() { return idJugador; }
    public boolean isDerecha() { return derecha; }
    public boolean isIzquierda() { return izquierda; }
    public boolean isSaltar() { return saltar; }
    public boolean isAgacharse() { return agacharse; }
    public boolean isDisparar() { return disparar; }
    public boolean isRecargar() { return recargar; }
    public boolean isAtacarCuchillo() { return atacarCuchillo; }
    public boolean isAgarrarse() { return agarrarse; }
    public float getMouseX() { return mouseX; }
    public float getMouseY() { return mouseY; }
    public long getTimestamp() { return timestamp; }
}