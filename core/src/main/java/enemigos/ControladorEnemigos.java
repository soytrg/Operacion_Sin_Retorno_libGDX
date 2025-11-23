package enemigos;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import jugador.Jugador;
import jugador.JugadorRemoto;
import jugador.JugadorRemotoControlable;
import bala.Bala;
import audio.ControladorDeAudio;
import powerups.PowerUp;
import configuracion.ConfiguracionJuego;
import java.util.ArrayList;

public class ControladorEnemigos {
    
    private ArrayList<Enemigo> enemigos = new ArrayList<>();
    private ArrayList<Enemigo> enemigosParaEliminar = new ArrayList<>();
    private ControladorDeAudio controladorAudio;
    private ConfiguracionJuego config;
    
    private ArrayList<PowerUp> powerUpsDropeados = new ArrayList<>();
    
    private boolean nuclearActivo = false;
    
    // 🔥 PROTECCIÓN CONTRA ATAQUES DUPLICADOS
    private float tiempoUltimoAtaqueJugador1 = 0f;
    private float tiempoUltimoAtaqueJugador2 = 0f;
    private final float COOLDOWN_ATAQUE = 0.5f; // 500ms entre ataques
    
    public ControladorEnemigos() {
    }
    
    public void setConfiguracion(ConfiguracionJuego config) {
        this.config = config;
    }
    
    public void setControladorAudio(ControladorDeAudio controlador) {
        this.controladorAudio = controlador;
    }
    
    public int getCantidadEnemigosVivos() {
        int count = 0;
        for (Enemigo enemigo : enemigos) {
            if (enemigo.estaVivo()) count++;
        }
        return count;
    }
    
    public void limpiar() {
        for (Enemigo enemigo : enemigos) {
            if (enemigo instanceof Canibal) {
                ((Canibal) enemigo).dispose();
            } else if (enemigo instanceof SoldadoNazi) {
                ((SoldadoNazi) enemigo).dispose();
            }
        }
        
        for (PowerUp powerUp : powerUpsDropeados) {
            powerUp.dispose();
        }
        powerUpsDropeados.clear();
        
        enemigos.clear();
        enemigosParaEliminar.clear();
    }
    
    public ArrayList<Enemigo> getEnemigos() {
        return enemigos;
    }
    
    public ArrayList<PowerUp> getPowerUpsDropeados() {
        return powerUpsDropeados;
    }
    
    public void agregarCanibal(float x, float y) {
        Canibal canibal = new Canibal(x, y);
        
        if (controladorAudio != null) {
            canibal.setControladorAudio(controladorAudio);
        }
        
        enemigos.add(canibal);
    }
    
    public void agregarSoldadoNazi(float x, float y) {
        SoldadoNazi nazi = new SoldadoNazi(x, y);
        
        if (controladorAudio != null) {
            nazi.setControladorAudio(controladorAudio);
        }
        
        enemigos.add(nazi);
    }
    
    public void activarNuclear() {
        System.out.println("☢️ NUCLEAR ACTIVADO - Eliminando todos los enemigos");
        
        nuclearActivo = true;
        
        int canibalesMuertos = 0;
        int nazisMuertos = 0;
        
        for (Enemigo enemigo : enemigos) {
            if (!enemigo.estaVivo()) continue;
            
            if (enemigo instanceof SoldadoNazi) {
                enemigo.recibirDaño(999999, false);
                nazisMuertos++;
                System.out.println("💀 Nazi eliminado por nuclear");
            } else if (enemigo instanceof Canibal) {
                enemigo.recibirDaño(999999, false);
                canibalesMuertos++;
                System.out.println("💀 Caníbal eliminado por nuclear");
            }
        }
        
        nuclearActivo = false;
        
        System.out.println("☢️ Nuclear completado: " + nazisMuertos + " nazis y " + canibalesMuertos + " caníbales eliminados");
    }
    
    public void actualizar(float delta, Jugador jugadorLocal, JugadorRemoto jugadorRemoto, TiledMapTileLayer capa, float tiempoTotal) {
        float jugador1X = jugadorLocal.getX() + jugadorLocal.getAncho() / 2f;
        float jugador1Y = jugadorLocal.getY() + jugadorLocal.getAlto() / 2f;
        
        float jugador2X = 0, jugador2Y = 0;
        boolean hayJugador2 = (jugadorRemoto != null);
        
        if (hayJugador2) {
            jugador2X = jugadorRemoto.getX() + jugadorRemoto.getAncho() / 2f;
            jugador2Y = jugadorRemoto.getY() + jugadorRemoto.getAlto() / 2f;
        }

        boolean jugadorLocalOculto = jugadorLocal.estaOcultoEnArbusto();
        boolean jugadorRemotoOculto = hayJugador2 && jugadorRemoto.estaOcultoEnArbusto();
        
        for (Enemigo enemigo : enemigos) {
            float targetX, targetY;
            boolean objetivoOculto;
            
            if (!hayJugador2) {
                targetX = jugador1X;
                targetY = jugador1Y;
                objetivoOculto = jugadorLocalOculto;
            } else {
                float dist1 = (float) Math.sqrt(
                    Math.pow(enemigo.getX() - jugador1X, 2) + 
                    Math.pow(enemigo.getY() - jugador1Y, 2)
                );
                
                float dist2 = (float) Math.sqrt(
                    Math.pow(enemigo.getX() - jugador2X, 2) + 
                    Math.pow(enemigo.getY() - jugador2Y, 2)
                );
                
                if (jugadorLocalOculto && !jugadorRemotoOculto) {
                    targetX = jugador2X;
                    targetY = jugador2Y;
                    objetivoOculto = false;
                } else if (!jugadorLocalOculto && jugadorRemotoOculto) {
                    targetX = jugador1X;
                    targetY = jugador1Y;
                    objetivoOculto = false;
                } else if (jugadorLocalOculto && jugadorRemotoOculto) {
                    targetX = jugador1X;
                    targetY = jugador1Y;
                    objetivoOculto = true;
                } else {
                    if (dist1 <= dist2) {
                        targetX = jugador1X;
                        targetY = jugador1Y;
                        objetivoOculto = false;
                    } else {
                        targetX = jugador2X;
                        targetY = jugador2Y;
                        objetivoOculto = false;
                    }
                }
            }
            
            enemigo.actualizar(delta, targetX, targetY, capa, tiempoTotal, objetivoOculto);
            
            if (enemigo instanceof SoldadoNazi) {
                SoldadoNazi nazi = (SoldadoNazi) enemigo;
                if (nazi.debeSerEliminado()) {
                    enemigosParaEliminar.add(enemigo);
                }
            } else if (!enemigo.estaVivo()) {
                enemigosParaEliminar.add(enemigo);
            }
        }
        
        for (Enemigo enemigo : enemigosParaEliminar) {
            if (enemigo instanceof Canibal) {
                ((Canibal) enemigo).dispose();
            } else if (enemigo instanceof SoldadoNazi) {
                ((SoldadoNazi) enemigo).dispose();
            }
            enemigos.remove(enemigo);
        }
        enemigosParaEliminar.clear();
        
        ArrayList<PowerUp> powerUpsParaEliminar = new ArrayList<>();
        for (PowerUp powerUp : powerUpsDropeados) {
            powerUp.actualizar(delta);
            if (powerUp.debeSerEliminado()) {
                powerUpsParaEliminar.add(powerUp);
            }
        }
        
        for (PowerUp powerUp : powerUpsParaEliminar) {
            powerUp.dispose();
            powerUpsDropeados.remove(powerUp);
        }
    }
    
    public void actualizar(float delta, Jugador jugador, TiledMapTileLayer capa, float tiempoTotal) {
        actualizar(delta, jugador, null, capa, tiempoTotal);
    }
    
    private void generarPowerUpAleatorio(float x, float y) {
        PowerUp.TipoPowerUp tipo = PowerUp.generarPowerUpAleatorio();
        
        if (tipo != null) {
            PowerUp powerUp = new PowerUp(x, y, tipo);
            powerUpsDropeados.add(powerUp);
            
            String nombrePowerUp = "";
            switch(tipo) {
                case NUCLEAR: nombrePowerUp = "☢️ NUCLEAR"; break;
                case INSTAKILL: nombrePowerUp = "💀 INSTAKILL"; break;
                case RECARGA_RAPIDA: nombrePowerUp = "⚡ RECARGA RÁPIDA"; break;
            }
            
            System.out.println("🎁 " + nombrePowerUp + " dropeado en (" + (int)x + ", " + (int)y + ")");
        }
    }
    
    private void otorgarPuntos(Object jugador, Enemigo enemigo, int idJugador) {
        if (nuclearActivo || jugador == null) {
            return;
        }
        
        int puntos = 0;
        
        if (enemigo instanceof Canibal) {
            puntos = 50;
        } else if (enemigo instanceof SoldadoNazi) {
            puntos = 75;
        }
        
        if (puntos > 0) {
            if (jugador instanceof Jugador) {
                ((Jugador) jugador).sumarPuntos(puntos);
                System.out.println("💰 JUGADOR " + (idJugador + 1) + " ganó " + puntos + " puntos");
            } else if (jugador instanceof JugadorRemotoControlable) {
                ((JugadorRemotoControlable) jugador).sumarPuntos(puntos);
                System.out.println("💰 JUGADOR REMOTO " + (idJugador + 1) + " ganó " + puntos + " puntos");
            }
        }
    }
    
    private void otorgarMunicionNazi(Object jugador) {
        if (nuclearActivo || jugador == null) {
            return;
        }
        
        if (jugador instanceof Jugador) {
            ((Jugador) jugador).agregarMunicionGlock(10);
            System.out.println("📦 +10 balas de Glock al jugador local");
        } else if (jugador instanceof JugadorRemotoControlable) {
            ((JugadorRemotoControlable) jugador).agregarMunicionGlock(10);
            System.out.println("📦 +10 balas de Glock al jugador remoto");
        }
    }
    
    public ResultadoColisionBalas verificarColisionesBalas(ArrayList<Bala> balas, Jugador jugadorLocal, JugadorRemoto jugadorRemoto) {
        float sanidadRecuperada = 0f;
        float saludMentalRecuperada = 0f;
        
        boolean instaKillLocal = jugadorLocal.tieneInstaKill();
        boolean instaKillRemoto = (jugadorRemoto != null) ? jugadorRemoto.tieneInstaKill() : false;
        
        int idJugadorLocal = (config != null) ? config.getIdJugador() : 0;
        boolean esHost = (config != null) && config.esHost();
        
        for (int i = 0; i < balas.size(); i++) {
            Bala bala = balas.get(i);
            if (!bala.estaActiva()) continue;
            
            Rectangle rectBala = new Rectangle(bala.getX(), bala.getY(), bala.getAncho(), bala.getAlto());
            
            for (Enemigo enemigo : enemigos) {
                if (!enemigo.estaVivo()) continue;
                
                if (rectBala.overlaps(enemigo.getBounds())) {
                    boolean estabaVivo = enemigo.estaVivo();
                    
                    int idDueñoBala = bala.getIdDueño();
                    
                    boolean instaKillActivo = false;
                    int dañoAplicar = 0;
                    
                    if (idDueñoBala == idJugadorLocal) {
                        instaKillActivo = instaKillLocal;
                        dañoAplicar = jugadorLocal.getDaño();
                        
                    } else if (esHost && idDueñoBala == 1 && jugadorRemoto instanceof JugadorRemotoControlable) {
                        JugadorRemotoControlable jugadorControlable = (JugadorRemotoControlable) jugadorRemoto;
                        instaKillActivo = instaKillRemoto;
                        dañoAplicar = jugadorControlable.getDaño();
                    }
                    
                    if (instaKillActivo) {
                        enemigo.recibirDaño(999999, true);
                    } else {
                        enemigo.recibirDaño(dañoAplicar, true);
                    }
                    
                    bala.desactivar();
                    
                    if (estabaVivo && !enemigo.estaVivo() && enemigo.fueAsinadoADistancia()) {
                        generarPowerUpAleatorio(enemigo.getX(), enemigo.getY());
                        
                        if (idDueñoBala == idJugadorLocal) {
                            otorgarPuntos(jugadorLocal, enemigo, idJugadorLocal);
                            
                            if (enemigo instanceof SoldadoNazi) {
                                otorgarMunicionNazi(jugadorLocal);
                            }
                            
                            if (enemigo instanceof Canibal) {
                                sanidadRecuperada += ((Canibal) enemigo).getSanidadRecuperada();
                                saludMentalRecuperada += 5f;
                            } else if (enemigo instanceof SoldadoNazi) {
                                sanidadRecuperada += ((SoldadoNazi) enemigo).getSanidadRecuperada();
                                saludMentalRecuperada += 10f;
                            }
                            
                        } else if (esHost && idDueñoBala == 1 && jugadorRemoto instanceof JugadorRemotoControlable) {
                            JugadorRemotoControlable jugadorControlable = (JugadorRemotoControlable) jugadorRemoto;
                            otorgarPuntos(jugadorControlable, enemigo, 1);
                            
                            if (enemigo instanceof SoldadoNazi) {
                                otorgarMunicionNazi(jugadorControlable);
                            }
                        }
                    }
                    break;
                }
            }
        }
        
        return new ResultadoColisionBalas(sanidadRecuperada, saludMentalRecuperada);
    }
    
    public ResultadoColisionBalas verificarColisionesBalas(ArrayList<Bala> balas, Jugador jugador) {
        return verificarColisionesBalas(balas, jugador, null);
    }
    
    public void verificarColisionesCuchillo(Jugador jugadorLocal, JugadorRemoto jugadorRemoto) {
        int idJugadorLocal = (config != null) ? config.getIdJugador() : 0;
        
        if (jugadorLocal.estaAtacandoConCuchillo()) {
            procesarAtaqueCuchillo(jugadorLocal, jugadorLocal.getRangoCuchillo(), jugadorLocal.tieneInstaKill(), jugadorLocal.getDañoCuchillo(), idJugadorLocal);
            jugadorLocal.resetearAtaqueCuchillo();
        }
        
        if (jugadorRemoto != null && jugadorRemoto instanceof JugadorRemotoControlable) {
            JugadorRemotoControlable jugadorControlable = (JugadorRemotoControlable) jugadorRemoto;
            
            if (jugadorControlable.estaAtacandoConCuchillo()) {
                Rectangle rangoCuchilloRemoto = jugadorControlable.getRangoCuchillo();
                boolean instaKillRemoto = jugadorControlable.tieneInstaKill();
                int dañoCuchilloRemoto = jugadorControlable.getDañoCuchillo();
                
                procesarAtaqueCuchillo(jugadorControlable, rangoCuchilloRemoto, instaKillRemoto, dañoCuchilloRemoto, 1);
                jugadorControlable.resetearAtaqueCuchillo();
            }
        }
    }
    
    private void procesarAtaqueCuchillo(Object jugador, Rectangle rangoCuchillo, boolean instaKillActivo, int dañoCuchillo, int idJugador) {
        for (Enemigo enemigo : enemigos) {
            if (!enemigo.estaVivo()) continue;
            
            if (rangoCuchillo.overlaps(enemigo.getBounds())) {
                boolean estabaVivo = enemigo.estaVivo();
                
                if (instaKillActivo) {
                    enemigo.recibirDaño(999999, false);
                } else {
                    enemigo.recibirDaño(dañoCuchillo, false);
                }
                
                if (estabaVivo && !enemigo.estaVivo()) {
                    generarPowerUpAleatorio(enemigo.getX(), enemigo.getY());
                    
                    otorgarPuntos(jugador, enemigo, idJugador);
                    
                    if (enemigo instanceof Canibal) {
                        if (!instaKillActivo) {
                            if (jugador instanceof Jugador) {
                                ((Jugador) jugador).penalizarMatarCanibalConCuchillo();
                            } else if (jugador instanceof JugadorRemotoControlable) {
                                ((JugadorRemotoControlable) jugador).penalizarMatarCanibalConCuchillo();
                            }
                        }
                        
                    } else if (enemigo instanceof SoldadoNazi) {
                        otorgarMunicionNazi(jugador);
                    }
                }
            }
        }
    }
    
    public void verificarColisionesCuchillo(Jugador jugador) {
        int idJugadorLocal = (config != null) ? config.getIdJugador() : 0;
        verificarColisionesCuchillo(jugador, null);
    }
    
    /**
     * 🔥🔥🔥 CORREGIDO: Protección contra daño duplicado a jugadores
     */
    public void verificarColisionesJugador(Jugador jugadorLocal, JugadorRemoto jugadorRemoto) {
        float tiempoActual = com.badlogic.gdx.utils.TimeUtils.nanoTime() / 1_000_000_000f;
        
        Rectangle rectJugador1 = new Rectangle(
            jugadorLocal.getX(), 
            jugadorLocal.getY(), 
            jugadorLocal.getAncho(), 
            jugadorLocal.getAlto()
        );
        
        Rectangle rectJugador2 = null;
        if (jugadorRemoto != null) {
            rectJugador2 = new Rectangle(
                jugadorRemoto.getX(),
                jugadorRemoto.getY(),
                jugadorRemoto.getAncho(),
                jugadorRemoto.getAlto()
            );
        }
        
        // 🔥 PROCESAR COLISIONES CON ENEMIGOS (CON COOLDOWN)
        for (Enemigo enemigo : enemigos) {
            if (!enemigo.estaVivo()) continue;
            
            // JUGADOR LOCAL
            if (rectJugador1.overlaps(enemigo.getBounds()) && enemigo.estaAtacando()) {
                if (tiempoActual - tiempoUltimoAtaqueJugador1 >= COOLDOWN_ATAQUE) {
                    int daño = enemigo.getDaño();
                    jugadorLocal.recibirDaño(daño);
                    tiempoUltimoAtaqueJugador1 = tiempoActual;
                    
                    System.out.println("💥 Jugador LOCAL recibe " + daño + " de daño de " + enemigo.getClass().getSimpleName());
                    
                    if (enemigo instanceof Canibal) {
                        jugadorLocal.penalizarAtaqueCanibal();
                    }
                }
            }
            
            // JUGADOR REMOTO
            if (rectJugador2 != null && rectJugador2.overlaps(enemigo.getBounds()) && enemigo.estaAtacando()) {
                if (tiempoActual - tiempoUltimoAtaqueJugador2 >= COOLDOWN_ATAQUE) {
                    int daño = enemigo.getDaño();
                    
                    if (jugadorRemoto instanceof JugadorRemotoControlable) {
                        JugadorRemotoControlable jugadorControlable = (JugadorRemotoControlable) jugadorRemoto;
                        
                        jugadorControlable.recibirDaño(daño);
                        tiempoUltimoAtaqueJugador2 = tiempoActual;
                        
                        System.out.println("💥 Jugador REMOTO recibe " + daño + " de daño de " + enemigo.getClass().getSimpleName());
                        
                        if (enemigo instanceof Canibal) {
                            jugadorControlable.penalizarAtaqueCanibal();
                        }
                    }
                }
            }
        }
        
        // 🔥 PROCESAR BALAS DE NAZIS (SIN COOLDOWN, SE DESACTIVAN AL IMPACTAR)
        for (Enemigo enemigo : enemigos) {
            if (enemigo instanceof SoldadoNazi) {
                SoldadoNazi nazi = (SoldadoNazi) enemigo;
                
                for (SoldadoNazi.BalaEnemiga bala : nazi.getBalas()) {
                    if (!bala.estaActiva()) continue;
                    
                    Rectangle rectBala = new Rectangle(bala.getX(), bala.getY(), bala.getAncho(), bala.getAlto());
                    
                    // BALA IMPACTA JUGADOR LOCAL
                    if (rectBala.overlaps(rectJugador1)) {
                        jugadorLocal.recibirDaño(bala.getDaño());
                        bala.desactivar();
                        System.out.println("🔫 Bala de Nazi impacta JUGADOR LOCAL (" + bala.getDaño() + " daño)");
                        continue;
                    }
                    
                    // BALA IMPACTA JUGADOR REMOTO
                    if (rectJugador2 != null && rectBala.overlaps(rectJugador2)) {
                        if (jugadorRemoto instanceof JugadorRemotoControlable) {
                            JugadorRemotoControlable jugadorControlable = (JugadorRemotoControlable) jugadorRemoto;
                            jugadorControlable.recibirDaño(bala.getDaño());
                            System.out.println("🔫 Bala de Nazi impacta JUGADOR REMOTO (" + bala.getDaño() + " daño)");
                        }
                        bala.desactivar();
                    }
                }
            }
        }
    }
    
    public void verificarColisionesJugador(Jugador jugador) {
        verificarColisionesJugador(jugador, null);
    }
    
    public void renderTexturas(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        for (Enemigo enemigo : enemigos) {
            if (enemigo instanceof Canibal) {
                Canibal canibal = (Canibal) enemigo;
                canibal.renderTextura(batch);
            } else if (enemigo instanceof SoldadoNazi) {
                SoldadoNazi nazi = (SoldadoNazi) enemigo;
                nazi.renderTextura(batch);
            }
        }
    }
    
    public void renderPowerUps(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        for (PowerUp powerUp : powerUpsDropeados) {
            if (!powerUp.estaRecolectado()) {
                powerUp.render(batch);
            }
        }
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (Enemigo enemigo : enemigos) {
            enemigo.render(shapeRenderer);
        }
        
        shapeRenderer.setColor(Color.ORANGE);
        for (Enemigo enemigo : enemigos) {
            if (enemigo instanceof SoldadoNazi) {
                SoldadoNazi nazi = (SoldadoNazi) enemigo;
                for (SoldadoNazi.BalaEnemiga bala : nazi.getBalas()) {
                    if (bala.estaActiva()) {
                        shapeRenderer.rect(bala.getX(), bala.getY(), bala.getAncho(), bala.getAlto());
                    }
                }
            }
        }
     
        shapeRenderer.end();
    }
    
    public static class ResultadoColisionBalas {
        public final float sanidadRecuperada;
        public final float saludMentalRecuperada;
        
        public ResultadoColisionBalas(float sanidad, float saludMental) {
            this.sanidadRecuperada = sanidad;
            this.saludMentalRecuperada = saludMental;
        }
    }
}