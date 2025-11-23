package powerups;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class PowerUp {

    // Enumeración para tipos de power-up
    public enum TipoPowerUp {
        RECARGA_RAPIDA,
        INSTAKILL,
        NUCLEAR
    }

    private float x, y;
    private final float ANCHO = 64f;
    private final float ALTO = 64f;
    private boolean recolectado = false;
    private float animacionTiempo = 0f;
    
    // ⭐ NUEVO: Sistema de tiempo de vida
    private float tiempoVida = 0f;
    private final float DURACION_MAXIMA = 15f; // 15 segundos antes de desaparecer
    private boolean expirado = false;

    private TipoPowerUp tipo;
    private Texture textura;

    public PowerUp(float x, float y, TipoPowerUp tipo) {
        this.x = x;
        this.y = y;
        this.tipo = tipo;
        cargarTextura();
    }

    private void cargarTextura() {
        try {
            switch (tipo) {
                case RECARGA_RAPIDA:
                    textura = new Texture(Gdx.files.internal("PowerUps Imagenes/manos_rapidas.png"));
                    break;
                case INSTAKILL:
                    textura = new Texture(Gdx.files.internal("PowerUps Imagenes/INSTAKILL.png"));
                    break;
                case NUCLEAR:
                    textura = new Texture(Gdx.files.internal("PowerUps Imagenes/nuke.png"));
                    break;
            }
            System.out.println("✅ Textura cargada para power-up: " + tipo);
        } catch (Exception e) {
            System.err.println("❌ Error al cargar textura para " + tipo + ": " + e.getMessage());
            textura = null;
        }
    }

    public void actualizar(float delta) {
        if (!recolectado && !expirado) {
            animacionTiempo += delta;
            tiempoVida += delta;
            
            // ⭐ Verificar si expiró
            if (tiempoVida >= DURACION_MAXIMA) {
                expirado = true;
                System.out.println("⏰ Power-up " + tipo + " expiró después de " + DURACION_MAXIMA + " segundos");
            }
        }
    }

    /**
     * Renderiza el power-up con su textura
     */
    public void render(SpriteBatch batch) {
        if (recolectado || expirado || textura == null) return;
        
        // Efecto de levitación y pulsación
        float pulsacion = (float)(Math.sin(animacionTiempo * 3) * 0.1f + 1.0f);
        float levitacion = (float)(Math.sin(animacionTiempo * 2) * 5f);
        
        float anchoAnimado = ANCHO * pulsacion;
        float altoAnimado = ALTO * pulsacion;
        
        // Calcular posición centrada con la animación
        float xCentrado = x - (anchoAnimado - ANCHO) / 2;
        float yCentrado = y - (altoAnimado - ALTO) / 2 + levitacion;
        
        // Transparencia pulsante (más rápida cuando está por expirar)
        float tiempoRestante = DURACION_MAXIMA - tiempoVida;
        float velocidadParpadeo = tiempoRestante < 3f ? 8f : 4f; // Parpadea más rápido en los últimos 3 segundos
        float alpha = 0.8f + (float)(Math.sin(animacionTiempo * velocidadParpadeo) * 0.2f);
        
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(textura, xCentrado, yCentrado, anchoAnimado, altoAnimado);
        
        // Restaurar color normal
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * @deprecated Ya no se usa ShapeRenderer
     */
    @Deprecated
    public void render(ShapeRenderer shapeRenderer) {
        // Mantenido por compatibilidad
    }

    public boolean verificarColision(float jugadorX, float jugadorY, float jugadorAncho, float jugadorAlto) {
        if (recolectado || expirado) return false;

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
    
    // ⭐ NUEVO: Verificar si debe ser eliminado
    public boolean debeSerEliminado() {
        return recolectado || expirado;
    }

    public TipoPowerUp getTipo() {
        return tipo;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getAncho() { return ANCHO; }
    public float getAlto() { return ALTO; }

    /**
     * Libera los recursos de la textura
     */
    public void dispose() {
        if (textura != null) {
            textura.dispose();
        }
    }
    
    // ⭐ NUEVO: Método estático para generar power-up aleatorio
    /**
     * Genera un power-up aleatorio basado en probabilidades
     * @return TipoPowerUp o null si no se generó ninguno
     */
    public static TipoPowerUp generarPowerUpAleatorio() {
        float random = (float) Math.random() * 100f;
        
        if (random < 2f) {
            // 2% Nuclear
            return TipoPowerUp.NUCLEAR;
        } else if (random < 5f) {
            // 3% InstaKill (2% + 3% = 5%)
            return TipoPowerUp.INSTAKILL;
        } else if (random < 9f) {
            // 4% Recarga Rápida (5% + 4% = 9%)
            return TipoPowerUp.RECARGA_RAPIDA;
        } else {
            // 91% Nada
            return null;
        }
    }
}