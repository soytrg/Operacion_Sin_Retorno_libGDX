package powerups;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class PowerUp {
    private float x, y;
    private final float ANCHO = 32f;
    private final float ALTO = 32f;
    private boolean recolectado = false;
    private float animacionTiempo = 0f;
    
    public PowerUp(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public void actualizar(float delta) {
        if (!recolectado) {
            animacionTiempo += delta;
        }
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        if (!recolectado) {
            // Efecto de animación - cambiar color y tamaño
            float pulsacion = (float)(Math.sin(animacionTiempo * 5) * 0.1f + 1.0f);
            float tamaño = ANCHO * pulsacion;
            
            // Dibujar power-up como rectángulo brillante
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(x - (tamaño - ANCHO) / 2, y - (tamaño - ALTO) / 2, tamaño, tamaño);
            
            // Borde más brillante
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(x - (tamaño - ANCHO) / 2 + 2, y - (tamaño - ALTO) / 2 + 2, tamaño - 4, tamaño - 4);
            
            // Centro azul
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.rect(x - (tamaño - ANCHO) / 2 + 4, y - (tamaño - ALTO) / 2 + 4, tamaño - 8, tamaño - 8);
        }
    }
    
    public boolean verificarColision(float jugadorX, float jugadorY, float jugadorAncho, float jugadorAlto) {
        if (recolectado) return false;
        
        Rectangle powerUpRect = new Rectangle(x, y, ANCHO, ALTO);
        Rectangle jugadorRect = new Rectangle(jugadorX, jugadorY, jugadorAncho, jugadorAlto);
        
        return powerUpRect.overlaps(jugadorRect);
    }
    
    public void recolectar() {
        recolectado = true;
    }
    
    public boolean estaRecolectado() {
        return recolectado;
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public float getAncho() { return ANCHO; }
    public float getAlto() { return ALTO; }
}