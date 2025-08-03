package configuracion;

public class ConfiguracionJuego {
    // Configuración del juego
    private static ConfiguracionJuego instancia;
    
    // Modo de juego
    private boolean modoCooperativo = false;
    
    // Dificultad
    public enum Dificultad {
        FACIL, NORMAL, DIFICIL
    }
    
    private Dificultad dificultad = Dificultad.NORMAL;
    
    // Personalización del jugador
    private String colorJugador = "azul";
    private String skinJugador = "normal";
    private String armaJugador = "pistola";
    
    // Configuración de dificultad
    private int vidasJugador = 3;
    private int cantidadEnemigos = 5;
    private float probabilidadPowerUp = 0.3f;
    
    // Audio
    private float volumenMusica = 0.5f;
    private float volumenEfectos = 1.0f;
    
    private ConfiguracionJuego() {
        // Constructor privado para singleton
    }
    
    public static ConfiguracionJuego getInstancia() {
        if (instancia == null) {
            instancia = new ConfiguracionJuego();
        }
        return instancia;
    }
    
    public void configurarDificultad(Dificultad dificultad) {
        this.dificultad = dificultad;
        
        switch (dificultad) {
            case FACIL:
                vidasJugador = 5;
                cantidadEnemigos = 3;
                probabilidadPowerUp = 0.5f;
                break;
            case NORMAL:
                vidasJugador = 3;
                cantidadEnemigos = 5;
                probabilidadPowerUp = 0.3f;
                break;
            case DIFICIL:
                vidasJugador = 1;
                cantidadEnemigos = 8;
                probabilidadPowerUp = 0.1f;
                break;
        }
    }
    
    // Getters y Setters
    public boolean isModoCooperativo() {
        return modoCooperativo;
    }
    
    public void setModoCooperativo(boolean modoCooperativo) {
        this.modoCooperativo = modoCooperativo;
    }
    
    public Dificultad getDificultad() {
        return dificultad;
    }
    
    public void setDificultad(Dificultad dificultad) {
        this.dificultad = dificultad;
    }
    
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
    
    public int getVidasJugador() {
        return vidasJugador;
    }
    
    public int getCantidadEnemigos() {
        return cantidadEnemigos;
    }
    
    public float getProbabilidadPowerUp() {
        return probabilidadPowerUp;
    }
    
    public float getVolumenMusica() {
        return volumenMusica;
    }
    
    public void setVolumenMusica(float volumenMusica) {
        this.volumenMusica = volumenMusica;
    }
    
    public float getVolumenEfectos() {
        return volumenEfectos;
    }
    
    public void setVolumenEfectos(float volumenEfectos) {
        this.volumenEfectos = volumenEfectos;
    }
}