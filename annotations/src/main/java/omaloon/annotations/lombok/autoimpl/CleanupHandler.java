package omaloon.annotations.lombok.autoimpl;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import lombok.core.*;
import lombok.javac.*;
import omaloon.annotations.*;
import one.util.streamex.*;

@AlreadyHandledAnnotations
public class CleanupHandler extends JavacAnnotationHandler<AutoImplement>{

    @Override
    public void handle(AnnotationValues<AutoImplement> annotationValues, JCTree.JCAnnotation jcAnnotation, JavacNode javacNode){
        JCTree.JCClassDecl decl = (JCTree.JCClassDecl)javacNode.up().get();
        decl.defs = List.from(StreamEx.of(decl.defs).filter(it -> !(it instanceof JCTree.JCVariableDecl)));
        for(JCTree def : decl.defs){
            if(!(def instanceof JCTree.JCMethodDecl methodDecl)) continue;
            AutoImplUtil.removeAutoImplAnnos(methodDecl.mods);
            if((methodDecl.mods.flags & Flags.DEFAULT) == 0) continue;
            methodDecl.mods.flags &= ~Flags.DEFAULT;
            methodDecl.body = null;
        }
        javacNode.getAst().setChanged();
    }
}
