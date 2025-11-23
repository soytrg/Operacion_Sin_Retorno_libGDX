package jugador;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class ControladorEntrada extends InputAdapter {
    
    private boolean space = false;
    private boolean izquierda = false;
    private boolean derecha = false;
    private boolean arriba = false;
    private boolean abajo = false;
    private boolean disparar = false;
    private boolean recargar = false;
    private boolean atacarCuchillo = false;
    private boolean shift = false;
    private boolean escape = false;
    private boolean enter = false;
    private boolean e = false;
    private boolean v = false;
    
    private boolean tecla1 = false;
    private boolean tecla2 = false;
    private boolean tecla3 = false;
    private boolean tecla4 = false;
    
    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.SPACE:
                space = true;
                break;
            case Input.Keys.A:
            case Input.Keys.LEFT:
                izquierda = true;
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                derecha = true;
                break;
            case Input.Keys.W:
            case Input.Keys.UP:
                arriba = true;
                break;
            case Input.Keys.S:
            case Input.Keys.DOWN:
                abajo = true;
                break;
            case Input.Keys.R:
                recargar = true;
                break;
            case Input.Keys.F:
                atacarCuchillo = true;
                break;
            case Input.Keys.V:
                v = true;
                break;
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                shift = true;
                break;
            case Input.Keys.ESCAPE:
                escape = true;
                break;
            case Input.Keys.ENTER:
                enter = true;
                break;
            case Input.Keys.E:
                e = true;
                break;
            case Input.Keys.NUM_1:
            case Input.Keys.NUMPAD_1:
                tecla1 = true;
                break;
            case Input.Keys.NUM_2:
            case Input.Keys.NUMPAD_2:
                tecla2 = true;
                break;
            case Input.Keys.NUM_3:
            case Input.Keys.NUMPAD_3:
                tecla3 = true;
                break;
            case Input.Keys.NUM_4:
            case Input.Keys.NUMPAD_4:
                tecla4 = true;
                break;
        }
        return true;
    }
    
    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.SPACE:
                space = false;
                break;
            case Input.Keys.A:
            case Input.Keys.LEFT:
                izquierda = false;
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                derecha = false;
                break;
            case Input.Keys.W:
            case Input.Keys.UP:
                arriba = false;
                break;
            case Input.Keys.S:
            case Input.Keys.DOWN:
                abajo = false;
                break;
            case Input.Keys.R:
                recargar = false;
                break;
            case Input.Keys.F:
                atacarCuchillo = false;
                break;
            case Input.Keys.V:
                v = false;
                break;
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                shift = false;
                break;
            case Input.Keys.ESCAPE:
                escape = false;
                break;
            case Input.Keys.ENTER:
                enter = false;
                break;
            case Input.Keys.E:
                e = false;
                break;
            case Input.Keys.NUM_1:
            case Input.Keys.NUMPAD_1:
                tecla1 = false;
                break;
            case Input.Keys.NUM_2:
            case Input.Keys.NUMPAD_2:
                tecla2 = false;
                break;
            case Input.Keys.NUM_3:
            case Input.Keys.NUMPAD_3:
                tecla3 = false;
                break;
            case Input.Keys.NUM_4:
            case Input.Keys.NUMPAD_4:
                tecla4 = false;
                break;
        }
        return true;
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            disparar = true;
        }
        return true;
    }
    
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            disparar = false;
        }
        return true;
    }
    
    // === GETTERS ===
    public boolean isSpace() { return space; }
    public boolean isIzquierda() { return izquierda; }
    public boolean isDerecha() { return derecha; }
    public boolean isArriba() { return arriba; }
    public boolean isAbajo() { return abajo; }
    public boolean isDisparar() { return disparar; }
    public boolean isRecargar() { return recargar; }
    public boolean isAtacarCuchillo() { return atacarCuchillo; }
    public boolean isShift() { return shift; }
    public boolean isEscape() { return escape; }
    public boolean isEnter() { return enter; }
    public boolean isE() { return e; }
    public boolean isV() { return v; }
    
    public boolean is1() { return tecla1; }
    public boolean is2() { return tecla2; }
    public boolean is3() { return tecla3; }
    public boolean is4() { return tecla4; }
}