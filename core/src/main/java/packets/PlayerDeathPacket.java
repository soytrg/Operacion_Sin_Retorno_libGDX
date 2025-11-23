package packets;

import java.io.Serializable;

/**
 * ⭐ PACKET PARA NOTIFICAR LA MUERTE DE UN JUGADOR
 */
public class PlayerDeathPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int idJugadorMuerto;
    private boolean ambosJugadoresMuertos;
    
    public PlayerDeathPacket(int idJugadorMuerto, boolean ambosJugadoresMuertos) {
        this.idJugadorMuerto = idJugadorMuerto;
        this.ambosJugadoresMuertos = ambosJugadoresMuertos;
    }
    
    public int getIdJugadorMuerto() {
        return idJugadorMuerto;
    }
    
    public boolean isAmbosJugadoresMuertos() {
        return ambosJugadoresMuertos;
    }
}