package configuracion;

import java.util.HashMap;
import java.util.Map;

/**
 * ⭐ CLASE PARA PERSISTIR EL ESTADO DEL JUGADOR ENTRE NIVELES
 * 
 * Almacena:
 * - Puntos acumulados
 * - Armas compradas (slot -> datos del arma)
 * - Munición de cada arma
 * 
 * Esto permite que los jugadores mantengan su progreso al cambiar de nivel.
 */
public class EstadoJugador {
    
    // Clase interna para representar un arma guardada
    public static class ArmaGuardada {
        public String nombre;
        public int daño;
        public int municionActual;
        public int municionMaxima;
        public int municionReserva;
        public int municionReservaMaxima;
        public float tiempoRecarga;
        
        public ArmaGuardada(String nombre, int daño, int municionActual, int municionMaxima, 
                           int municionReserva, int municionReservaMaxima, float tiempoRecarga) {
            this.nombre = nombre;
            this.daño = daño;
            this.municionActual = municionActual;
            this.municionMaxima = municionMaxima;
            this.municionReserva = municionReserva;
            this.municionReservaMaxima = municionReservaMaxima;
            this.tiempoRecarga = tiempoRecarga;
        }
    }
    
    private int puntos;
    private Map<Integer, ArmaGuardada> armas; // slot -> ArmaGuardada
    private int slotArmaActual;
    
    public EstadoJugador() {
        this.puntos = 0;
        this.armas = new HashMap<>();
        this.slotArmaActual = 1;
    }
    
    // ===== MÉTODOS PARA GUARDAR ESTADO =====
    
    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }
    
    public void setArmas(Map<Integer, ArmaGuardada> armas) {
        this.armas = new HashMap<>(armas);
    }
    
    public void setSlotArmaActual(int slot) {
        this.slotArmaActual = slot;
    }
    
    public void agregarArma(int slot, String nombre, int daño, int municionActual, 
                           int municionMaxima, int municionReserva, int municionReservaMaxima, 
                           float tiempoRecarga) {
        ArmaGuardada arma = new ArmaGuardada(nombre, daño, municionActual, municionMaxima, 
                                             municionReserva, municionReservaMaxima, tiempoRecarga);
        this.armas.put(slot, arma);
    }
    
    // ===== MÉTODOS PARA RECUPERAR ESTADO =====
    
    public int getPuntos() {
        return puntos;
    }
    
    public Map<Integer, ArmaGuardada> getArmas() {
        return new HashMap<>(armas);
    }
    
    public int getSlotArmaActual() {
        return slotArmaActual;
    }
    
    public boolean tieneArmas() {
        return !armas.isEmpty();
    }
    
    public void limpiar() {
        this.puntos = 0;
        this.armas.clear();
        this.slotArmaActual = 1;
    }
    
    @Override
    public String toString() {
        return "EstadoJugador{puntos=" + puntos + ", armas=" + armas.size() + 
               ", slotActual=" + slotArmaActual + "}";
    }
}