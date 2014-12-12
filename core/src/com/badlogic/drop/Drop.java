package com.badlogic.drop;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Drop extends ApplicationAdapter {

	private Music rainMusic;

	private OrthographicCamera camera;
	private SpriteBatch spriteBatch;

	private Texture bucketImage;
	private Sprite bucketSprite;
	private Rectangle bucket;

	private Texture dropImage;
	private Array<Rectangle> raindrops;
	private long lastDropTime;
	private Sound dropSound;

	private BitmapFont font;

	private Vector3 touchPos;

	private static final int screenWidth = 800;
	private static final int screenHeight = 480;

	@Override
	public void create() {

		camera = new OrthographicCamera();
		camera.setToOrtho(false, screenWidth, screenHeight);

		spriteBatch = new SpriteBatch();

		font = new BitmapFont();

		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
		rainMusic.setLooping(true);
		rainMusic.play();

		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		raindrops = new Array<Rectangle>();

		bucket = new Rectangle();
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));
		bucketImage.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		bucketSprite = new Sprite(bucketImage);

		bucket.width = 64;
		bucket.height = 64;
		bucket.x = screenWidth / 2 - bucket.width / 2;
		bucket.y = 20;

		spawnRaindrop();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		spriteBatch.setProjectionMatrix(camera.combined);

		// SPRITEBATCH BEGIN------------------------------------------------------------------
		spriteBatch.begin();
		// SPRITEBATCH BEGIN------------------------------------------------------------------

		bucketSprite.setSize(bucket.width, bucket.height);
		bucketSprite.setColor(new Color(255, 10, 10, 255));
		spriteBatch.draw(bucketSprite, bucket.x, bucket.y, bucket.width, bucket.height);

		font.draw(spriteBatch, "bucket.width    : " + bucket.width, 20, screenHeight - 20);
		font.draw(spriteBatch, "bucket.height   : " + bucket.height, 20, screenHeight - 40);
		font.draw(spriteBatch, "bucketSprite.width    : " + bucketSprite.getWidth(), 20, screenHeight - 60);
		font.draw(spriteBatch, "bucketSprite.height   : " + bucketSprite.getHeight(), 20, screenHeight - 80);

		for (Rectangle raindrop : raindrops) {
			spriteBatch.draw(dropImage, raindrop.x, raindrop.y);
		}

		// SPRITEBATCH END--------------------------------------------------------------------
		spriteBatch.end();
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

		// Screen edge checks
		if (bucket.x < 0) {
			bucket.x = 0;
		}
		if (bucket.x > screenWidth - 64) {
			bucket.x = screenWidth - 64;
		}

		// Raindrop spawn timer
		if (TimeUtils.nanoTime() - lastDropTime > 1000000000) {
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
				dropSound.play();
				iter.remove();
			}
		}
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, screenWidth - 64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	@Override
	public void dispose() {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		spriteBatch.dispose();
		font.dispose();
	}
}
