package ol.world.blocks.crafting.multicraft;

public class Recipe {
    public IOEntry input;
    public IOEntry output;
    public float craftTime = 0f;

    public Recipe(IOEntry input, IOEntry output, float craftTime) {
        this.input = input;
        this.output = output;
        this.craftTime = craftTime;
    }

    public Recipe() {
    }

    public void cacheUnique() {
        input.cacheUnique();
        output.cacheUnique();
    }

    public boolean isAnyEmpty() {
        if (input == null || output == null) return true;
        return input.isEmpty() || output.isEmpty();
    }

    public void shrinkSize() {
        input.shrinkSize();
        output.shrinkSize();
    }

    public boolean isOutputFluid() {
        return !output.fluids.isEmpty();
    }

    public boolean isOutputItem() {
        return !output.items.isEmpty();
    }

    public boolean isConsumeFluid() {
        return !input.fluids.isEmpty();
    }

    public boolean isConsumeItem() {
        return !input.items.isEmpty();
    }

    public boolean isConsumeHeat() {
        return input.heat > 0f;
    }
    public boolean isOutputHeat(){
        return output.heat> 0f;
    }
    public boolean hasHeat(){
        return isConsumeHeat() || isOutputHeat();
    }

    public boolean hasItem() {
        return isConsumeItem() || isOutputItem();
    }

    public boolean hasFluid() {
        return isOutputFluid() || isOutputFluid();
    }

    public int maxItemAmount() {
        return Math.max(input.maxItemAmount(), output.maxItemAmount());
    }

    public float maxFluidAmount() {
        return Math.max(input.maxFluidAmount(), output.maxFluidAmount());
    }

    public float maxPower() {
        return Math.max(input.power, output.power);
    }

    public float maxHeat() {
        return Math.max(input.heat, output.heat);
    }

    @Override
    public String toString() {
        return "Recipe{" +
            "input=" + input +
            "output=" + output +
            "craftTime" + craftTime +
            "}";
    }
}
