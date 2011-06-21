package com.gregmcnew.android.pax;

import android.graphics.PointF;

/**Abstract class. Represents any entity in the game's id, health, velocity, etc.**/
public abstract class Entity {
	
	public static final int NO_ENTITY = -1;
	
	public final int type;
	public final int[] targetPriorities;
	public final float[] targetSearchLimits;
	public final float radius;
	public final float diameter;
	public final float turnSpeed;
	public final float[] accelerationLimits;
	public final float maxSpeed;
	public final float pi = (float)Math.PI;
	
	public int health;
	
	public Entity target;
	public float targetHeading = pi/2; 
	public float difference; // difference between heading and targetHeading.	
	public CircleF body;
	public float heading; // in radians
	public PointF velocity;
	
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
	
	public static float[] Radii = {
		Fighter.DIAMETER / 2, Bomber.DIAMETER / 2, Frigate.DIAMETER / 2, Factory.DIAMETER / 2,
		Laser.DIAMETER / 2, Bomb.DIAMETER / 2, Missile.DIAMETER / 2
		};
	
	protected Entity(int Type, int[] TargetPriorities, float[] TargetSearchLimits, int Health, float Diameter, float TurnSpeed, float[] AccelerationLimits, float MaxSpeed) {
		type = Type;
		targetPriorities = TargetPriorities;
		targetSearchLimits = TargetSearchLimits;
		radius = Diameter / 2;
		diameter = Diameter;
		turnSpeed = TurnSpeed;
		accelerationLimits = AccelerationLimits;
		maxSpeed = MaxSpeed;
		
		health = Health;
		
		target = null;
		
		id = NO_ENTITY;
		
		body = new CircleF(new Point2(), radius);
		heading = 0f;
		velocity = new PointF();
		
		mRetargetCounter = Pax.sRandom.nextInt(100);
	}
	
	/**Sets the ship to max speed on its current heading.**/
	public void fullSpeedAhead(){
		velocity.x = (float) Math.cos(heading) * maxSpeed;
		velocity.y = (float) Math.sin(heading) * maxSpeed;
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
	
	public void updatePosition(){
		float dx_t = velocity.x * Pax.UPDATE_INTERVAL_MS / 1000;
		float dy_t = velocity.y * Pax.UPDATE_INTERVAL_MS / 1000;
		body.center.offset(dx_t, dy_t);
		
		// If an entity has extra collision points, move them, too.
		if (mExtraPoints.length > 0) {
			float headingX = (float) Math.cos(heading);
			float headingY = (float) Math.sin(heading);
			for (int i = 0; i < mExtraPoints.length; i++) {
				Point2 point = mExtraPoints[i];
				float offset = radius * mExtraPointOffsets[i];
				point.set(body.center);
				point.offset(headingX * offset, headingY * offset);
			}
		}
	}
	
	public void updateHeading(){
		float dx, dy;
		if (target != null) {
			dx = target.body.center.x - body.center.x;
			dy = target.body.center.y - body.center.y;
			
			float leadSpeed = 0f;
			
			if (type == MISSILE) {
				// Lead the target using our speed.
				leadSpeed = Missile.INITIAL_VELOCITY;
			}
			else if (type == FIGHTER) {
				
				Ship fighter = (Ship) this;
				if (fighter.shotsLeft == 0 && fighter.reloadTimer < fighter.reloadTimeMs / 2) {
					// Run away, run away!
					dx = -dx;
					dy = -dy;
				}
				else {
					// Lead the target using the laser's speed.
					leadSpeed = Laser.INITIAL_VELOCITY;
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
		targetHeading = (float) Math.atan2((double) dy, (double) dx);
		if (targetHeading <= 0){
			targetHeading = (float)Math.PI * 2 + targetHeading; // Normalizes to [0...2pi] instead of [-pi...pi]
		}
		//Gets the difference within +/- 2*pi.
		difference = (targetHeading - heading) % (pi*2);
		
		//Gets difference within +/- pi.
		if(Math.abs(difference) >= pi){
			difference += (difference > 0) ? -2*pi : 2*pi;
		}
		
		if(Math.abs(difference) >= turnSpeed){ //If the difference is less than what the ship can turn in one update...
			if(difference >= 0){
				heading += turnSpeed;
			} else {
				heading -= turnSpeed;
			}
		} else {
			heading += difference;
		}
	}
	
	public void updateVelocity(){
		//Find velocities components in terms of the ships coord. frame.
		float cosH = (float)Math.cos(heading);
		float sinH = (float)Math.sin(heading);
		float velH = velocity.y * sinH + velocity.x * cosH; //Speed in the direction of heading.
		float velP = velocity.x * sinH - velocity.y * cosH; //Speed in the direction 90deg CW of heading.
		if(velP > 0){
			velP = (velP > accelerationLimits[1]) ? velP - accelerationLimits[1] : 0;
		} else if(velP < 0) {
			velP = (Math.abs(velP) > accelerationLimits[1]) ? velP + accelerationLimits[1] : 0;
		}
		if(Math.abs(difference) > pi/2){
			velH -= accelerationLimits[0];
			if(velH < .75*maxSpeed) velH = (float).75*maxSpeed;
		} else { 
			velH += accelerationLimits[0]*Math.cos(difference);
			if(velH > maxSpeed) velH = maxSpeed;
		}
		velocity.x = velH * cosH + velP * sinH;
		velocity.y = velH * sinH - velP * cosH;
	}
	
	public void move(){
		updatePosition();
		updateHeading();
		updateVelocity();
	}
	
	
	// Protected methods
	
	public void setExtraPoints(int numPoints, float[] offsets) {
		mExtraPoints = new Point2[offsets.length];
		mExtraPointOffsets = offsets;
		
		for (int i = 0; i < mExtraPoints.length; i++) {
			mExtraPoints[i] = new Point2();
			mExtraPoints[i].set(body.center);
		}
	}
}
