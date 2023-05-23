package ol.world.blocks.distribution;

import arc.Core;
import mindustry.gen.Building;
import mindustry.world.blocks.distribution.*;
import net.tmmc.util.GraphBlock;
import net.tmmc.util.XBlocks;
import ol.atlas.ILayer;
import ol.atlas.ILayerBlock;
import ol.atlas.ILayerBuilding;
import ol.atlas.DrawAtlas;

import java.util.List;

public class TubeConveyor extends Conveyor implements ILayerBlock {
    public List<ILayer> layerList = List.of(new DrawAtlas() {{
        this.boolf = (tile, self) -> {
            Building building = XBlocks.of(tile);
            if(building != null) {
                if(building instanceof TubeConveyorBuild) {
                    return building.nearby(building.rotation) == self || switch(building.rotation) {
                        case 0 -> building.nearby(2);
                        case 1 -> building.nearby(3);
                        case 2 -> building.nearby(0);
                        case 3 -> building.nearby(1);
                        default -> throw new IllegalStateException();
                    } == self || self.nearby(self.rotation) == building || switch(self.rotation) {
                        case 0 -> self.nearby(2);
                        case 1 -> self.nearby(3);
                        case 2 -> self.nearby(0);
                        case 3 -> self.nearby(1);
                        default -> throw new IllegalStateException();
                    } == building;
                } else {
                    return building.block.hasItems;
                }
            } else {
                return false;
            }
        };
        this.prefix = "top";
    }});

    public TubeConveyor(String name) {
        super(name);
    }

    @Override
    public List<ILayer> getLayers() {
        return layerList == null ? List.of() : layerList;
    }

    @Override
    public void load() {
        super.load();
        loadLayers(this);
        uiIcon = Core.atlas.find(name + "-icon");
    }

    public class TubeConveyorBuild extends ConveyorBuild implements ILayerBuilding {
        @Override
        public void draw() {
            super.draw();
            draw(getLayers(), block, this);
        }
    }
}
