package com.gregmcnew.android.pax;

public class Game {
	
	public enum State { IN_PROGRESS, RED_WINS, BLUE_WINS, TIE };
	public static int NUM_PLAYERS = 2;
	public static int NO_ENTITY = -1;
	
	public Game()
	{
		mPlayers = new Player[NUM_PLAYERS];
		for (int i = 0; i < NUM_PLAYERS; i++) {
			mPlayers[i] = new Player(i, NUM_PLAYERS);
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
		mPlayers[player].buildTarget = buildTarget;
	}
	
	public Player[] mPlayers;
	
	private Game.State mState;

}
