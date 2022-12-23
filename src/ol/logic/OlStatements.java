package ol.logic;

import arc.scene.ui.layout.Table;
import arc.util.Log;

import mindustry.logic.*;
import mindustry.ui.Styles;

import ol.logic.statements.OlStatement;
import ol.utils.pressure.PressureRenderer;
import java.util.ArrayList;

public class OlStatements {
    //this not example statement, but I can use this statement say how create other statements
    //rule 1: do not use mindustry.annotations.Annotations.RegisterStatement, this call errors on all statement
    //rule 2: to add statement used OlLogicIO.load();
    //rule 3: category must be set (not optimal)
    public static class CommentStatement extends OlStatement {
        //step 1: create vars if statement have fields
        public String comment = "";

        public CommentStatement() {
            super();

            this.name = "Comment";
            this.lCategory = OlLogicIO.comments;
        }

        //all vars must be saved
        //and read using setup method
        //okay not must but if var must be saved
        //when must

        @Override
        public void build(Table table) {
            //step 2: create fields for vars
            table.field(comment, str -> this.comment = str)
                    .growX()
                    .padLeft(2f)
                    .padRight(6f)
                    .color(table.color);

            //table.field used in this cause because this command
            //and need in full screen
            //this.field(table) make only to 1/5
            //field used to set comment (!!!)
        }

        @Override
        public void setup(String[] args, int length) {
            //step 3: load args from buffer
            if(length > 1) {
                this.comment = this.readStr(args[1]);
            }

            //the base
            //...
            //readStr method can give value without "" (if this string)
            //100% works
        }

        @Override
        public void writeArgs(StringBuilder builder) {
            //step 4: save to args
            this.writeStrArg(comment, builder);

            //this method sets args better when anuken cringe method
            //writeStrArg was saves in "" string argument if need in ""
            //this need for example in comments, etc
        }
    }

    public static class LogStatement extends OlStatement {
        public Log.LogLevel type = Log.LogLevel.info;
        public String text = "";

        public LogStatement() {
            this.name = "Logger";
            this.lCategory = OlLogicIO.comments;
        }

        @Override
        public void build(Table table) {
            table.left();
            table.add(" with logger level ");

            table.button(b -> {
                b.label(() -> type.name());
                b.clicked(() -> showSelect(b, Log.LogLevel.values(), type, t -> {
                    this.type = t;

                }, 2, cell -> cell.size(100, 50)));
            }, Styles.logict, () -> {}).size(90, 40).color(table.color).left().padLeft(2);

            table.add(" print message ");
            table.field(text, str -> this.text = str).growX().pad(6f);
        }

        @Override
        public void writeArgs(StringBuilder builder) {
            this.writeArg(type.ordinal(), builder);
            this.writeArg(text, builder);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return exec -> {
                Object obj = this.getStringOrField(text, builder, exec);

                String text = "NULL";
                if(obj != null) {
                    text = obj.toString();
                }

                Log.log(type, text == null ? "NULL" : text);
            };
        }

        @Override
        public void setup(String[] args, int length) {
            if(length > 1) {
                type = Log.LogLevel.values()[this.readInt(args[1])];
            }

            if(length > 2) {
                text = args[2];
            }
        }
    }

    public static class PressureReloadStatement extends OlStatement {
        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return ignored -> PressureRenderer.reload();
        }

        public PressureReloadStatement() {
            super();

            this.id = "prreload";
            this.name = "Pressure reload";
            this.lCategory = OlLogicIO.pressure;
            this.privileged = true;
        }

        @Override
        public void build(Table table) {
            table.add(" [WARNING] the statement at 0 ticks [red]can call lags[]");
        }
    }

    public static class PressureUnlinkStatement extends OlStatement {
        @Override
        public void build(Table table) {
            table.add(" [WARNING] the statement can [red]destroy all pressure system[]");
        }

        public PressureUnlinkStatement() {
            super();

            this.id = "prdelete";
            this.name = "Unlink pressure";
            this.lCategory = OlLogicIO.pressure;
            this.privileged = true;
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return ignored -> PressureRenderer.nets = new ArrayList<>();
        }
    }
}