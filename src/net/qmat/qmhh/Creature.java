/*
 * Based on Daniel Shiffman's Flocking example <http://www.shiffman.net>.
 */

package net.qmat.qmhh;

import java.util.ArrayList;

import org.jbox2d.dynamics.*;
import org.jbox2d.common.*;
import org.jbox2d.collision.shapes.*;

public class Creature extends ProcessingObject {
	
	private int stage = 0;
	private float w = 10;
	private float h = 10;
	private float maxForce = 5.0f;
	private float maxSpeed = 20.0f;
	private Hand target = null;
	private Body body;
	
	private static float DESIRED_SEPARATION = 30.0f;
	private static float NEIGHBOR_DISTANCE  = 50.0f;
	
	public Creature() {
		makeBody();
	}
	
	// This function adds the rectangle to the box2d world
	private void makeBody() {

	    // Define a polygon (this is what we use for a rectangle)
	    PolygonShape sd = new PolygonShape();
	    sd.setAsBox(box2d.scalarPixelsToWorld(w), 
	    			box2d.scalarPixelsToWorld(h));

	    // Define a fixture
	    FixtureDef fd = new FixtureDef();
	    fd.shape = sd;
	    // Parameters that affect physics
	    fd.density = 1;
	    fd.friction = 0.3f;
	    fd.restitution = 0.5f;

	    // Define the body and make it from the shape
	    BodyDef bd = new BodyDef();
	    bd.type = BodyType.DYNAMIC;
	    bd.position.set(box2d.coordPixelsToWorld(new Vec2(Main.centerX, 
	    												  Main.centerY)));

	    body = box2d.createBody(bd);
	    body.createFixture(fd);
	    
	    body.setLinearVelocity(new Vec2(p.random(-0.5f, 0.5f),
	    								p.random(-0.5f, 0.5f)));
	    body.setAngularVelocity(0.0f);
	}
	  
	public void destroy() {
		box2d.destroyBody(body);
	}
	
	public void draw() {
		p.pushMatrix();
		Vec2 loc = box2d.getBodyPixelCoord(body);
		p.translate(loc.x, loc.y);
		p.rotate(body.getAngle());
		p.rect(0, 0, w, h);
		p.popMatrix();
		
	}
	
	public void update(ArrayList<Creature> creatures) {
		if(target != null) {
			target();
		} else {
			flock(creatures);
		}
	}
	
	public void setTarget(Hand target) {
		if(target != null)
			this.target = target;
	}
	
	public void removeTarget() {
		this.target = null;
	}
	
	private void flock(ArrayList<Creature> creatures) {
		Vec2 sep = separate(creatures);
		Vec2 ali = align(creatures);
		Vec2 coh = cohesion(creatures);
		Vec2 loc = body.getWorldCenter();
		body.applyForce(sep,loc);
	    body.applyForce(ali,loc);
	    body.applyForce(coh,loc);
	}
	
	private Vec2 seek(Vec2 t, float force) {
		Vec2 loc = body.getWorldCenter();
		Vec2 desired = t.sub(loc);
		if (desired.length() == 0) 
			return new Vec2(0, 0);
		desired.normalize();
		desired.mulLocal(maxSpeed);
		Vec2 vel = body.getLinearVelocity();
		Vec2 steer = desired.sub(vel);
		if(steer.length() > force) {
			steer.normalize();
			steer.mulLocal(force);
		}
		return steer;
	}
	
	private Vec2 seek(Vec2 t) {
		return seek(t, maxForce);
	}
	
	private void target() {
		if(target != null) {
			body.applyForce(seek(box2d.coordPixelsToWorld(target.getCPosition().toVec2().mulLocal(5.0f)), maxForce * 2.0f), 
						    body.getWorldCenter());
		}
	}
	
	// Separation - method checks for nearby boids and steers away
	@SuppressWarnings("static-access")
	private Vec2 separate (ArrayList<Creature> creatures) {
		float desiredSeparation = box2d.scalarPixelsToWorld(DESIRED_SEPARATION);

		Vec2 steer = new Vec2(0,0);
		int count = 0;
		
		// For every boid in the system, check if it's too close
		Vec2 locA = body.getWorldCenter();
		for (Creature other : creatures) {
			Vec2 locB = other.body.getWorldCenter();
			
			float d = p.dist(locA.x, locA.y, locB.x, locB.y);
			// If the distance is greater than 0 and less than an arbitrary amount (0 when you are yourself)
			if ((d > 0.0f) && (d < desiredSeparation)) {
				// Calculate vector pointing away from neighbor
				Vec2 diff = locA.sub(locB);
				diff.normalize();
				diff.mulLocal(1.0f/d); // Weight by distance
				steer.addLocal(diff);
				count++;               // Keep track of how many
			}
		}
		// Average -- divide by how many
		if (count > 0) {
			steer.mulLocal(1.0f/count);
		}

		// As long as the vector is greater than 0
		if (steer.length() > 0.0f) {
			// Implement Reynolds: Steering = Desired - Velocity
			steer.normalize();
			steer.mulLocal(maxSpeed);
			Vec2 vel = body.getLinearVelocity();
			steer.subLocal(vel);
			if (steer.length() > maxForce) {
				steer.normalize();
				steer.mulLocal(maxForce);
			}
		}
		return steer;
	}

	// Alignment - for every nearby boid in the system, calculate the average velocity
	@SuppressWarnings("static-access")
	Vec2 align (ArrayList<Creature> creatures) {
		float neighborDist = box2d.scalarPixelsToWorld(NEIGHBOR_DISTANCE);
		Vec2 steer = new Vec2(0,0);
		int count = 0;
		Vec2 locA = body.getWorldCenter();
		for (Creature other : creatures) {
			Vec2 locB = other.body.getWorldCenter();
			
			float d = p.dist(locA.x,locA.y,locB.x,locB.y);
			if ((d > 0) && (d < neighborDist)) {
				Vec2 vel = other.body.getLinearVelocity();
				steer.addLocal(vel);
				count++;
			}
		}
		if (count > 0) {
			steer.mulLocal(1.0f/count);
		}

		// As long as the vector is greater than 0
		if (steer.length() > 0) {
			// Implement Reynolds: Steering = Desired - Velocity
			steer.normalize();
			steer.mulLocal(maxSpeed);
			Vec2 vel = body.getLinearVelocity();
			steer.subLocal(vel);
			if (steer.length() > maxForce) {
				steer.normalize();
				steer.mulLocal(maxForce);
			}
		}
		return steer;
	}

	// Cohesion - for the average location (i.e. center) of all nearby 
	// creatures, calculate steering vector towards that location
	@SuppressWarnings("static-access")
	Vec2 cohesion (ArrayList<Creature> creatures) {
		float neighborDist = box2d.scalarPixelsToWorld(NEIGHBOR_DISTANCE);
		Vec2 sum = new Vec2(0,0);   // Start with empty vector to accumulate all locations
		int count = 0;
		Vec2 locA = body.getWorldCenter();
		for (Creature other : creatures) {
			Vec2 locB = other.body.getWorldCenter();

			float d = p.dist(locA.x,locA.y,locB.x,locB.y);
			if ((d > 0) && (d < neighborDist)) {
				sum.addLocal(locB); // Add location
				count++;
			}
		}
		if (count > 0) {
			sum.mulLocal(1.0f/count);
			return seek(sum);  // Steer towards the location
		}
		return sum;
	}

	public PPoint2 getPPosition() {
		return getCPosition().toPPoint2();
	}
	
	public CPoint2 getCPosition() {
		Vec2 pos = box2d.getBodyPixelCoord(body);
		return new CPoint2(pos);
	}
	
	public boolean hasTargetP() {
		return target != null;
	}
	
}
