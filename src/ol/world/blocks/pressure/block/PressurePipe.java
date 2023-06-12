package ol.world.blocks.pressure.block;

import arc.struct.Seq;
import me13.core.block.instance.AdvancedBlock;
import mindustry.gen.Building;
import ol.world.blocks.pressure.netting.IncludeToTheNet;

public class PressurePipe extends AdvancedBlock {
    public PressurePipe(String name) {
        super(name);
        rotate = true;
        rotateDraw = false;
        quickRotate = true;
    }

    @IncludeToTheNet(inHostNet=false)
    public class PipeBuild extends AdvancedBuild implements IWasNetWire {
        @Override
        public Building[] getChild() {
            Seq<Building> result = Seq.with(reversedNearby(), nearby());
            for(int i : new int[] {1, 3}) {
                var b = nearby((rotation + i) % 4);
                if(b instanceof PipeBuild build) {
                    if(build.nearby() == this || build.reversedNearby() == this) {
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