package com.gregmcnew.android.pax;

import android.util.Log;

public class AI {
	
	public enum Difficulty { EASY, MEDIUM, HARD }
	
	public float mWeightParameters[] = { 0, 0, 0, 0, 0, 0 };
	/*
	public float mWeightParameters[] = {
			 0.3395f,  0,
			 0.4651f, -0.0496f,
		     0.3816f,  0,
			};
	*/
	//public float mWeightParameters[] = { 1, 1, 1, 1, 1, 1 };

	
	public AI(Player player) {
		mPlayer = player;
		setDifficulty(Difficulty.EASY);
	}
	
	public void setDifficulty(Difficulty difficulty) {
		switch (difficulty) {
			case EASY:
				mIntelligence = -1f;
				break;
			case MEDIUM:
			default:
				mIntelligence = 0;
				break;
			case HARD:
				mIntelligence = 1f;
				break;
		}
	}
	
	public void update(Player[] allPlayers) {
		
		// Do nothing if the player already has a build target.
		if (mPlayer.mBuildTarget != Player.BuildTarget.NONE) {
			return;
		}
		
		// Count enemy entities by type.
		for (int type : Entity.TYPES) {
			mNumEnemyEntities[type] = 0;
			for (Player player : allPlayers) {
				if (player != mPlayer) {
					mNumEnemyEntities[type] += player.mEntities[type].size();
				}
			}
		}

		float[] shipBuildWeights = { 0, 0, 0 };
		
		setShipBuildWeights(shipBuildWeights);
		
		//Log.v(Pax.TAG, String.format("AI build weights: %f, %f, %f", shipBuildWeights[0], shipBuildWeights[1], shipBuildWeights[2]));

		// Find the maximum build weight value.
		float maxWeight = shipBuildWeights[0];
		for (int i = 0; i < shipBuildWeights.length; i++) {
			if (shipBuildWeights[i] >= maxWeight) {
				maxWeight = shipBuildWeights[i];
			}
		}

		float sumCostFactors = 0;
		for (int i = 0; i < shipBuildWeights.length; i++) {
			if (shipBuildWeights[i] >= maxWeight) {
				sumCostFactors += 1f / (float) Player.BuildCosts[i];
			}
		}
		
		float costFactors = 0;
		float r = Pax.sRandom.nextFloat() * sumCostFactors;
		for (int i = 0; i < shipBuildWeights.length; i++) {
			if (shipBuildWeights[i] >= maxWeight) {
				costFactors += 1f / (float) Player.BuildCosts[i];
				if (r <= costFactors) {
					mPlayer.mBuildTarget = Player.sBuildTargetValues[i];
					break;
				}
			}
		}
	}
	
	private void setShipBuildWeights(float shipBuildWeights[]) {

		int enemyFighterMoney = Player.BuildCosts[Ship.FIGHTER] * mNumEnemyEntities[Ship.FIGHTER];
		int enemyBomberMoney  = Player.BuildCosts[Ship.BOMBER]  * mNumEnemyEntities[Ship.BOMBER];
		int enemyFrigateMoney = Player.BuildCosts[Ship.FRIGATE] * mNumEnemyEntities[Ship.FRIGATE];

		// Set weights based on (1) what would beat enemy ships and (2) what
		// would lose to enemy ships. For example, fighters beat bombers, so the
		// more bombers enemies have, the more fighters we should build.
		// However, fighters lose to frigates, so the more frigates enemies
		// have, the fewer fighters we should build.
		shipBuildWeights[Ship.FIGHTER] = ((enemyBomberMoney
		                               - enemyFrigateMoney) * mWeightParameters[0]) + mWeightParameters[1];
		shipBuildWeights[Ship.BOMBER]  = ((enemyFrigateMoney
		                               - enemyFighterMoney) * mWeightParameters[2]) + mWeightParameters[3];
		shipBuildWeights[Ship.FRIGATE] = ((enemyFighterMoney
		                               - enemyBomberMoney)  * mWeightParameters[4]) + mWeightParameters[5];
		
		/*
		// Enemy ships are important if intelligence is positive or negative but
		// are completely ignored when intelligence is zero. (mIntelligence is
		// between -1 and 1, so enemyShipImportance will be between 0 and 1.)
		float enemyShipImportance = (mIntelligence < 0) ? -mIntelligence : mIntelligence;

		float sum = 0;
		for (int i = 0; i < shipBuildWeights.length; i++) {

			// If intelligence is negative, negate weights.
			if (mIntelligence < 0) {
				shipBuildWeights[i] = -shipBuildWeights[i];
			}
			
			// Zero negative weights.
			if (shipBuildWeights[i] < 0) {
				shipBuildWeights[i] = 0;
			}
			
			sum += shipBuildWeights[i];
		}
		
		// Use equal weights if all are zero.
		if (sum == 0) {
			for (int i = 0; i < shipBuildWeights.length; i++) {
				shipBuildWeights[i] = 1;
				sum++;
			}
		}
		
		for (int i = 0; i < shipBuildWeights.length; i++) {
			
			// Make each weight relative to the sum.
			shipBuildWeights[i] /= sum;
			
			// Fudge the build weights: as enemyShipImportance drops to 0,
			// converge each build weight toward (1 / shipBuildWeights.length).
			// When enemyShipImportance is 0, this will effectively ignore enemy
			// ships.
			shipBuildWeights[i] *= enemyShipImportance;
			shipBuildWeights[i] += (1.0f - enemyShipImportance) / shipBuildWeights.length;
		
			// Don't allow any weights to be negative. (This can occur when
			// intelligence is negative.)
			shipBuildWeights[i] = shipBuildWeights[i] < 0 ? 0 : shipBuildWeights[i];

			// So far, shipBuildWeights just tells us which ship type we should
			// focus on building. We need to take ship cost into account, though, so
			// we don't build expensive ships too frequently.
			//shipBuildWeights[i] /= Player.BuildCosts[i];
		}
		
		// Re-normalize.
		sum = 0;
		for (float f : shipBuildWeights) {
			sum += f;
		}
		for (int i = 0; i < shipBuildWeights.length; i++) {
			shipBuildWeights[i] /= sum;
		}
		sum = 1;
		*/
	}
	
	// Intelligence ranges from -1 to 1:
	//    -1: build ships that will lose to enemy ships
	//     0: ignore what enemies have built
	//     1: build ships that will defeat enemy ships
	private float mIntelligence;

	private Player mPlayer;
	private int mNumEnemyEntities[] = new int[Entity.TYPES.length];
}
