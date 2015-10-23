package com.gregmcnew.android.pax;

public class Constants {
	
	// General settings
	
	public static final float GAME_SPEED_NORMAL = 1f;
	public static final float GAME_SPEED_FAST   = 3f;
	public static final float GAME_SPEED_INSANE = 6f;

	public static final int LOG_FRAMERATE_INTERVAL_UPDATES = -1; // in ms; make negative to disable
	public static final int UPDATE_INTERVAL_MS = 40;

	public static final float SEARCH_LIMIT = 1000f;
	public static final float MAX_SHOT_DISTANCE_SQUARED = 1000f * 1000f;
	public static final float INITIAL_FACTORY_DISTANCE = 250f;
	
	public static float sGameSpeed;
	
	public static AI.Difficulty sAIDifficulty;
	public static boolean sSound;
	public static boolean sBenchmarkMode;
	
	public static final boolean SELF_BENCHMARK = false;
	public static final boolean SIMPLE_BALANCE_TEST = false;
	public static final boolean BACKGROUND_IMAGE = false;
	public static final boolean MUSIC = false;
	public static final boolean PARTICLES = true;
  	public static final boolean AI_TRAINING_MODE = false;

	
	// Graphics settings

	public static boolean sShowShips;
	public static boolean sShowStars;
	public static boolean sShowParticles;
	public static boolean sShowCollisionBoxes;
	public static boolean sDebugMode;
	public static boolean sVertexBufferObjects;
	public static boolean sFastShipStyle;
	public static boolean sAllowUpgrades;
}
