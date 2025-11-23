package pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import audio.ControladorDeAudio;
import configuracion.ConfiguracionJuego;
import jugador.ControladorEntrada;

import com.badlogic.gdx.Screen;

public class PantallaMenuPrincipal implements Screen {
	
	private Game game;
    private BitmapFont font;
    private ControladorDeAudio controladorDeAudio;
    private ConfiguracionJuego config;
    private SpriteBatch batch;
    private ControladorEntrada controladorDeEntradas;
    
    private String[] opciones = {
            "Un jugador",
            "Cooperativo", 
            "Opciones",
            "Salir"
        };
        
    private int opcionSeleccionada = 0;
    private float tiempoUltimoInput = 0f;
    private final float DELAY_INPUT = 0.2f;
	
	public PantallaMenuPrincipal(Game game, BitmapFont font, ControladorDeAudio controladorDeAudio, SpriteBatch batch, ControladorEntrada controladorDeEntradas, ConfiguracionJuego config) {
		this.game = game;
		this.font = font;
        this.controladorDeAudio = controladorDeAudio;
        this.config = config;
        this.batch = batch;
        this.controladorDeEntradas = controladorDeEntradas;
	}
	

	@Override
	public void show() {
        this.controladorDeAudio.detenerTodaMusica();
        this.controladorDeAudio.cargarMusica();
        this.controladorDeAudio.iniciarMusicaMenu();
        this.controladorDeAudio.cargarSonidos("menu_move", "Efectos de sonido/menu_move.mp3");
		
		// RESETEAR LA PROYECCIÓN DEL BATCH
	    OrthographicCamera camera = new OrthographicCamera();
	    camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	    batch.setProjectionMatrix(camera.combined);
        
        Gdx.input.setInputProcessor(controladorDeEntradas);
	}

	@Override
	public void render(float delta) {
	    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	    
		batch.begin();
	    this.font.getData().setScale(3f);
	    this.font.setColor(Color.CYAN);
	    GlyphLayout layout = new GlyphLayout(this.font, "OPERACION SIN RETORNO");
	    float tituloX = Gdx.graphics.getWidth() / 2f - layout.width / 2f;
	    float tituloY = Gdx.graphics.getHeight() * 0.8f;
	    this.font.draw(batch, layout, tituloX, tituloY);

	    this.font.getData().setScale(2f);
	    float startY = Gdx.graphics.getHeight() * 0.6f;
	    GlyphLayout layoutOpciones = new GlyphLayout();
	    for (int i = 0; i < this.opciones.length; i++) {
	        this.font.setColor(i == this.opcionSeleccionada ? Color.YELLOW : Color.WHITE);
	        layoutOpciones.setText(this.font, (i == this.opcionSeleccionada ? "> " + this.opciones[i] + " <" : this.opciones[i]));
	        float opcionX = Gdx.graphics.getWidth() / 2f - layoutOpciones.width / 2f;
	        float opcionY = startY - i * 80;
	        this.font.draw(batch, layoutOpciones, opcionX, opcionY);
	    }

	    this.font.getData().setScale(1f);
	    this.font.setColor(Color.GRAY);
	    GlyphLayout layoutInstrucciones = new GlyphLayout(this.font, "Usa las flechas para navegar, ENTER para seleccionar");
	    this.font.draw(batch, layoutInstrucciones, Gdx.graphics.getWidth() / 2f - layoutInstrucciones.width / 2f, 50);
		
        batch.end();
        
        tiempoUltimoInput += delta;

        if (tiempoUltimoInput >= DELAY_INPUT) {
            if (controladorDeEntradas.isAbajo()) {
                opcionSeleccionada = (opcionSeleccionada + 1) % opciones.length;
                reproducirSonidoNavegacion();
                tiempoUltimoInput = 0;
            } else if (controladorDeEntradas.isArriba()) {
                opcionSeleccionada = (opcionSeleccionada - 1 + opciones.length) % opciones.length;
                reproducirSonidoNavegacion();
                tiempoUltimoInput = 0;
            } else if (controladorDeEntradas.isEnter()) {
                seleccionarOpcion();
                tiempoUltimoInput = 0;
            }
        }
	}
	
    private void reproducirSonidoNavegacion() {
        if (controladorDeAudio != null) controladorDeAudio.reproducirSonido("menu_move");
    }
	
    private void seleccionarOpcion() {
    	reproducirSonidoNavegacion();
        
        switch (opcionSeleccionada) {
        
            case 0: // Un jugador
                config.setModoCooperativo(false);
                config.setModoOnline(false);
                this.game.setScreen(new PantallaPersonalizacion(this.game, controladorDeAudio, config, batch, font, controladorDeEntradas));
                break;
                
            case 1: // Cooperativo
                config.setModoCooperativo(true);
                config.setModoOnline(true);
                this.game.setScreen(new PantallaLobby(this.game, batch, controladorDeAudio, controladorDeEntradas, config));
                break;
                
            case 2: // Opciones
            	this.game.setScreen(new PantallaOpciones(game, batch, font, controladorDeAudio, config, controladorDeEntradas, this));
                break;
                
            case 3: // Salir
                Gdx.app.exit();
                break;
        	}
        }

	@Override
	public void dispose() {
	    if (controladorDeAudio != null) {
	        controladorDeAudio.detenerMusicaMenu();
	        controladorDeAudio.dispose();
	    }		
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}
}