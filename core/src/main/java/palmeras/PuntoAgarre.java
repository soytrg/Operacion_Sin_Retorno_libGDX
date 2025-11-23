package palmeras;

import com.badlogic.gdx.math.Rectangle;

/**
 * 🌴 PUNTO DE AGARRE GENÉRICO
 *
 * Representa cualquier objeto de agarre en el mapa (palmera, cuerda, viga, etc.)
 * Se carga automáticamente desde la capa "ramas de agarre" en Tiled.
 *
 * Los objetos en Tiled deben ser rectángulos con el nombre "agarre"
 */
public class PuntoAgarre {

    private float x, y;           // Posición del rectángulo en Tiled
    private float ancho, alto;    // Dimensiones del rectángulo
    private boolean ocupado = false;

    // Radio de detección (qué tan cerca debe estar el jugador)
    private static final float RADIO_DETECCION = 80f;

    /**
     * Constructor desde un objeto de Tiled
     * @param x Coordenada X del objeto
     * @param y Coordenada Y del objeto
     * @param ancho Ancho del rectángulo
     * @param alto Alto del rectángulo
     */
    public PuntoAgarre(float x, float y, float ancho, float alto) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;

//        System.out.println("🎯 Punto de agarre creado en X=" + (int)x + ", Y=" + (int)y +
//                          " (Tamaño: " + (int)ancho + "x" + (int)alto + ")");
    }

    /**
     * Verifica si el jugador puede agarrarse a este punto
     * @param jugadorX Posición X del jugador
     * @param jugadorY Posición Y del jugador
     * @param jugadorAncho Ancho del jugador
     * @param jugadorAlto Alto del jugador
     * @return true si el jugador está lo suficientemente cerca y el punto está libre
     */
    public boolean puedeAgarrarse(float jugadorX, float jugadorY, float jugadorAncho, float jugadorAlto) {
        // Centro del jugador
        float centroJugadorX = jugadorX + jugadorAncho / 2f;
        float centroJugadorY = jugadorY + jugadorAlto / 2f;

        // Centro del punto de agarre
        float centroAgarreX = x + ancho / 2f;
        float centroAgarreY = y + alto / 2f;

        // Calcular distancia
        float distanciaX = Math.abs(centroJugadorX - centroAgarreX);
        float distanciaY = Math.abs(centroJugadorY - centroAgarreY);
        float distancia = (float) Math.sqrt(distanciaX * distanciaX + distanciaY * distanciaY);

        boolean dentroRango = distancia <= RADIO_DETECCION && !ocupado;

//        if (dentroRango) {
//            System.out.println("✅ Punto de agarre disponible! Distancia=" + (int)distancia + "px");
//        }

        return dentroRango;
    }

    /**
     * ⭐ AJUSTADO: Obtiene la posición X donde el jugador debe colocarse al agarrarse
     * @return Coordenada X del centro del agarre
     */
    public float getPosicionAgarreX() {
        return x + ancho / 2f;
    }

    /**
     * ⭐ AJUSTADO: Obtiene la posición Y donde el jugador debe colocarse al agarrarse
     * Ahora el jugador se agarra en la parte SUPERIOR del rectángulo (más arriba)
     * @return Coordenada Y en la parte superior del agarre
     */
    public float getPosicionAgarreY() {
        // Cambiado: ahora usa y + alto en lugar de y + alto / 2f
        // Esto coloca al jugador en la parte SUPERIOR del rectángulo
        return y + alto;
    }

    /**
     * Marca este punto como ocupado (un jugador se agarró)
     */
    public void ocupar() {
        this.ocupado = true;
        System.out.println("🔒 Punto de agarre ocupado");
    }

    /**
     * Libera este punto (el jugador se soltó)
     */
    public void liberar() {
        this.ocupado = false;
        System.out.println("🔓 Punto de agarre liberado");
    }

    /**
     * Verifica si este punto está ocupado
     * @return true si está ocupado
     */
    public boolean estaOcupado() {
        return ocupado;
    }

    /**
     * Obtiene el rectángulo de colisión de este punto
     * @return Rectangle con las dimensiones del agarre
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, ancho, alto);
    }

    // === GETTERS ===

    public float getX() { return x; }
    public float getY() { return y; }
    public float getAncho() { return ancho; }
    public float getAlto() { return alto; }
}