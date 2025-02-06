package omaloon.annotations.lombok.load;

import asmlib.lombok.javaparser.*;
import bytelogic.lombok.util.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.List;
import lombok.core.*;
import lombok.javac.*;
import lombok.javac.handlers.*;
import omaloon.annotations.*;

import java.util.*;

//TODO try handle something like @Load(name+"-hello")
//TODO support for delegate function
public class LoadAnnotationASTVisitor extends JavacASTAdapter{
    @Override
    public void visitType(JavacNode typeNode, JCTree.JCClassDecl type){
        super.visitType(typeNode, type);
        ArrayList<FieldDescriptor> loadFields = new ArrayList<>();
        JavacNode loadMethod = null;
        for(JavacNode field : typeNode.down()){
            if(field.getKind() == AST.Kind.METHOD){
                //TODO hierarchy check
                JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl)field.get();
                if(field.getName().equals("load") && methodDecl.getParameters().isEmpty()){
                    loadMethod = field;
                }
                continue;
            }
            if(field.getKind() != AST.Kind.FIELD){
                continue;
            }
            AnnotationValues<Load> annotation = field.findAnnotation(Load.class);
            if(annotation == null) continue;
            loadFields.add(new FieldDescriptor(field, annotation.getInstance()));
        }
        if(loadFields.isEmpty()) return;
        CompileBodyVisitor transformer = Util.transformer(typeNode);
        if(loadMethod == null){
            BlockStmt body = new BlockStmt();
            MethodDeclaration declaration = new MethodDeclaration()
            .setName("load")
            .setType(void.class)
            .addAnnotation(Override.class)
            .setModifiers(Modifier.Keyword.PUBLIC)
            .setBody(body);

            body.addStatement(StaticJavaParser.parseStatement("super.load();"));

            addFields(loadFields, body);

            JCTree.JCMethodDecl methodDecl = transformer.visit(declaration, null);
            JavacHandlerUtil.injectMethod(typeNode, methodDecl);
        }else{
            JCTree.JCMethodDecl decl = (JCTree.JCMethodDecl)loadMethod.get();
            List<JCTree.JCStatement> oldStats = decl.body.stats;

            JCTree.JCBlock newBlock = transformer.visit(addFields(loadFields, new BlockStmt()), null);
            List<JCTree.JCStatement> stats = newBlock.stats;
            decl.body.stats = stats.appendList(oldStats);
            loadMethod.getAst().setChanged();
        }

    }

    private static BlockStmt addFields(java.util.List<FieldDescriptor> loadFields, BlockStmt block){
        for(FieldDescriptor field : loadFields)
            field.addMe(block);
        return block;
    }

}
