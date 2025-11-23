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

public class PantallaOpciones implements Screen {

 	private Game game;
    private BitmapFont font;
    private Screen pantallaAnterior;
    private ControladorDeAudio controladorDeAudio;
    private ControladorEntrada controladorDeEntradas;
    private ConfiguracionJuego config;
    private SpriteBatch batch;

    private String[] opciones = {
        "Volumen Música",
        "Volumen Efectos",
        "Controles",
        "Volver"
    };
    
    private int opcionSeleccionada = 0;
    private float tiempoUltimoInput = 0;
    private final float DELAY_INPUT = 0.2f;
    
    public PantallaOpciones(Game game, SpriteBatch batch, BitmapFont font, ControladorDeAudio controladorDeAudio, ConfiguracionJuego config, ControladorEntrada controladorDeEntradas, Screen pantallaAnterior)
    {
    	this.game = game;
        this.font = font;
        this.batch = batch;
        this.controladorDeAudio = controladorDeAudio;
        this.config = config;
        this.controladorDeEntradas = controladorDeEntradas;
        this.pantallaAnterior = pantallaAnterior;
    }
    
    
	@Override
	public void show() {
		Gdx.input.setInputProcessor(controladorDeEntradas);
	}

	@Override
	public void render(float delta) {
		
	    Gdx.gl.glClearColor(0, 0, 0, 1); // Fondo negro
	    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	    
		float startY = Gdx.graphics.getHeight();
    	float startX = Gdx.graphics.getWidth();
        batch.begin();
        
        // Título
        font.setColor(Color.CYAN);
        font.getData().setScale(2.5f);
        font.draw(batch, "OPCIONES", 
                 Gdx.graphics.getWidth() / 2 - 100, 
                 Gdx.graphics.getHeight() - 80);
        
        // Opciones del menú
        font.getData().setScale(1.8f);
        
        
        for (int i = 0; i < opciones.length; i++) {
            float posY = startY / 2 + 150 - i * 80;
            
            if (i == opcionSeleccionada) {
                font.setColor(Color.YELLOW);
                font.draw(batch, "> " + opciones[i], 100, posY);
            } else {
                font.setColor(Color.WHITE);
                font.draw(batch, opciones[i], 120, posY);
            }
            
            // Mostrar valores actuales
            font.getData().setScale(1.4f);
            switch (i) {
                case 0: // Volumen Música
                    if (i == opcionSeleccionada) {
                        font.setColor(Color.ORANGE);
                        font.draw(batch, "< " + (int)(config.getVolumenMusica() * 100) + "% >", 400, posY);
                    } else {
                        font.setColor(Color.LIGHT_GRAY);
                        font.draw(batch, (int)(config.getVolumenMusica() * 100) + "%", 420, posY);
                    }
                    break;
                    
                case 1: // Volumen Efectos
                    if (i == opcionSeleccionada) {
                        font.setColor(Color.ORANGE);
                        font.draw(batch, "< " + (int)(config.getVolumenEfectos() * 100) + "% >", 400, posY);
                    } else {
                        font.setColor(Color.LIGHT_GRAY);
                        font.draw(batch, (int)(config.getVolumenEfectos() * 100) + "%", 420, posY);
                    }
                    break;
                    
                case 2: // Controles
                    if (i == opcionSeleccionada) {
                        font.setColor(Color.ORANGE);
                        font.draw(batch, "< Ver >", 400, posY);
                    } else {
                        font.setColor(Color.LIGHT_GRAY);
                        font.draw(batch, "Ver", 420, posY);
                    }
                    break;
            }
            font.getData().setScale(1.8f);
        }
        
        // Información de controles (solo si está seleccionada esa opción)
        if (opcionSeleccionada == 2) {
            font.getData().setScale(1.2f);
            font.setColor(Color.CYAN);
            font.draw(batch, "CONTROLES:", startX/1.5f, startY/1.1f);
            
            font.getData().setScale(1.0f);
            font.setColor(Color.WHITE);
            font.draw(batch, "A / D: Mover izquierda/derecha",  startX/1.5f, startY/1.15f);
            font.draw(batch, "SPACE: Saltar",  startX/1.5f, startY/1.2f);
            font.draw(batch, "W + A/D: Disparar en diagonal",  startX/1.5f, startY/1.25f);
            font.draw(batch, "S: Agacharse",  startX/1.5f, startY/1.3f);
            font.draw(batch, "ESC: Pausar (en juego)",  startX/1.5f, startY/1.35f);
        }
        
        // Instrucciones
        font.getData().setScale(1.0f);
        font.setColor(Color.GRAY);
        font.draw(batch, "Flechas Arriba/Abajo: Navegar", 50, 80);
        font.draw(batch, "Flechas Izquierda/Derecha: Cambiar valores", 50, 50);
        font.draw(batch, "ENTER: Seleccionar | ESC: Volver", 50, 20);
        
        batch.end();
        
        // Actualizar tiempo
        tiempoUltimoInput += Gdx.graphics.getDeltaTime();
        
        verificarTeclaPresionada();
		
	}
	
	
//	private void verificarTeclaPresionada()
//	{
//		if(this.controladorDeEntradas.isArriba()) {
//			  opcionSeleccionada = (opcionSeleccionada - 1 + opciones.length) % opciones.length;
//              reproducirSonidoSeleccion();
//
//              tiempoUltimoInput = 0;
//		} else if(this.controladorDeEntradas.isAbajo()) {
//			  opcionSeleccionada = (opcionSeleccionada + 1) % opciones.length;
//              reproducirSonidoSeleccion();
//
//              tiempoUltimoInput = 0;  
//		} else if(this.controladorDeEntradas.isIzquierda()) {
//			 cambiarValorIzquierda();
//             tiempoUltimoInput = 0;
//		} else if(this.controladorDeEntradas.isDerecha()) {
//			 cambiarValorDerecha();
//             tiempoUltimoInput = 0;
//		} else if(this.controladorDeEntradas.isEnter()) {
//            if (opcionSeleccionada == 3) { // Volver
//            	this.game.setScreen(this.pantallaAnterior);
//          }
//          reproducirSonidoSeleccion();
//		} else if(this.controladorDeEntradas.isEscape()) {
//			
//		}
//		
//	}
	
	private void verificarTeclaPresionada() {
	    // si no pasó el delay, no procesamos
	    if (tiempoUltimoInput < DELAY_INPUT) return;

	    if (this.controladorDeEntradas.isArriba()) {
	        opcionSeleccionada = (opcionSeleccionada - 1 + opciones.length) % opciones.length;
	        reproducirSonidoSeleccion();
	        tiempoUltimoInput = 0;

	    } else if (this.controladorDeEntradas.isAbajo()) {
	        opcionSeleccionada = (opcionSeleccionada + 1) % opciones.length;
	        reproducirSonidoSeleccion();
	        tiempoUltimoInput = 0;

	    } else if (this.controladorDeEntradas.isIzquierda()) {
	        cambiarValorIzquierda();
	        tiempoUltimoInput = 0;

	    } else if (this.controladorDeEntradas.isDerecha()) {
	        cambiarValorDerecha();
	        tiempoUltimoInput = 0;

	    } else if (this.controladorDeEntradas.isEnter()) {
	        if (opcionSeleccionada == 3) { // Volver
	            this.game.setScreen(this.pantallaAnterior);
	        }
	        reproducirSonidoSeleccion();
	        tiempoUltimoInput = 0;
	    }
	}
	
   private void cambiarValorIzquierda() {
        switch (opcionSeleccionada) {
            case 0: // Volumen Música
                float nuevoVolumenMusica = Math.max(0.0f, config.getVolumenMusica() - 0.1f);
                config.setVolumenMusica(nuevoVolumenMusica);
                if (controladorDeAudio != null) {
                	controladorDeAudio.bajarVolumenMusica();
                }
                reproducirSonidoSeleccion();

                break;
                
            case 1: // Volumen Efectos
                float nuevoVolumenEfectos = Math.max(0.0f, config.getVolumenEfectos() - 0.1f);
                config.setVolumenEfectos(nuevoVolumenEfectos);
                if (controladorDeAudio != null) {
                	controladorDeAudio.bajarVolumenSonidos();
                }
                reproducirSonidoSeleccion();

                break;
        }
    }
	
	private void cambiarValorDerecha() {
        switch (opcionSeleccionada) {
            case 0: // Volumen Música
                float nuevoVolumenMusica = Math.min(1.0f, config.getVolumenMusica() + 0.1f);
                config.setVolumenMusica(nuevoVolumenMusica);
                if (controladorDeAudio != null) {
                	controladorDeAudio.subirVolumenMusica();
                }
                reproducirSonidoSeleccion();

                break;
                
            case 1: // Volumen Efectos
                float nuevoVolumenEfectos = Math.min(1.0f, config.getVolumenEfectos() + 0.1f);
                config.setVolumenEfectos(nuevoVolumenEfectos);
                if (controladorDeAudio != null) {
                	controladorDeAudio.subirVolumenSonidos();
                }
                reproducirSonidoSeleccion();

                break;
        }
    }
	
    private void reproducirSonidoSeleccion() {
        if (controladorDeAudio != null) {
            try {
                controladorDeAudio.reproducirSonido("menu_move");
            } catch (Exception e) {
               System.out.println("Sonido NO disponible");
            }
        }
    }

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
