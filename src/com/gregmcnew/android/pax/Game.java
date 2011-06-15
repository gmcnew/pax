package com.gregmcnew.android.pax;

public class Game {
	
	public enum State { IN_PROGRESS, RED_WINS, BLUE_WINS, TIE }
	public static int NUM_PLAYERS = 2;
	
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
		
		// Rebuild all octrees.
		for (Player player : mPlayers) {
			for (Entity.Type type : Entity.Type.values()) {
				
				// Tweak all of the points in the quadtree, then reset it.
				int i = 0;
				Quadtree eTree = player.mBodies.get(type);
				
				for (Entity e : player.mEntities.get(type)) {
					eTree.mPoints[i] = e.body.center;
					i++;
				}
				eTree.reset(0, i);
			}
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
	
	private void retarget(Player player, Entity entity) {
		entity.target = null;
		for (int i = 0; i < entity.targetPriorities.length && entity.target == null; i++) {
			
			Entity.Type targetType = entity.targetPriorities[i];
			
			float searchLimit = 9000.1f; // XXX
			if (entity.targetSearchLimits != null) {
				searchLimit = entity.targetSearchLimits[i];
			}
			
			// TODO: The quadtree only stores ships' centers, so we need to add
			// the radius of the target ship type to searchLimit. Or, better
			// yet, construct each quadtree with a float which it knows to add
			// to the searchLimit on every search.
			
			for (Player victim : mPlayers) {
				if (victim != player) {
					Point2 p = victim.mBodies.get(targetType).collide(entity.body.center, searchLimit);
					if (p != null) {
						assert(p.id != Entity.NO_ENTITY);
						entity.target = victim.mEntities.get(targetType).get(p.id);
					}
				}
			}
		}
	}
	
	public Player[] mPlayers;
	
	private Game.State mState;

}
