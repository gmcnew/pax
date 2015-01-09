package com.gregmcnew.android.pax;

import com.gregmcnew.android.pax.Player.BuildTarget;

public class AI {
	
	/**
	 * How the AI works
	 * ===============
	 * 
	 * An AI decides which ship to build in a given situation. This decision is
	 * made by examining enemy ships (specifically, the amount of money
	 * enemies have spent on the ships that are currently alive). The AI does
	 * not cheat -- it simply makes dumber decisions on lower difficulty levels.
	 * 
	 * 
	 * Build scores
	 * ------------
	 * 
	 * The AI generates a score for each unit type it can build. It then builds
	 * the ship with the biggest score. For example:
	 * 
	 *  	fighter score =  0.4
	 *  	bomber score  = -0.3
	 *      frigate score =  1.7
	 *      
	 * will cause the AI to build a frigate, as the frigate score is highest.
	 * (If multiple ship types are tied for highest score, the AI will pick one
	 * of them at random.)
	 * 
	 * These scores are calculated by examining enemy ships -- specifically, the
	 * amount of money represented by all enemy ships that are alive:
	 * 
	 * 		enemyFighterMoney = numEnemyFighters * Fighter.COST
	 * 		enemyBomberMoney  = numEnemyBombers  *  Bomber.COST
	 * 		enemyFrigateMoney = numEnemyFrigates * Frigate.COST
	 * 
	 * These enemy ship costs are used, with weights, to determine build scores.
	 * The formulas also have some basic knowledge of the game built-in. (For
	 * instance, we know that fighters do well against bombers but poorly
	 * against frigates.) The build score formulas are:
	 * 
	 * 		fighterScore = (enemyBomberMoney  - enemyFrigateMoney) * w[0] + w[1]
	 * 		bomberScore  = (enemyFrigateMoney - enemyFighterMoney) * w[2] + w[3]
	 * 		frigateScore = (enemyFighterMoney - enemyBomberMoney)  * w[4]
	 * 
	 * Assuming weight 0 is positive (which it is), this means that more
	 * fighters will be built when the enemies have a lot of bombers.
	 * 
	 * 
	 * Determining weights
	 * -------------------
	 * 
	 * Good weights were learned through playing games between the "null" AI 
	 * (an AI with all weights initialized to 0) and a random AI (with each
	 * weight set to a random value in [-1..1] before each game). After each
	 * game, the random AI's weights were printed, along with its "game score"
	 * (also [-1..1]). This game score was determined as follows:
	 * 
	 * 		tie:	0
	 *   	win: 	myFactoryHealth / initialFactoryHealth
	 *    	loss:	-enemyFactoryHealth / initialFactoryHealth
	 * 
	 * The game score will always be positive for a win and negative for a loss.
	 * 
	 * After thousands of such games, linear regression was used to create a
	 * function to approximate the score resulting from a set of weights.
	 * Given score 's' and weights w0..w4, the linear regression function was:
	 * 
	 *   	w0f0 + w1f1 + w2f2 + w3f3 + w4f4 + c = s
	 *   
	 * The resulting f0..f4 values are in the OPTIMAL_WEIGHTS array above.
	 * (These weights are almost certainly not truly optimal, but we're not
	 * aware of any better ones. =))
	 * 
	 * 
	 * Difficulty levels
	 * -----------------
	 * 
	 * The hardest difficulty is easy to implement: just use the optimal weights
	 * which were discovered through training. Medium difficulty is simple as
	 * well: use weights of 0, causing the AI to completely ignore what its
	 * enemy is building. The easiest AI is also trivial: negate the optimal
	 * weights and use those -- in other words, -try- to lose.
	 * 
	 * All other difficulty levels are implemented by randomly negating certain
	 * weights prior to making a decision. The more often we negate weights, the
	 * worse the AI will do. But how frequently should we negate weights?
	 * 
	 * The difficulty gap between adjacent levels should be constant. I.e.,
	 * moving from "easy" to "medium" should increase the AI's strength by the
	 * same relative amount as a jump from "medium" to "hard". To this end, the
	 * intermediate difficulty levels between "medium" and "hard ...
	 * 
	 * <to be continued>
	 */
	
	public enum Difficulty { BRAINDEAD, EASY, MEDIUM, HARD, VERY_HARD, INSANE, CHEATER }
	
	public AI(Player player) {
		mPlayer = player;
		mWeights = new AIWeights();
		setDifficulty(Difficulty.EASY);
		mCanUpgrade = false;
	}
	
	public void setDifficulty(Difficulty difficulty) {
		mIntelligence = 0f;
		
		mCanUpgrade = false;
		
		// The insane AI beats the medium AI about 93.6% of the time.
		// Difficulties between medium and insane are intended to be geometric
		// means. That is, the percentage of games that an AI wins against the
		// next-easiest AI should be constant (and is about 61%).
		switch (difficulty) {
			case BRAINDEAD: mIntelligence = -1f; 	 break;
			case EASY:		mIntelligence = -0.33f;  break;
			case MEDIUM:	mIntelligence =  0f;	 break;
			case HARD:		mIntelligence =  0.132f; break;
			case VERY_HARD: mIntelligence =  0.354f; break;
			case INSANE:	mIntelligence =  1f;     break;
			case CHEATER:
				mIntelligence =  1f;
				
				// Pay attention to the health of enemy units.
				mWeights.w[AIWeights.ENEMY_HEALTH] = 0.5f;
				break;
		}
	}
	
	public AIWeights getWeights() {
		return mWeights.clone();
	}
	
	public void randomizeWeights() {
		mWeights.randomize();
	}
	
	public void setWeights(AIWeights weights) {
		mWeights = weights;
	}
	
	public void buildFinished() {
		mPlayer.mBuildTarget = Player.BuildTarget.NONE;
		mBuilds++;
	}
	
	public void enableUpgrades() {
		mCanUpgrade = true;
	}
	
	public void disableUpgrades() {
		mCanUpgrade = false;
	}
	
	public void reset() {
		mBuilds = 0;
	}
	
	public void update(Player[] allPlayers) {
		
		countEntities(allPlayers);

		if (mPlayer.mBuildTarget == Player.BuildTarget.NONE) {
			resetDistortion();
		}
		
		/*
		if (mBuilds == 10 && mPlayer.playerNo == 0) {
			mPlayer.mBuildTarget = BuildTarget.UPGRADE;
			return;
		}
		*/

		setShipBuildScores();
		
		//Log.v(Pax.TAG, String.format("AI build weights: %f, %f, %f", shipBuildWeights[0], shipBuildWeights[1], shipBuildWeights[2]));

		// Find the maximum build weight value.
		float maxScore = mBuildScores[0];
		for (int i = 1; i < mBuildScores.length; i++) {
			if (mBuildScores[i] > maxScore) {
				maxScore = mBuildScores[i];
			}
		}
		
		int playerBuildTarget = mPlayer.mBuildTarget.ordinal();
		if (playerBuildTarget < mBuildScores.length
				&& mBuildScores[playerBuildTarget] >= maxScore) {
			// If the player's current build target is tied for the highest
			// score (or has the highest score outright), keep it. This prevents
			// the AI from rapidly switching between randomly-selected build
			// targets when multiple build targets are tied for the highest
			// score.
			return;
		}
		
		pickBuildTarget(mBuildScores, maxScore);
	}
	
	
	//
	// Private methods
	//
	
	private void countEntities(Player[] allPlayers) {
		// Count entities by type.
		for (int type : Ship.TYPES) {
			mNumEnemyEntities[type] = 0;
			for (Player player : allPlayers) {
				float healthWeight = mWeights.w[AIWeights.ENEMY_HEALTH];
				float count = 0;
				if (healthWeight == 0) {
					count += player.mEntities[type].size();
				}
				else {
					for (Entity e : player.mEntities[type]) {
						float percentHealthLost = 1f - (float) e.health / e.originalHealth;
						count += 1f - percentHealthLost * healthWeight;
					}
				}
				
				if (player != mPlayer) {
					mNumEnemyEntities[type] += count;
				}
				else {
					mNumOwnEntities[type] += count;
				}
			}
		}
	}

	// Randomly pick a build target with the maximum score. (There's usually
	// only one, but ties are possible.) Weigh by cost so expensive ships
	// aren't built too often.
	private void pickBuildTarget(float[] buildScores, float maxScore) {
		float sumCostFactors = 0;
		for (int i = 0; i < buildScores.length; i++) {
			if (buildScores[i] >= maxScore) {
				sumCostFactors += 1f / (float) Player.BuildCosts[i];
			}
		}
		
		float costFactors = 0;
		float r = Game.sRandom.nextFloat() * sumCostFactors;
		for (int i = 0; i < buildScores.length; i++) {
			if (buildScores[i] >= maxScore) {
				costFactors += 1f / (float) Player.BuildCosts[i];
				if (r <= costFactors) {
					mPlayer.mBuildTarget = Player.sBuildTargetValues[i];
					break;
				}
			}
		}
	}
	
	private void resetDistortion() {
		float threshold = (mIntelligence < 0) ? -mIntelligence : mIntelligence;
		mDistorted = (Game.sRandom.nextFloat() > threshold);
	}
	
	private void setShipBuildScores() {
		
		for (int i = 0; i < mBuildScores.length; i++) {
			mBuildScores[i] = 0;
		}

		// For now, make sure the upgrade score isn't the highest.
		mBuildScores[3] = mBuildScores[0] - 1;

		// If we're blinded, leave weights alone.
		if (mDistorted) {
			return;
		}
		
		float numEnemyAttackShips = mNumEnemyEntities[Ship.FIGHTER]
		                          + mNumEnemyEntities[Ship.BOMBER]
		                          + mNumEnemyEntities[Ship.FRIGATE];
		
		// Special-case when there are no enemy attack ships: just build something
		// at random (by leaving all weights equal).
		if (numEnemyAttackShips > 0) {

			float ourMoney[]   = new float[Ship.TYPES.length];
			float enemyMoney[] = new float[Ship.TYPES.length];
			for (int i = 0; i < Ship.TYPES.length; i++) {
				ourMoney[i]   = Player.BuildCosts[i] * mNumOwnEntities[i];
				enemyMoney[i] = Player.BuildCosts[i] * mNumEnemyEntities[i];
			}
	
			// Set scores based on (1) what would beat enemy ships and (2) what
			// would lose to enemy ships. For example, fighters beat bombers, so the
			// more bombers enemies have, the more fighters we should build.
			// However, fighters lose to frigates, so the more frigates enemies
			// have, the fewer fighters we should build.
			mBuildScores[Ship.FIGHTER] 	= ( (enemyMoney[Ship.BOMBER] - enemyMoney[Ship.FRIGATE])
										    * mWeights.w[AIWeights.FIGHTER_X]
										  )	+ mWeights.w[AIWeights.FIGHTER_C];
			mBuildScores[Ship.BOMBER]  	= ( (enemyMoney[Ship.FRIGATE] - enemyMoney[Ship.FIGHTER])
										    * mWeights.w[AIWeights.BOMBER_X]
										  ) + mWeights.w[AIWeights.BOMBER_C];
			mBuildScores[Ship.FRIGATE] 	= ( (enemyMoney[Ship.FIGHTER] - enemyMoney[Ship.BOMBER])
										    * mWeights.w[AIWeights.FRIGATE_X]
										  ) + mWeights.w[AIWeights.FRIGATE_C];
			
			if (mCanUpgrade) {

				float ownShipMoney   = ourMoney[Ship.FIGHTER]   + ourMoney[Ship.BOMBER]   + ourMoney[Ship.FRIGATE];
				float enemyShipMoney = enemyMoney[Ship.FIGHTER] + enemyMoney[Ship.BOMBER] + enemyMoney[Ship.FRIGATE];

				// The build score for an upgrade should take into account the total
				// amount of money represented by (1) enemy ships and (2) our own ships. 
				mBuildScores[3] 			= ( ( enemyShipMoney - ownShipMoney)
											    * mWeights.w[AIWeights.UPGRADE_X]
											  ) + mWeights.w[AIWeights.UPGRADE_C];
			}
			
			// If intelligence is negative, negate all scores.
			if (mIntelligence < 0) {
				for (int i = 0; i < mBuildScores.length; i++) {
					mBuildScores[i] *= -1;
				}
			}
		}
	}
	
	
	//
	// Private members
	//
	
	private boolean mDistorted = false;
	
	private int mBuilds = 0;
	
	private AIWeights mWeights;
	
	private boolean mCanUpgrade;
	
	// Intelligence ranges from -1 to 1:
	//    -1: build ships that will lose to enemy ships
	//     0: ignore what enemies have built
	//     1: build ships that will defeat enemy ships
	private float mIntelligence;

	private Player mPlayer;
	private float mNumOwnEntities[]   = new float[Entity.TYPES.length];
	private float mNumEnemyEntities[] = new float[Entity.TYPES.length];
	private float mBuildScores[] = new float[BuildTarget.values().length - 1];
}
