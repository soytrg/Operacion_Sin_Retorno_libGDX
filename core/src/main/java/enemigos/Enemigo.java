package enemigos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import audio.ControladorDeAudio;

public abstract class Enemigo {
    
    // FÍSICAS
    protected float x, y;
    protected float ancho, alto;
    protected float velocidad;
    protected float velocidadVertical = 0;
    protected float gravedad = -350f;
    protected boolean enSuelo = false;
    
    // ATRIBUTOS
    protected int vidaActual;
    protected int vidaMaxima;
    protected int daño;
    protected boolean vivo = true;
    protected boolean mirandoDerecha = true;
    
    // COMBATE
    protected float rangoDeteccion = 400f;
    protected float rangoAtaque;
    protected float tiempoUltimoAtaque = 0f;
    protected float cadenciaAtaque = 1.0f;
    protected boolean atacando = false;
    
    // TIPOS DE MUERTE
    protected boolean muertoADistancia = false;
    
    // TEXTURAS
    protected Color colorEnemigo = Color.RED;
    
    // AUDIO
    protected ControladorDeAudio controladorAudio;
    
    public Enemigo(float x, float y, float ancho, float alto, int vidaMaxima, int daño, float velocidad, float rangoAtaque) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
        this.vidaMaxima = vidaMaxima;
        this.vidaActual = vidaMaxima;
        this.daño = daño;
        this.velocidad = velocidad;
        this.rangoAtaque = rangoAtaque;
    }
    
    public boolean estaMuerto() {
        return this.vidaActual <= 0;
    }
    
    /**
     * ⭐ ASIGNA EL CONTROLADOR DE AUDIO
     */
    public void setControladorAudio(ControladorDeAudio controlador) {
        this.controladorAudio = controlador;
    }
    
    public abstract void actualizar(float delta, float jugadorX, float jugadorY, TiledMapTileLayer capa, float tiempoTotal, boolean jugadorOculto);
    
    protected abstract void realizarAtaque(float jugadorX, float jugadorY, float tiempoTotal);
    
    protected void actualizarFisica(float delta, TiledMapTileLayer capa) {
        velocidadVertical += gravedad * delta;
        y += velocidadVertical * delta;
        
        if (hayColisionAbajo(capa)) {
            int yTile = (int)((y - 1) / capa.getTileHeight());
            this.y = (yTile + 1) * capa.getTileHeight();
            this.velocidadVertical = 0;
            this.enSuelo = true;
        } else {
            this.enSuelo = false;
        }
    }
    
    protected void moverHaciaJugador(float jugadorX, float jugadorY, float delta, TiledMapTileLayer capa) {
        float direccionX = Math.signum(jugadorX - this.x);
        
        mirandoDerecha = direccionX > 0;
        
        float nuevaX = this.x + direccionX * velocidad * delta;
        
        if (direccionX < 0 && !hayColisionIzquierda(capa)) {
            this.x = nuevaX;
        } else if (direccionX > 0 && !hayColisionDerecha(capa)) {
            this.x = nuevaX;
        }
    }
    
    protected float distanciaAlJugador(float jugadorX, float jugadorY) {
        float dx = jugadorX - (this.x + this.ancho / 2f);
        float dy = jugadorY - (this.y + this.alto / 2f);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    protected boolean puedeAtacar(float jugadorX, float jugadorY, float tiempoTotal) {
        float distancia = distanciaAlJugador(jugadorX, jugadorY);
        return distancia <= rangoAtaque && (tiempoTotal - tiempoUltimoAtaque) >= cadenciaAtaque;
    }
    
    /**
     * ⭐ Recibe daño y reproduce sonido de muerte si corresponde
     */
    public void recibirDaño(int cantidad, boolean esProyectil) {
        this.vidaActual -= cantidad;
        
        if (this.vidaActual <= 0) {
            boolean yaEstabaMuerto = !this.vivo;
            this.vivo = false;
            this.muertoADistancia = esProyectil;
            
            if (!yaEstabaMuerto && controladorAudio != null) {
                controladorAudio.reproducirSonido("muerte");
                System.out.println("💀 Sonido de muerte reproducido");
            }
        }
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        if (!vivo) return;
        
        shapeRenderer.setColor(colorEnemigo);
        shapeRenderer.rect(x, y, ancho, alto);
        
        float barraWidth = ancho;
        float barraHeight = 5f;
        float barraY = y + alto + 5f;
        
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, barraY, barraWidth, barraHeight);
        
        float vidaPorcentaje = (float) vidaActual / vidaMaxima;
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(x, barraY, barraWidth * vidaPorcentaje, barraHeight);
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, ancho, alto);
    }
    
    // === COLISIONES CON EL MAPA ===
    
    protected boolean hayColisionAbajo(TiledMapTileLayer capa) {
        float tileWidth = capa.getTileWidth();
        float tileHeight = capa.getTileHeight();
        
        int xTileInicio = (int)(this.x / tileWidth);
        int xTileFin = (int)((this.x + this.ancho) / tileWidth);
        int yTile = (int)((this.y - 1) / tileHeight);
        
        for (int i = xTileInicio; i <= xTileFin; i++) {
            if (capa.getCell(i, yTile) != null) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean hayColisionIzquierda(TiledMapTileLayer capa) {
        float tileWidth = capa.getTileWidth();
        float tileHeight = capa.getTileHeight();
        float margen = 2f;
        
        int xTile = (int)((this.x - 1) / tileWidth);
        int yTileInicio = (int)((this.y + margen) / tileHeight);
        int yTileFin = (int)((this.y + this.alto - margen) / tileHeight);
        
        for (int i = yTileInicio; i <= yTileFin; i++) {
            if (capa.getCell(xTile, i) != null) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean hayColisionDerecha(TiledMapTileLayer capa) {
        float tileWidth = capa.getTileWidth();
        float tileHeight = capa.getTileHeight();
        float margen = 2f;
        
        int xTile = (int)((this.x + this.ancho + 1) / tileWidth);
        int yTileInicio = (int)((this.y + margen) / tileHeight);
        int yTileFin = (int)((this.y + this.alto - margen) / tileHeight);
        
        for (int i = yTileInicio; i <= yTileFin; i++) {
            if (capa.getCell(xTile, i) != null) {
                return true;
            }
        }
        return false;
    }
    
    // === GETTERS ===
    
    public float getX() { return x; }
    public float getY() { return y; }
    public float getAncho() { return ancho; }
    public float getAlto() { return alto; }
    public boolean estaVivo() { return vivo; }
    public boolean fueAsinadoADistancia() { return muertoADistancia; }
    public int getDaño() { return daño; }
    public boolean estaAtacando() { 
        return atacando; 
    }
 // Agregar a Enemigo.java (clase abstracta)

    public int getVidaActual() { 
        return vidaActual; 
    }

    public int getVidaMaxima() { 
        return vidaMaxima; 
    }

    public boolean getMirandoDerecha() {
        return mirandoDerecha;
    }
    
// === SETTERS PARA SINCRONIZACIÓN DE RED ===
    
    public void setX(float x) {
        this.x = x;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public void setVidaActual(int vida) {
        this.vidaActual = vida;
        if (this.vidaActual <= 0) {
            this.vivo = false;
        }
    }
}