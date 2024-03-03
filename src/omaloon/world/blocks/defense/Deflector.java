package omaloon.world.blocks.defense;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class Deflector extends Block {
	public TextureRegion baseRegion;

	public int rotations = 8;
	public float deflectAngle = 120f;
	public float deflectRange = 80f;
	public float deflectAlpha = 0.2f;
	public Color deflectColor = Pal.heal;

	public Deflector(String name) {
		super(name);
		update = true;
		solid = true;
		group = BlockGroup.projectors;
		ambientSound = Sounds.shield;
		ambientSoundVolume = 0.08f;
	}

	@Override
	public void load() {
		super.load();
		baseRegion = Core.atlas.find(name + "-base", "block-" + size);
	}

	public class DeflectorBuild extends Building {
		public int rot = 2;

		@Override
		public void draw() {
			Draw.rect(baseRegion, x, y, 0);
			Draw.rect(region, x, y, rot * 360f/rotations);
			Draw.color(deflectColor, deflectAlpha);
			Fill.arc(x, y, deflectRange, deflectAngle/360f, -deflectAngle/2f + rot * 360f/rotations);
		}

		@Override
		public void updateTile() {
			Groups.bullet.intersect(x - deflectRange, y - deflectRange, deflectRange * 2f, deflectRange * 2f, b -> {
				float distance = Mathf.dst(x, y, b.x, b.y);
				float angle = Math.abs((Tmp.v1.set(b).angleTo(x, y) + rot * 360f/rotations) % 360 - 180);

				if (distance <= deflectRange && angle <= deflectAngle/2f || distance <= size * 2) b.absorb();
			});
		}
	}
}
