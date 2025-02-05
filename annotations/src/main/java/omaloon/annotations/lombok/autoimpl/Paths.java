package omaloon.annotations.lombok.autoimpl;

import lombok.core.TypeLibrary;
import lombok.javac.JavacNode;
import omaloon.annotations.AutoImplement;

public class Paths {
    public static final String PARAM_METHOD = AutoImplement.Util.class.getCanonicalName() + ".Param";
    public static final String INJECT_METHOD = AutoImplement.Util.class.getCanonicalName() + ".Inject";
    public static final TypeLibrary library = new TypeLibrary() {{
        addType(AutoImplement.Util.class.getName() + "$Param");
        addType(AutoImplement.Util.class.getName() + "$Inject");
        lock();
    }};

    public static boolean expressionMatch(JavacNode context, String expressionToCheck, String expected) {
        String resolved = fullifyName(context, expressionToCheck, library);
        return resolved != null && resolved.equals(expected);
    }

    public static String fullifyName(JavacNode context, String expressionToUnwrap, TypeLibrary library1) {
        return context
            .getImportListAsTypeResolver()
            .typeRefToFullyQualifiedName(context, library1, expressionToUnwrap);
    }
}
