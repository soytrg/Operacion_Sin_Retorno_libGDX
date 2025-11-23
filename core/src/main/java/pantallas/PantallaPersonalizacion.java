package pantallas;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import audio.ControladorDeAudio;
import configuracion.ConfiguracionJuego;
import jugador.ControladorEntrada;

public class PantallaPersonalizacion implements Screen {
	
    private Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private ControladorDeAudio controladorDeAudio;
    private ControladorEntrada controladorDeEntradas;
    private ConfiguracionJuego config;

    private String[] categorias = {
        "Color del personaje",
        "Skin del personaje", 
        "Arma inicial",
        "Dificultad",
        "Comenzar Juego"
    };

    private String[] colores = {"Azul", "Rojo"};
    private String[] skins = {"Normal", "Militar"};
    private String[] armas = {"Pistola", "Rifle"};
    private String[] dificultades = {"Facil", "Normal", "Dificil"};

    private int categoriaSeleccionada = 0;
    private int[] opcionesSeleccionadas = {0, 0, 0, 1}; // color, skin, arma, dificultad

    private float tiempoUltimoInput = 0;
    private final float DELAY_INPUT = 0.18f;

    public PantallaPersonalizacion(Game game, ControladorDeAudio controladorDeAudio, ConfiguracionJuego config,SpriteBatch batch, BitmapFont font, ControladorEntrada controladorDeEntradas) {
        this.game = game;
        this.controladorDeAudio = controladorDeAudio;
        this.config = config;
        this.batch = batch;
        this.font = font;
        this.controladorDeEntradas = controladorDeEntradas;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this.controladorDeEntradas); 
    }

    @Override
    public void render(float delta) {
        tiempoUltimoInput += delta;

        // Fondo
        Gdx.gl.glClearColor(0, 0, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Input
        manejarInput();

        // Dibujar
        batch.begin();

        font.setColor(Color.CYAN);
        font.getData().setScale(2.5f);
        font.draw(batch, "PERSONALIZACION", 
                  Gdx.graphics.getWidth() / 2f - 200,
                  Gdx.graphics.getHeight() - 80);

        font.getData().setScale(1.8f);
        float startY = Gdx.graphics.getHeight() / 2f + 120;

        for (int i = 0; i < categorias.length; i++) {
            float posY = startY - i * 80;

            if (i == categoriaSeleccionada) {
                font.setColor(Color.YELLOW);
                font.draw(batch, "> " + categorias[i], 50, posY);
            } else {
                font.setColor(Color.WHITE);
                font.draw(batch, categorias[i], 70, posY);
            }

            // Mostrar las opciones (solo para las primeras 4 categorías)
            if (i < 4) {
                font.getData().setScale(1.4f);
                String opcion = obtenerOpcionActual(i);
                if (i == categoriaSeleccionada) {
                    font.setColor(Color.ORANGE);
                    font.draw(batch, "< " + opcion + " >", 400, posY);
                } else {
                    font.setColor(Color.LIGHT_GRAY);
                    font.draw(batch, opcion, 420, posY);
                }
                font.getData().setScale(1.8f);
            }
        }

        // Instrucciones
        font.getData().setScale(1.1f);
        font.setColor(Color.GRAY);
        font.draw(batch, "Flechas: Navegar | ENTER: Seleccionar | ESC: Volver", 50, 60);

        batch.end();
    }

    private void manejarInput() {
        if (tiempoUltimoInput < DELAY_INPUT) return;
        
        boolean arriba = this.controladorDeEntradas.isArriba();
        boolean abajo = this.controladorDeEntradas.isAbajo();
        boolean izq = this.controladorDeEntradas.isIzquierda();
        boolean der = this.controladorDeEntradas.isDerecha();
        boolean enter = this.controladorDeEntradas.isEnter();      
        boolean esc = this.controladorDeEntradas.isEscape();
        
        
        if (arriba) {
            categoriaSeleccionada = (categoriaSeleccionada - 1 + categorias.length) % categorias.length;
            reproducirSonidoSeleccion();
            tiempoUltimoInput = 0;
        } else if (abajo) {
            categoriaSeleccionada = (categoriaSeleccionada + 1) % categorias.length;
            reproducirSonidoSeleccion();
            tiempoUltimoInput = 0;
        } else if (izq) {
            cambiarOpcionIzquierda();
            reproducirSonidoSeleccion();
            tiempoUltimoInput = 0;
        } else if (der) {
            cambiarOpcionDerecha();
            reproducirSonidoSeleccion();
            tiempoUltimoInput = 0;
        } else if (enter) {
            if (categoriaSeleccionada == 4) {
                guardarConfiguracion();
                reproducirSonidoSeleccion();
                this.controladorDeAudio.detenerTodaMusica();
                this.controladorDeAudio.iniciarMusicaNivel1();
                this.config.configurarDificultad(this.config.getDificultad());
                game.setScreen(new PantallaJuego(game, batch, controladorDeAudio,controladorDeEntradas, config));
            }
        } else if (esc) {
            reproducirSonidoSeleccion();
            // Volver al menú principal
            this.game.setScreen(new PantallaMenuPrincipal(game, font, controladorDeAudio, batch, controladorDeEntradas, config));
        }
    }

    private String obtenerOpcionActual(int categoria) {
        switch (categoria) {
            case 0: return colores[opcionesSeleccionadas[0]];
            case 1: return skins[opcionesSeleccionadas[1]];
            case 2: return armas[opcionesSeleccionadas[2]];
            case 3: return dificultades[opcionesSeleccionadas[3]];
            default: return "";
        }
    }

    private void cambiarOpcionIzquierda() {
        switch (categoriaSeleccionada) {
            case 0:
                opcionesSeleccionadas[0] = (opcionesSeleccionadas[0] - 1 + colores.length) % colores.length;
                break;
            case 1:
                opcionesSeleccionadas[1] = (opcionesSeleccionadas[1] - 1 + skins.length) % skins.length;
                break;
            case 2:
                opcionesSeleccionadas[2] = (opcionesSeleccionadas[2] - 1 + armas.length) % armas.length;
                break;
            case 3:
                opcionesSeleccionadas[3] = (opcionesSeleccionadas[3] - 1 + dificultades.length) % dificultades.length;
                break;
        }
    }

    private void cambiarOpcionDerecha() {
        switch (categoriaSeleccionada) {
            case 0:
                opcionesSeleccionadas[0] = (opcionesSeleccionadas[0] + 1) % colores.length;
                break;
            case 1:
                opcionesSeleccionadas[1] = (opcionesSeleccionadas[1] + 1) % skins.length;
                break;
            case 2:
                opcionesSeleccionadas[2] = (opcionesSeleccionadas[2] + 1) % armas.length;
                break;
            case 3:
                opcionesSeleccionadas[3] = (opcionesSeleccionadas[3] + 1) % dificultades.length;
                break;
        }
    }

    private void guardarConfiguracion() {

        config.setColorJugador(colores[opcionesSeleccionadas[0]].toLowerCase());
        config.setSkinJugador(skins[opcionesSeleccionadas[1]].toLowerCase());
        config.setArmaJugador(armas[opcionesSeleccionadas[2]].toLowerCase());
        config.setDificultad(dificultades[opcionesSeleccionadas[3]].toLowerCase());

    }

    private void reproducirSonidoSeleccion() {
        if (controladorDeAudio != null) {
            try {
                controladorDeAudio.reproducirSonido("menu_move");
            } catch (Exception ignored) {}
        }
    }

    // Métodos de Screen
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {

    }
}