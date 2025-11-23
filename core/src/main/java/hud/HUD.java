package hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import configuracion.ConfiguracionJuego;
import jugador.Jugador;

public class HUD {  
	
    private Stage stage;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    
    private int vidas;
    private int saludActual;
    private int saludMaxima;
    private float saludMental;
    private int puntuacion;
    private String municion;
    private String armaActual;
    
    private Label vidasLabel;
    private Label saludLabel;
    private Label saludMentalLabel;
    private Label puntuacionLabel;
    private Label municionLabel;
    private Label armaLabel;
    private Label powerUpRecargaLabel;
    private Label powerUpInstaKillLabel;
    private Label palmeraLabel;

    private ConfiguracionJuego config;
    
    public HUD(SpriteBatch batch, ConfiguracionJuego config) {
    	this.config = config;
        this.puntuacion = 0;
        this.saludActual = 300;
        this.saludMaxima = 300;
        this.saludMental = 100f;
        this.vidas = this.config.getVidasJugador();
        this.municion = "12/60";
        this.armaActual = "Glock";
        
        this.viewport = new FitViewport(800, 600, new OrthographicCamera());
        this.stage = new Stage(viewport, batch);
        this.shapeRenderer = new ShapeRenderer();
        
        BitmapFont font = new BitmapFont();

        Label.LabelStyle estiloNormal = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle estiloPowerUpRecarga = new Label.LabelStyle(font, Color.CYAN);
        Label.LabelStyle estiloPowerUpInstaKill = new Label.LabelStyle(font, Color.GREEN);
        Label.LabelStyle estiloPalmera = new Label.LabelStyle(font, Color.LIME);
        Label.LabelStyle estiloArma = new Label.LabelStyle(font, Color.ORANGE);

        this.vidasLabel = new Label("Vidas: " + vidas, estiloNormal);
        this.saludLabel = new Label("Salud: " + saludActual + "/" + saludMaxima, estiloNormal);
        this.saludMentalLabel = new Label("Salud Mental: " + (int)saludMental + "%", estiloNormal);
        this.puntuacionLabel = new Label("Puntos: " + puntuacion, estiloNormal);
        this.armaLabel = new Label("Arma: " + armaActual, estiloArma);
        this.municionLabel = new Label("Munición: " + municion, estiloNormal);
        this.powerUpRecargaLabel = new Label("", estiloPowerUpRecarga);
        this.powerUpInstaKillLabel = new Label("", estiloPowerUpInstaKill);
        this.palmeraLabel = new Label("", estiloPalmera);

        Table tabla = new Table();
        tabla.top().left();
        tabla.setFillParent(true);
        
        tabla.add(this.vidasLabel).padTop(10).padLeft(10).left().width(250);
        tabla.row();
        tabla.add(this.saludLabel).padTop(5).padLeft(10).left().width(250);
        tabla.row();
        tabla.add(this.saludMentalLabel).padTop(5).padLeft(10).left().width(250);
        tabla.row();
        tabla.add(this.armaLabel).padTop(5).padLeft(10).left().width(250);
        tabla.row();
        tabla.add(this.municionLabel).padTop(5).padLeft(10).left().width(250);
        tabla.row();
        tabla.add(this.puntuacionLabel).padTop(5).padLeft(10).left().width(250);
        tabla.row();
        tabla.add(this.powerUpRecargaLabel).padTop(5).padLeft(10).left().width(250);
        tabla.row();
        tabla.add(this.powerUpInstaKillLabel).padTop(5).padLeft(10).left().width(250);
        tabla.row();
        tabla.add(this.palmeraLabel).padTop(5).padLeft(10).left().width(250);
        
        this.stage.addActor(tabla);
    }

    public void render() {
    	renderBarras();

    	this.stage.getViewport().apply();
    	this.stage.act(Gdx.graphics.getDeltaTime());
    	this.stage.draw();
    }
    
    private void renderBarras() {
        this.shapeRenderer.setProjectionMatrix(this.stage.getCamera().combined);
        this.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        float barraX = 10;
        float barraSaludY = 540;
        float barraSaludMentalY = 515;
        float barraAncho = 200;
        float barraAlto = 15;
        
        // BARRA DE SALUD
        this.shapeRenderer.setColor(0.3f, 0, 0, 1);
        this.shapeRenderer.rect(barraX, barraSaludY, barraAncho, barraAlto);
        
        float porcentajeSalud = (float)saludActual / saludMaxima;
        Color colorSalud = porcentajeSalud > 0.5f ? Color.GREEN : 
                          porcentajeSalud > 0.25f ? Color.ORANGE : Color.RED;
        this.shapeRenderer.setColor(colorSalud);
        this.shapeRenderer.rect(barraX, barraSaludY, barraAncho * porcentajeSalud, barraAlto);
        
        this.shapeRenderer.end();
        this.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        this.shapeRenderer.setColor(Color.WHITE);
        this.shapeRenderer.rect(barraX, barraSaludY, barraAncho, barraAlto);
        this.shapeRenderer.end();
        this.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // BARRA DE SALUD MENTAL
        this.shapeRenderer.setColor(0, 0, 0.3f, 1);
        this.shapeRenderer.rect(barraX, barraSaludMentalY, barraAncho, barraAlto);
        
        float porcentajeMental = saludMental / 100f;
        Color colorMental = porcentajeMental > 0.6f ? Color.CYAN : 
                           porcentajeMental > 0.3f ? Color.YELLOW : Color.RED;
        this.shapeRenderer.setColor(colorMental);
        this.shapeRenderer.rect(barraX, barraSaludMentalY, barraAncho * porcentajeMental, barraAlto);
        
        this.shapeRenderer.end();
        this.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        this.shapeRenderer.setColor(Color.WHITE);
        this.shapeRenderer.rect(barraX, barraSaludMentalY, barraAncho, barraAlto);
        
        this.shapeRenderer.end();
    }
    
    public void resize(int width, int height) {
    	this.viewport.update(width, height);
    }

    public void dispose() {
    	this.stage.dispose();
    	this.shapeRenderer.dispose();
    }

    public void sumarPuntos(int puntos) {
    	this.puntuacion += puntos;
    	this.puntuacionLabel.setText("Puntos: " + this.puntuacion);
    }
    
    public void actualizarVidas(int vidas) {
        this.vidas = vidas;
        this.vidasLabel.setText("Vidas: " + this.vidas);
    }
    
    public void actualizarSalud(int actual, int maxima) {
        this.saludActual = actual;
        this.saludMaxima = maxima;
        this.saludLabel.setText("Salud: " + this.saludActual + "/" + this.saludMaxima);
    }
    
    public void actualizarSaludMental(float porcentaje) {
        this.saludMental = porcentaje;
        this.saludMentalLabel.setText("Salud Mental: " + (int)this.saludMental + "%");
    }

    /**
     * ⭐ ACTUALIZADO: Muestra munición como "cargador/reserva"
     */
    public void actualizarMunicion(int actual, int maxima, String nombreArma, boolean recargando, float progreso) {
        this.armaActual = nombreArma;
        this.armaLabel.setText("Arma: " + this.armaActual);
        
        // ⭐ OBTENER MUNICIÓN DE RESERVA DEL JUGADOR
        // Nota: Necesitarás pasar la reserva como parámetro adicional
        String texto = "Munición: " + actual + "/" + maxima;
        
        if (recargando) {
            int porcentaje = (int)(progreso * 100);
            texto = "Munición: " + actual + " (Recargando " + porcentaje + "%)";
        }
        
        this.municionLabel.setText(texto);
    }

    public void actualizarPowerUpRecarga(boolean activo, float tiempoRestante) {
        if (activo) {
            int segundos = (int)Math.ceil(tiempoRestante);
            this.powerUpRecargaLabel.setText("⚡ RECARGA RÁPIDA: " + segundos + "s");
            this.powerUpRecargaLabel.setColor(Color.CYAN);
        } else {
        	this.powerUpRecargaLabel.setText("");
        }
    }
    
    public void actualizarPowerUpInstaKill(boolean activo, float tiempoRestante) {
        if (activo) {
            int segundos = (int)Math.ceil(tiempoRestante);
            this.powerUpInstaKillLabel.setText("💀 INSTAKILL: " + segundos + "s");
            this.powerUpInstaKillLabel.setColor(Color.GREEN);
        } else {
        	this.powerUpInstaKillLabel.setText("");
        }
    }
    
    public void actualizarPuntos(Jugador jugador) {
        this.puntuacion = jugador.getPuntos();
        this.puntuacionLabel.setText("Puntos: " + this.puntuacion);
    }

    public Stage getStage() {
        return this.stage;
    }
    
    public void actualizarEstadoPalmera(boolean agarrado) {
        if (agarrado) {
            this.palmeraLabel.setText("🌴 AGARRADO A PALMERA (SPACE/Movimiento para soltar)");
            this.palmeraLabel.setColor(Color.LIME);
        } else {
            this.palmeraLabel.setText("");
        }
    }
}