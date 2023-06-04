package ol.pressure.netting;

import mindustry.gen.Building;

/**
 * Пакет для тиску який використовується для передачі тиску між блоками
 */
public class PressurePackage {
    /** Пакет, який створий цей пакет, він є пакетним власником */
    public PressurePackage ownerPackage;
    /** Власник цього пакету саме він відправив цей пакет */
    public Building owner;
    /** Кількість тиску, який передає пакет */
    public float pressure;

    public PressurePackage(Building owner, float pressure) {
        this(owner, null, pressure);
    }

    public PressurePackage(Building owner, PressurePackage ownerPackage, float pressure) {
        this.owner = owner;
        this.pressure = pressure;
        this.ownerPackage = ownerPackage;
    }

    /**
     * Поділяє пакет тиску на 2 менші пакеті, count завжди буде менше кількості тиску в пакеті
     * @param count кількість тиску яке береться з пакета
     * @return пакет, який був відділений від цього пакета
     */
    public PressurePackage div(float count) {
        count = Math.min(count, pressure);
        pressure -= count;
        return new PressurePackage(owner, count);
    }
}