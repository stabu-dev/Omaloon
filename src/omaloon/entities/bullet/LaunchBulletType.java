package omaloon.entities.bullet;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arclibrary.graphics.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;

import static arc.Core.*;

public class LaunchBulletType extends BasicBulletType {
	public float fadeAt = 0.25f;

	public float fragAt = 0.75f;
	public float fragRadius = 24f;
	public Interp shadowInterp = Interp.linear, fragInterp = Interp.linear;

	public float pitch = 1f;

	public static final FrameBuffer
		drawBuffer = new FrameBuffer(),
		shadowBuffer = new FrameBuffer();
	public static final Seq<Runnable> shadowRuns = new Seq<>();
	public static boolean shouldResize;

	public static final Quat quat = new Quat();
	public static final Mat3D mat = new Mat3D();

	static {
		Events.run(EventType.Trigger.draw, () -> {
			if(!shadowRuns.isEmpty()) {
				shadowBuffer.resize(graphics.getWidth(), graphics.getHeight());
				Seq<Runnable> buffer = shadowRuns.copy();
				shadowRuns.clear();
				Draw.draw(Layer.bullet - 1f, () -> {
					Draw.flush();
					shadowBuffer.begin(Color.clear);
					buffer.each(Runnable::run);
					shadowBuffer.end();
					Draw.color(Pal.shadow, Pal.shadow.a);
					EDraw.drawBuffer(shadowBuffer);
					Draw.flush();
					Draw.color();
				});
			}
			if(shouldResize) {
				drawBuffer.resize(graphics.getWidth(), graphics.getHeight());
				shouldResize = false;
			}
		});
	}

	public LaunchBulletType(float speed, float damage) {
		super(speed, damage);
		collides = collidesAir = collidesGround = false;
		scaleLife = true;
	}

	@Override
	public void createFrags(Bullet b, float x, float y) {
		if(fragBullet != null) {
			Angles.randLenVectors(b.id, fragBullets, fragRadius, (ox, oy) -> fragBullet.create(
				b,
				Mathf.lerp(b.originX, x + ox, fragAt),
				Mathf.lerp(b.originY, y + oy, fragAt),
				Angles.angle(b.originX, b.originY, x + ox, y + oy),
				1f,
				b.lifetime/lifetime
			));
		}
	}

	@Override
	public void draw(Bullet b) {
		drawShadow(b);
		drawTrail(b);
		drawParts(b);

		if (b.fin() <= fadeAt) {
			shouldResize = true;
			float currentPitch = b.fin()/fadeAt;

			mat.set(quat.set(
				Angles.trnsx(b.rotation(), shrinkInterp.apply(currentPitch) * pitch),
				Angles.trnsy(b.rotation(), shrinkInterp.apply(currentPitch) * pitch),
				1f, 0f
			));

			drawBuffer.begin(Color.clear);
			for(int i = 4; i >= 0; i--) {
				if (backRegion.found()) {
					Draw3d.rect(
						mat, backRegion,
						b.x - width/2f + Angles.trnsx(b.rotation(), currentPitch/4f * i),
						b.y - height/2f + Angles.trnsy(b.rotation(), currentPitch/4f * i),
						width, height, b.rotation()
					);
				}
			}
			for(int i = 4; i >= 0; i--) {
				Draw3d.rect(
					mat, frontRegion,
					b.x - width/2f + Angles.trnsx(b.rotation(), currentPitch/4f * i),
					b.y - height/2f + Angles.trnsy(b.rotation(), currentPitch/4f * i),
					width, height, b.rotation() + 90
				);
			}
			drawBuffer.end();

			Draw.draw(Layer.bullet - 0.1f, () -> {
				Draw.color(Color.white);
				Draw.alpha(1f - b.fin() / fadeAt);
				EDraw.drawBuffer(drawBuffer);
			});
		}
	}

	public void drawShadow(Bullet b) {
		if (b.fin() <= 0.5f) {
			shadowRuns.add(() -> Fill.circle(
				Mathf.lerp(b.x, b.originX, shadowInterp.apply(Mathf.clamp(b.fin() * 2f))),
				Mathf.lerp(b.y, b.originY, shadowInterp.apply(Mathf.clamp(b.fin() * 2f))),
				hitSize
			));
		} else {
			if (fragBullet != null) {
				shadowRuns.add(() -> Angles.randLenVectors(b.id, fragBullets, fragRadius, (x, y) -> {
					float endX = b.x + Tmp.v1.set(b.vel).scl(b.lifetime - b.time).x + x;
					float endY = b.y + Tmp.v1.set(b.vel).scl(b.lifetime - b.time).y + y;
					Fill.circle(
						Mathf.lerp(b.originX, endX, fragInterp.apply(Mathf.clamp(b.fin() * 2f - 1f)) * fragAt),
						Mathf.lerp(b.originY, endY, fragInterp.apply(Mathf.clamp(b.fin() * 2f - 1f)) * fragAt),
						fragBullet.hitSize
					);
				}));
			}
		}
	}

	@Override
	public void drawTrail(Bullet b) {
		if(trailLength > 0 && b.trail != null && b.fin() <= fadeAt){
			float z = Draw.z();
			Draw.z(z - 0.0001f);
			b.trail.draw(trailColor, trailWidth * (1f - b.fin()/fadeAt));
			Draw.z(z);
		}
	}
}
