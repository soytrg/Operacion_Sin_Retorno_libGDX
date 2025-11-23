package packets;

import java.io.Serializable;

public abstract class Packet implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum PacketType {
        PLAYER_INPUT,      // Cliente → Host: Inputs del jugador
        GAME_STATE,        // Host → Cliente: Estado completo del juego
        CONNECTION,        // Handshake inicial
        DISCONNECT,        // Notificación de desconexión
        PING               // Keep-alive
    }
    
    private PacketType tipo;
    private long timestamp;
    
    public Packet(PacketType tipo) {
        this.tipo = tipo;
        this.timestamp = System.currentTimeMillis();
    }
    
    public PacketType getTipo() {
        return tipo;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}