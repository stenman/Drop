package com.badlogic.drop;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {

	private final Drop game;

	private BitmapFont font;

	private Music rainMusic;
	private Sound dropSound;

	private OrthographicCamera camera;

	private Texture bucketImage;
	private Rectangle bucket;
	private Sprite bucketSprite;

	private Texture dropImage;
	private Array<Rectangle> raindrops;
	private long lastDropTime;
	private long nextDropTime;
	private int dropsGathered;

	private Vector3 touchPos;

	private static final int screenWidth = 800;
	private static final int screenHeight = 480;

	public GameScreen(final Drop game) {
		this.game = game;

		font = new BitmapFont();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, screenWidth, screenHeight);

		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
		rainMusic.setLooping(true);

		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));

		dropImage = new Texture(Gdx.files.internal("droplet.png"));

		bucket = new Rectangle();
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));
		bucketImage.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		bucketSprite = new Sprite(bucketImage);

		bucket.width = 64;
		bucket.height = 64;
		bucket.x = screenWidth / 2 - bucket.width / 2;
		bucket.y = 20;

		raindrops = new Array<Rectangle>();
		spawnRaindrop();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		game.batch.setProjectionMatrix(camera.combined);

		// SPRITEBATCH BEGIN------------------------------------------------------------------
		game.batch.begin();
		// SPRITEBATCH BEGIN------------------------------------------------------------------

		font.draw(game.batch, "Drop Collected: " + dropsGathered, 0, 480);

		// Are these needed?
		bucketSprite.setSize(bucket.width, bucket.height);
		bucketSprite.setColor(new Color(255, 10, 10, 255));

		game.batch.draw(bucketSprite, bucket.x, bucket.y, bucket.width, bucket.height);

		printOnScreenInfo();

		for (Rectangle raindrop : raindrops) {
			game.batch.draw(dropImage, raindrop.x, raindrop.y);
		}

		// SPRITEBATCH END--------------------------------------------------------------------
		game.batch.end();
		// SPRITEBATCH END--------------------------------------------------------------------

		// Move bucket
		if (Gdx.input.isTouched()) {
			touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - bucket.width / 2;
		}

		// Key input control
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			bucket.x -= 300 * Gdx.graphics.getDeltaTime();
		}
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			bucket.x += 300 * Gdx.graphics.getDeltaTime();
		}
		if (Gdx.input.isKeyPressed(Keys.UP)) {
			bucket.setSize(bucket.width + 3, bucket.height + 3);
		}
		if (Gdx.input.isKeyPressed(Keys.DOWN)) {
			bucket.setSize(bucket.width - 3, bucket.height - 3);
		}
		if (Gdx.input.isKeyPressed(Keys.C)) {
		}

		// Bucket size check
		if (bucket.width < 20 || bucket.height < 20) {
			bucket.setSize(bucket.width + 3, bucket.height + 3);
		}
		if (bucket.width > 250 || bucket.height > 250) {
			bucket.setSize(bucket.width - 3, bucket.height - 3);
		}

		// Screen edge checks
		if (bucket.x < 0) {
			bucket.x = 0;
		}
		if (bucket.x > screenWidth - bucket.width) {
			bucket.x = screenWidth - bucket.width;
		}

		// Raindrop spawn timer
		if (TimeUtils.nanoTime() - lastDropTime > nextDropTime) {
			spawnRaindrop();
		}

		// Raindrop collision check
		Iterator<Rectangle> iter = raindrops.iterator();
		while (iter.hasNext()) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y + 64 < 0) {
				iter.remove();
			}
			if (raindrop.overlaps(bucket)) {
				dropsGathered++;
				dropSound.play();
				iter.remove();
			}
		}

	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		rainMusic.play();
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
	}

	// DEBUG
	private void printOnScreenInfo() {
		font.draw(game.batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, screenHeight - 20);
		font.draw(game.batch, "bucket.width: " + bucket.width, 20, screenHeight - 40);
		font.draw(game.batch, "bucket.height: " + bucket.height, 20, screenHeight - 60);
		font.draw(game.batch, "bucketSprite.width: " + bucketSprite.getWidth(), 20, screenHeight - 80);
		font.draw(game.batch, "bucketSprite.height: " + bucketSprite.getHeight(), 20, screenHeight - 100);
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, screenWidth - 64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
		nextDropTime = MathUtils.random(300000000, 1200000000);
	}

}
