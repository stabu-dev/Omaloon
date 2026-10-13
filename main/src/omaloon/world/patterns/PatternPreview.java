package omaloon.world.patterns;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.world.*;
import omaloon.gen.*;

import java.util.*;

import static mindustry.Vars.*;

/**
 * Draws plans as the patterns they form.
 * @author stabu_
 */
public class PatternPreview{
    ObjectIntMap<BuildPlan> order = new ObjectIntMap<>();
    int queued;
    ObjectSet<BuildPlan> resolved = new ObjectSet<>();
    ObjectMap<BuildPlan, Pattern> drawn = new ObjectMap<>();
    ObjectIntMap<BuildPlan> origins = new ObjectIntMap<>();
    long frame = -1;
    @Nullable Eachable<BuildPlan> list;

    /** resolved clusters in build order */
    LongMap<Resolved> cache = new LongMap<>();
    Seq<Pattern> patterns;

    PatternClaim claim = new PatternClaim();
    /** simulation after queued plans */
    PatternClaim checkpoint = new PatternClaim();
    long checkpointKey;
    int prefix;
    long prefixKey;
    IntMap<BuildPlan> plans = new IntMap<>();
    Seq<BuildPlan> ordered = new Seq<>();
    IntSet seen = new IntSet();
    IntSeq queue = new IntSeq();

    Block base;
    int x, y;
    Boolf<BuildPlan> find = o -> o.x == x && o.y == y && !o.breaking && (o.block == base || o.block instanceof PatternTileBlockc b && b.cellBase() == base);
    BuildPlan placing;
    Boolf<BuildPlan> blocking = o -> o != placing && !o.breaking && order.get(o, queued) < queued && !(placing.block.canReplace(o.block) && o.x == placing.x && o.y == placing.y);
    Comparator<BuildPlan> byOrder = Structs.comparingLong(p -> order.containsKey(p) ? order.get(p, 0) : (1L << 40) + Point2.pack(p.x, p.y));

    public void draw(Block base, BuildPlan plan, Eachable<BuildPlan> list){
        if(Core.graphics.getFrameId() != frame || list != this.list){
            frame = Core.graphics.getFrameId();
            this.list = list;
            order.clear();
            resolved.clear();
            drawn.clear();
            origins.clear();
            if(player.unit() != null){
                for(BuildPlan p : player.unit().plans) order.put(p, order.size);
            }
            queued = order.size;
            for(BuildPlan p : control.input.linePlans) order.put(p, order.size);
            for(BuildPlan p : control.input.selectPlans) order.put(p, order.size);
        }

        // the cursor never groups
        if(plan != control.input.bplan && !resolved.contains(plan)) resolve(base, plan, list);

        Pattern p = drawn.get(plan);
        if(p != null){
            int b = origins.get(plan, 0);
            Draw.rect(p.region, (Point2.x(b) + (p.shape.width() - 1) / 2f) * tilesize, (Point2.y(b) + (p.shape.height() - 1) / 2f) * tilesize);
        }else if(!drawn.containsKey(plan)){
            Draw.rect(base.fullIcon, plan.drawx(), plan.drawy());
        }
    }

    void resolve(Block base, BuildPlan start, Eachable<BuildPlan> list){
        this.base = base;
        PatternTileBlockc tiles = (PatternTileBlockc)base;
        claim.clear();
        plans.clear();
        ordered.clear();
        seen.clear();
        queue.clear();
        resolved.add(start);

        queue.add(Point2.pack(start.x, start.y));
        while(!queue.isEmpty()){
            int pos = queue.pop();
            x = Point2.x(pos);
            y = Point2.y(pos);
            if(!seen.add(pos)) continue;
            BuildPlan plan = x == start.x && y == start.y ? start : Block.findPlan(list, x, y, find);
            // invalid plans are drawn alone, in red
            if(plan == null || plan.worldContext && !valid(plan)) continue;
            plans.put(pos, plan);
            ordered.add(plan);
            Point2[] cells = ((PatternTileBlockc)plan.block).planGroup(plan);
            if(cells != null) claim.remember(pos, cells, true, null);
            for(Point2 d : Geometry.d4) queue.add(Point2.pack(x + d.x, y + d.y));
        }

        ordered.sort(byOrder);
        resolved.addAll(ordered);
        patterns = tiles.patterns();

        // queued plans come first in build order
        long key = System.identityHashCode(base);
        prefix = 0;
        for(BuildPlan plan : ordered){
            key = key * 0x9E3779B97F4A7C15L + Point2.pack(plan.x, plan.y);
            key = key * 0x9E3779B97F4A7C15L + System.identityHashCode(plan.block) * 4L + plan.rotation;
            key = key * 0x9E3779B97F4A7C15L + System.identityHashCode(((PatternTileBlockc)plan.block).planGroup(plan));
            if(order.get(plan, queued) < queued){
                prefix++;
                prefixKey = key;
            }
        }
        Resolved cached = cache.get(key);
        if(cached == null){
            // only clusters drawn recently stay
            if(cache.size >= 64) cache.clear();
            cache.put(key, cached = simulate());
        }
        for(int i = 0; i < cached.at.size; i++){
            BuildPlan plan = plans.get(cached.at.get(i));
            int pattern = cached.pattern.get(i);
            drawn.put(plan, pattern < 0 ? null : patterns.get(pattern));
            if(pattern >= 0) origins.put(plan, cached.origin.get(i));
        }
    }

    Resolved simulate(){
        PatternTileBlockc tiles = (PatternTileBlockc)base;
        int start = 0;
        if(prefix > 0 && checkpointKey == prefixKey){
            // resume after the queue, plans after it keep their pasted groups
            claim.set(checkpoint);
            for(int id = prefix; id < ordered.size; id++){
                BuildPlan plan = ordered.get(id);
                Point2[] cells = ((PatternTileBlockc)plan.block).planGroup(plan);
                if(cells != null) claim.remember(Point2.pack(plan.x, plan.y), cells, true, null);
            }
            start = prefix;
        }
        boolean unpasted = false;
        for(var e : claim.ids) unpasted |= !claim.planned.contains(e.key);
        for(int id = start; id < ordered.size; id++){
            BuildPlan plan = ordered.get(id);
            int at = Point2.pack(plan.x, plan.y);
            claim.ids.put(at, id);
            unpasted |= !claim.planned.contains(at);
            // only cells without a pasted group remember what they were claimed into
            if(unpasted || id == ordered.size - 1) step(tiles);
            if(id == prefix - 1) save();
        }

        Resolved out = new Resolved();
        for(int i = 0; i < claim.anchors.size; i++){
            Pattern p = claim.claimed.get(i);
            if(p.region == null || !p.region.found()) continue;
            int b = claim.bases.get(i), first = Integer.MAX_VALUE;
            for(int dy = 0; dy < p.shape.height(); dy++){
                for(int dx = 0; dx < p.shape.width(); dx++){
                    int pos = Point2.pack(Point2.x(b) + dx, Point2.y(b) + dy);
                    if(p.shape.get(dx, dy) && plans.containsKey(pos)) first = Math.min(first, pos);
                }
            }
            for(int dy = 0; dy < p.shape.height(); dy++){
                for(int dx = 0; dx < p.shape.width(); dx++){
                    int pos = Point2.pack(Point2.x(b) + dx, Point2.y(b) + dy);
                    if(!p.shape.get(dx, dy) || !plans.containsKey(pos)) continue;
                    // drawn by the lowest plan
                    out.at.add(pos);
                    out.pattern.add(pos == first ? patterns.indexOf(p, true) : -1);
                    out.origin.add(b);
                }
            }
        }
        return out;
    }

    void step(PatternTileBlockc tiles){
        claim.claim(patterns);
        // unpasted cells keep their claimed group, like builds
        for(var e : claim.ids) if(!claim.planned.contains(e.key)) claim.groups.remove(e.key, 0);
        for(int i = 0; i < claim.anchors.size; i++){
            Pattern p = claim.claimed.get(i);
            int b = claim.bases.get(i);
            for(int dy = 0; dy < p.shape.height(); dy++){
                for(int dx = 0; dx < p.shape.width(); dx++){
                    int pos = Point2.pack(Point2.x(b) + dx, Point2.y(b) + dy);
                    if(p.shape.get(dx, dy) && !claim.planned.contains(pos)) claim.remember(pos, ((PatternTileBlockc)tiles.cell(p, dx, dy, 0)).cellGroup(0), false, p);
                }
            }
        }
    }

    void save(){
        checkpoint.clear();
        for(var e : claim.ids){
            checkpoint.ids.put(e.key, e.value);
            if(claim.planned.contains(e.key)) checkpoint.planned.add(e.key);
            if(!claim.groups.containsKey(e.key)) continue;
            int group = claim.groups.get(e.key, 0);
            checkpoint.groups.put(e.key, group);
            checkpoint.sizes.put(group, claim.sizes.get(group, 0));
            if(claim.patterns.containsKey(group)) checkpoint.patterns.put(group, claim.patterns.get(group));
        }
        checkpointKey = prefixKey;
    }

    static class Resolved{
        /** grouped plans and their patterns */
        IntSeq at = new IntSeq(), pattern = new IntSeq(), origin = new IntSeq();
    }

    boolean valid(BuildPlan plan){
        int o = order.get(plan, -1);
        // never touches the drawn list
        // e.g. plans of other players, never drawn as invalid
        if(o < 0) return true;
        if(!Build.validPlace(plan.block, player.team(), plan.x, plan.y, plan.rotation)) return o < queued && control.input.planMatches(plan);
        if(o < queued) return true;
        placing = plan;
        return Block.findPlan(control.input.allPlans(), plan.x, plan.y, plan.block.size, blocking) == null;
    }
}
