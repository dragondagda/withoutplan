package de.ggj14bremen.withoutplan.controller;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;
import de.ggj14bremen.withoutplan.GameSettings;
import de.ggj14bremen.withoutplan.MainActivity;
import de.ggj14bremen.withoutplan.R;

public class Sounds 
{
	private MediaPlayer tick;
	
	private MediaPlayer finalTick;
	
	private MediaPlayer spawn;
	
	private MediaPlayer destroyed;
	
	private MediaPlayer blackout;
	
	private MediaPlayer darker;
	
	private MediaPlayer move;

	private static MediaPlayer mediaPlayer;

	private static SoundPool soundPool;

	private static HashMap<Integer, Integer> sounds = new HashMap<Integer, Integer>();
	
	private Sounds()
	{
		
	}
	
	public static void init(Context context)
	{
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		try
		{
			mediaPlayer.prepare();
		} catch (IllegalStateException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mediaPlayer.start();
		
    	soundPool 	= new SoundPool(8, AudioManager.STREAM_MUSIC, 0);

    	sounds.put(R.raw.timer_2, soundPool.load(context, R.raw.timer_2, 1));
    	sounds.put(R.raw.timer, soundPool.load(context, R.raw.timer, 1));
    	sounds.put(R.raw.fail_2, soundPool.load(context, R.raw.fail_2, 1));
    	sounds.put(R.raw.spawn, soundPool.load(context, R.raw.spawn, 1));
    	sounds.put(R.raw.player_spawn, soundPool.load(context, R.raw.player_spawn, 1));
    	sounds.put(R.raw.destroy, soundPool.load(context, R.raw.destroy, 1));
    	sounds.put(R.raw.fail, soundPool.load(context, R.raw.fail, 1));
    	sounds.put(R.raw.counter_fade, soundPool.load(context, R.raw.counter_fade, 1));
    	sounds.put(R.raw.movement_5, soundPool.load(context, R.raw.movement_5, 1));
	}
	public static final void destroy()
	{
		mediaPlayer.release();
		soundPool.release();
	}
	public static void playSound(int resID)
	{
		Integer soundID = sounds.get(resID);
		if(soundID != null) 
		{
			if(!GameSettings.isMuted())	soundPool.play(soundID, GameSettings.getVolume(),  GameSettings.getVolume(), 1, 0, 1f);
		}
		else if(MainActivity.DEBUG) Log.e(MainActivity.TAG, "Error loading sound.");
	}
	@Deprecated
	public void tick(){
		tick.start();
	}
	@Deprecated
	public void finalTick(){
		finalTick.start();
	}
	@Deprecated
	public void enemySpawned(){
		this.spawn.start();
	}
	@Deprecated
	public void start(){
		// TODO
	}
	@Deprecated
	public void enemyDestroyed(){
		this.destroyed.start();
	}
	@Deprecated
	public void nextFigure(){
		// TODO 
	}
	@Deprecated
	public void darker(){
		this.darker.start();
	}
	@Deprecated
	public void blackout(){
		this.blackout.start();
	}
	@Deprecated
	public void move(){
		this.move.start();
	}
}
