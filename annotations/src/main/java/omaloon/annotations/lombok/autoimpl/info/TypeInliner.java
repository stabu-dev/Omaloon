package omaloon.annotations.lombok.autoimpl.info;

import bytelogic.lombok.util.Util;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import lombok.javac.handlers.JavacHandlerUtil;
import org.jetbrains.annotations.Nullable;

public class TypeInliner extends TreeCopier<Void> {

    private final JavacNode context;
    private TreeMaker M;

    /**
     * Creates a new instance of TreeCopier
     *
     * @param M
     * @param context
     */
    public TypeInliner(TreeMaker M, JavacNode context) {
        super(M);
        this.M = M;
        this.context = context;
    }

    public static <T extends JCTree> T copyWithInlineTypes(JavacNode javacNode) {
        T resolved = Util.resolveSym(javacNode);
        TypeInliner inliner = new TypeInliner(javacNode.getTreeMaker().getUnderlyingTreeMaker(), javacNode);
        return inliner.copy(resolved);
    }
    public static <T extends JCTree> T copyWithInlineTypes(JavacNode context,T tree) {
        JavacResolution resolution = Util.resolution(context.getContext());
        //noinspection unchecked
        T resolved = (T) resolution.resolveMethodMember(context).get(tree);
        return
            new TypeInliner(context.getTreeMaker().getUnderlyingTreeMaker(), context)
                .copy(resolved);
    }


    @Override
    public <T extends JCTree> T copy(T tree, Void unused) {
        if (tree instanceof JCFieldAccess fieldAccess) {
            T new_ = unwrap(tree, fieldAccess.sym);
            if (new_ != tree) return new_;

        }
        if (tree instanceof JCTree.JCIdent ident) {
            T new_ = unwrap(tree, ident.sym);
            if (new_ != tree) return new_;
        }
        return super.copy(tree, unused);
    }

    @Nullable
    private <T extends JCTree> T unwrap(T tree, Symbol sym) {

        if (!(sym instanceof Symbol.ClassSymbol classSymbol)) return tree;
        if (classSymbol.name.isEmpty()) {
            //Anonymous
            //TO debug
            throw new RuntimeException("To debug");
        } else {
            String string = classSymbol.className();

            JCFieldAccess jcFieldAccess = (JCFieldAccess) JavacHandlerUtil.chainDots(context, string.split("\\."));
            jcFieldAccess.sym = sym;
            jcFieldAccess.pos = tree.pos;
            //noinspection unchecked
            return (T) jcFieldAccess;
        }
    }


    private <T extends JCTree> T cast(Tree node) {
        //noinspection unchecked
        return (T) node;
    }

    @Override
    public JCTree visitMethodInvocation(MethodInvocationTree node, Void unused) {
        return super.visitMethodInvocation(node, unused);
    }

    @Override
    public <T extends JCTree> List<T> copy(List<T> trees, Void unused) {
        return super.copy(trees, unused);
    }
}

