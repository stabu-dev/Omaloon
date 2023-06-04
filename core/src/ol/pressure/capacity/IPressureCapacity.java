package ol.pressure.capacity;

/**
 * Інтерфейс для усього що має тиск, в ньому є маніпуляція максимального/мінімального тиску,
 * а також Екстракція/Приймання тиску, яку можна маніпулювати
 */
public interface IPressureCapacity {
    void maxPressure(float max);
    void minPressure(float min);
    void pressure(float pressure);
    void maxExtract(float extract);
    void maxReceive(float receive);

    /** @return Максимальне значення тиску */
    float maxPressure();

    /** @return Мінімальне значення тиску */
    float minPressure();

    /** @return Кількість тиску, яке зараз є */
    float pressure();

    /** @return Максимальна кількість тиску, яку можна вивільнити */
    float maxExtract();

    /** @return Максимальна кількість тиску, яку можна прийняти */
    float maxReceive();

    /**
     * Аналогічно illegal методу, але pressure буде більше ніж -1 і менше ніж максимальне вивільнення блоку + 1
     * @param pressure скільки тиску вивільнити
     * @return 0, якщо не може вивільняти, інакше скільки тиску було вивільнено
     */
    float extractPressure(float pressure);

    /**
     * Аналогічно illegal методу, але pressure буде більше ніж -1 і менше ніж максимальне прийняття блоку + 1
     * @param pressure скільки тиску прийняти
     * @return 0, якщо не може прийняти, інакше скільки тиску було прийнято
     */
    float receivePressure(float pressure);

    /**
     * Вивільняє тиск, але може вивільнити більше ніж максимальне вивільнення і неважливо чи може він вивільняти
     * @throws IllegalStateException якщо аргумент pressure < 0
     * @param pressure скільки тиску треба вивільнити
     * @return скільки тиску було вивільнено
     */
    float illegalExtractPressure(float pressure);

    /**
     * Приймає тиск, але може прийняти більше ніж максимальне прийняття і неважливо чи може він прийняти
     * @throws IllegalStateException якщо аргумент pressure < 0
     * @param pressure скільки тиску треба прийняти
     * @return скільки тиску було прийнято
     */
    float illegalReceivePressure(float pressure);

    /** @return Значення яке дорівнює тому, чи може блок вивільняти тиск */
    boolean canExtract();
    /** @return Значення яке дорівнює тому, чи може блок прийняти тиск */
    boolean canReceive();

    /** @return Значення тиску, але в барі блоку */
    default int func_30258() { return 0; };
}