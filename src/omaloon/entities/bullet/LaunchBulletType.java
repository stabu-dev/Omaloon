package omaloon.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class LaunchBulletType extends BasicBulletType {
	public float fadeAt = 0.25f;

	public float fragAt = 0.75f;
	public float fragRadius = 24f;
	public Interp shadowInterp = Interp.linear, fragInterp = Interp.linear;

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
			float shrink = shrinkInterp.apply(b.fout());
			float height = this.height * ((1f - shrinkY) + shrinkY * shrink);
			float width = this.width * ((1f - shrinkX) + shrinkX * shrink);
			float offset = -90 + (spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f) + rotationOffset;

			Color mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin());

			Draw.mixcol(mix, mix.a);

			if (backRegion.found()) {
				Draw.color(backColor);
				Draw.alpha(1 - b.fin()/fadeAt);
				Draw.rect(backRegion, b.x, b.y, width, height, b.rotation() + offset);
			}

			Draw.color(frontColor);
			Draw.alpha(1 - b.fin()/fadeAt);
			Draw.rect(frontRegion, b.x, b.y, width, height, b.rotation() + offset);

			Draw.reset();
		}
	}

	public void drawShadow(Bullet b) {
		float z = Draw.z();
		Draw.z(Layer.bullet - 1);
		Draw.mixcol(Pal.shadow, 1f);
		Draw.alpha(Pal.shadow.a);
		if (b.fin() <= 0.5f) {
			Fill.circle(
				Mathf.lerp(b.x, b.originX, shadowInterp.apply(Mathf.clamp(b.fin() * 2f))),
				Mathf.lerp(b.y, b.originY, shadowInterp.apply(Mathf.clamp(b.fin() * 2f))),
				hitSize
			);
		} else {
			if (fragBullet != null) {
				Angles.randLenVectors(b.id, fragBullets, fragRadius, (x, y) -> {
					float endX = b.x + Tmp.v1.set(b.vel).scl(b.lifetime - b.time).x + x;
					float endY = b.y + Tmp.v1.set(b.vel).scl(b.lifetime - b.time).y + y;
					Fill.circle(
						Mathf.lerp(b.originX, endX, fragInterp.apply(Mathf.clamp(b.fin() * 2f - 1f)) * fragAt),
						Mathf.lerp(b.originY, endY, fragInterp.apply(Mathf.clamp(b.fin() * 2f - 1f)) * fragAt),
						b.type.hitSize
					);
				});
			}
		}
		Draw.reset();
		Draw.z(z);
	}
}
