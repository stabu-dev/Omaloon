package omaloon.annotations;

import asmlib.annotations.LombokPluginStarter;
import asmlib.annotations.Permit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Source.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.*;
import java.util.*;

import static com.sun.tools.javac.code.Source.Feature.*;

public class Starter extends LombokPluginStarter {
    static{
        initSelf(omaloon.annotations.Starter.class);
    }
    static{
        try{
            // Get the trusted private lookup.
            Lookup lookup = (Lookup) Permit.getField(Lookup.class, "IMPL_LOOKUP").get(null);
            // Get the minimum level setter, to force certain features to qualify as a Java 8 feature.
            MethodHandle set = lookup.findSetter(Feature.class, "minLevel", Source.class);

            // Downgrade most Java 8-compatible features.
            for(Feature feature : new Feature[]{
                EFFECTIVELY_FINAL_VARIABLES_IN_TRY_WITH_RESOURCES,
                PRIVATE_SAFE_VARARGS,
                DIAMOND_WITH_ANONYMOUS_CLASS_CREATION,
                LOCAL_VARIABLE_TYPE_INFERENCE,
                VAR_SYNTAX_IMPLICIT_LAMBDAS,
                SWITCH_MULTIPLE_CASE_LABELS,
                SWITCH_RULE,
                SWITCH_EXPRESSION,
                TEXT_BLOCKS,
                PATTERN_MATCHING_IN_INSTANCEOF,
                REIFIABLE_TYPES_INSTANCEOF
            }) set.invokeExact(feature, Source.JDK8);
        }catch(Throwable t){
            throw new RuntimeException(t);
        }
    }
    @Override
    public String dontForgetToInitSelfInStatic(){
        return "ok";
    }
}
