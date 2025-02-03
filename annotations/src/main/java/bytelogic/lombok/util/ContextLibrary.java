package bytelogic.lombok.util;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import lombok.core.TypeLibrary;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import org.jetbrains.annotations.Contract;

import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@With
@Getter
public class ContextLibrary {
    public final JavacNode context;
    public final TypeLibrary library;
    public final JavacResolution resolution;
    public final Map<JCTree,JCTree> resolvedParts;

    public static ContextLibrary makeLib(JavacNode contextNode, TypeLibrary library) {
        Context context = contextNode.getContext();
        JavacResolution javacResolution = Util.resolution(context);
        return new ContextLibrary(
            contextNode,
            library,
            javacResolution,
            javacResolution.resolveMethodMember(contextNode)
        );
    }

    public static ContextLibrary ofClasses(JavacNode context) {
        return makeLib(context, Util.buildClassLibrary(context));
    }

    @Contract("null -> null")
    public String className(JCTree typeRepresentation) {
        if (typeRepresentation == null) return null;
        String resolved = resolveFull(typeRepresentation.toString());
        if(resolved==null){
            return resolvedParts.get(typeRepresentation).type.toString();
        }
        return resolved;
    }


    public String resolveFull(String string) {
        return context.getImportListAsTypeResolver()
                      .typeRefToFullyQualifiedName(context, library, string);
    }
}
