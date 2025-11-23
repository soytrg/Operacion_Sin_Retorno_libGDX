package configuracion;

/**
 * ⭐ CONFIGURACIÓN COMPLETA DEL JUEGO
 * Esta clase centraliza TODA la configuración del juego
 */
public class ConfiguracionJuego {
    
    // ===== CONFIGURACIÓN DEL JUGADOR =====
    private String colorJugador = "rojo";
    private String skinJugador = "default";
    private String armaJugador = "pistola";
    private String nombreJugador = "Jugador 1";
    private int vidasJugador = 3;
    
    // ===== CONFIGURACIÓN DE DIFICULTAD =====
    private String dificultad = "normal";
    
    // ===== CONFIGURACIÓN DE AUDIO =====
    private float volumenMusica = 0.1f;
    private float volumenEfectos = 0.3f;
    
    // ===== CONFIGURACIÓN DE NIVEL =====
    private int nivel = 1;
    
    // ===== CONFIGURACIÓN DE MODO DE JUEGO =====
    private boolean modoCooperativo = false;
    
    // ===== CONFIGURACIÓN DE RED =====
    private boolean modoOnline = false;
    private boolean esHost = false;
    private int idJugador = 0;
    private String ipServidor = "localhost";
    private int puertoServidor = 9999;
    private EstadoJugador estadoJugador1; // Estado del HOST
    private EstadoJugador estadoJugador2; // Estado del CLIENTE
    
    // ===== CONSTRUCTOR =====
    public ConfiguracionJuego() {
        configurarDificultad("normal");
        this.estadoJugador1 = new EstadoJugador();
        this.estadoJugador2 = new EstadoJugador();
    }
    
    // ===== MÉTODOS DE JUGADOR =====
    
    public String getColorJugador() {
        return colorJugador;
    }
    
    public void setColorJugador(String colorJugador) {
        this.colorJugador = colorJugador;
    }
    
    public String getSkinJugador() {
        return skinJugador;
    }
    
    public void setSkinJugador(String skinJugador) {
        this.skinJugador = skinJugador;
    }
    
    public String getArmaJugador() {
        return armaJugador;
    }
    
    public void setArmaJugador(String armaJugador) {
        this.armaJugador = armaJugador;
    }
    
    public String getNombreJugador() {
        return nombreJugador;
    }
    
    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }
    
    public int getVidasJugador() {
        return vidasJugador;
    }
    
    public void setVidasJugador(int vidasJugador) {
        this.vidasJugador = vidasJugador;
    }
    
    // ===== MÉTODOS DE DIFICULTAD =====
    
    public String getDificultad() {
        return dificultad;
    }
    
    public void setDificultad(String dificultad) {
        this.dificultad = dificultad;
        configurarDificultad(dificultad);
    }
    
    public void configurarDificultad(String dificultad) {
        switch (dificultad.toLowerCase()) {
            case "facil":
                this.vidasJugador = 5;
                break;
            case "normal":
                this.vidasJugador = 3;
                break;
            case "dificil":
                this.vidasJugador = 1;
                break;
            default:
                this.vidasJugador = 3;
                break;
        }
    }
    
    // ===== MÉTODOS DE AUDIO =====
    
    public float getVolumenMusica() {
        return volumenMusica;
    }
    
    public void setVolumenMusica(float volumenMusica) {
        this.volumenMusica = Math.max(0.0f, Math.min(1.0f, volumenMusica));
    }
    
    public float getVolumenEfectos() {
        return volumenEfectos;
    }
    
    public void setVolumenEfectos(float volumenEfectos) {
        this.volumenEfectos = Math.max(0.0f, Math.min(1.0f, volumenEfectos));
    }
    
    // ===== MÉTODOS DE NIVEL =====
    
    public int getNivel() {
        return nivel;
    }
    
    public void setNivel(int nivel) {
        this.nivel = nivel;
    }
    
    // ===== MÉTODOS DE MODO DE JUEGO =====
    
    public boolean getModoCooperativo() {
        return modoCooperativo;
    }
    
    public void setModoCooperativo(boolean modoCooperativo) {
        this.modoCooperativo = modoCooperativo;
    }
    
    // ===== MÉTODOS DE RED =====
    
    public boolean getModoOnline() {
        return modoOnline;
    }
    
    public void setModoOnline(boolean modoOnline) {
        this.modoOnline = modoOnline;
    }
    
    public boolean esHost() {
        return esHost;
    }
    
    public void setEsHost(boolean esHost) {
        this.esHost = esHost;
    }
    
    public int getIdJugador() {
        return idJugador;
    }
    
    public void setIdJugador(int idJugador) {
        this.idJugador = idJugador;
    }
    
    public String getIpServidor() {
        return ipServidor;
    }
    
    public void setIpServidor(String ipServidor) {
        this.ipServidor = ipServidor;
    }
    
    public int getPuertoServidor() {
        return puertoServidor;
    }
    
    public void setPuertoServidor(int puertoServidor) {
        this.puertoServidor = puertoServidor;
    }
    
    public EstadoJugador getEstadoJugador(int idJugador) {
        return idJugador == 0 ? estadoJugador1 : estadoJugador2;
    }

    public EstadoJugador getEstadoJugadorLocal() {
        return getEstadoJugador(this.idJugador);
    }

    public EstadoJugador getEstadoJugadorRemoto() {
        return getEstadoJugador(this.idJugador == 0 ? 1 : 0);
    }

    // Método para limpiar estados (cuando se vuelve al menú)
    public void limpiarEstadosJugadores() {
        estadoJugador1.limpiar();
        estadoJugador2.limpiar();
    }
    
    
}