package pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import audio.ControladorDeAudio;
import configuracion.ConfiguracionJuego;
import jugador.ControladorEntrada;

public class PantallaPausa implements Screen {

    private final Game game;
    private final SpriteBatch batch;
    private final ControladorDeAudio controladorDeAudio;
    private final ControladorEntrada controladorDeEntradas;
    private final Screen pantallaAnterior;

    private ConfiguracionJuego config;
    
    private BitmapFont font;
    private String[] opciones = {"Reanudar", "Menu Principal", "Salir"};
    private int opcionSeleccionada = 0;

    private float tiempoUltimoInput = 0f;
    private final float DELAY_INPUT = 0.2f;

    public PantallaPausa(Game game, SpriteBatch batch, ControladorDeAudio controladorDeAudio,ControladorEntrada controladorDeEntradas,ConfiguracionJuego config, Screen pantallaAnterior) {
        this.game = game;
        this.batch = batch;
        this.controladorDeAudio = controladorDeAudio;
        this.controladorDeEntradas = controladorDeEntradas;
        this.pantallaAnterior = pantallaAnterior;
        this.config = config;
    }

    @Override
    public void show() {
        this.font = new BitmapFont();
        Gdx.input.setInputProcessor(controladorDeEntradas);
    }

    @Override
    public void render(float delta) {
        // Limpiar pantalla con leve transparencia
        Gdx.gl.glClearColor(0, 0, 0, 0.6f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Título
        font.getData().setScale(3f);
        font.setColor(Color.CYAN);
        GlyphLayout layoutTitulo = new GlyphLayout(font, "PAUSA");
        float tituloX = Gdx.graphics.getWidth() / 2f - layoutTitulo.width / 2f;
        float tituloY = Gdx.graphics.getHeight() * 0.8f;
        font.draw(batch, layoutTitulo, tituloX, tituloY);

        // Opciones del menú
        font.getData().setScale(2f);
        float startY = Gdx.graphics.getHeight() * 0.55f;
        GlyphLayout layoutOpciones = new GlyphLayout();

        for (int i = 0; i < opciones.length; i++) {
            font.setColor(i == opcionSeleccionada ? Color.YELLOW : Color.WHITE);
            String texto = (i == opcionSeleccionada ? "> " + opciones[i] + " <" : opciones[i]);
            layoutOpciones.setText(font, texto);
            float opcionX = Gdx.graphics.getWidth() / 2f - layoutOpciones.width / 2f;
            float opcionY = startY - i * 80;
            font.draw(batch, layoutOpciones, opcionX, opcionY);
        }

        // Instrucciones
        font.getData().setScale(1f);
        font.setColor(Color.GRAY);
        GlyphLayout layoutInstrucciones = new GlyphLayout(font, "Usa las flechas para navegar, ENTER para seleccionar");
        font.draw(batch, layoutInstrucciones,
                Gdx.graphics.getWidth() / 2f - layoutInstrucciones.width / 2f, 50);

        batch.end();

        // Manejo del input con retardo
        tiempoUltimoInput += delta;

        if (tiempoUltimoInput >= DELAY_INPUT) {
            if (controladorDeEntradas.isAbajo()) {
                opcionSeleccionada = (opcionSeleccionada + 1) % opciones.length;
                controladorDeAudio.reproducirSonido("menu_move");
                tiempoUltimoInput = 0;
            } else if (controladorDeEntradas.isArriba()) {
                opcionSeleccionada = (opcionSeleccionada - 1 + opciones.length) % opciones.length;
                controladorDeAudio.reproducirSonido("menu_move");
                tiempoUltimoInput = 0;
            } else if (controladorDeEntradas.isEnter()) {
                seleccionarOpcion();
                tiempoUltimoInput = 0;
            }
        }
    }

    private void seleccionarOpcion() {
        controladorDeAudio.reproducirSonido("menu_select");

        switch (opcionSeleccionada) {
            case 0: // Reanudar
            	this.controladorDeAudio.detenerTodaMusica();
            	this.controladorDeAudio.iniciarMusicaMenu();
            	game.setScreen(pantallaAnterior);
                break;
                
            case 1: // Volver al menú principal
                
            	this.controladorDeAudio.detenerTodaMusica();
            	this.controladorDeAudio.iniciarMusicaMenu();
            	game.setScreen(new PantallaMenuPrincipal(this.game, new BitmapFont(), controladorDeAudio, batch, controladorDeEntradas, this.config));
                
            	break;
                
            case 2: // Salir del juego
                Gdx.app.exit();
                break;
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        font.dispose();
    }
}
