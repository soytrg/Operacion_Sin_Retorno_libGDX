package packets;

import java.io.Serializable;

/**
 * ⭐ PACKET PARA SINCRONIZAR COMPRAS EN LA TIENDA
 * Se envía cuando un jugador compra algo en la tienda
 */
public class ShopPurchasePacket implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int idJugador; // 0 = Host, 1 = Cliente
    private String tipoCompra; // "arma", "medicina"
    private String nombreItem; // "Glock", "Deagle/Revolver", "Uzi", "AK-47", "Medicina"
    private int slot; // Para armas: 1, 2, 3, 4
    private int precio; // Puntos gastados
    
    // Estadísticas del arma (si es arma)
    private int daño;
    private int municionMaxima;
    private int municionReserva;
    private float tiempoRecarga;
    
    // Constructores
    public ShopPurchasePacket() {}
    
    /**
     * Constructor para compra de MEDICINA
     */
    public ShopPurchasePacket(int idJugador, int precio) {
        this.idJugador = idJugador;
        this.tipoCompra = "medicina";
        this.nombreItem = "Medicina";
        this.precio = precio;
    }
    
    /**
     * Constructor para compra de ARMA
     */
    public ShopPurchasePacket(int idJugador, String nombreArma, int slot, int precio, 
                              int daño, int municionMaxima, int municionReserva, float tiempoRecarga) {
        this.idJugador = idJugador;
        this.tipoCompra = "arma";
        this.nombreItem = nombreArma;
        this.slot = slot;
        this.precio = precio;
        this.daño = daño;
        this.municionMaxima = municionMaxima;
        this.municionReserva = municionReserva;
        this.tiempoRecarga = tiempoRecarga;
    }
    
    // Getters
    public int getIdJugador() {
        return idJugador;
    }
    
    public String getTipoCompra() {
        return tipoCompra;
    }
    
    public String getNombreItem() {
        return nombreItem;
    }
    
    public int getSlot() {
        return slot;
    }
    
    public int getPrecio() {
        return precio;
    }
    
    public int getDaño() {
        return daño;
    }
    
    public int getMunicionMaxima() {
        return municionMaxima;
    }
    
    public int getMunicionReserva() {
        return municionReserva;
    }
    
    public float getTiempoRecarga() {
        return tiempoRecarga;
    }
    
    // Setters
    public void setIdJugador(int idJugador) {
        this.idJugador = idJugador;
    }
    
    public void setTipoCompra(String tipoCompra) {
        this.tipoCompra = tipoCompra;
    }
    
    public void setNombreItem(String nombreItem) {
        this.nombreItem = nombreItem;
    }
    
    public void setSlot(int slot) {
        this.slot = slot;
    }
    
    public void setPrecio(int precio) {
        this.precio = precio;
    }
    
    public void setDaño(int daño) {
        this.daño = daño;
    }
    
    public void setMunicionMaxima(int municionMaxima) {
        this.municionMaxima = municionMaxima;
    }
    
    public void setMunicionReserva(int municionReserva) {
        this.municionReserva = municionReserva;
    }
    
    public void setTiempoRecarga(float tiempoRecarga) {
        this.tiempoRecarga = tiempoRecarga;
    }
    
    @Override
    public String toString() {
        if (tipoCompra.equals("medicina")) {
            return "ShopPurchasePacket{Jugador=" + idJugador + ", Item=Medicina, Precio=" + precio + "}";
        } else {
            return "ShopPurchasePacket{Jugador=" + idJugador + ", Arma=" + nombreItem + 
                   ", Slot=" + slot + ", Daño=" + daño + ", Munición=" + municionMaxima + 
                   "/" + municionReserva + ", Recarga=" + tiempoRecarga + "s, Precio=" + precio + "}";
        }
    }
}