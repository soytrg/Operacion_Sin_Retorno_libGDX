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

public class MenuPersonalizacion implements InputProcessor {
    private Principal juego;
    private BitmapFont font;
    private ControladorDeAudio controladorDeAudio;
    private ConfiguracionJuego config;
    
    private String[] categorias = {
        "Color del personaje",
        "Skin del personaje", 
        "Arma inicial",
        "Continuar"
    };
    
    private String[] colores = {"Azul", "Rojo", "Verde", "Amarillo", "Morado"};
    private String[] skins = {"Normal", "Militar", "Espacial", "Ninja"};
    private String[] armas = {"Pistola", "Rifle", "Escopeta", "Laser"};
    
    private int categoriaSeleccionada = 0;
    private int[] opcionesSeleccionadas = {0, 0, 0}; // índices para color, skin, arma
    
    private float tiempoUltimoInput = 0;
    private final float DELAY_INPUT = 0.2f;
    
    public MenuPersonalizacion(Principal juego, BitmapFont font, ControladorDeAudio controladorDeAudio) {
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
        font.draw(batch, "PERSONALIZACION", 
                 Gdx.graphics.getWidth() / 2 - 200, 
                 Gdx.graphics.getHeight() - 80);
        
        // Modo de juego actual
        font.getData().setScale(1.5f);
        font.setColor(Color.GREEN);
        String modo = config.isModoCooperativo() ? "Modo: Cooperativo" : "Modo: Un jugador";
        font.draw(batch, modo, 
                 Gdx.graphics.getWidth() / 2 - 100, 
                 Gdx.graphics.getHeight() - 140);
        
        // Opciones de personalización
        font.getData().setScale(1.8f);
        float startY = Gdx.graphics.getHeight() / 2 + 120;
        
        for (int i = 0; i < categorias.length; i++) {
            float posY = startY - i * 80;
            
            if (i == categoriaSeleccionada) {
                font.setColor(Color.YELLOW);
                font.draw(batch, "> " + categorias[i], 50, posY);
            } else {
                font.setColor(Color.WHITE);
                font.draw(batch, categorias[i], 70, posY);
            }
            
            // Mostrar opciones disponibles para cada categoría
            if (i < 3) { // Solo para las categorías de personalización
                font.getData().setScale(1.4f);
                String opcionActual = obtenerOpcionActual(i);
                
                if (i == categoriaSeleccionada) {
                    font.setColor(Color.ORANGE);
                    font.draw(batch, "< " + opcionActual + " >", 400, posY);
                } else {
                    font.setColor(Color.LIGHT_GRAY);
                    font.draw(batch, opcionActual, 420, posY);
                }
                font.getData().setScale(1.8f);
            }
        }
        
        // Instrucciones
        font.getData().setScale(1.0f);
        font.setColor(Color.GRAY);
        font.draw(batch, "Flechas Arriba/Abajo: Navegar categorías", 50, 120);
        font.draw(batch, "Flechas Izquierda/Derecha: Cambiar opciones", 50, 90);
        font.draw(batch, "ENTER: Continuar | ESC: Volver", 50, 60);
        
        batch.end();
        
        // Actualizar tiempo
        tiempoUltimoInput += Gdx.graphics.getDeltaTime();
    }
    
    private String obtenerOpcionActual(int categoria) {
        switch (categoria) {
            case 0: return colores[opcionesSeleccionadas[0]];
            case 1: return skins[opcionesSeleccionadas[1]];
            case 2: return armas[opcionesSeleccionadas[2]];
            default: return "";
        }
    }
    
    @Override
    public boolean keyDown(int keycode) {
        if (tiempoUltimoInput < DELAY_INPUT) {
            return false;
        }
        
        switch (keycode) {
            case Input.Keys.UP:
                categoriaSeleccionada = (categoriaSeleccionada - 1 + categorias.length) % categorias.length;
                reproducirSonidoSeleccion();

                tiempoUltimoInput = 0;
                return true;
                
            case Input.Keys.DOWN:
                categoriaSeleccionada = (categoriaSeleccionada + 1) % categorias.length;
                reproducirSonidoSeleccion();
                tiempoUltimoInput = 0;
                return true;
                
            case Input.Keys.LEFT:
                if (categoriaSeleccionada < 3) {
                    cambiarOpcionIzquierda();
                    reproducirSonidoSeleccion();
                    tiempoUltimoInput = 0;
                }
                return true;
                
            case Input.Keys.RIGHT:
                if (categoriaSeleccionada < 3) {
                    cambiarOpcionDerecha();
                    
                    reproducirSonidoSeleccion();
                    tiempoUltimoInput = 0;
                }
                return true;
                
            case Input.Keys.ENTER:
                if (categoriaSeleccionada == 3) { // Continuar
                    guardarConfiguracion();
                    juego.cambiarEstado(EstadoJuego.MENU_DIFICULTAD);
                }
            reproducirSonidoSeleccion();
                return true;
                
            case Input.Keys.ESCAPE:
                juego.cambiarEstado(EstadoJuego.MENU_PRINCIPAL);
                return true;
        }
        
        return false;
    }
    
    private void cambiarOpcionIzquierda() {
        switch (categoriaSeleccionada) {
            case 0: // Color
                opcionesSeleccionadas[0] = (opcionesSeleccionadas[0] - 1 + colores.length) % colores.length;
                break;
            case 1: // Skin
                opcionesSeleccionadas[1] = (opcionesSeleccionadas[1] - 1 + skins.length) % skins.length;
                break;
            case 2: // Arma
                opcionesSeleccionadas[2] = (opcionesSeleccionadas[2] - 1 + armas.length) % armas.length;
                break;
        }
    }
    
    private void cambiarOpcionDerecha() {
        switch (categoriaSeleccionada) {
            case 0: // Color
                opcionesSeleccionadas[0] = (opcionesSeleccionadas[0] + 1) % colores.length;
                break;
            case 1: // Skin
                opcionesSeleccionadas[1] = (opcionesSeleccionadas[1] + 1) % skins.length;
                break;
            case 2: // Arma
                opcionesSeleccionadas[2] = (opcionesSeleccionadas[2] + 1) % armas.length;
                break;
        }
    }
    
    private void guardarConfiguracion() {
        config.setColorJugador(colores[opcionesSeleccionadas[0]].toLowerCase());
        config.setSkinJugador(skins[opcionesSeleccionadas[1]].toLowerCase());
        config.setArmaJugador(armas[opcionesSeleccionadas[2]].toLowerCase());
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
