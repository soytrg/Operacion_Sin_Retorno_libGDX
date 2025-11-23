package enemigos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import audio.ControladorDeAudio;
import java.util.ArrayList;

public class SoldadoNazi extends Enemigo {
    
    private static final float SANIDAD_RECUPERADA = 10f;
    private static final float RANGO_CUCHILLO = 60f;
    private static final float RANGO_DISPARO = 350f;
    
    private ArrayList<BalaEnemiga> balas = new ArrayList<>();
    private float cadenciaDisparo = 3.0f;
    
    // SISTEMA DE RÁFAGA
    private boolean disparandoRafaga = false;
    private int balasRestantesRafaga = 0;
    private final int BALAS_POR_RAFAGA = 3;
    private float tiempoEntreBalasRafaga = 0f;
    private final float INTERVALO_RAFAGA = 0.25f;
    private float jugadorXObjetivo, jugadorYObjetivo;
    
    // TEXTURAS
    private Texture texturaQuieto;
    private Texture texturaDisparando;
    private Texture texturaMuriendo;
    
    // SISTEMA DE MUERTE
    private boolean muriendo = false;
    private float tiempoMuerte = 0f;
    private final float DURACION_MUERTE = 5f;
    
    // SISTEMA DE AUDIO
    private ControladorDeAudio controladorAudio;
    
    // SISTEMA DE DIÁLOGOS
    private boolean yaReprodujoDialogoDeteccion = false;
    private String dialogoAsignado = null;
    private float tiempoUltimoDialogo = 0f;
    private final float INTERVALO_DIALOGOS = 15f;
    
    public SoldadoNazi(float x, float y) {
        super(
            x, y,
            52f, 52f,
            300,
            50,
            80f,
            RANGO_DISPARO
        );
        
        this.colorEnemigo = Color.GRAY;
        this.cadenciaAtaque = 1.8f;
        
        // Cargar texturas
        this.texturaQuieto = new Texture("Nazi Imagenes/nazi-quieto.png");
        this.texturaDisparando = new Texture("Nazi Imagenes/nazi-disparando.png");
        this.texturaMuriendo = new Texture("Nazi Imagenes/nazi-muriendo.png");
        
        System.out.println("✅ Soldado Nazi creado en (" + x + ", " + y + ")");
    }
    
    public void setControladorAudio(ControladorDeAudio controlador) {
        this.controladorAudio = controlador;
        System.out.println("✅ Audio asignado al nazi en (" + x + ", " + y + ")");
    }
    
    @Override
    public void actualizar(float delta, float jugadorX, float jugadorY, TiledMapTileLayer capa, float tiempoTotal, boolean jugadorLocalOculto) {
        // Si está muriendo
        if (muriendo) {
            tiempoMuerte += delta;
            
            if (dialogoAsignado != null && controladorAudio != null) {
                controladorAudio.liberarDialogo(dialogoAsignado);
                dialogoAsignado = null;
            }
            
            if (tiempoMuerte >= DURACION_MUERTE) {
                vivo = false;
            }
            return;
        }
        
        if (!vivo) {
            iniciarMuerte();
            return;
        }
        
        if (jugadorLocalOculto) {
            yaReprodujoDialogoDeteccion = false;
            atacando = false;
            disparandoRafaga = false;
            return;
        }
        
        // Actualizar física
        actualizarFisica(delta, capa);
        
        // Actualizar balas
        for (int i = 0; i < balas.size(); i++) {
            BalaEnemiga bala = balas.get(i);
            bala.actualizar(delta);
            if (!bala.estaActiva()) {
                balas.remove(i);
                i--;
            }
        }
        
        // SISTEMA DE RÁFAGA
        if (disparandoRafaga) {
            tiempoEntreBalasRafaga += delta;
            
            if (tiempoEntreBalasRafaga >= INTERVALO_RAFAGA && balasRestantesRafaga > 0) {
                dispararBala(jugadorXObjetivo, jugadorYObjetivo);
                balasRestantesRafaga--;
                tiempoEntreBalasRafaga = 0f;
                
                if (balasRestantesRafaga <= 0) {
                    disparandoRafaga = false;
                }
            }
        }
        
        float distancia = distanciaAlJugador(jugadorX, jugadorY);
        
        // ACTUALIZAR DIRECCIÓN VISUAL
        float centroX = this.x + this.ancho / 2f;
        mirandoDerecha = (jugadorX > centroX);
        
        // SISTEMA DE DIÁLOGOS
        if (distancia <= rangoDeteccion) {
            // Diálogo INICIAL (solo una vez al detectar)
            if (!yaReprodujoDialogoDeteccion) {
                System.out.println("🎯 Nazi detectó al jugador a " + (int)distancia + " píxeles");
                reproducirDialogoAleatorio();
                yaReprodujoDialogoDeteccion = true;
                tiempoUltimoDialogo = tiempoTotal;
            }
            
            // Diálogos PERIÓDICOS (cada 15 segundos)
            if ((tiempoTotal - tiempoUltimoDialogo) >= INTERVALO_DIALOGOS) {
                System.out.println("⏰ 15 segundos pasados, reproduciendo nuevo diálogo");
                reproducirDialogoAleatorio();
                tiempoUltimoDialogo = tiempoTotal;
            }
        } else {
            // Si el jugador se aleja mucho, resetear el flag de detección
            if (distancia > rangoDeteccion * 1.5f && yaReprodujoDialogoDeteccion) {
                yaReprodujoDialogoDeteccion = false;
                System.out.println("↩️ Jugador se alejó, reseteando detección");
            }
        }
        
        // COMPORTAMIENTO DE COMBATE
        if (distancia <= rangoDeteccion) {
            
            if (distancia <= RANGO_CUCHILLO) {
                if (puedeAtacar(jugadorX, jugadorY, tiempoTotal)) {
                    realizarAtaqueCuchillo(jugadorX, jugadorY, tiempoTotal);
                } else {
                    moverHaciaJugador(jugadorX, jugadorY, delta, capa);
                    atacando = false;
                }
            } else if (distancia <= RANGO_DISPARO) {
                if (!disparandoRafaga && (tiempoTotal - tiempoUltimoAtaque) >= cadenciaDisparo) {
                    realizarAtaque(jugadorX, jugadorY, tiempoTotal);
                }
                atacando = false;
            } else {
                moverHaciaJugador(jugadorX, jugadorY, delta, capa);
                atacando = false;
            }
        } else {
            atacando = false;
        }
    }
    
    /**
     * Reproduce un diálogo aleatorio
     */
    private void reproducirDialogoAleatorio() {
        if (controladorAudio == null) {
            System.out.println("❌ ControladorAudio es null");
            return;
        }
        
        // Liberar diálogo anterior si existía
        if (dialogoAsignado != null) {
            System.out.println("🔓 Liberando diálogo anterior: " + dialogoAsignado);
            controladorAudio.liberarDialogo(dialogoAsignado);
        }
        
        // Obtener nuevo diálogo
        dialogoAsignado = controladorAudio.obtenerDialogoAleatorioDisponible();
        
        if (dialogoAsignado != null) {
            controladorAudio.reproducirDialogoNazi(dialogoAsignado);
            System.out.println("✅ Nazi reprodujo: " + dialogoAsignado);
        } else {
            System.out.println("⚠️ Todos los diálogos están en uso");
        }
    }
    
    /**
     * ⭐ Inicia el proceso de muerte y reproduce sonido
     * DETIENE el diálogo del nazi si está hablando
     */
    private void iniciarMuerte() {
        muriendo = true;
        tiempoMuerte = 0f;
        
        // ⭐ DETENER Y LIBERAR DIÁLOGO INMEDIATAMENTE
        if (dialogoAsignado != null && controladorAudio != null) {
            System.out.println("🔇 Deteniendo diálogo del nazi al morir: " + dialogoAsignado);
            controladorAudio.detenerDialogoNazi(dialogoAsignado); // ⭐ DETENER AUDIO
            controladorAudio.liberarDialogo(dialogoAsignado);      // ⭐ LIBERAR RECURSO
            dialogoAsignado = null;
        }
        
        balas.clear();
        disparandoRafaga = false;
        atacando = false;
        
        // ⭐ REPRODUCIR SONIDO DE MUERTE
        if (controladorAudio != null) {
            controladorAudio.reproducirSonido("muerte");
            System.out.println("💀 Sonido de muerte reproducido para Nazi");
        }
        
        System.out.println("Nazi iniciando muerte - Textura 'nazi-muriendo.png' activada");
    }
    
    @Override
    protected void realizarAtaque(float jugadorX, float jugadorY, float tiempoTotal) {
        tiempoUltimoAtaque = tiempoTotal;
        disparandoRafaga = true;
        balasRestantesRafaga = BALAS_POR_RAFAGA;
        tiempoEntreBalasRafaga = 0f;
        
        jugadorXObjetivo = jugadorX;
        jugadorYObjetivo = jugadorY;
        
        dispararBala(jugadorX, jugadorY);
        balasRestantesRafaga--;
    }
    
    private void dispararBala(float targetX, float targetY) {
        float centroX = this.x + this.ancho / 2f;
        float centroY = this.y + this.alto / 2f;
        
        BalaEnemiga bala = new BalaEnemiga(centroX, centroY, targetX, targetY, 30);
        balas.add(bala);
        
        if (controladorAudio != null) {
            controladorAudio.reproducirSonido("nazi-shoot");
        }
    }
    
    private void realizarAtaqueCuchillo(float jugadorX, float jugadorY, float tiempoTotal) {
        atacando = true;
        tiempoUltimoAtaque = tiempoTotal;
    }
    
    public void renderTextura(SpriteBatch batch) {
        if (!vivo && !muriendo) return;
        
        Texture frameActual;
        
        if (muriendo) {
            frameActual = texturaMuriendo;
        } else if (disparandoRafaga) {
            frameActual = texturaDisparando;
        } else {
            frameActual = texturaQuieto;
        }
        
        if (mirandoDerecha) {
            batch.draw(frameActual, x, y, ancho, alto);
        } else {
            batch.draw(frameActual, x + ancho, y, -ancho, alto);
        }
    }
    
    @Override
    public void render(ShapeRenderer shapeRenderer) {
        if (muriendo) return;
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
    
    @Override
    public boolean estaVivo() {
        return vivo && !muriendo;
    }
    
    public boolean debeSerEliminado() {
        return !vivo && (!muriendo || tiempoMuerte >= DURACION_MUERTE);
    }
    
    public float getSanidadRecuperada() {
        return SANIDAD_RECUPERADA;
    }
    
    public ArrayList<BalaEnemiga> getBalas() {
        return balas;
    }
    
    public void dispose() {
        // ⭐ DETENER Y LIBERAR DIÁLOGO AL ELIMINAR
        if (dialogoAsignado != null && controladorAudio != null) {
            controladorAudio.detenerDialogoNazi(dialogoAsignado);
            controladorAudio.liberarDialogo(dialogoAsignado);
            dialogoAsignado = null;
        }
        
        if (texturaQuieto != null) {
            texturaQuieto.dispose();
        }
        if (texturaDisparando != null) {
            texturaDisparando.dispose();
        }
        if (texturaMuriendo != null) {
            texturaMuriendo.dispose();
        }
    }
    
    // ===== CLASE INTERNA PARA BALAS =====
    
    public static class BalaEnemiga {
        private float x, y;
        private float velocidadX, velocidadY;
        private boolean activa = true;
        private int daño;
        
        private static final float VELOCIDAD = 300f;
        private static final float ANCHO = 8f;
        private static final float ALTO = 8f;
        private static final float TIEMPO_VIDA = 3f;
        private float tiempoVida = 0f;
        
        public BalaEnemiga(float x, float y, float targetX, float targetY, int daño) {
            this.x = x;
            this.y = y;
            this.daño = daño;
            
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
        
        public void desactivar() {
            activa = false;
        }
        
        public boolean estaActiva() {
            return activa;
        }
        
        public float getX() { return x; }
        public float getY() { return y; }
        public float getAncho() { return ANCHO; }
        public float getAlto() { return ALTO; }
        public int getDaño() { return daño; }
    }
}