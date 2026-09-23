package omaloon.world.patterns;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import mindustry.world.modules.*;
import omaloon.annotations.Annotations.*;
import omaloon.gen.PatternTileBlockc.*;
import omaloon.world.patterns.*;

import java.lang.reflect.*;

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

    @PatternShare
    public abstract class PatternTileBuildComp extends Building{
        public @PatternLocal Block baseBlock;
        public @Nullable Building patternAnchor;
        public @Nullable Pattern activePattern;
        public transient Seq<Building> group = new Seq<>();
        @PatternLocal transient float sx, sy;

        private static final ObjectMap<Class<?>, Field[]> modCache = new ObjectMap<>();

        private static void swapOuter(Building b, Block target){
            BlockCloner.swapOuter(b, target);
        }

        @Override
        public Building create(Block block, Team team){
            baseBlock = content.block(block.id);
            return super.create(block, team);
        }

        @Override
        public void created(){
            super.created();
            baseBlock = content.block(block.id);
        }

        public Block baseBlock(){
            if(baseBlock == null || baseBlock.id != block.id || baseBlock != content.block(block.id)){
                baseBlock = content.block(block.id);
            }
            return baseBlock;
        }

        public boolean isPatternAnchor(){
            return patternAnchor == this;
        }

        public boolean member(){
            return patternAnchor != null && patternAnchor != this;
        }

        @InternalImpl
        public abstract void distributeState(ObjectSet<BlockModule> used);

        @InternalImpl
        public abstract void pullMirrorState();

        @InternalImpl
        public abstract void pushMirrorState();

        @InternalImpl
        public abstract void freshState();

        @Replace
        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(isPatternAnchor());
            if(isPatternAnchor()){
                write.s(patterns.indexOf(activePattern));
            }
        }

        @Replace
        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            if(read.bool()){
                int index = read.s();
                patternAnchor = this;
                activePattern = patterns.get(index);
            }else{
                patternAnchor = null;
                activePattern = null;
            }
        }

        public Pattern active(){
            if(activePattern != null) return activePattern;
            if(patternAnchor instanceof PatternTileBuildc ab) return ab.activePattern();
            return null;
        }

        @Replace
        @Override
        public void onProximityAdded(){
            if(!member()) super.onProximityAdded();
            resolveCluster();
        }

        @Replace
        @Override
        public void onProximityUpdate(){
            if(!member()) super.onProximityUpdate();
            resolveCluster();
        }

        @Replace
        @Override
        public void onProximityRemoved(){
            super.onProximityRemoved();
            leaveGraphs(this);
            handleRemoval();
        }

        @Replace
        @Override
        public void onRemoved(){
            super.onRemoved();
            leaveGraphs(this);
            handleRemoval();
        }

        void center(){
            sx = x;
            sy = y;
            x = patternX();
            y = patternY();
        }

        void uncenter(){
            x = sx;
            y = sy;
        }

        @Replace
        @Override
        public void updateConsumption(){
            if(member()) return;
            center();
            try{
                super.updateConsumption();
            }finally{
                uncenter();
            }
        }

        @Replace
        @Override
        public void updateTile(){
            if(member()){
                pullMirrorState();
                return;
            }
            center();
            try{
                super.updateTile();
            }finally{
                uncenter();
            }
            pushMirrorState();
        }

        void externals(Cons<Building> each){
            ObjectSet<Building> seen = new ObjectSet<>();
            for(Building m : group){
                if(m == null || m.tile == null) continue;
                for(Point2 p : Geometry.d4){
                    Building o = world.build(m.tile.x + p.x, m.tile.y + p.y);
                    if(o == null || o == this || o.team != team || !seen.add(o)) continue;
                    if(o instanceof PatternTileBuildc) continue;
                    each.get(o);
                }
            }
        }

        @Replace
        @Override
        public void updateProximity(){
            if(!isPatternAnchor()){
                super.updateProximity();
                return;
            }
            proximity.clear();
            externals(o -> {
                proximity.add(o);
                o.proximity.addUnique(this);
            });
            super.onProximityAdded();
            super.onProximityUpdate();
            for(Building o : proximity) o.onProximityUpdate();
        }

        @Replace
        @Override
        public void removeFromProximity(){
            if(!isPatternAnchor()){
                super.removeFromProximity();
                return;
            }
            super.onProximityRemoved();
            externals(o -> {
                o.proximity.remove(this, true);
                o.onProximityUpdate();
            });
            proximity.clear();
        }

        Building edge(Building other){
            if(other == null || other.tile == null) return self();
            for(Building m : group){
                if(m == null || m.tile == null || !m.isValid()) continue;
                int dx = other.tile.x - m.tile.x, dy = other.tile.y - m.tile.y;
                if(dx * dx + dy * dy == 1) return m;
            }
            return self();
        }

        @Replace
        @Override
        public void offload(Item item){
            produced(item, 1);
            int dump = this.cdump;
            for(int i = 0; i < proximity.size; i++){
                incrementDump(proximity.size);
                Building other = proximity.get((i + dump) % proximity.size);
                Building src = isPatternAnchor() ? edge(other) : self();
                if(other.acceptItem(src, item) && canDump(other, item)){
                    other.handleItem(src, item);
                    return;
                }
            }
            handleItem(self(), item);
        }

        @Replace
        @Override
        public boolean dump(Item todump){
            if(!block.hasItems || items.total() == 0 || proximity.size == 0 || (todump != null && !items.has(todump))) return false;
            int dump = this.cdump;
            var allItems = content.items();
            int itemSize = allItems.size;
            Object[] itemArray = allItems.items;
            if(todump == null){
                for(int i = 0; i < proximity.size; i++){
                    Building other = proximity.get((i + dump) % proximity.size);
                    for(int ii = 0; ii < itemSize; ii++){
                        if(!items.has(ii)) continue;
                        if(give(other, (Item)itemArray[ii])) return true;
                    }
                    incrementDump(proximity.size);
                }
            }else{
                for(int i = 0; i < proximity.size; i++){
                    Building other = proximity.get((i + dump) % proximity.size);
                    if(give(other, todump)) return true;
                    incrementDump(proximity.size);
                }
            }
            return false;
        }

        boolean give(Building other, Item item){
            Building src = isPatternAnchor() ? edge(other) : self();
            if(other.acceptItem(src, item) && canDump(other, item)){
                other.handleItem(src, item);
                items.remove(item, 1);
                incrementDump(proximity.size);
                return true;
            }
            return false;
        }

        @Replace
        @Override
        public void display(Table table){
            if(member() && patternAnchor.isValid()){
                patternAnchor.display(table);
                return;
            }
            super.display(table);
        }

        @Replace
        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(member() && patternAnchor.isValid()){
                patternAnchor.control(type, p1, p2, p3, p4);
                return;
            }
            super.control(type, p1, p2, p3, p4);
        }

        @Replace
        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4){
            if(member() && patternAnchor.isValid()){
                patternAnchor.control(type, p1, p2, p3, p4);
                return;
            }
            super.control(type, p1, p2, p3, p4);
        }

        Seq<Building> collect(){
            Seq<Building> out = new Seq<>();
            if(world == null || tile == null || !isValid()) return out;
            Block base = baseBlock();
            ObjectSet<Building> seen = new ObjectSet<>();
            Seq<Building> queue = new Seq<>();
            queue.add(this);
            seen.add(this);
            while(!queue.isEmpty()){
                Building cur = queue.pop();
                out.add(cur);
                if(cur.tile == null) continue;
                for(Point2 p : Geometry.d4){
                    Tile t = world.tile(cur.tile.x + p.x, cur.tile.y + p.y);
                    if(t == null || t.build == null) continue;
                    Building o = t.build;
                    if(!o.isValid() || o.team != team || !(o instanceof PatternTileBuildc ob) || ob.baseBlock() != base) continue;
                    if(seen.add(o)) queue.add(o);
                }
            }
            return out;
        }

        void handleRemoval(){
            Block base = baseBlock();
            if(base != null && block != base){
                block = base;
                swapOuter(this, base);
            }
            patternAnchor = null;
            activePattern = null;
        }

        void resolveCluster(){
            Seq<Building> cluster = collect();
            if(cluster.isEmpty()) return;
            ObjectMap<Building, Building> prevAnchors = new ObjectMap<>();
            ObjectMap<Building, Pattern> prevPatterns = new ObjectMap<>();
            for(Building b : cluster){
                if(b instanceof PatternTileBuildc mb && mb.isPatternAnchor() && mb.activePattern() != null && mb.activePattern().shape != null){
                    Pattern p = mb.activePattern();
                    int bx = b.tile.x - p.shape.anchorX;
                    int by = b.tile.y - p.shape.anchorY;
                    for(int dy = 0; dy < p.shape.height(); dy++){
                        for(int dx = 0; dx < p.shape.width(); dx++){
                            if(p.shape.get(dx, dy)){
                                Building tileB = world.build(bx + dx, by + dy);
                                if(tileB != null){
                                    prevAnchors.put(tileB, b);
                                    prevPatterns.put(tileB, p);
                                }
                            }
                        }
                    }
                }
            }
            for(Building b : cluster){
                if(b instanceof PatternTileBuildc mb){
                    Block base = mb.baseBlock();
                    b.block = base;
                    swapOuter(b, base);
                    mb.patternAnchor(null);
                    mb.activePattern(null);
                }
            }
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            IntMap<Building> map = new IntMap<>();
            for(Building b : cluster){
                int tx = b.tile.x, ty = b.tile.y;
                map.put(Point2.pack(tx, ty), b);
                if(tx < minX) minX = tx;
                if(tx > maxX) maxX = tx;
                if(ty < minY) minY = ty;
                if(ty > maxY) maxY = ty;
            }

            class Candidate{
                final Pattern pattern;
                final Building anchor;
                final Seq<Building> members;
                final int area;
                final boolean wasActive;
                final int minId;
                final int maxId;

                Candidate(Pattern pattern, Building anchor, Seq<Building> members, int area, boolean wasActive, int minId, int maxId){
                    this.pattern = pattern;
                    this.anchor = anchor;
                    this.members = members;
                    this.area = area;
                    this.wasActive = wasActive;
                    this.minId = minId;
                    this.maxId = maxId;
                }
            }

            Seq<Candidate> candidates = new Seq<>();

            for(Pattern p : patterns){
                if(p.shape == null) continue;
                int pw = p.shape.width(), ph = p.shape.height();
                if(pw > maxX - minX + 1 || ph > maxY - minY + 1) continue;
                int area = pw * ph;
                for(int by = minY; by <= maxY - ph + 1; by++){
                    for(int bx = minX; bx <= maxX - pw + 1; bx++){
                        boolean fits = true;
                        for(int dy = 0; dy < ph && fits; dy++){
                            for(int dx = 0; dx < pw; dx++){
                                if(!p.shape.get(dx, dy)) continue;
                                int pos = Point2.pack(bx + dx, by + dy);
                                if(!map.containsKey(pos)){
                                    fits = false;
                                    break;
                                }
                            }
                        }
                        if(!fits) continue;
                        Building anchor = map.get(Point2.pack(bx + p.shape.anchorX, by + p.shape.anchorY));
                        if(anchor == null){
                            for(int dy = 0; dy < ph && anchor == null; dy++){
                                for(int dx = 0; dx < pw && anchor == null; dx++){
                                    if(p.shape.get(dx, dy)) anchor = map.get(Point2.pack(bx + dx, by + dy));
                                }
                            }
                        }
                        Seq<Building> g = new Seq<>();
                        int minId = Integer.MAX_VALUE;
                        int maxId = Integer.MIN_VALUE;
                        boolean wasActive = true;

                        for(int dy = 0; dy < ph; dy++){
                            for(int dx = 0; dx < pw; dx++){
                                if(!p.shape.get(dx, dy)) continue;
                                int pos = Point2.pack(bx + dx, by + dy);
                                Building m = map.get(pos);
                                g.add(m);
                                minId = Math.min(minId, m.id);
                                maxId = Math.max(maxId, m.id);
                                if(prevAnchors.get(m) != anchor || prevPatterns.get(m) != p){
                                    wasActive = false;
                                }
                            }
                        }
                        candidates.add(new Candidate(p, anchor, g, area, wasActive, minId, maxId));
                    }
                }
            }

            candidates.sort((c1, c2) -> {
                int areaComp = Integer.compare(c2.area, c1.area);
                if(areaComp != 0) return areaComp;

                int activeComp = Boolean.compare(c2.wasActive, c1.wasActive);
                if(activeComp != 0) return activeComp;

                int minAgeComp = Integer.compare(c1.minId, c2.minId);
                if(minAgeComp != 0) return minAgeComp;

                return Integer.compare(c1.maxId, c2.maxId);
            });

            IntSet claimed = new IntSet();
            Seq<Seq<Building>> groups = new Seq<>();
            Seq<Building> anchors = new Seq<>();

            for(Candidate c : candidates){
                boolean fits = true;
                for(Building m : c.members){
                    if(claimed.contains(Point2.pack(m.tile.x, m.tile.y))){
                        fits = false;
                        break;
                    }
                }
                if(!fits) continue;

                for(Building m : c.members){
                    int pos = Point2.pack(m.tile.x, m.tile.y);
                    claimed.add(pos);
                    if(m instanceof PatternTileBuildc mb){
                        mb.patternAnchor(c.anchor);
                        mb.activePattern(c.pattern);
                    }
                }
                groups.add(c.members);
                anchors.add(c.anchor);
            }

            link(groups, anchors);
            for(Building b : cluster){
                if(b instanceof PatternTileBuildc mb && mb.patternAnchor() == null && prevAnchors.containsKey(b)){
                    mb.freshState();
                    leaveGraphs(b);
                }
            }
        }

        void link(Seq<Seq<Building>> groups, Seq<Building> anchors){
            ObjectSet<BlockModule> used = new ObjectSet<>();
            for(int i = 0; i < groups.size; i++){
                Seq<Building> g = groups.get(i);
                Building anchor = anchors.get(i);
                if(!(anchor instanceof PatternTileBuildc ab) || !anchor.isValid()) continue;
                ab.group(g);
                ab.distributeState(used);
                Pattern pat = ab.activePattern();
                Block target = pat != null && pat.syntheticBlock != null ? pat.syntheticBlock : ab.baseBlock();
                for(Building m : g){
                    m.block = target;
                    swapOuter(m, target);
                    if(m != anchor) leaveGraphs(m);
                }
                ab.pushMirrorState();
                anchor.updateProximity();
            }
        }

        static void leaveGraphs(Building b){
            if(b == null) return;
            for(Field f : mods(b.getClass())){
                BlockModule mod;
                try{
                    mod = (BlockModule)f.get(b);
                }catch(Exception e){
                    continue;
                }
                if(mod == null) continue;
                Object graph;
                try{
                    Field gf;
                    try{
                        gf = mod.getClass().getField("graph");
                    }catch(NoSuchFieldException e){
                        gf = mod.getClass().getDeclaredField("graph");
                    }
                    gf.setAccessible(true);
                    graph = gf.get(mod);
                }catch(Exception e){
                    continue;
                }
                if(graph == null) continue;
                for(Field sf : graph.getClass().getFields()){
                    if(!Seq.class.isAssignableFrom(sf.getType())) continue;
                    try{
                        @SuppressWarnings("unchecked")
                        Seq<Object> seq = (Seq<Object>)sf.get(graph);
                        if(seq != null && seq.contains(b)) seq.remove(b);
                    }catch(Exception ignored){}
                }
                try{
                    graph.getClass().getMethod("checkEntity").invoke(graph);
                }catch(Exception ignored){}
            }
        }

        static Field[] mods(Class<?> cls){
            Field[] c = modCache.get(cls);
            if(c != null) return c;
            Seq<Field> out = new Seq<>();
            for(Class<?> x = cls; x != null && x != Object.class; x = x.getSuperclass()){
                for(Field f : x.getDeclaredFields()){
                    if(Modifier.isStatic(f.getModifiers()) || !BlockModule.class.isAssignableFrom(f.getType())) continue;
                    f.setAccessible(true);
                    if(!out.contains(f)) out.add(f);
                }
                if(x == Building.class) break;
            }
            c = out.toArray(Field.class);
            modCache.put(cls, c);
            return c;
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
                if(activePattern.region != null) block.region = activePattern.region;
                try{
                    super.draw();
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
                    super.drawLight();
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
                Pattern pat = active();
                if(pat != null && pat.icon() != null && pat.icon().found()) return pat.icon();
            }
            return super.getDisplayIcon();
        }

        @Replace
        @Override
        public String getDisplayName(){
            if(usePatternName){
                Pattern pat = active();
                if(pat != null) return pat.localizedName();
            }
            return super.getDisplayName();
        }
    }
}
