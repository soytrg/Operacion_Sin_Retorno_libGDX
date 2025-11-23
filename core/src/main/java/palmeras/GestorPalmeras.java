package palmeras;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import java.util.ArrayList;

/**
 * 🌴 GESTOR DE PUNTOS DE AGARRE
 * 
 * Administra todos los puntos de agarre cargados desde Tiled.
 * Ya no usa posiciones hardcodeadas, todo viene del mapa.
 */
public class GestorPalmeras {
    
    private ArrayList<PuntoAgarre> puntosDeAgarre = new ArrayList<>();
    
    /**
     * Constructor vacío - los puntos se cargan con cargarDesdeMapaTiled()
     */
    public GestorPalmeras() {
        System.out.println("🌴 Gestor de Puntos de Agarre inicializado");
    }
    
    /**
     * ⭐ CARGA LOS PUNTOS DE AGARRE DESDE EL MAPA DE TILED
     * 
     * @param objetosRamas MapObjects de la capa "ramas de agarre" en Tiled
     */
    public void cargarDesdeMapaTiled(MapObjects objetosRamas) {
        if (objetosRamas == null) {
            System.err.println("❌ No se encontró la capa 'ramas de agarre' en el mapa");
            return;
        }
        
        int puntosCreados = 0;
        
        for (MapObject objeto : objetosRamas) {
            // Solo procesar objetos rectangulares con nombre "agarre"
            if (objeto instanceof RectangleMapObject) {
                String nombre = objeto.getName();
                
                // Verificar que el objeto se llame "agarre" (case-insensitive)
                if (nombre != null && nombre.equalsIgnoreCase("agarre")) {
                    try {
                        float x = (float) objeto.getProperties().get("x");
                        float y = (float) objeto.getProperties().get("y");
                        float ancho = (float) objeto.getProperties().get("width");
                        float alto = (float) objeto.getProperties().get("height");
                        
                        // Crear punto de agarre
                        PuntoAgarre punto = new PuntoAgarre(x, y, ancho, alto);
                        puntosDeAgarre.add(punto);
                        puntosCreados++;
                        
                    } catch (Exception e) {
                        System.err.println("❌ Error al cargar punto de agarre: " + e.getMessage());
                    }
                }
            }
        }
        
        System.out.println("✅ Cargados " + puntosCreados + " puntos de agarre desde Tiled");
    }
    
    /**
     * Busca el punto de agarre más cercano al jugador
     * 
     * @param jugadorX Posición X del jugador
     * @param jugadorY Posición Y del jugador
     * @param jugadorAncho Ancho del jugador
     * @param jugadorAlto Alto del jugador
     * @return El punto de agarre más cercano disponible, o null si no hay ninguno
     */
    public PuntoAgarre buscarPalmeraCercana(float jugadorX, float jugadorY, 
                                             float jugadorAncho, float jugadorAlto) {
        PuntoAgarre puntoMasCercano = null;
        float distanciaMinima = Float.MAX_VALUE;
        
        float centroJugadorX = jugadorX + jugadorAncho / 2f;
        float centroJugadorY = jugadorY + jugadorAlto / 2f;
        
        for (PuntoAgarre punto : puntosDeAgarre) {
            if (punto.puedeAgarrarse(jugadorX, jugadorY, jugadorAncho, jugadorAlto)) {
                // Calcular distancia al centro del punto
                float centroX = punto.getX() + punto.getAncho() / 2f;
                float centroY = punto.getY() + punto.getAlto() / 2f;
                
                float dx = centroJugadorX - centroX;
                float dy = centroJugadorY - centroY;
                float distancia = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    puntoMasCercano = punto;
                }
            }
        }
        
        return puntoMasCercano;
    }
    
    /**
     * Obtiene la cantidad de puntos de agarre cargados
     * @return Número de puntos disponibles
     */
    public int getCantidadPalmeras() {
        return puntosDeAgarre.size();
    }
    
    /**
     * Libera todos los puntos de agarre
     */
    public void liberarTodos() {
        for (PuntoAgarre punto : puntosDeAgarre) {
            punto.liberar();
        }
    }
    
    /**
     * Limpia la lista de puntos (para cambiar de nivel)
     */
    public void limpiar() {
        puntosDeAgarre.clear();
        System.out.println("🧹 Puntos de agarre limpiados");
    }
    
    /**
     * Dispose (por compatibilidad)
     */
    public void dispose() {
        limpiar();
    }
}