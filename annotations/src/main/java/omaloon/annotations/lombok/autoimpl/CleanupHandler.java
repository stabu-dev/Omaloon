package omaloon.annotations.lombok.autoimpl;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.AllArgsConstructor;
import lombok.core.AlreadyHandledAnnotations;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import omaloon.annotations.AutoImplement;
import one.util.streamex.StreamEx;

@AlreadyHandledAnnotations
public class CleanupHandler extends JavacAnnotationHandler<AutoImplement> {

    @Override
    public void handle(AnnotationValues<AutoImplement> annotationValues, JCTree.JCAnnotation jcAnnotation, JavacNode javacNode) {
        JCTree.JCClassDecl decl = (JCTree.JCClassDecl) javacNode.up().get();
        decl.defs=List.from(StreamEx.of(decl.defs).filter(it->!(it instanceof JCTree.JCVariableDecl)));
        for (JCTree def : decl.defs) {
            if(!(def instanceof JCTree.JCMethodDecl methodDecl))continue;
            AutoImplUtil.removeAutoImplAnnos(methodDecl.mods);
            if ((methodDecl.mods.flags& Flags.DEFAULT)==0) continue;
            methodDecl.mods.flags&=~Flags.DEFAULT;
            methodDecl.body=null;
        }
        javacNode.getAst().setChanged();
    }
}
