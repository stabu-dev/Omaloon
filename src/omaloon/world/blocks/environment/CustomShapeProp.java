package omaloon.world.blocks.environment;

import arc.graphics.g2d.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.world.interfaces.*;

import static arc.Core.*;

public class CustomShapeProp extends Prop implements MultiPropI {
    public TextureRegion shadow;

    public CustomShapeProp(String name) {
        super(name);
        customShadow = solid = breakable = true;
        alwaysReplace = false;
        breakEffect = Fx.breakProp;
        breakSound = Sounds.rockBreak;
    }

    @Override
    public void load(){
        super.load();
        shadow = atlas.find(name + "-shadow");
    }

    @Override
    public void drawBase(Tile tile) {
        Draw.rect(shadow, tile.worldx(), tile.worldy());
        super.drawBase(tile);
    }
}
