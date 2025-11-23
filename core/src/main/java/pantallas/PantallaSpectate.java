package pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * ⭐ PANTALLA DE SPECTATE - Muestra cuando un jugador muere en modo cooperativo
 */
public class PantallaSpectate {
    
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    
    private String mensajeSpectate;
    private float alpha = 0.8f;
    
    public PantallaSpectate(SpriteBatch batch) {
        this.batch = batch;
        this.font = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();
        
        this.mensajeSpectate = "HAS MUERTO - SPECTANDO AL OTRO JUGADOR";
    }
    
    /**
     * Renderiza el overlay de spectate
     */
    public void render(OrthographicCamera camara) {
        // Fondo oscuro semi-transparente
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camara.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.5f);
        
        float anchoOverlay = 600f;
        float altoOverlay = 100f;
        float x = camara.position.x - anchoOverlay / 2f;
        float y = camara.position.y + 100f;
        
        shapeRenderer.rect(x, y, anchoOverlay, altoOverlay);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        
        // Texto
        batch.setProjectionMatrix(camara.combined);
        batch.begin();
        
        font.getData().setScale(2f);
        font.setColor(Color.RED);
        
        GlyphLayout layout = new GlyphLayout(font, mensajeSpectate);
        float textX = camara.position.x - layout.width / 2f;
        float textY = camara.position.y + 150f;
        
        font.draw(batch, layout, textX, textY);
        
        // Mensaje adicional
        font.getData().setScale(1.3f);
        font.setColor(Color.LIGHT_GRAY);
        String mensaje2 = "Esperando a que tu compañero sobreviva o muera...";
        GlyphLayout layout2 = new GlyphLayout(font, mensaje2);
        float text2X = camara.position.x - layout2.width / 2f;
        float text2Y = camara.position.y + 110f;
        
        font.draw(batch, layout2, text2X, text2Y);
        
        batch.end();
    }
    
    public void dispose() {
        if (font != null) font.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}