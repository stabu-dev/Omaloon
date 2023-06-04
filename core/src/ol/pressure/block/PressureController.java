package ol.pressure.block;

import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.gen.Tex;
import mindustry.world.Block;
import net.tmmc.util.GraphBlock;
import ol.pressure.netting.IncludeToTheNet;
import ol.pressure.netting.Netting;

import java.util.HashMap;
import java.util.Map;

public class PressureController extends GraphBlock {
    public PressureController(String name) {
        super(name);
        configurable = true;
    }

    @IncludeToTheNet(inHostNet=false)
    public class PressureControllerBuild extends GraphBlockBuild implements IWasNetWire {
        @Override
        public void buildConfiguration(Table table) {
            Map<Block, Integer> map = new HashMap<>();
            for(Building building : Netting.net(this).buildings()) {
                if(map.containsKey(building.block)) {
                    map.replace(building.block, map.get(building.block)+1);
                } else {
                    map.put(building.block, 1);
                }
            }
            map.keySet().forEach(block1 -> {
                table.table(t -> {
                    t.background(Tex.button);
                    t.image(block1.uiIcon).left().size(30).pad(6);
                    t.table(info -> {
                        info.add(block1.localizedName).row();
                        info.add(block1.name + " [white](x" + map.get(block1) + ')').color(Color.gray);
                    }).pad(6).height(30);
                }).width(400).height(50).pad(6).row();
            });
        }

        @Override
        public Building[] getChild() {
            var result = new Building[1];
            for(var build : proximity) {
                if(Netting.inNet(build)) {
                    result[0] = build;
                }
            }
            return result;
        }
    }
}