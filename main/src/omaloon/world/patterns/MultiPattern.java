package omaloon.world.patterns;

import arc.graphics.g2d.*;
import arc.struct.*;

/**
 * A container holding multiple alternative {@link Pattern} options for a single block.
 * @author stabu_
 */
public class MultiPattern extends Pattern{
    public Seq<Pattern> patterns = new Seq<>();

    public MultiPattern(Pattern... patterns){
        super(patterns != null && patterns.length > 0 ? patterns[0].name : "");
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

    public TextureRegion icon(int index){
        return get(index).icon();
    }

    public String localizedName(int index){
        return get(index).localizedName;
    }
}
