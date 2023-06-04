package ol.world.blocks.pressure.block;

import arc.struct.Seq;
import mindustry.gen.Building;
import net.tmmc.util.GraphBlock;
import ol.world.blocks.pressure.netting.IncludeToTheNet;

public class PressurePipe extends GraphBlock {
    public PressurePipe(String name) {
        super(name);
        rotate = true;
        rotateDraw = false;
        quickRotate = true;
    }

    @IncludeToTheNet(inHostNet=false)
    public class PipeBuild extends GraphBlockBuild implements IWasNetWire {
        @Override
        public Building[] getChild() {
            Seq<Building> result = Seq.with(antiNearby(), nearby());
            for(int i : new int[] {1, 3}) {
                var b = nearby((rotation + i) % 4);
                if(b instanceof PipeBuild build) {
                    if(build.nearby() == this || build.antiNearby() == this) {
                        result.add(build);
                    }
                } else {
                    result.add(b);
                }
            }
            return result.items;
        }
    }
}