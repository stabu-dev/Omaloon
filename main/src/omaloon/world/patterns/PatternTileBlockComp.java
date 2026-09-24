package omaloon.world.patterns;

import arc.Core;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
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
                int dim = Math.max(p.shape.width(), p.shape.height());
                clipSize = Math.max(clipSize, dim * tilesize * 2f);
            }
        }
        patterns.sort((p1, p2) -> Integer.compare(patternArea(p2), patternArea(p1)));
    }

    @Replace
    @Override
    public void load(){
        super.load();
        for(Pattern p : patterns){
            p.load(this);
        }
    }

    @Replace
    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        int pos = Point2.pack(plan.x, plan.y);
        //only the plans in the player's build queue take part in a pattern, every other preview is left to vanilla
        if(plan.breaking || !updateGhosts() || !ghostTiles.containsKey(pos) || !player.unit().plans.contains(plan, true)){
            super.drawPlanRegion(plan, list);
            return;
        }
        Pattern p = ghostTiles.get(pos);
        if(p == null || p.shape == null || p.region == null || !p.region.found()) return;
        drawPattern(p, plan.drawx(), plan.drawy(), rotate && rotateDraw ? plan.rotation * 90f : 0f);
    }

    private long ghostFrame = -1;
    private final IntMap<Pattern> ghostTiles = new IntMap<>();

    private boolean updateGhosts(){
        if(player == null || player.unit() == null) return false;
        if(ghostFrame == Core.graphics.getFrameId()) return true;
        ghostFrame = Core.graphics.getFrameId();
        ghostTiles.clear();

        IntMap<Integer> ids = new IntMap<>();
        for(BuildPlan plan : player.unit().plans){
            if(plan.breaking || plan.block != this || !Build.validPlace(this, player.team(), plan.x, plan.y, plan.rotation)) continue;
            int pos = Point2.pack(plan.x, plan.y);
            if(!ids.containsKey(pos)) ids.put(pos, ids.size);
        }
        claim(ids, patterns, ghostTiles, null, null, null, null);
        return true;
    }

    static void claim(IntMap<Integer> ids, Seq<Pattern> patterns, IntMap<Pattern> claimed,
                      @Nullable IntMap<Integer> bases, @Nullable IntSeq order, @Nullable IntMap<Integer> prevAnchors, @Nullable IntMap<Pattern> prevPatterns){
        class Cand{
            Pattern pattern;
            int base, anchor, active = 1;
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        }
        Seq<Cand> cands = new Seq<>();
        IntSeq cells = ids.keys().toSeq(new IntSeq());
        IntSet seen = new IntSet();

        for(Pattern p : patterns){
            if(p.shape == null) continue;
            int pw = p.shape.width(), ph = p.shape.height();
            seen.clear();
            for(int i = 0; i < cells.size; i++){
                int cx = Point2.x(cells.get(i)), cy = Point2.y(cells.get(i));
                for(int dy = 0; dy < ph; dy++){
                    for(int dx = 0; dx < pw; dx++){
                        if(!p.shape.get(dx, dy)) continue;
                        int bx = cx - dx, by = cy - dy;
                        if(!seen.add(Point2.pack(bx, by))) continue;

                        Cand c = new Cand();
                        c.pattern = p;
                        c.base = Point2.pack(bx, by);
                        int apos = Point2.pack(bx + p.shape.anchorX, by + p.shape.anchorY);
                        if(!ids.containsKey(apos)){
                            search:
                            for(int sy = 0; sy < ph; sy++){
                                for(int sx = 0; sx < pw; sx++){
                                    if(p.shape.get(sx, sy)){
                                        apos = Point2.pack(bx + sx, by + sy);
                                        break search;
                                    }
                                }
                            }
                        }
                        c.anchor = apos;

                        boolean fits = true;
                        for(int sy = 0; sy < ph && fits; sy++){
                            for(int sx = 0; sx < pw; sx++){
                                if(!p.shape.get(sx, sy)) continue;
                                int pos = Point2.pack(bx + sx, by + sy);
                                if(!ids.containsKey(pos)){
                                    fits = false;
                                    break;
                                }
                                int id = ids.get(pos, 0);
                                if(id < c.min) c.min = id;
                                if(id > c.max) c.max = id;
                                if(prevPatterns != null && (prevPatterns.get(c.anchor) != p || prevAnchors.get(pos, Integer.MIN_VALUE) != c.anchor)) c.active = 0;
                            }
                        }
                        if(fits) cands.add(c);
                    }
                }
            }
        }

        cands.sort((a, b) -> {
            int area = Integer.compare(patternArea(b.pattern), patternArea(a.pattern));
            if(area != 0) return area;
            if(a.active != b.active) return Integer.compare(b.active, a.active);
            if(a.min != b.min) return Integer.compare(a.min, b.min);
            return Integer.compare(a.max, b.max);
        });

        for(Cand c : cands){
            Pattern p = c.pattern;
            int bx = Point2.x(c.base), by = Point2.y(c.base);
            boolean free = true;
            for(int dy = 0; dy < p.shape.height() && free; dy++){
                for(int dx = 0; dx < p.shape.width(); dx++){
                    if(p.shape.get(dx, dy) && claimed.containsKey(Point2.pack(bx + dx, by + dy))){
                        free = false;
                        break;
                    }
                }
            }
            if(!free) continue;

            p.shape.each((dx, dy) -> claimed.put(Point2.pack(bx + dx, by + dy), null));
            claimed.put(c.anchor, p);
            if(bases != null) bases.put(c.anchor, c.base);
            if(order != null) order.add(c.anchor);
        }
    }

    static int patternArea(Pattern p){
        return p.shape == null ? 0 : p.shape.width() * p.shape.height();
    }

    static void drawPattern(Pattern p, float x, float y, float rotation){
        Draw.rect(p.region,
        x + ((p.shape.width() - 1) / 2f - p.shape.anchorX) * tilesize,
        y + ((p.shape.height() - 1) / 2f - p.shape.anchorY) * tilesize, rotation);
    }

    @PatternShare
    public abstract class PatternTileBuildComp extends Building{
        public @PatternLocal Block baseBlock;
        public @Nullable Building patternAnchor;
        public @Nullable Pattern activePattern;
        public transient Seq<Building> group = new Seq<>();
        @PatternLocal transient float sx, sy;

        private static final ObjectMap<Class<?>, Field[]> modCache = new ObjectMap<>();

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
            boolean anchor = isPatternAnchor();
            write.bool(anchor);
            if(anchor) write.s(patterns.indexOf(activePattern));
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
            handleRemoval();
        }

        @Replace
        @Override
        public void onRemoved(){
            super.onRemoved();
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
                    if(o == null || o == this || o.team != team || o instanceof PatternTileBuildc || !seen.add(o)) continue;
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
                if(Math.abs(dx) + Math.abs(dy) == 1) return m;
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
                Building s = src(other);
                if(other.acceptItem(s, item) && canDump(other, item)){
                    other.handleItem(s, item);
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
            Seq<Item> allItems = content.items();
            int itemSize = allItems.size;
            Object[] itemArray = allItems.items;
            for(int i = 0; i < proximity.size; i++){
                Building other = proximity.get((i + dump) % proximity.size);
                if(todump == null){
                    for(int ii = 0; ii < itemSize; ii++){
                        if(items.has(ii) && give(other, (Item)itemArray[ii])) return true;
                    }
                }else if(give(other, todump)){
                    return true;
                }
                incrementDump(proximity.size);
            }
            return false;
        }

        Building src(Building other){
            return isPatternAnchor() ? edge(other) : self();
        }

        boolean give(Building other, Item item){
            Building s = src(other);
            if(other.acceptItem(s, item) && canDump(other, item)){
                other.handleItem(s, item);
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

        static void swapBlock(Building b, Block target){
            b.block = target;
            BlockCloner.swapOuter(b, target);
        }

        void handleRemoval(){
            leaveGraphs(this);
            Block base = baseBlock();
            if(base != null && block != base) swapBlock(this, base);
            patternAnchor = null;
            activePattern = null;
        }

        void resolveCluster(){
            Seq<Building> cluster = collect();
            if(cluster.isEmpty()) return;

            IntMap<Integer> ids = new IntMap<>();
            IntMap<Building> map = new IntMap<>();
            for(Building b : cluster){
                int pos = Point2.pack(b.tile.x, b.tile.y);
                map.put(pos, b);
                ids.put(pos, b.id);
            }

            IntMap<Integer> prevAnchors = new IntMap<>();
            IntMap<Pattern> prevPatterns = new IntMap<>();
            for(Building b : cluster){
                if(b instanceof PatternTileBuildc mb){
                    Pattern p = mb.isPatternAnchor() ? mb.activePattern() : null;
                    if(p != null && p.shape != null && b.tile != null){
                        int apos = Point2.pack(b.tile.x, b.tile.y);
                        prevPatterns.put(apos, p);
                        p.shape.each((dx, dy) -> {
                            int cpos = Point2.pack(b.tile.x - p.shape.anchorX + dx, b.tile.y - p.shape.anchorY + dy);
                            if(ids.containsKey(cpos)) prevAnchors.put(cpos, apos);
                        });
                    }
                    swapBlock(b, mb.baseBlock());
                    mb.patternAnchor(null);
                    mb.activePattern(null);
                }
            }

            IntMap<Pattern> claimed = new IntMap<>();
            IntMap<Integer> bases = new IntMap<>();
            IntSeq order = new IntSeq();

            claim(ids, patterns, claimed, bases, order, prevAnchors, prevPatterns);

            Seq<Seq<Building>> groups = new Seq<>();
            Seq<Building> groupAnchors = new Seq<>();
            for(int i = 0; i < order.size; i++){
                int apos = order.get(i);
                Pattern p = claimed.get(apos);
                int base = bases.get(apos, 0);
                int bx = Point2.x(base), by = Point2.y(base);
                Building anchor = map.get(apos);
                Seq<Building> members = new Seq<>();
                p.shape.each((dx, dy) -> {
                    Building m = map.get(Point2.pack(bx + dx, by + dy));
                    members.add(m);
                    if(m instanceof PatternTileBuildc mb){
                        mb.patternAnchor(anchor);
                        mb.activePattern(p);
                    }
                });
                groups.add(members);
                groupAnchors.add(anchor);
            }

            link(groups, groupAnchors);

            for(Building b : cluster){
                if(b instanceof PatternTileBuildc mb && mb.patternAnchor() == null && prevAnchors.containsKey(Point2.pack(b.tile.x, b.tile.y))){
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
                    swapBlock(m, target);
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
                        if(seq != null) seq.remove(b);
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
                    out.add(f);
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
            if(member()) return;
            Pattern pat = activePattern;
            if(pat != null && pat.region != null && pat.region.found()){
                drawPattern(pat, x, y, drawrot());
            }else{
                super.draw();
            }
        }

        @Replace
        @Override
        public void drawLight(){
            if(!member()) super.drawLight();
        }

        @Replace
        @Override
        public TextureRegion getDisplayIcon(){
            Pattern pat = usePatternIcon ? active() : null;
            TextureRegion icon = pat == null ? null : pat.icon();
            return icon != null && icon.found() ? icon : super.getDisplayIcon();
        }

        @Replace
        @Override
        public String getDisplayName(){
            Pattern pat = usePatternName ? active() : null;
            return pat != null ? pat.localizedName() : super.getDisplayName();
        }
    }
}
