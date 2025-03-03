package omaloon.ui;

import arc.struct.*;
import arc.util.io.*;
import mindustry.type.*;

import static mindustry.Vars.content;

public class MultiItemData{
    private final Seq<Item> items = new Seq<>();

    public int length(){
        return items.size;
    }

    public IntSet asIntSet(){
        IntSet seq = new IntSet();
        items.forEach((i) -> seq.add(i.id));
        return seq;
    }

    public void write(Writes writes){
        writes.i(items.size);
        items.forEach(item -> writes.str(item.name));
    }

    public void read(Reads reads){
        int len = reads.i();
        for(int i = 0; i < len; i++){
            toggle(reads.str());
        }
    }

    public int[] config(){
        int[] config = new int[items.size];
        for(int i = 0; i < config.length; i++){
            config[i] = items.get(i).id;
        }
        return config;
    }

    public boolean isToggled(Item item){
        return items.contains(item);
    }

    public boolean isToggled(String name){
        return isToggled(content.item(name));
    }

    public boolean isToggled(int id){
        return isToggled(content.item(id));
    }

    public void toggle(Item item){
        if(item != null){
            if(items.contains(item)){
                items.remove(item);
            }else{
                items.add(item);
            }
        }
    }

    public void toggle(String name){
        toggle(content.item(name));
    }

    public void toggle(int id){
        toggle(content.item(id));
    }

    public void clear(){
        items.clear();
    }

    public void enable(Item item){
        if(!items.contains(item)){
            items.add(item);
        }
    }

    public void disable(Item item){
        if(items.contains(item)){
            items.remove(item);
        }
    }

    public Seq<Item> getItems(){
        return items;
    }
}
