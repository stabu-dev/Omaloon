package ol.logic;

import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import ol.logic.OlLogicIO;

public abstract class OlStatement extends LStatement {
    public LCategory lCategory = super.category();

    public boolean
            privileged    = super.privileged(),
            nonPrivileged = super.nonPrivileged(),
            hidden        = super.hidden();

    public String name;

    @Override
    public boolean hidden() {
        return hidden;
    }

    /**
     * Privileged instructions are only allowed in world processors.
     */
    @Override
    public boolean privileged() {
        return privileged;
    }

    /**
     * If true, this statement is considered useless with privileged processors and is not allowed in them.
     */
    @Override
    public boolean nonPrivileged() {
        return nonPrivileged;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append(name());
        this.writeArgs(builder);
    }

    public OlStatement() {
        super();
    }

    @Override
    public LCategory category() {
        return lCategory;
    }

    public void writeArgs(StringBuilder builder) {
    }

    public void setup(String[] args, int length) {
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return ignored -> {};
    }

    public void writeStrArg(String str, StringBuilder builder) {
        if(builder == null) {
            return;
        }

        builder.append(" \"").append(str).append("\"");
    }

    public String readStr(String str) {
        return str.substring(1, str.length() - 1);
    }

    public int readInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch(Exception ignored) {
            return 0;
        }
    }

    public float readFloat(String str) {
        try {
            return Float.parseFloat(str);
        } catch(Exception ignored) {
            return Float.NaN;
        }
    }

    public void writeArg(Object obj, StringBuilder builder) {
        if(builder == null) {
            return;
        }

        if(obj == null) {
            builder.append(" null");
            return;
        }

        if(obj.toString().isEmpty()) {
            builder.append(" empty");
            return;
        }

        builder.append(" ").append(toString());
    }

    @Override
    public String name() {
        return name;
    }
}