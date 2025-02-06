package omaloon.annotations.lombok.autoimpl;

import bytelogic.lombok.hierarchy.*;
import bytelogic.lombok.hierarchy.info.*;
import bytelogic.lombok.util.*;
import com.sun.source.util.*;
import com.sun.tools.javac.tree.JCTree.*;
import lombok.core.*;
import lombok.javac.*;

import java.util.*;

import static bytelogic.lombok.util.Util.mirror;

//0b10000000000000000000000000000010
@HandlerPriority(value = -5)
@ResolutionResetNeeded
public class HierarchyMarker extends JavacASTAdapter{
    public static InfoKey<Boolean> hasSavingProcKey = new InfoKey<>();
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
        if(typeNode.getElement() == null) return;//Anonymous classes
        if(!typeNode.getName().equals("CustomSaveBuilding") || !Util.isInterface(type.mods)) return;

        InterfaceInfo interfaceInfo = CollectedHierarchyInfo.interfaceInfo(mirror(typeNode).toString());
        ArrayList<ClassInfo> implementedClasses = CollectedHierarchyInfo.collectAllImpl(interfaceInfo);

        for(ClassInfo info : implementedClasses)
            info.put(hasSavingProcKey, true);

    }

    @Override
    public void setTrees(Trees trees){
        this.trees = (trees);
    }
}
