package omaloon.world.patterns;

import arc.struct.*;

public class MultiPattern extends Pattern{
    public Seq<Pattern> patterns = new Seq<>();

    public MultiPattern(String name, Pattern... patterns){
        super(name);
        if(patterns != null && patterns.length > 0){
            this.patterns.addAll(patterns);
            this.shape = patterns[0].shape;
        }
    }

    @Override
    public void loadRegion(){
        super.loadRegion();
        for(Pattern p : patterns){
            p.loadRegion();
        }
    }

    @Override
    public void load(){
        super.load();
        for(Pattern p : patterns){
            p.load();
        }
    }

    public Pattern get(int index){
        if(index < 0 || index >= patterns.size) return this;
        return patterns.get(index);
    }

    public int getSliceOffset(int index){
        int offset = 0;
        int max = Math.min(index, patterns.size);
        for(int i = 0; i < max; i++){
            Pattern p = patterns.get(i);
            offset += (p.shape.width() * p.shape.height()) * Math.max(1, p.variants);
        }
        return offset;
    }

    public int getSliceOffset(Pattern sub){
        int idx = patterns.indexOf(sub);
        return idx >= 0 ? getSliceOffset(idx) : 0;
    }
}
