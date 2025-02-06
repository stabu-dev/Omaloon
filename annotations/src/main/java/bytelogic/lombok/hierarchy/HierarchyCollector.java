package bytelogic.lombok.hierarchy;

import bytelogic.lombok.hierarchy.info.*;
import bytelogic.lombok.util.*;
import com.sun.source.util.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.model.*;
import com.sun.tools.javac.tree.JCTree.*;
import lombok.*;
import lombok.core.*;
import lombok.javac.*;

import java.util.*;

import static bytelogic.lombok.hierarchy.CollectedHierarchyInfo.*;
import static bytelogic.lombok.util.Util.*;

@HandlerPriority(value = -10, subValue = 0)
public class HierarchyCollector extends JavacASTAdapter{

    Trees trees;

    @Override
    public void visitCompilationUnit(JavacNode top, JCCompilationUnit unit){
        super.visitCompilationUnit(top, unit);
    }

    @Override
    public void endVisitCompilationUnit(JavacNode top, JCCompilationUnit unit){
        super.endVisitCompilationUnit(top, unit);
    }

    @Override
    public void visitType(JavacNode typeNode, JCClassDecl type){
        if(typeNode.getElement() == null){//Anonymous class
//            System.out.println("Warning: empty element");
            return;
        }
        Type selfMirror = mirror(typeNode);
        List<Type> supertypes = supertypes(typeNode);
        if(!Util.isInterface(type.mods)){

            Type parent = supertypes.get(0);
            indexHierarchy(parent, selfMirror, typeNode.getTypesUtil());
            ClassInfo selfInfo = getOrCreateClassInfo(selfMirror);
            for(int i = 1; i < supertypes.size(); i++){
                getOrCreateInterInfo(supertypes.get(i)).addImpl(selfInfo);
            }
            return;
        }


        InterfaceInfo target = getOrCreateInterInfo(selfMirror);

        List<InterfaceInfo> list = supertypes
        .stream()
        .map(this::getOrCreateInterInfo)
        .toList();

        for(val info : list){
            if(target == info) continue;
            info.addSub(target);
        }

    }

    private void indexHierarchy(@NonNull Type parent, @NonNull Type subtype, JavacTypes typesUtil){

        ClassInfo parentInfo = getOrCreateClassInfo(parent);
        ClassInfo subInfo = getOrCreateClassInfo(subtype);

        parentInfo.addSub(subInfo);

        if(parent.toString().equals(CollectedHierarchyInfo.JAVA_LANG_OBJECT)) return;
        Type newParent = supertypes(parent, typesUtil).get(0);
        indexHierarchy(newParent, parent, typesUtil);
    }

    private ClassInfo getOrCreateClassInfo(Type mirror){
        return classInfoMap.computeIfAbsent(mirror.toString(), name -> new ClassInfo(mirror));
    }

    private InterfaceInfo getOrCreateInterInfo(Type mirror){
        String string = mirror.toString();
        if(string.equals(JAVA_LANG_OBJECT)) return interfacesRoot;
        return interfacesInfoMap.computeIfAbsent(string, type -> new InterfaceInfo(mirror));
    }


    @Override
    public void setTrees(Trees trees){
        this.trees = (trees);

        classInfoMap = new HashMap<>();
        classInfoMap.put(javaLangObject.name, javaLangObject);
        interfacesInfoMap = new HashMap<>();
        interfacesInfoMap.put(interfacesRoot.name, interfacesRoot);
    }
}
