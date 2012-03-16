package net.qmat.qmhh.models;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

import net.qmat.qmhh.Main;
import net.qmat.qmhh.utils.CPoint2;
import net.qmat.qmhh.utils.PPoint2;

public class Hand extends ProcessingObject {
	
	private float x = 0.0f;
	private float y = 0.0f;
	private float radius = 10.0f;
	
	private boolean rebuildBeamP;
	private Beam beam;
	
	public Hand(float x, float y) {
		updatePosition(x, y);
		rebuildBeamP = true;
		
	}
	
	public void updatePosition(float x, float y) {
		this.x = x;
		this.y = y;
		rebuildBeamP = true;
	}
	
	public CPoint2 getCPosition() {
		return new CPoint2(x, y);
	}
	
	private void rebuildBeam() {
		if(beam != null) beam.rebuildShape();
		else beam = new Beam(this);
		rebuildBeamP = false;
	}
	
	
	public void draw() {
		if(rebuildBeamP) rebuildBeam();
		
		p.noStroke();
		p.fill(p.color(0, 155, 0));
		p.ellipse(x, y, radius, radius);
		//p.stroke(p.color(200, 200, 0));
		//p.line(x, y, Main.centerX, Main.centerY);
		
		beam.draw();
	}
	
	public void destroy() {
		if(beam != null) beam.destroy();
	}
	
	private class Beam extends ProcessingObject {
		
		Body body;
		Hand hand;
		
		Beam(Hand hand) {
			this.hand = hand;
			makeBody();
		}
		
		private void rebuildShape() {
			// update size of the body
			Fixture f = body.getFixtureList();
			while(f != null) {
				body.destroyFixture(f);
				f = f.getNext();
			}
			body.createFixture(createFixture());
		}
		
		private void makeBody() {

			FixtureDef fd = createFixture();

			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			// set position to be in between the center and the hand position
			bd.position.set(box2d.coordPixelsToWorld(new Vec2((hand.x + Main.centerX)*0.5f, (hand.y + Main.centerY)*0.5f)));
			bd.angle = new CPoint2(hand.x, hand.y).toPPoint2().t;

			body = box2d.createBody(bd);
			body.createFixture(fd);
			body.setUserData(this);
		}
		
		private FixtureDef createFixture() {
			PolygonShape sd = new PolygonShape();
			PPoint2 handPos = new CPoint2(x, y).toPPoint2();
			// TODO: calculate the actual angleOffset from the hand size
			float angleOffset = Main.TWO_PI / 92.0f;
			PPoint2 v1 = new PPoint2(handPos.r, handPos.r - angleOffset);
			PPoint2 v2 = new PPoint2(handPos.r, handPos.r + angleOffset);
			// offset from the middle is perpendicular to the beam
			PPoint2 v3 = new PPoint2(10.0f, handPos.r + Main.PI/2.0f);
			PPoint2 v4 = new PPoint2(10.0f, handPos.r - Main.PI/2.0f);
			Vec2 vs[] = new Vec2[4];
			vs[0] = box2d.coordPixelsToWorld(v1.toVec2());
			vs[1] = box2d.coordPixelsToWorld(v2.toVec2());
			vs[2] = box2d.coordPixelsToWorld(v3.toVec2());
			vs[3] = box2d.coordPixelsToWorld(v4.toVec2());
			sd.set(vs, 4);
			FixtureDef fd = new FixtureDef();
			fd.shape = sd;
			fd.isSensor = true;
			return fd;
		}

		public void destroy() {
			box2d.destroyBody(body);
		}
		
		public void draw() {
			p.fill(237, 212, 69, 150);
			p.stroke(234, 187, 31);
			p.beginShape();
			for(Fixture f=body.getFixtureList(); f!=null; f=f.getNext()) {
				PolygonShape shape = (PolygonShape) f.getShape();
				Transform transform = body.getTransform();
				for(int i=0; i<shape.getVertexCount(); i++) {
					Vec2 pos = shape.getVertex(i);
					Vec2 v2 = box2d.coordWorldToPixels(Transform.mul(transform, pos));
					p.vertex(v2.x, v2.y);
					p.println(v2.x + " " + v2.y);
				}
			}
			p.endShape(Main.CLOSE);
		}
		
	}
	
}
