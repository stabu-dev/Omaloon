package omaloon.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Load {
    /**
     * The region name to load. Variables can be used:
     * "@" -> block name
     * "@size" -> block size
     * "#" "#1" "#2" -> index number, for arrays
     */
    String value();

    /**
     * 1D Array length, if applicable.
     */
    int length() default 1;

    /**
     * 2D array lengths.
     */
    int[] lengths() default {};

    /**
     * Fallback strings used to replace "@" (the block name) if the region isn't found.
     */
    String[] fallback() default {};
}
