package ol.pressure.netting;

import java.lang.annotation.*;

/**
 * Якщо будівля має цю анотацію, то вона буде в системі тиск
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IncludeToTheNet {
    /* Якщо true, то цей блок має тиск, false якщо не має тиску, а
    ле є частиною системи тиску (наприклад: контролер, перехрестя, труби і мости) */
    boolean inHostNet();
}
