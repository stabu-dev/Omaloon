package omaloon.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface AutoImplement {


    @Target(ElementType.METHOD)
    @interface NoInject {
            Class<?>[] value();
    }


    /**
     * This need ONLY for Annotation Processing, and it will be removed during compilation
     */
    interface Util {
        static <T> T Inject(Class<?> type, Object... args) {
            return error();
        }

        static <T> T Param(String paramName, String defaultExpression) {
            return error();
        }

        static <T> T error() {
            throw new RuntimeException("Why are you calling this?! This need ONLY for Annotation Processing and it will be removed during compilation");
        }


        @interface SetParam {
            String name();
            String value();
        }
    }


    public @interface Inject {
        InjectPosition value();
        boolean shouldAddSuper() default true;
        enum InjectPosition{
            AfterSuper,BeforeSuper,

            Head,Tail;
        }
    }
}
