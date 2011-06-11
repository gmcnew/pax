package com.gregmcnew.android.pax;

import java.util.Date;

public class Game {
	
	public enum State { IN_PROGRESS, RED_WINS, BLUE_WINS, TIE };
	public enum BuildTarget { FIGHTER, BOMBER, FRIGATE, UPGRADE, NONE };
	public static int[] UnitCosts = { 50, 170, 360, 1080, 0 };
	public static int NUM_PLAYERS = 2;
	
	public static int NO_ENTITY = -1;
	
	public Game()
	{
		reset();
	}
	
	public void reset() {
		mState = State.IN_PROGRESS;
		for (int player = 0; player < NUM_PLAYERS; player++) {
			mPlayers[player] = new Player();
		}
	}
	
	public Game.State getState() {
		return mState;
	}
	
	public void update() {
		if (mState != State.IN_PROGRESS) {
			return;
		}
		
		// Allow all players to produce and build.
		for (Player player : mPlayers) {
			player.produce();
			player.build();
			player.moveShips();
		}
		
		// TODO: Allow ships to shoot projectiles.
		
		// TODO: Allow ships and projectiles to move.
		
		// Figure out how much time has passed, since that affects
		// blob growth.
		long dt;
		if (Pax.SELF_BENCHMARK || Pax.BENCHMARK) {
			dt = Pax.UPDATE_INTERVAL_MS;
		}
		else {
			long now = new Date().getTime();
			dt = now - mLastUpdate;
			mLastUpdate = now;
		}
		/*
		// Grow.
		for (Blob blob : mBlobs) {
			blob.updateSize(dt);
			blob.getBoundingPolygon().reset();
		}
		
		// Collide.
		for (Blob i : mBlobs) {
			for (Blob j : mBlobs) {
				if (i != j) {
					i.collide(j);
				}
			}
		}
		
		// Remove dead blobs.
		List<Blob> deadBlobs = new ArrayList<Blob>();
		for (Blob blob : mBlobs) {
			blob.checkVitalSigns();
			if (blob.isDead()) {
				deadBlobs.add(blob);
			}
		}
		mBlobs.removeAll(deadBlobs);
		*/
	}
	
	public void setBuildTarget(int player, BuildTarget buildTarget)
	{
		mPlayers[player].buildTarget = buildTarget;
	}
	
	public Player[] mPlayers = new Player[NUM_PLAYERS];
	
	private Game.State mState;
	private long mLastUpdate;

}
