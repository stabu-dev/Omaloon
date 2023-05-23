package ol.world.blocks.distribution;

import mindustry.gen.Building;
import mindustry.world.blocks.distribution.*;
import net.tmmc.util.XBlocks;
import ol.atlas.ILayer;
import ol.atlas.ILayerBlock;
import ol.atlas.ILayerBuilding;
import ol.atlas.DrawAtlas;

import java.util.List;

public class TubeConveyor extends Conveyor implements ILayerBlock {
    public List<ILayer> layerList = List.of(new DrawAtlas() {{
        this.boolf = tile -> {
            Building building = XBlocks.of(tile);
            return building != null && building.block.hasItems;
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
    }

    public class TubeConveyorBuild extends ConveyorBuild implements ILayerBuilding {
        @Override
        public void draw() {
            super.draw();
            draw(getLayers(), block, this);
        }
    }
}
