package packets;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * ⭐ PACKET CON EL ESTADO COMPLETO DEL JUEGO
 */
public class GameStatePacket implements Serializable {
    private static final long serialVersionUID = 2L;
    
    private PlayerDataPacket jugador1;
    private PlayerDataPacket jugador2;
    private ArrayList<EnemyDataPacket> enemigos;
    private ArrayList<BulletDataPacket> balas;
    private ArrayList<PowerUpDataPacket> powerUps;
    private int nivelActual;
    private boolean nivelCompletado;
    private boolean cambiandoNivel;
    private int siguienteNivel;
    private long timestamp;
    
    public GameStatePacket() {
        this.enemigos = new ArrayList<>();
        this.balas = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.cambiandoNivel = false;
        this.siguienteNivel = 1;
        this.timestamp = System.currentTimeMillis();
    }
    
    public GameStatePacket(PlayerDataPacket jugador1, PlayerDataPacket jugador2,
                          ArrayList<EnemyDataPacket> enemigos,
                          ArrayList<BulletDataPacket> balas,
                          ArrayList<PowerUpDataPacket> powerUps,
                          int nivelActual, boolean nivelCompletado) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.enemigos = enemigos != null ? enemigos : new ArrayList<>();
        this.balas = balas != null ? balas : new ArrayList<>();
        this.powerUps = powerUps != null ? powerUps : new ArrayList<>();
        this.nivelActual = nivelActual;
        this.nivelCompletado = nivelCompletado;
        this.cambiandoNivel = false;
        this.siguienteNivel = nivelActual;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters y Setters
    public PlayerDataPacket getJugador1() { return jugador1; }
    public void setJugador1(PlayerDataPacket jugador1) { this.jugador1 = jugador1; }
    
    public PlayerDataPacket getJugador2() { return jugador2; }
    public void setJugador2(PlayerDataPacket jugador2) { this.jugador2 = jugador2; }
    
    public ArrayList<EnemyDataPacket> getEnemigos() { return enemigos; }
    public void setEnemigos(ArrayList<EnemyDataPacket> enemigos) { this.enemigos = enemigos; }
    
    public ArrayList<BulletDataPacket> getBalas() { return balas; }
    public void setBalas(ArrayList<BulletDataPacket> balas) { this.balas = balas; }
    
    public ArrayList<PowerUpDataPacket> getPowerUps() { return powerUps; }
    public void setPowerUps(ArrayList<PowerUpDataPacket> powerUps) { this.powerUps = powerUps; }
    
    public int getNivelActual() { return nivelActual; }
    public void setNivelActual(int nivelActual) { this.nivelActual = nivelActual; }
    
    public boolean isNivelCompletado() { return nivelCompletado; }
    public void setNivelCompletado(boolean nivelCompletado) { this.nivelCompletado = nivelCompletado; }
    
    public boolean isCambiandoNivel() { return cambiandoNivel; }
    public void setCambiandoNivel(boolean cambiandoNivel) { this.cambiandoNivel = cambiandoNivel; }
    
    public int getSiguienteNivel() { return siguienteNivel; }
    public void setSiguienteNivel(int siguienteNivel) { this.siguienteNivel = siguienteNivel; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}