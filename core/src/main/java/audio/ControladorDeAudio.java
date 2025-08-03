package audio;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class ControladorDeAudio {

    // Música
    private Music musicaMenu;
    private Music musicaNivel1;
    private float volumenMusica = 0.5f, volumenEfectosDeSonido = 1.0f;
    private final float MAX = 1f, MIN = 0;
    private boolean musicaMenuReproduciendo = false;

    // Sonidos
    private HashMap<String, Sound> efectosDeSonido = new HashMap<>();

    // === CARGA ===
    public void cargarMusica() {
        this.musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("Efectos de sonido/menu_select.mp3"));
        this.musicaMenu.setLooping(true);
        this.musicaMenu.setVolume(this.volumenMusica);

        this.musicaNivel1 = Gdx.audio.newMusic(Gdx.files.internal("Musica/Musica nivel 1.mp3"));
        this.musicaNivel1.setLooping(true);
        this.musicaNivel1.setVolume(this.volumenMusica);
    }

    public void cargarSonidos(String nombre, String rutaSonido) {
        this.efectosDeSonido.put(nombre, Gdx.audio.newSound(Gdx.files.internal(rutaSonido)));
    }

    public void reproducirSonido(String nombre) {
        Sound s = efectosDeSonido.get(nombre);
        if (s != null) s.play(volumenEfectosDeSonido);
    }

    // === MÚSICA MENÚ ===
    public void iniciarMusicaMenu() {
        if (!musicaMenuReproduciendo && !this.musicaMenu.isPlaying()) {
            this.musicaMenu.play();
            musicaMenuReproduciendo = true;
        }
    }

    public void detenerMusicaMenu() {
        if (musicaMenuReproduciendo) {
            this.musicaMenu.stop();
            musicaMenuReproduciendo = false;
        }
    }

    public void pausarMusicaMenu() {
        this.musicaMenu.pause();
    }

    // === MÚSICA NIVEL 1 ===
    public void iniciarMusicaNivel1() {
        detenerMusicaMenu(); // Detener música del menú primero
        if (!this.musicaNivel1.isPlaying()) {
            this.musicaNivel1.play();
        }
    }

    public void detenerMusicaNivel1() {
        this.musicaNivel1.stop();
    }

    // Método para reproducir música del nivel
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

    // === CAMBIO AL MENÚ ===
    public void volverAlMenu() {
        detenerMusicaNivel1();
        iniciarMusicaMenu();
    }

    // === CONTROL DE VOLUMEN SONIDOS ===
    public void subirVolumenSonidos() {
        if (this.volumenEfectosDeSonido < this.MAX) {
            this.volumenEfectosDeSonido += 0.1f;
        } else {
            this.volumenEfectosDeSonido = 1.0f;
        }
    }

    public void bajarVolumenSonidos() {
        if (this.volumenEfectosDeSonido > this.MIN) {
            this.volumenEfectosDeSonido = Math.max(0f, this.volumenEfectosDeSonido - 0.1f);
        } else {
            this.volumenEfectosDeSonido = 0f;
        }
    }

    // === CONTROL DE VOLUMEN MÚSICA ===
    public void subirVolumenMusica() {
        if (this.volumenMusica < this.MAX) {
            this.volumenMusica += 0.1f;
        } else {
            this.volumenMusica = 1.0f;
        }
        this.musicaNivel1.setVolume(this.volumenMusica);
        this.musicaMenu.setVolume(this.volumenMusica);
    }

    public void bajarVolumenMusica() {
        if (this.volumenMusica > this.MIN) {
            this.volumenMusica = Math.max(0f, this.volumenMusica - 0.1f);
        } else {
            this.volumenMusica = 0f;
        }
        this.musicaNivel1.setVolume(this.volumenMusica);
        this.musicaMenu.setVolume(this.volumenMusica);
    }

    // === LIBERAR MEMORIA ===
    public void dispose() {
        this.musicaNivel1.dispose();
        this.musicaMenu.dispose();
        for (Sound s : efectosDeSonido.values()) {
            s.dispose();
        }
    }
}
