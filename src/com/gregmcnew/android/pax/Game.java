package com.gregmcnew.android.pax;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Game {
	
	public enum State { IN_PROGRESS, RED_WINS, BLUE_WINS, TIE };
	public static int NUM_PLAYERS = 2;
	public static int NO_ENTITY = -1;
	
	public Game()
	{
		reset();
	}
	
	public void reset() {
		mState = State.IN_PROGRESS;
		mPlayers = new ArrayList<Player>();
		for (int player = 0; player < NUM_PLAYERS; player++) {
			mPlayers.add(new Player());
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
			player.updateEntities();
		}
		
		// Let projectiles kill stuff.
		for (Player player : mPlayers) {
			player.tryToKill(mPlayers);
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
	
	public void setBuildTarget(int player, Player.BuildTarget buildTarget)
	{
		mPlayers.get(player).buildTarget = buildTarget;
	}
	
	public List<Player> mPlayers;
	
	private Game.State mState;
	private long mLastUpdate;

}
