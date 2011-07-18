package com.gregmcnew.android.pax;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class GameSounds {
	
	public enum Sound { SHOOT_BOMB, SHOOT_LASER, SHOOT_MISSILE, EXPLOSION, UPGRADE }	
	
	private static SoundPool sSoundPool = null;
	
	private static Map<Sound, Integer> sGameSounds;
	
	public static void initialize(Context context) {
		if (sSoundPool != null) {
			sSoundPool.release();
		}
		sGameSounds = new HashMap<Sound, Integer>();
		sSoundPool = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
		
		sGameSounds.put(Sound.SHOOT_BOMB,    sSoundPool.load(context, R.raw.shoot_bomb, 0));
		sGameSounds.put(Sound.SHOOT_LASER,   sSoundPool.load(context, R.raw.shoot_laser, 0));
		sGameSounds.put(Sound.SHOOT_MISSILE, sSoundPool.load(context, R.raw.shoot_missile, 0));
		sGameSounds.put(Sound.EXPLOSION,     sSoundPool.load(context, R.raw.explode_factory, 0));
		sGameSounds.put(Sound.UPGRADE,       sSoundPool.load(context, R.raw.upgrade, 0));
	}
	
	public static void play(Sound sound) {
		if (sGameSounds != null && Pax.sSound) {
			// TODO: Implement directional sound.
			Integer loadedSound = sGameSounds.get(sound);
			if (loadedSound != null) {
				sSoundPool.play(loadedSound, 1f, 1f, 0, 0, 1f);
			}
		}
	}
}
