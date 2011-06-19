package com.gregmcnew.android.pax;

import android.util.Log;

public class Game {
	
	public enum State { IN_PROGRESS, RED_WINS, BLUE_WINS, TIE }
	public static final int NUM_PLAYERS = 2;
	
	public Game()
	{
		mPlayers = new Player[NUM_PLAYERS];
		for (int i = 0; i < NUM_PLAYERS; i++) {
			mPlayers[i] = new Player(i, NUM_PLAYERS);
		}
		reset();
	}
	
	// Restart the game on the next update().
	public void restart() {
		mRestart = true;
	}
	
	public Game.State getState() {
		return mState;
	}
	
	private long firstUpdateTime = 0;
	private long numUpdates = 0;
	
	public void update() {
		if (mRestart) {
			reset();
			mRestart = false;
		}
		if (mState != State.IN_PROGRESS) {
			return;
		}
		
		if (firstUpdateTime == 0) {
			firstUpdateTime = System.currentTimeMillis();
		}
		
		numUpdates++;
		if (numUpdates % 25 == 0) {
			long dt = System.currentTimeMillis() - firstUpdateTime;
			Log.v("Game.update", String.format("average update interval: %d", dt / numUpdates));
		}
		
		// Allow all players to produce and build.
		for (Player player : mPlayers) {
			player.produce();
			player.updateEntities();
			
			// Collision spaces should be marked as invalid when entities are being added or moved.
			player.invalidateCollisionSpaces();
			
			player.build();
			player.moveEntities();
			
			player.rebuildCollisionSpaces();
		}
		
		// Let projectiles kill stuff.
		for (Player player : mPlayers) {
			for (Player victim : mPlayers) {
				if (player != victim) {
					player.attack(victim);
				}
			}
		}
		
		for (Player player : mPlayers) {
			for (Entity entity : player.mRetargetQueue) {
				retarget(player, entity);
			}
			player.mRetargetQueue.clear();
		}
		
		// See if the game is over.
		boolean blueHasLost = mPlayers[0].hasLost();
		boolean redHasLost = mPlayers[1].hasLost();
		
		if (blueHasLost && redHasLost) {
			mState = State.TIE;
		}
		else if (blueHasLost) {
			mState = State.RED_WINS;
		}
		else if (redHasLost) {
			mState = State.BLUE_WINS;
		}
	}
	
	public void setBuildTarget(int player, Player.BuildTarget buildTarget)
	{
		mPlayers[player].mBuildTarget = buildTarget;
	}
	
	
	// Private methods
	
	private void reset() {
		for (Player player : mPlayers) {
			player.reset();
		}
		mState = State.IN_PROGRESS;
	}
	
	private void retarget(Player player, Entity entity) {
		entity.target = null;
		for (int i = 0; i < entity.targetPriorities.length && entity.target == null; i++) {
			
			int targetType = entity.targetPriorities[i];
			
			float searchLimit = 9000.1f; // XXX
			if (entity.targetSearchLimits != null) {
				searchLimit = entity.targetSearchLimits[i];
			}
			
			for (Player victim : mPlayers) {
				if (victim != player) {
					Entity closest = victim.mEntities[targetType].collide(entity.body.center, searchLimit);
					if (closest != null) {
						entity.target = closest;
						break;
					}
				}
			}
		}
	}
	
	public Player[] mPlayers;
	
	private Game.State mState;
	
	private boolean mRestart;

}
