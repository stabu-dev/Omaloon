package omaloon.world.save;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.io.SaveFileReader;
import mindustry.io.SaveVersion;

import java.io.*;

public abstract class OlSaveChunk implements SaveFileReader.CustomChunk {
    private final Writes writes = new Writes(new DataOutputStream(new ByteArrayOutputStream()));
    private final Reads reads = new Reads(new DataInputStream(new ByteArrayInputStream(new byte[0])));
    public OlSaveChunk(String chunkName){
        SaveVersion.addCustomChunk("omaloon-"+chunkName,this);
    }

    abstract int version();

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(version());
        writes.output = dataOutput;
        write(writes,dataOutput);
        writes.output = null;
    }

    protected abstract void write(Writes write, DataOutput dataOutput);
    protected abstract void read(Reads read, DataInput dataInput,int version);

    @Override
    public void read(DataInput dataInput) throws IOException {
        int version = dataInput.readInt();
        reads.input=dataInput;
        read(reads,dataInput,version);
        reads.input=null;
    }
}
