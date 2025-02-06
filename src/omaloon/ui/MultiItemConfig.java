package omaloon.ui;

import arc.func.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;

public class MultiItemConfig{
    public static <T extends Building> void configure(Block block, Func<T, MultiItemData> getter){
        block.config(Integer.class, (T build, Integer config) -> {
            getter.get(build).toggle(config);
        });

        block.config(String.class, (T build, String config) -> {
            getter.get(build).toggle(config);
        });

        block.config(Item.class, (T build, Item config) -> {
            getter.get(build).toggle(config);
        });

        block.config(int[].class, (T build, int[] config) -> {
            var data = getter.get(build);
            for(int i : config){
                data.toggle(i);
            }
        });

        block.config(String[].class, (T build, String[] config) -> {
            var data = getter.get(build);
            for(String i : config){
                data.toggle(i);
            }
        });

        block.config(Item[].class, (T build, Item[] config) -> {
            var data = getter.get(build);
            for(Item i : config){
                data.toggle(i);
            }
        });

        block.configClear((T build) -> {
            getter.get(build).clear();
        });
    }
}
