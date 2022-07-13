package ol.world.blocks.crafting.multicraft;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.ArcRuntimeException;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.core.UI;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.ui.ItemImage;
import mindustry.world.Block;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;

import static mindustry.Vars.tilesize;

public class MultiCrafter extends Block {
    public float itemCapacityMultiplier = 1f;
    public float fluidCapacityMultiplier = 1f;
    public float powerCapacityMultiplier = 1f;
    /*
    [ ==> Seq
      { ==> ObjectMap
        // String ==> Any --- Value may be a Seq, Map or String
        input:{
          // String ==> Any --- Value may be a Seq<String>, Seq<Map>, String or Map
          items:["mod-id-item/1","mod-id-item2/1"],
          fluids:["mod-id-liquid/10.5","mod-id-gas/10"]
          power: 3 pre tick
        },
        output:{
          items:["mod-id-item/1","mod-id-item2/1"],
          fluids:["mod-id-liquid/10.5","mod-id-gas/10"]
          heat: 10
        },
        craftTime: 120
      }
    ]
     */
    /**
     * For Json and Javascript to configure
     */
    public Object recipes;
    /**
     * The resolved recipes.
     */
    @Nullable
    public Seq<Recipe> resolvedRecipes = null;
    public String menu = "Simple";
    @Nullable
    public RecipeShown selector = null;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public int[] fluidsOutputDirections = {-1};
    public float updateEffectChance = 0.04f;
    public float warmupSpeed = 0.019f;
    /**
     * Whether stop production when the fluid is full.
     * Turn off this to ignore fluid output, for instance, the fluid is only by-product.
     */
    public boolean stopProductionWhenFullFluid = true;
    /**
     * If true, the crafter with multiple fluid outputs will dump excess,
     * when there's still space for at least one fluid type.
     */
    public boolean dumpExtraFluid = true;
    public DrawBlock drawer = new DrawDefault();

    protected boolean isOutputItem = false;
    protected boolean isConsumeItem = false;
    protected boolean isConsumeFluid = false;
    protected boolean isConsumePower = false;
    protected boolean isOutputHeat = false;
    protected boolean isConsumeHeat = false;
    public Color heatColor = new Color(1f, 0.22f, 0.22f, 0.8f);
    public float powerCapacity = 0f;

    /**
     * For {@linkplain HeatConsumer},
     * it's used to display something of block or initialize the recipe index.
     */
    public int defaultRecipeIndex = 0;
    /**
     * For {@linkplain HeatConsumer},
     * after heat meets this requirement, excess heat will be scaled by this number.
     */
    public float overheatScale = 1f;
    /**
     * For {@linkplain HeatConsumer},
     * maximum possible efficiency after overheat.
     */
    public float maxEfficiency = 4f;
    /**
     * For {@linkplain HeatBlock}
     */
    public float warmupRate = 0.15f;

    public MultiCrafter(String name) {
        super(name);
        update = true;
        solid = true;
        sync = true;
        flags = EnumSet.of(BlockFlag.factory);
        ambientSound = Sounds.machine;
        configurable = true;
        saveConfig = true;
        ambientSoundVolume = 0.03f;
        config(Integer.class, MultiCrafterBuild::setCurRecipeIndexFromRemote);
    }

    @Override
    public void init() {
        hasItems = false;
        hasPower = false;
        hasLiquids = false;
        outputsPower = false;
        if (resolvedRecipes == null && recipes != null) { // if the recipe is already set in another way, don't analyze it again.
            resolvedRecipes = MultiCrafterAnalyzer.analyze(this, recipes);
        }
        if (resolvedRecipes == null || resolvedRecipes.isEmpty())
            throw new ArcRuntimeException(MultiCrafterAnalyzer.genName(this) + " has no recipe! It's perhaps because all recipes didn't find items or fluids they need.");
        if (selector == null) {
            selector = RecipeShown.get(menu);
        }
        decorateRecipes();
        setupBlockByRecipes();
        defaultRecipeIndex = Mathf.clamp(defaultRecipeIndex, 0, resolvedRecipes.size - 1);
        recipes = null; // free the recipe Seq, it's useless now.
        setupConsumers();
        super.init();
    }

    @Nullable
    public static Table hoveredInfo;

    public class MultiCrafterBuild extends Building implements HeatBlock, HeatConsumer {
        /**
         * For {@linkplain HeatConsumer}, only enabled when the multicrafter requires heat input
         */
        public float[] sideHeat = new float[4];
        /**
         * For {@linkplain HeatConsumer} and {@linkplain HeatBlock},
         * only enabled when the multicrafter requires heat as input or can output heat.
         * Serialized
         */
        public float heat = 0f;
        /**
         * Serialized
         */
        public float craftingTime;
        /**
         * Serialized
         */
        public float warmup;
        /**
         * Serialized
         */
        public int curRecipeIndex = defaultRecipeIndex;

        public void setCurRecipeIndexFromRemote(int index) {
            curRecipeIndex = Mathf.clamp(index, 0, resolvedRecipes.size - 1);
            if (!Vars.headless) {
                rebuildHoveredInfoIfNeed();
            }
        }

        public Recipe getCurRecipe() {
            // Prevent out of bound
            curRecipeIndex = Mathf.clamp(curRecipeIndex, 0, resolvedRecipes.size - 1);
            return resolvedRecipes.get(curRecipeIndex);
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return hasItems &&
                getCurRecipe().input.itemsUnique.contains(item) &&
                items.get(item) < getMaximumAccepted(item);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return hasLiquids &&
                getCurRecipe().input.fluidsUnique.contains(liquid) &&
                liquids.get(liquid) < liquidCapacity;
        }

        @Override
        public float edelta() {
            Recipe cur = getCurRecipe();
            if (cur.input.power > 0f) {

                return this.efficiency *
                    Mathf.clamp(getCurPowerStore() / cur.input.power) *
                    this.delta();
            } else {
                return this.efficiency * this.delta();
            }
        }

        @Override
        public void updateTile() {
            Recipe cur = getCurRecipe();
            float craftTimeNeed = cur.craftTime;
            // As HeatConsumer
            if (cur.isConsumeHeat()) {
                heat = calculateHeat(sideHeat);
            }
            if (cur.isOutputHeat()) {
                float heatOutput = cur.output.heat;
                heat = Mathf.approachDelta(heat, heatOutput * efficiency, warmupRate * delta());
            }
            if (efficiency > 0 && getCurPowerStore() >= cur.input.power) {
                // if <= 0, instantly produced
                craftingTime += craftTimeNeed > 0 ? edelta() : craftTimeNeed;
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);
                setCurPowerStore((getCurPowerStore() + (cur.output.power - cur.input.power) * delta()));

                //continuously output fluid based on efficiency
                if (cur.isOutputFluid()) {
                    float increment = getProgressIncrease(1f);
                    for (LiquidStack output : cur.output.fluids) {
                        Liquid fluid = output.liquid;
                        handleLiquid(this, fluid, Math.min(output.amount * increment, liquidCapacity - liquids.get(fluid)));
                    }
                }
                // particle fx
                if (wasVisible && Mathf.chanceDelta(updateEffectChance)) {
                    updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
                }
            } else {
                // cool down
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            if (craftingTime >= craftTimeNeed) {
                craft();
            }

            dumpOutputs();
        }

        @Override
        public boolean shouldConsume() {
            Recipe cur = getCurRecipe();
            if (hasItems) {

                for (ItemStack output : cur.output.items) {
                    if (items.get(output.item) + output.amount > itemCapacity) {
                        return false;
                    }
                }
            }

            if (hasLiquids) {
                if (cur.isOutputFluid() && stopProductionWhenFullFluid) {
                    boolean allFull = true;
                    for (LiquidStack output : cur.output.fluids) {
                        if (liquids.get(output.liquid) >= liquidCapacity - 0.001f) {
                            if (!dumpExtraFluid) {
                                return false;
                            }
                        } else
                            allFull = false; //if there's still space left, it's not full for all fluids
                    }

                    //if there is no space left for any fluid, it can't reproduce
                    if (allFull) return false;
                }
            }
            return enabled;
        }

        public void craft() {
            consume();
            Recipe cur = getCurRecipe();
            if (cur.isOutputItem()) {
                for (ItemStack output : cur.output.items) {
                    for (int i = 0; i < output.amount; i++) {
                        offload(output.item);
                    }
                }
            }

            if (wasVisible) {
                craftEffect.at(x, y);
            }
            if (cur.craftTime > 0f)
                craftingTime %= cur.craftTime;
            else
                craftingTime = 0f;
        }

        public void dumpOutputs() {
            Recipe cur = getCurRecipe();
            if (cur.isOutputItem() && timer(timerDump, dumpTime / timeScale)) {
                for (ItemStack output : cur.output.items) {
                    dump(output.item);
                }
            }

            if (cur.isOutputFluid()) {
                Seq<LiquidStack> fluids = cur.output.fluids;
                for (int i = 0; i < fluids.size; i++) {
                    int dir = fluidsOutputDirections.length > i ? fluidsOutputDirections[i] : -1;

                    dumpLiquid(fluids.get(i).liquid, 2f, dir);
                }
            }
        }

        /**
         * As {@linkplain HeatBlock}
         */
        @Override
        public float heat() {
            return heat;
        }

        /**
         * As {@linkplain HeatBlock}
         */
        @Override
        public float heatFrac() {
            Recipe cur = getCurRecipe();
            if (isOutputHeat && cur.isOutputHeat()) {
                return heat / cur.output.heat;
            } else if (isConsumeHeat && cur.isConsumeHeat()) {
                return heat / cur.input.heat;
            }
            return 0f;
        }

        /**
         * As {@linkplain HeatConsumer}
         * Only for visual effects
         */
        @Override
        public float[] sideHeat() {
            return sideHeat;
        }

        /**
         * As {@linkplain HeatConsumer}
         * Only for visual effects
         */
        @Override
        public float heatRequirement() {
            Recipe cur = getCurRecipe();
            // When As HeatConsumer
            if (isConsumeHeat && cur.isConsumeHeat()) {
                return cur.input.heat;
            }
            return 0f;
        }

        @Override
        public void buildConfiguration(Table table) {
            selector.build(MultiCrafter.this, this, table);
        }

        public float getCurPowerStore() {
            return power.status * powerCapacity;
        }

        public void setCurPowerStore(float powerStore) {
            power.status = Mathf.clamp(powerStore / powerCapacity);
        }

        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public Object config() {
            return curRecipeIndex;
        }

        @Override
        public boolean shouldAmbientSound() {
            return efficiency > 0;
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress) return progress();
            //attempt to prevent wild total fluid fluctuation, at least for crafter
            //if(sensor == LAccess.totalLiquids && outputLiquid != null) return liquids.get(outputLiquid.liquid);
            return super.sense(sensor);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(craftingTime);
            write.f(warmup);
            write.i(curRecipeIndex);
            write.f(heat);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            craftingTime = read.f();
            warmup = read.f();
            curRecipeIndex = Mathf.clamp(read.i(), 0, resolvedRecipes.size - 1);
            heat = read.f();
        }

        public float warmupTarget() {
            Recipe cur = getCurRecipe();
            // When As HeatConsumer
            if (isConsumeHeat && cur.isConsumeHeat()) {
                return Mathf.clamp(heat / cur.input.heat);
            } else {
                return 1f;
            }
        }

        @Override
        public void updateEfficiencyMultiplier() {
            Recipe cur = getCurRecipe();
            // When As HeatConsumer
            if (isConsumeHeat && cur.isConsumeHeat()) {
                efficiency *= efficiencyScale();
                potentialEfficiency *= efficiencyScale();
            }
        }

        public float efficiencyScale() {
            Recipe cur = getCurRecipe();
            // When As HeatConsumer
            if (isConsumeHeat && cur.isConsumeHeat()) {
                float heatRequirement = cur.input.heat;
                float over = Math.max(heat - heatRequirement, 0f);
                return Math.min(Mathf.clamp(heat / heatRequirement) + over / heatRequirement * overheatScale, maxEfficiency);
            } else {
                return 1f;
            }
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float progress() {
            Recipe cur = getCurRecipe();
            return Mathf.clamp(cur.craftTime > 0f ? craftingTime / cur.craftTime : 1f);
        }

        @Override
        public void display(Table table) {
            super.display(table);
            hoveredInfo = table;
        }

        public void rebuildHoveredInfoIfNeed() {
            try {
                Table info = hoveredInfo;
                if (info != null) {
                    info.clear();
                    display(info);
                }
            } catch (Exception ignored) {
                // Maybe null pointer or cast exception
            }
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.output, this::buildStats);
    }

    public void buildStats(Table stat) {
        stat.row();
        for (Recipe recipe : resolvedRecipes) {
            Table t = new Table();
            t.background(Tex.whiteui);
            t.setColor(Pal.darkestGray);
            // Input
            buildIOEntry(t, recipe, true);
            // Time
            Table time = new Table();
            t.add(String.format("%.2f", recipe.craftTime / 60f)).color(Color.lightGray);
            t.add(" " + Core.bundle.get("unit.seconds")).color(Color.lightGray).pad(6f);
            t.add(new Image(Icon.right));
            t.add(time).pad(12f);
            // Output
            buildIOEntry(t, recipe, false);
            stat.add(t).pad(10f).grow();
            stat.row();
        }
        stat.row();
        stat.defaults().grow();
    }

    protected void buildIOEntry(Table table, Recipe recipe, boolean isInput) {
        Table t = new Table();
        if (isInput) t.left();
        else t.right();
        Table mat = new Table();
        IOEntry entry = isInput ? recipe.input : recipe.output;
        int i = 0;
        for (ItemStack stack : entry.items) {
            Cell<ItemImage> iconCell = mat.add(new ItemImage(stack.item.uiIcon, stack.amount))
                .pad(2f);
            if (isInput) iconCell.left();
            else iconCell.right();
            if (i != 0 && i % 2 == 0) {
                mat.row();
            }
            i++;
        }
        i++;
        for (LiquidStack stack : entry.fluids) {
            Cell<FluidImage> iconCell = mat.add(new FluidImage(stack.liquid.uiIcon, stack.amount, 60f))
                .pad(2f);
            if (isInput) iconCell.left();
            else iconCell.right();
            if (i != 0 && i % 2 == 0) {
                mat.row();
            }
            i++;
        }
        Cell<Table> matCell = t.add(mat);
        if (isInput) matCell.left();
        else matCell.right();
        t.row();
        // No redundant ui
        // Power
        if (entry.power > 0f) {
            Table power = new Table();
            power.add((isInput ? "-" : "+") + (int) (entry.power * 60f));
            power.image(Icon.power).color(Pal.power);
            if (isInput) power.left();
            else power.right();
            t.add(power).grow().row();
        }
        //Heat
        if (entry.heat > 0f) {
            Table heat = new Table();
            heat.add(isInput ? "-" : "+").color(Pal.accent).pad(6f);
            heat.image(Icon.terrain).color(entry.heat > 0f ? heatColor : Pal.gray);
            if (isInput) heat.left();
            else heat.right();
            t.add(heat).grow().row();
        }
        Cell<Table> tCell = table.add(t).pad(12f).width(120f).fill();
    }

    @Override
    public void setBars() {
        super.setBars();
        if (isConsumeHeat || isOutputHeat) {
            addBar("heat", (MultiCrafterBuild b) -> new Bar("bar.heat", Pal.lightOrange, b::heatFrac));
        }
        removeBar("items");
        if(consPower != null){
            boolean buffered = consPower.buffered;
            float capacity = consPower.capacity;

            addBar("power", entity -> new Bar(
                    () -> buffered ? Core.bundle.format("bar.power", Float.isNaN(entity.power.status * capacity) ? "<ERROR>" : UI.formatAmount((int)(entity.power.status * capacity))) :
                          Core.bundle.get("bar.power"),
                    () -> Pal.powerBar,
                    () -> Mathf.zero(consPower.requestedPower(entity)) && entity.power.graph.getPowerProduced() + entity.power.graph.getBatteryStored() > 0f ? 1f : entity.power.status)
            );
        }
    }

    @Override
    public boolean rotatedOutput(int x, int y) {
        return false;
    }

    @Override
    public void load() {
        super.load();

        drawer.load(this);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        drawer.getRegionsToOutline(this, out);
    }

    @Override
    public boolean outputsItems() {
        return isOutputItem;
    }

    @Override
    public void drawOverlay(float x, float y, int rotation) {
        Recipe firstRecipe = resolvedRecipes.get(defaultRecipeIndex);
        Seq<LiquidStack> fluids = firstRecipe.output.fluids;
        for (int i = 0; i < fluids.size; i++) {
            int dir = fluidsOutputDirections.length > i ? fluidsOutputDirections[i] : -1;

            if (dir != -1) {
                Draw.rect(
                    fluids.get(i).liquid.fullIcon,
                    x + Geometry.d4x(dir + rotation) * (size * tilesize / 2f + 4),
                    y + Geometry.d4y(dir + rotation) * (size * tilesize / 2f + 4),
                    8f, 8f
                );
            }
        }
    }

    protected void decorateRecipes() {
        resolvedRecipes.shrink();
        for (Recipe recipe : resolvedRecipes) {
            recipe.shrinkSize();
            recipe.cacheUnique();
        }
    }

    protected void setupBlockByRecipes() {
        int maxItemAmount = 0;
        float maxFluidAmount = 0f;
        float maxPower = 0f;
        for (Recipe recipe : resolvedRecipes) {
            maxItemAmount = Math.max(recipe.maxItemAmount(), maxItemAmount);
            maxFluidAmount = Math.max(recipe.maxFluidAmount(), maxFluidAmount);
            hasItems |= !recipe.hasItem();
            hasLiquids |= !recipe.hasFluid();
            maxPower = Math.max(recipe.maxPower(), maxPower);
            isOutputItem |= recipe.isOutputItem();
            isConsumeItem |= recipe.isConsumeItem();
            isConsumeFluid |= recipe.isConsumeFluid();
            isConsumeHeat |= recipe.isConsumeHeat();
            isOutputHeat |= recipe.isOutputHeat();
        }
        hasPower = maxPower > 0f;
        outputsPower = hasPower;
        itemCapacity = Math.max((int) (maxItemAmount * itemCapacityMultiplier), itemCapacity);
        liquidCapacity = Math.max((int) (maxFluidAmount * 60f * fluidCapacityMultiplier), liquidCapacity);
        powerCapacity = Math.max(maxPower * 60f * powerCapacityMultiplier, powerCapacity);
        if (isOutputHeat) {
            rotate = true;
            rotateDraw = false;
            canOverdrive = false;
            drawArrow = true;
        }
    }

    protected void setupConsumers() {
        if (isConsumeItem) {
            consume(new ConsumeItemDynamic( // items seq is already shrunk, it's safe to access
                (MultiCrafterBuild b) -> b.getCurRecipe().input.items.items)
            );
        }
        if (isConsumeFluid) {
            ConsumeFluidDynamic.Pair pair = new ConsumeFluidDynamic.Pair();
            pair.displayMultiplier = 60f;
            consume(new ConsumeFluidDynamic(
                (MultiCrafterBuild b) -> {
                    Recipe cur = b.getCurRecipe();
                    pair.fluids = cur.input.fluids.items;
                    pair.displayMultiplier = cur.craftTime;
                    return pair;
                }
            ));
        }
        if (hasPower) {
            consumePowerBuffered(powerCapacity);
        }
    }
}
