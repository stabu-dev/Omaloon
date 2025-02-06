package omaloon.annotations.lombok.autoimpl;

import bytelogic.lombok.hierarchy.CollectedHierarchyInfo;
import bytelogic.lombok.hierarchy.info.ClassInfo;
import bytelogic.lombok.hierarchy.info.InfoKey;
import bytelogic.lombok.hierarchy.info.InterfaceInfo;
import bytelogic.lombok.util.Util;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import lombok.core.HandlerPriority;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacNode;
import lombok.javac.ResolutionResetNeeded;

import java.util.ArrayList;

import static bytelogic.lombok.util.Util.mirror;

//0b10000000000000000000000000000010
@HandlerPriority(value = -5)
@ResolutionResetNeeded
public class HierarchyMarker extends JavacASTAdapter {
    public static InfoKey<Boolean> hasSavingProcKey = new InfoKey<>();
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
        if (typeNode.getElement() == null) return;//Anonymous classes
        if (!typeNode.getName().equals("CustomSaveBuilding") || !Util.isInterface(type.mods)) return;

        InterfaceInfo interfaceInfo = CollectedHierarchyInfo.interfaceInfo(mirror(typeNode).toString());
        ArrayList<ClassInfo> implementedClasses = CollectedHierarchyInfo.collectAllImpl(interfaceInfo);

        for (ClassInfo info : implementedClasses)
            info.put(hasSavingProcKey, true);

    }

    @Override
    public void setTrees(Trees trees) {
        this.trees = (trees);
    }
}
