package packets;

import java.io.Serializable;

public class DisconnectPacket implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idJugador;
    private String razon;

    public DisconnectPacket(int idJugador, String razon) {
        this.idJugador = idJugador;
        this.razon = razon;
    }

    public int getIdJugador() { return idJugador; }
    public String getRazon() { return razon; }
}