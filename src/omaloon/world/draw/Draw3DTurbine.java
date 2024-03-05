package omaloon.world.draw;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arclibrary.graphics.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.draw.*;

public class Draw3DTurbine extends DrawBlock {
	public TextureRegion baseRegion, rotorRegion;
	public Vec2 shadowOffset = new Vec2();
	public Floatf<Building> baseRotationProv = build -> Time.time, rotorRotationProv = build -> Time.time;
	public String baseSuffix, rotorSuffix;
	public float angle = 45;
	public float rotorDistance = 3;
	public float rotorWidth = 1;
	public int rotorSegments = 4;
	public boolean advanced = false;

	private static final FrameBuffer shadowBuffer = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());

	public Draw3DTurbine(String baseSuffix, String rotorSuffix) {
		this.baseSuffix = baseSuffix;
		this.rotorSuffix = rotorSuffix;
	}

	@Override
	public void draw(Building build) {
		drawShadow(build);

		Vec2 trns = new Vec2().trns(baseRotationProv.get(build), rotorDistance);
		Vec2 pos = new Vec2().set(build.x, build.y).sub(rotorRegion.width/8f, rotorRegion.height/8f);
		Mat3D transformMatrix = new Mat3D().set(new Vec3(), new Quat().set(new Vec3(trns.x, trns.y, 0).nor(), angle), new Vec3(1, 1, 1));

		Draw.z(Layer.power + 0.1f);
		float baseRot = baseRotationProv.get(build) % 180f;
		Draw.alpha(1);
		Draw.rect(baseRegion, build.x, build.y, baseRot - 90);
		Draw.alpha(Mathf.maxZero(baseRot - 90)/90);
		Draw.rect(baseRegion, build.x, build.y, baseRot + 90);
		for (int i = 0; i < rotorSegments; i++) {
			float progress = i/(float)rotorSegments;
			trns.trns(baseRotationProv.get(build) + 90f, rotorDistance - rotorWidth + progress * rotorWidth);
			pos.add(trns);
			float rot = (rotorRotationProv.get(build) - 180f) % 90f + trns.angle() + 90f;
			Draw.alpha(1f);
			Draw3d.rect(transformMatrix, rotorRegion, pos.x, pos.y, rotorRegion.width/4f, rotorRegion.height/4f, rot);
			Draw.alpha(rotorRotationProv.get(build) % 90f / 90f);
			Draw3d.rect(transformMatrix, rotorRegion, pos.x, pos.y, rotorRegion.width/4f, rotorRegion.height/4f, rot - 90f);
			pos.sub(trns);
		}
	}

	public void drawShadow(Building build) {
		Vec2 trns = new Vec2().trns(baseRotationProv.get(build), rotorDistance);
		Vec2 pos = new Vec2().set(build.x, build.y).sub(rotorRegion.width/8f, rotorRegion.height/8f);
		Mat3D transformMatrix = new Mat3D().set(new Vec3(), new Quat().set(new Vec3(trns.x, trns.y, 0).nor(), angle), new Vec3(1, 1, 1));
		if (Core.settings.getBool("@setting.omaloon.advanced-shadows", false)) {
			Draw.draw(Layer.blockProp + 1, () -> {
				shadowBuffer.begin(Color.clear);
				Lines.stroke(3f);
				pos.sub(shadowOffset.x, shadowOffset.y);
				Draw.rect(baseRegion, build.x - shadowOffset.x, build.y - shadowOffset.y, baseRotationProv.get(build) - 90f);
				trns.trns(baseRotationProv.get(build) + 90f, rotorDistance);
				pos.add(trns);
				Draw3d.rect(transformMatrix, rotorRegion, pos.x, pos.y, rotorRegion.width / 4f, rotorRegion.height / 4f, rotorRotationProv.get(build) + trns.angle());
				Lines.line(build.x, build.y, build.x - shadowOffset.x, build.y - shadowOffset.y);
				pos.add(shadowOffset.x, shadowOffset.y).sub(trns);
				shadowBuffer.end();
				Draw.color(Pal.shadow, Pal.shadow.a);
				EDraw.drawBuffer(shadowBuffer);
				Draw.flush();
				Draw.color();
			});
		} else {
			Draw.z(Layer.blockProp + 1f);
			Draw.mixcol(Pal.shadow, 1f);
			Draw.alpha(Pal.shadow.a);
			Lines.stroke(3f);
			pos.sub(shadowOffset.x, shadowOffset.y).add(trns.trns(baseRotationProv.get(build) + 90f, rotorDistance));
			Draw.rect(baseRegion, build.x - shadowOffset.x, build.y - shadowOffset.y, baseRotationProv.get(build) - 90f);
			Draw3d.rect(transformMatrix, rotorRegion, pos.x, pos.y, rotorRegion.width / 4f, rotorRegion.height / 4f, rotorRotationProv.get(build) + trns.angle());
			Lines.line(build.x, build.y, build.x - shadowOffset.x, build.y - shadowOffset.y);
			pos.add(shadowOffset.x, shadowOffset.y).sub(trns);
			Draw.reset();
		}
	}

	@Override
	public void load(Block block) {
		baseRegion = Core.atlas.find(block.name + baseSuffix);
		rotorRegion = Core.atlas.find(block.name + rotorSuffix);
	}

	@Override
	public TextureRegion[] icons(Block block) {
		return new TextureRegion[]{rotorRegion};
	}
}
