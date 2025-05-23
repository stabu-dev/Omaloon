package omaloon.annotations.lombok.autoimpl.info;

import bytelogic.lombok.util.*;
import com.sun.tools.javac.tree.*;
import lombok.*;

import java.io.*;

public class InlineApplyPrinter extends Pretty{

    final String expectedMethod;
    final ContextLibrary contextLibrary;
    final Handler handler;

    public InlineApplyPrinter(Writer out, String expectedMethod, ContextLibrary contextLibrary, Handler handler){
        super(out, true);
        this.expectedMethod = expectedMethod;
        this.contextLibrary = contextLibrary;
        this.handler = handler;
    }

    @SneakyThrows
    @Override
    public void visitApply(JCTree.JCMethodInvocation tree){

        String string = contextLibrary.resolveFull(tree.meth.toString());
        if(!expectedMethod.equals(string)){
            super.visitApply(tree);
            return;
        }
        handler.handle(this, tree);
    }

    @Override
    public void printExpr(JCTree tree) throws IOException{
        super.printExpr(tree);
    }

    public interface Handler{
        void handle(InlineApplyPrinter self, JCTree.JCMethodInvocation tree) throws IOException;
    }
}
