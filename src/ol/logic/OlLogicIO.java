package ol.logic;

import arc.func.Func;
import arc.func.Prov;
import arc.graphics.Color;

import mindustry.gen.LogicIO;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LStatement;

import java.util.ArrayList;

public class OlLogicIO {
    public static final ArrayList<Prov<LStatement>> provArrayList = new ArrayList<>();

    public static final LCategory
            pressure = new LCategory("pressure", Color.green),
            comments = new LCategory("comments", Color.darkGray);

    public static void load() {
        registerStatement("comment",  OlStatements.CommentStatement::new);
        registerStatement("prdelete", OlStatements.PressureUnlinkStatement::new);
        registerStatement("prreload", OlStatements.PressureReloadStatement::new);
        registerStatement("logger",   OlStatements.LogStatement::new);
    }

    public static Func<String[], LStatement> getReadHandler() {
        return str -> OlLogicIO.read(str, str.length);
    }

    public static void registerStatement(String name, Prov<LStatement> statementProv) {
        LogicIO.allStatements.add(statementProv);
        provArrayList.add(statementProv);

        //name need to add to parsers (very need)
        LAssembler.customParsers.put(name, OlLogicIO.getReadHandler());
    }

    public static LStatement read(String[] args, int length) {
        for(Prov<LStatement> statementProv : provArrayList) {
            //get statement from element
            LStatement statement = statementProv.get();

            //if statement name is found
            if(statement.name().toLowerCase().equals(args[0])) {
                //setup statement if this possible
                if(statement instanceof OlStatement olStatement) {
                    olStatement.setup(args, length);
                }

                //return statement
                statement.afterRead();
                return statement;
            }
        }

        //execute cringe code if not found
        return LogicIO.read(args, length);
    }
}