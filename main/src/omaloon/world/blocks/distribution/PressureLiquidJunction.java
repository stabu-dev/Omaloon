package omaloon.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import omaloon.annotations.Annotations.*;
import omaloon.world.*;
import omaloon.world.interfaces.*;

import static arc.Core.atlas;

public class PressureLiquidJunction extends GenericPressureBlock{
    public DrawBlock drawer = new DrawDefault();
    @Load("@-side1")
    public TextureRegion side1;
    @Load("@-side2")
    public TextureRegion side2;

    public PressureLiquidJunction(String name){
        super(name);
        update = true;
        destructible = true;
    }

    @Override
    public void load(){
        super.load();
        drawer.load(this);
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{
        atlas.find(name + "-icon")
        };
    }

    @Override
    public boolean canReplace(Block other){
        return super.canReplace(other) || other instanceof PressureLiquidConduit;
    }

    public class PressureLiquidJunctionBuild extends GenericPressureBlockBuild{
        @Override
        public void draw(){
            drawer.draw(this);

            for(int i = 0; i < 4; i++){
                Building neighbor = nearby(i);
                Building opposite = nearby((i + 2) % 4);

                if(!(neighbor instanceof HasPressure) || !(opposite instanceof HasPressure)){
                    Draw.rect(i >= 2 ? side2 : side1, x, y, i * 90);
                }
            }
        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public HasPressure getFluidDestination(HasPressure source, @Nullable Liquid fluid){
            if(!enabled) return this;

            int dir = (source.toBuilding().relativeTo(tile.x, tile.y) + 4) % 4;
            HasPressure next = nearby(dir) instanceof HasPressure ? (HasPressure)nearby(dir) : null;
            if(next == null){
                return this;
            }
            return next.getFluidDestination(this, fluid);
        }

        @Override
        public boolean acceptsFluid(HasPressure from, @Nullable Liquid liquid, float amount){
            return false;
        }

        @Override
        public boolean connects(HasPressure to){
            return false;
        }

        @Override
        public Seq<HasPressure> connections(){
            return Seq.with();
        }

        @Override
        public boolean outputsFluid(HasPressure to, @Nullable Liquid liquid, float amount){
            return false;
        }
    }
}