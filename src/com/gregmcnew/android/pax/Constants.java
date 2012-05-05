package com.gregmcnew.android.pax;

public class Constants {
	
	// General settings
	
	public static final float GAME_SPEED_NORMAL = 1f;
	public static final float GAME_SPEED_FAST   = 3f;
	public static final float GAME_SPEED_INSANE = 6f;
	
	public static float sGameSpeed;
	
	public static AI.Difficulty sAIDifficulty;
	public static boolean sSound;
	public static boolean sBenchmarkMode;
	
	public static final boolean SELF_BENCHMARK = false;
	public static final boolean SIMPLE_BALANCE_TEST = false;
	public static final boolean BACKGROUND_IMAGE = false;
	public static final boolean FIGHTER_SPAM_TEST = false;
	public static final boolean MUSIC = false;
	public static final boolean PARTICLES = true;
  	public static final boolean AI_TRAINING_MODE = false;

	
	// Graphics settings
	
	public static boolean sShowShips;
	public static boolean sShowParticles;
	public static boolean sShowCollisionBoxes;
	public static boolean sShowFPS;
	public static boolean sShowHealthForAllShipTypes;
	public static boolean sVertexBufferObjects;
	public static boolean sFadeOutIntro;
}
