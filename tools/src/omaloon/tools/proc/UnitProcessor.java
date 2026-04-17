package omaloon.tools.proc;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.noise.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.*;
import omaloon.tools.GenAtlas.*;
import omaloon.tools.*;

import java.util.concurrent.*;

import static mindustry.Vars.content;
import static omaloon.tools.Tools.*;

/**
 * A processor to generate unit sprites such as:
 * <ul>
 *     <li> Weapon preview sprites ({@code -preview}).</li>
 *     <li> Full composite icons ({@code -full}).</li>
 *     <li> Wreck regions ({@code -wreck}).</li>
 * </ul>
 * Checks if regions already exist before generating them. Outlines are generated internally but not saved separately.
 * @author GlennFolker
 * @author Drullkus
 * @author Anuke
 * @author SK7725 (integration of outline/preview generation)
 * @author stabu_
 */
public class UnitProcessor implements Processor{

    private Pixmap tintCell(GenRegion region){
        Pixmap original = region.pixmap();
        Pixmap tinted = original.copy();

        int whitePlaceholder = Color.whiteRgba;
        int grayPlaceholder = Color.valueOf("DCC6C6").rgba();
        Color targetColor1 = Color.valueOf("FFA664");
        Color targetColor2 = Color.valueOf("D06B53");

        tinted.each((x, y) -> {
            int currentColor = tinted.getRaw(x, y);

            if(currentColor == whitePlaceholder){
                tinted.set(x, y, targetColor1);
            }else if(currentColor == grayPlaceholder){
                tinted.set(x, y, targetColor2);
            }
        });

        return tinted;
    }

    private GenRegion ensureRegion(String name, GenRegion relative, Prov<Pixmap> pixmapProvider){
        GenRegion existing = Tools.atlas.find(name);
        if(existing.found()){
            return existing;
        }

        Pixmap pixmap = pixmapProvider.get();
        if(pixmap == null){
            Log.err("Pixmap provider returned null for @", name);
            return Tools.atlas.find(name);
        }

        String relativePath = "";
        if(relative != null && relative.found()){
            relativePath = relative.relativePath;
        }

        GenRegion reg = new GenRegion(name, pixmap);
        reg.relativePath = relativePath;
        reg.save(true);

        return Tools.atlas.find(name);
    }

    private void ensureWreckRegion(String unitName, int index, Prov<Pixmap> pixmapProvider){
        String name = unitName + "-wreck" + index;
        String wreckPath = "rubble/";

        GenRegion existing = Tools.atlas.find(name);
        if(existing.found()){
            return;
        }

        Pixmap pixmap = pixmapProvider.get();
        if(pixmap == null){
            Log.err("Pixmap provider returned null for wreck @", name);
            return;
        }

        GenRegion reg = new GenRegion(name, pixmap);
        reg.relativePath = wreckPath;
        reg.save(true);
    }

    @Override
    public void process(ExecutorService exec){
        content.units().each(OmaloonMod::isOmaloon, (UnitType type) -> submit(exec, type.name, () -> {
            Pixmap unitOutlinePixmap = null;
            Pixmap compositeIcon = null;

            try{
                init(type);
                load(type);

                float scl = Draw.scl / 4f;

                GenRegion unitBaseRegion = conv(type.region);
                if(!unitBaseRegion.found()){
                    Log.warn("Base region @ not found for unit: @. Skipping sprite generation.", unitBaseRegion, type.name);
                    return;
                }

                unitOutlinePixmap = Pixmaps.outline(new PixmapRegion(unitBaseRegion.pixmap()), type.outlineColor, type.outlineRadius);

                ObjectMap<Weapon, GenRegion> weaponPreviews = new ObjectMap<>();

                for(var weapon : type.weapons){
                    weapon.load();

                    GenRegion weaponRegion = conv(weapon.region);
                    if(!weaponRegion.found()){
                        continue;
                    }
                    final GenRegion finalWeaponRegion = weaponRegion;

                    GenRegion weaponPreviewRegion = ensureRegion(weapon.name + "-preview", weaponRegion, () -> {
                        Pixmap weaponOutlinePixmapInternal = null;
                        Pixmap previewPixmap;
                        try{
                            weaponOutlinePixmapInternal = Pixmaps.outline(new PixmapRegion(finalWeaponRegion.pixmap()), type.outlineColor, type.outlineRadius);

                            int pWidth = Math.max(finalWeaponRegion.width, weaponOutlinePixmapInternal.width);
                            int pHeight = Math.max(finalWeaponRegion.height, weaponOutlinePixmapInternal.height);
                            previewPixmap = new Pixmap(pWidth, pHeight);

                            previewPixmap.draw(weaponOutlinePixmapInternal, pWidth / 2 - weaponOutlinePixmapInternal.width / 2, pHeight / 2 - weaponOutlinePixmapInternal.height / 2, false);
                            previewPixmap.draw(finalWeaponRegion.pixmap(), pWidth / 2 - finalWeaponRegion.width / 2, pHeight / 2 - finalWeaponRegion.height / 2, true);

                            GenRegion weaponCellRegion = conv(weapon.cellRegion);
                            if(weaponCellRegion.found()){
                                Pixmap cell = tintCell(weaponCellRegion);
                                previewPixmap.draw(cell, pWidth / 2 - cell.width / 2, pHeight / 2 - cell.height / 2, true);
                                cell.dispose();
                            }
                            return previewPixmap;
                        }finally{
                            if(weaponOutlinePixmapInternal != null){
                                weaponOutlinePixmapInternal.dispose();
                            }
                        }
                    });
                    weaponPreviews.put(weapon, weaponPreviewRegion);
                }

                String fullIconName = type.name + "-full";
                GenRegion existingFull = Tools.atlas.find(fullIconName);
                Pixmap fullIconPixmap = null;
                GenRegion fullRegion;

                if(existingFull.found()){
                    fullRegion = existingFull;
                    if(fullRegion.found()) fullIconPixmap = fullRegion.pixmap();
                }else{
                    Seq<DrawInstruction> instructions = new Seq<>();
                    Unit unit = type.constructor.get();
                    Pixmap tintedCell = null;

                    if(unit instanceof Mechc){
                        GenRegion base = conv(type.baseRegion);
                        GenRegion leg = conv(type.legRegion);
                        if(base.found()) instructions.add(new DrawInstruction(base.pixmap(), 0, 0, -2));
                        if(leg.found()){
                            instructions.add(new DrawInstruction(leg.pixmap(), 0, 0, -1));
                            Pixmap flippedLeg = leg.pixmap().flipX();
                            DrawInstruction flippedInstr = new DrawInstruction(flippedLeg, 0, 0, -1);
                            flippedInstr.tempFlipped = flippedLeg;
                            instructions.add(flippedInstr);
                        }
                    }

                    if(unit instanceof Tankc){
                        GenRegion treads = conv(type.treadRegion);
                        if(treads.found()){
                            Pixmap outlinedTreads = Pixmaps.outline(new PixmapRegion(treads.pixmap()), type.outlineColor, type.outlineRadius);
                            DrawInstruction treadsInstr = new DrawInstruction(outlinedTreads, 0, 0, -0.05f);
                            treadsInstr.tempFlipped = outlinedTreads;
                            instructions.add(treadsInstr);
                        }
                    }

                    type.weapons.select(w -> w.layerOffset < 0).each(weapon -> {
                        GenRegion preview = weaponPreviews.get(weapon);
                        if(preview == null || !preview.found()) return;
                        Pixmap pix = preview.pixmap();
                        DrawInstruction instr = new DrawInstruction(pix, weapon.x / scl, -weapon.y / scl, weapon.layerOffset - 0.01f);
                        if(weapon.flipSprite){
                            instr.tempFlipped = pix.flipX();
                            instr.pixmap = instr.tempFlipped;
                        }
                        instructions.add(instr);
                    });

                    if(unitOutlinePixmap != null){
                        DrawInstruction outlineInstr = new DrawInstruction(unitOutlinePixmap, 0, 0, -0.1f);
                        outlineInstr.blend = false;
                        instructions.add(outlineInstr);
                    }
                    if(unitBaseRegion.found()){
                        instructions.add(new DrawInstruction(unitBaseRegion.pixmap(), 0, 0, 0f));
                    }

                    GenRegion cellRegion = conv(type.cellRegion);
                    if(cellRegion.found()){
                        tintedCell = tintCell(cellRegion);
                        instructions.add(new DrawInstruction(tintedCell, 0, 0, 0.01f));
                    }

                    type.weapons.select(w -> w.layerOffset >= 0).each(weapon -> {
                        GenRegion preview = weaponPreviews.get(weapon);
                        if(preview == null || !preview.found()) return;
                        Pixmap pix = preview.pixmap();
                        DrawInstruction instr = new DrawInstruction(pix, weapon.x / scl, -weapon.y / scl, weapon.layerOffset + 0.1f);
                        if(weapon.flipSprite){
                            instr.tempFlipped = pix.flipX();
                            instr.pixmap = instr.tempFlipped;
                        }
                        instructions.add(instr);
                    });

                    if(instructions.isEmpty()){
                        Log.warn("No parts to draw for unit: @. Skipping -full generation.", type.name);
                        if(tintedCell != null) tintedCell.dispose();
                        instructions.each(DrawInstruction::disposeTemporary);
                        return;
                    }

                    instructions.sort(i -> i.layerOffset);

                    Rect bounds = new Rect();
                    float maxAbsX = 0, maxAbsY = 0;
                    for(DrawInstruction instr : instructions){
                        instr.bounds(bounds);
                        maxAbsX = Math.max(maxAbsX, Math.abs(bounds.x));
                        maxAbsX = Math.max(maxAbsX, Math.abs(bounds.x + bounds.width));
                        maxAbsY = Math.max(maxAbsY, Math.abs(bounds.y));
                        maxAbsY = Math.max(maxAbsY, Math.abs(bounds.y + bounds.height));
                    }

                    int finalWidth = Math.max(1, Mathf.ceil(maxAbsX * 2f));
                    int finalHeight = Math.max(1, Mathf.ceil(maxAbsY * 2f));
                    compositeIcon = new Pixmap(finalWidth, finalHeight);

                    float originX = finalWidth / 2f;
                    float originY = finalHeight / 2f;
                    for(DrawInstruction instr : instructions){
                        Pixmap pix = instr.pixmap;
                        int drawX = Mathf.floor(originX + instr.offsetX - pix.width / 2f);
                        int drawY = Mathf.floor(originY + instr.offsetY - pix.height / 2f);
                        compositeIcon.draw(pix, drawX, drawY, instr.blend);
                        instr.disposeTemporary();
                    }

                    if(tintedCell != null){
                        tintedCell.dispose();
                    }

                    fullRegion = new GenRegion(fullIconName, compositeIcon);
                    fullRegion.relativePath = unitBaseRegion.relativePath;
                    fullRegion.save(true);

                    if(fullRegion.found()) fullIconPixmap = fullRegion.pixmap();
                }

                if(fullIconPixmap != null){
                    String uiIconName = type.name + "-ui";
                    if(!atlas.has(uiIconName)){
                        GenRegion uiRegion = new GenRegion(uiIconName, fullIconPixmap.copy());
                        uiRegion.relativePath = "ui";
                        uiRegion.save(true);
                    }
                }

                if(type.health > 0 && fullIconPixmap != null){
                    Rand rand = new Rand();
                    rand.setSeed(type.name.hashCode());
                    int splits = 3;
                    float degrees = rand.random(360f);
                    float offsetRange = Math.max(fullIconPixmap.width, fullIconPixmap.height) * 0.15f;
                    Vec2 offset = new Vec2(1, 1).rotate(rand.random(360f)).setLength(rand.random(0, offsetRange)).add(fullIconPixmap.width / 2f, fullIconPixmap.height / 2f);
                    VoronoiNoise voronoi = new VoronoiNoise(type.id, true);

                    final Pixmap sourcePixmapForWrecks = fullIconPixmap;
                    for(int i = 0; i < splits; i++){
                        int finalI = i;
                        ensureWreckRegion(type.name, finalI + 1, () -> {
                            Pixmap wreckPixmap = new Pixmap(sourcePixmapForWrecks.width, sourcePixmapForWrecks.height);
                            sourcePixmapForWrecks.each((x, y) -> {
                                int rawColor = sourcePixmapForWrecks.getRaw(x, y);
                                if((rawColor & 0xff) == 0) return;
                                if(voronoi.noise(x, y, 1f / (14f + sourcePixmapForWrecks.width / 40f)) <= 0.47d){
                                    boolean darken = Math.max(Ridged.noise2d(1, x, y, 3, 1f / (20f + sourcePixmapForWrecks.width / 8f)), 0) > 0.16f;
                                    float dst = offset.dst(x, y);
                                    float noise = (float)Noise.rawNoise(dst / (9f + sourcePixmapForWrecks.width / 70f)) * (60 + sourcePixmapForWrecks.width / 30f);
                                    int wreckIndex = (int)Mathf.clamp(Mathf.mod(offset.angleTo(x, y) + noise + degrees, 360f) / 360f * splits, 0, splits - 1);

                                    if(wreckIndex == finalI){
                                        wreckPixmap.setRaw(x, y, Color.muli(rawColor, darken ? 0.7f : 1f));
                                    }
                                }
                            });
                            return wreckPixmap;
                        });
                    }
                }else if(fullIconPixmap == null && type.health > 0){
                    Log.warn("Cannot generate wrecks for @ because full icon pixmap is missing.", type.name);
                }

            }catch(Exception e){
                Log.err("Failed to process sprites for unit: " + type.name, e);
            }finally{
                if(unitOutlinePixmap != null){
                    unitOutlinePixmap.dispose();
                }

                if(compositeIcon != null){
                    compositeIcon.dispose();
                }
            }
        }));
    }

    private static class DrawInstruction{
        Pixmap pixmap;
        float offsetX, offsetY;
        float layerOffset;
        boolean blend = true;

        Pixmap tempFlipped = null;

        DrawInstruction(Pixmap pixmap, float offsetX, float offsetY, float layerOffset){
            this.pixmap = pixmap;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.layerOffset = layerOffset;
        }

        Rect bounds(Rect out){
            float w = pixmap.width / 2f;
            float h = pixmap.height / 2f;
            return out.set(offsetX - w, offsetY - h, pixmap.width, pixmap.height);
        }

        void disposeTemporary(){
            if(tempFlipped != null){
                tempFlipped.dispose();
                tempFlipped = null;
            }
        }
    }
}