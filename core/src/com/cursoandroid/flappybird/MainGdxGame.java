package com.cursoandroid.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;


public class MainGdxGame extends ApplicationAdapter {
    private final static float DELTA_T = 1f;
    private final static float ESPACO_ENTRE_CANOS = 300;
    private final static int VIRTUAL_WIDTH = 720;
    private final static int VIRTUAL_HEIGHT = 1280;
    private SpriteBatch batch;
    private AnimacaoPorArray passaros;
    private Texture fundo;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture gameOver;
    private ShapeRenderer shapeRenderer;
    private Circle circuloPassaro;
    private Rectangle retanguloTopo;
    private Rectangle retanguloBaixo;
    private Random random;
	private int WIDTH;
	private int WIDTH_2;
    private int WIDTH_TEXT_PONTUACAO;
	private int HEIGHT;
	private int HEIGHT_2;
    private float velocidadeY;
    private float posicaoPassaroY;
    private float posicaoPassaroX;
    private float radiusPassaroX;
    private float radiusPassaroY;
    private float posicaoCanoX;
    private float posicaoCanoY;
    private int pontuacao;
    private int pontuacaoMax;
    private boolean passouCano = false;
    private BitmapFont textPontuacao;
    private BitmapFont textReiniciar;
    private BitmapFont melhorPontuacao;

    Sound somVoando;
    Sound somColisao;
    Sound somPontuacao;

    private enum STATUS  {INICIO, JOGO, COLICAO}
    private STATUS estadoJogo = STATUS.INICIO;

    private Preferences preferences;

    private OrthographicCamera camera;

    private Viewport viewport;

    @Override
	public void create () {
		init();
		carregarTextura();
	}

	@Override
	public void render () {
        ScreenUtils.clear(Color.CLEAR);
        logica();
        validarPontos();
        desenhar();
        detectarColisoes();
	}

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
	public void dispose () {
		fundo.dispose();
        passaros.dispose();
        shapeRenderer.dispose();
	}

    private float inpulso(float tempo, int fator){
        return fator * tempo;
    }
    private float calculaPosicao(float posicao, float velocidade, float tempo){
        return posicao - velocidade * tempo;
    }

    private float calculaTempo(int fator_tempo){
        return fator_tempo * Gdx.graphics.getDeltaTime();
    }

    private void init(){
        random = new Random();
        WIDTH = VIRTUAL_WIDTH;
        HEIGHT = VIRTUAL_HEIGHT;
        WIDTH_2 = WIDTH/2;
        WIDTH_TEXT_PONTUACAO = WIDTH/2;
        HEIGHT_2 = HEIGHT/2;
        posicaoPassaroX = 50;
        posicaoPassaroY = HEIGHT/2f;
        velocidadeY = 0f;
        posicaoCanoX = WIDTH;
        posicaoCanoY = 0;
        pontuacao = 0;
        pontuacaoMax = 0;
        confFont();
        confColisoes();
        confSons();
        confPreference();
        confCamera();
    }

    private void confCamera(){
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH/2f, VIRTUAL_HEIGHT/2f, 0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
    }
    private void confPreference(){
        preferences = Gdx.app.getPreferences("flappyBird");
        pontuacaoMax = preferences.getInteger("pontuacaoMaxima",0);
    }
    private void confSons(){
        somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
        somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
        somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
    }

    private void confFont(){
        textPontuacao = new BitmapFont();
        textPontuacao.setColor(Color.WHITE);
        textPontuacao.getData().setScale(8);

        textReiniciar = new BitmapFont();
        textReiniciar.setColor(Color.GREEN);
        textReiniciar.getData().setScale(2);

        melhorPontuacao = new BitmapFont();
        melhorPontuacao.setColor(Color.RED);
        melhorPontuacao.getData().setScale(2);
    }
    private void confColisoes(){
        shapeRenderer = new ShapeRenderer();
        circuloPassaro = new Circle();
        retanguloTopo = new Rectangle();
        retanguloBaixo = new Rectangle();
    }
    private void carregarTextura(){
        batch = new SpriteBatch();
        passaros = new AnimacaoPorArray(10, new Texture[]{new Texture("passaro1.png"),
                new Texture("passaro2.png"), new Texture("passaro3.png")});
        fundo = new Texture("fundo.png");
        canoTopo = new Texture("cano_topo_maior.png");
        canoBaixo = new Texture("cano_baixo_maior.png");
        gameOver = new Texture("game_over.png");
        radiusPassaroX = passaros.getCurrentTexture().getWidth()/2f;
        radiusPassaroY = passaros.getCurrentTexture().getHeight()/2f;
    }

    private void desenhar(){
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(fundo, 0, 0, WIDTH, HEIGHT);
        batch.draw(passaros.getCurrentTexture(), posicaoPassaroX, posicaoPassaroY);
        batch.draw(canoBaixo, posicaoCanoX, HEIGHT_2 -canoBaixo.getHeight() - ESPACO_ENTRE_CANOS /2 + posicaoCanoY);
        batch.draw(canoTopo, posicaoCanoX, HEIGHT_2 + ESPACO_ENTRE_CANOS /2 + posicaoCanoY);
        textPontuacao.draw(batch,String.valueOf(pontuacao),WIDTH_TEXT_PONTUACAO - textPontuacao.getXHeight(),HEIGHT);
        if (estadoJogo == STATUS.INICIO){
            textReiniciar.draw(batch, "Toque para Começar!!", WIDTH_2 - 140, HEIGHT_2 - gameOver.getHeight()/2f);
        }
        if(estadoJogo == STATUS.COLICAO){
            batch.draw(gameOver, WIDTH_2 - gameOver.getWidth()/2f, HEIGHT_2);
            textReiniciar.draw(batch, "Toque para reiniciar!", WIDTH_2 - 140, HEIGHT_2 - gameOver.getHeight()/2f);
            melhorPontuacao.draw(batch, "Seu record é: " + pontuacaoMax +" pontos",WIDTH_2 - 140, HEIGHT_2 - gameOver.getHeight());
        }
        batch.end();
    }

    private void logica(){
        if(estadoJogo == STATUS.INICIO){
            if (Gdx.input.justTouched()){
                estadoJogo = STATUS.JOGO;
                somVoando.play();
            }
        } else if (estadoJogo == STATUS.JOGO){
            //evento velocidade passaro
            eventoToque();
            //eventos cano
            eventoCano();
        } else if (estadoJogo == STATUS.COLICAO) {

            if (pontuacao > pontuacaoMax){
                pontuacaoMax = pontuacao;
                preferences.putInteger("pontuacaoMaxima", pontuacaoMax);
            }
            posicaoPassaroX -= Gdx.graphics.getDeltaTime() * 500;

            if (Gdx.input.justTouched()){
                estadoJogo = STATUS.JOGO;
                pontuacao = 0;
                velocidadeY = 0;
                posicaoPassaroX = 50;
                posicaoPassaroY = HEIGHT/2f;
                posicaoCanoX = WIDTH;
                passouCano = false;
            }
        }
    }

    private void eventoToque(){
        boolean toqueTela = Gdx.input.justTouched();
        if (toqueTela){
            velocidadeY = inpulso(DELTA_T, -15);
            somVoando.play();
        }
        // aplica velocidade de queda no passaro
        if (posicaoPassaroY > 24 || toqueTela){
            posicaoPassaroY = calculaPosicao(posicaoPassaroY,velocidadeY, DELTA_T);
        }
        velocidadeY++;
    }

    private void eventoCano(){
        posicaoCanoX -= calculaTempo(200);
        if (posicaoCanoX < -canoTopo.getWidth()){
            posicaoCanoX = WIDTH;
            posicaoCanoY = random.nextInt(HEIGHT_2) - HEIGHT_2;
            passouCano = false;
        }
    }

    private void validarPontos(){
        if(posicaoCanoX < 0 && !passouCano){
            pontuacao++;
            passouCano = true;
            somPontuacao.play();
        }
    }

    private void detectarColisoes(){
        circuloPassaro.set(posicaoPassaroX + radiusPassaroX,posicaoPassaroY + radiusPassaroY, radiusPassaroX);
        retanguloBaixo.set(posicaoCanoX, HEIGHT_2 -canoBaixo.getHeight() - ESPACO_ENTRE_CANOS /2 + posicaoCanoY,
                canoBaixo.getWidth(), canoBaixo.getHeight());
        retanguloTopo.set(posicaoCanoX, HEIGHT_2 + ESPACO_ENTRE_CANOS /2 + posicaoCanoY,
                canoTopo.getWidth(), canoTopo.getHeight());

        if(Intersector.overlaps(circuloPassaro, retanguloBaixo) ||
                Intersector.overlaps(circuloPassaro, retanguloTopo) ){
            if (estadoJogo == STATUS.JOGO){
                estadoJogo = STATUS.COLICAO;
                somColisao.play();
            }
        }
    }

    public static class AnimacaoPorArray{
        private final int fatorTempo;
        private final Texture[] textures;
        private float variacao;
        public AnimacaoPorArray(int fatorTempo, Texture[] textures){
            this.fatorTempo = fatorTempo;
            this.textures = textures;
        }

        public Texture getCurrentTexture(){
            variacao += fatorTempo * Gdx.graphics.getDeltaTime();
            if (variacao > 3){
                variacao = 0;
            }
            return textures[(int) variacao];
        }

        public void dispose(){
            for (Texture t: textures){
                t.dispose();
            }
        }
    }
}
