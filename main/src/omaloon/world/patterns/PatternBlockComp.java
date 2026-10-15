package omaloon.world.patterns;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.modules.*;
import omaloon.annotations.Annotations.*;
import omaloon.gen.PatternBlockc.*;

import java.lang.reflect.*;

import static mindustry.Vars.*;

/**
 * <s>Custom-shaped block.</s> A block whose builds form patterns acting as one.
 * @author stabu_
 */
//TODO: make it into a proper multi-block, rather than a weird PatternTileBlock alternative like it is now.
@MergeComponent
public abstract class PatternBlockComp extends Block{
    public Seq<Pattern> patterns = new Seq<>();
    public boolean usePatternName = false;
    public boolean usePatternIcon = false;

    public PatternBlockComp(String name){
        super(name);
    }

    @PatternShare
    public abstract class PatternBuildComp extends Building{
        public transient @Nullable Building patternAnchor;
        public transient @Nullable Pattern activePattern;
        public transient Seq<Building> group = new Seq<>();
        @PatternLocal transient float sx, sy;
        @PatternLocal public transient boolean keepConfig;

        private static final ObjectMap<Class<?>, Field[]> modCache = new ObjectMap<>();

        public boolean isPatternAnchor(){
            return patternAnchor == this;
        }

        public boolean member(){
            return patternAnchor != null && patternAnchor != this;
        }

        public Pattern active(){
            if(activePattern != null) return activePattern;
            if(patternAnchor instanceof PatternBuildc ab) return ab.activePattern();
            return null;
        }

        @InternalImpl
        public abstract void distributeState(ObjectSet<BlockModule> used);

        @InternalImpl
        public abstract void pullMirrorState();

        @InternalImpl
        public abstract void pushMirrorState();

        @InternalImpl
        public abstract void freshState();

        /** makes this build a group anchor */
        public void link(Seq<Building> members, ObjectSet<BlockModule> used){
            if(!isValid()) return;
            group = members;
            distributeState(used);
            for(Building m : members){
                if(m != self()) ((PatternBuildc)m).leaveGraphs();
            }
            pushMirrorState();
            spreadHealth(groupHealth());
            // the group takes the anchor's config
            Object config = config();
            if(config != null) spreadConfig(null, config);
            updateProximity();
        }

        public boolean grouped(){
            return patternAnchor != null && (patternAnchor == this || patternAnchor.isValid());
        }

        public float groupHealth(){
            float sum = 0f;
            for(Building m : ((PatternBuildc)patternAnchor).group()) sum += m.health;
            return sum;
        }

        public float groupMaxHealth(){
            float sum = 0f;
            for(Building m : ((PatternBuildc)patternAnchor).group()) sum += m.maxHealth;
            return sum;
        }

        public void spreadHealth(float total){
            float max = groupMaxHealth();
            for(Building m : ((PatternBuildc)patternAnchor).group()){
                m.health = Mathf.clamp(total / max, 0f, 1f) * m.maxHealth;
                m.healthChanged();
            }
        }

        public void spreadConfig(@Nullable Unit builder, Object value){
            if(spreading) return;
            spreading = true;
            try{
                for(Building m : ((PatternBuildc)patternAnchor).group()){
                    if(m != self() && !((PatternBuildc)m).keepConfig()) m.configured(builder, value);
                }
            }finally{
                spreading = false;
            }
        }

        static boolean spreading;

        @Replace
        @Override
        public void configured(@Nullable Unit builder, @Nullable Object value){
            super.configured(builder, value);
            if(grouped()) spreadConfig(builder, value);
        }

        @Replace
        @Override
        public double sense(LAccess sensor){
            if(!grouped()) return super.sense(sensor);
            if(sensor == LAccess.health) return groupHealth();
            if(sensor == LAccess.maxHealth) return groupMaxHealth();
            return member() ? patternAnchor.sense(sensor) : super.sense(sensor);
        }

        @Replace
        @Override
        public Object senseObject(LAccess sensor){
            return member() && patternAnchor.isValid() ? patternAnchor.senseObject(sensor) : super.senseObject(sensor);
        }

        @Replace
        @Override
        public void applyBoost(float intensity, float duration){
            if(member() && patternAnchor.isValid()){
                patternAnchor.applyBoost(intensity, duration);
            }else{
                super.applyBoost(intensity, duration);
            }
        }

        @Replace
        @Override
        public void applySlowdown(float intensity, float duration){
            if(member() && patternAnchor.isValid()){
                patternAnchor.applySlowdown(intensity, duration);
            }else{
                super.applySlowdown(intensity, duration);
            }
        }

        @Replace
        @Override
        public boolean canPickup(){
            return !grouped() && super.canPickup();
        }

        @Replace
        @Override
        public boolean shouldAmbientSound(){
            return !member() && super.shouldAmbientSound();
        }

        public void joinGraphs(){
            super.onProximityAdded();
            super.onProximityUpdate();
        }

        @SuppressWarnings("unchecked")
        public void leaveGraphs(){
            Building b = self();
            for(Field f : mods(b.getClass())){
                try{
                    Object mod = f.get(b);
                    Field gf = mod == null ? null : graphField(mod.getClass());
                    Object graph = gf == null ? null : gf.get(mod);
                    if(graph == null) continue;
                    for(Field sf : graphSeqs(graph.getClass())){
                        if(sf.get(graph) instanceof Seq<?> seq) ((Seq<Object>)seq).remove(b);
                    }
                    java.lang.reflect.Method check = graphChecks.get(graph.getClass());
                    if(check != null) check.invoke(graph);
                }catch(Exception ignored){
                }
            }
        }

        static final ObjectMap<Class<?>, Field> graphFields = new ObjectMap<>();
        static final ObjectMap<Class<?>, Field[]> graphSeqs = new ObjectMap<>();
        static final ObjectMap<Class<?>, java.lang.reflect.Method> graphChecks = new ObjectMap<>();

        static @Nullable Field graphField(Class<?> module){
            if(graphFields.containsKey(module)) return graphFields.get(module);
            Field out = null;
            for(Class<?> c = module; c != null && c != Object.class && out == null; c = c.getSuperclass()){
                try{
                    out = c.getDeclaredField("graph");
                    out.setAccessible(true);
                }catch(NoSuchFieldException ignored){
                }
            }
            graphFields.put(module, out);
            return out;
        }

        static Field[] graphSeqs(Class<?> graph){
            Field[] out = graphSeqs.get(graph);
            if(out != null) return out;
            Seq<Field> seqs = new Seq<>();
            for(Field f : graph.getFields()) if(Seq.class.isAssignableFrom(f.getType())) seqs.add(f);
            try{
                graphChecks.put(graph, graph.getMethod("checkEntity"));
            }catch(NoSuchMethodException ignored){
            }
            graphSeqs.put(graph, out = seqs.toArray(Field.class));
            return out;
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
            return activePattern == null ? x : x + activePattern.offsetX();
        }

        public float patternY(){
            return activePattern == null ? y : y + activePattern.offsetY();
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
                    if(o == null || o == this || o.team != team || o instanceof PatternBuildc || !seen.add(o)) continue;
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
                // externals only see the member tiles they touch, never the anchor
                for(Building m : group) if(m != null) o.proximity.remove(m, true);
                for(Building m : group){
                    if(m == null || m.tile == null) continue;
                    if(Math.abs(o.tile.x - m.tile.x) + Math.abs(o.tile.y - m.tile.y) == 1) o.proximity.addUnique(m);
                }
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
                for(Building m : group) if(m != null) o.proximity.remove(m, true);
                o.onProximityUpdate();
            });
            proximity.clear();
        }

        /** @return member next to other */
        Building src(Building other){
            if(isPatternAnchor() && other != null && other.tile != null){
                for(Building m : group){
                    if(m != null && m.tile != null && m.isValid() && Math.abs(other.tile.x - m.tile.x) + Math.abs(other.tile.y - m.tile.y) == 1) return m;
                }
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

        @Replace
        @Override
        public void draw(){
            if(member()) return;
            Pattern pat = activePattern;
            if(pat != null && pat.region != null && pat.region.found()){
                Draw.rect(pat.region, x + pat.offsetX(), y + pat.offsetY());
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
