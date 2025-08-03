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

public class MenuPrincipal implements InputProcessor {
    private Principal juego;
    private BitmapFont font;
    private ControladorDeAudio controladorDeAudio;
    private ConfiguracionJuego config;
    
    private String[] opciones = {
        "Un jugador",
        "Cooperativo", 
        "Opciones",
        "Salir"
    };
    
    private int opcionSeleccionada = 0;
    private float tiempoUltimoInput = 0;
    private final float DELAY_INPUT = 0;
    
    public MenuPrincipal(Principal juego, BitmapFont font, ControladorDeAudio controladorDeAudio) {
        this.juego = juego;
        this.font = font;
        this.controladorDeAudio = controladorDeAudio;
        this.config = ConfiguracionJuego.getInstancia();
    }
    
    public void render(SpriteBatch batch) {
        batch.begin();
        
        // Título del juego
        font.setColor(Color.CYAN);
        font.getData().setScale(3.0f);
//        font.draw(batch, "OPERACION SIN RETORNO",  Gdx.graphics.getWidth() / 2 -300, Gdx.graphics.getHeight() -100);
        font.draw(batch, "OPERACION SIN RETORNO",  Gdx.graphics.getWidth() / 8 , Gdx.graphics.getHeight()/1.20f );

        // Opciones del menú
        font.getData().setScale(2.0f);
        float startY = Gdx.graphics.getHeight() / 1.50f;
        
        for (int i = 0; i < opciones.length; i++) {
            if (i == opcionSeleccionada) {
                font.setColor(Color.YELLOW);
                font.draw(batch, "> " + opciones[i] + " <",Gdx.graphics.getWidth() / 2 - 100,  startY - i * 80);
            } else {
                font.setColor(Color.WHITE);
                font.draw(batch, opciones[i], Gdx.graphics.getWidth() / 2 - 80, startY - i * 80);
            }
        }
        
        // Instrucciones
        font.getData().setScale(1.0f);
        font.setColor(Color.GRAY);
        font.draw(batch, "Usa las flechas para navegar, ENTER para seleccionar", Gdx.graphics.getWidth() / 2 - 200, 50);
        
        batch.end();
        
        // Actualizar tiempo
//        tiempoUltimoInput += Gdx.graphics.getDeltaTime();
    }
    
    @Override
    public boolean keyDown(int keycode) {
        if (tiempoUltimoInput < DELAY_INPUT) {
            return false;
        }
    	reproducirSonidoNavegacion();

        switch (keycode) {
            case Input.Keys.UP:
                opcionSeleccionada = (opcionSeleccionada - 1 + opciones.length) % opciones.length;
                tiempoUltimoInput = 0;
                return true;
                
            case Input.Keys.DOWN:
            	opcionSeleccionada = (opcionSeleccionada + 1) % opciones.length;
                tiempoUltimoInput = 0;
                return true;
                
            case Input.Keys.ENTER:
                seleccionarOpcion();
                return true;
                
            case Input.Keys.ESCAPE:
                Gdx.app.exit();
                return true;
        }
        
        return false;
    }
    
    private void seleccionarOpcion() {
    	reproducirSonidoNavegacion();
        
        switch (opcionSeleccionada) {
            case 0: // Un jugador
                config.setModoCooperativo(false);
                juego.cambiarEstado(EstadoJuego.MENU_PERSONALIZACION);
                break;
                
            case 1: // Cooperativo
                config.setModoCooperativo(true);
                juego.cambiarEstado(EstadoJuego.MENU_PERSONALIZACION);
                break;
                
            case 2: // Opciones
                juego.cambiarEstado(EstadoJuego.OPCIONES);
                break;
                
            case 3: // Salir
                Gdx.app.exit();
                break;
        }
    }
    
    private void reproducirSonidoNavegacion() {
        if (controladorDeAudio != null) controladorDeAudio.reproducirSonido("menu_move");
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