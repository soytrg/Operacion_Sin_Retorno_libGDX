package pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import audio.ControladorDeAudio;
import configuracion.ConfiguracionJuego;
import jugador.ControladorEntrada;

public class PantallaGameOver implements Screen {
    
    private final Game game;
    private Stage stage;
    private Viewport viewport;
    private SpriteBatch batch;
    private ControladorDeAudio controladorAudio;
    private ControladorEntrada controladorEntradas;
    private ConfiguracionJuego config;
    private BitmapFont font;
    
    public PantallaGameOver(Game game, SpriteBatch batch, ControladorDeAudio controladorAudio, 
                            ControladorEntrada controladorEntradas, ConfiguracionJuego config) {
        this.game = game;
        this.batch = batch;
        this.controladorAudio = controladorAudio;
        this.controladorEntradas = controladorEntradas;
        this.config = config;
        this.font = new BitmapFont();
    }
    
    @Override
    public void show() {
        // Detener música del nivel
        if (controladorAudio != null) {
            controladorAudio.detenerTodaMusica();
        }
        
        // Crear viewport y stage
        viewport = new FitViewport(800, 600, new OrthographicCamera());
        stage = new Stage(viewport, batch);
        
        // Configurar input
        Gdx.input.setInputProcessor(stage);
        
        // Crear fuentes
        font.getData().setScale(2f); // Fuente más grande
        
        BitmapFont fontBotones = new BitmapFont();
        fontBotones.getData().setScale(1.5f);
        
        // Estilos
        Label.LabelStyle estiloTitulo = new Label.LabelStyle(font, Color.RED);
        Label.LabelStyle estiloTexto = new Label.LabelStyle(fontBotones, Color.WHITE);
        
        // Estilo para botones
        TextButton.TextButtonStyle estiloBotones = new TextButton.TextButtonStyle();
        estiloBotones.font = fontBotones;
        estiloBotones.fontColor = Color.WHITE;
        estiloBotones.overFontColor = Color.YELLOW;
        estiloBotones.downFontColor = Color.GRAY;
        
        // Crear labels
        Label tituloLabel = new Label("GAME OVER", estiloTitulo);
        Label mensajeLabel = new Label("Has perdido todas tus vidas", estiloTexto);
        
        // Crear botones
        TextButton botonReiniciar = new TextButton("Reiniciar Nivel", estiloBotones);
        TextButton botonMenu = new TextButton("Ir al Menu", estiloBotones);
        
        // Listeners de botones
        botonReiniciar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                reiniciarNivel();
            }
        });
        
        botonMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                irAlMenu();
            }
        });
        
        // Crear tabla para organizar elementos
        Table tabla = new Table();
        tabla.setFillParent(true);
        tabla.center();
        
        tabla.add(tituloLabel).padBottom(30);
        tabla.row();
        tabla.add(mensajeLabel).padBottom(50);
        tabla.row();
        tabla.add(botonReiniciar).width(250).height(60).padBottom(20);
        tabla.row();
        tabla.add(botonMenu).width(250).height(60);
        
        stage.addActor(tabla);
    }
    
    private void reiniciarNivel() {
        // Reiniciar el nivel actual (mantener el mismo nivel)
        game.setScreen(new PantallaJuego(game, batch, controladorAudio, controladorEntradas, config));
    }
    
    private void irAlMenu() {
        // Resetear nivel a 1
        config.setNivel(1);
        
        // Detener toda la música
        if (controladorAudio != null) {
            controladorAudio.detenerTodaMusica();
        }
        
        // IR AL MENÚ PRINCIPAL (CORREGIDO)
        game.setScreen(new PantallaMenuPrincipal(game, new BitmapFont(), controladorAudio, batch, controladorEntradas, config));
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.act(delta);
        stage.draw();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
    
    @Override
    public void pause() {
    }
    
    @Override
    public void resume() {
    }
    
    @Override
    public void hide() {
        dispose();
    }
    
    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
    }
}