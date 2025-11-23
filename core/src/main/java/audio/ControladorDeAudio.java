package audio;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class ControladorDeAudio {

    // Música
    private Music musicaMenu;
    private Music musicaNivel1;
    private float volumenMusica = 0.1f, volumenEfectosDeSonido = 0.3f;
    private final float MAX = 1f, MIN = 0;
    private boolean musicaMenuReproduciendo = false;

    // Sonidos
    private HashMap<String, Sound> efectosDeSonido = new HashMap<>();
    
    // === MÚSICA PARA RESPIRACIÓN DEL CANÍBAL ===
    private Music respiracionCanibal;
    private boolean respiracionCargada = false;
    
    // === SISTEMA DE DUCKING ===
    private float volumenOriginalMusica = 0.5f;
    private float volumenDucking = 0.2f;
    private boolean musicaDuckeada = false;
    private int contadorSonidosCanibal = 0;

    // === SISTEMA DE DIÁLOGOS NAZIS ===
    private HashSet<String> dialogosEnUso = new HashSet<>();
    private ArrayList<String> dialogosDisponibles = new ArrayList<>();
    
    
    // === CARGA ===
    public void cargarMusica() {
    	
        this.musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("Musica/menu musica.mp3"));
        this.musicaMenu.setLooping(true);
        this.musicaMenu.setVolume(this.volumenMusica);

        this.musicaNivel1 = Gdx.audio.newMusic(Gdx.files.internal("Musica/Musica nivel 1.mp3"));
        this.musicaNivel1.setLooping(true);
        this.musicaNivel1.setVolume(this.volumenMusica);
        
        this.volumenOriginalMusica = this.volumenMusica;
    }
    
    public void cargarRespiracionCanibal() {
        if (!respiracionCargada) {
            this.respiracionCanibal = Gdx.audio.newMusic(Gdx.files.internal("Efectos de sonido/canibal-respiracion.mp3"));
            this.respiracionCanibal.setLooping(true);
            this.respiracionCanibal.setVolume(this.volumenEfectosDeSonido * 0.7f);
            respiracionCargada = true;
        }
    }
    
    /**
     * ⭐ Inicializa el pool de diálogos de nazis
     */
    public void inicializarDialogosNazis() {
        dialogosDisponibles.clear();
        dialogosDisponibles.add("nazi-dialogo1");
        dialogosDisponibles.add("nazi-dialogo2");
        dialogosDisponibles.add("nazi-dialogo3");
        dialogosEnUso.clear();
        
        System.out.println("✅ Pool de diálogos inicializado: " + dialogosDisponibles.size() + " diálogos disponibles");
    }
    
    /**
     * ⭐ Obtiene un diálogo aleatorio que NO esté en uso
     */
    public String obtenerDialogoAleatorioDisponible() {
        System.out.println("🔍 Buscando diálogo disponible...");
        System.out.println("   Diálogos en uso: " + dialogosEnUso.size() + " - " + dialogosEnUso);
        
        // Filtrar diálogos disponibles
        ArrayList<String> disponibles = new ArrayList<>();
        for (String dialogo : dialogosDisponibles) {
            if (!dialogosEnUso.contains(dialogo)) {
                disponibles.add(dialogo);
            }
        }
        
        System.out.println("   Diálogos disponibles: " + disponibles.size() + " - " + disponibles);
        
        if (disponibles.isEmpty()) {
            System.out.println("⚠️ No hay diálogos disponibles");
            return null;
        }
        
        // Elegir uno al azar
        Collections.shuffle(disponibles);
        String dialogoElegido = disponibles.get(0);
        
        // Marcarlo como en uso
        dialogosEnUso.add(dialogoElegido);
        
        System.out.println("✅ Diálogo asignado: " + dialogoElegido);
        return dialogoElegido;
    }
    
    /**
     * ⭐ Libera un diálogo
     */
    public void liberarDialogo(String nombreDialogo) {
        if (nombreDialogo != null && dialogosEnUso.contains(nombreDialogo)) {
            dialogosEnUso.remove(nombreDialogo);
            System.out.println("🔓 Diálogo liberado: " + nombreDialogo);
        }
    }
    
    /**
     * ⭐ Reproduce un diálogo de nazi
     */
    public void reproducirDialogoNazi(String nombreDialogo) {
        if (nombreDialogo == null) {
            System.out.println("⚠️ Intento de reproducir diálogo nulo");
            return;
        }
        
        Sound s = efectosDeSonido.get(nombreDialogo);
        if (s != null) {
            System.out.println("🔊 Reproduciendo: " + nombreDialogo);
            long id = s.play(volumenEfectosDeSonido);
            
            if (id == -1) {
                System.out.println("❌ Error al reproducir el sonido (ID = -1)");
                liberarDialogo(nombreDialogo);
                return;
            }
            
            // Liberar después de 5 segundos (ajusta según duración de tus audios)
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // ⭐ Aumentado a 5 segundos
                    liberarDialogo(nombreDialogo);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            System.out.println("❌ Sonido no encontrado: " + nombreDialogo);
            System.out.println("   Sonidos cargados: " + efectosDeSonido.keySet());
        }
    }
    
    /**
     * ⭐ Detiene un diálogo de nazi que está sonando
     */
    public void detenerDialogoNazi(String nombreDialogo) {
        if (nombreDialogo == null) {
            System.out.println("⚠️ Intento de detener diálogo nulo");
            return;
        }
        
        Sound s = efectosDeSonido.get(nombreDialogo);
        if (s != null) {
            s.stop(); // ⭐ Detiene todas las instancias del sonido
            System.out.println("🔇 Diálogo detenido: " + nombreDialogo);
        } else {
            System.out.println("⚠️ No se pudo detener el diálogo (no encontrado): " + nombreDialogo);
        }
    }
    
    private void aplicarDucking() {
        if (!musicaDuckeada && contadorSonidosCanibal > 0) {
            if (musicaNivel1 != null && musicaNivel1.isPlaying()) {
                musicaNivel1.setVolume(volumenOriginalMusica * volumenDucking);
                musicaDuckeada = true;
            }
        }
    }
    
    private void restaurarVolumenMusica() {
        if (musicaDuckeada && contadorSonidosCanibal == 0) {
            if (musicaNivel1 != null && musicaNivel1.isPlaying()) {
                musicaNivel1.setVolume(volumenOriginalMusica);
                musicaDuckeada = false;
            }
        }
    }

    public void cargarSonidos(String nombre, String rutaSonido) {
        try {
            this.efectosDeSonido.put(nombre, Gdx.audio.newSound(Gdx.files.internal(rutaSonido)));
            System.out.println("✅ Sonido cargado: " + nombre + " desde " + rutaSonido);
        } catch (Exception e) {
            System.out.println("❌ Error al cargar sonido: " + nombre + " - " + e.getMessage());
        }
    }
    
    public void iniciarRespiracionCanibal() {
        if (respiracionCanibal != null && !respiracionCanibal.isPlaying()) {
            respiracionCanibal.play();
            contadorSonidosCanibal++;
            aplicarDucking();
        }
    }
    
    public void detenerRespiracionCanibal() {
        if (respiracionCanibal != null && respiracionCanibal.isPlaying()) {
            respiracionCanibal.stop();
            contadorSonidosCanibal--;
            if (contadorSonidosCanibal < 0) contadorSonidosCanibal = 0;
            restaurarVolumenMusica();
        }
    }

    public void reproducirSonido(String nombre) {
        Sound s = efectosDeSonido.get(nombre);
        if (s != null) {
            if (nombre.equals("canibal-te-ve")) {
                contadorSonidosCanibal++;
                aplicarDucking();
                
                s.play(volumenEfectosDeSonido);
                
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        contadorSonidosCanibal--;
                        if (contadorSonidosCanibal < 0) contadorSonidosCanibal = 0;
                        restaurarVolumenMusica();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                s.play(volumenEfectosDeSonido);
            }
        }
    }

    // === MÚSICA MENÚ ===
    public void iniciarMusicaMenu() {
        if (!musicaMenuReproduciendo || !this.musicaMenu.isPlaying()) {
            this.musicaMenu.play();
        }
        musicaMenuReproduciendo = true;
    }

    public void detenerMusicaMenu() {
        if (this.musicaMenu != null || this.musicaMenu.isPlaying()) {
            this.musicaMenu.stop();
        }
        musicaMenuReproduciendo = false;
    }

    public void pausarMusicaMenu() {
        this.musicaMenu.pause();
    }

    // === MÚSICA NIVEL 1 ===
    public void iniciarMusicaNivel1() {
        detenerMusicaMenu();
        if (!this.musicaNivel1.isPlaying()) {
            this.musicaNivel1.play();
        }
        
        musicaDuckeada = false;
        contadorSonidosCanibal = 0;
    }

    public void detenerMusicaNivel1() {
        this.musicaNivel1.stop();
        musicaDuckeada = false;
        contadorSonidosCanibal = 0;
    }

    public void reproducirMusica() {
        if (!this.musicaNivel1.isPlaying()) {
            this.musicaNivel1.play();
        }
    }

    public void pausarMusica() {
        this.musicaNivel1.pause();
    }

    public void pararMusica() {
        this.musicaNivel1.stop();
    }
    
    public void detenerTodaMusica() {
        if (musicaMenu != null && musicaMenu.isPlaying()) musicaMenu.stop();
        if (musicaNivel1 != null && musicaNivel1.isPlaying()) musicaNivel1.stop();
        if (respiracionCanibal != null && respiracionCanibal.isPlaying()) respiracionCanibal.stop();
        musicaMenuReproduciendo = false;
        musicaDuckeada = false;
        contadorSonidosCanibal = 0;
        dialogosEnUso.clear();
    }

    public void volverAlMenu() {
        detenerMusicaNivel1();
        detenerRespiracionCanibal();
        iniciarMusicaMenu();
    }

    // === CONTROL DE VOLUMEN ===
    public void subirVolumenSonidos() {
        if (this.volumenEfectosDeSonido < this.MAX) {
            this.volumenEfectosDeSonido += 0.1f;
        } else {
            this.volumenEfectosDeSonido = 1.0f;
        }
        
        if (respiracionCanibal != null) {
            respiracionCanibal.setVolume(this.volumenEfectosDeSonido * 0.7f);
        }
    }

    public void bajarVolumenSonidos() {
        if (this.volumenEfectosDeSonido > this.MIN) {
            this.volumenEfectosDeSonido = Math.max(0f, this.volumenEfectosDeSonido - 0.1f);
        } else {
            this.volumenEfectosDeSonido = 0f;
        }
        
        if (respiracionCanibal != null) {
            respiracionCanibal.setVolume(this.volumenEfectosDeSonido * 0.7f);
        }
    }

    public void subirVolumenMusica() {
        if (this.volumenMusica < this.MAX) {
            this.volumenMusica += 0.1f;
        } else {
            this.volumenMusica = 1.0f;
        }
        this.volumenOriginalMusica = this.volumenMusica;
        
        if (!musicaDuckeada) {
            this.musicaNivel1.setVolume(this.volumenMusica);
            this.musicaMenu.setVolume(this.volumenMusica);
        } else {
            this.musicaNivel1.setVolume(this.volumenMusica * volumenDucking);
        }
        this.musicaMenu.setVolume(this.volumenMusica);
    }

    public void bajarVolumenMusica() {
        if (this.volumenMusica > this.MIN) {
            this.volumenMusica = Math.max(0f, this.volumenMusica - 0.1f);
        } else {
            this.volumenMusica = 0f;
        }
        this.volumenOriginalMusica = this.volumenMusica;
        
        if (!musicaDuckeada) {
            this.musicaNivel1.setVolume(this.volumenMusica);
            this.musicaMenu.setVolume(this.volumenMusica);
        } else {
            this.musicaNivel1.setVolume(this.volumenMusica * volumenDucking);
        }
        this.musicaMenu.setVolume(this.volumenMusica);
    }

    // === LIBERAR MEMORIA ===
    public void dispose() {
        if (musicaNivel1 != null) {
            musicaNivel1.dispose();
            musicaNivel1 = null;
        }

        if (musicaMenu != null) {
            musicaMenu.dispose();
            musicaMenu = null;
        }
        
        if (respiracionCanibal != null) {
            respiracionCanibal.dispose();
            respiracionCanibal = null;
            respiracionCargada = false;
        }

        for (Sound s : efectosDeSonido.values()) {
            if (s != null) s.dispose();
        }
        efectosDeSonido.clear();

        musicaMenuReproduciendo = false;
        musicaDuckeada = false;
        contadorSonidosCanibal = 0;
        dialogosEnUso.clear();
        dialogosDisponibles.clear();
    }
}