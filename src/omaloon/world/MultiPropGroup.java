package omaloon.world;

import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.world.*;

public class MultiPropGroup {
	public Seq<Tile> group = new Seq<>();
	public Tile center;
	public Block type;
	public boolean removed = false;

	public MultiPropGroup(Block type) {
		this.type = type;
	}

	public void findCenter() {
		center = group.max(tile -> tile.x + Vars.world.width() * tile.y);
		if (center == null) Log.errTag("what", "HUH?");
	}

	public void remove() {
		group.each(tile -> tile.setBlock(Blocks.air));
		removed = true;
	}

	public void update() {
		if (group.contains(tile -> tile.block() != type)) remove();
	}
}
