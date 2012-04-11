package net.qmat.qmhh.models;

import net.qmat.qmhh.Main;
import net.qmat.qmhh.utils.CPoint2;
import net.qmat.qmhh.utils.PPoint2;
import net.qmat.qmhh.utils.Settings;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;

import processing.core.PGraphics;
import processing.core.PImage;

public class Background extends ProcessingObject {

	private static int NR_SECTIONS = 16;

	private float centerX, centerY;
	private float innerDiameter, outerDiameter;
	private float innerRadius, outerRadius;
	private Body body;
	private Vec2[] vertices;
	private GLTexture backdropImageRevealed;
	private GLTexture maskedBackdropRevealed;
	private PImage backdropImageUnrevealed;
	private PImage backdropGradient;
	private GLGraphicsOffScreen backdropRevealed;
	private GLGraphicsOffScreen backdropMask;
	private PGraphics backdropUnrevealed;
	private GLTextureFilter maskFilter;

	public Background() {
		centerX = Settings.getInteger(Settings.PR_CENTER_X);
		centerY = Settings.getInteger(Settings.PR_CENTER_Y);
		innerDiameter = Settings.getInteger(Settings.PR_RING_INNER_RADIUS) * 2.0f;
		outerDiameter = Settings.getInteger(Settings.PR_RING_OUTER_RADIUS) * 2.0f;
		innerRadius = innerDiameter / 2.0f;
		outerRadius = outerDiameter / 2.0f;

		backdropImageRevealed = new GLTexture(p, Settings.getString(Settings.PR_BACKDROP_REVEALED));
		backdropImageUnrevealed = p.loadImage(Settings.getString(Settings.PR_BACKDROP_UNREVEALED));

		// scale the backdrop
		backdropRevealed = new GLGraphicsOffScreen(p, Main.centerX*2, Main.centerY*2);
		backdropRevealed.beginDraw();
		backdropRevealed.translate(Main.centerX, Main.centerY);
		backdropRevealed.imageMode(Main.CENTER);
		backdropRevealed.image(backdropImageRevealed, 0, 0, backdropRevealed.width, backdropRevealed.height);
		backdropRevealed.endDraw();
		backdropMask = new GLGraphicsOffScreen(p, Main.centerX*2, Main.centerY*2);
		backdropMask.beginDraw();
		backdropMask.background(0);
		backdropMask.endDraw();
		maskedBackdropRevealed = new GLTexture(p, Main.centerX*2, Main.centerY*2);

		backdropUnrevealed = p.createGraphics(Main.centerX*2, Main.centerY*2, Main.P3D);
		backdropUnrevealed.beginDraw();
		backdropUnrevealed.translate(Main.centerX, Main.centerY);
		backdropUnrevealed.imageMode(Main.CENTER);
		backdropUnrevealed.image(backdropImageUnrevealed, 0, 0, backdropUnrevealed.width, backdropUnrevealed.height);
		backdropUnrevealed.endDraw();

		// gradient on top
		backdropGradient = p.loadImage("backdrop_gradient.png");

		maskFilter = new GLTextureFilter(p, "Mask.xml");

		createRingBox2D();
	}


	@SuppressWarnings("static-access")
	public void draw() {
		p.background(0);

		// draw backdrop
		p.pushMatrix();
		p.translate(centerX, centerY);
		p.imageMode(Main.CENTER);
		p.image(backdropUnrevealed, 0, 0);
		p.image(backdropGradient, 0, 0, innerRadius*2, innerRadius*2);
		p.popMatrix();
		
		// make the sides black
		p.fill(0);
		p.noStroke();
		p.rectMode(Main.CORNER);
		p.rect(0, 0, p.centerX-innerRadius, p.height);
		p.rect(0, 0, p.width, p.centerY-innerRadius);
		p.rect(p.centerX+innerRadius, 0, p.width, p.height);
		p.rect(0, p.centerY+innerRadius, p.width, p.height);

		// draw revealed background
		GLTexture backdropMaskTexture = backdropMask.getTexture();
		maskFilter.setParameterValue("mask_factor", 0.0f);
		maskFilter.apply(new GLTexture[]{backdropImageRevealed, backdropMaskTexture}, maskedBackdropRevealed);
		p.pushMatrix();
		p.translate(centerX, centerY);
		p.image(maskedBackdropRevealed, 0, 0);
		p.popMatrix();
		
		backdropMask.beginDraw();
		backdropMask.background(0);
		backdropMask.endDraw();
		
		/* Debug the ring boundary.
		int steps = 60;
		float stepSize = Main.TWO_PI / steps;
		p.rectMode(p.CENTER);
		float w = box2d.scalarWorldToPixels(0.1f);
		float h = box2d.scalarWorldToPixels(2.0f);
		for(int i=0; i<vertices.length; i++) {
			p.pushMatrix();
			Vec2 pos = box2d.coordWorldToPixels(vertices[i]);
			p.translate(pos.x, pos.y);
			float angle = stepSize * i;
			p.rotate(angle);
			p.rect(0, 0, w, h);
			p.popMatrix();
		}
		 */
	}

	public void destroy() {
		box2d.destroyBody(body);
	}

	private Vec2 calculateChainPosition(float angle) {
		PPoint2 pos1 = new PPoint2(innerDiameter / 2.0f, angle);
		Vec2 pos2 = pos1.toVec2();
		Vec2 pos3 = new Vec2(pos2.x, pos2.y);
		Vec2 pos4 = box2d.coordPixelsToWorld(pos3);
		return pos4;
	}

	private void createRingBox2D() {

		int steps = 60;
		float stepSize = Main.TWO_PI / steps;
		vertices = new Vec2[steps];
		body = box2d.getGroundBody();
		// N.B. link the only static body to the background.
		body.setUserData(this);
		for(int i=0; i<steps; i++) {
			float angle = stepSize * i;
			vertices[i] = calculateChainPosition(angle);
			PolygonShape shape = new PolygonShape();
			shape.setAsBox(0.1f, 5.0f, vertices[i], angle);
			body.createFixture(shape, 0.0f);
		}
	}

	public PGraphics getBackdropMask() {
		return backdropMask;
	}

}
