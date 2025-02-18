package omaloon.io;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.annotations.Annotations;
import mindustry.gen.Itemsc;
import mindustry.io.TypeIO;
import mindustry.type.Item;

@Annotations.TypeIOHandler
@ent.anno.Annotations.TypeIOHandler
public class OlTypeIO extends TypeIO{
    public static void writeItemCons(Writes writes,Itemsc[] itemscs){
        writes.i(itemscs.length);
        for (Itemsc itemsc : itemscs) {
            TypeIO.writeObject(writes, itemsc);
        }
    }
    public static Itemsc[] readItemConsumers(Reads read){
        int amount = read.i();
        Itemsc[] itemscs = new Itemsc[amount];
        for (int i = 0; i < itemscs.length; i++) {
            itemscs[i]=TypeIO.readEntity(read);
        }
        return itemscs;
    }
}
