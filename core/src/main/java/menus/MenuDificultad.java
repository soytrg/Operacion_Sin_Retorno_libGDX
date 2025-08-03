package menus;

import com.MiEmpresa.OperacionSinRetorno.Principal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import audio.ControladorDeAudio;
import configuracion.ConfiguracionJuego;
import configuracion.EstadoJuego;

public class MenuDificultad implements InputProcessor {
    private Principal juego;
    private BitmapFont font;
    private ControladorDeAudio controladorDeAudio;
    private ConfiguracionJuego config;
    
    private ConfiguracionJuego.Dificultad[] dificultades = {
        ConfiguracionJuego.Dificultad.FACIL,
        ConfiguracionJuego.Dificultad.NORMAL,
        ConfiguracionJuego.Dificultad.DIFICIL
    };
    
    private String[] nombresDificultad = {"Fácil", "Normal", "Difícil"};
    private String[] descripcionesDificultad = {
        "5 vidas, 3 enemigos, muchos power-ups",
        "3 vidas, 5 enemigos, algunos power-ups", 
        "1 vida, 8 enemigos, pocos power-ups"
    };
    
    private int dificultadSeleccionada = 1; // Normal por defecto
    private float tiempoUltimoInput = 0;
    private final float DELAY_INPUT = 0.2f;
    
    public MenuDificultad(Principal juego, BitmapFont font, ControladorDeAudio controladorDeAudio) {
        this.juego = juego;
        this.font = font;
        this.controladorDeAudio = controladorDeAudio;
        this.config = ConfiguracionJuego.getInstancia();
    }
    
    public void render(SpriteBatch batch) {
        batch.begin();
        
        // Título
        font.setColor(Color.CYAN);
        font.getData().setScale(2.5f);
        font.draw(batch, "SELECCIONAR DIFICULTAD", 
                 Gdx.graphics.getWidth() / 2 - 250, 
                 Gdx.graphics.getHeight() - 80);
        
        // Información del jugador
        font.getData().setScale(1.3f);
        font.setColor(Color.GREEN);
        String modo = config.isModoCooperativo() ? "Modo: Cooperativo" : "Modo: Un jugador";
        font.draw(batch, modo, 50, Gdx.graphics.getHeight() - 140);
        
        font.setColor(Color.YELLOW);
        font.draw(batch, "Personaje: " + config.getColorJugador() + " " + config.getSkinJugador(), 
                 50, Gdx.graphics.getHeight() - 170);
        font.draw(batch, "Arma: " + config.getArmaJugador(), 
                 50, Gdx.graphics.getHeight() - 200);
        
        // Opciones de dificultad
        font.getData().setScale(2.0f);
        float startY = Gdx.graphics.getHeight() / 2 + 100;
        
        for (int i = 0; i < nombresDificultad.length; i++) {
            float posY = startY - i * 120;
            
            if (i == dificultadSeleccionada) {
                font.setColor(Color.YELLOW);
                font.draw(batch, "> " + nombresDificultad[i] + " <", 
                         Gdx.graphics.getWidth() / 2 - 100, posY);
                
                // Descripción de la dificultad seleccionada
                font.getData().setScale(1.2f);
                font.setColor(Color.ORANGE);
                font.draw(batch, descripcionesDificultad[i], 
                         Gdx.graphics.getWidth() / 2 - 200, posY - 40);
                font.getData().setScale(2.0f);
            } else {
                font.setColor(Color.WHITE);
                font.draw(batch, nombresDificultad[i], 
                         Gdx.graphics.getWidth() / 2 - 80, posY);
            }
        }
        
        // Botón continuar
        font.getData().setScale(1.5f);
        font.setColor(Color.LIME);
        font.draw(batch, "Presiona ENTER para comenzar", 
                 Gdx.graphics.getWidth() / 2 - 150, 150);
        
        // Instrucciones
        font.getData().setScale(1.0f);
        font.setColor(Color.GRAY);
        font.draw(batch, "Flechas Arriba/Abajo: Cambiar dificultad", 50, 80);
        font.draw(batch, "ENTER: Comenzar juego | ESC: Volver", 50, 50);
        
        batch.end();
        
        // Actualizar tiempo
        tiempoUltimoInput += Gdx.graphics.getDeltaTime();
    }
    
    @Override
    public boolean keyDown(int keycode) {
        if (tiempoUltimoInput < DELAY_INPUT) {
            return false;
        }
        
        switch (keycode) {
            case Input.Keys.UP:
                dificultadSeleccionada = (dificultadSeleccionada - 1 + dificultades.length) % dificultades.length;
                reproducirSonidoSeleccion();

                tiempoUltimoInput = 0;
                return true;
                
            case Input.Keys.DOWN:
                dificultadSeleccionada = (dificultadSeleccionada + 1) % dificultades.length;
                reproducirSonidoSeleccion();

                tiempoUltimoInput = 0;
                return true;
                
            case Input.Keys.ENTER:
                // Configurar dificultad y comenzar el juego
                config.configurarDificultad(dificultades[dificultadSeleccionada]);
                reproducirSonidoSeleccion();

                juego.iniciarJuego();
                return true;
                
            case Input.Keys.ESCAPE:
                juego.cambiarEstado(EstadoJuego.MENU_PERSONALIZACION);
                return true;
        }
        
        return false;
    }
    
   
    private void reproducirSonidoSeleccion() {
        if (controladorDeAudio != null) {
            try {
                controladorDeAudio.reproducirSonido("menu_move");
            } catch (Exception e) {
                // Sonido no disponible
            }
        }
    }
    
    // Métodos requeridos por InputProcessor
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
}