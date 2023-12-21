package omaloon.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.distribution.*;

import static arc.Core.*;

public class TubeGate extends OverflowGate {
	public TextureRegion top, topOver, topUnder;

	public TubeGate(String name) {
		super(name);
	}

	@Override
	public void load() {
		super.load();
		top = Core.atlas.find(name + "-top");
		topOver = Core.atlas.find(name + "-top-overflow");
		topUnder = Core.atlas.find(name + "-top-underflow");
	}

	@Override
	public TextureRegion[] icons(){
		return new TextureRegion[]{atlas.find(name + "-icon")};
	}

	public class TubeGateBuild extends OverflowGateBuild {
		boolean invert = false;

		@Override
		public void tapped() {
			invert = !invert;
		}

		@Override
		public void draw() {
			super.draw();
			Draw.rect(top, x, y, 0);
			if (invert) {
				Draw.rect(topUnder, x, y, 0);
			} else {
				Draw.rect(topOver, x, y, 0);
			}
		}

		@Override
		public @Nullable Building getTileTarget(Item item, Building src, boolean flip){
			int from = relativeToEdge(src.tile);
			if(from == -1) return null;
			Building to = nearby((from + 2) % 4);
			boolean
				fromInst = src.block.instantTransfer,
				canForward = to != null && to.team == team && !(fromInst && to.block.instantTransfer) && to.acceptItem(this, item),
				inv = invert == enabled;

			if(!canForward || inv){
				Building a = nearby(Mathf.mod(from - 1, 4));
				Building b = nearby(Mathf.mod(from + 1, 4));
				boolean ac = a != null && !(fromInst && a.block.instantTransfer) && a.team == team && a.acceptItem(this, item);
				boolean bc = b != null && !(fromInst && b.block.instantTransfer) && b.team == team && b.acceptItem(this, item);

				if(!ac && !bc){
					return inv && canForward ? to : null;
				}

				if(ac && !bc){
					to = a;
				}else if(bc && !ac){
					to = b;
				}else{
					to = (rotation & (1 << from)) == 0 ? a : b;
					if(flip) rotation ^= (1 << from);
				}
			}

			return to;
		}
	}
}
