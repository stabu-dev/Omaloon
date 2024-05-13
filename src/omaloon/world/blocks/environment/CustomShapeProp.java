package omaloon.world.blocks.environment;

import arc.graphics.g2d.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.world.blocks.environment.*;

import static arc.Core.*;

public class CustomShapeProp extends Prop {
    public TextureRegion shadow;

    public CustomShapeProp(String name) {
        super(name);
        customShadow = solid = breakable = true;
        breakEffect = Fx.breakProp;
        breakSound = Sounds.rockBreak;
    }

    @Override
    public void load(){
        super.load();
        shadow = atlas.find(name + "-shadow");
    }
}
