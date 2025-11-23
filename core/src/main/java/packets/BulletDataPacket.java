package packets;

import java.io.Serializable;

/**
 * 🔥 CORREGIDO: Ahora incluye el ID del dueño de la bala
 */
public class BulletDataPacket implements Serializable {
    private static final long serialVersionUID = 2L; // ⭐ Incrementado por cambio de estructura

    private int idBala;
    private float x, y;
    private float velocidadX, velocidadY;
    private boolean activa;
    private int idDueño; // ⭐ NUEVO: 0 = Host, 1 = Cliente

    public BulletDataPacket(int idBala, float x, float y, float velocidadX, float velocidadY, boolean activa, int idDueño) {
        this.idBala = idBala;
        this.x = x;
        this.y = y;
        this.velocidadX = velocidadX;
        this.velocidadY = velocidadY;
        this.activa = activa;
        this.idDueño = idDueño;
    }

    // Getters
    public int getIdBala() { return idBala; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getVelocidadX() { return velocidadX; }
    public float getVelocidadY() { return velocidadY; }
    public boolean isActiva() { return activa; }
    public int getIdDueño() { return idDueño; } // ⭐ NUEVO
}