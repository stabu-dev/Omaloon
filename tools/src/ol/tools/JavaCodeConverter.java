package ol.tools;

import arc.files.Fi;
import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Log;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;

import java.util.List;
import java.util.Optional;

public class JavaCodeConverter {
    JavaParser javaParser = new JavaParser();
private boolean logProblems=true;

    public JavaCodeConverter() {
    }
    public JavaCodeConverter(boolean logProblems) {
        this.logProblems = logProblems;
    }

    private static Expression binaryFromIf(InstanceOfExpr inst, PatternExpr pattern) {
        Expression expression = inst.getExpression();
        AssignExpr assignExpr = new AssignExpr(new NameExpr(pattern.getName()), new CastExpr(pattern.getType(), expression), AssignExpr.Operator.ASSIGN);
        BinaryExpr binaryExpr = new BinaryExpr(new EnclosedExpr(assignExpr), expression, BinaryExpr.Operator.EQUALS);
        //((Class)e=obj)==obj)
        return new EnclosedExpr(new BinaryExpr(inst, binaryExpr, BinaryExpr.Operator.AND));
    }

    public static void main(String... args) {
        Fi fi = Fi.get("debug/compJava");
        String name =
//                "CommanderComp"
//                "EntityComp"
//                "PayloadComp"
                "UnitComp"
                ;
        Fi child = fi.child(name + ".java");
        JavaCodeConverter codeConverter = new JavaCodeConverter();
        String convert = codeConverter.convert(child.readString(), child.nameWithoutExtension());
        fi.child(name + "--.java").writeString(convert);
    }

    private Seq<Expression> getAll(Seq<Expression> exceptionSeq, BinaryExpr binaryExpr) {
        if (exceptionSeq == null) exceptionSeq = new Seq<>();
        Expression left = openExpression(binaryExpr.getLeft());
        Expression right = openExpression(binaryExpr.getRight());
        if (left.isBinaryExpr()) {
            getAll(exceptionSeq, left.asBinaryExpr());
        } else exceptionSeq.add(left);
        if (right.isBinaryExpr()) {
            getAll(exceptionSeq, right.asBinaryExpr());
        } else exceptionSeq.add(right);
        return exceptionSeq;
    }

    public String convert(String code, String className) {
        StringBuilder imports = new StringBuilder();
        for (String s : code.split("\n")) {
            if (!s.replace(" ", "").startsWith("@") || s.contains(" class ")) {
                imports.append(s).append("\n");
            } else {
                break;
            }
        }
        ParseResult<CompilationUnit> parseR = javaParser.parse(code);
        if (logProblems){
            for (Problem problem : parseR.getProblems()) {
                Log.info("problem: @", problem.toString());
            }
        }
        CompilationUnit parse = parseR.getResult().get();
        Optional<ClassOrInterfaceDeclaration> classOpt = parse.getClassByName(className);
        if (!classOpt.isPresent()) {
            Log.info("class @ cannot find in @", className);
            return null;
        }
        ClassOrInterfaceDeclaration class_ = classOpt.get();
        List<MethodDeclaration> methods = class_.getMethods();
        for (int i = 0; i < methods.size(); i++) {
            MethodDeclaration method = methods.get(i);
            if (method.isAbstract()) continue;
            Optional<BlockStmt> bodyOpt = method.getBody();
            if (bodyOpt.isPresent()) {
                BlockStmt body = bodyOpt.get();
                NodeList<Statement> statements = body.getStatements();
                Seq<Statement> newStatements = new Seq<>();
                for (int j = 0; j < statements.size(); j++) {
                    Statement statement = statements.get(j);
                    Seq<Statement> check = check(statement);
                    if (check.size > 1 && check.first().toString().contains("LogicAI ai;")) {
                        NodeList<Statement> nodeList = new NodeList<>();
                        for (Statement b : check) {
                            nodeList.add(b);
                        }
                        newStatements.add(new BlockStmt(nodeList));
                    } else {
                        newStatements.addAll(check);
                    }
//                    newStatements.addAll(check);
                }
                NodeList<Statement> nodeList = new NodeList<>();
                for (Statement newStatement : newStatements) {
                    nodeList.add(newStatement);
                }
                body.setStatements(nodeList);
            }
        }
        return imports.toString() + "\n" + class_.toString();
    }

    private Seq<Statement> check(Statement statement) {
        return check(statement, true);
    }

    private Seq<Statement> check(Statement statement, boolean add) {
        Seq<Statement> statements = new Seq<>();
        if (statement.isIfStmt()) {
            IfStmt ifStmt = statement.asIfStmt();
            Expression condition = ifStmt.getCondition();
            chechIfStmt(statements, ifStmt, condition);
            Optional<Statement> elseStmtOpt = ifStmt.getElseStmt();
            if (elseStmtOpt.isPresent()) {
                Statement elseStmt = elseStmtOpt.get();
                statements.addAll(check(elseStmt,false));
            }
            Statement thenStmt = ifStmt.getThenStmt();
            statements.addAll(check(thenStmt, false));
//            NodeList<Statement> nodeList = new NodeList<>();
//            nodeList.add(statement);
//       statement=     new BlockStmt(nodeList);
//                        if (condition.toString().contains("instanceof "))
        } else if (statement.isExpressionStmt()) {
            ExpressionStmt expressionStmt = statement.asExpressionStmt();
            Expression expression = expressionStmt.getExpression();
            chechIfStmt(statements, null, expression);
        } else if (statement.isBlockStmt()) {
            BlockStmt blockStmt = statement.asBlockStmt();
            for (Statement stmtStatement : blockStmt.getStatements()) {
                statements.addAll(check(stmtStatement, false));
            }
        } else if (statement.isReturnStmt()) {
            ReturnStmt returnStmt = statement.asReturnStmt();
            Optional<Expression> expressionOpt = returnStmt.getExpression();
            if (expressionOpt.isPresent()) {
                Expression expression = expressionOpt.get();
                if (expression.isSwitchExpr()) {
                    SwitchExpr switchExpr = expression.asSwitchExpr();
                    for (SwitchEntry entry : switchExpr.getEntries()) {
                        NodeList<Statement> list = entry.getStatements();
                        NodeList<Statement> nstatements = new NodeList<>(list);
                        for (int i = 0; i < list.size(); i++) {
//                                new ExpressionStmt()
                            ReturnStmt element = new ReturnStmt(list.get(i).asExpressionStmt().getExpression());
                            chechIfStmt(statements, element, element.getExpression().get());
                            nstatements.set(i, element);
                        }
                        entry.setStatements(nstatements);
                    }

//                    statement = new ExpressionStmt(switchExpr);
                    statement=new SwitchStmt(switchExpr.getSelector(),switchExpr.getEntries());
//                        returnStmt.setExpression(new NameExpr("return__"));
                } else {
                    chechIfStmt(statements,returnStmt,expression);
                }
            }
        }
        if (add) statements.add(statement);
        return statements;
    }

    private Seq<Statement> chechIfStmt(Seq<Statement> statements, Statement statement, final Expression condition) {
        if (condition.toString().contains("instanceof")) {
            Expression condition_ = openExpression(condition);
            if (condition_.isNameExpr() || condition_.isFieldAccessExpr()) {
                return statements;
            } else if (condition_.isLambdaExpr()) {

                LambdaExpr lambdaExpr = condition_.asLambdaExpr();
                Statement body = lambdaExpr.getBody();
                if (body.isBlockStmt()) {
                    Seq<Statement> check = check(body, false);
                    statements.addAll(check);
                } else if (body.isExpressionStmt()) {
                    Seq<Statement> seq = new Seq<>();
                    Expression expression = body.asExpressionStmt().getExpression();
                    chechIfStmt(seq, null, expression);
                    if (seq.any()) {


                        NodeList<Statement> nodeList = new NodeList<>();
                        for (Statement statement1 : seq) {
                            nodeList.add(statement1);
                        }
                        nodeList.add(new ReturnStmt(expression));
                        lambdaExpr.setBody(new BlockStmt(nodeList));
                    }
                }
            } else if (condition_.isConditionalExpr()) {
                ConditionalExpr condExpr = condition_.asConditionalExpr();
                Expression exprCondition = condExpr.getCondition();
                Expression exprElse = condExpr.getElseExpr();
                Expression exprThen = condExpr.getThenExpr();
                if (exprCondition.isInstanceOfExpr()) {
                    InstanceOfExpr inst = exprCondition.asInstanceOfExpr();
                    tranformIf(statements, inst, condExpr::setCondition);
                } else chechIfStmt(statements, null, exprCondition);

                if (exprElse.isInstanceOfExpr()) {
                    InstanceOfExpr inst = exprElse.asInstanceOfExpr();
                    tranformIf(statements, inst, condExpr::setCondition);
                } else chechIfStmt(statements, null, exprElse);

                if (exprThen.isInstanceOfExpr()) {
                    InstanceOfExpr inst = exprThen.asInstanceOfExpr();
                    tranformIf(statements, inst, condExpr::setCondition);
                } else chechIfStmt(statements, null, exprThen);
            } else if (condition_.isMethodCallExpr()) {
                MethodCallExpr methodCallExpr = condition_.asMethodCallExpr();
                for (int i = 0; i < methodCallExpr.getArguments().size(); i++) {
                    Expression expr = methodCallExpr.getArgument(i);
                    if (expr.isInstanceOfExpr()) {
                        InstanceOfExpr inst = expr.asInstanceOfExpr();
                        int j = i;
                        tranformIf(statements, inst, b -> methodCallExpr.setArgument(j, b));
                    } else {
                        chechIfStmt(statements, null, expr);
                    }
                }
                return statements;
            } else if (condition_.isBinaryExpr()) {
                BinaryExpr binaryExpr = condition_.asBinaryExpr();
                {
                    Expression expr = openExpression(binaryExpr.getLeft());
                    if (expr.isInstanceOfExpr()) {
                        InstanceOfExpr inst = expr.asInstanceOfExpr();
                        tranformIf(statements, inst, binaryExpr::setLeft);
                    } else chechIfStmt(statements, null, expr);
                }
                {
                    Expression expr = openExpression(binaryExpr.getRight());
                    if (expr.isInstanceOfExpr()) {
                        InstanceOfExpr inst = expr.asInstanceOfExpr();
                        tranformIf(statements, inst, binaryExpr::setRight);
                    } else chechIfStmt(statements, null, expr);
                }
//                chechIfStmt(statements, e);

            } else if (condition_.isEnclosedExpr()) {
                EnclosedExpr expr = condition_.asEnclosedExpr();
                if (expr.getInner().isInstanceOfExpr()) {
                    InstanceOfExpr inst = expr.getInner().asInstanceOfExpr();
                    tranformIf(statements, inst, expr::setInner);
                }
            } else if (condition_.isUnaryExpr()) {
                UnaryExpr unaryExpr = condition_.asUnaryExpr();
                if (unaryExpr.getExpression().isInstanceOfExpr()) {
                    InstanceOfExpr inst = unaryExpr.getExpression().asInstanceOfExpr();
                    tranformIf(statements, inst, unaryExpr::setExpression);
                }
            } else if (condition_.isInstanceOfExpr()) {
                if (statement == null) {
                    throw new RuntimeException("cannot change (parent: " + condition_.getParentNode() + ")");
                }
                InstanceOfExpr inst = condition_.asInstanceOfExpr();
                if (statement.isIfStmt()) {
                    IfStmt ifStmt = statement.asIfStmt();
                    tranformIf(statements, inst, ifStmt::setCondition);
                }else if (statement.isReturnStmt()){
                    ReturnStmt returnStmt = statement.asReturnStmt();
                    tranformIf(statements,inst,returnStmt::setExpression);
                } else {
                    throw new RuntimeException("have not handler for "+statement.getMetaModel());
                }


            }
            if (statement != null) {
                 if (statement.isReturnStmt()) {
                    ReturnStmt returnStmt = statement.asReturnStmt();
                    Optional<Expression> expressionOpt = returnStmt.getExpression();
                    if (expressionOpt.isPresent()) {
                        Expression expression = expressionOpt.get();
                        if (expression.isInstanceOfExpr()) {
//                            chechIfStmt(statements, null, expression);
                            InstanceOfExpr inst = expression.asInstanceOfExpr();
                            tranformIf(statements, inst, returnStmt::setExpression);
                        }
                    }

                }
            }
            return statements;
//                if (condition)

//                            if ()
        }

        return statements;
    }

    private void tranformIf(Seq<Statement> statements, InstanceOfExpr inst, Cons<Expression> cons) {
        Optional<PatternExpr> patternOpt = inst.getPattern();
        if (patternOpt.isPresent()) {
            PatternExpr pattern = patternOpt.get();
            inst.setPattern(null);
            statements.add(new ExpressionStmt(pattern.clone()));
            cons.get(binaryFromIf(inst, pattern));
        }
    }

    private Expression openExpression(Expression condition) {
        while (true) {
            if (condition.isUnaryExpr()) {
                UnaryExpr unaryExpr = condition.asUnaryExpr();
                Expression expression = unaryExpr.getExpression();
                if (expression.isInstanceOfExpr()) break;
                condition = expression;
            } else if (condition.isEnclosedExpr()) {
                EnclosedExpr enclosedExpr = condition.asEnclosedExpr();
                Expression expression = enclosedExpr.getInner();
                if (expression.isInstanceOfExpr()) break;
                condition = expression;
            } else break;
        }
        return condition;
    }
}
