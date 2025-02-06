package bytelogic.lombok.remove;

import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.*;
import lombok.core.*;
import lombok.javac.*;
import omaloon.annotations.*;

import java.util.function.*;

public class RemoveFromCompilationHandler extends JavacAnnotationHandler<RemoveFromCompilation>{

    @Override
    public void handle(AnnotationValues<RemoveFromCompilation> annotationValues, JCAnnotation jcAnnotation, JavacNode javacNode){
        JavacNode element = javacNode.up();
        JavacNode parent = element.up();
        JCTree o = element.get();
        List<JCTree> defs = null;
        Consumer<List<JCTree>> consumer;
        if(parent.get() instanceof JCClassDecl it){
            defs = it.defs;
            consumer = val -> it.defs = val;
        }else if(parent.get() instanceof JCCompilationUnit it){
            defs = it.defs;
            consumer = val -> it.defs = val;
        }else{
            javacNode.addError("Unsupported parent (" + parent.get().getKind() + ")");
            return;
        }
        List<JCTree> prev = null;
        while(defs != null && defs.head != o){
            prev = defs;
            defs = defs.tail;
        }
        if(defs == null){
            javacNode.addError("Stange, I cant find annotated element in parent");
            return;
        }
        if(prev == null){
            consumer.accept(defs.tail);
            return;
        }
        prev.tail = defs.tail;
    }
}
