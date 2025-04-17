package omaloon.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.type.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.draw.*;
import omaloon.annotations.*;

public class TubeGate extends OverflowGate{
		public DrawBlock drawer = new DrawDefault();

		@Load("@-rotator")
		public TextureRegion rotorRegion;

    public TubeGate(String name){
        super(name);
    }

		@Override
		public void load(){
				super.load();
				drawer.load(this);
		}

		public class TubeGateBuild extends OverflowGateBuild{
        public ObjectIntMap<Item> directionalItems = new ObjectIntMap<>();

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            short size = read.s();
            for(int i = 0; i < size; i++){
                int item = read.i();
                int build = read.i();
                directionalItems.put(Vars.content.item(item), build);
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.s(directionalItems.size);
            directionalItems.keys().toArray().each(item -> {
                write.i(item.id);
                write.i(directionalItems.get(item));
            });
        }
    }
}
