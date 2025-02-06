package bytelogic.lombok.util;

import asmlib.lombok.javaparser.*;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.model.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import lombok.*;
import lombok.core.*;
import lombok.javac.*;
import lombok.javac.handlers.*;
import one.util.streamex.*;
import org.jetbrains.annotations.*;

import javax.lang.model.type.*;
import java.lang.reflect.*;
import java.util.List;
import java.util.*;

import static com.sun.tools.javac.code.Flags.INTERFACE;

public class Util{

    public static List<Type> supertypes(JavacNode typeNode){
        return supertypes(mirror(typeNode), typeNode.getTypesUtil());
    }

    public static List<Type> supertypes(TypeMirror mirror, JavacTypes typesUtil){
        if(mirror != null && mirror.toString().equals("java.lang.Object")) return List.of();
        return typesUtil.directSupertypes(mirror);
    }

    public static Type mirror(JavacNode typeNode){
        return (Type)typeNode.getElement().asType();
    }

    public static boolean isInterface(JCTree.JCModifiers modifiers){
        return (modifiers.flags & INTERFACE) != 0;
    }

    public static Type supertype(JavacNode typeNode){
        return supertypes(typeNode).get(0);
    }

    @Contract("_,null->null;_, !null -> !null")
    public static String methodDesc(@NonNull JavacNode context, JCTree.JCMethodDecl decl){
        return methodDesc(ContextLibrary.ofClasses(context), decl);
    }

    @Contract("_, null->null; _, !null -> !null")
    public static String methodDesc(ContextLibrary library, JCTree.JCMethodDecl decl){
        if(decl == null) return null;
        String ret = library.className(decl.getReturnType());
        String params = StreamEx.of(decl.getParameters())
                                .map(JCTree.JCVariableDecl::getType)
                                .map(library::className)
                                .joining(";", "(", ")");
        return decl.name.toString() + params + ";" + ret;
    }

    @SneakyThrows
    @NotNull
    public static TypeLibrary buildClassLibrary(@NotNull JavacNode context){
        TypeLibrary library = new TypeLibrary();
        StreamEx.of(context)
                .map(JavacNode::getSymbolTable)
                .map(Symtab::getAllClasses)
                .map(Iterable::iterator)
                .flatMap(StreamEx::of)
                .map(it -> it.flatname.toString())
                .forEach(library::addType);
        String[] primitives = {
        "byte", "short", "char", "int", "long",
        "float", "double",
        "boolean",
        "void"
        };
        Field field = TypeLibrary.class.getDeclaredField("unqualifiedToQualifiedMap");
        field.setAccessible(true);
        //noinspection unchecked
        final Map<String, Object> unqualifiedToQualifiedMap = (Map<String, Object>)field.get(library);

        for(String primitive : primitives){
            unqualifiedToQualifiedMap.put(primitive, primitive);
        }
        library.lock();
        return library;
    }

    @Contract("null -> null; !null -> !null")
    public static JavacNode extractCompilationUnit(JavacNode javacNode){
        if(javacNode == null) return null;
        return javacNode.top();
    }

    public static String canonicalFullname(JavacNode typeNode){
        return mirror(typeNode).toString();
    }

    public static String reflectionFullname(JavacNode typeNode){
        return mirror(typeNode).tsym.flatName().toString();
    }

    @NotNull
    public static CompileBodyVisitor transformer(JavacNode typeNode){
        return new CompileBodyVisitor(
        typeNode.getTreeMaker(),
        typeNode.getAst(),
        typeNode.getContext()
        );
    }

    @NotNull
    public static JavacResolution resolution(Context context){
        JavacResolution javacResolution = context.get(JavacResolution.class);
        if(javacResolution == null){
            context.put(JavacResolution.class, javacResolution = new JavacResolution(context));
        }
        return javacResolution;
    }

    public static <U extends JCTree> U resolveSym(JavacNode node){
        JavacResolution resolution = resolution(node.getContext());
        //noinspection unchecked
        return (U)resolution.resolveMethodMember(node).get(node.get());

    }

    public static <T extends JCTree> T setGeneratedBy(T node, JavacNode contextNode, JCTree sourceTree){
        if(node == null) return null;
        if(sourceTree == null){
            JavacAugments.JCTree_generatedNode.clear(node);
            return node;
        }
        JavacAugments.JCTree_generatedNode.set(node, sourceTree);
        if(JavacAugments.JCTree_keepPosition.get(node)){
            return node;
        }
        if(JavacHandlerUtil.inNetbeansEditor(contextNode.getContext()) && !JavacHandlerUtil.isParameter(node)){
            return node;
        }
        node.pos = sourceTree.getPreferredPosition();
        Javac.storeEnd(node, contextNode.getEndPosition(sourceTree), (JCTree.JCCompilationUnit)(contextNode.top()).get());
        return node;

    }

    public static String methodSymbolString(ContextLibrary library, JCTree.JCMethodDecl decl){
        if(decl == null) return null;

        String params = StreamEx.of(decl.getParameters())
                                .map(JCTree.JCVariableDecl::getType)
                                .map(library::className)
                                .joining(",", "(", ")");
        ;
        return decl.name.toString() + params;
    }
}
