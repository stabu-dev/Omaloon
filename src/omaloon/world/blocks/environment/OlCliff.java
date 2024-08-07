package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.world.*;

public class OlCliff extends Block {
	public float lightMultiplier = 1.5f;
	public float darkMultiplier = 0.5f;
	public TextureRegion[] cliffs;

	public OlCliff(String name){
		super(name);
		breakable = alwaysReplace = false;
		solid = true;
		cacheLayer = CacheLayer.walls;
		fillsTile = false;
		hasShadow = false;
	}

	public static void processCliffs() {
		Vars.world.tiles.eachTile(tile -> {
			if (tile.block() instanceof OlCliff && tile.data == 0) {
				for(int i = 0; i < 4; i++) {
					if (tile.nearby((i + 2) % 4).block() instanceof CliffHelper) tile.data |= 1 << i;
				}
				if (tile.data == 0) tile.setBlock(Blocks.air);
			}
		});
		Vars.world.tiles.eachTile(tile -> {
			if (tile.block() instanceof CliffHelper) mindustry.gen.Call.setTile(tile, Blocks.air, Team.derelict, 0);
		});
	}

	@Override
	public void drawBase(Tile tile) {
		Draw.color(Tmp.c1.set(tile.floor().mapColor).mul(darkMultiplier));
		if ((tile.data & 4) > 0) Draw.rect(cliffs[2], tile.worldx(), tile.worldy());
		if ((tile.data & 8) > 0) Draw.rect(cliffs[3], tile.worldx(), tile.worldy());
		Draw.color(Tmp.c1.set(tile.floor().mapColor).mul(lightMultiplier));
		if ((tile.data & 1) > 0) Draw.rect(cliffs[0], tile.worldx(), tile.worldy());
		if ((tile.data & 2) > 0) Draw.rect(cliffs[1], tile.worldx(), tile.worldy());
		Draw.color();
		if ((tile.data & 15) == 0) Draw.rect(region, tile.worldx(), tile.worldy());
	}

	@Override
	public void load() {
		super.load();
		cliffs = new TextureRegion[4];
		for(int i = 0; i < 4; i++) {
			cliffs[i] = Core.atlas.find(name + "-" + (i + 1), "omaloon-cliff-" + (i + 1));
		}
	}

	@Override
	public int minimapColor(Tile tile){
		return Tmp.c1.set(tile.floor().mapColor).mul(1.2f).rgba();
	}
}