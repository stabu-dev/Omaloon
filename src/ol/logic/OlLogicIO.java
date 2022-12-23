package ol.logic;

import arc.func.Func;
import arc.func.Prov;
import arc.graphics.Color;

import mindustry.gen.LogicIO;
import mindustry.graphics.Pal;

import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LStatement;

import ol.logic.statements.OlStatement;
import java.util.ArrayList;

public class OlLogicIO {
    public static final ArrayList<Prov<LStatement>> provArrayList = new ArrayList<>();

    public static final LCategory
            pressure = new LCategory("pressure", Pal.heal),
            comments = new LCategory("comments", Color.darkGray);

    public static void load() {
        //pressure
        registerStatement(OlStatements.PressureUnlinkStatement::new);
        registerStatement(OlStatements.PressureReloadStatement::new);

        //comments
        registerStatement(OlStatements.CommentStatement::new);
        registerStatement(OlStatements.LogStatement::new);
    }

    public static Func<String[], LStatement> getReadHandler() {
        return str -> OlLogicIO.read(str, str.length);
    }

    public static void registerStatement(Prov<LStatement> statementProv) {
        if(statementProv == null) {
            return;
        }

        LStatement lStatement = statementProv.get();

        if(lStatement == null) {
            return;
        }

        //init statement and statement parser for him
        LogicIO.allStatements.add(statementProv);
        provArrayList.add(statementProv);

        if(lStatement instanceof OlStatement olStatement) {
            LAssembler.customParsers.put(olStatement.getId(), OlLogicIO.getReadHandler());
        } else {
            LAssembler.customParsers.put(lStatement.name(), OlLogicIO.getReadHandler());
        }
    }

    public static LStatement read(String[] args, int length) {
        for(Prov<LStatement> statementProv : provArrayList) {
            //get statement from element
            LStatement statement = statementProv.get();

            //execute other code for omaloon statements
            if(statement instanceof OlStatement olStatement) {
                if(olStatement.getId().equals(args[0])) {
                    olStatement.setup(args, length);
                    olStatement.afterRead();

                    return olStatement;
                }

                continue;
            }

            //if statement name is found
            if(statement.name().toLowerCase().equals(args[0])) {
                //return statement
                statement.afterRead();
                return statement;
            }
        }

        //execute cringe code if not found
        return LogicIO.read(args, length);
    }
}