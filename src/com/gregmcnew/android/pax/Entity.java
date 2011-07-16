package com.gregmcnew.android.pax;

import android.graphics.PointF;

/**Abstract class. Represents any entity in the game's id, health, velocity, etc.**/
public abstract class Entity {
	
	public static final int NO_ENTITY = -1;
	
	// 24.8 days ought to be enough for anybody.
	public static final int INFINITE_LIFE_MS = Integer.MAX_VALUE;
	
	public final int type;
	public final int[] targetPriorities;
	public final float[] targetSearchLimits;
	public final float radius;
	public final float diameter;
	public final float length; // equal to diameter for circular targets
	public final float turnSpeed; // in radians per second
	public final float[] accelerationLimits;
	public final float maxSpeed;
	public final int originalLifeMs;
	public final CircleF body;
	public final PointF velocity;
	
	public int lifeMs;
	public int health;
	
	public Entity target;
	public float targetHeading;
	public float headingToTargetHeading; // difference between heading and targetHeading.
	public float heading; // in radians
	
	// An entity can have extra collision circles, each specified as a
	// (Point2, float) pair. The Point2 is the center of a collision circle with
	// the same radius as 'body'. The float is the number of ship radii by which
	// the point is ahead (i.e., toward 'heading') of the ship's center.
	protected Point2[] mExtraPoints = { };
	protected float[] mExtraPointOffsets = { };
	
	private int mRetargetCounter;
	
	protected int id;

	public static final int FIGHTER = 0;
	public static final int BOMBER  = 1;
	public static final int FRIGATE = 2;
	public static final int FACTORY = 3;
	public static final int LASER   = 4;
	public static final int BOMB    = 5;
	public static final int MISSILE = 6;
	public static final int[] TYPES = { FIGHTER, BOMBER, FRIGATE, FACTORY, LASER, BOMB, MISSILE };
	
	public static float[] CollisionRadii = {
		Fighter.DIAMETER / 2, Bomber.DIAMETER / 2, Frigate.DIAMETER / 2, Factory.DIAMETER / 2,
		Laser.DIAMETER / 2, Bomb.DIAMETER / 2, Missile.DIAMETER / 2
		};
	
	private final int originalHealth;
	
	protected Entity(int Type, int[] TargetPriorities, float[] TargetSearchLimits, int LifeMs, int Health, float Diameter, float Length, float TurnSpeed, float[] AccelerationLimits, float MaxSpeed) {
		type = Type;
		targetPriorities = TargetPriorities;
		targetSearchLimits = TargetSearchLimits;
		originalLifeMs = LifeMs;
		radius = Diameter / 2;
		diameter = Diameter;
		length = Length;
		turnSpeed = TurnSpeed;
		accelerationLimits = AccelerationLimits;
		maxSpeed = MaxSpeed;
		
		body = new CircleF(new Point2(), radius);
		velocity = new PointF();
		
		originalHealth = Health;
		
		id = NO_ENTITY;
	}
	
	protected void reset(Ship parent) {
		lifeMs = originalLifeMs;
		health = originalHealth;
		
		target = null;
		
		body.center.x = 0;
		body.center.y = 0;
		heading = 0f;
		velocity.x = 0;
		velocity.y = 0;
		
		mRetargetCounter = Pax.sRandom.nextInt(100);
	}
	
	/**Sets the ship to max speed on its current heading.**/
	public void fullSpeedAhead(){
		velocity.x = (float) Math.cos(heading) * maxSpeed;
		velocity.y = (float) Math.sin(heading) * maxSpeed;
	}
	
	public void emitParticles(Emitter[] emitters, long dt) {
		
	}
	
	public boolean wantsNewTarget() {
		
		boolean retarget = false;
		
		if (targetPriorities != null) {
			
			mRetargetCounter = (mRetargetCounter + 1) % 100;
			
			retarget = false
				// Retarget once in a while no matter what else is going on.
				|| (mRetargetCounter == 0)
				
				// Forget about dead targets.
				|| 	(target != null && target.health <= 0)
				
				// Retarget fairly often while I don't have a target.
				|| 	(target == null && mRetargetCounter % 10 == 0)
				;
		}
		
		return retarget;
	}
	
	public void updatePosition(long dt) {
		float dx_t = velocity.x * dt / 1000;
		float dy_t = velocity.y * dt / 1000;
		body.center.add(dx_t, dy_t);
		
		// If an entity has extra collision points, move them, too.
		if (mExtraPoints.length > 0) {
			float headingX = (float) Math.cos(heading);
			float headingY = (float) Math.sin(heading);
			for (int i = 0; i < mExtraPoints.length; i++) {
				Point2 point = mExtraPoints[i];
				float offset = radius * mExtraPointOffsets[i];
				point.set(body.center);
				point.add(headingX * offset, headingY * offset);
			}
		}
	}
	
	public void updateHeading(long dt) {
		float dx, dy;
		if (target != null) {
			dx = target.body.center.x - body.center.x;
			dy = target.body.center.y - body.center.y;
			
			float leadSpeed = 0f;
			
			if (type == MISSILE) {
				// Lead the target using our max speed.
				leadSpeed = Missile.MAX_VELOCITY;
			}
			else if (type == FIGHTER) {
				
				Fighter fighter = (Fighter) this;
				
				if (fighter.shotsLeft == 0 && fighter.reloadTimer < fighter.reloadTimeMs / 2) {
					// Run away during the first half of the reload timer.
					fighter.mIsRunningAway = true;
				}
				else if (fighter.mIsRunningAway) {
					// We have ammo (or are at least halfway reloaded), but
					// we're still in the "run away" state because we've been
					// too close to our target. See if we're far enough away
					// now.
					float minDistance = Fighter.MIN_PREFERRED_TARGET_DISTANCE;
					if (body.center.distanceToSquared(target.body.center) > minDistance * minDistance) {
						fighter.mIsRunningAway = false;
					}
				}
				
				if (fighter.mIsRunningAway) {
					// Try to move away from the target.
					dx = -dx;
					dy = -dy;
					
					// Cheat toward the center, slightly -- this will help keep
					// fighters from becoming lost offscreen.
					dx -= body.center.x / 20;
					dy -= body.center.y / 20;
				}
				else {
					// Lead the target using the laser's speed.
					leadSpeed = Laser.MAX_VELOCITY;
				}
			}
			
			if (leadSpeed != 0) {
				double distanceToTarget = Math.sqrt(dx * dx + dy * dy);
				
				// How long would it take to reach the target at the given
				// speed?
				double timeToTarget = distanceToTarget / leadSpeed;
				
				// Aim for where the target is going to be.
				dx += target.velocity.x * timeToTarget;
				dy += target.velocity.y * timeToTarget;
			}
		}
		else {
			dx = -body.center.x;
			dy = -body.center.y;
		}
		
		float pi = (float) Math.PI;
		
		// [-pi..pi]
		targetHeading = (float) Math.atan2((double) dy, (double) dx);
		
		//Gets the difference within +/- 2*pi.
		headingToTargetHeading = targetHeading - heading;
		
		// Clamp to [-pi .. pi]
		while (headingToTargetHeading < -pi) {
			headingToTargetHeading += 2 * pi;
		}
		while (headingToTargetHeading > pi) {
			headingToTargetHeading -= 2 * pi;
		}
		
		float turn = headingToTargetHeading;
		
		float maxTurn = turnSpeed * dt / 1000;
		
		if (turn < -maxTurn) {
			turn = -maxTurn;
		}
		else if (turn > maxTurn) {
			turn = maxTurn;
		}

		heading += turn;
	}
	
	public void updateVelocity(long dt) {
		//Find velocities components in terms of the ships coord. frame.
		float cosH = (float)Math.cos(heading);
		float sinH = (float)Math.sin(heading);
		float velH = velocity.y * sinH + velocity.x * cosH; //Speed in the direction of heading.
		float velP = velocity.x * sinH - velocity.y * cosH; //Speed in the direction 90deg CW of heading.
		
		// TODO: "/ 40"? Specify acceleration limits correctly.
		float dvP = accelerationLimits[1] * dt / 40;
		float dvH = accelerationLimits[0] * dt / 40;
		
		float newVelP = (velP > 0 ? velP : -velP);
		newVelP -= dvP;
		if (newVelP < 0) {
			newVelP = 0;
		}
		velP = (velP > 0 ? newVelP : -newVelP);
		
		float halfPi = (float) Math.PI / 2;
		
		if (headingToTargetHeading < -halfPi || headingToTargetHeading > halfPi) {
			velH -= dvH;
			if(velH < .75*maxSpeed) velH = (float).75*maxSpeed;
		} else { 
			velH += dvH * Math.cos(headingToTargetHeading);
			if(velH > maxSpeed) velH = maxSpeed;
		}
		velocity.x = velH * cosH + velP * sinH;
		velocity.y = velH * sinH - velP * cosH;
	}
	
	
	// Protected methods
	
	public void setExtraPoints(int numPoints, float[] offsets) {
		mExtraPointOffsets = offsets;
		
		for (int i = 0; i < mExtraPoints.length; i++) {
			mExtraPoints[i].set(body.center);
		}
	}
}
