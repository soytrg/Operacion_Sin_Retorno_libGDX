package packets;

import java.io.Serializable;

/**
 * ⭐ PACKET DE CONEXIÓN INICIAL
 */
public class ConnectionPacket implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombreJugador;
    private int versionProtocolo;
    private boolean aceptado;

    public ConnectionPacket(String nombreJugador, int versionProtocolo, boolean aceptado) {
        this.nombreJugador = nombreJugador;
        this.versionProtocolo = versionProtocolo;
        this.aceptado = aceptado;
    }

    public String getNombreJugador() { return nombreJugador; }
    public int getVersionProtocolo() { return versionProtocolo; }
    public boolean isAceptado() { return aceptado; }
}