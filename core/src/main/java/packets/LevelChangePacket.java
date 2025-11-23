package packets;

import java.io.Serializable;

/**
 * ⭐ PACKET PARA SINCRONIZAR CAMBIO DE NIVEL EN COOPERATIVO
 * El HOST envía este packet al CLIENTE cuando cualquier jugador completa el nivel
 */
public class LevelChangePacket implements Serializable {
    private static final long serialVersionUID = 1L;

    private int nivelActual;
    private int siguienteNivel;
    private int jugadorQueCompleto; // 0 = Jugador 1 (Host), 1 = Jugador 2 (Cliente)
    private long timestamp;

    public LevelChangePacket(int nivelActual, int siguienteNivel, int jugadorQueCompleto) {
        this.nivelActual = nivelActual;
        this.siguienteNivel = siguienteNivel;
        this.jugadorQueCompleto = jugadorQueCompleto;
        this.timestamp = System.currentTimeMillis();
    }

    // ===== GETTERS =====
    public int getNivelActual() { return nivelActual; }
    public int getSiguienteNivel() { return siguienteNivel; }
    public int getJugadorQueCompleto() { return jugadorQueCompleto; }
    public long getTimestamp() { return timestamp; }
}