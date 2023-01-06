package ol.logic;

import arc.func.*;
import arc.graphics.*;

import java.util.*;

import arc.struct.*;
import mindustry.gen.*;
import mindustry.graphics.*;

import mindustry.logic.*;

import ol.logic.statements.*;

public class OlLogicIO {
    public static final Seq<Prov<LStatement>> provSeq = new Seq<>();

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

        LogicIO.allStatements.add(statementProv);
        provSeq.add(statementProv);

        if(lStatement instanceof OlStatement olStatement) {
            LAssembler.customParsers.put(olStatement.getId(), OlLogicIO.getReadHandler());
        } else {
            LAssembler.customParsers.put(lStatement.name(), OlLogicIO.getReadHandler());
        }
    }

    public static LStatement read(String[] args, int length) {
        for(Prov<LStatement> statementProv : provSeq) {
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