package bytelogic.lombok.hierarchy;

import bytelogic.lombok.hierarchy.info.ClassInfo;
import bytelogic.lombok.hierarchy.info.InterfaceInfo;
import bytelogic.lombok.util.Util;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import lombok.NonNull;
import lombok.core.HandlerPriority;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacNode;
import lombok.val;

import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.List;

import static bytelogic.lombok.hierarchy.CollectedHierarchyInfo.*;
import static bytelogic.lombok.util.Util.mirror;
import static bytelogic.lombok.util.Util.supertypes;

@HandlerPriority(value = -10, subValue = 0)
public class HierarchyCollector extends JavacASTAdapter {

    Trees trees;

    @Override
    public void visitCompilationUnit(JavacNode top, JCCompilationUnit unit) {
        super.visitCompilationUnit(top, unit);
    }

    @Override
    public void endVisitCompilationUnit(JavacNode top, JCCompilationUnit unit) {
        super.endVisitCompilationUnit(top, unit);
    }

    @Override
    public void visitType(JavacNode typeNode, JCClassDecl type) {
        if (typeNode.getElement() == null) {//Anonymous class
//            System.out.println("Warning: empty element");
            return;
        }
        Type selfMirror = mirror(typeNode);
        List<Type> supertypes = supertypes(typeNode);
        if (!Util.isInterface(type.mods)) {

            Type parent = supertypes.get(0);
            indexHierarchy(parent, selfMirror, typeNode.getTypesUtil());
            ClassInfo selfInfo = getOrCreateClassInfo(selfMirror);
            for (int i = 1; i < supertypes.size(); i++) {
                getOrCreateInterInfo(supertypes.get(i)).addImpl(selfInfo);
            }
            return;
        }


        InterfaceInfo target = getOrCreateInterInfo(selfMirror);

        List<InterfaceInfo> list = supertypes
           .stream()
           .map(this::getOrCreateInterInfo)
           .toList();

        for (val info : list) {
            if (target == info) continue;
            info.addSub(target);
        }

    }

    private void indexHierarchy(@NonNull Type parent, @NonNull Type subtype, JavacTypes typesUtil) {

        ClassInfo parentInfo = getOrCreateClassInfo(parent);
        ClassInfo subInfo = getOrCreateClassInfo(subtype);

        parentInfo.addSub(subInfo);

        if (parent.toString().equals(CollectedHierarchyInfo.JAVA_LANG_OBJECT)) return;
        Type newParent = supertypes(parent, typesUtil).get(0);
        indexHierarchy(newParent, parent, typesUtil);
    }

    private ClassInfo getOrCreateClassInfo(Type mirror) {
        return classInfoMap.computeIfAbsent(mirror.toString(), name -> new ClassInfo(mirror));
    }
    private InterfaceInfo getOrCreateInterInfo(Type mirror) {
        String string = mirror.toString();
        if(string.equals(JAVA_LANG_OBJECT))return interfacesRoot;
        return interfacesInfoMap.computeIfAbsent(string, type -> new InterfaceInfo(mirror));
    }


    @Override
    public void setTrees(Trees trees) {
        this.trees = (trees);

        classInfoMap = new HashMap<>();
        classInfoMap.put(javaLangObject.name, javaLangObject);
        interfacesInfoMap = new HashMap<>();
        interfacesInfoMap.put(interfacesRoot.name,interfacesRoot);
    }
}
