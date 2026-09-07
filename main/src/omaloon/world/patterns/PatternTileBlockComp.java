package omaloon.world.patterns;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import omaloon.annotations.Annotations.*;
import omaloon.gen.PatternTileBlockc.*;

import static mindustry.Vars.*;

@MergeComponent
public abstract class PatternTileBlockComp extends Block{
    public Seq<Pattern> patterns = new Seq<>();
    public boolean usePatternName = false;
    public boolean usePatternIcon = false;

    public PatternTileBlockComp(String name){
        super(name);
    }

    @Replace
    @Override
    public void init(){
        super.init();
        for(Pattern p : patterns){
            if(p.shape != null){
                clipSize = Math.max(clipSize, Math.max(p.shape.width(), p.shape.height()) * tilesize * 2f);
            }
        }
        patterns.sort((p1, p2) -> {
            int a1 = p1.shape == null ? 0 : p1.shape.width() * p1.shape.height();
            int a2 = p2.shape == null ? 0 : p2.shape.width() * p2.shape.height();
            return Integer.compare(a2, a1);
        });
    }

    @Replace
    @Override
    public void load(){
        super.load();
        for(Pattern p : patterns){
            p.load(this);
        }
    }

    public abstract class PatternTileBuildComp extends Building{
        public @Nullable Building patternAnchor;
        public @Nullable Pattern activePattern;

        private static boolean resolving = false;
        private boolean removedHandled = false;

        public boolean isPatternAnchor(){
            return patternAnchor == this;
        }

        @Replace
        @Override
        public void onProximityAdded(){
            super.onProximityAdded();
            removedHandled = false;
            resolveCluster();
        }

        @Replace
        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            resolveCluster();
        }

        @Replace
        @Override
        public void onProximityRemoved(){
            super.onProximityRemoved();
            handleRemoval();
        }

        @Replace
        @Override
        public void onRemoved(){
            super.onRemoved();
            handleRemoval();
        }

        public void handleRemoval(){
            if(removedHandled) return;
            removedHandled = true;

            Seq<Building> neighbors = new Seq<>();
            for(Building other : proximity){
                if(other instanceof PatternTileBuildc && other != this && other.isValid() && other.block == block && other.team == team){
                    neighbors.add(other);
                }
            }

            Building oldAnchor = this.patternAnchor;
            this.patternAnchor = null;
            this.activePattern = null;

            if(oldAnchor != null && oldAnchor != this && oldAnchor.isValid() && !neighbors.contains(oldAnchor)){
                neighbors.add(oldAnchor);
            }

            for(Building nb : neighbors){
                if(nb.isValid() && nb instanceof PatternTileBuildc mb){
                    mb.resolveClusterInternal(this);
                }
            }
        }

        public void resolveCluster(){
            resolveClusterInternal(null);
        }

        public void resolveClusterInternal(@Nullable Building ignore){
            if(resolving) return;
            resolving = true;
            try{
                Seq<Building> cluster = new Seq<>();
                ObjectSet<Building> visited = new ObjectSet<>();
                Seq<Building> queue = new Seq<>();

                if(this != ignore && isValid()){
                    queue.add(this);
                    visited.add(this);
                }

                while(!queue.isEmpty()){
                    Building curr = queue.pop();
                    cluster.add(curr);
                    for(Building other : curr.proximity){
                        if(other instanceof PatternTileBuildc && other != ignore && other.isValid() && other.block == block && other.team == team){
                            if(visited.add(other)){
                                queue.add(other);
                            }
                        }
                    }
                }

                if(cluster.isEmpty()) return;

                for(Building b : cluster){
                    if(b instanceof PatternTileBuildc mb){
                        mb.patternAnchor(null);
                        mb.activePattern(null);
                    }
                }

                if(patterns.isEmpty()) return;

                int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
                int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

                IntMap<Building> tileMap = new IntMap<>();
                for(Building b : cluster){
                    int tx = b.tile.x, ty = b.tile.y;
                    tileMap.put(Point2.pack(tx, ty), b);
                    if(tx < minX) minX = tx;
                    if(tx > maxX) maxX = tx;
                    if(ty < minY) minY = ty;
                    if(ty > maxY) maxY = ty;
                }

                IntSet claimed = new IntSet();

                for(Pattern p : patterns){
                    int pw = p.shape.width();
                    int ph = p.shape.height();
                    if(pw > (maxX - minX + 1) || ph > (maxY - minY + 1)) continue;

                    for(int by = minY; by <= maxY - ph + 1; by++){
                        for(int bx = minX; bx <= maxX - pw + 1; bx++){
                            boolean fits = true;
                            for(int dy = 0; dy < ph; dy++){
                                for(int dx = 0; dx < pw; dx++){
                                    if(p.shape.get(dx, dy)){
                                        int pos = Point2.pack(bx + dx, by + dy);
                                        if(claimed.contains(pos) || !tileMap.containsKey(pos)){
                                            fits = false;
                                            break;
                                        }
                                    }
                                }
                                if(!fits) break;
                            }

                            if(fits){
                                int rootPos = Point2.pack(bx + p.shape.anchorX, by + p.shape.anchorY);
                                Building anchor = tileMap.get(rootPos);
                                if(anchor == null){
                                    for(int dy = 0; dy < ph && anchor == null; dy++){
                                        for(int dx = 0; dx < pw && anchor == null; dx++){
                                            if(p.shape.get(dx, dy)){
                                                anchor = tileMap.get(Point2.pack(bx + dx, by + dy));
                                            }
                                        }
                                    }
                                }

                                for(int dy = 0; dy < ph; dy++){
                                    for(int dx = 0; dx < pw; dx++){
                                        if(p.shape.get(dx, dy)){
                                            int pos = Point2.pack(bx + dx, by + dy);
                                            claimed.add(pos);
                                            Building member = tileMap.get(pos);
                                            if(member instanceof PatternTileBuildc mb){
                                                mb.patternAnchor(anchor);
                                                mb.activePattern(p);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }finally{
                resolving = false;
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            Building target = patternAnchor != null ? patternAnchor : this;
            if(target != this){
                return target.acceptItem(source, item);
            }
            return super.acceptItem(source, item);
        }

        @Replace
        @Override
        public void handleItem(Building source, Item item){
            Building target = patternAnchor != null ? patternAnchor : this;
            if(target != this){
                target.handleItem(source, item);
                return;
            }
            super.handleItem(source, item);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            Building target = patternAnchor != null ? patternAnchor : this;
            if(target != this){
                return target.acceptLiquid(source, liquid);
            }
            return super.acceptLiquid(source, liquid);
        }

        @Replace
        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount){
            Building target = patternAnchor != null ? patternAnchor : this;
            if(target != this){
                target.handleLiquid(source, liquid, amount);
                return;
            }
            super.handleLiquid(source, liquid, amount);
        }

        @Replace
        @Override
        public void damage(float damage){
            Building target = patternAnchor != null ? patternAnchor : this;
            if(target != this){
                target.damage(damage);
                return;
            }
            super.damage(damage);
        }

        public float patternX(){
            return activePattern == null ? x : x + ((activePattern.shape.width() - 1) / 2f - activePattern.shape.anchorX) * tilesize;
        }

        public float patternY(){
            return activePattern == null ? y : y + ((activePattern.shape.height() - 1) / 2f - activePattern.shape.anchorY) * tilesize;
        }

        @Replace
        @Override
        public void draw(){
            if(isPatternAnchor() && activePattern != null){
                float px = this.x, py = this.y;
                this.x = patternX();
                this.y = patternY();
                TextureRegion orig = block.region;
                if(activePattern.region != null){
                    block.region = activePattern.region;
                }
                try{
                    if(activePattern.drawer != null){
                        activePattern.drawer.draw(this);
                    }else{
                        super.draw();
                    }
                }finally{
                    this.x = px;
                    this.y = py;
                    block.region = orig;
                }
            }else if(patternAnchor == null || isPatternAnchor()){
                super.draw();
            }
        }

        @Replace
        @Override
        public void drawLight(){
            if(isPatternAnchor() && activePattern != null){
                float px = this.x, py = this.y;
                this.x = patternX();
                this.y = patternY();
                try{
                    if(activePattern.drawer != null){
                        activePattern.drawer.drawLight(this);
                    }else{
                        super.drawLight();
                    }
                }finally{
                    this.x = px;
                    this.y = py;
                }
            }else if(patternAnchor == null || isPatternAnchor()){
                super.drawLight();
            }
        }

        @Replace
        @Override
        public TextureRegion getDisplayIcon(){
            if(usePatternIcon){
                Pattern pat = activePattern != null ? activePattern : (patternAnchor instanceof PatternTileBuildc ab ? ab.activePattern() : null);
                if(pat != null && pat.icon() != null && pat.icon().found()){
                    return pat.icon();
                }
            }
            return super.getDisplayIcon();
        }

        @Replace
        @Override
        public String getDisplayName(){
            if(usePatternName){
                Pattern pat = activePattern != null ? activePattern : (patternAnchor instanceof PatternTileBuildc ab ? ab.activePattern() : null);
                if(pat != null) return pat.localizedName();
            }
            return super.getDisplayName();
        }
    }
}
