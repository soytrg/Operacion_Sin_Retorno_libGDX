package bala;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

/**
 * 🔥 CORREGIDO: Sistema de identificación de dueño mejorado
 */
public class Bala {
    private float x, y;
    private float velocidadX, velocidadY;
    private boolean activa = true;
    
    private static final float VELOCIDAD = 600f;
    private static final float ANCHO = 10f;
    private static final float ALTO = 5f;
    private static final float TIEMPO_VIDA = 3f;
    private float tiempoVida = 0f;
    
    private int idDueño; // 0 = Host, 1 = Cliente
    
    /**
     * ⚠️ DEPRECADO: Usa el constructor con idDueño
     */
    @Deprecated
    public Bala(float x, float y, float targetX, float targetY) {
        this(x, y, targetX, targetY, 0); // Por defecto Host, pero debería evitarse
        System.err.println("⚠️ WARNING: Bala creada sin especificar idDueño");
    }
    
    /**
     * ✅ CONSTRUCTOR RECOMENDADO
     */
    public Bala(float x, float y, float targetX, float targetY, int idDueño) {
        this.x = x;
        this.y = y;
        this.idDueño = idDueño;
        
        float dx = targetX - x;
        float dy = targetY - y;
        float distancia = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distancia != 0) {
            this.velocidadX = (dx / distancia) * VELOCIDAD;
            this.velocidadY = (dy / distancia) * VELOCIDAD;
        }
    }
    
    public void actualizar(float delta) {
        if (!activa) return;
        
        x += velocidadX * delta;
        y += velocidadY * delta;
        
        tiempoVida += delta;
        if (tiempoVida >= TIEMPO_VIDA) {
            activa = false;
        }
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        if (!activa) return;
        
        // Color según el dueño
        if (idDueño == 0) {
            shapeRenderer.setColor(Color.YELLOW); // Host = amarillo
        } else {
            shapeRenderer.setColor(Color.CYAN); // Cliente = cyan
        }
        
        shapeRenderer.rect(x, y, ANCHO, ALTO);
    }
    
    public void desactivar() {
        activa = false;
    }
    
    public boolean estaActiva() {
        return activa;
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getAncho() { return ANCHO; }
    public float getAlto() { return ALTO; }
    public float getVelocidadX() { return velocidadX; }
    public float getVelocidadY() { return velocidadY; }
    public int getIdDueño() { return idDueño; }
}