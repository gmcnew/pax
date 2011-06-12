package com.gregmcnew.android.pax;

import java.util.ArrayList;
import java.util.List;

public class Game {
	
	public enum State { IN_PROGRESS, RED_WINS, BLUE_WINS, TIE };
	public static int NUM_PLAYERS = 2;
	public static int NO_ENTITY = -1;
	
	public Game()
	{
		mPlayers = new ArrayList<Player>();
		for (int player = 0; player < NUM_PLAYERS; player++) {
			mPlayers.add(new Player(player, NUM_PLAYERS));
		}
		reset();
	}
	
	public void reset() {
		for (Player player : mPlayers) {
			player.reset();
		}
		mState = State.IN_PROGRESS;
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
			for (Player victim : mPlayers) {
				if (player != victim) {
					player.attack(victim);
				}
			}
		}
		
		// See if the game is over.
		boolean blueHasLost = mPlayers.get(0).hasLost();
		boolean redHasLost = mPlayers.get(1).hasLost();
		
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
		mPlayers.get(player).buildTarget = buildTarget;
	}
	
	public List<Player> mPlayers;
	
	private Game.State mState;

}
