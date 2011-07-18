package com.gregmcnew.android.pax;

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
	
	public enum Difficulty { EASY, MEDIUM, HARD }
	
	// These weights were determined through experimentation. Over 3000 games
	// were played between the null AI (with weights of 0) and an AI with random
	// weights (re-randomized in each round). After each round, the random AI
	// was assigned a score: if they won, their score was the percentage of
	// health their factory had left. If they lost, their score was the
	// percentage of health the enemy's factory had left, negated. The resulting
	// scores and weights were run through linear regression to come up with the
	// following optimal weights.
	public static final float OPTIMAL_WEIGHTS[] = {
			0.6277f, -0.2779f,
			1.1031f, -0.1376f,
			1.0f
			};
	
	public float mWeights[];
	
	public AI(Player player) {
		mPlayer = player;
		mWeights = new float[OPTIMAL_WEIGHTS.length];
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
		for (int i = 0; i < OPTIMAL_WEIGHTS.length; i++) {
			mWeights[i] = mIntelligence * OPTIMAL_WEIGHTS[i];
		}
	}
	
	public void update(Player[] allPlayers) {
		
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
		
		int numEnemyAttackShips = mNumEnemyEntities[Ship.FIGHTER]
		                        + mNumEnemyEntities[Ship.BOMBER]
		                        + mNumEnemyEntities[Ship.FRIGATE];
		
		// Special-case when there are no enemy attack ships: just build something
		// at random (by leaving all weights equal).
		if (numEnemyAttackShips > 0) {
			setShipBuildWeights(shipBuildWeights);
		}
		
		//Log.v(Pax.TAG, String.format("AI build weights: %f, %f, %f", shipBuildWeights[0], shipBuildWeights[1], shipBuildWeights[2]));

		// Find the maximum build weight value.
		float minWeight = shipBuildWeights[0];
		float maxWeight = shipBuildWeights[0];
		for (int i = 1; i < shipBuildWeights.length; i++) {
			if (shipBuildWeights[i] < minWeight) {
				minWeight = shipBuildWeights[i];
			}
			else if (shipBuildWeights[i] > maxWeight) {
				maxWeight = shipBuildWeights[i];
			}
		}
		
		if (minWeight == maxWeight && mPlayer.mBuildTarget != Player.BuildTarget.NONE) {
			// All build weights are equal, so our choice will be random. The
			// player already has a build target, though, so we'll just keep it.
			// (Otherwise, the AI will rapidly switch between randomly-selected
			// build targets while weights are equal.)
			return;
		}

		float sumCostFactors = 0;
		for (int i = 0; i < shipBuildWeights.length; i++) {
			if (shipBuildWeights[i] >= maxWeight) {
				sumCostFactors += 1f / (float) Player.BuildCosts[i];
			}
		}
		
		// Randomly pick a build target with the maximum score. (There's usually
		// only one, but ties are possible.)
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
		                               - enemyFrigateMoney) * mWeights[0]) + mWeights[1];
		shipBuildWeights[Ship.BOMBER]  = ((enemyFrigateMoney
		                               - enemyFighterMoney) * mWeights[2]) + mWeights[3];
		shipBuildWeights[Ship.FRIGATE] = ((enemyFighterMoney
		                               - enemyBomberMoney)  * mWeights[4]);
	}
	
	// Intelligence ranges from -1 to 1:
	//    -1: build ships that will lose to enemy ships
	//     0: ignore what enemies have built
	//     1: build ships that will defeat enemy ships
	private float mIntelligence;

	private Player mPlayer;
	private int mNumEnemyEntities[] = new int[Entity.TYPES.length];
}
