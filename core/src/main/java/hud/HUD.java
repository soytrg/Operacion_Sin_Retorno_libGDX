package hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class HUD {  // <- AGREGUÉ "public" AQUÍ
    private Stage stage;
    private Viewport viewport;
    private Integer vidas;
    private Integer puntuacion;
    private Label vidasLabel;
    private Label puntuacionLabel;
    private Label tiempoLabel;
    private Label municionLabel;
    private Label powerUpLabel;

    public HUD(SpriteBatch batch) {
    	this.vidas = 3;
        this.puntuacion = 0;

        // Cámara y viewport independientes del juego
        this.viewport = new FitViewport(800, 600, new OrthographicCamera());
        this.stage = new Stage(viewport, batch);

        // Fuente simple para los labels
        BitmapFont font = new BitmapFont();

        // Estilos para Labels
        Label.LabelStyle estiloNormal = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle estiloPowerUp = new Label.LabelStyle(font, Color.YELLOW);

        // Inicializamos los Labels
        this.vidasLabel = new Label("Vidas: " + vidas, estiloNormal);
        this.puntuacionLabel = new Label("Puntos: " + puntuacion, estiloNormal);
        this.tiempoLabel = new Label("Tiempo: 00", estiloNormal);
        this.municionLabel = new Label("Munición: 30/30", estiloNormal);
        this.powerUpLabel = new Label("", estiloPowerUp);

        // Creamos una tabla para ordenar los elementos
        Table tabla = new Table();
        tabla.top().left();
        tabla.setFillParent(true);

        // Añadimos a la tabla
        tabla.add(this.vidasLabel).padTop(10).padLeft(10).left().width(250);
        tabla.row();
        tabla.add(this.municionLabel).padTop(5).padLeft(10).left().width(250);
        tabla.row();
        tabla.add(this.puntuacionLabel).padTop(5).padLeft(10).left().width(250);
        tabla.row();
        tabla.add(this.tiempoLabel).padTop(5).padLeft(10).left().width(250);
        tabla.row();
        tabla.add(this.powerUpLabel).padTop(5).padLeft(10).left().width(250);

        // Añadimos la tabla al Stage
        this.stage.addActor(tabla);
    }

    public void render() {
    	this.stage.draw();
    }

    public void resize(int width, int height) {
    	this.viewport.update(width, height);
    }

    public void dispose() {
    	this.stage.dispose();
    }

    public void sumarPuntos(int puntos) {
    	this.puntuacion += puntos;
    	this.puntuacionLabel.setText("Puntos: " + this.puntuacion);
    }

    public void perderVida() {
        if (this.vidas > 0) this.vidas--;
        this.vidasLabel.setText("Vidas: " + this.vidas);
    }

    public void actualizarTiempo(int segundos) {
    	this.tiempoLabel.setText("Tiempo: " + segundos);
    }

    public void actualizarMunicion(int actual, int maxima, boolean recargando, float progreso) {
        String texto = "Munición: " + actual +"/"+ maxima;
        if (recargando) {
            int porcentaje = (int)(progreso * 100);
            texto = "Munición: " + actual +"/"+ maxima + " (Recargando " + porcentaje + "%)";
        }
        this.municionLabel.setText(texto);
    }

    public void actualizarPowerUp(boolean activo, float tiempoRestante) {
        if (activo) {
            int segundos = (int)Math.ceil(tiempoRestante);
            this.powerUpLabel.setText("RECARGA RÁPIDA: " + segundos + "s");
            this.powerUpLabel.setColor(Color.YELLOW);
        } else {
        	this.powerUpLabel.setText("");
        }
    }

    public Stage getStage() {
        return this.stage;
    }
}