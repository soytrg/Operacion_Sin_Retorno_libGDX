package packets;

import java.io.Serializable;

public class PowerUpDataPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int idPowerUp;
    private String tipo; // "NUCLEAR", "INSTAKILL", "RECARGA_RAPIDA"
    private float x, y;
    private boolean recolectado;
    
    public PowerUpDataPacket(int idPowerUp, String tipo, float x, float y, boolean recolectado) {
        this.idPowerUp = idPowerUp;
        this.tipo = tipo;
        this.x = x;
        this.y = y;
        this.recolectado = recolectado;
    }
    
    // Getters
    public int getIdPowerUp() { return idPowerUp; }
    public String getTipo() { return tipo; }
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isRecolectado() { return recolectado; }
}