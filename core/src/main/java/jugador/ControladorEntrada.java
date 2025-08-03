package jugador;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;

public class ControladorEntrada implements InputProcessor {

    private boolean izquierda, derecha;

    public boolean isIzquierda() {
        return izquierda;
    }

    public boolean isDerecha() {
        return derecha;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.LEFT) {
            izquierda = true;
        } else if (keycode == Keys.RIGHT) {
            derecha = true;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Keys.LEFT) {
            izquierda = false;
        } else if (keycode == Keys.RIGHT) {
            derecha = false;
        }
        return true;
    }

    // Los demás métodos no los vamos a usar pero deben implementarse
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}
}
