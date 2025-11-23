package enemigos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import audio.ControladorDeAudio;

public class Canibal extends Enemigo {

    private static final float SANIDAD_RECUPERADA = 5f;

    // TEXTURAS
    private Texture texturaQuieto;
    private Texture[] framesCaminandoDerecha;
    private Texture[] framesCaminandoIzquierda;
    
    // ANIMACIÓN
    private int frameIndex = 0;
    private float tiempoFrame = 0f;
    private float duracionFrame = 0.15f;
    private boolean seEstaMoviendo = false;
    
    // SISTEMA DE AUDIO (específico del caníbal)
    private boolean yaReprodujoSonidoDeteccion = false;
    private boolean respiracionReproduciendose = false;
    private final float RANGO_RESPIRACION = 250f;

    public Canibal(float x, float y) {
        super(
            x, y,
            48f, 48f,
            150,
            40,
            120f,
            60f
        );

        this.colorEnemigo = Color.BROWN;
        this.cadenciaAtaque = 1.5f;

        // Cargar texturas
        this.texturaQuieto = new Texture("Canibal Imagenes/canibal-quieto.png");
        
        this.framesCaminandoDerecha = new Texture[3];
        for (int i = 0; i < 3; i++) {
            this.framesCaminandoDerecha[i] = new Texture("Canibal Imagenes/canibal-derecha-" + (i + 1) + ".png");
        }
        
        this.framesCaminandoIzquierda = new Texture[3];
        for (int i = 0; i < 3; i++) {
            this.framesCaminandoIzquierda[i] = new Texture("Canibal Imagenes/canibal-izquierda-" + (i + 1) + ".png");
        }
    }
    
    /**
     * ⭐ Asigna el controlador de audio (tanto a la clase padre como al caníbal)
     */
    public void setControladorAudio(ControladorDeAudio controlador) {
        this.controladorAudio = controlador; // ⭐ Asignar a la clase padre (Enemigo)
    }

    @Override
    public void actualizar(float delta, float jugadorX, float jugadorY, TiledMapTileLayer capa, float tiempoTotal, boolean jugadorLocalOculto) {
        if (!vivo) {
            // Detener respiración si murió
            if (respiracionReproduciendose && controladorAudio != null) {
                controladorAudio.detenerRespiracionCanibal();
                respiracionReproduciendose = false;
            }
            return;
        }

        // Actualizar física
        actualizarFisica(delta, capa);

        float distancia = distanciaAlJugador(jugadorX, jugadorY);

        // AGREGAR ESTE BLOQUE COMPLETO:
        if (jugadorLocalOculto) {
            if (respiracionReproduciendose && controladorAudio != null) {
                controladorAudio.detenerRespiracionCanibal();
                respiracionReproduciendose = false;
            }
            yaReprodujoSonidoDeteccion = false;
            atacando = false;
            seEstaMoviendo = false;
            return;
        }
        
        seEstaMoviendo = false;

        // SISTEMA DE SONIDO: "CANIBAL TE VE"
        if (distancia <= rangoDeteccion && !yaReprodujoSonidoDeteccion) {
            if (controladorAudio != null) {
                controladorAudio.reproducirSonido("canibal-te-ve");
                System.out.println("¡El caníbal te ha detectado!");
            }
            yaReprodujoSonidoDeteccion = true;
        }
        
        // SISTEMA DE SONIDO: "RESPIRACIÓN"
        if (distancia <= RANGO_RESPIRACION) {
            if (!respiracionReproduciendose && controladorAudio != null) {
                controladorAudio.iniciarRespiracionCanibal();
                respiracionReproduciendose = true;
                System.out.println("Respiración del caníbal activada");
            }
        } else {
            if (respiracionReproduciendose && controladorAudio != null) {
                controladorAudio.detenerRespiracionCanibal();
                respiracionReproduciendose = false;
            }
        }

        // Si detecta al jugador, perseguirlo
        if (distancia <= rangoDeteccion) {

            if (puedeAtacar(jugadorX, jugadorY, tiempoTotal)) {
                realizarAtaque(jugadorX, jugadorY, tiempoTotal);
                seEstaMoviendo = false;
            } else {
                moverHaciaJugador(jugadorX, jugadorY, delta, capa);
                atacando = false;
                seEstaMoviendo = true;
            }
        } else {
            atacando = false;
            seEstaMoviendo = false;
        }
        
        // Actualizar animación
        if (seEstaMoviendo) {
            tiempoFrame += delta;
            
            if (tiempoFrame >= duracionFrame) {
                frameIndex++;
                if (frameIndex >= 3) {
                    frameIndex = 0;
                }
                tiempoFrame = 0f;
            }
        } else {
            frameIndex = 0;
            tiempoFrame = 0f;
        }
    }

    @Override
    protected void realizarAtaque(float jugadorX, float jugadorY, float tiempoTotal) {
        atacando = true;
        tiempoUltimoAtaque = tiempoTotal;
    }

    /**
     * Renderiza el caníbal con textura animada
     */
    public void renderTextura(SpriteBatch batch) {
        if (!vivo) return;

        Texture frameActual;
        
        if (seEstaMoviendo) {
            if (mirandoDerecha) {
                frameActual = framesCaminandoDerecha[frameIndex];
            } else {
                frameActual = framesCaminandoIzquierda[frameIndex];
            }
        } else {
            frameActual = texturaQuieto;
        }
        
        batch.draw(frameActual, x, y, ancho, alto);
    }

    /**
     * Renderiza solo la barra de vida
     */
    @Override
    public void render(ShapeRenderer shapeRenderer) {
        if (!vivo) return;

        float barraWidth = ancho;
        float barraHeight = 5f;
        float barraY = y + alto + 5f;

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, barraY, barraWidth, barraHeight);

        float vidaPorcentaje = (float) vidaActual / vidaMaxima;
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(x, barraY, barraWidth * vidaPorcentaje, barraHeight);
    }

    public float getSanidadRecuperada() {
        return SANIDAD_RECUPERADA;
    }

    /**
     * Liberar recursos
     */
    public void dispose() {
        if (respiracionReproduciendose && controladorAudio != null) {
            controladorAudio.detenerRespiracionCanibal();
        }
        
        if (texturaQuieto != null) {
            texturaQuieto.dispose();
        }
        
        if (framesCaminandoDerecha != null) {
            for (Texture frame : framesCaminandoDerecha) {
                if (frame != null) {
                    frame.dispose();
                }
            }
        }
        
        if (framesCaminandoIzquierda != null) {
            for (Texture frame : framesCaminandoIzquierda) {
                if (frame != null) {
                    frame.dispose();
                }
            }
        }
    }
}