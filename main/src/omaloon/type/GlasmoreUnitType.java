package omaloon.type;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.part.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.entities.abilities.*;
import omaloon.entities.part.*;
import omaloon.gen.*;
import omaloon.world.meta.*;

public class GlasmoreUnitType extends UnitType{

    public static Unit currentUnit;
    public boolean killSmallChains = false;
    public boolean splittable = false;
    public boolean combinedHealth = false;
    public float segmentLayerOffset = 0.001f;
    public Rect[][] treadStrips;
    public transient TextureRegion chunkReg = new TextureRegion(), treadChainRegion;

    public float bladeDeathMoveSlowdown = 0.01f, fallDriftScl = 60f;
    public float fallSmokeX = 0f, fallSmokeY = 0f, fallSmokeChance = 0.1f;

    public GlasmoreUnitType(String name){
        super(name);
        outlineColor = Color.valueOf("2f2f36");
        envDisabled = Env.space;
        researchCostMultiplier = 8f;
        stats = new Stats(){
            @Override
            public OrderedMap<StatCat, OrderedMap<Stat, Seq<StatValue>>> toMap(){
                var map = super.toMap();
                for(var entry : map.entries()){
                    entry.value.orderedKeys().sort((s1, s2) -> {
                        float p1 = s1 == Stat.health ? 0f : (s1 == OlStats.minMaxSegments ? 0.1f : s1.id + 1);
                        float p2 = s2 == Stat.health ? 0f : (s2 == OlStats.minMaxSegments ? 0.1f : s2.id + 1);
                        return Float.compare(p1, p2);
                    });
                }
                return map;
            }
        };
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);

        for(var part : parts){
            if(part instanceof BladePart blade){
                blade.load(name);
                if(blade.bladeRegion.found() && !blade.bladeOutlineRegion.found()){
                    makeOutline(PageType.main, packer, blade.bladeRegion, true, outlineColor, outlineRadius);
                }
            }
        }

        if(sample instanceof Tankc){
            PixmapRegion pr = packer.get(name + "-treads");
            if(pr != null && treadRects.length == 0){
                setupTreads(pr);
            }
        }
    }

    protected void setupTreads(PixmapRegion pr){
        int w = pr.width, h = pr.height, out = outlineColor.rgba();
        boolean[][] solid = new boolean[w][h];

        for(int x = 0; x < w; x++)
            for(int y = 0; y < h; y++){
                int c = pr.pixmap.get(pr.x + x, pr.y + y);
                solid[x][y] = (c >>> 24) > 16 && c != out;
            }

        Seq<Seq<Rect>> islands = new Seq<>();

        for(int x = 0; x < w; x++)
            for(int y = 0; y < h; y++){
                if(!solid[x][y]) continue;

                Seq<Rect> strips = new Seq<>();
                IntSeq q = IntSeq.with(x, y);
                solid[x][y] = false;

                int[] iMin = new int[w], iMax = new int[w];
                java.util.Arrays.fill(iMin, h);
                java.util.Arrays.fill(iMax, -1);

                while(!q.isEmpty()){
                    int cy = q.pop(), cx = q.pop();
                    iMin[cx] = Math.min(iMin[cx], cy);
                    iMax[cx] = Math.max(iMax[cx], cy);

                    for(int d = 0; d < 4; d++){
                        int nx = cx + Geometry.d4x[d], ny = cy + Geometry.d4y[d];
                        if(nx >= 0 && nx < w && ny >= 0 && ny < h && solid[nx][ny]){
                            solid[nx][ny] = false;
                            q.add(nx, ny);
                        }
                    }
                }

                Rect last = null;
                for(int cx = 0; cx < w; cx++){
                    if(iMax[cx] == -1) continue;
                    int sy = iMin[cx], sh = iMax[cx] - sy + 1;

                    if(last != null && last.y == sy && last.height == sh) last.width++;
                    else strips.add(last = new Rect(cx, sy, 1, sh));
                }
                islands.add(strips);
            }

        treadRects = new Rect[islands.size];
        treadStrips = new Rect[islands.size][];

        for(int i = 0; i < islands.size; i++){
            treadStrips[i] = islands.get(i).toArray(Rect.class);
            treadRects[i] = new Rect(treadStrips[i][0]);
            for(Rect r : treadStrips[i]) treadRects[i].merge(r);
            treadRects[i].move(-w / 2f, -h / 2f);
        }
    }

    @Override
    public void draw(Unit unit){
        currentUnit = unit;
        ConstructPart.currentUnitRotation = unit.rotation;
        float ground = groundLayer;
        float air = flyingLayer;

        if(unit instanceof Chainedc chain){
            groundLayer += segmentLayerOffset * chain.segment();
            flyingLayer += segmentLayerOffset * chain.segment();
        }

        super.draw(unit);

        groundLayer = ground;
        flyingLayer = air;
        currentUnit = null;
    }

    @Override
    public void load(){
        super.load();
        treadChainRegion = Core.atlas.find(name + "-treads-chain");
    }

    @Override
    public <T extends Unit&Tankc> void drawTank(T unit){
        if(treadChainRegion == null || !treadChainRegion.found() || treadStrips == null){
            super.drawTank(unit);
            return;
        }

        applyColor(unit);
        Draw.rect(treadRegion, unit.x, unit.y, unit.rotation - 90);

        float s = Draw.scl;
        float rot = unit.rotation - 90;
        float progress = unit.treadTime() * 2f;

        for(int i = 0; i < treadRects.length; i++){
            int trackY = Math.round(treadRects[i].y + treadRegion.height / 2f);
            int trackH = Math.round(treadRects[i].height);
            float offset = (progress % trackH + trackH) % trackH;

            for(Rect strip : treadStrips[i]){
                float dy = trackY + (strip.y - trackY + offset) % trackH;
                float dh = Math.min(strip.height, trackY + trackH - dy);

                float ox = (strip.x + strip.width / 2f - treadRegion.width / 2f) * s;
                float oy = (treadRegion.height / 2f - strip.y) * s;

                drawChunk(strip.x, dy, strip.width, dh, ox, oy - dh / 2f * s, rot, unit);

                if(dh < strip.height){
                    float rh = strip.height - dh;
                    drawChunk(strip.x, trackY, strip.width, rh, ox, oy - dh * s - rh / 2f * s, rot, unit);
                }
            }
        }
    }

    protected void drawChunk(float sx, float sy, float sw, float sh, float ox, float oy, float rot, Unit unit){
        chunkReg.set(treadChainRegion, (int)sx, (int)sy, (int)sw, (int)sh);
        Tmp.v1.set(ox, oy).rotate(rot);
        Draw.rect(chunkReg, unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, sw * Draw.scl, sh * Draw.scl, rot);
    }

    public float getCombinedMaxHealth(int unitsCount){
        float total = health;
        UnitType segType = segmentUnit == null ? this : segmentUnit;
        for(int i = 0; i < unitsCount - 1; i++){
            UnitType type = (i == unitsCount - 2 && segmentEndUnit != null) ? segmentEndUnit : segType;
            total += type.health;
        }
        return total;
    }

    @Override
    public void setStats(){
        super.setStats();
        if(sample instanceof Chainedc){
            if(segmentUnit != null) stats.add(Stat.weapons, StatValues.weapons(this, segmentUnit.weapons));
            if(segmentEndUnit != null) stats.add(Stat.weapons, StatValues.weapons(this, segmentEndUnit.weapons));

            if(combinedHealth){
                float maxConnections = segmentUnits;
                for(var ability : abilities){
                    if(ability instanceof ConnectChainAbility c){
                        maxConnections = c.maxConnections;
                        break;
                    }
                }

                float minTotalHealth = getCombinedMaxHealth(segmentUnits);

                if(maxConnections > segmentUnits){
                    float maxTotalHealth = getCombinedMaxHealth((int)maxConnections);
                    stats.replace(Stat.health, OlStatValues.range(minTotalHealth, maxTotalHealth));
                    stats.add(OlStats.minMaxSegments, OlStatValues.range(segmentUnits, maxConnections));
                }else{
                    stats.replace(Stat.health, StatValues.number(minTotalHealth, StatUnit.none));
                    stats.add(OlStats.minMaxSegments, StatValues.number(segmentUnits, StatUnit.none));
                }
            }
        }
    }

    @Override
    public Unit spawn(Team team, float x, float y, float rotation, Cons<Unit> cons){
        Unit unit = super.spawn(team, x, y, rotation, cons);

        if(unit instanceof Chainedc chain && segmentUnit != null && chain.child() == null){
            chain.isExiting(false);
            UnitType segType = segmentUnit;
            for(int i = 0; i < segmentUnits - 1; i++){
                UnitType type = (i == segmentUnits - 2 && segmentEndUnit != null) ? segmentEndUnit : segType;
                Unit segment = type.create(team);
                Tmp.v1.trns(unit.rotation + 180f, segmentSpacing * (i + 1f)).add(unit);
                segment.set(Tmp.v1);
                segment.add();
                chain.connect(segment);
            }
            chain.head().updateChain();
        }

        return unit;
    }
}