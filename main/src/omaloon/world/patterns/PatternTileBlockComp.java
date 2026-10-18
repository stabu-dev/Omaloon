package omaloon.world.patterns;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.ctype.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.ConstructBlock.*;
import mindustry.world.meta.*;
import mindustry.world.modules.*;
import omaloon.annotations.Annotations.*;
import omaloon.gen.*;
import omaloon.gen.PatternGroupc.*;
import omaloon.gen.PatternTileBlockc.*;
import sun.misc.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import static mindustry.Vars.*;

/**
 * <s>Another</s> A block whose builds form patterns acting as one.
 * @author stabu_
 */
@MergeComponent
public abstract class PatternTileBlockComp extends Block implements PatternGroupc{
    @Import Seq<Pattern> patterns;

    public @Nullable Block cellBase;
    public @Nullable Pattern cellPattern;
    public @Nullable Point2[][] cellGroups;
    public @Nullable Block lone;

    public PatternTileBlockComp(String name){
        super(name);
        if(planChannel == null) registerEvents();
    }

    public void createCells(){
        if(lone == null) lone = createCell(name + "-lone");
        int rotations = rotations();
        for(Pattern p : patterns){
            int w = p.shape.width();
            if(p.cells != null && p.cells.length == w * p.shape.height() * rotations) continue;
            p.cells = new Block[w * p.shape.height() * rotations];
            p.shape.each((x, y) -> {
                for(int r = 0; r < rotations; r++) p.cells[(x + y * w) * rotations + r] = createCell(p.name + "-" + x + "-" + y + (rotate ? "-" + r : ""));
            });
        }
    }

    int rotations(){
        return rotate ? 4 : 1;
    }

    public Block cell(Pattern p, int x, int y, int rotation){
        return p.cells[(x + y * p.shape.width()) * rotations() + (rotate ? Mathf.mod(rotation, 4) : 0)];
    }

    Block createCell(String cellName){
        try{
            String prefix = content.transformName("");
            Class<?> type = getClass().isAnonymousClass() ? getClass().getSuperclass() : getClass();
            Block cell = (Block)type.getConstructor(String.class).newInstance(cellName.startsWith(prefix) ? cellName.substring(prefix.length()) : cellName);
            // schematics find cells by name
            if(content.blocks().peek() == cell) content.blocks().pop();
            cell.id = id;
            // filled in init
            ((PatternTileBlockc)cell).cellBase(this);
            return cell;
        }catch(Exception e){
            throw new RuntimeException("Failed to create pattern cell " + cellName, e);
        }
    }

    void fillCell(Block cell, @Nullable Pattern p, int x, int y, int rotation){
        try{
            for(Class<?> c = cell.getClass(); c != UnlockableContent.class; c = c.getSuperclass()){
                for(Field f : c.getDeclaredFields()){
                    if(Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers()) || f.getType() == Stats.class) continue;
                    f.setAccessible(true);
                    Object value = f.get(this);
                    if(value instanceof Seq<?> seq){
                        value = new Seq<>(seq);
                    }else if(value instanceof OrderedMap<?, ?> map){
                        value = new OrderedMap<>(map);
                    }else if(value instanceof ObjectMap<?, ?> map){
                        value = new ObjectMap<>(map);
                    }else if(value instanceof ObjectSet<?> set){
                        value = new ObjectSet<>(set);
                    }
                    f.set(cell, value);
                }
            }
        }catch(Exception e){
            throw new RuntimeException("Failed to fill pattern cell " + cell.name, e);
        }
        PatternTileBlockc c = (PatternTileBlockc)cell;
        c.cellBase(this);
        c.cellPattern(p);
        if(p != null){
            // indexed by build rotation
            Point2[][] groups = new Point2[4][];
            for(int r = 0; r < 4; r++) groups[(r + rotation) % 4] = p.offsets(x, y, r);
            c.cellGroups(groups);
        }
        cell.minfo = minfo;
        cell.localizedName = localizedName;
        cell.description = description;
        cell.details = details;
        cell.rotate = rotate || p != null;
        cell.rotateDraw = false;
        cell.quickRotate = false;
        cell.drawArrow = false;
        if(p != null && p.patch != null) p.patch.apply(cell);
    }

    public @Nullable Point2[] cellGroup(int rotation){
        return cellGroups == null ? null : cellGroups[Mathf.mod(rotation, 4)];
    }

    public @Nullable Block planCell(Point2[] cells, int[] rotation){
        for(Pattern p : patterns){
            for(int y = 0; y < p.shape.height(); y++){
                for(int x = 0; x < p.shape.width(); x++){
                    if(!p.shape.get(x, y)) continue;
                    for(int r = 0; r < 4; r++){
                        Point2[] group = ((PatternTileBlockc)cell(p, x, y, 0)).cellGroup(r);
                        boolean same = group.length == cells.length;
                        for(int i = 0; i < cells.length && same; i++) same = Structs.contains(group, cells[i]);
                        if(!same) continue;
                        Block cell = cell(p, x, y, rotation[0] - r);
                        if(!rotate) rotation[0] = r;
                        return cell;
                    }
                }
            }
        }
        return null;
    }

    static @Nullable Unsafe unsafe = unsafe();
    static final ObjectMap<Class<?>, Field[]> outerFields = new ObjectMap<>();

    static @Nullable Unsafe unsafe(){
        try{
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe)f.get(null);
        }catch(Throwable e){
            return null;
        }
    }

    static void setOuter(Building b, Block target){
        Field[] fields = outerFields.get(b.getClass());
        if(fields == null){
            Seq<Field> out = new Seq<>();
            for(Class<?> c = b.getClass(); c != null && c != Object.class; c = c.getSuperclass()){
                for(Field f : c.getDeclaredFields()){
                    if(!f.isSynthetic() || !Block.class.isAssignableFrom(f.getType())) continue;
                    f.setAccessible(true);
                    out.add(f);
                }
            }
            outerFields.put(b.getClass(), fields = out.toArray(Field.class));
        }
        for(Field f : fields){
            try{
                if(unsafe != null){
                    unsafe.putObject(b, unsafe.objectFieldOffset(f), target);
                }else{
                    f.set(b, target);
                }
            }catch(Throwable e){
                // once Unsafe is gone, final fields may still be set by reflection
                if(unsafe == null) throw new IllegalStateException("Cannot point " + b.getClass().getName() + " at a pattern cell on this runtime", e);
                unsafe = null;
                setOuter(b, target);
                return;
            }
        }
    }

    static Block root(Block block){
        Block base = block instanceof PatternTileBlockc b ? b.cellBase() : null;
        return base == null ? block : base;
    }

    @Replace
    @Override
    public void init(){
        if(cellBase == null){
            createCells();
            patterns.sort((p1, p2) -> Integer.compare(p2.area(), p1.area()));
            fillCell(lone, null, 0, 0, 0);
            int rotations = rotations();
            for(Pattern p : patterns){
                for(int i = 0; i < p.cells.length; i++){
                    int at = i / rotations;
                    if(p.cells[i] != null) fillCell(p.cells[i], p, at % p.shape.width(), at / p.shape.width(), i % rotations);
                }
            }
        }
        super.init();
        for(Pattern p : patterns){
            clipSize = Math.max(clipSize, Math.max(p.shape.width(), p.shape.height()) * tilesize * 2f);
        }
        if(cellBase != null) return;
        lone.init();
        for(Pattern p : patterns){
            for(Block cell : p.cells) if(cell != null) cell.init();
        }
        // fail fast if cells cannot swap
        setOuter(newBuilding(), lone);
    }

    @Replace
    @Override
    public void load(){
        super.load();
        if(cellBase != null){
            region = cellBase.region;
            fullIcon = cellBase.fullIcon;
            uiIcon = cellBase.uiIcon;
            if(cellPattern != null && cellPattern.patch != null) cellPattern.patch.load(this);
            return;
        }
        for(Pattern p : patterns) p.load();
        lone.load();
        for(Pattern p : patterns){
            for(Block cell : p.cells) if(cell != null) cell.load();
        }
    }

    @Replace
    @Override
    public boolean unlocked(){
        return cellBase != null ? cellBase.unlocked() : super.unlocked();
    }

    @Replace
    @Override
    public boolean unlockedHost(){
        return cellBase != null ? cellBase.unlockedHost() : super.unlockedHost();
    }

    @Replace
    @Override
    public boolean isBanned(){
        return cellBase != null ? cellBase.isBanned() : super.isBanned();
    }

    @Replace
    @Override
    public boolean canReplace(Block other){
        Block base = root(this);
        // cells never replace each other
        if(root(other) == base && other != base) return false;
        return this == base ? super.canReplace(other) : base.canReplace(other);
    }

    @Replace
    @Override
    public void flipRotation(BuildPlan plan, boolean x){
        Point2[] group = cellGroup(plan.rotation);
        super.flipRotation(plan, x);
        if(group == null) return;
        Point2[] mirror = new Point2[group.length];
        for(int i = 0; i < group.length; i++) mirror[i] = new Point2(x ? -group[i].x : group[i].x, x ? group[i].y : -group[i].y);
        PatternTileBlockc base = (PatternTileBlockc)cellBase;
        int[] rotation = {plan.rotation};
        Block cell = base.planCell(mirror, rotation);
        plan.block = cell != null ? cell : base.lone();
        plan.rotation = cell != null ? rotation[0] : cellBase.planRotation(plan.rotation);
    }

    @Replace
    @Override
    public void onPicked(Tile tile){
        super.onPicked(tile);
        if(cellBase != null){
            control.input.block = cellBase;
            cellBase.lastConfig = lastConfig;
        }
    }

    @Replace
    @Override
    public void onNewPlan(BuildPlan plan){
        super.onNewPlan(plan);
        if(cellBase == null) return;
        // vanilla syncs plans without groups
        int pos = Point2.pack(plan.x, plan.y);
        Point2[] group = cellGroup(plan.rotation);
        plan(pos, player.team(), group == null ? alone : group);
        if(net.client()){
            Call.serverBinaryPacketReliable(planChannel, planPacket(pos));
        }else if(net.server()){
            Call.clientBinaryPacketReliable(planChannel, planPacket(pos));
        }
        plan.block = cellBase;
        plan.rotation = cellBase.planRotation(plan.rotation);
    }

    @Replace
    @Override
    public void placeEnded(Tile tile, @Nullable Unit builder, int rotation, @Nullable Object config){
        super.placeEnded(tile, builder, rotation, config);
        if(!(tile.build instanceof PatternTileBuildc build)) return;
        // constructFinish skips this for cells
        if(config != null && tile.build.block != this) tile.build.configured(builder, config);
        // hand placed
        if(build.patternPlan() == null) build.clearPlans(true);
    }

    @Replace
    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        preview.draw(root(this), plan, list);
    }

    public @Nullable Point2[] planGroup(BuildPlan plan){
        return cellBase != null ? cellGroup(plan.rotation) : plan.worldContext ? plannedCells.get(Point2.pack(plan.x, plan.y)) : null;
    }

    static final PatternPreview preview = new PatternPreview();

    static final Point2[] alone = {new Point2()};
    static final IntMap<Point2[]> plannedCells = new IntMap<>();
    static final IntMap<Team> plannedTeams = new IntMap<>();
    static String planChannel;
    /** bumped on cluster change */
    static int clusterVersion, visitStamp;
    static boolean resolving;

    void registerEvents(){
        planChannel = "omaloon-" + getClass().getName() + "-plans";

        // before schematics load
        Events.on(ModContentLoadEvent.class, e -> content.blocks().copy().each(b -> {
            if(b instanceof PatternTileBlockc p && p.cellBase() == null) p.createCells();
        }));

        Events.on(ResetEvent.class, e -> {
            plannedCells.clear();
            plannedTeams.clear();
        });

        SaveVersion.addCustomChunk(planChannel, new SaveFileReader.CustomChunk(){
            @Override
            public void write(DataOutput stream) throws IOException{
                stream.writeInt(plannedCells.size);
                for(var e : plannedCells) writePlan(stream, e.key);
            }

            @Override
            public void read(DataInput stream) throws IOException{
                int amount = stream.readInt();
                for(int i = 0; i < amount; i++){
                    try{
                        readPlan(stream, null);
                    }catch(IOException e){
                        Log.warn("Skipped pattern plan: @", e.getMessage());
                    }
                }
            }
        });

        Interval timer = new Interval();
        Events.run(Trigger.update, () -> {
            if(!net.client() && state.isGame() && timer.get(120f)) prunePlans();
        });

        // vanilla counts the cell that was placed, but objectives are written against the block itself
        Events.on(BlockBuildEndEvent.class, e -> {
            if(e.breaking || e.team != state.rules.defaultTeam) return;
            if(e.tile.block() instanceof PatternTileBlockc cell && cell.cellBase() != null){
                state.stats.placedBlockCount.increment(cell.cellBase(), 1);
            }
        });

        // net handlers are created after content
        Runnable server = () -> netServer.addBinaryPacketHandler(planChannel, (sender, data) -> {
            try{
                Call.clientBinaryPacketReliable(planChannel, planPacket(readPlan(new DataInputStream(new ByteArrayInputStream(data)), sender.team())));
            }catch(IOException e){
                Log.warn("Invalid pattern plan from @: @", sender.plainName(), e.getMessage());
            }
        });
        Events.run(ServerLoadEvent.class, server);
        Events.on(ClientLoadEvent.class, e -> {
            server.run();
            netClient.addBinaryPacketHandler(planChannel, data -> {
                try{
                    readPlan(new DataInputStream(new ByteArrayInputStream(data)), null);
                }catch(IOException ex){
                    Log.err(ex);
                }
            });
        });
    }

    public static void prunePlans(){
        IntSeq stale = new IntSeq();
        for(var e : plannedTeams){
            int x = Point2.x(e.key), y = Point2.y(e.key);
            if(world.build(e.key) instanceof ConstructBuild
            || e.value.data().units.contains(u -> u.plans.find(p -> p.x == x && p.y == y && !p.breaking) != null)
            || e.value.data().plans.find(p -> p.x == x && p.y == y) != null) continue;
            stale.add(e.key);
        }
        for(int i = 0; i < stale.size; i++){
            dropPlan(stale.get(i));
            // empty packet removes
            if(net.server()) Call.clientBinaryPacketReliable(planChannel, planPacket(stale.get(i)));
        }
    }

    static void dropPlan(int pos){
        Point2[] group = plannedCells.remove(pos);
        plannedTeams.remove(pos);
        if(group == null) return;
        for(Point2 m : group){
            if(!(world.build(Point2.x(pos) + m.x, Point2.y(pos) + m.y) instanceof PatternTileBuildc mb) || mb.patternPlan() == null) continue;
            Building b = (Building)mb;
            for(Point2 c : mb.patternPlan()){
                int cx = b.tile.x + c.x, cy = b.tile.y + c.y;
                if(!(world.build(cx, cy) instanceof PatternTileBuildc) && !plannedCells.containsKey(Point2.pack(cx, cy))){
                    mb.dropPlan();
                    break;
                }
            }
        }
    }

    static void plan(int pos, Team team, Point2[] cells){
        plannedCells.put(pos, cells);
        plannedTeams.put(pos, team);
    }

    static byte[] planPacket(int pos){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try{
            writePlan(new DataOutputStream(out), pos);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    static void writePlan(DataOutput stream, int pos) throws IOException{
        Point2[] cells = plannedCells.get(pos, new Point2[0]);
        stream.writeInt(pos);
        stream.writeByte(plannedTeams.get(pos, Team.derelict).id);
        stream.writeByte(cells.length);
        for(Point2 c : cells){
            stream.writeByte(c.x);
            stream.writeByte(c.y);
        }
    }

    /** @param team overrides the written team */
    static int readPlan(DataInput stream, @Nullable Team team) throws IOException{
        int pos = stream.readInt();
        Team written = Team.get(stream.readUnsignedByte());
        int amount = stream.readUnsignedByte();
        if(amount > 64) throw new IOException("Too many pattern cells: " + amount);
        if(amount == 0){
            dropPlan(pos);
            return pos;
        }
        Point2[] cells = new Point2[amount];
        boolean self = false;
        for(int i = 0; i < amount; i++){
            cells[i] = new Point2(stream.readByte(), stream.readByte());
            if(cells[i].x == 0 && cells[i].y == 0) self = true;
            for(int j = 0; j < i; j++) if(cells[j].equals(cells[i])) throw new IOException("Repeated pattern cell");
        }
        if(!self) throw new IOException("Pattern plan without its own tile");
        if(world.tile(pos) == null) throw new IOException("Pattern plan outside of the world");
        plan(pos, team == null ? written : team, cells);
        return pos;
    }

    public abstract class PatternTileBuildComp extends Building implements PatternGroupBuildc{
        @Import @Nullable Building patternAnchor;
        @Import @Nullable Pattern activePattern;
        @Import boolean keepConfig;
        /** group this build was pasted or rebuilt with */
        public transient @Nullable Point2[] patternPlan;
        @PatternLocal transient int resolvedVersion = -1;
        /** set once removal starts */
        @PatternLocal transient boolean leaving;
        @PatternLocal transient int visited;

        @Replace
        @Override
        public void created(){
            super.created();
            if(plannedTeams.get(tile.pos()) == team){
                patternPlan = plannedCells.remove(tile.pos());
                plannedTeams.remove(tile.pos());
            }
            // e.g. a loadout
            if(patternPlan == null && ((PatternTileBlockc)block).cellBase() != null){
                patternPlan = intent() != null ? intent() : alone;
            }
        }

        /** group this build wants */
        public @Nullable Point2[] intent(){
            return ((PatternTileBlockc)block).cellGroup(rotation);
        }

        public Block baseBlock(){
            return root(block);
        }

        public void rememberGroup(PatternClaim claim){
            if(patternPlan != null){
                claim.remember(tile.pos(), patternPlan, true, null);
            }else if(intent() != null){
                // only unrotated groups match their pattern
                claim.remember(tile.pos(), intent(), false, rotation == 0 ? ((PatternTileBlockc)block).cellPattern() : null);
            }
        }

        @Replace
        @Override
        public void write(Writes write){
            super.write(write);
            Point2[] cells = patternPlan != null ? patternPlan : intent();
            int amount = cells == null ? 0 : cells.length;
            write.b(0x80 | amount);
            for(int i = 0; i < amount; i++){
                write.b(cells[i].x);
                write.b(cells[i].y);
            }
        }

        @Replace
        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            int head = read.ub();
            if((head & 0x80) == 0){
                // legacy format
                int index = head == 1 ? read.s() : -1;
                Pattern p = index >= 0 && index < patterns.size ? patterns.get(index) : null;
                patternPlan = p == null ? null : p.offsets(p.shape.anchorX, p.shape.anchorY, 0);
                return;
            }
            int amount = head & 0x7f;
            patternPlan = amount == 0 ? null : new Point2[amount];
            for(int i = 0; i < amount; i++) patternPlan[i] = new Point2(read.b(), read.b());
        }

        public void dropPlan(){
            clusterVersion++;
            patternPlan = null;
            resolveCluster();
        }

        public void clearPlans(boolean resolve){
            clusterVersion++;
            boolean any = false;
            for(int i = -1; i < 4; i++){
                Building o = i < 0 ? self() : world.build(tile.x + Geometry.d4[i].x, tile.y + Geometry.d4[i].y);
                if(!(o instanceof PatternTileBuildc ob) || ob.patternPlan() == null || ob.baseBlock() != baseBlock()) continue;
                for(Point2 c : ob.patternPlan()){
                    if(world.build(o.tile.x + c.x, o.tile.y + c.y) instanceof PatternTileBuildc mb && mb.patternPlan() != null){
                        mb.patternPlan(null);
                        if(mb.patternAnchor() == null && mb != self()) swapLone((Building)mb);
                        any = true;
                    }
                }
            }
            if(any && resolve) resolveCluster();
        }

        @Replace
        @Override
        public void addPlan(boolean checkPrevious, boolean ignoreConditions){
            // rebuild plans use the base block
            Point2[] cells = patternPlan != null ? patternPlan : intent();
            Block current = block;
            int rot = rotation;
            block = baseBlock();
            rotation = block.planRotation(rotation);
            try{
                super.addPlan(checkPrevious, ignoreConditions);
            }finally{
                block = current;
                rotation = rot;
            }
            plan(tile.pos(), team, cells != null ? cells : alone);
        }

        @Replace
        @Override
        public void onProximityAdded(){
            if(!member()) super.onProximityAdded();
            clusterVersion++;
            resolveCluster();
        }

        @Replace
        @Override
        public void onProximityUpdate(){
            if(!member()) super.onProximityUpdate();
            // anchors of the cluster being resolved update their neighbors once it is done
            if(resolving) return;
            if(resolvedVersion != clusterVersion){
                resolveCluster();
            }else if(patternAnchor != null && patternAnchor.isValid()){
                // only a building outside the cluster changed
                if(member()) leaveGraphs();
                patternAnchor.updateProximity();
            }
        }

        @Replace
        @Override
        public void onRemoved(){
            clusterVersion++;
            leaving = true;
            super.onRemoved();
            if(dead && patternAnchor instanceof PatternTileBuildc ab){
                // wait for the rebuild
                for(Building m : ab.group()){
                    if(m != self() && m instanceof PatternTileBuildc mb && mb.patternPlan() == null) mb.patternPlan(mb.intent());
                }
            }else{
                clearPlans(false);
            }
            leaveGraphs();
            patternAnchor = null;
            activePattern = null;
        }

        Seq<Building> collect(){
            Seq<Building> out = new Seq<>();
            if(world == null || tile == null || !isValid() || leaving) return out;
            Block base = baseBlock();
            int stamp = ++visitStamp;
            Seq<Building> queue = new Seq<>();
            queue.add(this);
            visited = stamp;
            while(!queue.isEmpty()){
                Building cur = queue.pop();
                out.add(cur);
                if(cur.tile == null) continue;
                for(Point2 p : Geometry.d4){
                    Tile t = world.tile(cur.tile.x + p.x, cur.tile.y + p.y);
                    if(t == null || t.build == null) continue;
                    Building o = t.build;
                    if(!o.isValid() || o.team != team || !(o instanceof PatternTileBuildc ob) || ob.leaving() || ob.baseBlock() != base) continue;
                    if(ob.visited() != stamp){
                        ob.visited(stamp);
                        queue.add(o);
                    }
                }
            }
            return out;
        }

        void resolveCluster(){
            Seq<Building> cluster = collect();
            if(cluster.isEmpty()) return;
            resolving = true;
            try{
                resolve(cluster);
            }finally{
                resolving = false;
            }
        }

        void resolve(Seq<Building> cluster){

            PatternClaim claim = new PatternClaim();
            IntMap<Building> map = new IntMap<>();
            for(Building b : cluster){
                map.put(b.pos(), b);
                claim.ids.put(b.pos(), b.id);
                ((PatternTileBuildc)b).rememberGroup(claim);
            }
            claim.claim(patterns);

            Seq<Seq<Building>> groups = new Seq<>();
            IntSet grouped = new IntSet();
            BoolSeq kept = new BoolSeq();
            for(int i = 0; i < claim.anchors.size; i++){
                Pattern p = claim.claimed.get(i);
                Building anchor = map.get(claim.anchors.get(i));
                int bx = Point2.x(claim.bases.get(i)), by = Point2.y(claim.bases.get(i));
                Seq<Building> members = new Seq<>();
                p.shape.each((dx, dy) -> members.add(map.get(Point2.pack(bx + dx, by + dy))));
                // groups that stay as they were keep their state
                boolean same = ((PatternTileBuildc)anchor).patternAnchor() == anchor && ((PatternTileBuildc)anchor).group().size == members.size;
                for(Building m : members){
                    same &= ((PatternTileBuildc)m).patternAnchor() == anchor && ((PatternTileBuildc)m).activePattern() == p;
                    grouped.add(m.pos());
                }
                groups.add(members);
                kept.add(same);
            }

            // pasted builds keep config until complete
            for(Building b : cluster) ((PatternTileBuildc)b).keepConfig(((PatternTileBuildc)b).patternPlan() != null);
            for(int i = 0; i < groups.size; i++){
                Seq<Building> members = groups.get(i);
                for(Building m : members){
                    PatternTileBuildc mb = (PatternTileBuildc)m;
                    if(mb.patternPlan() == null || mb.patternPlan().length != members.size) continue;
                    boolean complete = true;
                    for(Point2 c : mb.patternPlan()) complete &= members.contains(o -> o.tile.x == m.tile.x + c.x && o.tile.y == m.tile.y + c.y);
                    if(complete) mb.keepConfig(false);
                }
            }

            Seq<Building> left = new Seq<>();
            for(Building b : cluster){
                PatternTileBuildc mb = (PatternTileBuildc)b;
                if(grouped.contains(b.pos())) continue;
                if(mb.patternAnchor() != null) left.add(b);
                mb.patternAnchor(null);
                mb.activePattern(null);
                // copies keep unfinished pastes
                int[] rotation = {b.rotation};
                Block cell = mb.patternPlan() == null ? null : ((PatternTileBlockc)mb.baseBlock()).planCell(mb.patternPlan(), rotation);
                if(cell != null){
                    swapBlock(b, cell, rotation[0]);
                }else{
                    swapLone(b);
                }
            }

            ObjectSet<BlockModule> used = new ObjectSet<>();
            for(int i = 0; i < groups.size; i++){
                if(kept.get(i)) continue;
                Pattern p = claim.claimed.get(i);
                Building anchor = map.get(claim.anchors.get(i));
                int bx = Point2.x(claim.bases.get(i)), by = Point2.y(claim.bases.get(i));
                p.shape.each((dx, dy) -> {
                    Building m = map.get(Point2.pack(bx + dx, by + dy));
                    PatternTileBuildc mb = (PatternTileBuildc)m;
                    mb.patternAnchor(anchor);
                    mb.activePattern(p);
                    swapBlock(m, ((PatternTileBlockc)mb.baseBlock()).cell(p, dx, dy, m.rotation), root(m.block).rotate ? m.rotation : 0);
                });
                ((PatternTileBuildc)anchor).link(groups.get(i), used);
            }

            for(Building b : left){
                ((PatternTileBuildc)b).freshState();
                ((PatternTileBuildc)b).joinGraphs();
            }
            for(Building b : cluster) ((PatternTileBuildc)b).resolvedVersion(clusterVersion);
        }

        static void swapLone(Building b){
            swapBlock(b, ((PatternTileBlockc)root(b.block)).lone(), root(b.block).rotate ? b.rotation : 0);
        }

        static void swapBlock(Building b, Block target, int rotation){
            b.rotation = rotation;
            if(b.block == target) return;
            // new builds are indexed by TileChangeEvent
            int i = b.indexerBuildIndex & 0xffff;
            boolean index = !world.isGenerating() && b.tile.build == b && i < b.team.data().buildings.size && b.team.data().buildings.get(i) == b;
            if(index) indexer.removeIndex(b.tile);
            b.tile.updateBlockReference(target);
            b.block = target;
            setOuter(b, target);
            if(index) indexer.addIndex(b.tile);
        }

    }
}
