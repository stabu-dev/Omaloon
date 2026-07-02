package omaloon.type;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.entities.*;
import omaloon.entities.part.*;
import omaloon.gen.*;

public class GlasmoreUnitType extends UnitType{

    public boolean killSmallChains = false;
    public boolean splittable = false;

    public float segmentLayerOffset = 0.001f;

    public Rect[][] treadStrips;
    public transient TextureRegion chunkReg = new TextureRegion(), treadChainRegion;

    public Seq<Blade> blades = new Seq<>();
    public float bladeDeathMoveSlowdown = 0.01f, fallDriftScl = 60f;
    public float fallSmokeX = 0f, fallSmokeY = 0f, fallSmokeChance = 0.1f;

    public GlasmoreUnitType(String name){
        super(name);
        outlineColor = Color.valueOf("2f2f36");
        envDisabled = Env.space;
        researchCostMultiplier = 8f;
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
        for(Blade blade : blades){
            if(!blade.bladeRegion.found() || blade.bladeOutlineRegion.found()) continue;
            makeOutline(PageType.main, packer, blade.bladeRegion, true, outlineColor, outlineRadius);
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
        ConstructPart.currentUnitRotation = unit.rotation;
        float ground = groundLayer;
        float air = flyingLayer;

        if(unit instanceof Chainedc chain){
            groundLayer += segmentLayerOffset * chain.segment();
            flyingLayer += segmentLayerOffset * chain.segment();
        }

        if(unit instanceof Ornithopterc) drawBlades((Unit & Ornithopterc)unit);
        super.draw(unit);

        groundLayer = ground;
        flyingLayer = air;
    }

    public <T extends Unit&Ornithopterc> void drawBlades(T unit){
        applyColor(unit);
        float z = unit.elevation > 0.5f ? (lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f);

        int i = 0;
        for(Blade.BladeMount mount : unit.blades()){
            Blade blade = mount.blade;
            float rx = unit.x + Angles.trnsx(unit.rotation - 90, blade.x, blade.y);
            float ry = unit.y + Angles.trnsy(unit.rotation - 90, blade.x, blade.y);
            float bladeScl = Draw.scl * blade.bladeSizeScl;
            float shadeScl = Draw.scl * blade.shadeSizeScl;

            int seedIndex = blade.mirror ? i / 2 : i;
            float moveAngle = Mathf.randomSeed(unit.drawSeed() + seedIndex, blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle);
            float rot = unit.rotation - 90 + blade.side * moveAngle;

            if(blade.bladeRegion.found()){
                Draw.z(z + blade.layerOffset);
                Draw.alpha(blade.blurRegion.found() ? 1 - (unit.bladeMoveSpeedScl() / 0.8f) : 1);
                Draw.rect(
                blade.bladeOutlineRegion, rx, ry,
                blade.bladeOutlineRegion.width * bladeScl * blade.side,
                blade.bladeOutlineRegion.height * bladeScl,
                rot
                );
                Draw.mixcol(Color.white, unit.hitTime);
                Draw.rect(blade.bladeRegion, rx, ry,
                blade.bladeRegion.width * bladeScl * blade.side,
                blade.bladeRegion.height * bladeScl,
                rot
                );
                Draw.reset();
            }

            if(blade.blurRegion.found()){
                Draw.z(z + blade.layerOffset);
                Draw.alpha(unit.bladeMoveSpeedScl() * blade.blurAlpha * (unit.dead() ? unit.bladeMoveSpeedScl() * 0.5f : 1));
                Draw.rect(
                blade.blurRegion, rx, ry,
                blade.blurRegion.width * bladeScl * blade.side,
                blade.blurRegion.height * bladeScl,
                rot
                );
                Draw.reset();
            }

            if(blade.shadeRegion.found()){
                Draw.alpha(unit.bladeMoveSpeedScl() * blade.blurAlpha * (unit.dead() ? unit.bladeMoveSpeedScl() * 0.5f : 1));
                Draw.rect(
                blade.shadeRegion, rx, ry,
                blade.shadeRegion.width * shadeScl * blade.side,
                blade.shadeRegion.height * shadeScl,
                rot
                );
                Draw.mixcol(Color.white, unit.hitTime);
                Draw.reset();
            }
            i++;
        }
    }

    @Override
    public void init(){
        super.init();

        Seq<Blade> temp = new Seq<>(blades);
        blades.clear();

        for(Blade blade : temp){
            blades.add(blade);
            if(blade.mirror){
                Blade clone = blade.copy();
                clone.x *= -1f;
                clone.flipSprite = !clone.flipSprite;
                clone.side = -1f;
                blades.add(clone);
            }
        }
    }

    @Override
    public void load(){
        super.load();
        treadChainRegion = Core.atlas.find(name + "-treads-chain");
        blades.each(Blade::load);
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

    @Override
    public void setStats(){
        super.setStats();
        if(sample instanceof Chainedc){
            if(segmentUnit != null) stats.add(Stat.weapons, StatValues.weapons(this, segmentUnit.weapons));
            if(segmentEndUnit != null) stats.add(Stat.weapons, StatValues.weapons(this, segmentEndUnit.weapons));
        }
    }

    @Override
    public Unit spawn(Team team, float x, float y, float rotation, Cons<Unit> cons){
        Unit unit = super.spawn(team, x, y, rotation, cons);

        if(unit instanceof Chainedc chain && segmentUnit != null){
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