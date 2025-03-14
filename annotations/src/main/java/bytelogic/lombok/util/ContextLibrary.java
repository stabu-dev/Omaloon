package bytelogic.lombok.util;

import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import lombok.*;
import lombok.core.*;
import lombok.javac.*;
import org.jetbrains.annotations.*;

import java.util.*;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@With
@Getter
public class ContextLibrary{
    public final JavacNode context;
    public final TypeLibrary library;
    public final JavacResolution resolution;
    public final Map<JCTree, JCTree> resolvedParts;

    public static ContextLibrary makeLib(JavacNode contextNode, TypeLibrary library){
        Context context = contextNode.getContext();
        JavacResolution javacResolution = Util.resolution(context);
        Map<JCTree, JCTree> resolvedParts = null;
        try{

            resolvedParts = javacResolution.resolveMethodMember(contextNode);
        }catch(Throwable e){
            try{
                JavacNode node = contextNode;
                while(node != null){
                    if(node.getKind() == AST.Kind.TYPE)
                        for(JavacNode javacNode : node.down())
                            javacResolution.resolveClassMember(javacNode);
                    node = node.up();
                }
                resolvedParts = javacResolution.resolveMethodMember(contextNode);
            }catch(Throwable ex){

            }

        }
        return new ContextLibrary(
        contextNode,
        library,
        javacResolution,
        resolvedParts
        );
    }

    public static ContextLibrary ofClasses(JavacNode context){
        return makeLib(context, Util.buildClassLibrary(context));
    }

    @Contract("null -> null")
    public String className(JCTree typeRepresentation){
        if(typeRepresentation == null) return null;
        String resolved = resolveFull(typeRepresentation.toString());
        if(resolved == null){
            /*if (resolvedParts==null) {
                try {
                    resolvedParts=resolution.resolveMethodMember(context);
                }catch (Exception i){

                }
            }*/
            if(resolvedParts != null){
                return resolvedParts.get(typeRepresentation).type.toString();
            }
        }
        return resolved;
    }


    public String resolveFull(String string){
        return context.getImportListAsTypeResolver()
                      .typeRefToFullyQualifiedName(context, library, string);
    }
}
