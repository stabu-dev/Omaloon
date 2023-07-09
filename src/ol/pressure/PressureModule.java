package ol.pressure;

import arc.struct.IntSeq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.world.modules.BlockModule;

public class PressureModule extends BlockModule {
    public PressureGraph graph = new PressureGraph();
    public IntSeq links = new IntSeq();
    public float status = 0;

    @Override
    public void write(Writes write) {
        write.i(links.size);
        links.each(write::i);
        write.f(status);
    }

    @Override
    public void read(Reads read) {
        for(int i = 0; i < read.i(); i++) {
            links.add(read.i());
        }
        status = read.f();
    }
}