package ol.pressure.capacity;

/** Сховище тиску яке використовують за умовчанням в блоках, юнітах, планетах і таке інше */
public class PressureCapacity implements IPressureCapacity {
    /** Значення, на яке помножують Math.floor-ене значення тиску, щоб отримати значення яке буде відображено на шкалі блоку */
    public static float BAR_DISPLAY_LIMIT = 0.005f;
    private float capacity, capacityMin, pressure,
            maxExtract, maxReceive;

    /**
     * Перетворює значення тиску на значення яке буде показано в барі блоку
     * @param pressure значення тиску
     * @return OP
     */
    public static int getBarPressure(float pressure) {
        return (int) (Math.floor(pressure) * BAR_DISPLAY_LIMIT);
    }

    public PressureCapacity(float capacity, float capacityMin, float pressure, float maxExtract, float maxReceive) {
        maxPressure(capacity);
        minPressure(capacityMin);
        pressure(pressure);
        maxExtract(maxExtract);
        maxReceive(maxReceive);
    }

    public PressureCapacity(float capacity, float capacityMin, float pressure, float transfer) {
        this(capacity, capacityMin, pressure, transfer, transfer);
    }

    public PressureCapacity(float capacity, float pressure, float transfer) {
        this(capacity, -capacity, pressure, transfer);
    }

    public PressureCapacity(float capacity, float transfer) {
        this(capacity, 0, transfer);
    }

    public PressureCapacity(float capacity) {
        this(capacity, Float.POSITIVE_INFINITY);
    }

    public PressureCapacity() {
        this(Float.POSITIVE_INFINITY);
    }

    @Override
    public void maxPressure(float max) {
        capacity = max;
    }

    @Override
    public float maxPressure() {
        return capacity;
    }

    @Override
    public void minPressure(float min) {
        capacityMin = min;
    }

    @Override
    public float minPressure() {
        return capacityMin;
    }

    @Override
    public void pressure(float pressure) {
        this.pressure = pressure;
    }

    @Override
    public float pressure() {
        return pressure;
    }

    @Override
    public void maxExtract(float extract) {
        this.maxExtract = extract;
    }

    @Override
    public float maxExtract() {
        return maxExtract;
    }

    @Override
    public void maxReceive(float receive) {
        this.maxReceive = receive;
    }

    @Override
    public float maxReceive() {
        return maxReceive;
    }

    @Override
    public float extractPressure(float pressure) {
        if(!canExtract()) {
            return 0;
        }

        return illegalExtractPressure(Math.max(0, Math.min(pressure, maxExtract())));
    }

    @Override
    public float receivePressure(float pressure) {
        if(!canReceive()) {
            return 0;
        }

        return illegalReceivePressure(Math.max(0, Math.min(pressure, maxReceive())));
    }

    @Override
    public float illegalExtractPressure(float pressure) {
        if(pressure == 0 || Float.isNaN(pressure)) {
            return 0;
        } else if(pressure < 0) {
            throw new IllegalStateException("Illegal usage of extract method");
        } else if(pressure >= maxPressure()) {
            float old = pressure();
            pressure(minPressure());
            return old;
        } else {
            float old = pressure();
            pressure(Math.max(pressure() - pressure, minPressure()));
            return old - pressure();
        }
    }

    @Override
    public float illegalReceivePressure(float pressure) {
        if(pressure == 0 || Float.isNaN(pressure)) {
            return 0;
        } else if(pressure < 0) {
            throw new IllegalStateException("Illegal usage of receive method");
        } else if(pressure >= maxPressure()) {
            float old = pressure();
            pressure(maxPressure());
            return pressure() - old;
        } else {
            float old = pressure();
            pressure(Math.min(pressure() + pressure, maxPressure()));
            return pressure() - old;
        }
    }

    @Override
    public boolean canExtract() {
        return maxExtract() > 0;
    }

    @Override
    public boolean canReceive() {
        return maxReceive() > 0;
    }

    @Override
    public int func_30258() {
        return getBarPressure(pressure());
    }
}