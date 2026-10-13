package omaloon.world.patterns;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

import java.util.*;

/**
 * Splits tiles into groups, largest first.
 * @author stabu_
 */
public class PatternClaim{
    /** build ids by position */
    public IntIntMap ids = new IntIntMap();
    public IntIntMap groups = new IntIntMap(), sizes = new IntIntMap();
    public IntMap<Pattern> patterns = new IntMap<>();
    /** positions with a pasted group */
    public IntSet planned = new IntSet();

    public Seq<Pattern> claimed = new Seq<>();
    public IntSeq anchors = new IntSeq(), bases = new IntSeq();

    // tiles on a local grid of the cluster's bounds, indexed row by row
    int[] grid = {}, tilePos = {}, tileRank = {}, tileGroup = {}, order = {};
    boolean[] taken = {}, seen = {};
    long[] keys = {};
    int[] candBase = {}, candPattern = {};
    Seq<Pattern> sorted = new Seq<>();

    public void clear(){
        ids.clear();
        groups.clear();
        sizes.clear();
        patterns.clear();
        planned.clear();
        claimed.clear();
        anchors.clear();
        bases.clear();
    }

    public void set(PatternClaim other){
        clear();
        ids.putAll(other.ids);
        groups.putAll(other.groups);
        sizes.putAll(other.sizes);
        patterns.putAll(other.patterns);
        planned.addAll(other.planned);
    }

    /** pasted groups never merge with formed ones */
    public void remember(int pos, Point2[] cells, boolean plan, @Nullable Pattern pattern){
        int key = Integer.MAX_VALUE;
        for(Point2 c : cells) key = Math.min(key, Point2.pack(Point2.x(pos) + c.x, Point2.y(pos) + c.y));
        if(plan){
            key = ~key;
            planned.add(pos);
        }
        groups.put(pos, key);
        sizes.put(key, cells.length);
        if(pattern != null) patterns.put(key, pattern);
    }

    public void claim(Seq<Pattern> options){
        claimed.clear();
        anchors.clear();
        bases.clear();
        int n = ids.size;
        if(n == 0) return;

        int minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = Integer.MIN_VALUE, maxy = Integer.MIN_VALUE;
        for(var e : ids){
            minx = Math.min(minx, Point2.x(e.key));
            miny = Math.min(miny, Point2.y(e.key));
            maxx = Math.max(maxx, Point2.x(e.key));
            maxy = Math.max(maxy, Point2.y(e.key));
        }
        int w = maxx - minx + 1, h = maxy - miny + 1;
        if(grid.length < w * h) grid = new int[w * h];
        if(tilePos.length < n){
            tilePos = new int[n];
            tileRank = new int[n];
            tileGroup = new int[n];
            order = new int[n];
            taken = new boolean[n];
        }
        if(keys.length < n) keys = new long[n];

        Arrays.fill(grid, 0, w * h, -1);
        for(var e : ids) grid[Point2.x(e.key) - minx + (Point2.y(e.key) - miny) * w] = 0;
        int t = 0;
        for(int i = 0; i < w * h; i++){
            if(grid[i] < 0) continue;
            int pos = Point2.pack(minx + i % w, miny + i / w);
            tilePos[t] = pos;
            tileGroup[t] = groups.get(pos, Integer.MIN_VALUE);
            keys[t] = (long)ids.get(pos, 0) << 32 | t;
            grid[i] = t++;
        }
        Arrays.sort(keys, 0, n);
        for(int i = 0; i < n; i++) tileRank[(int)keys[i]] = i;
        // ties keep the order placements were found in, which follows the ids
        t = 0;
        for(var e : ids) order[t++] = grid[Point2.x(e.key) - minx + (Point2.y(e.key) - miny) * w];
        Arrays.fill(taken, 0, n, false);

        sorted.selectFrom(options, p -> p.shape != null);
        sorted.sort(Structs.comparingInt(p -> -p.area()));
        for(int from = 0, to; from < sorted.size; from = to){
            for(to = from; to < sorted.size && sorted.get(to).area() == sorted.get(from).area(); to++);

            int cands = 0;
            for(int pi = from; pi < to; pi++){
                Pattern p = sorted.get(pi);
                int[] cells = p.cellList();
                int pw = p.shape.width(), ph = p.shape.height(), sw = w + pw;
                if(seen.length < sw * (h + ph)) seen = new boolean[sw * (h + ph)];
                Arrays.fill(seen, 0, sw * (h + ph), false);
                for(int i = 0; i < n * cells.length / 2; i++){
                    int tile = order[i / (cells.length / 2)], cell = i % (cells.length / 2) * 2;
                    int bx = Point2.x(tilePos[tile]) - minx - cells[cell], by = Point2.y(tilePos[tile]) - miny - cells[cell + 1];
                    if(seen[bx + pw + (by + ph) * sw]) continue;
                    seen[bx + pw + (by + ph) * sw] = true;

                    int lo = Integer.MAX_VALUE, hi = -1, group = tileGroup[tile];
                    boolean same = true;
                    for(int c = 0; c < cells.length; c += 2){
                        int local = local(bx + cells[c], by + cells[c + 1], w, h);
                        if(local < 0){
                            lo = -1;
                            break;
                        }
                        lo = Math.min(lo, tileRank[local]);
                        hi = Math.max(hi, tileRank[local]);
                        same &= tileGroup[local] == group;
                    }
                    if(lo < 0) continue;

                    Pattern prev = patterns.get(group);
                    boolean active = same && group != Integer.MIN_VALUE && cells.length / 2 == sizes.get(group, -1) && (prev == null || prev == p);
                    if(cands >= candBase.length){
                        candBase = Arrays.copyOf(candBase, Math.max(64, cands * 2));
                        candPattern = Arrays.copyOf(candPattern, candBase.length);
                    }
                    if(cands >= keys.length) keys = Arrays.copyOf(keys, cands * 2);
                    candBase[cands] = Point2.pack(bx, by);
                    candPattern[cands] = pi;
                    keys[cands] = (active ? 0L : 1L) << 62 | (long)lo << 41 | (long)hi << 20 | cands;
                    cands++;
                }
            }

            Arrays.sort(keys, 0, cands);
            for(int k = 0; k < cands; k++){
                int ci = (int)(keys[k] & 0xfffff), bx = Point2.x(candBase[ci]), by = Point2.y(candBase[ci]);
                Pattern p = sorted.get(candPattern[ci]);
                int[] cells = p.cellList();
                boolean free = true;
                for(int c = 0; c < cells.length && free; c += 2) free = !taken[grid[bx + cells[c] + (by + cells[c + 1]) * w]];
                if(!free) continue;
                for(int c = 0; c < cells.length; c += 2) taken[grid[bx + cells[c] + (by + cells[c + 1]) * w]] = true;

                int ax = bx + p.shape.anchorX, ay = by + p.shape.anchorY;
                if(local(ax, ay, w, h) < 0){
                    ax = bx + cells[0];
                    ay = by + cells[1];
                }
                claimed.add(p);
                anchors.add(Point2.pack(minx + ax, miny + ay));
                bases.add(Point2.pack(minx + bx, miny + by));
            }
        }
    }

    int local(int x, int y, int w, int h){
        return x < 0 || y < 0 || x >= w || y >= h ? -1 : grid[x + y * w];
    }
}
