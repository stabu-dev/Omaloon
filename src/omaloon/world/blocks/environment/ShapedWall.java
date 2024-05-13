package omaloon.world.blocks.environment;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;

public class ShapedWall extends StaticWall {
	public Point2[] shape = new Point2[]{};
	public Vec2 offset = new Vec2();

	public ShapedWall(String name) {
		super(name);
		cacheLayer = CacheLayer.normal;
	}

	public boolean valid(Tile tile) {
		for(Point2 next : shape) {
			if (tile.nearby(next.x, next.y).block() != this) return false;
		}
		return true;
	}

	@Override
	public void drawBase(Tile tile) {
		if (valid(tile)) Draw.rect(region, tile.worldx() + offset.x, tile.worldy() + offset.y, 0);
	}
}
