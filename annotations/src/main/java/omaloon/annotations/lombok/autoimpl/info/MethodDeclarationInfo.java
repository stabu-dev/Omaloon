package omaloon.annotations.lombok.autoimpl.info;

import bytelogic.lombok.util.*;
import com.sun.tools.javac.tree.*;
import lombok.*;
import lombok.core.*;
import lombok.experimental.*;
import lombok.javac.*;

@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class MethodDeclarationInfo{

    public static final TypeLibrary EMPTY_LIB = new TypeLibrary();
    public String name;
    public final String descriptor;
    @NonNull
    JavacNode node;
    @NonNull
    JCTree.JCMethodDecl decl;
    ContextLibrary contextLibrary;
    public final String symbolString;

    public static MethodDeclarationInfo make(JavacNode methodNode){
        ContextLibrary library = ContextLibrary.makeLib(methodNode, EMPTY_LIB);
        JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl)methodNode.get();
        return new MethodDeclarationInfo(
        methodNode.getName(),
        Util.methodDesc(library, methodDecl),
        methodNode,
        methodDecl,
        library,
        Util.methodSymbolString(library, methodDecl)
        );
    }
}
