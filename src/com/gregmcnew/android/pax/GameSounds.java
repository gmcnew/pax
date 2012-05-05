package com.gregmcnew.android.pax;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class GameSounds {
	
	public enum Sound { SHOOT_BOMB, SHOOT_LASER, SHOOT_MISSILE, EXPLOSION, UPGRADE }	
	
	private static SoundPool sSoundPool = null;
	
	private static Map<Sound, Integer> sGameSounds;
	private static Context sContext;
	
	public static void initialize(Context context) {
		if (sSoundPool != null) {
			sSoundPool.release();
			sSoundPool = null;
		}
		sContext = context;
	}
	
	private static void lazyInitialize() {
		Log.v(Pax.TAG, "loading sounds");
		
		sGameSounds = new HashMap<Sound, Integer>();
		sSoundPool = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
		
		sGameSounds.put(Sound.SHOOT_BOMB,    sSoundPool.load(sContext, R.raw.shoot_bomb, 0));
		sGameSounds.put(Sound.SHOOT_LASER,   sSoundPool.load(sContext, R.raw.shoot_laser, 0));
		sGameSounds.put(Sound.SHOOT_MISSILE, sSoundPool.load(sContext, R.raw.shoot_missile, 0));
		sGameSounds.put(Sound.EXPLOSION,     sSoundPool.load(sContext, R.raw.explode_factory, 0));
		sGameSounds.put(Sound.UPGRADE,       sSoundPool.load(sContext, R.raw.upgrade, 0));
	}
	
	public static void play(Sound sound) {
		if (Constants.sSound) {
			if (sSoundPool == null) {
				lazyInitialize();
			}
			
			// TODO: Implement directional sound.
			Integer loadedSound = sGameSounds.get(sound);
			if (loadedSound != null) {
				sSoundPool.play(loadedSound, 1f, 1f, 0, 0, 1f);
			}
		}
	}
}
