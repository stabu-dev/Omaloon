package ol.world.blocks.pressure;

import arc.graphics.Color;
import me13.core.block.instance.EnumTextureMapping;
import me13.core.block.instance.Layer;
import mindustry.ui.Bar;
import ol.pressure.PUtil;

public class PressurePipe extends PressureBlock {
    public PressurePipe(String name) {
        super(name);
        drawBase = false;
        layers.add(new Layer(this, "-", EnumTextureMapping.TF_TYPE) {{
            this.hand = (self, other, tile) -> PUtil.isPressureBlock(other);
            this.hand2 = (self, other, tile) -> self.block instanceof IPressureBuild.Block b && b.isPressure();
        }});
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("pressure", (PressurePipeBuild build) -> new Bar(
                () -> "Omaloon Pressure",
                () -> Color.lightGray,
                build::barValueDelta
        ));
    }

    public class PressurePipeBuild extends PressureBuild {
        public float barValueDelta() {
            var p = pressure();
            if(p != null) {
                //TODO remake this statement (result must be from 0 to 1)
                return p.graph.getPressureStored() / p.graph.getPressureCapacity();
            }
            return 0;
        }
    }
}
